package com.vasic.client.hud;

import com.vasic.client.VasicClient;
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
            int borderColor = el.isDragging() ? 0xFF26C6DA : 0xAAFFFFFF;
            int bgColor = el.isDragging() ? 0x4026C6DA : 0x20FFFFFF;

            // Background
            context.fill(el.getX(), el.getY(), el.getX() + el.getWidth(), el.getY() + el.getHeight(), bgColor);

            // Border
            context.fill(el.getX(), el.getY(), el.getX() + el.getWidth(), el.getY() + 1, borderColor);
            context.fill(el.getX(), el.getY() + el.getHeight() - 1, el.getX() + el.getWidth(), el.getY() + el.getHeight(), borderColor);
            context.fill(el.getX(), el.getY(), el.getX() + 1, el.getY() + el.getHeight(), borderColor);
            context.fill(el.getX() + el.getWidth() - 1, el.getY(), el.getX() + el.getWidth(), el.getY() + el.getHeight(), borderColor);

            // Label
            context.drawTextWithShadow(textRenderer, el.getDisplayName(),
                    el.getX() + 2, el.getY() + (el.getHeight() - 8) / 2, 0xFFFFFFFF);
        }

        // Instructions
        String hint = "Drag elements to move them. Press ESC to save.";
        int hintW = textRenderer.getWidth(hint);
        context.fill(width / 2 - hintW / 2 - 6, height - 22, width / 2 + hintW / 2 + 6, height - 6, 0xCC000000);
        context.drawTextWithShadow(textRenderer, hint, width / 2 - hintW / 2, height - 18, 0xFF26C6DA);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

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
    public void close() {
        VasicClient.getInstance().getConfigManager().save();
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
