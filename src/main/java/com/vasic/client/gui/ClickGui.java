package com.vasic.client.gui;

import com.vasic.client.VasicClient;
import com.vasic.client.hud.HudEditorScreen;
import com.vasic.client.module.Category;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ClickGui extends Screen {

    private final List<CategoryPanel> panels = new ArrayList<>();
    private int hudButtonX, hudButtonY, hudButtonW, hudButtonH;

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
        context.fill(0, 0, width, height, 0xB0000000);

        // Title
        String title = VasicClient.NAME;
        String version = "v" + VasicClient.VERSION;
        context.drawTextWithShadow(textRenderer, title, width / 2 - textRenderer.getWidth(title) / 2, 10, 0xFF26C6DA);
        context.drawTextWithShadow(textRenderer, version,
                width / 2 + textRenderer.getWidth(title) / 2 + 4, 10, 0xFF666666);

        // Edit HUD button
        String hudText = "[ Edit HUD Layout ]";
        hudButtonW = textRenderer.getWidth(hudText) + 8;
        hudButtonH = 14;
        hudButtonX = width / 2 - hudButtonW / 2;
        hudButtonY = 24;
        boolean hudHovered = mouseX >= hudButtonX && mouseX <= hudButtonX + hudButtonW
                && mouseY >= hudButtonY && mouseY <= hudButtonY + hudButtonH;
        context.fill(hudButtonX, hudButtonY, hudButtonX + hudButtonW, hudButtonY + hudButtonH,
                hudHovered ? 0x60FFFFFF : 0x30FFFFFF);
        context.drawTextWithShadow(textRenderer, hudText, hudButtonX + 4, hudButtonY + 3,
                hudHovered ? 0xFF26C6DA : 0xFF999999);

        for (CategoryPanel panel : panels) {
            panel.render(context, mouseX, mouseY, textRenderer);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check Edit HUD button
        if (button == 0 && mouseX >= hudButtonX && mouseX <= hudButtonX + hudButtonW
                && mouseY >= hudButtonY && mouseY <= hudButtonY + hudButtonH) {
            client.setScreen(new HudEditorScreen());
            return true;
        }

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
