--!strict
-- Drives the local character with the Movement module. We let Roblox's
-- physics own the character body and only feed it velocity each tick — this
-- preserves built-in collision while we get full control over accel/friction.
--
-- Anti-cheat lives server-side; here we periodically send a position snapshot
-- so the server can sanity-check us.

local Players = game:GetService("Players")
local RunService = game:GetService("RunService")
local ReplicatedStorage = game:GetService("ReplicatedStorage")
local Workspace = game:GetService("Workspace")

local Movement = require(ReplicatedStorage.Modules.Movement)
local Constants = require(ReplicatedStorage.Modules.Constants)

local InputAdapter = require(script.Parent.InputAdapter)

local Remotes = ReplicatedStorage:WaitForChild("Remotes")
local MovementSnapshot = Remotes:WaitForChild("MovementSnapshot") :: RemoteEvent

local player = Players.LocalPlayer

local state = Movement.newState(Vector3.zero)
local lastSnapshot = 0

local function getRoot(): BasePart?
	local char = player.Character
	return char and char:FindFirstChild("HumanoidRootPart") :: BasePart
end

local function getHumanoid(): Humanoid?
	local char = player.Character
	return char and char:FindFirstChildOfClass("Humanoid")
end

-- Map (moveX, moveY) from input into world-space direction based on camera yaw.
local function worldMoveDir(input): Vector3
	local camera = Workspace.CurrentCamera
	local look = camera.CFrame.LookVector
	look = Vector3.new(look.X, 0, look.Z)
	if look.Magnitude < 0.001 then
		return Vector3.zero
	end
	look = look.Unit
	local right = Vector3.new(look.Z, 0, -look.X)
	local desired = right * input.moveX + look * input.moveY
	if desired.Magnitude < 0.001 then return Vector3.zero end
	return desired.Unit
end

local prevGrounded = true

-- Disable Roblox's built-in character controller so it doesn't fight our
-- velocity writes. We still rely on the engine for gravity, collisions, and
-- step-up; we just own the horizontal velocity ourselves.
local function tameHumanoid(humanoid: Humanoid)
	humanoid.WalkSpeed = 0
	humanoid.AutoRotate = false
	humanoid.UseJumpPower = true
	humanoid.JumpPower = 0   -- jumping is handled by our movement code
end

player.CharacterAdded:Connect(function(character)
	local hum = character:WaitForChild("Humanoid") :: Humanoid
	tameHumanoid(hum)
end)
if player.Character then
	local hum = player.Character:FindFirstChildOfClass("Humanoid")
	if hum then tameHumanoid(hum) end
end

RunService.RenderStepped:Connect(function(dt)
	local root = getRoot(); if not root then return end
	local humanoid = getHumanoid(); if not humanoid then return end

	local input = InputAdapter.get()
	local now = os.clock()

	-- Sync our state with the engine's truth (gravity, collisions handled by
	-- the engine; we only own horizontal velocity & jump impulse).
	state.position = root.Position
	state.velocity = root.AssemblyLinearVelocity
	local isGrounded = humanoid.FloorMaterial ~= Enum.Material.Air

	if isGrounded and not prevGrounded then
		Movement.onLand(state, now)
	end
	state.onGround = isGrounded
	prevGrounded = isGrounded

	Movement.step(state, {
		moveDir = worldMoveDir(input),
		jump = input.jump,
		crouch = input.crouch,
		slide = input.slide,
		sprint = input.sprint,
	}, dt, now)

	-- Push horizontal velocity back to the character; preserve engine Y.
	local newVel = Vector3.new(state.velocity.X, root.AssemblyLinearVelocity.Y, state.velocity.Z)
	if input.jump and isGrounded then
		newVel = Vector3.new(state.velocity.X, Constants.JUMP_IMPULSE, state.velocity.Z)
	end
	root.AssemblyLinearVelocity = newVel

	-- Crouch / slide visuals via hip height.
	humanoid.HipHeight = (state.crouched or state.sliding)
		and Constants.CROUCH_HIPHEIGHT
		or Constants.STAND_HIPHEIGHT

	-- Periodic snapshot to the server for anti-cheat.
	if now - lastSnapshot > 0.1 then
		lastSnapshot = now
		MovementSnapshot:FireServer({ position = root.Position })
	end
end)
