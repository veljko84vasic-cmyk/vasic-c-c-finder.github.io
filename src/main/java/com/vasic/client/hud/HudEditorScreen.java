package com.vasic.client.hud;

import com.vasic.client.VasicClient;
import com.vasic.client.module.Module;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.Collection;

public class HudEditorScreen extends Screen {

    public HudEditorScreen() {
        super(Text.literal("HUD Editor"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x60000000);

        Collection<HudElement> elements = VasicClient.getInstance().getHudRenderer().getElements();

        for (HudElement el : elements) {
            boolean enabled = isElementEnabled(el);
            int borderColor = el.isDragging() ? 0xFF26C6DA : enabled ? 0xAAFFFFFF : 0x55FF4444;
            int bgColor = el.isDragging() ? 0x4026C6DA : enabled ? 0x20FFFFFF : 0x15FF4444;

            int sw = el.getScaledWidth();
            int sh = el.getScaledHeight();

            context.fill(el.getX(), el.getY(), el.getX() + sw, el.getY() + sh, bgColor);

            context.fill(el.getX(), el.getY(), el.getX() + sw, el.getY() + 1, borderColor);
            context.fill(el.getX(), el.getY() + sh - 1, el.getX() + sw, el.getY() + sh, borderColor);
            context.fill(el.getX(), el.getY(), el.getX() + 1, el.getY() + sh, borderColor);
            context.fill(el.getX() + sw - 1, el.getY(), el.getX() + sw, el.getY() + sh, borderColor);

            int labelColor = enabled ? 0xFFFFFFFF : 0x66FFFFFF;
            context.drawTextWithShadow(textRenderer, el.getDisplayName(),
                    el.getX() + 2, el.getY() + (sh - 8) / 2, labelColor);

            // Scale label
            int pct = Math.round(el.getScale() * 100);
            if (pct != 100) {
                String scaleText = pct + "%";
                context.drawTextWithShadow(textRenderer, scaleText,
                        el.getX(), el.getY() + sh + 2, 0xFF26C6DA);
            }

            if (!el.getModuleName().isEmpty()) {
                int bx = el.getX() + sw - 10;
                int by = el.getY() - 2;
                boolean hovered = mouseX >= bx && mouseX <= bx + 10 && mouseY >= by && mouseY <= by + 10;

                int xBg = enabled ? (hovered ? 0xDDFF2222 : 0xAAFF4444) : (hovered ? 0xDD22CC22 : 0xAA44AA44);
                context.fill(bx, by, bx + 10, by + 10, xBg);

                String symbol = enabled ? "x" : "+";
                int symColor = hovered ? 0xFFFFFFFF : 0xFFDDDDDD;
                context.drawTextWithShadow(textRenderer, symbol, bx + 2, by + 1, symColor);
            }
        }

        String hint = "Drag to move | Scroll to scale | X to toggle | ESC to save";
        int hintW = textRenderer.getWidth(hint);
        context.fill(width / 2 - hintW / 2 - 6, height - 22, width / 2 + hintW / 2 + 6, height - 6, 0xCC000000);
        context.drawTextWithShadow(textRenderer, hint, width / 2 - hintW / 2, height - 18, 0xFF26C6DA);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        for (HudElement el : VasicClient.getInstance().getHudRenderer().getElements()) {
            if (!el.getModuleName().isEmpty() && el.isCloseButtonHit(mouseX, mouseY)) {
                Module mod = VasicClient.getInstance().getModuleManager().getModule(el.getModuleName());
                if (mod != null) {
                    mod.toggle();
                }
                return true;
            }
        }

        for (HudElement el : VasicClient.getInstance().getHudRenderer().getElements()) {
            if (el.contains(mouseX, mouseY)) {
                el.startDrag(mouseX, mouseY);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (HudElement el : VasicClient.getInstance().getHudRenderer().getElements()) {
            if (el.isDragging()) {
                el.updateDrag(mouseX, mouseY);
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (HudElement el : VasicClient.getInstance().getHudRenderer().getElements()) {
            el.stopDrag();
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (HudElement el : VasicClient.getInstance().getHudRenderer().getElements()) {
            if (el.contains(mouseX, mouseY)) {
                el.setScale(el.getScale() + (float) (verticalAmount * 0.1));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void close() {
        VasicClient.getInstance().getConfigManager().save();
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private boolean isElementEnabled(HudElement el) {
        if (el.getModuleName().isEmpty()) return true;
        Module mod = VasicClient.getInstance().getModuleManager().getModule(el.getModuleName());
        return mod != null && mod.isEnabled();
    }
}
