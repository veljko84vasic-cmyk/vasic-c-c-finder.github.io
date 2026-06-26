package com.vasic.client.gui;

import com.vasic.client.VasicClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;

public class LoginScreen extends Screen {

    private String username = "";
    private String errorMsg = "";
    private int errorColor = 0xFFFF6BD6;
    private long openTime;

    // Layout
    private int cardX, cardY, cardW, cardH;

    public LoginScreen() {
        super(Text.literal("Login"));
        openTime = System.currentTimeMillis();
    }

    @Override
    protected void init() {
        cardW = 280;
        cardH = 260;
        cardX = width / 2 - cardW / 2;
        cardY = height / 2 - cardH / 2;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Deep dark background
        ctx.fill(0, 0, width, height, 0xFF07060F);

        // Aurora glow blobs (simulated with colored rectangles + transparency)
        long t = System.currentTimeMillis() - openTime;
        int glow1X = (int) (width * 0.15 + Math.sin(t / 3000.0) * 30);
        int glow1Y = (int) (height * 0.2 + Math.cos(t / 4000.0) * 20);
        for (int r = 120; r > 0; r -= 4) {
            int alpha = Math.max(1, 12 - r / 12);
            ctx.fill(glow1X - r, glow1Y - r, glow1X + r, glow1Y + r,
                    (alpha << 24) | 0x00A66BFF);
        }
        int glow2X = (int) (width * 0.8 + Math.sin(t / 4500.0) * 25);
        int glow2Y = (int) (height * 0.75 + Math.cos(t / 3500.0) * 20);
        for (int r = 100; r > 0; r -= 4) {
            int alpha = Math.max(1, 10 - r / 12);
            ctx.fill(glow2X - r, glow2Y - r, glow2X + r, glow2Y + r,
                    (alpha << 24) | 0x002BD9C9);
        }

        // Stars (small white dots)
        drawStar(ctx, width * 0.2, height * 0.3);
        drawStar(ctx, width * 0.7, height * 0.15);
        drawStar(ctx, width * 0.85, height * 0.6);
        drawStar(ctx, width * 0.1, height * 0.75);
        drawStar(ctx, width * 0.55, height * 0.85);
        drawStar(ctx, width * 0.4, height * 0.1);
        drawStar(ctx, width * 0.9, height * 0.35);

        // Card background
        ctx.fill(cardX, cardY, cardX + cardW, cardY + cardH, 0xE014112450);
        // Card border
        drawBorder(ctx, cardX, cardY, cardW, cardH, 0xFF2A2440);
        // Card glow shadow
        ctx.fill(cardX - 1, cardY - 1, cardX + cardW + 1, cardY, 0x40A66BFF);
        ctx.fill(cardX - 1, cardY + cardH, cardX + cardW + 1, cardY + cardH + 1, 0x202BD9C9);

        // "LOGIN REQUIRED" tag
        String reqTag = "LOGIN REQUIRED";
        int reqW = textRenderer.getWidth(reqTag) + 20;
        int reqX = width / 2 - reqW / 2;
        int reqY = cardY + 16;
        ctx.fill(reqX, reqY, reqX + reqW, reqY + 16, 0x20FF6BD6);
        drawBorder(ctx, reqX, reqY, reqW, 16, 0x59FF6BD6);
        ctx.drawTextWithShadow(textRenderer, reqTag, reqX + 10, reqY + 4, 0xFFFF6BD6);

        // Brand name "VASIC"
        String brand = "VASIC";
        int brandW = textRenderer.getWidth(brand);
        // Draw it bigger by centering
        ctx.drawTextWithShadow(textRenderer, brand, width / 2 - brandW / 2, cardY + 40, 0xFF26C6DA);

        // Tagline
        String tagline = "Sign in to continue to your client";
        int tagW = textRenderer.getWidth(tagline);
        ctx.drawTextWithShadow(textRenderer, tagline, width / 2 - tagW / 2, cardY + 56, 0xFF5E577A);

        // Microsoft button
        int btnX = cardX + 20;
        int btnY = cardY + 78;
        int btnW = cardW - 40;
        int btnH = 22;
        boolean msHover = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + btnH;
        ctx.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0xFF1C1A2B);
        drawBorder(ctx, btnX, btnY, btnW, btnH, msHover ? 0xFF2BD9C9 : 0xFF2A2440);
        String msText = "Continue with Microsoft";
        ctx.drawTextWithShadow(textRenderer, msText, btnX + btnW / 2 - textRenderer.getWidth(msText) / 2, btnY + 7, 0xFFEEEEEE);
        String msNote = "coming soon";
        ctx.drawTextWithShadow(textRenderer, msNote, btnX + btnW - textRenderer.getWidth(msNote) - 6, btnY + 7, 0xFF5E577A);

        // Divider "or play as"
        int divY = btnY + btnH + 12;
        ctx.fill(cardX + 20, divY + 4, width / 2 - 26, divY + 5, 0xFF2A2440);
        ctx.fill(width / 2 + 26, divY + 4, cardX + cardW - 20, divY + 5, 0xFF2A2440);
        String divText = "or play as";
        ctx.drawTextWithShadow(textRenderer, divText, width / 2 - textRenderer.getWidth(divText) / 2, divY, 0xFF5E577A);

