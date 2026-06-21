--!strict
-- Game-wide tunables. Mirror of Source-engine sv_* convars where applicable.

local Constants = {}

Constants.TICK_RATE = 64
Constants.STEP = 1 / Constants.TICK_RATE

-- Movement (units in studs/s — Roblox default character ~16 studs/s walk)
Constants.MAX_WALK_SPEED       = 20
Constants.MAX_RUN_SPEED        = 28      -- Rivals-ish sprint top speed
Constants.MAX_CROUCH_SPEED     = 10
Constants.MAX_AIR_SPEED        = 1.5     -- per-tick air-accel speed cap (low = strafe-only gain)
Constants.GROUND_ACCEL         = 220     -- snappier start/stop
Constants.AIR_ACCEL            = 120     -- high air accel + low MAX_AIR_SPEED = Source feel
Constants.FRICTION             = 6.5
Constants.STOP_SPEED           = 3
Constants.JUMP_IMPULSE         = 55
Constants.GRAVITY              = 196.2
Constants.SLIDE_BOOST          = 12
Constants.SLIDE_FRICTION       = 1.2
Constants.SLIDE_MIN_ENTRY      = 14      -- can slide right out of a walk
Constants.SLIDE_DURATION       = 1.1
Constants.BHOP_WINDOW          = 0.08    -- seconds after landing to auto-jump cleanly
Constants.SPEED_CAP_HARD       = 80      -- anti-cheat hard ceiling

-- Crouch
Constants.STAND_HIPHEIGHT      = 2.0
Constants.CROUCH_HIPHEIGHT     = 1.0

-- Combat
Constants.HEADSHOT_MULTIPLIER  = 4.0
Constants.LEG_MULTIPLIER       = 0.75
Constants.MAX_SHOT_DISTANCE    = 400

-- Match
Constants.ROUND_TIME           = 115     -- seconds
Constants.BUY_TIME             = 20
Constants.WIN_SCORE            = 13
Constants.MAX_TEAM_SIZE        = 5

-- Rank / season
Constants.SEASON_LENGTH_DAYS   = 90
Constants.PLACEMENT_MATCHES    = 10

return Constants
