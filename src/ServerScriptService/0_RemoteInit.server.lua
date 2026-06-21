--!strict
-- Creates the RemoteEvent / RemoteFunction instances inside
-- ReplicatedStorage.Remotes at boot. Runs before Main.server.lua because the
-- filename sorts first alphabetically.

local ReplicatedStorage = game:GetService("ReplicatedStorage")
local Defs = require(ReplicatedStorage.Modules.RemoteDefs)

local container = ReplicatedStorage:WaitForChild("Remotes")

local FUNCTION_REMOTES = {
	GetProfile = true,
	GetLeaderboard = true,
	GetMatchHistory = true,
}

for _, name in Defs do
	if container:FindFirstChild(name) then continue end
	local instance
	if FUNCTION_REMOTES[name] then
		instance = Instance.new("RemoteFunction")
	else
		instance = Instance.new("RemoteEvent")
	end
	instance.Name = name
	instance.Parent = container
end
