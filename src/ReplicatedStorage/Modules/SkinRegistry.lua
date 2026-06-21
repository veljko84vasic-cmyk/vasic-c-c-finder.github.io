--!strict
-- Cosmetic-only skin registry. Skins are keyed by `weapon` (gun or knife id)
-- and carry ONLY visual fields. There is no `damage`, `recoil`, `rpm`, or
-- movement field — the stat path goes through WeaponConfigs / KnifeConfigs.
--
-- Rarity follows the familiar CS pattern only as a visual/economy label and
-- has no in-match effect.

export type Rarity = "Consumer" | "Industrial" | "MilSpec" | "Restricted" | "Classified" | "Covert" | "Contraband"

export type Skin = {
	id: string,
	weapon: string,        -- weapon or knife id this skin applies to
	displayName: string,
	rarity: Rarity,
	viewmodelTexture: string?,
	worldmodelTexture: string?,
	tracerColor: Color3?,
	muzzleColor: Color3?,
	inspectAnim: string?,
	hasStatTrak: boolean?,
}

local Skins: {[string]: Skin} = {
	-- Knife skins (Karambit family)
	["karambit_fade"] = {
		id = "karambit_fade", weapon = "karambit",
		displayName = "Karambit | Fade", rarity = "Covert",
		viewmodelTexture = "rbxassetid://0",
	},
	["karambit_doppler"] = {
		id = "karambit_doppler", weapon = "karambit",
		displayName = "Karambit | Doppler", rarity = "Covert",
	},
	["karambit_marble"] = {
		id = "karambit_marble", weapon = "karambit",
		displayName = "Karambit | Marble Fade", rarity = "Covert",
	},
	-- Butterfly
	["butterfly_tigertooth"] = {
		id = "butterfly_tigertooth", weapon = "butterfly",
		displayName = "Butterfly | Tiger Tooth", rarity = "Covert",
	},
	["butterfly_crimsonweb"] = {
		id = "butterfly_crimsonweb", weapon = "butterfly",
		displayName = "Butterfly | Crimson Web", rarity = "Covert",
	},
	-- Gun skins
	["ak47_redline"] = {
		id = "ak47_redline", weapon = "ak47",
		displayName = "AK-47 | Redline", rarity = "Classified",
		tracerColor = Color3.fromRGB(220, 40, 40),
	},
	["ak47_vulcan"] = {
		id = "ak47_vulcan", weapon = "ak47",
		displayName = "AK-47 | Vulcan", rarity = "Covert",
	},
	["m4a4_howl"] = {
		id = "m4a4_howl", weapon = "m4a4",
		displayName = "M4A4 | Howl", rarity = "Contraband",
	},
	["awp_dragonlore"] = {
		id = "awp_dragonlore", weapon = "awp",
		displayName = "AWP | Dragon Lore", rarity = "Covert",
		tracerColor = Color3.fromRGB(255, 180, 60),
	},
}

local Module = {}

function Module.get(id: string): Skin?
	return Skins[id]
end

function Module.forWeapon(weapon: string): {Skin}
	local out = {}
	for _, skin in Skins do
		if skin.weapon == weapon then
			table.insert(out, skin)
		end
	end
	return out
end

function Module.all(): {[string]: Skin}
	return Skins
end

return Module
