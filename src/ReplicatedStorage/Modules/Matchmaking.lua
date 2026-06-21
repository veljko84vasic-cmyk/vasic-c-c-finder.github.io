--!strict
-- SBMM queue. Players enter with their MMR/RD; the matcher tries to form
-- two teams of MAX_TEAM_SIZE with the smallest skill gap. Search window
-- expands over time so nobody waits forever.

local Constants = require(script.Parent.Constants)
local RankSystem = require(script.Parent.RankSystem)

local Matchmaking = {}

export type Ticket = {
	userId: number,
	mmr: number,
	rd: number,
	enqueuedAt: number,
	partyId: string?,
}

export type Match = {
	teamA: {Ticket},
	teamB: {Ticket},
	avgA: number,
	avgB: number,
}

local Queue: {Ticket} = {}

function Matchmaking.enqueue(ticket: Ticket)
	for _, t in Queue do
		if t.userId == ticket.userId then return end
	end
	table.insert(Queue, ticket)
end

function Matchmaking.dequeue(userId: number)
	for i, t in Queue do
		if t.userId == userId then
			table.remove(Queue, i)
			return
		end
	end
end

function Matchmaking.size(): number
	return #Queue
end

-- Search window in MMR points scales with wait time.
local function windowFor(ticket: Ticket, now: number): number
	local wait = now - ticket.enqueuedAt
	return 80 + wait * 25
end

local function partition(candidates: {Ticket}): ({Ticket}, {Ticket})
	-- Greedy snake-draft sorted by MMR descending — gives roughly equal sums
	-- with small N, which is what we want at 5v5.
	table.sort(candidates, function(a, b) return a.mmr > b.mmr end)
	local a, b = {}, {}
	local sumA, sumB = 0, 0
	for _, t in candidates do
		if sumA <= sumB then
			table.insert(a, t); sumA += t.mmr
		else
			table.insert(b, t); sumB += t.mmr
		end
	end
	return a, b
end

-- Try to form one match. Returns nil if the queue can't yet support it.
function Matchmaking.tryForm(now: number): Match?
	local needed = Constants.MAX_TEAM_SIZE * 2
	if #Queue < needed then return nil end

	-- Pick the oldest ticket as the anchor; gather the closest-MMR others
	-- that fall inside the union of search windows.
	table.sort(Queue, function(a, b) return a.enqueuedAt < b.enqueuedAt end)
	local anchor = Queue[1]
	local anchorWindow = windowFor(anchor, now)

	local candidates: {Ticket} = {}
	for _, t in Queue do
		local mutualWindow = math.max(anchorWindow, windowFor(t, now))
		if math.abs(t.mmr - anchor.mmr) <= mutualWindow then
			table.insert(candidates, t)
			if #candidates == needed then break end
		end
	end

	if #candidates < needed then return nil end

	local teamA, teamB = partition(candidates)
	local function avg(team: {Ticket}): number
		local s = 0
		for _, t in team do s += t.mmr end
		return s / #team
	end

	-- Reject obviously lopsided matches unless the anchor has waited a while.
	local avgA, avgB = avg(teamA), avg(teamB)
	local skew = math.abs(avgA - avgB)
	local maxSkew = 80 + (now - anchor.enqueuedAt) * 10
	if skew > maxSkew then return nil end

	for _, t in candidates do
		Matchmaking.dequeue(t.userId)
	end

	return { teamA = teamA, teamB = teamB, avgA = avgA, avgB = avgB }
end

-- Diagnostic helper for UI ("avg search time at your rank")
function Matchmaking.snapshot(): {anchor: number?, queueSize: number}
	local oldest
	for _, t in Queue do
		if not oldest or t.enqueuedAt < oldest then
			oldest = t.enqueuedAt
		end
	end
	return { anchor = oldest, queueSize = #Queue }
end

return Matchmaking
