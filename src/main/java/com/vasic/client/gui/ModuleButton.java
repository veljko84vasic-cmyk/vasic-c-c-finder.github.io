package com.vasic.client.gui;

import com.vasic.client.module.Module;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class ModuleButton {

    public static final int HEIGHT = 18;
    private final Module module;
    private final int offsetY;

    public ModuleButton(Module module, int offsetY) {
        this.module = module;
        this.offsetY = offsetY;
    }

    public void render(DrawContext context, int panelX, int panelY, int width,
                       int mouseX, int mouseY, TextRenderer textRenderer) {
        int x = panelX;
        int y = panelY + offsetY;

        boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + HEIGHT;

        int bgColor;
        if (module.isEnabled()) {
            bgColor = hovered ? 0xCC3D5AFE : 0xCC2979FF;
        } else {
            bgColor = hovered ? 0xCC333355 : 0xCC222244;
        }
        context.fill(x, y, x + width, y + HEIGHT, bgColor);

        context.drawTextWithShadow(textRenderer, module.getName(),
                x + 4, y + 5, module.isEnabled() ? 0xFFFFFFFF : 0xFFAAAAAA);

        String status = module.isEnabled() ? "ON" : "OFF";
        int statusColor = module.isEnabled() ? 0xFF00FF00 : 0xFFFF4444;
        int statusWidth = textRenderer.getWidth(status);
        context.drawTextWithShadow(textRenderer, status,
                x + width - statusWidth - 4, y + 5, statusColor);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int panelX, int panelY,
                                int width, int button) {
        int x = panelX;
        int y = panelY + offsetY;

        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + HEIGHT) {
            if (button == 0) {
                module.toggle();
                return true;
            }
        }
        return false;
    }
}
