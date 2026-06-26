package com.vasic.client.gui;

import com.vasic.client.VasicClient;
import com.vasic.client.hud.HudEditorScreen;
import com.vasic.client.module.Category;
import com.vasic.client.module.Module;
import com.vasic.client.module.modules.render.CustomCrosshair;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ClickGui extends Screen {

    private static final int CARD_W = 150;
    private static final int CARD_H = 92;
    private static final int CARD_GAP = 8;
    private static final int COLS = 3;
    private static final int PAD = 14;

    private static String selectedFilter = "ALL";
    private int scrollY = 0;
    private int panelX, panelY, panelW, panelH;
    private int gridX, cardsTopY;
    private final List<Module> filtered = new ArrayList<>();

    private static final String[] TABS = {"ALL", "RENDER", "HUD", "MOVEMENT", "PLAYER"};
    private static final int[] TAB_COLORS = {
            0xFF26C6DA, 0xFF5C6BC0, 0xFF26C6DA, 0xFF66BB6A, 0xFFFFA726
    };

    public ClickGui() {
        super(Text.literal("NebulaX"));
    }

    @Override
    protected void init() {
        int gridW = COLS * CARD_W + (COLS - 1) * CARD_GAP;
        panelW = gridW + PAD * 2;
        panelH = Math.min(height - 30, 420);
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;
        gridX = panelX + PAD;
        cardsTopY = panelY + 58;
        scrollY = 0;
        updateFilter();
    }

    private void updateFilter() {
        filtered.clear();
        List<Module> all = VasicClient.getInstance().getModuleManager().getModules();
        if ("ALL".equals(selectedFilter)) {
            filtered.addAll(all);
        } else {
            for (Module m : all) {
                if (m.getCategory().getName().equalsIgnoreCase(selectedFilter)) {
                    filtered.add(m);
                }
            }
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0x90000000);

        // Panel background
        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xF0101018);
        drawBorder(ctx, panelX, panelY, panelW, panelH, 0xFF2A2440);
        ctx.fill(panelX, panelY, panelX + panelW, panelY + 2, 0xFF26C6DA);

        // Title
        ctx.drawCenteredTextWithShadow(textRenderer, VasicClient.NAME, panelX + panelW / 2, panelY + 8, 0xFF26C6DA);

        // Category tabs
        renderTabs(ctx, mouseX, mouseY);

        // Utility buttons
        renderUtilButtons(ctx, mouseX, mouseY);

        // Module cards (clipped to panel)
        int panelBottom = panelY + panelH - PAD;
        for (int i = 0; i < filtered.size(); i++) {
            int col = i % COLS;
            int row = i / COLS;
            int cx = gridX + col * (CARD_W + CARD_GAP);
            int cy = cardsTopY + row * (CARD_H + CARD_GAP) + scrollY;
            if (cy + CARD_H < cardsTopY || cy > panelBottom) continue;
            renderCard(ctx, filtered.get(i), cx, cy, mouseX, mouseY);
        }
    }

    private void renderTabs(DrawContext ctx, int mx, int my) {
        int tabY = panelY + 22;
        int totalW = 0;
        int[] tw = new int[TABS.length];
        for (int i = 0; i < TABS.length; i++) {
            tw[i] = textRenderer.getWidth(TABS[i]) + 12;
            totalW += tw[i] + 3;
        }
        totalW -= 3;
        int tx = panelX + (panelW - totalW) / 2;
        for (int i = 0; i < TABS.length; i++) {
            boolean sel = TABS[i].equals(selectedFilter);
            boolean hov = mx >= tx && mx <= tx + tw[i] && my >= tabY && my <= tabY + 13;
            ctx.fill(tx, tabY, tx + tw[i], tabY + 13, sel ? TAB_COLORS[i] : (hov ? 0x50FFFFFF : 0x20FFFFFF));
            ctx.drawTextWithShadow(textRenderer, TABS[i],
                    tx + (tw[i] - textRenderer.getWidth(TABS[i])) / 2, tabY + 3,
                    sel ? 0xFF000000 : 0xFFBBBBBB);
            tx += tw[i] + 3;
        }
    }

    private void renderUtilButtons(DrawContext ctx, int mx, int my) {
        int y = panelY + 40;
        String hudText = "Edit HUD";
        int hudW = textRenderer.getWidth(hudText) + 8;
        int hudX = panelX + panelW / 2 - hudW - 3;
        boolean hudHov = mx >= hudX && mx <= hudX + hudW && my >= y && my <= y + 13;
        ctx.fill(hudX, y, hudX + hudW, y + 13, hudHov ? 0x50FFFFFF : 0x20FFFFFF);
        ctx.drawTextWithShadow(textRenderer, hudText, hudX + 4, y + 3, hudHov ? 0xFF26C6DA : 0xFF888888);

        String currentName = VasicClient.getCustomUsername();
        String nameText = currentName != null && !currentName.isEmpty() ? currentName : "Set Username";
        int nameW = textRenderer.getWidth(nameText) + 8;
        int nameX = panelX + panelW / 2 + 3;
        boolean nameHov = mx >= nameX && mx <= nameX + nameW && my >= y && my <= y + 13;
        ctx.fill(nameX, y, nameX + nameW, y + 13, nameHov ? 0x50FFFFFF : 0x20FFFFFF);
        ctx.drawTextWithShadow(textRenderer, nameText, nameX + 4, y + 3, nameHov ? 0xFF26C6DA : 0xFF888888);
    }

    private void renderCard(DrawContext ctx, Module module, int x, int y, int mx, int my) {
        boolean enabled = module.isEnabled();
        boolean hovered = mx >= x && mx <= x + CARD_W && my >= y && my <= y + CARD_H;

        ctx.fill(x, y, x + CARD_W, y + CARD_H, hovered ? 0xE8181828 : 0xE0101018);
        ctx.fill(x, y, x + CARD_W, y + 2, module.getCategory().getColor());

        ctx.drawCenteredTextWithShadow(textRenderer, module.getName(), x + CARD_W / 2, y + 12, 0xFFEEEEEE);

        String desc = module.getDescription();
        if (desc.length() > 22) desc = desc.substring(0, 20) + "..";
        ctx.drawCenteredTextWithShadow(textRenderer, desc, x + CARD_W / 2, y + 26, 0xFF555566);

        if (hasSettings(module)) {
            String optText = "OPTIONS";
            int optW = textRenderer.getWidth(optText) + 10;
            int optX = x + CARD_W / 2 - optW / 2;
            int optY = y + 42;
            boolean optHov = mx >= optX && mx <= optX + optW && my >= optY && my <= optY + 14;
            ctx.fill(optX, optY, optX + optW, optY + 14, optHov ? 0x50FFFFFF : 0x25FFFFFF);
            ctx.drawTextWithShadow(textRenderer, optText,
                    optX + (optW - textRenderer.getWidth(optText)) / 2, optY + 3,
                    optHov ? 0xFFFFFFFF : 0xFF999999);
        }

        int toggleW = CARD_W - 16;
        int toggleX = x + 8;
        int toggleY = y + CARD_H - 22;
        int toggleH = 16;
        boolean toggleHov = mx >= toggleX && mx <= toggleX + toggleW && my >= toggleY && my <= toggleY + toggleH;
        int toggleBg = enabled
                ? (toggleHov ? 0xFF1DB954 : 0xFF17A84A)
                : (toggleHov ? 0xFFCC3344 : 0xFFAA2233);
        ctx.fill(toggleX, toggleY, toggleX + toggleW, toggleY + toggleH, toggleBg);
        ctx.drawCenteredTextWithShadow(textRenderer, enabled ? "ENABLED" : "DISABLED",
                toggleX + toggleW / 2, toggleY + 4, 0xFFFFFFFF);
    }

    private boolean hasSettings(Module module) {
        return module instanceof CustomCrosshair;
    }

    private void drawBorder(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        ctx.fill(x, y, x + 1, y + h, color);
        ctx.fill(x + w - 1, y, x + w, y + h, color);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        // Category tabs
        int tabY = panelY + 22;
        int totalW = 0;
        int[] tw = new int[TABS.length];
        for (int i = 0; i < TABS.length; i++) {
            tw[i] = textRenderer.getWidth(TABS[i]) + 12;
            totalW += tw[i] + 3;
        }
        totalW -= 3;
        int tx = panelX + (panelW - totalW) / 2;
        for (int i = 0; i < TABS.length; i++) {
            if (mouseX >= tx && mouseX <= tx + tw[i] && mouseY >= tabY && mouseY <= tabY + 13) {
                selectedFilter = TABS[i];
                scrollY = 0;
                updateFilter();
                return true;
            }
            tx += tw[i] + 3;
        }

        // Utility buttons
        int utilY = panelY + 40;
        String hudText = "Edit HUD";
        int hudW = textRenderer.getWidth(hudText) + 8;
        int hudX = panelX + panelW / 2 - hudW - 3;
        if (mouseX >= hudX && mouseX <= hudX + hudW && mouseY >= utilY && mouseY <= utilY + 13) {
            client.setScreen(new HudEditorScreen());
            return true;
        }
        String currentName = VasicClient.getCustomUsername();
        String nameText = currentName != null && !currentName.isEmpty() ? currentName : "Set Username";
        int nameW = textRenderer.getWidth(nameText) + 8;
        int nameX = panelX + panelW / 2 + 3;
        if (mouseX >= nameX && mouseX <= nameX + nameW && mouseY >= utilY && mouseY <= utilY + 13) {
            client.setScreen(new UsernameScreen(this));
            return true;
        }

        // Module cards
        int panelBottom = panelY + panelH - PAD;
        for (int i = 0; i < filtered.size(); i++) {
            int col = i % COLS;
            int row = i / COLS;
            int cx = gridX + col * (CARD_W + CARD_GAP);
            int cy = cardsTopY + row * (CARD_H + CARD_GAP) + scrollY;
            if (cy + CARD_H < cardsTopY || cy > panelBottom) continue;

            Module module = filtered.get(i);

            if (hasSettings(module)) {
                String optText = "OPTIONS";
                int optW = textRenderer.getWidth(optText) + 10;
                int optX = cx + CARD_W / 2 - optW / 2;
                int optY = cy + 42;
                if (mouseX >= optX && mouseX <= optX + optW && mouseY >= optY && mouseY <= optY + 14) {
                    openSettings(module);
                    return true;
                }
            }

            int toggleW = CARD_W - 16;
            int toggleX = cx + 8;
            int toggleY = cy + CARD_H - 22;
            int toggleH = 16;
            if (mouseX >= toggleX && mouseX <= toggleX + toggleW
                    && mouseY >= toggleY && mouseY <= toggleY + toggleH) {
                module.toggle();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int rows = (filtered.size() + COLS - 1) / COLS;
        int contentH = rows * (CARD_H + CARD_GAP);
        int viewH = panelY + panelH - PAD - cardsTopY;
        if (contentH > viewH) {
            scrollY += (int) (verticalAmount * 25);
            scrollY = Math.max(-(contentH - viewH), Math.min(0, scrollY));
        }
        return true;
    }

    private void openSettings(Module module) {
        if (module instanceof CustomCrosshair) {
            client.setScreen(new CrosshairSettingsScreen(this));
        }
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