        // Username input field
        int inputY = divY + 18;
        int inputH = 22;
        boolean inputFocused = true;
        ctx.fill(btnX, inputY, btnX + btnW, inputY + inputH, 0x4D000000);
        int inputBorder = inputFocused ? 0xFFA66BFF : 0xFF2A2440;
        drawBorder(ctx, btnX, inputY, btnW, inputH, inputBorder);
        if (inputFocused) {
            ctx.fill(btnX - 1, inputY - 1, btnX + btnW + 1, inputY, 0x30A66BFF);
            ctx.fill(btnX - 1, inputY + inputH, btnX + btnW + 1, inputY + inputH + 1, 0x30A66BFF);
        }
        String displayText = username.isEmpty() ? "Enter a username" : username;
        int displayColor = username.isEmpty() ? 0xFF5E577A : 0xFFF1EEFC;
        String cursor = !username.isEmpty() && System.currentTimeMillis() % 1000 < 500 ? "_" : "";
        ctx.drawTextWithShadow(textRenderer, displayText + cursor, btnX + 8, inputY + 7, displayColor);

        // Error message
        if (!errorMsg.isEmpty()) {
            ctx.drawTextWithShadow(textRenderer, errorMsg, btnX + 2, inputY + inputH + 4, errorColor);
        }

        // Continue button (gradient-styled)
        int contY = inputY + inputH + 18;
        boolean contHover = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= contY && mouseY <= contY + btnH;
        int contBg = contHover ? 0xFF2BD9C9 : 0xFFA66BFF;
        ctx.fill(btnX, contY, btnX + btnW, contY + btnH, contBg);
        String contText = "Continue with username";
        ctx.drawTextWithShadow(textRenderer, contText, btnX + btnW / 2 - textRenderer.getWidth(contText) / 2,
                contY + 7, 0xFF0A0816);

        // Guest button
        int guestY = contY + btnH + 6;
        boolean guestHover = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= guestY && mouseY <= guestY + btnH;
        ctx.fill(btnX, guestY, btnX + btnW, guestY + btnH, 0x0AFFFFFF);
        drawBorder(ctx, btnX, guestY, btnW, btnH, guestHover ? 0xFFA66BFF : 0xFF2A2440);
        String guestText = "Continue as Guest";
        ctx.drawTextWithShadow(textRenderer, guestText, btnX + btnW / 2 - textRenderer.getWidth(guestText) / 2,
                guestY + 7, guestHover ? 0xFFF1EEFC : 0xFF9B93B8);

        // Footnote
        String foot = "Microsoft login planned for a later build.";
        ctx.drawTextWithShadow(textRenderer, foot, width / 2 - textRenderer.getWidth(foot) / 2,
                cardY + cardH - 18, 0xFF5E577A);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (username.length() < 16 && (Character.isLetterOrDigit(chr) || chr == '_')) {
            username += chr;
            errorMsg = "";
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 259 && !username.isEmpty()) {
            username = username.substring(0, username.length() - 1);
            errorMsg = "";
            return true;
        }
        if (keyCode == 257) {
            loginWithUsername();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int btnX = cardX + 20;
        int btnW = cardW - 40;
        int btnH = 22;

        // Microsoft button
        int msY = cardY + 78;
        if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= msY && mouseY <= msY + btnH) {
            errorMsg = "Microsoft login coming soon!";
            errorColor = 0xFFFF6BD6;
            return true;
        }

        // Continue button
        int divY = msY + btnH + 12;
        int inputY = divY + 18;
        int contY = inputY + 22 + 18;
        if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= contY && mouseY <= contY + btnH) {
            loginWithUsername();
            return true;
        }

        // Guest button
        int guestY = contY + btnH + 6;
        if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= guestY && mouseY <= guestY + btnH) {
            loginAsGuest();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void loginWithUsername() {
        String name = username.trim();
        if (name.isEmpty()) {
            errorMsg = "Enter a username first.";
            errorColor = 0xFFFF6BD6;
            return;
        }
        if (name.length() < 3) {
            errorMsg = "Username must be at least 3 characters.";
            errorColor = 0xFFFF6BD6;
            return;
        }
        VasicClient.setCustomUsername(name);
        VasicClient.getInstance().getConfigManager().save();
        VasicClient.setLoggedIn(true);
        client.setScreen(new TitleScreen());
    }

    private void loginAsGuest() {
        VasicClient.setCustomUsername("");
        VasicClient.setLoggedIn(true);
        client.setScreen(new TitleScreen());
    }

    private void drawStar(DrawContext ctx, double x, double y) {
        ctx.fill((int) x, (int) y, (int) x + 1, (int) y + 1, 0x80FFFFFF);
    }

    private void drawBorder(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        ctx.fill(x, y, x + 1, y + h, color);
        ctx.fill(x + w - 1, y, x + w, y + h, color);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
