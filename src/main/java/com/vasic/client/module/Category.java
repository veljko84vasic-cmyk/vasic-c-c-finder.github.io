package com.vasic.client.module;

public enum Category {
    RENDER("Render", 0xFF5C6BC0),
    HUD("HUD", 0xFF26C6DA),
    MOVEMENT("Movement", 0xFF66BB6A),
    PLAYER("Player", 0xFFFFA726);

    private final String name;
    private final int color;

    Category(String name, int color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public int getColor() {
        return color;
    }
}
