package com.vasic.client.hud;

import com.vasic.client.VasicClient;
import com.vasic.client.module.Module;
import com.vasic.client.module.modules.misc.ArmorHud;
import com.vasic.client.module.modules.misc.Timer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

import java.util.List;

public class HudRenderer {

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private long sessionStartTime = System.currentTimeMillis();

    public void render(DrawContext context) {
        if (mc.player == null || mc.options.hudHidden) return;

        TextRenderer textRenderer = mc.textRenderer;
        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();

        renderFpsCounter(context, textRenderer);
        renderCoordinates(context, textRenderer, screenHeight);
        renderActiveModules(context, textRenderer, screenWidth);

        if (ArmorHud.isActive()) {
            renderArmorHud(context, textRenderer, screenWidth, screenHeight);
        }

        if (Timer.isActive()) {
            renderTimer(context, textRenderer, screenWidth);
        }
    }

    private void renderFpsCounter(DrawContext context, TextRenderer textRenderer) {
        String fps = mc.getCurrentFps() + " FPS";
        context.fill(2, 2, textRenderer.getWidth(fps) + 6, 14, 0x88000000);
        context.drawTextWithShadow(textRenderer, fps, 4, 4, getFpsColor(mc.getCurrentFps()));
    }

    private void renderCoordinates(DrawContext context, TextRenderer textRenderer, int screenHeight) {
        if (mc.player == null) return;
        String coords = String.format("X: %.1f  Y: %.1f  Z: %.1f",
                mc.player.getX(), mc.player.getY(), mc.player.getZ());
        String facing = "Facing: " + getFacingDirection();

        int y = screenHeight - 24;
        context.fill(2, y - 2, Math.max(textRenderer.getWidth(coords), textRenderer.getWidth(facing)) + 6, y + 22, 0x88000000);
        context.drawTextWithShadow(textRenderer, coords, 4, y, 0xFFFFFFFF);
        context.drawTextWithShadow(textRenderer, facing, 4, y + 10, 0xFFAAAAAA);
    }

    private void renderActiveModules(DrawContext context, TextRenderer textRenderer, int screenWidth) {
        List<Module> enabled = VasicClient.getInstance().getModuleManager().getEnabledModules();
        int y = 16;
        for (Module module : enabled) {
            String name = module.getName();
            int width = textRenderer.getWidth(name);
            int x = screenWidth - width - 4;
            context.fill(x - 2, y - 1, screenWidth, y + 10, 0x88000000);
            context.drawTextWithShadow(textRenderer, name, x, y, module.getCategory().getColor());
            y += 12;
        }
    }

    private void renderArmorHud(DrawContext context, TextRenderer textRenderer, int screenWidth, int screenHeight) {
        if (mc.player == null) return;

        int x = screenWidth - 20;
        int y = screenHeight / 2 - 40;

        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (EquipmentSlot slot : slots) {
            ItemStack stack = mc.player.getEquippedStack(slot);
            if (!stack.isEmpty()) {
                context.drawItem(stack, x, y);
                if (stack.isDamageable()) {
                    int durability = stack.getMaxDamage() - stack.getDamage();
                    int maxDurability = stack.getMaxDamage();
                    float ratio = (float) durability / maxDurability;
                    int color = ratio > 0.5f ? 0xFF00FF00 : ratio > 0.25f ? 0xFFFFFF00 : 0xFFFF0000;
                    context.drawTextWithShadow(textRenderer, String.valueOf(durability), x - textRenderer.getWidth(String.valueOf(durability)) - 2, y + 4, color);
                }
            }
            y += 20;
        }
    }

    private void renderTimer(DrawContext context, TextRenderer textRenderer, int screenWidth) {
        long elapsed = System.currentTimeMillis() - sessionStartTime;
        long seconds = (elapsed / 1000) % 60;
        long minutes = (elapsed / (1000 * 60)) % 60;
        long hours = elapsed / (1000 * 60 * 60);

        String time = String.format("Session: %02d:%02d:%02d", hours, minutes, seconds);
        int width = textRenderer.getWidth(time);
        int x = (screenWidth - width) / 2;
        context.fill(x - 2, 2, x + width + 2, 14, 0x88000000);
        context.drawTextWithShadow(textRenderer, time, x, 4, 0xFFFFFFFF);
    }

    private int getFpsColor(int fps) {
        if (fps >= 60) return 0xFF00FF00;
        if (fps >= 30) return 0xFFFFFF00;
        return 0xFFFF0000;
    }

    private String getFacingDirection() {
        if (mc.player == null) return "";
        float yaw = mc.player.getYaw() % 360;
        if (yaw < 0) yaw += 360;

        if (yaw >= 315 || yaw < 45) return "South (+Z)";
        if (yaw >= 45 && yaw < 135) return "West (-X)";
        if (yaw >= 135 && yaw < 225) return "North (-Z)";
        return "East (+X)";
    }
}
