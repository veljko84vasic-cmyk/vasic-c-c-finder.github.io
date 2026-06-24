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
        int x = 20;
        for (Category category : Category.values()) {
            panels.add(new CategoryPanel(category, x, 20));
            x += CategoryPanel.WIDTH + 10;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        context.drawTextWithShadow(textRenderer, VasicClient.NAME + " v" + VasicClient.VERSION,
                4, 4, 0xFFAAAAAA);

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
