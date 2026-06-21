--!strict
-- Lightweight movement & fire-rate sanity checks. Not a full anti-cheat, but
-- enough to refuse the most common attacks (teleport, speedhack, rapidfire).
-- All combat decisions live on the server; this module just decides whether
-- to trust a given client-supplied tick.

local ReplicatedStorage = game:GetService("ReplicatedStorage")
local Constants = require(ReplicatedStorage.Modules.Constants)
local WeaponConfigs = require(ReplicatedStorage.Modules.WeaponConfigs)

local AntiCheat = {}

export type Trail = {
	lastPosition: Vector3?,
	lastTick: number,
	lastShot: {[string]: number},
	violations: number,
}

local trails: {[number]: Trail} = {}

local function getTrail(userId: number): Trail
	local t = trails[userId]
	if not t then
		t = { lastPosition = nil, lastTick = 0, lastShot = {}, violations = 0 }
		trails[userId] = t
	end
	return t
end

function AntiCheat.checkMovement(userId: number, position: Vector3, now: number): boolean
	local t = getTrail(userId)
	if t.lastPosition then
		local dt = math.max(0.001, now - t.lastTick)
		local horiz = (position - t.lastPosition) * Vector3.new(1, 0, 1)
		local speed = horiz.Magnitude / dt
		-- 1.5× cap leaves room for slide-jump peaks while still catching teleports.
		if speed > Constants.SPEED_CAP_HARD * 1.5 then
			t.violations += 1
			return false
		end
	end
	t.lastPosition = position
	t.lastTick = now
	return true
end

function AntiCheat.checkFireRate(userId: number, weaponId: string, now: number): boolean
	local t = getTrail(userId)
	local cfg = WeaponConfigs.get(weaponId)
	local minInterval = 60 / cfg.rpm * 0.9   -- 10% leniency for jitter
	local last = t.lastShot[weaponId] or 0
	if now - last < minInterval then
		t.violations += 1
		return false
	end
	t.lastShot[weaponId] = now
	return true
end

function AntiCheat.checkHitLine(origin: Vector3, target: Vector3): boolean
	return (target - origin).Magnitude <= Constants.MAX_SHOT_DISTANCE
end

function AntiCheat.violations(userId: number): number
	local t = trails[userId]
	return t and t.violations or 0
end

function AntiCheat.clear(userId: number)
	trails[userId] = nil
end

return AntiCheat
