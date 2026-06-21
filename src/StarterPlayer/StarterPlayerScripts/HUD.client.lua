--!strict
-- Minimal heads-up display: ammo, current weapon, dynamic crosshair, hitmarkers,
-- and a rank pill in the corner. Hand off to your designer for real art.

local Players = game:GetService("Players")
local RunService = game:GetService("RunService")
local ReplicatedStorage = game:GetService("ReplicatedStorage")

local Remotes = ReplicatedStorage:WaitForChild("Remotes")
local RankSystem = require(ReplicatedStorage.Modules.RankSystem)

local player = Players.LocalPlayer
local gui = Instance.new("ScreenGui")
gui.Name = "VasicHUD"
gui.IgnoreGuiInset = true
gui.ResetOnSpawn = false
gui.Parent = player:WaitForChild("PlayerGui")

local function label(name: string, anchor: Vector2, pos: UDim2, size: UDim2): TextLabel
	local l = Instance.new("TextLabel")
	l.Name = name
	l.AnchorPoint = anchor
	l.Position = pos
	l.Size = size
	l.BackgroundTransparency = 0.4
	l.BackgroundColor3 = Color3.fromRGB(0, 0, 0)
	l.TextColor3 = Color3.new(1, 1, 1)
	l.Font = Enum.Font.GothamBold
	l.TextScaled = true
	l.Parent = gui
	return l
end

local ammoLabel = label("Ammo", Vector2.new(1, 1), UDim2.new(1, -20, 1, -20), UDim2.new(0, 220, 0, 60))
local weaponLabel = label("Weapon", Vector2.new(1, 1), UDim2.new(1, -20, 1, -90), UDim2.new(0, 220, 0, 30))
local rankLabel = label("Rank", Vector2.new(0, 0), UDim2.new(0, 20, 0, 20), UDim2.new(0, 240, 0, 40))
rankLabel.Text = "Unranked"

-- Crosshair
local cross = Instance.new("Frame")
cross.Name = "Crosshair"
cross.AnchorPoint = Vector2.new(0.5, 0.5)
cross.Position = UDim2.new(0.5, 0, 0.5, 0)
cross.Size = UDim2.new(0, 24, 0, 24)
cross.BackgroundTransparency = 1
cross.Parent = gui

local function bar(thickness: number, length: number, position: UDim2): Frame
	local f = Instance.new("Frame")
	f.AnchorPoint = Vector2.new(0.5, 0.5)
	f.Position = position
	f.Size = UDim2.new(0, length, 0, thickness)
	f.BackgroundColor3 = Color3.fromRGB(0, 255, 80)
	f.BorderSizePixel = 0
	f.Parent = cross
	return f
end

local top    = bar(2, 2, UDim2.new(0.5, 0, 0, -6))
local bottom = bar(2, 2, UDim2.new(0.5, 0, 1, 6))
local left   = bar(2, 2, UDim2.new(0, -6, 0.5, 0))
local right  = bar(2, 2, UDim2.new(1, 6, 0.5, 0))
top.Size = UDim2.new(0, 2, 0, 6); left.Size = UDim2.new(0, 6, 0, 2)
bottom.Size = UDim2.new(0, 2, 0, 6); right.Size = UDim2.new(0, 6, 0, 2)

-- Hitmarker
local hitmarker = Instance.new("TextLabel")
hitmarker.AnchorPoint = Vector2.new(0.5, 0.5)
hitmarker.Position = UDim2.new(0.5, 0, 0.5, 0)
hitmarker.Size = UDim2.new(0, 30, 0, 30)
hitmarker.BackgroundTransparency = 1
hitmarker.Text = "X"
hitmarker.TextColor3 = Color3.fromRGB(255, 240, 240)
hitmarker.Font = Enum.Font.GothamBold
hitmarker.TextScaled = true
hitmarker.TextTransparency = 1
hitmarker.Parent = gui

Remotes.HitConfirm.OnClientEvent:Connect(function(payload)
	hitmarker.TextColor3 = payload.headshot and Color3.fromRGB(255, 80, 80) or Color3.new(1, 1, 1)
	hitmarker.TextTransparency = 0
	task.delay(0.15, function()
		hitmarker.TextTransparency = 1
	end)
end)

Remotes.RankUpdate.OnClientEvent:Connect(function(payload)
	local tier
	for _, t in RankSystem.Tiers do
		if t.id == payload.tier then tier = t end
	end
	rankLabel.Text = string.format("%s %d  (%d MMR, %+d)",
		tier and tier.name or "?", payload.division, payload.mmr, payload.delta)
end)

RunService.Heartbeat:Connect(function()
	if _G.VasicWeapon then
		local id, slot = _G.VasicWeapon.getCurrent()
		local a = _G.VasicWeapon.getAmmo()
		if a then
			ammoLabel.Text = string.format("%d / %d", a.mag, a.reserve)
		else
			ammoLabel.Text = "--"
		end
		weaponLabel.Text = string.format("[%s] %s", slot, id)
	end
end)
