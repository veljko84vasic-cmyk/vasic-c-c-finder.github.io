package com.vasic.armorhud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ArmorHudConfigScreen extends Screen {

    private final HudConfig config;
    private boolean draggingHud = false;

    public ArmorHudConfigScreen(HudConfig config) {
        super(Text.literal("Armor HUD Settings"));
        this.config = config;
    }

    @Override
    protected void init() {
        int panelX = this.width - 165;
        int y = 30;
        int w = 155;
        int h = 20;
        int gap = 25;

        addDrawableChild(ButtonWidget.builder(
            visibleLabel(),
            btn -> {
                config.visible = !config.visible;
                btn.setMessage(visibleLabel());
            }
        ).dimensions(panelX, y, w, h).build());
        y += gap;

        addDrawableChild(ButtonWidget.builder(
            modeLabel(),
            btn -> {
                config.showPercentage = !config.showPercentage;
                btn.setMessage(modeLabel());
            }
        ).dimensions(panelX, y, w, h).build());
        y += gap;

        addDrawableChild(ButtonWidget.builder(
            layoutLabel(),
            btn -> {
                config.horizontal = !config.horizontal;
                btn.setMessage(layoutLabel());
            }
        ).dimensions(panelX, y, w, h).build());
        y += gap;

        addDrawableChild(ButtonWidget.builder(
            offhandLabel(),
            btn -> {
                config.showOffhand = !config.showOffhand;
                btn.setMessage(offhandLabel());
            }
        ).dimensions(panelX, y, w, h).build());
        y += gap;

        addDrawableChild(ButtonWidget.builder(
            colorsLabel(),
            btn -> {
                config.colorCoded = !config.colorCoded;
                btn.setMessage(colorsLabel());
            }
        ).dimensions(panelX, y, w, h).build());
        y += gap;

        // Scale controls
        addDrawableChild(ButtonWidget.builder(Text.literal("Scale -"),
            btn -> config.scale = Math.max(0.5f, Math.round((config.scale - 0.1f) * 10) / 10f)
        ).dimensions(panelX, y, 70, h).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Scale +"),
            btn -> config.scale = Math.min(3.0f, Math.round((config.scale + 0.1f) * 10) / 10f)
        ).dimensions(panelX + 80, y, 70, h).build());
        y += gap;

        addDrawableChild(ButtonWidget.builder(Text.literal("Reset Position"),
            btn -> { config.x = 5; config.y = 5; }
        ).dimensions(panelX, y, w, h).build());
        y += gap;

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"),
            btn -> { config.save(); this.close(); }
        ).dimensions(panelX, y, w, h).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);

        context.drawTextWithShadow(textRenderer,
            Text.literal("Click & drag to reposition HUD"),
            10, this.height - 20, 0xAAAAAA);

        context.drawTextWithShadow(textRenderer,
            Text.literal(String.format("Scale: %.1fx", config.scale)),
            this.width - 165, this.height - 30, 0xFFFFFF);

        // Draw HUD position preview box
        int previewW = config.horizontal ? (int)(80 * config.scale) : (int)(60 * config.scale);
        int previewH = config.horizontal ? (int)(25 * config.scale) : (int)(90 * config.scale);
        int px = (int) config.x;
        int py = (int) config.y;

        context.fill(px, py, px + previewW, py + previewH, 0x33FFFF00);
        context.drawBorder(px, py, previewW, previewH, 0xFFFFFF55);
        context.drawTextWithShadow(textRenderer, Text.literal("HUD"), px + 3, py + 3, 0xFFFF55);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (button == 0) {
            draggingHud = true;
            moveHud(mouseX, mouseY);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true;
        if (draggingHud && button == 0) {
            moveHud(mouseX, mouseY);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) draggingHud = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) return true;
        float delta = verticalAmount > 0 ? 0.1f : -0.1f;
        config.scale = Math.max(0.5f, Math.min(3.0f, Math.round((config.scale + delta) * 10) / 10f));
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private void moveHud(double mouseX, double mouseY) {
        config.x = (float) Math.max(0, Math.min(mouseX, this.width - 60));
        config.y = (float) Math.max(0, Math.min(mouseY, this.height - 60));
    }

    private Text visibleLabel()  { return Text.literal("Visible: "  + (config.visible        ? "ON"          : "OFF"));      }
    private Text modeLabel()     { return Text.literal("Mode: "     + (config.showPercentage  ? "Percentage"  : "Durability")); }
    private Text layoutLabel()   { return Text.literal("Layout: "   + (config.horizontal      ? "Horizontal"  : "Vertical")); }
    private Text offhandLabel()  { return Text.literal("Offhand: "  + (config.showOffhand     ? "ON"          : "OFF"));      }
    private Text colorsLabel()   { return Text.literal("Colors: "   + (config.colorCoded      ? "ON"          : "OFF"));      }
}
