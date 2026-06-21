--!strict
-- Placeholder viewmodel: a coloured block in front of the camera that swaps
-- shape/colour based on the equipped weapon. Replace with real meshes when
-- you have art.

local Players = game:GetService("Players")
local RunService = game:GetService("RunService")
local Workspace = game:GetService("Workspace")

local player = Players.LocalPlayer
local camera = Workspace.CurrentCamera

local viewmodel = Instance.new("Model")
viewmodel.Name = "Viewmodel"
viewmodel.Parent = camera

local gunPart = Instance.new("Part")
gunPart.Name = "Gun"
gunPart.Anchored = true
gunPart.CanCollide = false
gunPart.CanQuery = false
gunPart.CanTouch = false
gunPart.Massless = true
gunPart.Material = Enum.Material.SmoothPlastic
gunPart.Parent = viewmodel

local barrelPart = Instance.new("Part")
barrelPart.Name = "Barrel"
barrelPart.Anchored = true
barrelPart.CanCollide = false
barrelPart.CanQuery = false
barrelPart.CanTouch = false
barrelPart.Massless = true
barrelPart.Material = Enum.Material.Metal
barrelPart.Color = Color3.fromRGB(40, 40, 40)
barrelPart.Parent = viewmodel

local SHAPES: {[string]: {size: Vector3, barrel: Vector3, color: Color3, barrelOffset: Vector3}} = {
	ak47 = {
		size = Vector3.new(0.45, 0.55, 2.6),
		barrel = Vector3.new(0.18, 0.18, 1.6),
		color = Color3.fromRGB(120, 70, 40),
		barrelOffset = Vector3.new(0, 0.05, -1.8),
	},
	m4a4 = {
		size = Vector3.new(0.42, 0.5, 2.4),
		barrel = Vector3.new(0.16, 0.16, 1.4),
		color = Color3.fromRGB(70, 75, 80),
		barrelOffset = Vector3.new(0, 0.05, -1.7),
	},
	awp = {
		size = Vector3.new(0.4, 0.45, 3.2),
		barrel = Vector3.new(0.14, 0.14, 2.2),
		color = Color3.fromRGB(40, 60, 90),
		barrelOffset = Vector3.new(0, 0.05, -2.2),
	},
	deagle = {
		size = Vector3.new(0.32, 0.55, 1.2),
		barrel = Vector3.new(0.14, 0.14, 0.7),
		color = Color3.fromRGB(180, 150, 60),
		barrelOffset = Vector3.new(0, 0.1, -0.95),
	},
	glock = {
		size = Vector3.new(0.3, 0.5, 1.0),
		barrel = Vector3.new(0.12, 0.12, 0.5),
		color = Color3.fromRGB(40, 40, 45),
		barrelOffset = Vector3.new(0, 0.08, -0.8),
	},
	karambit = {
		size = Vector3.new(0.08, 0.6, 1.0),
		barrel = Vector3.new(0.02, 0.05, 0.8),
		color = Color3.fromRGB(220, 220, 230),
		barrelOffset = Vector3.new(0, 0.05, -0.7),
	},
	butterfly = {
		size = Vector3.new(0.06, 0.5, 1.1),
		barrel = Vector3.new(0.02, 0.05, 0.9),
		color = Color3.fromRGB(180, 180, 195),
		barrelOffset = Vector3.new(0, 0.05, -0.8),
	},
}

local function applyShape(id: string)
	local s = SHAPES[id] or SHAPES.ak47
	gunPart.Size = s.size
	gunPart.Color = s.color
	barrelPart.Size = s.barrel
	gunPart:SetAttribute("BarrelOffset", s.barrelOffset)
end

-- Slight bob + sway. Keeps the viewmodel from feeling glued to the camera.
local bobPhase = 0

RunService.RenderStepped:Connect(function(dt)
	if not _G.VasicWeapon then return end
	local id = _G.VasicWeapon.getCurrent()
	if id ~= gunPart:GetAttribute("WeaponId") then
		gunPart:SetAttribute("WeaponId", id)
		applyShape(id)
	end

	-- Detect movement for bob.
	local char = player.Character
	local root = char and char:FindFirstChild("HumanoidRootPart") :: BasePart?
	local speed = root and Vector3.new(root.AssemblyLinearVelocity.X, 0, root.AssemblyLinearVelocity.Z).Magnitude or 0
	bobPhase += dt * math.clamp(speed / 8, 0, 2.5)
	local bob = Vector3.new(
		math.sin(bobPhase * 2) * 0.04,
		math.abs(math.sin(bobPhase)) * 0.03,
		0
	)

	-- Offset from the camera: down-right of centre, slight forward depth.
	local handOffset = Vector3.new(0.45, -0.5, -1.6) + bob
	local cf = camera.CFrame * CFrame.new(handOffset)
	gunPart.CFrame = cf
	local bOff: Vector3? = gunPart:GetAttribute("BarrelOffset")
	if bOff then
		barrelPart.CFrame = cf * CFrame.new(bOff)
	end
end)

player.CharacterAdded:Connect(function()
	-- Re-parent to camera after respawns (Camera persists, but be safe).
	viewmodel.Parent = camera
end)
