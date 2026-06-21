--!strict
-- Forces every player into an R6 (classic blocky) rig on spawn. Runs as a
-- standalone server script so character settings stay independent of Main.

local Players = game:GetService("Players")

local function makeBlocky(character: Model)
	local humanoid = character:WaitForChild("Humanoid") :: Humanoid
	if humanoid.RigType == Enum.HumanoidRigType.R6 then return end
	local ok, desc = pcall(function()
		return humanoid:GetAppliedDescription()
	end)
	if not ok or not desc then
		desc = Instance.new("HumanoidDescription")
	end
	pcall(function()
		humanoid:ApplyDescription(desc, Enum.HumanoidRigType.R6)
	end)
end

Players.PlayerAdded:Connect(function(player)
	player.CharacterAdded:Connect(makeBlocky)
end)

for _, player in Players:GetPlayers() do
	if player.Character then
		task.spawn(makeBlocky, player.Character)
	end
	player.CharacterAdded:Connect(makeBlocky)
end
