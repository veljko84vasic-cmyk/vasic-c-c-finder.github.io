package com.vasic.client.hud;

public class HudElement {

    private final String id;
    private final String displayName;
    private int x, y;
    private int width, height;
    private boolean dragging;
    private int dragOffsetX, dragOffsetY;

    public HudElement(String id, String displayName, int x, int y, int width, int height) {
        this.id = id;
        this.displayName = displayName;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
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
