--!strict
-- EquipSkin handler. Validates ownership and that the skin matches the
-- weapon it's being applied to before mutating the profile.

local ReplicatedStorage = game:GetService("ReplicatedStorage")

local SkinRegistry = require(ReplicatedStorage.Modules.SkinRegistry)
local DataService = require(script.Parent.DataService)

local Remotes = ReplicatedStorage:WaitForChild("Remotes")
local EquipSkinRE      = Remotes:WaitForChild("EquipSkin") :: RemoteEvent
local InventoryUpdateRE = Remotes:WaitForChild("InventoryUpdate") :: RemoteEvent

local InventoryService = {}

function InventoryService.start()
	EquipSkinRE.OnServerEvent:Connect(function(player, payload)
		if typeof(payload) ~= "table" then return end
		local weaponId, skinId = payload.weaponId, payload.skinId
		if type(weaponId) ~= "string" or type(skinId) ~= "string" then return end

		local skin = SkinRegistry.get(skinId)
		if not skin or skin.weapon ~= weaponId then return end

		local profile = DataService.get(player.UserId)
		if not profile or not profile.inventory[skinId] then return end

		if DataService.equipSkin(player.UserId, weaponId, skinId) then
			InventoryUpdateRE:FireClient(player, {
				equipped = profile.equipped,
				inventory = profile.inventory,
			})
		end
	end)
end

return InventoryService
