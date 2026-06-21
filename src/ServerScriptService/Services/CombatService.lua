--!strict
-- Server-authoritative combat. The client reports a fire intent; the server
-- raycasts using its own weapon config, applies damage, awards XP, and tells
-- the shooter via HitConfirm. Skins are NEVER consulted in this path.

local Players = game:GetService("Players")
local ReplicatedStorage = game:GetService("ReplicatedStorage")
local Workspace = game:GetService("Workspace")

local WeaponConfigs = require(ReplicatedStorage.Modules.WeaponConfigs)
local KnifeConfigs = require(ReplicatedStorage.Modules.KnifeConfigs)
local Recoil = require(ReplicatedStorage.Modules.Recoil)
local Constants = require(ReplicatedStorage.Modules.Constants)
local RemoteDefs = require(ReplicatedStorage.Modules.RemoteDefs)
local AntiCheat = require(script.Parent.AntiCheat)
local DataService = require(script.Parent.DataService)

local Remotes = ReplicatedStorage:WaitForChild("Remotes")

local CombatService = {}

local recoilStates: {[number]: {[string]: Recoil.State}} = {}

local function getRecoil(userId: number, weaponId: string): Recoil.State
	local byPlayer = recoilStates[userId]
	if not byPlayer then
		byPlayer = {}
		recoilStates[userId] = byPlayer
	end
	local state = byPlayer[weaponId]
	if not state then
		state = Recoil.newState()
		byPlayer[weaponId] = state
	end
	return state
end

local function hitMultiplier(part: BasePart): number
	local name = part.Name:lower()
	if name:find("head") then return Constants.HEADSHOT_MULTIPLIER end
	if name:find("leg") or name:find("foot") then return Constants.LEG_MULTIPLIER end
	return 1.0
end

local function applyDamage(victim: Player, attacker: Player, amount: number, headshot: boolean)
	local character = victim.Character
	local humanoid = character and character:FindFirstChildOfClass("Humanoid")
	if not humanoid or humanoid.Health <= 0 then return end
	humanoid:TakeDamage(amount)
	Remotes.DamageTaken:FireClient(victim, { from = attacker.UserId, dmg = amount, headshot = headshot })
	if humanoid.Health <= 0 then
		DataService.bumpStat(attacker.UserId, "kills", 1)
		DataService.bumpStat(victim.UserId, "deaths", 1)
		if headshot then
			DataService.bumpStat(attacker.UserId, "headshots", 1)
		end
	end
end

function CombatService.handleFire(player: Player, payload: any)
	if typeof(payload) ~= "table" then return end
	local weaponId = payload.weaponId
	local origin: Vector3 = payload.origin
	local direction: Vector3 = payload.direction
	local seed: number = payload.seed
	if type(weaponId) ~= "string" or typeof(origin) ~= "Vector3" or typeof(direction) ~= "Vector3" then
		return
	end

	local ok, cfg = pcall(WeaponConfigs.get, weaponId)
	if not ok then return end

	local now = os.clock()
	if not AntiCheat.checkFireRate(player.UserId, weaponId, now) then return end

	-- Validate that origin is plausibly the player's head.
	local character = player.Character
	local head = character and character:FindFirstChild("Head")
	if not head or (origin - head.Position).Magnitude > 3 then return end

	-- Reproduce recoil/spread deterministically on the server.
	local kick = Recoil.step(getRecoil(player.UserId, weaponId), now, cfg.recoilPattern)
	local kickedForward = (direction
		+ Vector3.new(math.tan(math.rad(kick.X)), math.tan(math.rad(kick.Y)), 0))
		.Unit
	local shotForward = Recoil.coneOffset(seed, kickedForward, cfg.spread)

	local params = RaycastParams.new()
	params.FilterDescendantsInstances = { character }
	params.FilterType = Enum.RaycastFilterType.Exclude
	local result = Workspace:Raycast(origin, shotForward * Constants.MAX_SHOT_DISTANCE, params)
	if not result then return end

	local hitPart = result.Instance
	local hitModel = hitPart:FindFirstAncestorOfClass("Model")
	local victim = hitModel and Players:GetPlayerFromCharacter(hitModel)
	if not victim or victim == player then return end

	local mult = hitMultiplier(hitPart)
	local damage = cfg.damage * mult
	local headshot = mult == Constants.HEADSHOT_MULTIPLIER
	applyDamage(victim, player, damage, headshot)

	Remotes.HitConfirm:FireClient(player, {
		targetId = victim.UserId,
		dmg = damage,
		headshot = headshot,
	})
end

function CombatService.handleMelee(player: Player, payload: any)
	if typeof(payload) ~= "table" then return end
	local character = player.Character
	local root = character and character:FindFirstChild("HumanoidRootPart")
	if not root then return end

	local knifeId = payload.knifeId or "karambit"
	local ok, cfg = pcall(KnifeConfigs.get, knifeId)
	if not ok then return end
	local isStab = payload.kind == "Stab"
	local cooldown = isStab and cfg.stabCooldown or cfg.slashCooldown

	local now = os.clock()
	if not AntiCheat.checkFireRate(player.UserId, "melee_" .. knifeId, now) then return end

	-- Find the closest opponent within range and in front.
	local forward = root.CFrame.LookVector
	local best, bestDist, bestBack
	for _, other in Players:GetPlayers() do
		if other == player then continue end
		local otherChar = other.Character
		local otherRoot = otherChar and otherChar:FindFirstChild("HumanoidRootPart")
		if not otherRoot then continue end
		local delta = otherRoot.Position - root.Position
		if delta.Magnitude > cfg.range then continue end
		if forward:Dot(delta.Unit) < 0.3 then continue end
		if not bestDist or delta.Magnitude < bestDist then
			best = other
			bestDist = delta.Magnitude
			bestBack = otherRoot.CFrame.LookVector:Dot(forward) > 0.4
		end
	end

	if not best then return end
	local base = isStab and cfg.stabDamage or cfg.slashDamage
	local mult = 1
	if bestBack then
		mult = isStab and cfg.stabBackMult or cfg.slashBackMult
	end
	applyDamage(best, player, base * mult, false)
	DataService.bumpStat(player.UserId, "knifeHits", 1)
end

function CombatService.start()
	Remotes.FireShot.OnServerEvent:Connect(CombatService.handleFire)
	Remotes.MeleeSwing.OnServerEvent:Connect(CombatService.handleMelee)
	Players.PlayerRemoving:Connect(function(player)
		recoilStates[player.UserId] = nil
	end)
end

return CombatService
