--!strict
-- Local weapon handling: slot state, fire timing, recoil prediction, reloads.
-- Sends FireShot to the server with origin/direction/seed — the server is
-- authoritative for all damage.

local Players = game:GetService("Players")
local RunService = game:GetService("RunService")
local ReplicatedStorage = game:GetService("ReplicatedStorage")
local Workspace = game:GetService("Workspace")

local WeaponConfigs = require(ReplicatedStorage.Modules.WeaponConfigs)
local KnifeConfigs  = require(ReplicatedStorage.Modules.KnifeConfigs)
local Recoil = require(ReplicatedStorage.Modules.Recoil)

local InputAdapter = require(script.Parent.InputAdapter)

local Remotes = ReplicatedStorage:WaitForChild("Remotes")

local player = Players.LocalPlayer

local loadout = {
	Primary = "ak47",
	Secondary = "deagle",
	Melee = "karambit",
}
local current: "Primary" | "Secondary" | "Melee" = "Primary"
local ammo = { ak47 = { mag = 30, reserve = 90 }, deagle = { mag = 7, reserve = 35 } }
local recoilState = Recoil.newState()
local lastFire = 0
local reloading = false
local drawUntil = 0

local function camera() return Workspace.CurrentCamera end

local function activeWeaponId(): string
	return loadout[current]
end

local function canFire(now: number): boolean
	if reloading or now < drawUntil then return false end
	if current == "Melee" then return false end
	local cfg = WeaponConfigs.get(activeWeaponId())
	local interval = 60 / cfg.rpm
	if now - lastFire < interval then return false end
	if (ammo[activeWeaponId()] or {mag=0}).mag <= 0 then return false end
	return true
end

local function fire(now: number)
	local id = activeWeaponId()
	local cfg = WeaponConfigs.get(id)
	local cf = camera().CFrame
	local origin = cf.Position
	local dir = cf.LookVector
	local seed = math.random(1, 2^31 - 1)

	-- Predict recoil locally so the crosshair kicks immediately.
	local kick = Recoil.step(recoilState, now, cfg.recoilPattern)
	if _G.VasicCameraShake then _G.VasicCameraShake(kick.Y * 0.4) end

	ammo[id].mag -= 1
	lastFire = now

	Remotes.FireShot:FireServer({
		weaponId = id,
		origin = origin,
		direction = dir,
		seed = seed,
		clientTime = now,
	})
end

local function reload()
	local id = activeWeaponId()
	if current == "Melee" then return end
	local cfg = WeaponConfigs.get(id)
	local pool = ammo[id]
	if not pool or pool.mag >= cfg.magazine or pool.reserve <= 0 then return end
	reloading = true
	task.delay(cfg.reloadTime, function()
		local missing = cfg.magazine - pool.mag
		local taken = math.min(missing, pool.reserve)
		pool.mag += taken
		pool.reserve -= taken
		reloading = false
	end)
end

local function swap(slot: "Primary" | "Secondary" | "Melee")
	if slot == current then return end
	current = slot
	reloading = false
	local cfg
	if slot == "Melee" then
		cfg = KnifeConfigs.get(loadout.Melee)
		drawUntil = os.clock() + cfg.drawTime
	else
		cfg = WeaponConfigs.get(loadout[slot])
		drawUntil = os.clock() + cfg.drawTime
	end
end

local function meleeSwing(kind: "Slash" | "Stab")
	Remotes.MeleeSwing:FireServer({ knifeId = loadout.Melee, kind = kind })
end

InputAdapter.subscribe(function(s)
	if s.swapPrimary then swap("Primary") end
	if s.swapSecondary then swap("Secondary") end
	if s.swapMelee then swap("Melee") end
	if s.reload then reload() end
	if s.inspect then Remotes.InspectWeapon:FireServer() end
end)

RunService.Heartbeat:Connect(function()
	local s = InputAdapter.get()
	local now = os.clock()
	if current == "Melee" then
		if s.fire and now > drawUntil then
			drawUntil = now + (KnifeConfigs.get(loadout.Melee).slashCooldown)
			meleeSwing("Slash")
		elseif s.aim and now > drawUntil then
			local cfg = KnifeConfigs.get(loadout.Melee)
			drawUntil = now + cfg.stabCooldown
			meleeSwing("Stab")
		end
		return
	end
	if s.fire and canFire(now) then
		local cfg = WeaponConfigs.get(activeWeaponId())
		fire(now)
		if cfg.fireMode == "Semi" then
			-- prevent auto-repeat on hold for semi weapons
			s.fire = false
		end
	end
end)

-- Expose for HUD
_G.VasicWeapon = {
	getCurrent = function() return activeWeaponId(), current end,
	getAmmo = function() return ammo[activeWeaponId()] end,
	isReloading = function() return reloading end,
}
