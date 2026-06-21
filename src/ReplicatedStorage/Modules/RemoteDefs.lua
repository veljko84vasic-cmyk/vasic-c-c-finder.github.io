--!strict
-- Names of every RemoteEvent / RemoteFunction. Centralised so client and
-- server agree on the exact strings, and so a fresh contributor can see the
-- full network surface in one file.

return {
	-- Client → Server
	FireShot           = "FireShot",            -- {weaponId, origin, direction, shotIndex, seed, clientTime}
	Reload             = "Reload",
	SwapSlot           = "SwapSlot",
	MeleeSwing         = "MeleeSwing",          -- {kind: "Slash" | "Stab"}
	BuyItem            = "BuyItem",
	JoinQueue          = "JoinQueue",
	LeaveQueue         = "LeaveQueue",
	EquipSkin          = "EquipSkin",           -- {weaponId, skinId}
	InspectWeapon      = "InspectWeapon",
	MovementSnapshot   = "MovementSnapshot",    -- client→server: pos, vel, tick

	-- Server → Client
	MatchFound         = "MatchFound",
	MatchStart         = "MatchStart",
	RoundStart         = "RoundStart",
	RoundEnd           = "RoundEnd",
	MatchEnd           = "MatchEnd",
	HitConfirm         = "HitConfirm",          -- {targetId, dmg, headshot}
	DamageTaken        = "DamageTaken",
	XpAwarded          = "XpAwarded",
	AchievementUnlocked= "AchievementUnlocked",
	RankUpdate         = "RankUpdate",
	InventoryUpdate    = "InventoryUpdate",
	LeaderboardUpdate  = "LeaderboardUpdate",

	-- Request/Response (RemoteFunction)
	GetProfile         = "GetProfile",
	GetLeaderboard     = "GetLeaderboard",
	GetMatchHistory    = "GetMatchHistory",
}
