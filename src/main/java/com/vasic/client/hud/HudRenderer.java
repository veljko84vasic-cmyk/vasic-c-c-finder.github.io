package com.vasic.client.hud;

import com.vasic.client.VasicClient;
import com.vasic.client.module.Module;
import com.vasic.client.module.modules.misc.*;
import com.vasic.client.module.modules.render.CustomCrosshair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.List;

public class HudRenderer {

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final long sessionStartTime = System.currentTimeMillis();

    public void render(DrawContext context) {
        if (mc.player == null || mc.options.hudHidden) return;
        if (mc.currentScreen != null) return;

        TextRenderer tr = mc.textRenderer;
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        int topLeftY = 4;

        // FPS
        if (FPSDisplay.isActive()) {
            int fps = mc.getCurrentFps();
            String text = fps + " FPS";
            drawTag(context, tr, text, 4, topLeftY, getFpsColor(fps));
            topLeftY += 14;
        }

        // CPS
        if (CPSCounter.isActive()) {
            String text = "L: " + CPSCounter.getLeftCPS() + " | R: " + CPSCounter.getRightCPS();
            drawTag(context, tr, text, 4, topLeftY, 0xFFCCCCCC);
            topLeftY += 14;
        }

        // Ping
        if (PingDisplay.isActive()) {
            int ping = PingDisplay.getPing();
            int color = ping < 50 ? 0xFF00FF00 : ping < 100 ? 0xFFFFFF00 : 0xFFFF4444;
            drawTag(context, tr, ping + "ms", 4, topLeftY, color);
            topLeftY += 14;
        }

        // Saturation
        if (SaturationDisplay.isActive()) {
            float sat = mc.player.getHungerManager().getSaturationLevel();
            int color = sat > 10 ? 0xFF00FF00 : sat > 5 ? 0xFFFFAA00 : 0xFFFF4444;
            drawTag(context, tr, String.format("Sat: %.1f", sat), 4, topLeftY, color);
            topLeftY += 14;
        }

        // Active modules list (top right)
        renderActiveModules(context, tr, sw);

        // Coordinates (bottom left)
        if (CoordinatesDisplay.isActive()) {
            renderCoordinates(context, tr, sh);
        }

        // Timer (top center)
        if (Timer.isActive()) {
            renderTimer(context, tr, sw);
        }

        // Armor HUD (right center)
        if (ArmorHud.isActive()) {
            renderArmorHud(context, tr, sw, sh);
        }

        // Food Preview (above hotbar)
        if (FoodPreview.isActive()) {
            renderFoodPreview(context, tr, sw, sh);
        }

        // Shield Status (near hotbar)
        if (ShieldStatus.isActive()) {
            renderShieldStatus(context, tr, sw, sh);
        }

        // Keystrokes (left center)
        if (KeystrokesModule.isActive()) {
            renderKeystrokes(context, tr, sh);
        }

        // Custom crosshair
        if (CustomCrosshair.isActive()) {
            renderCrosshair(context, sw, sh);
        }
    }

    private void drawTag(DrawContext ctx, TextRenderer tr, String text, int x, int y, int color) {
        int w = tr.getWidth(text);
        ctx.fill(x - 1, y - 1, x + w + 3, y + 10, 0x90000000);
        ctx.drawTextWithShadow(tr, text, x + 1, y, color);
    }

    private void renderActiveModules(DrawContext ctx, TextRenderer tr, int sw) {
        List<Module> enabled = VasicClient.getInstance().getModuleManager().getEnabledModules();
        int y = 4;
        for (Module m : enabled) {
            String name = m.getName();
            int w = tr.getWidth(name);
            int x = sw - w - 4;
            ctx.fill(x - 2, y - 1, sw, y + 10, 0x80000000);
            ctx.fill(sw - 1, y - 1, sw, y + 10, m.getCategory().getColor());
            ctx.drawTextWithShadow(tr, name, x, y, 0xFFDDDDDD);
            y += 12;
        }
    }

    private void renderCoordinates(DrawContext ctx, TextRenderer tr, int sh) {
        String coords = String.format("X: %.1f  Y: %.1f  Z: %.1f",
                mc.player.getX(), mc.player.getY(), mc.player.getZ());
        String facing = getFacingDirection();

        int y = sh - 26;
        drawTag(ctx, tr, coords, 4, y, 0xFFFFFFFF);
        drawTag(ctx, tr, facing, 4, y + 12, 0xFFAAAAAA);
    }

    private void renderTimer(DrawContext ctx, TextRenderer tr, int sw) {
        long elapsed = System.currentTimeMillis() - sessionStartTime;
        long s = (elapsed / 1000) % 60;
        long m = (elapsed / 60000) % 60;
        long h = elapsed / 3600000;

        String time = String.format("%02d:%02d:%02d", h, m, s);
        int w = tr.getWidth(time);
        int x = (sw - w) / 2;
        ctx.fill(x - 3, 2, x + w + 3, 14, 0x90000000);
        ctx.drawTextWithShadow(tr, time, x, 4, 0xFFCCCCCC);
    }

    private void renderArmorHud(DrawContext ctx, TextRenderer tr, int sw, int sh) {
        int x = sw - 22;
        int y = sh / 2 - 44;

        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (EquipmentSlot slot : slots) {
            ItemStack stack = mc.player.getEquippedStack(slot);
            if (!stack.isEmpty()) {
                ctx.drawItem(stack, x, y);
                if (stack.isDamageable()) {
                    int dur = stack.getMaxDamage() - stack.getDamage();
                    float ratio = (float) dur / stack.getMaxDamage();
                    int color = ratio > 0.5f ? 0xFF00FF00 : ratio > 0.25f ? 0xFFFFFF00 : 0xFFFF0000;
                    String durText = String.valueOf(dur);
                    ctx.drawTextWithShadow(tr, durText, x - tr.getWidth(durText) - 2, y + 4, color);
                }
            }
            y += 20;
        }
    }

