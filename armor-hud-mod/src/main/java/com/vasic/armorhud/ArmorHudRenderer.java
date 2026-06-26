package com.vasic.armorhud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public class ArmorHudRenderer {

    public static void register(HudConfig config) {
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            if (!config.visible) return;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.options.hudHidden) return;

            render(drawContext, client, config);
        });
    }

    private static void render(DrawContext context, MinecraftClient client, HudConfig config) {
        var inventory = client.player.getInventory();

        // Index 3=helmet, 2=chestplate, 1=leggings, 0=boots
        ItemStack[] armor = {
            inventory.getArmorStack(3),
            inventory.getArmorStack(2),
            inventory.getArmorStack(1),
            inventory.getArmorStack(0),
        };
        ItemStack offhand = client.player.getOffHandStack();

        float scale = config.scale;
        int baseX = (int) config.x;
        int baseY = (int) config.y;
        int slotStep = (int) (20 * scale);

        int slot = 0;
        for (int i = 0; i < 4; i++) {
            if (armor[i].isEmpty()) continue;
            int dx = config.horizontal ? baseX + slot * slotStep : baseX;
            int dy = config.horizontal ? baseY : baseY + slot * slotStep;
            drawEntry(context, client, armor[i], dx, dy, scale, config);
            slot++;
        }

        if (config.showOffhand && !offhand.isEmpty()) {
            int dx = config.horizontal ? baseX + slot * slotStep : baseX;
            int dy = config.horizontal ? baseY : baseY + slot * slotStep;
            drawEntry(context, client, offhand, dx, dy, scale, config);
        }
    }

    private static void drawEntry(DrawContext context, MinecraftClient client,
                                   ItemStack stack, int x, int y, float scale, HudConfig config) {
        MatrixStack matrices = context.getMatrices();

        // Draw item scaled
        matrices.push();
        matrices.scale(scale, scale, 1f);
        context.drawItem(stack, (int) (x / scale), (int) (y / scale));
        matrices.pop();

        // Draw durability text if item can break
        if (stack.isDamageable()) {
            String text = durabilityText(stack, config.showPercentage);
            int color = config.colorCoded ? durabilityColor(stack) : 0xFFFFFF;

            int textX = x + (int) (17 * scale);
            int textY = y + (int) (4 * scale);

            matrices.push();
            matrices.translate(textX, textY, 0);
            matrices.scale(scale, scale, 1f);
            context.drawTextWithShadow(client.textRenderer, text, 0, 0, color);
            matrices.pop();
        }
    }

    private static String durabilityText(ItemStack stack, boolean percentage) {
        int max = stack.getMaxDamage();
        int remaining = max - stack.getDamage();
        if (percentage) {
            return (int) ((float) remaining / max * 100) + "%";
        }
        return String.valueOf(remaining);
    }

    private static int durabilityColor(ItemStack stack) {
        float ratio = 1f - (float) stack.getDamage() / stack.getMaxDamage();
        if (ratio > 0.6f) return 0x55FF55;
        if (ratio > 0.3f) return 0xFFFF55;
        return 0xFF5555;
    }
}
