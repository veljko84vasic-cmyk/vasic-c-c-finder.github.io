--!strict
-- Unified input across keyboard/mouse, gamepad, and touch. Other controllers
-- subscribe to the action stream rather than touching UserInputService
-- directly, so we keep platform branches contained to this file.

local UserInputService = game:GetService("UserInputService")
local Players = game:GetService("Players")
local GuiService = game:GetService("GuiService")

local InputAdapter = {}

export type ActionState = {
	moveX: number,        -- [-1, 1] left/right
	moveY: number,        -- [-1, 1] back/forward
	lookDelta: Vector2,   -- per-frame look delta in pixels (mouse) or scaled stick
	jump: boolean,
	crouch: boolean,
	slide: boolean,
	sprint: boolean,
	fire: boolean,
	aim: boolean,
	reload: boolean,
	swapPrimary: boolean,
	swapSecondary: boolean,
	swapMelee: boolean,
	inspect: boolean,
}

local state: ActionState = {
	moveX = 0, moveY = 0, lookDelta = Vector2.zero,
	jump = false, crouch = false, slide = false, sprint = false,
	fire = false, aim = false, reload = false,
	swapPrimary = false, swapSecondary = false, swapMelee = false,
	inspect = false,
}

local listeners: {(state: ActionState) -> ()} = {}

function InputAdapter.subscribe(fn: (state: ActionState) -> ()): () -> ()
	table.insert(listeners, fn)
	return function()
		for i, f in listeners do
			if f == fn then table.remove(listeners, i); break end
		end
	end
end

function InputAdapter.get(): ActionState
	return state
end

local function dispatch()
	for _, fn in listeners do fn(state) end
end

-- Keyboard / mouse / gamepad ---------------------------------------------------

local KEY_BINDS = {
	[Enum.KeyCode.W] = "forward",
	[Enum.KeyCode.S] = "back",
	[Enum.KeyCode.A] = "left",
	[Enum.KeyCode.D] = "right",
	[Enum.KeyCode.Space] = "jump",
	[Enum.KeyCode.LeftControl] = "crouch",
	[Enum.KeyCode.LeftShift] = "sprint",
	[Enum.KeyCode.C] = "slide",
	[Enum.KeyCode.R] = "reload",
	[Enum.KeyCode.One] = "swapPrimary",
	[Enum.KeyCode.Two] = "swapSecondary",
	[Enum.KeyCode.Three] = "swapMelee",
	[Enum.KeyCode.F] = "inspect",
}

local axisHold = { forward = false, back = false, left = false, right = false }

local function updateMoveAxis()
	state.moveX = (axisHold.right and 1 or 0) - (axisHold.left and 1 or 0)
	state.moveY = (axisHold.forward and 1 or 0) - (axisHold.back and 1 or 0)
end

UserInputService.InputBegan:Connect(function(input, processed)
	if processed then return end
	if input.UserInputType == Enum.UserInputType.MouseButton1 then
		state.fire = true
	elseif input.UserInputType == Enum.UserInputType.MouseButton2 then
		state.aim = true
	elseif input.UserInputType == Enum.UserInputType.Keyboard then
		local bind = KEY_BINDS[input.KeyCode]
		if bind == "forward" or bind == "back" or bind == "left" or bind == "right" then
			axisHold[bind] = true
			updateMoveAxis()
		elseif bind then
			(state :: any)[bind] = true
		end
	end
	dispatch()
end)

UserInputService.InputEnded:Connect(function(input)
	if input.UserInputType == Enum.UserInputType.MouseButton1 then
		state.fire = false
	elseif input.UserInputType == Enum.UserInputType.MouseButton2 then
		state.aim = false
	elseif input.UserInputType == Enum.UserInputType.Keyboard then
		local bind = KEY_BINDS[input.KeyCode]
		if bind == "forward" or bind == "back" or bind == "left" or bind == "right" then
			axisHold[bind] = false
			updateMoveAxis()
		elseif bind then
			(state :: any)[bind] = false
		end
	end
	dispatch()
end)

UserInputService.InputChanged:Connect(function(input)
	if input.UserInputType == Enum.UserInputType.MouseMovement then
		state.lookDelta = Vector2.new(input.Delta.X, input.Delta.Y)
		dispatch()
		state.lookDelta = Vector2.zero
	end
end)

-- Touch (mobile) ---------------------------------------------------------------
-- Build a virtual stick + buttons only when the device is touch-only. On
-- hybrid devices we still let the player switch modes via Settings later.

