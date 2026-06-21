--!strict
-- XP curve, level table, achievement registry, and unlock resolution.
--
-- Skins earned through progression are still cosmetic only — Progression
-- only mutates inventory, never weapon stats.

local Progression = {}

-- Geometric XP curve: each level needs ~10% more than the previous, capped.
function Progression.xpForLevel(level: number): number
	if level <= 1 then return 0 end
	return math.floor(1000 * (1.1 ^ (level - 1)))
end

function Progression.totalXpForLevel(level: number): number
	local total = 0
	for i = 1, level - 1 do
		total += Progression.xpForLevel(i + 1)
	end
	return total
end

function Progression.levelFromXp(totalXp: number): (number, number, number)
	local level = 1
	local accumulated = 0
	while true do
		local need = Progression.xpForLevel(level + 1)
		if accumulated + need > totalXp then break end
		accumulated += need
		level += 1
		if level >= 999 then break end
	end
	local intoLevel = totalXp - accumulated
	local nextNeed = Progression.xpForLevel(level + 1)
	return level, intoLevel, nextNeed
end

export type Achievement = {
	id: string,
	name: string,
	description: string,
	xp: number,
	reward: {kind: "Skin" | "Charm" | "Title", id: string}?,
	stat: string,           -- key in player stats blob
	threshold: number,
}

Progression.Achievements = {
	{ id = "first_blood",   name = "First Blood",     description = "Win your first round.",
	  xp = 250, stat = "roundsWon", threshold = 1, reward = nil },
	{ id = "marksman_1",    name = "Marksman I",      description = "Land 100 headshots.",
	  xp = 500, stat = "headshots", threshold = 100,
	  reward = { kind = "Skin", id = "ak47_redline" } },
	{ id = "marksman_2",    name = "Marksman II",     description = "Land 1000 headshots.",
	  xp = 1500, stat = "headshots", threshold = 1000,
	  reward = { kind = "Skin", id = "ak47_vulcan" } },
	{ id = "knife_artist",  name = "Knife Artist",    description = "Get 50 knife kills.",
	  xp = 750, stat = "knifeKills", threshold = 50,
	  reward = { kind = "Skin", id = "karambit_fade" } },
	{ id = "untouchable",   name = "Untouchable",     description = "Win a round without taking damage.",
	  xp = 400, stat = "flawlessRounds", threshold = 1, reward = nil },
	{ id = "ace",           name = "Ace",             description = "Eliminate all 5 enemies in a single round.",
	  xp = 1000, stat = "aces", threshold = 1,
	  reward = { kind = "Charm", id = "ace_charm" } },
	{ id = "speedrunner",   name = "Speedrunner",     description = "Maintain 30+ stud/s for 60 seconds total.",
	  xp = 600, stat = "fastSeconds", threshold = 60,
	  reward = { kind = "Title", id = "speedrunner" } },
	{ id = "competitor",    name = "Competitor",      description = "Play 100 ranked matches.",
	  xp = 2000, stat = "rankedMatches", threshold = 100,
	  reward = { kind = "Skin", id = "butterfly_tigertooth" } },
} :: {Achievement}

-- Returns the list of achievements newly completed by `stats`.
function Progression.checkUnlocks(stats: {[string]: number}, unlocked: {[string]: boolean}): {Achievement}
	local out = {}
	for _, ach in Progression.Achievements do
		if not unlocked[ach.id] and (stats[ach.stat] or 0) >= ach.threshold then
			table.insert(out, ach)
		end
	end
	return out
end

return Progression
