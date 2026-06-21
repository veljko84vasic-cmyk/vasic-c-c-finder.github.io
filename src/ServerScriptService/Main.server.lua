--!strict
-- Boot: load every player profile on join, expose RemoteFunctions, drive
-- MatchService update loop. Order matters — DataService must be live before
-- combat/match services touch profiles.

local Players = game:GetService("Players")
local ReplicatedStorage = game:GetService("ReplicatedStorage")
local RunService = game:GetService("RunService")

local SeasonService     = require(script.Parent.Services.SeasonService)
local DataService       = require(script.Parent.Services.DataService)
local LeaderboardService= require(script.Parent.Services.LeaderboardService)
local CombatService     = require(script.Parent.Services.CombatService)
local MatchService      = require(script.Parent.Services.MatchService)
local QueueService      = require(script.Parent.Services.QueueService)
local MovementGuard     = require(script.Parent.Services.MovementGuard)
local InventoryService  = require(script.Parent.Services.InventoryService)

local Remotes = ReplicatedStorage:WaitForChild("Remotes")

DataService.start()
CombatService.start()
QueueService.start()
MovementGuard.start()
InventoryService.start()

Players.PlayerAdded:Connect(function(player)
	local seasonId = SeasonService.currentId()
	DataService.load(player, seasonId)
end)

-- Backfill for players already in-game when the script starts (Studio reload).
for _, player in Players:GetPlayers() do
	task.spawn(function()
		DataService.load(player, SeasonService.currentId())
	end)
end

Remotes.GetProfile.OnServerInvoke = function(player)
	return DataService.get(player.UserId)
end

Remotes.GetLeaderboard.OnServerInvoke = function(_player, payload)
	payload = typeof(payload) == "table" and payload or {}
	local seasonId = payload.seasonId
	local count = math.clamp(tonumber(payload.count) or 100, 1, 100)
	return LeaderboardService.top(seasonId, count)
end

Remotes.GetMatchHistory.OnServerInvoke = function(player)
	local profile = DataService.get(player.UserId)
	return profile and profile.matchHistory or {}
end

RunService.Heartbeat:Connect(function()
	MatchService.update()
end)
