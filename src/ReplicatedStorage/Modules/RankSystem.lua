--!strict
-- Rank tiers, MMR math, and season helpers.
--
-- MMR uses a simplified Glicko-2 update: rating drift is scaled by opponent
-- RD and outcome margin. RD shrinks as more matches are played and inflates
-- between sessions to keep matchmaking sensitive to layoffs.

local Constants = require(script.Parent.Constants)

local RankSystem = {}

export type Tier = {
	id: string,
	name: string,
	minMMR: number,
	color: Color3,
	divisions: number,
}

RankSystem.Tiers = {
	{ id = "iron",      name = "Iron",      minMMR = 0,    color = Color3.fromRGB(120, 120, 130), divisions = 3 },
	{ id = "bronze",    name = "Bronze",    minMMR = 800,  color = Color3.fromRGB(180, 110, 60),  divisions = 3 },
	{ id = "silver",    name = "Silver",    minMMR = 1100, color = Color3.fromRGB(200, 200, 210), divisions = 3 },
	{ id = "gold",      name = "Gold",      minMMR = 1400, color = Color3.fromRGB(230, 190, 70),  divisions = 3 },
	{ id = "platinum",  name = "Platinum",  minMMR = 1700, color = Color3.fromRGB(110, 200, 220), divisions = 3 },
	{ id = "diamond",   name = "Diamond",   minMMR = 2000, color = Color3.fromRGB(150, 180, 255), divisions = 3 },
	{ id = "master",    name = "Master",    minMMR = 2300, color = Color3.fromRGB(200, 130, 255), divisions = 1 },
	{ id = "champion",  name = "Champion",  minMMR = 2600, color = Color3.fromRGB(255, 80, 120),  divisions = 1 },
	{ id = "vasic",     name = "Vasic",     minMMR = 2900, color = Color3.fromRGB(255, 215, 0),   divisions = 1 },
} :: {Tier}

export type Profile = {
	mmr: number,
	rd: number,       -- rating deviation
	matchesPlayed: number,
	placementsRemaining: number,
	seasonId: number,
	seasonHigh: number,
	winStreak: number,
}

function RankSystem.newProfile(seasonId: number): Profile
	return {
		mmr = 1000,
		rd = 350,
		matchesPlayed = 0,
		placementsRemaining = Constants.PLACEMENT_MATCHES,
		seasonId = seasonId,
		seasonHigh = 1000,
		winStreak = 0,
	}
end

function RankSystem.tierFor(mmr: number): (Tier, number)
	local current = RankSystem.Tiers[1]
	for _, tier in RankSystem.Tiers do
		if mmr >= tier.minMMR then
			current = tier
		end
	end
	-- division 1 is highest; spread divisions across the tier's mmr band
	local nextIdx = table.find(RankSystem.Tiers, current) + 1
	local nextTier = RankSystem.Tiers[nextIdx]
	if not nextTier or current.divisions == 1 then
		return current, 1
	end
	local span = nextTier.minMMR - current.minMMR
	local progress = (mmr - current.minMMR) / span
	local division = current.divisions - math.floor(progress * current.divisions)
	return current, math.clamp(division, 1, current.divisions)
end

-- Compress |delta| toward zero when RDs are low (stable players move slowly)
-- and toward 1 when RDs are high (volatile players move fast).
local function gain(myMMR: number, oppMMR: number, myRD: number, oppRD: number, win: boolean, margin: number): number
	local diff = oppMMR - myMMR
	local expected = 1 / (1 + 10 ^ (-diff / 400))
	local actual = win and 1 or 0
	local k = 20 + (myRD / 350) * 20 + (oppRD / 350) * 10
	return k * (actual - expected) * (0.85 + 0.3 * margin)
end

-- margin in [0,1] — e.g. (roundsWon - roundsLost) / WIN_SCORE
function RankSystem.update(profile: Profile, oppMMR: number, oppRD: number, win: boolean, margin: number): number
	local delta = gain(profile.mmr, oppMMR, profile.rd, oppRD, win, math.clamp(margin, 0, 1))

	-- Placement boost: amplify gains/losses while finding initial rating.
	if profile.placementsRemaining > 0 then
		delta *= 2.0
		profile.placementsRemaining -= 1
	end

	-- Win-streak nudge to compensate against deflation.
	if win then
		profile.winStreak += 1
		if profile.winStreak >= 3 then
			delta += 4
		end
	else
		profile.winStreak = 0
	end

	profile.mmr = math.max(0, math.floor(profile.mmr + delta + 0.5))
	profile.rd = math.max(60, profile.rd - 4)
	profile.matchesPlayed += 1
	if profile.mmr > profile.seasonHigh then
		profile.seasonHigh = profile.mmr
	end
	return delta
end

-- Called once per real-world day to keep matchmaking responsive to layoffs.
function RankSystem.decayRD(profile: Profile, daysSinceLastMatch: number)
	profile.rd = math.min(350, profile.rd + 8 * daysSinceLastMatch)
end

-- Soft-reset: compress toward 1000 by ~30%, keep some signal, refresh RD.
function RankSystem.applySeasonReset(profile: Profile, newSeasonId: number)
	profile.mmr = math.floor(1000 + (profile.mmr - 1000) * 0.7 + 0.5)
	profile.rd = 200
	profile.placementsRemaining = math.floor(Constants.PLACEMENT_MATCHES / 2)
	profile.seasonId = newSeasonId
	profile.seasonHigh = profile.mmr
	profile.winStreak = 0
end

return RankSystem
