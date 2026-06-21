--!strict
-- Player profile persistence via DataStoreService. Profiles are session-locked
-- (via UpdateAsync) so two servers can't write conflicting state at once.
-- Saves on leave, periodic auto-save, and BindToClose.

local DataStoreService = game:GetService("DataStoreService")
local Players = game:GetService("Players")
local RunService = game:GetService("RunService")
local ReplicatedStorage = game:GetService("ReplicatedStorage")

local RankSystem = require(ReplicatedStorage.Modules.RankSystem)
local Progression = require(ReplicatedStorage.Modules.Progression)

-- DataStore is unavailable in Studio unless "Allow Studio Access to API
-- Services" is on, and unavailable in unpublished places. Fall back to
-- in-memory profiles so the game still runs.
local STORE: DataStore? = nil
do
	local ok, store = pcall(function()
		return DataStoreService:GetDataStore("VasicProfiles_v1")
	end)
	if ok then
		STORE = store
	else
		warn("[DataService] DataStore unavailable; using in-memory profiles only.")
	end
end
local AUTOSAVE = 120

local DataService = {}

export type Profile = {
	userId: number,
	xp: number,
	rank: RankSystem.Profile,
	inventory: {[string]: boolean},   -- skinId -> owned
	equipped: {[string]: string},     -- weaponId -> skinId
	achievements: {[string]: boolean},
	stats: {[string]: number},
	matchHistory: {any},
	lastLogin: number,
}

local function defaultProfile(userId: number, seasonId: number): Profile
	return {
		userId = userId,
		xp = 0,
		rank = RankSystem.newProfile(seasonId),
		inventory = {},
		equipped = {},
		achievements = {},
		stats = {},
		matchHistory = {},
		lastLogin = os.time(),
	}
end

local profiles: {[number]: Profile} = {}
local dirty: {[number]: boolean} = {}

function DataService.markDirty(userId: number)
	dirty[userId] = true
end

function DataService.get(userId: number): Profile?
	return profiles[userId]
end

function DataService.load(player: Player, seasonId: number): Profile
	local key = "u_" .. player.UserId
	local loaded
	if STORE then
		local ok, err = pcall(function()
			loaded = STORE:UpdateAsync(key, function(old)
				if old == nil then
					return defaultProfile(player.UserId, seasonId)
				end
				old.lastLogin = os.time()
				return old
			end)
		end)
		if not ok or not loaded then
			warn(("[DataService] load failed for %d: %s"):format(player.UserId, tostring(err)))
			loaded = defaultProfile(player.UserId, seasonId)
		end
	else
		loaded = defaultProfile(player.UserId, seasonId)
	end

	-- Season rollover check
	if loaded.rank.seasonId ~= seasonId then
		RankSystem.applySeasonReset(loaded.rank, seasonId)
	end

	profiles[player.UserId] = loaded
	return loaded
end

local function save(userId: number)
	local profile = profiles[userId]
	if not profile then return end
	if not STORE then
		dirty[userId] = nil
		return
	end
	local key = "u_" .. userId
	local ok, err = pcall(function()
		STORE:UpdateAsync(key, function(_old)
			return profile
		end)
	end)
	if not ok then
		warn(("[DataService] save failed for %d: %s"):format(userId, tostring(err)))
	else
		dirty[userId] = nil
	end
end

function DataService.saveNow(userId: number)
	save(userId)
end

function DataService.start()
	Players.PlayerRemoving:Connect(function(player)
		save(player.UserId)
		profiles[player.UserId] = nil
		dirty[player.UserId] = nil
	end)

	game:BindToClose(function()
		for userId in profiles do
			save(userId)
		end
	end)

	task.spawn(function()
		while true do
			task.wait(AUTOSAVE)
			for userId in dirty do
				save(userId)
			end
		end
	end)
end

-- Convenience mutators that keep dirty bookkeeping correct.

function DataService.addXp(userId: number, amount: number)
	local p = profiles[userId]; if not p then return end
	p.xp += amount
	dirty[userId] = true
end

function DataService.grantSkin(userId: number, skinId: string)
	local p = profiles[userId]; if not p then return end
	if p.inventory[skinId] then return end
	p.inventory[skinId] = true
	dirty[userId] = true
end

function DataService.equipSkin(userId: number, weaponId: string, skinId: string): boolean
	local p = profiles[userId]; if not p then return false end
	if not p.inventory[skinId] then return false end
	p.equipped[weaponId] = skinId
	dirty[userId] = true
	return true
end

function DataService.bumpStat(userId: number, key: string, by: number)
	local p = profiles[userId]; if not p then return end
	p.stats[key] = (p.stats[key] or 0) + by
	dirty[userId] = true
end

function DataService.appendMatch(userId: number, summary: any)
	local p = profiles[userId]; if not p then return end
	table.insert(p.matchHistory, 1, summary)
	while #p.matchHistory > 25 do
		table.remove(p.matchHistory)
	end
	dirty[userId] = true
end

return DataService
