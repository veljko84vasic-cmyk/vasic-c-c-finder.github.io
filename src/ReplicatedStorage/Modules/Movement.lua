--!strict
-- Source-engine-style movement math, isolated and side-effect free so it can
-- run identically on the client (prediction) and the server (authoritative).
--
-- The shape is intentionally close to Quake/CS: friction + accelerate +
-- airaccelerate, with an extremely low MAX_AIR_SPEED so air control comes
-- from strafing rather than from raw input acceleration. This is what gives
-- bunny-hopping its skill expression.

local Constants = require(script.Parent.Constants)

local Movement = {}

export type State = {
	position: Vector3,
	velocity: Vector3,
	onGround: boolean,
	crouched: boolean,
	sliding: boolean,
	slideTime: number,
	lastJumpTime: number,
	lastLandTime: number,
}

function Movement.newState(position: Vector3): State
	return {
		position = position,
		velocity = Vector3.zero,
		onGround = true,
		crouched = false,
		sliding = false,
		slideTime = 0,
		lastJumpTime = -1,
		lastLandTime = 0,
	}
end

-- Friction: linear ramp-down toward zero, with a "stop speed" floor that
-- makes deceleration crisper at low speed (matches Source).
local function applyFriction(velocity: Vector3, dt: number): Vector3
	local speed = Vector3.new(velocity.X, 0, velocity.Z).Magnitude
	if speed < 0.1 then
		return Vector3.new(0, velocity.Y, 0)
	end
	local control = math.max(speed, Constants.STOP_SPEED)
	local drop = control * Constants.FRICTION * dt
	local newSpeed = math.max(0, speed - drop) / speed
	return Vector3.new(velocity.X * newSpeed, velocity.Y, velocity.Z * newSpeed)
end

-- The classic accelerate(): only the projection of velocity onto wishdir
-- contributes to the cap, so strafing perpendicular to motion always adds
-- speed up to addSpeed each tick. This is the heart of air strafing.
local function accelerate(velocity: Vector3, wishDir: Vector3, wishSpeed: number, accel: number, dt: number): Vector3
	if wishDir.Magnitude < 0.001 then
		return velocity
	end
	local currentSpeed = velocity:Dot(wishDir)
	local addSpeed = wishSpeed - currentSpeed
	if addSpeed <= 0 then
		return velocity
	end
	local accelSpeed = math.min(accel * dt * wishSpeed, addSpeed)
	return velocity + wishDir * accelSpeed
end

export type Input = {
	moveDir: Vector3,    -- normalised XZ input in world space
	jump: boolean,
	crouch: boolean,
	slide: boolean,
	sprint: boolean,
}

-- Advance the state by one tick. Pure function aside from mutating `state`.
function Movement.step(state: State, input: Input, dt: number, now: number)
	-- Sliding takes priority over walk/crouch caps.
	local sliding = state.sliding
	if sliding then
		state.slideTime -= dt
		if state.slideTime <= 0 or not state.onGround then
			sliding = false
			state.sliding = false
		end
	end

	if state.onGround then
		if input.slide and not sliding and not state.crouched then
			local horiz = Vector3.new(state.velocity.X, 0, state.velocity.Z).Magnitude
			if horiz >= Constants.SLIDE_MIN_ENTRY then
				sliding = true
				state.sliding = true
				state.slideTime = Constants.SLIDE_DURATION
				local dir = Vector3.new(state.velocity.X, 0, state.velocity.Z).Unit
				state.velocity = Vector3.new(
					state.velocity.X + dir.X * Constants.SLIDE_BOOST,
					state.velocity.Y,
					state.velocity.Z + dir.Z * Constants.SLIDE_BOOST
				)
			end
		end
		state.crouched = input.crouch and not sliding
	end

	local wishSpeed
	if sliding then
		wishSpeed = 0    -- no extra accel during slide; friction handles it
	elseif state.crouched then
		wishSpeed = Constants.MAX_CROUCH_SPEED
	elseif input.sprint then
		wishSpeed = Constants.MAX_RUN_SPEED
	else
		wishSpeed = Constants.MAX_WALK_SPEED
	end

	-- Ground branch
	if state.onGround then
		if sliding then
			-- Custom low friction for slides
			local v = state.velocity
			local speed = Vector3.new(v.X, 0, v.Z).Magnitude
			if speed > 0.1 then
				local drop = speed * Constants.SLIDE_FRICTION * dt
				local scale = math.max(0, speed - drop) / speed
				state.velocity = Vector3.new(v.X * scale, v.Y, v.Z * scale)
			end
		else
			state.velocity = applyFriction(state.velocity, dt)
			state.velocity = accelerate(state.velocity, input.moveDir, wishSpeed, Constants.GROUND_ACCEL, dt)
		end

		-- Jump: classic bhop window — count any jump within BHOP_WINDOW of
		-- landing as a perfect hop and don't reapply friction this tick.
		if input.jump then
			local sinceLand = now - state.lastLandTime
			if sinceLand <= Constants.BHOP_WINDOW then
				-- restore pre-friction speed by undoing one tick of friction
				local v = state.velocity
				local horiz = Vector3.new(v.X, 0, v.Z).Magnitude
				if horiz > 0 then
					local restored = horiz / math.max(0.0001, 1 - Constants.FRICTION * dt)
					local scale = restored / horiz
					state.velocity = Vector3.new(v.X * scale, v.Y, v.Z * scale)
				end
			end
			state.velocity = Vector3.new(state.velocity.X, Constants.JUMP_IMPULSE, state.velocity.Z)
			state.onGround = false
			state.lastJumpTime = now
			state.sliding = false
		end
	else
		-- Air branch — Source-style: very low MAX_AIR_SPEED cap means players
		-- only gain speed by strafing into mouse turns.
		state.velocity = accelerate(state.velocity, input.moveDir, Constants.MAX_AIR_SPEED, Constants.AIR_ACCEL, dt)
		state.velocity = Vector3.new(
			state.velocity.X,
			state.velocity.Y - Constants.GRAVITY * dt,
			state.velocity.Z
		)
	end

	-- Hard cap as a sanity net (anti-cheat also checks).
	local horiz = Vector3.new(state.velocity.X, 0, state.velocity.Z)
	if horiz.Magnitude > Constants.SPEED_CAP_HARD then
		local clamped = horiz.Unit * Constants.SPEED_CAP_HARD
		state.velocity = Vector3.new(clamped.X, state.velocity.Y, clamped.Z)
	end

	state.position = state.position + state.velocity * dt
end

-- Called by the controller when a landing is detected.
function Movement.onLand(state: State, now: number)
	state.onGround = true
	state.lastLandTime = now
end

return Movement