    private void renderFoodPreview(DrawContext ctx, TextRenderer tr, int sw, int sh) {
        if (mc.player == null) return;
        ItemStack held = mc.player.getMainHandStack();
        if (held.isEmpty()) return;

        var foodComp = held.getItem().getFoodComponent();
        if (foodComp != null) {
            int hunger = foodComp.getHunger();
            float saturation = foodComp.getSaturationModifier();
            String text = "+" + hunger + " hunger  +" + String.format("%.1f", saturation * hunger * 2) + " sat";
            int w = tr.getWidth(text);
            int x = sw / 2 - w / 2;
            int y = sh - 56;
            ctx.fill(x - 3, y - 2, x + w + 3, y + 11, 0xB0000000);
            ctx.drawTextWithShadow(tr, text, x, y, 0xFFFFDD44);
        }
    }

    private void renderShieldStatus(DrawContext ctx, TextRenderer tr, int sw, int sh) {
        if (mc.player == null) return;

        boolean hasShield = false;
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.SHIELD)) {
                hasShield = true;
                break;
            }
        }
        if (!hasShield) return;

        boolean blocking = mc.player.isBlocking();
        int color = blocking ? 0xFF44FF44 : 0xFFFF4444;
        String text = blocking ? "SHIELD" : "SHIELD";

        int x = sw / 2 + 100;
        int y = sh - 42;

        ctx.fill(x - 2, y - 2, x + tr.getWidth(text) + 4, y + 11, 0xB0000000);
        ctx.fill(x - 2, y - 2, x, y + 11, color);
        ctx.drawTextWithShadow(tr, text, x + 2, y, color);
    }

    private void renderKeystrokes(DrawContext ctx, TextRenderer tr, int sh) {
        int baseX = 8;
        int baseY = sh / 2 - 40;
        int boxSize = 22;
        int gap = 2;

        boolean w = mc.options.forwardKey.isPressed();
        boolean a = mc.options.leftKey.isPressed();
        boolean s = mc.options.backKey.isPressed();
        boolean d = mc.options.rightKey.isPressed();
        boolean space = mc.options.jumpKey.isPressed();
        boolean lmb = mc.options.attackKey.isPressed();
        boolean rmb = mc.options.useKey.isPressed();

        // W
        drawKey(ctx, tr, "W", baseX + boxSize + gap, baseY, boxSize, boxSize, w);
        // A S D
        drawKey(ctx, tr, "A", baseX, baseY + boxSize + gap, boxSize, boxSize, a);
        drawKey(ctx, tr, "S", baseX + boxSize + gap, baseY + boxSize + gap, boxSize, boxSize, s);
        drawKey(ctx, tr, "D", baseX + (boxSize + gap) * 2, baseY + boxSize + gap, boxSize, boxSize, d);
        // Space
        int spaceWidth = boxSize * 3 + gap * 2;
        drawKey(ctx, tr, "---", baseX, baseY + (boxSize + gap) * 2, spaceWidth, 14, space);
        // LMB RMB
        int mouseWidth = (spaceWidth - gap) / 2;
        drawKey(ctx, tr, "LMB", baseX, baseY + (boxSize + gap) * 2 + 16, mouseWidth, 16, lmb);
        drawKey(ctx, tr, "RMB", baseX + mouseWidth + gap, baseY + (boxSize + gap) * 2 + 16, mouseWidth, 16, rmb);
    }

    private void drawKey(DrawContext ctx, TextRenderer tr, String label, int x, int y, int w, int h, boolean pressed) {
        int bg = pressed ? 0xDD26C6DA : 0xAA1A1A2E;
        int textColor = pressed ? 0xFF000000 : 0xFFCCCCCC;
        ctx.fill(x, y, x + w, y + h, bg);
        ctx.fill(x, y, x + w, y + 1, 0x40FFFFFF);
        int tx = x + (w - tr.getWidth(label)) / 2;
        int ty = y + (h - 8) / 2;
        ctx.drawTextWithShadow(tr, label, tx, ty, textColor);
    }

    private void renderCrosshair(DrawContext ctx, int sw, int sh) {
        int cx = sw / 2;
        int cy = sh / 2;
        int color = CustomCrosshair.getColor();
        int size = CustomCrosshair.getSize();
        int g = CustomCrosshair.getGap();
        int t = CustomCrosshair.getThickness();

        // Top
        ctx.fill(cx - t / 2, cy - g - size, cx + t / 2 + 1, cy - g, color);
        // Bottom
        ctx.fill(cx - t / 2, cy + g + 1, cx + t / 2 + 1, cy + g + size + 1, color);
        // Left
        ctx.fill(cx - g - size, cy - t / 2, cx - g, cy + t / 2 + 1, color);
        // Right
        ctx.fill(cx + g + 1, cy - t / 2, cx + g + size + 1, cy + t / 2 + 1, color);

        // Center dot
        if (CustomCrosshair.hasDot()) {
            ctx.fill(cx, cy, cx + 1, cy + 1, color);
        }
    }

    private int getFpsColor(int fps) {
        if (fps >= 60) return 0xFF00FF00;
        if (fps >= 30) return 0xFFFFFF00;
        return 0xFFFF4444;
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
