package com.vasic.client.hud;

import com.vasic.client.VasicClient;
import com.vasic.client.module.Module;
import com.vasic.client.module.modules.misc.FPSDisplay;
import com.vasic.client.module.modules.misc.CPSCounter;
import com.vasic.client.module.modules.misc.PingDisplay;
import com.vasic.client.module.modules.misc.KeystrokesModule;
import com.vasic.client.module.modules.misc.CoordinatesDisplay;
import com.vasic.client.module.modules.misc.ArmorHud;
import com.vasic.client.module.modules.misc.SaturationDisplay;
import com.vasic.client.module.modules.misc.FoodPreview;
import com.vasic.client.module.modules.misc.ShieldStatus;
import com.vasic.client.module.modules.render.CustomCrosshair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.*;
import java.util.Map;

public class HudRenderer {

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private final long sessionStartTime = System.currentTimeMillis();
    private final LinkedHashMap<String, HudElement> elements = new LinkedHashMap<>();
    private boolean initialized = false;

    public void initDefaults(int sw, int sh) {
        if (initialized) return;
        initialized = true;

        elements.put("fps", new HudElement("fps", "FPS", "FPS", 4, 4, 70, 12));
        elements.put("cps", new HudElement("cps", "CPS", "CPS", 4, 18, 100, 12));
        elements.put("ping", new HudElement("ping", "Ping", "Ping", 4, 32, 55, 12));
        elements.put("saturation", new HudElement("saturation", "Saturation", "Saturation", 4, 46, 75, 12));
        elements.put("coords", new HudElement("coords", "Coords", "Coords", 4, sh - 28, 200, 24));
        elements.put("timer", new HudElement("timer", "Timer", "Timer", sw / 2 - 30, 4, 60, 12));
        elements.put("armor", new HudElement("armor", "Armor", "ArmorHUD", sw - 40, sh / 2 - 44, 38, 80));
        elements.put("food", new HudElement("food", "Food Preview", "FoodPreview", sw / 2 - 50, sh - 58, 100, 14));
        elements.put("shield", new HudElement("shield", "Shield", "ShieldStatus", sw / 2 + 98, sh - 44, 55, 14));
        elements.put("keystrokes", new HudElement("keystrokes", "Keystrokes", "Keystrokes", 8, sh / 2 - 48, 70, 96));
        elements.put("modules", new HudElement("modules", "Modules", "", sw - 90, 4, 88, 120));
        elements.put("crosshair", new HudElement("crosshair", "Crosshair", "Crosshair", sw / 2 - 7, sh / 2 - 7, 15, 15));

        Map<String, int[]> saved = VasicClient.getInstance().getConfigManager().getSavedHudPositions();
        for (Map.Entry<String, int[]> entry : saved.entrySet()) {
            HudElement el = elements.get(entry.getKey());
            if (el != null) {
                el.setPosition(entry.getValue()[0], entry.getValue()[1]);
            }
        }
    }

    public Collection<HudElement> getElements() {
        return elements.values();
    }

    public HudElement getElement(String id) {
        return elements.get(id);
    }

    public void render(DrawContext context) {
        if (mc.player == null || mc.options.hudHidden) return;

        TextRenderer tr = mc.textRenderer;
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        initDefaults(sw, sh);

        if (mc.currentScreen instanceof HudEditorScreen) return;
        if (mc.currentScreen != null) return;

        if (FPSDisplay.isActive()) renderFPS(context, tr);
        if (CPSCounter.isActive()) renderCPS(context, tr);
        if (PingDisplay.isActive()) renderPing(context, tr);
        if (SaturationDisplay.isActive()) renderSaturation(context, tr);
        if (CoordinatesDisplay.isActive()) renderCoords(context, tr);
        if (com.vasic.client.module.modules.misc.Timer.isActive()) renderTimer(context, tr);
        if (ArmorHud.isActive()) renderArmor(context, tr);
        if (FoodPreview.isActive()) renderFood(context, tr);
        if (ShieldStatus.isActive()) renderShield(context, tr);
        if (KeystrokesModule.isActive()) renderKeystrokes(context, tr);
        if (CustomCrosshair.isActive()) renderCrosshair(context);

        renderActiveModules(context, tr);
    }

    private void renderFPS(DrawContext ctx, TextRenderer tr) {
        HudElement el = elements.get("fps");
        int fps = mc.getCurrentFps();
        String text = fps + " FPS";
        el.setSize(tr.getWidth(text) + 4, 12);
        drawTag(ctx, tr, text, el.getX(), el.getY(), getFpsColor(fps));
    }

