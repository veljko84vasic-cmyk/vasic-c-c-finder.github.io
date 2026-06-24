package com.vasic.client.gui;

import com.vasic.client.VasicClient;
import com.vasic.client.module.Category;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ClickGui extends Screen {

    private final List<CategoryPanel> panels = new ArrayList<>();

    public ClickGui() {
        super(Text.literal("Vasic Client"));
    }

    @Override
    protected void init() {
        panels.clear();
        int totalWidth = Category.values().length * (CategoryPanel.WIDTH + 8) - 8;
        int startX = (width - totalWidth) / 2;
        int x = startX;
        for (Category category : Category.values()) {
            panels.add(new CategoryPanel(category, x, 40));
            x += CategoryPanel.WIDTH + 8;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Dark overlay
        context.fill(0, 0, width, height, 0xB0000000);

        // Title bar
        String title = VasicClient.NAME;
        String version = "v" + VasicClient.VERSION;
        context.drawTextWithShadow(textRenderer, title, width / 2 - textRenderer.getWidth(title) / 2, 12, 0xFF26C6DA);
        context.drawTextWithShadow(textRenderer, version,
                width / 2 + textRenderer.getWidth(title) / 2 + 4, 12, 0xFF666666);

        for (CategoryPanel panel : panels) {
            panel.render(context, mouseX, mouseY, textRenderer);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (CategoryPanel panel : panels) {
            if (panel.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (CategoryPanel panel : panels) {
            panel.mouseReleased();
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (CategoryPanel panel : panels) {
            if (panel.mouseDragged(mouseX, mouseY)) {
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
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