local function makeTouchUI()
	local player = Players.LocalPlayer
	local gui = Instance.new("ScreenGui")
	gui.Name = "TouchControls"
	gui.IgnoreGuiInset = true
	gui.ResetOnSpawn = false
	gui.Parent = player:WaitForChild("PlayerGui")

	local function button(name: string, position: UDim2, size: UDim2, label: string): TextButton
		local b = Instance.new("TextButton")
		b.Name = name
		b.AnchorPoint = Vector2.new(0.5, 0.5)
		b.Position = position
		b.Size = size
		b.BackgroundTransparency = 0.4
		b.BackgroundColor3 = Color3.fromRGB(20, 20, 20)
		b.TextColor3 = Color3.new(1, 1, 1)
		b.TextScaled = true
		b.Text = label
		b.AutoButtonColor = false
		b.Parent = gui
		return b
	end

	-- Left stick — drag inside a circular zone to set moveX/moveY.
	local stickZone = Instance.new("Frame")
	stickZone.Name = "MoveStick"
	stickZone.Position = UDim2.new(0, 30, 1, -210)
	stickZone.Size = UDim2.new(0, 180, 0, 180)
	stickZone.BackgroundTransparency = 0.7
	stickZone.BackgroundColor3 = Color3.fromRGB(0, 0, 0)
	stickZone.BorderSizePixel = 0
	stickZone.Parent = gui
	local stickCorner = Instance.new("UICorner", stickZone); stickCorner.CornerRadius = UDim.new(1, 0)

	local stickKnob = Instance.new("Frame")
	stickKnob.Size = UDim2.new(0, 70, 0, 70)
	stickKnob.Position = UDim2.new(0.5, -35, 0.5, -35)
	stickKnob.BackgroundColor3 = Color3.fromRGB(180, 180, 180)
	stickKnob.BackgroundTransparency = 0.2
	stickKnob.BorderSizePixel = 0
	stickKnob.Parent = stickZone
	local knobCorner = Instance.new("UICorner", stickKnob); knobCorner.CornerRadius = UDim.new(1, 0)

	local activeTouch: InputObject?
	local stickCenter
	stickZone.InputBegan:Connect(function(input)
		if input.UserInputType == Enum.UserInputType.Touch then
			activeTouch = input
			stickCenter = stickZone.AbsolutePosition + stickZone.AbsoluteSize / 2
		end
	end)
	UserInputService.InputChanged:Connect(function(input)
		if input == activeTouch and stickCenter then
			local delta = Vector2.new(input.Position.X, input.Position.Y) - stickCenter
			local radius = stickZone.AbsoluteSize.X / 2
			local clamped = delta
			if delta.Magnitude > radius then
				clamped = delta.Unit * radius
			end
			stickKnob.Position = UDim2.new(0.5, clamped.X - 35, 0.5, clamped.Y - 35)
			state.moveX = math.clamp(clamped.X / radius, -1, 1)
			state.moveY = math.clamp(-clamped.Y / radius, -1, 1)
			-- Auto-sprint when stick is pushed past 80%
			state.sprint = clamped.Magnitude / radius > 0.8
			dispatch()
		end
	end)
	UserInputService.InputEnded:Connect(function(input)
		if input == activeTouch then
			activeTouch = nil
			stickKnob.Position = UDim2.new(0.5, -35, 0.5, -35)
			state.moveX, state.moveY, state.sprint = 0, 0, false
			dispatch()
		end
	end)

	-- Right side: action buttons
	local jump = button("Jump", UDim2.new(1, -120, 1, -120), UDim2.new(0, 100, 0, 100), "JUMP")
	local crouch = button("Crouch", UDim2.new(1, -240, 1, -120), UDim2.new(0, 100, 0, 100), "CRCH")
	local slide = button("Slide", UDim2.new(1, -180, 1, -240), UDim2.new(0, 90, 0, 90), "SLD")
	local fire = button("Fire", UDim2.new(1, -120, 1, -370), UDim2.new(0, 120, 0, 120), "FIRE")
	local aim = button("Aim", UDim2.new(1, -260, 1, -370), UDim2.new(0, 90, 0, 90), "AIM")
	local reload = button("Reload", UDim2.new(1, -380, 1, -240), UDim2.new(0, 90, 0, 90), "RL")

	local function bindHold(btn: TextButton, key: string)
		btn.InputBegan:Connect(function(i)
			if i.UserInputType == Enum.UserInputType.Touch then
				(state :: any)[key] = true; dispatch()
			end
		end)
		btn.InputEnded:Connect(function(i)
			if i.UserInputType == Enum.UserInputType.Touch then
				(state :: any)[key] = false; dispatch()
			end
		end)
	end
	bindHold(jump, "jump")
	bindHold(crouch, "crouch")
	bindHold(slide, "slide")
	bindHold(fire, "fire")
	bindHold(aim, "aim")
	bindHold(reload, "reload")

	-- Right half of screen drags look — exclude action button area.
	local actionsRight = 400
	local lookTouch: InputObject?
	local lookOrigin: Vector2?
	UserInputService.TouchStarted:Connect(function(input, processed)
		if processed then return end
		local pos = input.Position
		local viewport = workspace.CurrentCamera.ViewportSize
		if pos.X > viewport.X * 0.5 and pos.X < viewport.X - actionsRight and not lookTouch then
			lookTouch = input
			lookOrigin = Vector2.new(pos.X, pos.Y)
		end
	end)
	UserInputService.TouchMoved:Connect(function(input)
		if input == lookTouch and lookOrigin then
			local newPos = Vector2.new(input.Position.X, input.Position.Y)
			state.lookDelta = newPos - lookOrigin
			lookOrigin = newPos
			dispatch()
			state.lookDelta = Vector2.zero
		end
	end)
	UserInputService.TouchEnded:Connect(function(input)
		if input == lookTouch then
			lookTouch = nil
			lookOrigin = nil
		end
	end)
end

if UserInputService.TouchEnabled and not UserInputService.MouseEnabled then
	makeTouchUI()
end

return InputAdapter
