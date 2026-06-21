--!strict
-- Compute the current season id from a fixed epoch and season length.
-- Servers don't coordinate — they all derive the same id from the clock.

local ReplicatedStorage = game:GetService("ReplicatedStorage")
local Constants = require(ReplicatedStorage.Modules.Constants)

local EPOCH = os.time({year = 2025, month = 1, day = 1, hour = 0, min = 0, sec = 0})

local SeasonService = {}

function SeasonService.currentId(now: number?): number
	now = now or os.time()
	local secondsPerSeason = Constants.SEASON_LENGTH_DAYS * 86400
	return math.floor((now - EPOCH) / secondsPerSeason) + 1
end

function SeasonService.endsAt(seasonId: number): number
	local secondsPerSeason = Constants.SEASON_LENGTH_DAYS * 86400
	return EPOCH + seasonId * secondsPerSeason
end

return SeasonService
