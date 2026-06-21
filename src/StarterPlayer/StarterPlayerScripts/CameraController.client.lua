--!strict
-- First-person camera with InputAdapter-driven look (so touch + mouse share
-- the same code path). Handles sensitivity, FOV punch on fire, and an
-- adjustable aim-down-sights zoom.

local Players = game:GetService("Players")
local RunService = game:GetService("RunService")
local UserInputService = game:GetService("UserInputService")
local Workspace = game:GetService("Workspace")

local InputAdapter = require(script.Parent.InputAdapter)

local player = Players.LocalPlayer
local camera = Workspace.CurrentCamera

local sensitivity = 0.0022       -- radians per pixel of mouse delta
local touchSensitivity = 0.006   -- radians per touch unit
local hipFov = 90
local aimFov = 65

local yaw, pitch = 0, 0
local fovPunch = 0

UserInputService.MouseBehavior = Enum.MouseBehavior.LockCenter
UserInputService.MouseIconEnabled = false

camera.CameraType = Enum.CameraType.Scriptable

local function getHead(): BasePart?
	local char = player.Character
	return char and char:FindFirstChild("Head") :: BasePart
end

InputAdapter.subscribe(function(s)
	if s.lookDelta.Magnitude > 0 then
		local sens = UserInputService.TouchEnabled and touchSensitivity or sensitivity
		yaw -= s.lookDelta.X * sens
		pitch = math.clamp(pitch - s.lookDelta.Y * sens, -math.rad(89), math.rad(89))
	end
end)

-- Other modules call this to apply recoil / hit kicks.
local function shake(amount: number)
	fovPunch = math.min(fovPunch + amount, 8)
end

_G.VasicCameraShake = shake

RunService.RenderStepped:Connect(function(dt)
	local head = getHead()
	if not head then return end
	local s = InputAdapter.get()
	local targetFov = s.aim and aimFov or hipFov
	camera.FieldOfView = camera.FieldOfView + (targetFov + fovPunch - camera.FieldOfView) * math.min(1, dt * 12)
	fovPunch = math.max(0, fovPunch - dt * 18)

	local cf = CFrame.new(head.Position)
		* CFrame.Angles(0, yaw, 0)
		* CFrame.Angles(pitch, 0, 0)
	camera.CFrame = cf
end)
