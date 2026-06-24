package com.vasic.client.module;

public enum Category {
    RENDER("Render", 0xFF2196F3),
    MOVEMENT("Movement", 0xFF4CAF50),
    PLAYER("Player", 0xFFFF9800),
    MISC("Misc", 0xFF9C27B0);

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
