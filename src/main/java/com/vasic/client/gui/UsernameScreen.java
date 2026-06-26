package com.vasic.client.gui;

import com.vasic.client.VasicClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class UsernameScreen extends Screen {

    private final Screen parent;
    private String input;
    private boolean typing = true;
    private String status = "";
    private int statusColor = 0xFF666666;

    public UsernameScreen(Screen parent) {
        super(Text.literal("Change Username"));
        this.parent = parent;
        String current = VasicClient.getCustomUsername();
        this.input = (current != null && !current.isEmpty()) ? current : "";
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0xB0000000);

        int pw = 260, ph = 140;
        int px = width / 2 - pw / 2, py = height / 2 - ph / 2;

        ctx.fill(px, py, px + pw, py + ph, 0xF0101018);
        ctx.fill(px, py, px + pw, py + 2, 0xFF26C6DA);

        ctx.drawTextWithShadow(textRenderer, "Change Username", px + 10, py + 12, 0xFF26C6DA);

        int fieldX = px + 10, fieldY = py + 32, fieldW = pw - 20, fieldH = 20;
        ctx.fill(fieldX, fieldY, fieldX + fieldW, fieldY + fieldH, 0xFF0A0A14);
        int borderColor = typing ? 0xFF26C6DA : 0xFF2A2440;
        ctx.fill(fieldX, fieldY, fieldX + fieldW, fieldY + 1, borderColor);
        ctx.fill(fieldX, fieldY + fieldH - 1, fieldX + fieldW, fieldY + fieldH, borderColor);
        ctx.fill(fieldX, fieldY, fieldX + 1, fieldY + fieldH, borderColor);
        ctx.fill(fieldX + fieldW - 1, fieldY, fieldX + fieldW, fieldY + fieldH, borderColor);

        String display = input + (typing && System.currentTimeMillis() % 1000 < 500 ? "_" : "");
        ctx.drawTextWithShadow(textRenderer, display, fieldX + 4, fieldY + 6, 0xFFFFFFFF);

        int btnY = fieldY + 30;
        int btnW = (pw - 30) / 2;

        boolean applyHover = mouseX >= px + 10 && mouseX <= px + 10 + btnW
                && mouseY >= btnY && mouseY <= btnY + 18;
        ctx.fill(px + 10, btnY, px + 10 + btnW, btnY + 18,
                applyHover ? 0xFF26C6DA : 0xFF1A1A2E);
        ctx.drawTextWithShadow(textRenderer, "Apply",
                px + 10 + btnW / 2 - textRenderer.getWidth("Apply") / 2, btnY + 5,
                applyHover ? 0xFF000000 : 0xFFCCCCCC);

        boolean clearHover = mouseX >= px + 20 + btnW && mouseX <= px + 20 + btnW * 2
                && mouseY >= btnY && mouseY <= btnY + 18;
        ctx.fill(px + 20 + btnW, btnY, px + 20 + btnW * 2, btnY + 18,
                clearHover ? 0xFFFF4444 : 0xFF1A1A2E);
        ctx.drawTextWithShadow(textRenderer, "Reset",
                px + 20 + btnW + btnW / 2 - textRenderer.getWidth("Reset") / 2, btnY + 5,
                clearHover ? 0xFFFFFFFF : 0xFFCCCCCC);

        if (!status.isEmpty()) {
            ctx.drawTextWithShadow(textRenderer, status, px + 10, btnY + 26, statusColor);
        }

        ctx.drawTextWithShadow(textRenderer, "ESC to go back", px + 10, py + ph - 16, 0xFF444444);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int pw = 260, ph = 140;
        int px = width / 2 - pw / 2, py = height / 2 - ph / 2;
        int btnY = py + 62;
        int btnW = (pw - 30) / 2;

        if (mouseY >= btnY && mouseY <= btnY + 18) {
            if (mouseX >= px + 10 && mouseX <= px + 10 + btnW) {
                applyUsername();
                return true;
            }
            if (mouseX >= px + 20 + btnW && mouseX <= px + 20 + btnW * 2) {
                input = "";
                VasicClient.setCustomUsername("");
                VasicClient.getInstance().getConfigManager().save();
                status = "Reset to default";
                statusColor = 0xFFFF8844;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (typing && input.length() < 16) {
            if (Character.isLetterOrDigit(chr) || chr == '_') {
                input += chr;
                return true;
            }
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 259 && !input.isEmpty()) {
            input = input.substring(0, input.length() - 1);
            return true;
        }
        if (keyCode == 257) {
            applyUsername();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void applyUsername() {
        String name = input.trim();
        if (name.length() < 3) {
            status = "Must be at least 3 characters";
            statusColor = 0xFFFF4444;
            return;
        }
        VasicClient.setCustomUsername(name);
        VasicClient.getInstance().getConfigManager().save();
        status = "Username set to: " + name;
        statusColor = 0xFF44FF44;
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