    private void renderCPS(DrawContext ctx, TextRenderer tr) {
        HudElement el = elements.get("cps");
        String text = "L:" + CPSCounter.getLeftCPS() + " R:" + CPSCounter.getRightCPS();
        el.setSize(tr.getWidth(text) + 4, 12);
        drawTag(ctx, tr, text, el.getX(), el.getY(), 0xFFCCCCCC);
    }

    private void renderPing(DrawContext ctx, TextRenderer tr) {
        HudElement el = elements.get("ping");
        int ping = PingDisplay.getPing();
        String text = ping + "ms";
        int color = ping < 50 ? 0xFF00FF00 : ping < 100 ? 0xFFFFFF00 : 0xFFFF4444;
        el.setSize(tr.getWidth(text) + 4, 12);
        drawTag(ctx, tr, text, el.getX(), el.getY(), color);
    }

    private void renderSaturation(DrawContext ctx, TextRenderer tr) {
        HudElement el = elements.get("saturation");
        float sat = mc.player.getHungerManager().getSaturationLevel();
        String text = String.format("Sat: %.1f", sat);
        int color = sat > 10 ? 0xFF00FF00 : sat > 5 ? 0xFFFFAA00 : 0xFFFF4444;
        el.setSize(tr.getWidth(text) + 4, 12);
        drawTag(ctx, tr, text, el.getX(), el.getY(), color);
    }

    private void renderCoords(DrawContext ctx, TextRenderer tr) {
        HudElement el = elements.get("coords");
        String line1 = String.format("X: %.1f  Y: %.1f  Z: %.1f",
                mc.player.getX(), mc.player.getY(), mc.player.getZ());
        String line2 = getFacingDirection();
        int maxW = Math.max(tr.getWidth(line1), tr.getWidth(line2));
        el.setSize(maxW + 6, 24);
        drawTag(ctx, tr, line1, el.getX(), el.getY(), 0xFFFFFFFF);
        drawTag(ctx, tr, line2, el.getX(), el.getY() + 12, 0xFFAAAAAA);
    }

    private void renderTimer(DrawContext ctx, TextRenderer tr) {
        HudElement el = elements.get("timer");
        long elapsed = System.currentTimeMillis() - sessionStartTime;
        long s = (elapsed / 1000) % 60, m = (elapsed / 60000) % 60, h = elapsed / 3600000;
        String text = String.format("%02d:%02d:%02d", h, m, s);
        el.setSize(tr.getWidth(text) + 6, 12);
        drawTag(ctx, tr, text, el.getX(), el.getY(), 0xFFCCCCCC);
    }

    private void renderArmor(DrawContext ctx, TextRenderer tr) {
        HudElement el = elements.get("armor");
        int x = el.getX(), y = el.getY();
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (EquipmentSlot slot : slots) {
            ItemStack stack = mc.player.getEquippedStack(slot);
            if (!stack.isEmpty()) {
                ctx.drawItem(stack, x, y);
                if (stack.isDamageable()) {
                    int dur = stack.getMaxDamage() - stack.getDamage();
                    float ratio = (float) dur / stack.getMaxDamage();
                    int color = ratio > 0.5f ? 0xFF00FF00 : ratio > 0.25f ? 0xFFFFFF00 : 0xFFFF0000;
                    ctx.drawTextWithShadow(tr, String.valueOf(dur), x + 18, y + 4, color);
                }
            }
            y += 20;
        }
    }

    private void renderFood(DrawContext ctx, TextRenderer tr) {
        HudElement el = elements.get("food");
        ItemStack held = mc.player.getMainHandStack();
        if (held.isEmpty()) return;
        FoodComponent food = held.get(DataComponentTypes.FOOD);
        if (food == null) return;
        String text = "+" + food.nutrition() + " hunger";
        el.setSize(tr.getWidth(text) + 6, 14);
        int x = el.getX(), y = el.getY();
        ctx.fill(x - 3, y - 2, x + tr.getWidth(text) + 3, y + 11, 0xB0000000);
        ctx.drawTextWithShadow(tr, text, x, y, 0xFFFFDD44);
    }

    private void renderShield(DrawContext ctx, TextRenderer tr) {
        HudElement el = elements.get("shield");
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
        int x = el.getX(), y = el.getY();
        ctx.fill(x - 2, y - 2, x + tr.getWidth("SHIELD") + 4, y + 11, 0xB0000000);
        ctx.fill(x - 2, y - 2, x, y + 11, color);
        ctx.drawTextWithShadow(tr, "SHIELD", x + 2, y, color);
    }

