--!strict
-- Authoritative weapon stats keyed by weapon id. Skins reference these by id
-- and may NOT override any field here — see SkinRegistry for the cosmetic
-- fields skins can carry.

export type FireMode = "Semi" | "Auto" | "Burst"
export type SlotKind = "Primary" | "Secondary" | "Melee" | "Utility"

export type WeaponConfig = {
	id: string,
	displayName: string,
	slot: SlotKind,
	fireMode: FireMode,
	rpm: number,             -- rounds per minute
	damage: number,          -- base body damage
	headMult: number,
	legMult: number,
	magazine: number,
	reserve: number,
	reloadTime: number,
	drawTime: number,
	spread: number,          -- radians of cone, hipfire
	spreadAim: number,       -- when aiming
	recoilPattern: {Vector2}, -- 30-shot pattern (deg yaw/pitch)
	moveSpeedMult: number,   -- carrying penalty (cosmetic-equivalent only on melee)
	burstCount: number?,     -- if fireMode == Burst
}

local function makeRecoil(verticalStart: number, verticalEnd: number, horizDrift: number, shots: number): {Vector2}
	local out = {}
	for i = 1, shots do
		local t = (i - 1) / (shots - 1)
		local vert = verticalStart + (verticalEnd - verticalStart) * t
		local horiz = math.sin(t * math.pi * 2.4) * horizDrift * (0.4 + t * 0.6)
		out[i] = Vector2.new(horiz, vert)
	end
	return out
end

local Configs: {[string]: WeaponConfig} = {
	ak47 = {
		id = "ak47", displayName = "AK-47", slot = "Primary",
		fireMode = "Auto", rpm = 600,
		damage = 36, headMult = 4.0, legMult = 0.75,
		magazine = 30, reserve = 90, reloadTime = 2.4, drawTime = 0.45,
		spread = math.rad(1.1), spreadAim = math.rad(0.25),
		recoilPattern = makeRecoil(0.6, 1.6, 0.6, 30),
		moveSpeedMult = 0.92,
	},
	m4a4 = {
		id = "m4a4", displayName = "M4A4", slot = "Primary",
		fireMode = "Auto", rpm = 666,
		damage = 30, headMult = 4.0, legMult = 0.75,
		magazine = 30, reserve = 90, reloadTime = 2.6, drawTime = 0.45,
		spread = math.rad(0.9), spreadAim = math.rad(0.18),
		recoilPattern = makeRecoil(0.5, 1.3, 0.45, 30),
		moveSpeedMult = 0.92,
	},
	awp = {
		id = "awp", displayName = "AWP", slot = "Primary",
		fireMode = "Semi", rpm = 41,
		damage = 115, headMult = 4.0, legMult = 0.75,
		magazine = 10, reserve = 30, reloadTime = 3.6, drawTime = 1.0,
		spread = math.rad(8), spreadAim = 0,
		recoilPattern = makeRecoil(3.5, 3.5, 0, 1),
		moveSpeedMult = 0.7,
	},
	deagle = {
		id = "deagle", displayName = "Desert Eagle", slot = "Secondary",
		fireMode = "Semi", rpm = 240,
		damage = 60, headMult = 4.0, legMult = 0.75,
		magazine = 7, reserve = 35, reloadTime = 2.2, drawTime = 0.4,
		spread = math.rad(1.4), spreadAim = math.rad(0.4),
		recoilPattern = makeRecoil(2.0, 2.0, 0.2, 7),
		moveSpeedMult = 1.0,
	},
	glock = {
		id = "glock", displayName = "Glock-18", slot = "Secondary",
		fireMode = "Semi", rpm = 400,
		damage = 22, headMult = 4.0, legMult = 0.75,
		magazine = 20, reserve = 120, reloadTime = 2.0, drawTime = 0.35,
		spread = math.rad(1.2), spreadAim = math.rad(0.5),
		recoilPattern = makeRecoil(0.8, 1.2, 0.3, 20),
		moveSpeedMult = 1.0,
	},
}

local Module = {}

function Module.get(id: string): WeaponConfig
	local cfg = Configs[id]
	assert(cfg, "Unknown weapon id: " .. id)
	return cfg
end

function Module.all(): {[string]: WeaponConfig}
	return Configs
end

return Module
