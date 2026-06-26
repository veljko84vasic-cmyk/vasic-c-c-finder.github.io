package com.vasic.client.gui;

import com.vasic.client.module.modules.render.CustomCrosshair;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class CrosshairSettingsScreen extends Screen {

    private final Screen parent;
    private int panelX, panelY;
    private static final int PANEL_W = 200;
    private static final int PANEL_H = 210;
    private int draggingSlider = -1;

    public CrosshairSettingsScreen(Screen parent) {
        super(Text.literal("Crosshair Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        panelX = width / 2 - PANEL_W / 2;
        panelY = height / 2 - PANEL_H / 2;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0xB0000000);

        ctx.fill(panelX, panelY, panelX + PANEL_W, panelY + PANEL_H, 0xF0101018);
        ctx.fill(panelX, panelY, panelX + PANEL_W, panelY + 2, 0xFF26C6DA);

        ctx.drawTextWithShadow(textRenderer, "Crosshair Settings", panelX + 8, panelY + 8, 0xFF26C6DA);

        int y = panelY + 26;
        drawSlider(ctx, "Size", CustomCrosshair.getSize(), 1, 20, panelX + 8, y, mouseX, mouseY, 0);
        y += 22;
        drawSlider(ctx, "Gap", CustomCrosshair.getGap(), 0, 15, panelX + 8, y, mouseX, mouseY, 1);
        y += 22;
        drawSlider(ctx, "Thickness", CustomCrosshair.getThickness(), 1, 5, panelX + 8, y, mouseX, mouseY, 2);
        y += 22;
        drawSlider(ctx, "Red", CustomCrosshair.getRed(), 0, 255, panelX + 8, y, mouseX, mouseY, 3);
        y += 22;
        drawSlider(ctx, "Green", CustomCrosshair.getGreen(), 0, 255, panelX + 8, y, mouseX, mouseY, 4);
        y += 22;
        drawSlider(ctx, "Blue", CustomCrosshair.getBlue(), 0, 255, panelX + 8, y, mouseX, mouseY, 5);

        y += 26;
        boolean dotHovered = mouseX >= panelX + 8 && mouseX <= panelX + PANEL_W - 8
                && mouseY >= y && mouseY <= y + 16;
        ctx.fill(panelX + 8, y, panelX + PANEL_W - 8, y + 16,
                dotHovered ? 0x40FFFFFF : 0x20FFFFFF);
        String dotText = "Dot: " + (CustomCrosshair.hasDot() ? "ON" : "OFF");
        ctx.drawTextWithShadow(textRenderer, dotText, panelX + 14, y + 4,
                CustomCrosshair.hasDot() ? 0xFF44FF44 : 0xFFFF4444);

        y += 24;
        int previewCx = panelX + PANEL_W / 2;
        int previewCy = y + 16;
        ctx.fill(previewCx - 20, previewCy - 20, previewCx + 20, previewCy + 20, 0xFF222222);
        int c = CustomCrosshair.getColor();
        int sz = CustomCrosshair.getSize();
        int g = CustomCrosshair.getGap();
        int t = CustomCrosshair.getThickness();
        int half = t / 2;
        ctx.fill(previewCx - half, previewCy - g - sz, previewCx - half + t, previewCy - g, c);
        ctx.fill(previewCx - half, previewCy + g + 1, previewCx - half + t, previewCy + g + sz + 1, c);
        ctx.fill(previewCx - g - sz, previewCy - half, previewCx - g, previewCy - half + t, c);
        ctx.fill(previewCx + g + 1, previewCy - half, previewCx + g + sz + 1, previewCy - half + t, c);
        if (CustomCrosshair.hasDot()) {
            ctx.fill(previewCx - half, previewCy - half, previewCx - half + t, previewCy - half + t, c);
        }

        ctx.drawTextWithShadow(textRenderer, "Right-click to go back", panelX + 8, panelY + PANEL_H - 14, 0xFF666666);
    }

    private void drawSlider(DrawContext ctx, String label, int value, int min, int max,
                            int x, int y, int mouseX, int mouseY, int index) {
        int labelW = 70;
        int sliderX = x + labelW;
        int sliderW = PANEL_W - labelW - 16 - 30;
        int sliderH = 12;

        ctx.drawTextWithShadow(textRenderer, label, x, y + 2, 0xFFCCCCCC);

        ctx.fill(sliderX, y + 2, sliderX + sliderW, y + 2 + sliderH, 0xFF222233);

        float ratio = (float) (value - min) / (max - min);
        int fillW = (int) (ratio * sliderW);
        ctx.fill(sliderX, y + 2, sliderX + fillW, y + 2 + sliderH, 0xFF26C6DA);

        int knobX = sliderX + fillW - 2;
        ctx.fill(knobX, y, knobX + 4, y + 2 + sliderH + 2, 0xFFFFFFFF);

        String valStr = String.valueOf(value);
        ctx.drawTextWithShadow(textRenderer, valStr, sliderX + sliderW + 4, y + 2, 0xFFAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) {
            client.setScreen(parent);
            return true;
        }

        int y = panelY + 26;
        for (int i = 0; i < 6; i++) {
            if (isOnSlider(mouseX, mouseY, y + i * 22)) {
                draggingSlider = i;
                updateSlider(i, mouseX, y + i * 22);
                return true;
            }
        }

        int dotY = panelY + 26 + 6 * 22 + 4;
        if (mouseX >= panelX + 8 && mouseX <= panelX + PANEL_W - 8
                && mouseY >= dotY && mouseY <= dotY + 16) {
            CustomCrosshair.setDot(!CustomCrosshair.hasDot());
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingSlider >= 0) {
            int y = panelY + 26 + draggingSlider * 22;
            updateSlider(draggingSlider, mouseX, y);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingSlider = -1;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean isOnSlider(double mouseX, double mouseY, int rowY) {
        int sliderX = panelX + 8 + 70;
        int sliderW = PANEL_W - 70 - 16 - 30;
        return mouseX >= sliderX && mouseX <= sliderX + sliderW
                && mouseY >= rowY && mouseY <= rowY + 16;
    }

    private void updateSlider(int index, double mouseX, int rowY) {
        int sliderX = panelX + 8 + 70;
        int sliderW = PANEL_W - 70 - 16 - 30;
        float ratio = (float) Math.max(0, Math.min(1, (mouseX - sliderX) / sliderW));

        switch (index) {
            case 0 -> CustomCrosshair.setSize(Math.round(1 + ratio * 19));
            case 1 -> CustomCrosshair.setGap(Math.round(ratio * 15));
            case 2 -> CustomCrosshair.setThickness(Math.round(1 + ratio * 4));
            case 3 -> CustomCrosshair.setRed(Math.round(ratio * 255));
            case 4 -> CustomCrosshair.setGreen(Math.round(ratio * 255));
            case 5 -> CustomCrosshair.setBlue(Math.round(ratio * 255));
        }
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