    private void renderKeystrokes(DrawContext ctx, TextRenderer tr) {
        HudElement el = elements.get("keystrokes");
        int bx = el.getX(), by = el.getY();
        int box = 22, gap = 2;

        boolean w = mc.options.forwardKey.isPressed();
        boolean a = mc.options.leftKey.isPressed();
        boolean s = mc.options.backKey.isPressed();
        boolean d = mc.options.rightKey.isPressed();
        boolean sp = mc.options.jumpKey.isPressed();
        boolean lmb = mc.options.attackKey.isPressed();
        boolean rmb = mc.options.useKey.isPressed();

        drawKey(ctx, tr, "W", bx + box + gap, by, box, box, w);
        drawKey(ctx, tr, "A", bx, by + box + gap, box, box, a);
        drawKey(ctx, tr, "S", bx + box + gap, by + box + gap, box, box, s);
        drawKey(ctx, tr, "D", bx + (box + gap) * 2, by + box + gap, box, box, d);
        int spW = box * 3 + gap * 2;
        drawKey(ctx, tr, "---", bx, by + (box + gap) * 2, spW, 14, sp);
        int mW = (spW - gap) / 2;
        drawKey(ctx, tr, "LMB", bx, by + (box + gap) * 2 + 16, mW, 16, lmb);
        drawKey(ctx, tr, "RMB", bx + mW + gap, by + (box + gap) * 2 + 16, mW, 16, rmb);
    }

    private void renderCrosshair(DrawContext ctx) {
        HudElement el = elements.get("crosshair");
        int gs = CustomCrosshair.GRID_SIZE;
        int mid = gs / 2;
        int cx = el.getX() + el.getWidth() / 2;
        int cy = el.getY() + el.getHeight() / 2;

        if (CustomCrosshair.hasOutline()) {
            int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1},{-1,-1},{-1,1},{1,-1},{1,1}};
            for (int r = 0; r < gs; r++) {
                for (int c = 0; c < gs; c++) {
                    if (CustomCrosshair.getPixel(r, c) != 0) {
                        for (int[] d : dirs) {
                            int nr = r + d[0], nc = c + d[1];
                            if (nr >= 0 && nr < gs && nc >= 0 && nc < gs && CustomCrosshair.getPixel(nr, nc) == 0) {
                                int px = cx + (nc - mid);
                                int py = cy + (nr - mid);
                                ctx.fill(px, py, px + 1, py + 1, 0xCC000000);
                            }
                        }
                    }
                }
            }
        }

        for (int r = 0; r < gs; r++) {
            for (int c = 0; c < gs; c++) {
                int pixel = CustomCrosshair.getPixel(r, c);
                if (pixel != 0) {
                    int px = cx + (c - mid);
                    int py = cy + (r - mid);
                    ctx.fill(px, py, px + 1, py + 1, pixel);
                }
            }
        }
    }

    private void renderActiveModules(DrawContext ctx, TextRenderer tr) {
        HudElement el = elements.get("modules");
        List<Module> enabled = VasicClient.getInstance().getModuleManager().getEnabledModules();
        int x = el.getX(), y = el.getY();
        int maxW = 0;
        for (Module m : enabled) {
            String name = m.getName();
            int w = tr.getWidth(name);
            if (w > maxW) maxW = w;
            ctx.fill(x - 2, y - 1, x + w + 4, y + 10, 0x80000000);
            ctx.fill(x + w + 3, y - 1, x + w + 4, y + 10, m.getCategory().getColor());
            ctx.drawTextWithShadow(tr, name, x, y, 0xFFDDDDDD);
            y += 12;
        }
        el.setSize(maxW + 6, Math.max(enabled.size() * 12, 12));
    }

    private void drawTag(DrawContext ctx, TextRenderer tr, String text, int x, int y, int color) {
        int w = tr.getWidth(text);
        ctx.fill(x - 1, y - 1, x + w + 3, y + 10, 0x90000000);
        ctx.drawTextWithShadow(tr, text, x + 1, y, color);
    }

    private void drawKey(DrawContext ctx, TextRenderer tr, String label, int x, int y, int w, int h, boolean pressed) {
        ctx.fill(x, y, x + w, y + h, pressed ? 0xDD26C6DA : 0xAA1A1A2E);
        ctx.fill(x, y, x + w, y + 1, 0x40FFFFFF);
        int tx = x + (w - tr.getWidth(label)) / 2;
        int ty = y + (h - 8) / 2;
        ctx.drawTextWithShadow(tr, label, tx, ty, pressed ? 0xFF000000 : 0xFFCCCCCC);
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
