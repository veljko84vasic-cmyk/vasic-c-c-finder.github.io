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

    private static final int CARD_W = 170;
    private static final int CARD_H = 105;
    private static final int CARD_GAP = 10;
    private static final int COLS = 3;

    private static String selectedFilter = "ALL";
    private int scrollY = 0;
    private int gridX, gridStartY;
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
        gridX = (width - gridW) / 2;
        gridStartY = 62;
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
        ctx.fill(0, 0, width, height, 0xB0000000);

        // Title
        String title = VasicClient.NAME;
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 6, 0xFF26C6DA);

        // Category tabs
        renderTabs(ctx, mouseX, mouseY);

        // Utility buttons
        renderUtilButtons(ctx, mouseX, mouseY);

        // Module cards
        for (int i = 0; i < filtered.size(); i++) {
            int col = i % COLS;
            int row = i / COLS;
            int cx = gridX + col * (CARD_W + CARD_GAP);
            int cy = gridStartY + row * (CARD_H + CARD_GAP) + scrollY;
            if (cy + CARD_H < gridStartY || cy > height) continue;
            renderCard(ctx, filtered.get(i), cx, cy, mouseX, mouseY);
        }
    }

    private void renderTabs(DrawContext ctx, int mx, int my) {
        int totalW = 0;
        int[] tabWidths = new int[TABS.length];
        for (int i = 0; i < TABS.length; i++) {
            tabWidths[i] = textRenderer.getWidth(TABS[i]) + 14;
            totalW += tabWidths[i] + 4;
        }
        totalW -= 4;
        int tx = (width - totalW) / 2;
        for (int i = 0; i < TABS.length; i++) {
            int tw = tabWidths[i];
            boolean sel = TABS[i].equals(selectedFilter);
            boolean hov = mx >= tx && mx <= tx + tw && my >= 20 && my <= 34;
            ctx.fill(tx, 20, tx + tw, 34, sel ? TAB_COLORS[i] : (hov ? 0x50FFFFFF : 0x25FFFFFF));
            ctx.drawTextWithShadow(textRenderer, TABS[i],
                    tx + (tw - textRenderer.getWidth(TABS[i])) / 2, 23,
                    sel ? 0xFF000000 : 0xFFCCCCCC);
            tx += tw + 4;
        }
    }

    private void renderUtilButtons(DrawContext ctx, int mx, int my) {
        int y = 42;
        String hudText = "Edit HUD";
        int hudW = textRenderer.getWidth(hudText) + 10;
        int hudX = width / 2 - hudW - 4;
        boolean hudHov = mx >= hudX && mx <= hudX + hudW && my >= y && my <= y + 14;
        ctx.fill(hudX, y, hudX + hudW, y + 14, hudHov ? 0x60FFFFFF : 0x30FFFFFF);
        ctx.drawTextWithShadow(textRenderer, hudText, hudX + 5, y + 3, hudHov ? 0xFF26C6DA : 0xFF999999);

        String currentName = VasicClient.getCustomUsername();
        String nameText = currentName != null && !currentName.isEmpty() ? currentName : "Set Username";
        int nameW = textRenderer.getWidth(nameText) + 10;
        int nameX = width / 2 + 4;
        boolean nameHov = mx >= nameX && mx <= nameX + nameW && my >= y && my <= y + 14;
        ctx.fill(nameX, y, nameX + nameW, y + 14, nameHov ? 0x60FFFFFF : 0x30FFFFFF);
        ctx.drawTextWithShadow(textRenderer, nameText, nameX + 5, y + 3, nameHov ? 0xFF26C6DA : 0xFF999999);
    }

    private void renderCard(DrawContext ctx, Module module, int x, int y, int mx, int my) {
        boolean enabled = module.isEnabled();
        boolean hovered = mx >= x && mx <= x + CARD_W && my >= y && my <= y + CARD_H;

        ctx.fill(x, y, x + CARD_W, y + CARD_H, hovered ? 0xE8181828 : 0xE0101018);
        ctx.fill(x, y, x + CARD_W, y + 2, module.getCategory().getColor());

        ctx.drawCenteredTextWithShadow(textRenderer, module.getName(), x + CARD_W / 2, y + 14, 0xFFEEEEEE);

        String desc = module.getDescription();
        if (desc.length() > 24) desc = desc.substring(0, 22) + "..";
        ctx.drawCenteredTextWithShadow(textRenderer, desc, x + CARD_W / 2, y + 28, 0xFF666677);

        if (hasSettings(module)) {
            String optText = "OPTIONS";
            int optW = textRenderer.getWidth(optText) + 12;
            int optX = x + CARD_W / 2 - optW / 2;
            int optY = y + 46;
            boolean optHov = mx >= optX && mx <= optX + optW && my >= optY && my <= optY + 16;
            ctx.fill(optX, optY, optX + optW, optY + 16, optHov ? 0x60FFFFFF : 0x30FFFFFF);
            ctx.fill(optX, optY, optX + optW, optY + 1, 0x20FFFFFF);
            ctx.drawTextWithShadow(textRenderer, optText,
                    optX + (optW - textRenderer.getWidth(optText)) / 2, optY + 4,
                    optHov ? 0xFFFFFFFF : 0xFFAAAAAA);
        }

        int toggleW = CARD_W - 20;
        int toggleX = x + 10;
        int toggleY = y + CARD_H - 24;
        int toggleH = 18;
        boolean toggleHov = mx >= toggleX && mx <= toggleX + toggleW && my >= toggleY && my <= toggleY + toggleH;
        int toggleBg = enabled
                ? (toggleHov ? 0xFF1DB954 : 0xFF17A84A)
                : (toggleHov ? 0xFFCC3344 : 0xFFAA2233);
        ctx.fill(toggleX, toggleY, toggleX + toggleW, toggleY + toggleH, toggleBg);
        String toggleText = enabled ? "ENABLED" : "DISABLED";
        ctx.drawCenteredTextWithShadow(textRenderer, toggleText,
                toggleX + toggleW / 2, toggleY + 5, 0xFFFFFFFF);
    }

    private boolean hasSettings(Module module) {
        return module instanceof CustomCrosshair;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        // Category tabs
        int totalW = 0;
        int[] tabWidths = new int[TABS.length];
        for (int i = 0; i < TABS.length; i++) {
            tabWidths[i] = textRenderer.getWidth(TABS[i]) + 14;
            totalW += tabWidths[i] + 4;
        }
        totalW -= 4;
        int tx = (width - totalW) / 2;
        for (int i = 0; i < TABS.length; i++) {
            int tw = tabWidths[i];
            if (mouseX >= tx && mouseX <= tx + tw && mouseY >= 20 && mouseY <= 34) {
                selectedFilter = TABS[i];
                scrollY = 0;
                updateFilter();
                return true;
            }
            tx += tw + 4;
        }

        // Utility buttons
        int utilY = 42;
        String hudText = "Edit HUD";
        int hudW = textRenderer.getWidth(hudText) + 10;
        int hudX = width / 2 - hudW - 4;
        if (mouseX >= hudX && mouseX <= hudX + hudW && mouseY >= utilY && mouseY <= utilY + 14) {
            client.setScreen(new HudEditorScreen());
            return true;
        }

        String currentName = VasicClient.getCustomUsername();
        String nameText = currentName != null && !currentName.isEmpty() ? currentName : "Set Username";
        int nameW = textRenderer.getWidth(nameText) + 10;
        int nameX = width / 2 + 4;
        if (mouseX >= nameX && mouseX <= nameX + nameW && mouseY >= utilY && mouseY <= utilY + 14) {
            client.setScreen(new UsernameScreen(this));
            return true;
        }

        // Module cards
        for (int i = 0; i < filtered.size(); i++) {
            int col = i % COLS;
            int row = i / COLS;
            int cx = gridX + col * (CARD_W + CARD_GAP);
            int cy = gridStartY + row * (CARD_H + CARD_GAP) + scrollY;
            if (cy + CARD_H < gridStartY || cy > height) continue;

            Module module = filtered.get(i);

            // OPTIONS button
            if (hasSettings(module)) {
                String optText = "OPTIONS";
                int optW = textRenderer.getWidth(optText) + 12;
                int optX = cx + CARD_W / 2 - optW / 2;
                int optY = cy + 46;
                if (mouseX >= optX && mouseX <= optX + optW && mouseY >= optY && mouseY <= optY + 16) {
                    openSettings(module);
                    return true;
                }
            }

            // Toggle button
            int toggleW = CARD_W - 20;
            int toggleX = cx + 10;
            int toggleY = cy + CARD_H - 24;
            int toggleH = 18;
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
        int viewH = height - gridStartY;
        if (contentH > viewH) {
            scrollY += (int) (verticalAmount * 30);
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
