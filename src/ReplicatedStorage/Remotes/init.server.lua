--!strict
-- Materialise the remotes declared in RemoteDefs into actual Instances.
-- Runs before any server service starts because it lives under
-- ReplicatedStorage and is `init.server.lua` (executed at start).

local ReplicatedStorage = game:GetService("ReplicatedStorage")
local Defs = require(ReplicatedStorage.Modules.RemoteDefs)

local container = script.Parent

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
