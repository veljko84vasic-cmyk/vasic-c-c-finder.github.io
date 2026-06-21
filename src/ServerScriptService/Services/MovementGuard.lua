--!strict
-- Receives client movement snapshots and runs them through AntiCheat. Real
-- positions are owned by Roblox's character physics; this just refuses to
-- broadcast clearly impossible deltas to other systems (and accumulates
-- violations that MatchService can use to kick).

local Players = game:GetService("Players")
local ReplicatedStorage = game:GetService("ReplicatedStorage")

local RemoteDefs = require(ReplicatedStorage.Modules.RemoteDefs)
local AntiCheat = require(script.Parent.AntiCheat)

local Remotes = ReplicatedStorage:WaitForChild("Remotes")

local MovementGuard = {}

function MovementGuard.start()
	Remotes.MovementSnapshot.OnServerEvent:Connect(function(player, payload)
		if typeof(payload) ~= "table" then return end
		local pos = payload.position
		if typeof(pos) ~= "Vector3" then return end
		AntiCheat.checkMovement(player.UserId, pos, os.clock())
	end)

	Players.PlayerRemoving:Connect(function(player)
		AntiCheat.clear(player.UserId)
	end)
end

return MovementGuard
