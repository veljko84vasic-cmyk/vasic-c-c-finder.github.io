--!strict
-- Minimal main menu / queue UI. Toggle with Tab.

local Players = game:GetService("Players")
local UserInputService = game:GetService("UserInputService")
local ReplicatedStorage = game:GetService("ReplicatedStorage")
local RunService = game:GetService("RunService")

local Remotes = ReplicatedStorage:WaitForChild("Remotes")
local function rget(name: string): Instance
	return Remotes:WaitForChild(name)
end

local player = Players.LocalPlayer
local gui = Instance.new("ScreenGui")
gui.Name = "VasicMenu"
gui.IgnoreGuiInset = true
gui.ResetOnSpawn = false
gui.Enabled = false
gui.Parent = player:WaitForChild("PlayerGui")

local panel = Instance.new("Frame")
panel.AnchorPoint = Vector2.new(0.5, 0.5)
panel.Position = UDim2.new(0.5, 0, 0.5, 0)
panel.Size = UDim2.new(0, 480, 0, 360)
panel.BackgroundColor3 = Color3.fromRGB(20, 20, 25)
panel.BorderSizePixel = 0
panel.Parent = gui

local title = Instance.new("TextLabel")
title.Size = UDim2.new(1, 0, 0, 50)
title.Text = "Vasic CC"
title.Font = Enum.Font.GothamBlack
title.TextScaled = true
title.TextColor3 = Color3.new(1, 1, 1)
title.BackgroundTransparency = 1
title.Parent = panel

local function makeButton(text: string, y: number): TextButton
	local b = Instance.new("TextButton")
	b.Size = UDim2.new(0, 320, 0, 56)
	b.AnchorPoint = Vector2.new(0.5, 0)
	b.Position = UDim2.new(0.5, 0, 0, y)
	b.BackgroundColor3 = Color3.fromRGB(40, 50, 70)
	b.TextColor3 = Color3.new(1, 1, 1)
	b.Font = Enum.Font.GothamBold
	b.TextScaled = true
	b.Text = text
	b.AutoButtonColor = true
	b.Parent = panel
	return b
end

local queueBtn = makeButton("Find Ranked Match", 70)
local leaveBtn = makeButton("Leave Queue", 140)
local lbBtn = makeButton("View Leaderboard", 210)
local profileBtn = makeButton("My Profile", 280)

local status = Instance.new("TextLabel")
status.Position = UDim2.new(0, 0, 1, -30)
status.Size = UDim2.new(1, 0, 0, 30)
status.BackgroundTransparency = 1
status.TextColor3 = Color3.fromRGB(200, 200, 200)
status.Font = Enum.Font.Gotham
status.TextScaled = true
status.Text = ""
status.Parent = panel

local inQueue = false
local queueStarted = 0

queueBtn.MouseButton1Click:Connect(function()
	(rget("JoinQueue") :: RemoteEvent):FireServer()
	inQueue = true
	queueStarted = os.clock()
end)

leaveBtn.MouseButton1Click:Connect(function()
	(rget("LeaveQueue") :: RemoteEvent):FireServer()
	inQueue = false
	status.Text = "Left queue."
end)

lbBtn.MouseButton1Click:Connect(function()
	local board = (rget("GetLeaderboard") :: RemoteFunction):InvokeServer({ count = 10 })
	local lines = {"Top 10 (this season):"}
	for i, e in board do
		table.insert(lines, string.format("%d.  %d   %d MMR", i, e.userId, e.mmr))
	end
	status.Text = table.concat(lines, "\n")
end)

profileBtn.MouseButton1Click:Connect(function()
	local profile = (rget("GetProfile") :: RemoteFunction):InvokeServer()
	if profile then
		status.Text = string.format(
			"XP %d | MMR %d | RD %d | Matches %d",
			profile.xp, profile.rank.mmr, profile.rank.rd, profile.rank.matchesPlayed
		)
	end
end)

local MatchFound = rget("MatchFound") :: RemoteEvent
MatchFound.OnClientEvent:Connect(function(payload)
	inQueue = false
	status.Text = "Match found! Team " .. payload.team
end)

UserInputService.InputBegan:Connect(function(input, processed)
	if processed then return end
	if input.KeyCode == Enum.KeyCode.Tab then
		gui.Enabled = not gui.Enabled
		UserInputService.MouseBehavior = gui.Enabled
			and Enum.MouseBehavior.Default
			or Enum.MouseBehavior.LockCenter
	end
end)

RunService.Heartbeat:Connect(function()
	if inQueue then
		local wait = os.clock() - queueStarted
		status.Text = string.format("In queue... %ds", math.floor(wait))
	end
end)
