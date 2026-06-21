--!strict
-- Round/match lifecycle: counts rounds, decides round winners, fires
-- RoundStart/RoundEnd/MatchEnd remotes, and tells RankService to settle MMR
-- when the match concludes.

local Players = game:GetService("Players")
local ReplicatedStorage = game:GetService("ReplicatedStorage")

local Constants = require(ReplicatedStorage.Modules.Constants)
local RankSystem = require(ReplicatedStorage.Modules.RankSystem)
local Progression = require(ReplicatedStorage.Modules.Progression)
local RemoteDefs = require(ReplicatedStorage.Modules.RemoteDefs)
local DataService = require(script.Parent.DataService)
local LeaderboardService = require(script.Parent.LeaderboardService)
local SeasonService = require(script.Parent.SeasonService)

local Remotes = ReplicatedStorage:WaitForChild("Remotes")

local MatchService = {}

export type Match = {
	id: string,
	teamA: {Player},
	teamB: {Player},
	scoreA: number,
	scoreB: number,
	round: number,
	state: "Buy" | "Live" | "Ended",
	avgA: number,
	avgB: number,
	startedAt: number,
	roundStartedAt: number,
	roundXp: {[number]: number},
}

local activeMatches: {Match} = {}

local function teamOf(match: Match, player: Player): string?
	if table.find(match.teamA, player) then return "A" end
	if table.find(match.teamB, player) then return "B" end
	return nil
end

local function aliveCount(team: {Player}): number
	local n = 0
	for _, p in team do
		local h = p.Character and p.Character:FindFirstChildOfClass("Humanoid")
		if h and h.Health > 0 then n += 1 end
	end
	return n
end

local function broadcast(match: Match, remoteName: string, payload: any)
	local remote = Remotes:FindFirstChild(remoteName)
	if not remote then return end
	for _, p in match.teamA do remote:FireClient(p, payload) end
	for _, p in match.teamB do remote:FireClient(p, payload) end
end

local function awardRound(match: Match, winningTeam: string)
	if winningTeam == "A" then
		match.scoreA += 1
		for _, p in match.teamA do
			DataService.bumpStat(p.UserId, "roundsWon", 1)
			DataService.addXp(p.UserId, 100)
		end
	else
		match.scoreB += 1
		for _, p in match.teamB do
			DataService.bumpStat(p.UserId, "roundsWon", 1)
			DataService.addXp(p.UserId, 100)
		end
	end
	broadcast(match, "RoundEnd", {
		winner = winningTeam,
		scoreA = match.scoreA,
		scoreB = match.scoreB,
	})
end

local function settleMatch(match: Match, winningTeam: string)
	match.state = "Ended"
	local seasonId = SeasonService.currentId()
	local totalRounds = match.scoreA + match.scoreB
	local margin = math.abs(match.scoreA - match.scoreB) / Constants.WIN_SCORE

	local function settleTeam(team: {Player}, oppAvg: number, won: boolean)
		for _, player in team do
			local profile = DataService.get(player.UserId)
			if not profile then continue end
			local delta = RankSystem.update(profile.rank, oppAvg, 90, won, margin)
			DataService.bumpStat(player.UserId, "rankedMatches", 1)
			DataService.addXp(player.UserId, won and 500 or 200)
			LeaderboardService.recordMMR(player.UserId, profile.rank.mmr, seasonId)

			local tier, division = RankSystem.tierFor(profile.rank.mmr)
			Remotes.RankUpdate:FireClient(player, {
				mmr = profile.rank.mmr,
				delta = delta,
				tier = tier.id,
				division = division,
			})

			DataService.appendMatch(player.UserId, {
				matchId = match.id,
				won = won,
				scoreA = match.scoreA,
				scoreB = match.scoreB,
				mmr = profile.rank.mmr,
				mmrDelta = delta,
				rounds = totalRounds,
				endedAt = os.time(),
			})

			-- Achievement check
			local unlocks = Progression.checkUnlocks(profile.stats, profile.achievements)
			for _, ach in unlocks do
				profile.achievements[ach.id] = true
				DataService.addXp(player.UserId, ach.xp)
				if ach.reward and ach.reward.kind == "Skin" then
					DataService.grantSkin(player.UserId, ach.reward.id)
				end
				Remotes.AchievementUnlocked:FireClient(player, ach)
			end
		end
	end

	settleTeam(match.teamA, match.avgB, winningTeam == "A")
	settleTeam(match.teamB, match.avgA, winningTeam == "B")

	broadcast(match, "MatchEnd", {
		winner = winningTeam,
		scoreA = match.scoreA,
		scoreB = match.scoreB,
	})

	for i, m in activeMatches do
		if m == match then
			table.remove(activeMatches, i)
			break
		end
	end
end

local function startRound(match: Match)
	match.round += 1
	match.roundStartedAt = os.clock()
	broadcast(match, "RoundStart", { round = match.round, scoreA = match.scoreA, scoreB = match.scoreB })

	-- Respawn everyone — caller will move them to spawn points in the real map.
	for _, p in match.teamA do
		if p.Character then p:LoadCharacter() end
	end
	for _, p in match.teamB do
		if p.Character then p:LoadCharacter() end
	end
end

function MatchService.beginMatch(teamA: {Player}, teamB: {Player}, avgA: number, avgB: number): Match
	local match: Match = {
		id = game:GetService("HttpService"):GenerateGUID(false),
		teamA = teamA, teamB = teamB,
		scoreA = 0, scoreB = 0, round = 0,
		state = "Buy", avgA = avgA, avgB = avgB,
		startedAt = os.clock(),
		roundStartedAt = os.clock(),
		roundXp = {},
	}
	table.insert(activeMatches, match)
	broadcast(match, "MatchStart", { matchId = match.id, avgA = avgA, avgB = avgB })
	startRound(match)
	return match
end

function MatchService.update()
	local now = os.clock()
	for _, match in activeMatches do
		if match.state == "Ended" then continue end
		local aA = aliveCount(match.teamA)
		local aB = aliveCount(match.teamB)
		local elapsed = now - match.roundStartedAt
		local roundDone = false
		local winner: string?

		if aA == 0 and aB > 0 then
			roundDone, winner = true, "B"
		elseif aB == 0 and aA > 0 then
			roundDone, winner = true, "A"
		elseif elapsed >= Constants.ROUND_TIME then
			roundDone = true
			winner = aA > aB and "A" or "B"
		end

		if roundDone and winner then
			awardRound(match, winner)
			if match.scoreA >= Constants.WIN_SCORE or match.scoreB >= Constants.WIN_SCORE then
				settleMatch(match, match.scoreA > match.scoreB and "A" or "B")
			else
				task.delay(5, function()
					if match.state ~= "Ended" then startRound(match) end
				end)
			end
		end
	end
end

return MatchService
