package com.vasic.client.gui;

import com.vasic.client.VasicClient;
import com.vasic.client.module.Category;
import com.vasic.client.module.Module;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class CategoryPanel {

    public static final int WIDTH = 140;
    private static final int HEADER_HEIGHT = 26;

    private final Category category;
    private final List<ModuleButton> buttons = new ArrayList<>();
    private int x, y;
    private boolean dragging;
    private int dragOffsetX, dragOffsetY;
    private boolean expanded = true;

    public CategoryPanel(Category category, int x, int y) {
        this.category = category;
        this.x = x;
        this.y = y;

        int buttonY = 0;
        for (Module module : VasicClient.getInstance().getModuleManager().getModulesByCategory(category)) {
            buttons.add(new ModuleButton(module, buttonY));
            buttonY += ModuleButton.HEIGHT;
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, TextRenderer textRenderer) {
        // Panel background
        int panelHeight = expanded ? HEADER_HEIGHT + buttons.size() * ModuleButton.HEIGHT + 4 : HEADER_HEIGHT;
        context.fill(x, y, x + WIDTH, y + panelHeight, 0xE8101018);

        // Accent line at top
        context.fill(x, y, x + WIDTH, y + 2, category.getColor());

        // Header text
        context.drawTextWithShadow(textRenderer, category.getName(),
                x + 8, y + 9, 0xFFDDDDDD);

        // Expand/collapse arrow
        String arrow = expanded ? "-" : "+";
        context.drawTextWithShadow(textRenderer, arrow,
                x + WIDTH - 14, y + 9, 0xFF888888);

        if (expanded) {
            // Thin separator
            context.fill(x + 6, y + HEADER_HEIGHT - 1, x + WIDTH - 6, y + HEADER_HEIGHT, 0xFF222233);

            for (ModuleButton button : buttons) {
                button.render(context, x, y + HEADER_HEIGHT, WIDTH, mouseX, mouseY, textRenderer);
            }
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isInHeader(mouseX, mouseY)) {
            if (button == 1) {
                expanded = !expanded;
                return true;
            }
            dragging = true;
            dragOffsetX = (int) (mouseX - x);
            dragOffsetY = (int) (mouseY - y);
            return true;
        }

        if (expanded) {
            for (ModuleButton moduleButton : buttons) {
                if (moduleButton.mouseClicked(mouseX, mouseY, x, y + HEADER_HEIGHT, WIDTH, button)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void mouseReleased() {
        dragging = false;
    }

    public boolean mouseDragged(double mouseX, double mouseY) {
        if (dragging) {
            x = (int) (mouseX - dragOffsetX);
            y = (int) (mouseY - dragOffsetY);
            return true;
        }
        return false;
    }

    private boolean isInHeader(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + WIDTH && mouseY >= y && mouseY <= y + HEADER_HEIGHT;
    }
}
