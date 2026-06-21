--!strict
-- Wraps Matchmaking into a service: handles JoinQueue/LeaveQueue remotes,
-- runs the matcher on a tick, and hands formed matches to MatchService.

local Players = game:GetService("Players")
local ReplicatedStorage = game:GetService("ReplicatedStorage")

local Matchmaking = require(ReplicatedStorage.Modules.Matchmaking)
local DataService = require(script.Parent.DataService)
local MatchService = require(script.Parent.MatchService)

local Remotes = ReplicatedStorage:WaitForChild("Remotes")

local QueueService = {}

local function userIdToPlayer(id: number): Player?
	return Players:GetPlayerByUserId(id)
end

function QueueService.start()
	Remotes.JoinQueue.OnServerEvent:Connect(function(player)
		local profile = DataService.get(player.UserId)
		if not profile then return end
		Matchmaking.enqueue({
			userId = player.UserId,
			mmr = profile.rank.mmr,
			rd = profile.rank.rd,
			enqueuedAt = os.clock(),
			partyId = nil,
		})
	end)

	Remotes.LeaveQueue.OnServerEvent:Connect(function(player)
		Matchmaking.dequeue(player.UserId)
	end)

	Players.PlayerRemoving:Connect(function(player)
		Matchmaking.dequeue(player.UserId)
	end)

	task.spawn(function()
		while true do
			task.wait(2)
			local match = Matchmaking.tryForm(os.clock())
			if match then
				local function resolve(tickets)
					local list: {Player} = {}
					for _, t in tickets do
						local p = userIdToPlayer(t.userId)
						if p then table.insert(list, p) end
					end
					return list
				end
				local teamA, teamB = resolve(match.teamA), resolve(match.teamB)
				if #teamA > 0 and #teamB > 0 then
					for _, p in teamA do
						Remotes.MatchFound:FireClient(p, { team = "A" })
					end
					for _, p in teamB do
						Remotes.MatchFound:FireClient(p, { team = "B" })
					end
					MatchService.beginMatch(teamA, teamB, match.avgA, match.avgB)
				end
			end
		end
	end)
end

return QueueService
