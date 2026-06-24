package com.vasic.client.gui;

import com.vasic.client.module.Module;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class ModuleButton {

    public static final int HEIGHT = 20;
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

        // Background
        if (hovered) {
            context.fill(x + 2, y, x + width - 2, y + HEIGHT, 0x30FFFFFF);
        }

        // Module name
        int textColor = module.isEnabled() ? 0xFFFFFFFF : 0xFF777777;
        context.drawTextWithShadow(textRenderer, module.getName(), x + 8, y + 6, textColor);

        // Toggle circle
        int circleX = x + width - 16;
        int circleY = y + 7;
        int circleSize = 6;

        if (module.isEnabled()) {
            context.fill(circleX, circleY, circleX + circleSize, circleY + circleSize, module.getCategory().getColor());
        } else {
            context.fill(circleX, circleY, circleX + circleSize, circleY + circleSize, 0xFF333344);
            context.fill(circleX + 1, circleY + 1, circleX + circleSize - 1, circleY + circleSize - 1, 0xFF1A1A22);
        }

        // Keybind hint
        if (module.getKeyBind() != 0 && module.getKeyBind() != -1) {
            String keyName = org.lwjgl.glfw.GLFW.glfwGetKeyName(module.getKeyBind(), 0);
            if (keyName != null) {
                int keyWidth = textRenderer.getWidth("[" + keyName.toUpperCase() + "]");
                context.drawTextWithShadow(textRenderer, "[" + keyName.toUpperCase() + "]",
                        circleX - keyWidth - 4, y + 6, 0xFF444455);
            }
        }
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
