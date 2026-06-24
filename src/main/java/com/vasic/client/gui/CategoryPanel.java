package com.vasic.client.gui;

import com.vasic.client.VasicClient;
import com.vasic.client.module.Category;
import com.vasic.client.module.Module;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class CategoryPanel {

    public static final int WIDTH = 120;
    private static final int HEADER_HEIGHT = 22;

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
        context.fill(x, y, x + WIDTH, y + HEADER_HEIGHT, category.getColor());
        context.drawCenteredTextWithShadow(textRenderer, category.getName(),
                x + WIDTH / 2, y + 7, 0xFFFFFFFF);

        String arrow = expanded ? "v" : ">";
        context.drawTextWithShadow(textRenderer, arrow, x + WIDTH - 12, y + 7, 0xFFFFFFFF);

        if (expanded) {
            int panelHeight = buttons.size() * ModuleButton.HEIGHT;
            context.fill(x, y + HEADER_HEIGHT, x + WIDTH, y + HEADER_HEIGHT + panelHeight, 0xCC1A1A2E);

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
