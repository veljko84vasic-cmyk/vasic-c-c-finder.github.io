--!strict
-- Melee configs. All knives share the same combat numbers — only animation
-- timings and inspect flavour differ. Skins layer purely cosmetic models
-- (Karambit / Butterfly / Bayonet / Huntsman / Skeleton etc.) on top.

export type SwingKind = "Slash" | "Stab"

export type KnifeConfig = {
	id: string,
	displayName: string,
	slashDamage: number,
	stabDamage: number,
	slashBackMult: number,    -- backstab bonus on slash
	stabBackMult: number,
	slashCooldown: number,
	stabCooldown: number,
	range: number,
	drawTime: number,
	moveSpeedMult: number,    -- melee out = fastest move
}

local Configs: {[string]: KnifeConfig} = {
	karambit = {
		id = "karambit", displayName = "Karambit",
		slashDamage = 35, stabDamage = 65,
		slashBackMult = 2.3, stabBackMult = 3.0,
		slashCooldown = 0.4, stabCooldown = 1.0,
		range = 4.5, drawTime = 0.4, moveSpeedMult = 1.08,
	},
	butterfly = {
		id = "butterfly", displayName = "Butterfly Knife",
		slashDamage = 35, stabDamage = 65,
		slashBackMult = 2.3, stabBackMult = 3.0,
		slashCooldown = 0.42, stabCooldown = 1.05,
		range = 4.4, drawTime = 0.55, moveSpeedMult = 1.08,
	},
	bayonet = {
		id = "bayonet", displayName = "Bayonet",
		slashDamage = 35, stabDamage = 65,
		slashBackMult = 2.3, stabBackMult = 3.0,
		slashCooldown = 0.4, stabCooldown = 1.0,
		range = 4.7, drawTime = 0.4, moveSpeedMult = 1.08,
	},
	huntsman = {
		id = "huntsman", displayName = "Huntsman",
		slashDamage = 35, stabDamage = 65,
		slashBackMult = 2.3, stabBackMult = 3.0,
		slashCooldown = 0.4, stabCooldown = 1.0,
		range = 4.5, drawTime = 0.4, moveSpeedMult = 1.08,
	},
	skeleton = {
		id = "skeleton", displayName = "Skeleton Knife",
		slashDamage = 35, stabDamage = 65,
		slashBackMult = 2.3, stabBackMult = 3.0,
		slashCooldown = 0.4, stabCooldown = 1.0,
		range = 4.5, drawTime = 0.4, moveSpeedMult = 1.08,
	},
}

local Module = {}

function Module.get(id: string): KnifeConfig
	local cfg = Configs[id]
	assert(cfg, "Unknown knife id: " .. id)
	return cfg
end

function Module.all(): {[string]: KnifeConfig}
	return Configs
end

return Module
