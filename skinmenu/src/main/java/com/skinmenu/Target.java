package com.skinmenu;

/** Alles, was man per Bild austauschen kann. vanillaPath == null => eigener Hintergrund. */
public enum Target {
    INVENTORY("inventory", "Inventar", "textures/gui/container/inventory.png"),
    CRAFTING("crafting_table", "Werkbank", "textures/gui/container/crafting_table.png"),
    CHEST("chest", "Kiste", "textures/gui/container/generic_54.png"),
    FURNACE("furnace", "Ofen", "textures/gui/container/furnace.png"),
    SCREEN_BG("screen_background", "Screen-Hintergrund", null),
    CONFIG_BG("config_background", "Dieses Menü", null);

    public final String key;
    public final String label;
    public final String vanillaPath;

    Target(String key, String label, String vanillaPath) {
        this.key = key;
        this.label = label;
        this.vanillaPath = vanillaPath;
    }

    public boolean isSheet() {
        return vanillaPath != null;
    }
}
