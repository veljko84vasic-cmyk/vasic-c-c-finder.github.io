--!strict
-- Deterministic recoil + spread. Server reproduces the same numbers from the
-- shot's index in the burst, so the client can predict crosshair kick without
-- the server trusting client-supplied angles.

local Recoil = {}

export type State = {
	shotIndex: number,
	lastShot: number,
	resetTime: number,
}

function Recoil.newState(): State
	return { shotIndex = 0, lastShot = 0, resetTime = 0.35 }
end

function Recoil.step(state: State, now: number, pattern: {Vector2}): Vector2
	if now - state.lastShot > state.resetTime then
		state.shotIndex = 0
	end
	state.shotIndex = math.min(state.shotIndex + 1, #pattern)
	state.lastShot = now
	return pattern[state.shotIndex]
end

-- Spread: project a cone offset onto a forward vector. Deterministic given
-- a shared seed so the server can validate.
function Recoil.coneOffset(seed: number, forward: Vector3, halfAngle: number): Vector3
	local rng = Random.new(seed)
	local up = Vector3.yAxis
	local right = forward:Cross(up)
	if right.Magnitude < 0.001 then
		right = Vector3.xAxis
	end
	right = right.Unit
	local trueUp = right:Cross(forward).Unit
	local theta = rng:NextNumber(0, math.pi * 2)
	local r = math.tan(halfAngle) * math.sqrt(rng:NextNumber())
	return (forward + right * math.cos(theta) * r + trueUp * math.sin(theta) * r).Unit
end

return Recoil
