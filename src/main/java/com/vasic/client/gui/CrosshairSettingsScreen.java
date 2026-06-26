package com.vasic.client.gui;

import com.vasic.client.module.modules.render.CustomCrosshair;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class CrosshairSettingsScreen extends Screen {

    private final Screen parent;
    private static final int CELL = 14;
    private static final int GS = CustomCrosshair.GRID_SIZE;
    private static final int GRID_PX = GS * CELL;
    private static final int SWATCH = 18;
    private static final int SW_GAP = 2;
    private static final int SW_COLS = 5;

    private int gridX, gridY;
    private int palX, palY;

    private static int selectedColor = 0xFFFFFFFF;
    private static int selR = 255, selG = 255, selB = 255;

    private boolean painting, erasing;
    private int dragSlider = -1;

    private static final int[] PALETTE = {
        0xFFFFFFFF, 0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFFFF00,
        0xFF00FFFF, 0xFFFF00FF, 0xFFFF8800, 0xFFFF88CC, 0xFF888888
    };

    public CrosshairSettingsScreen(Screen parent) {
        super(Text.literal("Crosshair Editor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int totalW = GRID_PX + 24 + 110;
        gridX = width / 2 - totalW / 2;
        gridY = height / 2 - GRID_PX / 2 - 8;
        palX = gridX + GRID_PX + 24;
        palY = gridY;
    }

    private int swatchTopY()  { return palY + 14; }
    private int selectedY()   { return swatchTopY() + ((PALETTE.length + SW_COLS - 1) / SW_COLS) * (SWATCH + SW_GAP) + 6; }
    private int rgbTopY()     { return selectedY() + 18; }
    private int btnTopY()     { return rgbTopY() + 3 * 18 + 6; }
    private int previewTopY() { return btnTopY() + 22; }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0xC0000000);
        ctx.drawCenteredTextWithShadow(textRenderer, "Crosshair Editor", width / 2, gridY - 16, 0xFF26C6DA);

        renderGrid(ctx, mouseX, mouseY);
        renderPalette(ctx);
        renderRGBSliders(ctx);
        renderButtons(ctx, mouseX, mouseY);
        renderLivePreview(ctx);

        ctx.drawTextWithShadow(textRenderer, "LMB: paint  RMB: erase", gridX, gridY + GRID_PX + 6, 0xFF666666);
        ctx.drawTextWithShadow(textRenderer, "Right-click outside to go back", gridX, gridY + GRID_PX + 18, 0xFF555555);
    }

    private void renderGrid(DrawContext ctx, int mx, int my) {
        ctx.fill(gridX - 1, gridY - 1, gridX + GRID_PX + 1, gridY + GRID_PX + 1, 0xFF333344);
        int mid = GS / 2;
        for (int r = 0; r < GS; r++) {
            for (int c = 0; c < GS; c++) {
                int cx = gridX + c * CELL;
                int cy = gridY + r * CELL;
                boolean light = (r + c) % 2 == 0;
                ctx.fill(cx, cy, cx + CELL - 1, cy + CELL - 1, light ? 0xFF2A2A3A : 0xFF222233);
                int pixel = CustomCrosshair.getPixel(r, c);
                if (pixel != 0) {
                    ctx.fill(cx, cy, cx + CELL - 1, cy + CELL - 1, pixel);
                }
                if (r == mid || c == mid) {
                    ctx.fill(cx, cy, cx + CELL - 1, cy + 1, 0x15FFFFFF);
                    ctx.fill(cx, cy, cx + 1, cy + CELL - 1, 0x15FFFFFF);
                }
            }
        }
        int hr = getGridRow(my);
        int hc = getGridCol(mx);
        if (hr >= 0 && hr < GS && hc >= 0 && hc < GS) {
            int hx = gridX + hc * CELL;
            int hy = gridY + hr * CELL;
            ctx.fill(hx, hy, hx + CELL - 1, hy + CELL - 1, 0x40FFFFFF);
        }
    }

    private void renderPalette(DrawContext ctx) {
        ctx.drawTextWithShadow(textRenderer, "Colors", palX, palY, 0xFFCCCCCC);
        int y = swatchTopY();
        for (int i = 0; i < PALETTE.length; i++) {
            int sx = palX + (i % SW_COLS) * (SWATCH + SW_GAP);
            int sy = y + (i / SW_COLS) * (SWATCH + SW_GAP);
            ctx.fill(sx - 1, sy - 1, sx + SWATCH + 1, sy + SWATCH + 1,
                    selectedColor == PALETTE[i] ? 0xFFFFFFFF : 0xFF444444);
            ctx.fill(sx, sy, sx + SWATCH, sy + SWATCH, PALETTE[i]);
        }
        int sy = selectedY();
        ctx.drawTextWithShadow(textRenderer, "Active:", palX, sy, 0xFFAAAAAA);
        ctx.fill(palX + 48, sy - 1, palX + 72, sy + 11, 0xFF444444);
        ctx.fill(palX + 49, sy, palX + 71, sy + 10, selectedColor);
    }

    private void renderRGBSliders(DrawContext ctx) {
        int y = rgbTopY();
        drawSlider(ctx, "R", selR, y, 0xFFFF4444);
        drawSlider(ctx, "G", selG, y + 18, 0xFF44FF44);
        drawSlider(ctx, "B", selB, y + 36, 0xFF4444FF);
    }

    private void drawSlider(DrawContext ctx, String label, int val, int y, int barColor) {
        ctx.drawTextWithShadow(textRenderer, label, palX, y + 1, 0xFFCCCCCC);
        int sx = palX + 12, sw = 74;
        ctx.fill(sx, y + 1, sx + sw, y + 11, 0xFF222233);
        ctx.fill(sx, y + 1, sx + (int) (val / 255f * sw), y + 11, barColor);
        ctx.drawTextWithShadow(textRenderer, String.valueOf(val), sx + sw + 3, y + 1, 0xFF999999);
    }

    private void renderButtons(DrawContext ctx, int mx, int my) {
        int y = btnTopY();
        drawBtn(ctx, "Clear", palX, y, 48, 14, mx, my);
        drawBtn(ctx, "Default", palX + 52, y, 52, 14, mx, my);
    }

    private void drawBtn(DrawContext ctx, String text, int x, int y, int w, int h, int mx, int my) {
        boolean hov = mx >= x && mx <= x + w && my >= y && my <= y + h;
        ctx.fill(x, y, x + w, y + h, hov ? 0xFF26C6DA : 0xFF1A1A2E);
        ctx.fill(x, y, x + w, y + 1, 0x30FFFFFF);
        ctx.drawTextWithShadow(textRenderer, text,
                x + (w - textRenderer.getWidth(text)) / 2, y + 3,
                hov ? 0xFF000000 : 0xFFCCCCCC);
    }

    private void renderLivePreview(DrawContext ctx) {
        int y = previewTopY();
        ctx.drawTextWithShadow(textRenderer, "Preview:", palX, y, 0xFFAAAAAA);
        y += 12;
        int pvSize = 40;
        int pcx = palX + pvSize / 2 + 10;
        ctx.fill(pcx - pvSize / 2 - 1, y - 1, pcx + pvSize / 2 + 1, y + pvSize + 1, 0xFF444444);
        ctx.fill(pcx - pvSize / 2, y, pcx + pvSize / 2, y + pvSize, 0xFF111111);
        for (int r = 0; r < GS; r++) {
            for (int c = 0; c < GS; c++) {
                int pixel = CustomCrosshair.getPixel(r, c);
                if (pixel != 0) {
                    int px1 = pcx - pvSize / 2 + c * pvSize / GS;
                    int py1 = y + r * pvSize / GS;
                    int px2 = pcx - pvSize / 2 + (c + 1) * pvSize / GS;
                    int py2 = y + (r + 1) * pvSize / GS;
                    if (px2 > px1 && py2 > py1) ctx.fill(px1, py1, px2, py2, pixel);
                }
            }
        }
    }

    private int getGridRow(double my) { return (int) ((my - gridY) / CELL); }
    private int getGridCol(double mx) { return (int) ((mx - gridX) / CELL); }
    private boolean onGrid(double mx, double my) {
        return mx >= gridX && mx < gridX + GRID_PX && my >= gridY && my < gridY + GRID_PX;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn == 1) {
            if (onGrid(mx, my)) {
                erasing = true;
                CustomCrosshair.clearPixel(getGridRow(my), getGridCol(mx));
                return true;
            }
            client.setScreen(parent);
            return true;
        }
        if (btn == 0) {
            if (onGrid(mx, my)) {
                painting = true;
                CustomCrosshair.setPixel(getGridRow(my), getGridCol(mx), selectedColor);
                return true;
            }
            if (clickPalette(mx, my)) return true;
            if (clickSliders(mx, my)) return true;
            if (clickButtons(mx, my)) return true;
        }
        return super.mouseClicked(mx, my, btn);
    }

    private boolean clickPalette(double mx, double my) {
        int y = swatchTopY();
        for (int i = 0; i < PALETTE.length; i++) {
            int sx = palX + (i % SW_COLS) * (SWATCH + SW_GAP);
            int sy = y + (i / SW_COLS) * (SWATCH + SW_GAP);
            if (mx >= sx && mx <= sx + SWATCH && my >= sy && my <= sy + SWATCH) {
                selectedColor = PALETTE[i];
                selR = (selectedColor >> 16) & 0xFF;
                selG = (selectedColor >> 8) & 0xFF;
                selB = selectedColor & 0xFF;
                return true;
            }
        }
        return false;
    }

    private boolean clickSliders(double mx, double my) {
        int ry = rgbTopY();
        int sx = palX + 12, sw = 74;
        for (int i = 0; i < 3; i++) {
            int sly = ry + i * 18;
            if (mx >= sx && mx <= sx + sw && my >= sly + 1 && my <= sly + 11) {
                dragSlider = i;
                applySlider(i, mx, sx, sw);
                return true;
            }
        }
        return false;
    }

    private boolean clickButtons(double mx, double my) {
        int y = btnTopY();
        if (mx >= palX && mx <= palX + 48 && my >= y && my <= y + 14) {
            CustomCrosshair.clearAll();
            return true;
        }
        if (mx >= palX + 52 && mx <= palX + 104 && my >= y && my <= y + 14) {
            CustomCrosshair.loadDefaultCross();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (painting && onGrid(mx, my)) {
            CustomCrosshair.setPixel(getGridRow(my), getGridCol(mx), selectedColor);
            return true;
        }
        if (erasing && onGrid(mx, my)) {
            CustomCrosshair.clearPixel(getGridRow(my), getGridCol(mx));
            return true;
        }
        if (dragSlider >= 0) {
            applySlider(dragSlider, mx, palX + 12, 74);
            return true;
        }
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        painting = false;
        erasing = false;
        dragSlider = -1;
        return super.mouseReleased(mx, my, btn);
    }

    private void applySlider(int idx, double mx, int sx, int sw) {
        int val = Math.round(Math.max(0, Math.min(1, (float) (mx - sx) / sw)) * 255);
        switch (idx) {
            case 0 -> selR = val;
            case 1 -> selG = val;
            case 2 -> selB = val;
        }
        selectedColor = 0xFF000000 | (selR << 16) | (selG << 8) | selB;
    }

    @Override
    public void close() { client.setScreen(parent); }

    @Override
    public boolean shouldPause() { return false; }
}
