--!strict
-- Global and seasonal leaderboards backed by OrderedDataStore.
-- The store key is "season_<id>" so each season starts a fresh board.

local DataStoreService = game:GetService("DataStoreService")
local ReplicatedStorage = game:GetService("ReplicatedStorage")

local Constants = require(ReplicatedStorage.Modules.Constants)

local LeaderboardService = {}

local function storeFor(seasonId: number): OrderedDataStore
	return DataStoreService:GetOrderedDataStore("VasicLeaderboard_v1", "season_" .. seasonId)
end

local function globalStore(): OrderedDataStore
	return DataStoreService:GetOrderedDataStore("VasicLeaderboard_v1", "alltime")
end

function LeaderboardService.recordMMR(userId: number, mmr: number, seasonId: number)
	local key = tostring(userId)
	pcall(function()
		storeFor(seasonId):SetAsync(key, mmr)
	end)
	-- All-time records the player's peak across seasons.
	pcall(function()
		globalStore():UpdateAsync(key, function(old)
			if old == nil or mmr > old then
				return mmr
			end
			return old
		end)
	end)
end

export type Entry = { userId: number, mmr: number }

function LeaderboardService.top(seasonId: number?, count: number?): {Entry}
	count = count or 100
	local store = seasonId and storeFor(seasonId) or globalStore()
	local out: {Entry} = {}
	local ok, pages = pcall(function()
		return store:GetSortedAsync(false, count)
	end)
	if not ok or not pages then return out end
	local page = pages:GetCurrentPage()
	for _, item in page do
		table.insert(out, { userId = tonumber(item.key) or 0, mmr = item.value })
	end
	return out
end

return LeaderboardService
