package com.vasic.client.hud;

public class HudElement {

    private final String id;
    private final String displayName;
    private final String moduleName;
    private int x, y;
    private int width, height;
    private boolean dragging;
    private int dragOffsetX, dragOffsetY;

    public HudElement(String id, String displayName, String moduleName, int x, int y, int width, int height) {
        this.id = id;
        this.displayName = displayName;
        this.moduleName = moduleName;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public boolean isCloseButtonHit(double mouseX, double mouseY) {
        int bx = x + width - 10;
        int by = y - 2;
        return mouseX >= bx && mouseX <= bx + 10 && mouseY >= by && mouseY <= by + 10;
    }

    public void startDrag(double mouseX, double mouseY) {
        dragging = true;
        dragOffsetX = (int) (mouseX - x);
        dragOffsetY = (int) (mouseY - y);
    }

    public void updateDrag(double mouseX, double mouseY) {
        if (dragging) {
            x = (int) (mouseX - dragOffsetX);
            y = (int) (mouseY - dragOffsetY);
        }
    }

    public void stopDrag() {
        dragging = false;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getModuleName() { return moduleName; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isDragging() { return dragging; }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
