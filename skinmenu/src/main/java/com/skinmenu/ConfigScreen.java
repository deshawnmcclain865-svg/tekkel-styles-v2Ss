package com.skinmenu;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ConfigScreen extends Screen {
    private static final int PANEL_W = 300;
    private static final int ROW_H = 26;

    private final Screen parent;
    private final List<Object[]> labels = new ArrayList<>(); // {Component, x, y}
    private String status = "";
    private int panelTop, panelBottom;

    public ConfigScreen(Screen parent) {
        super(Component.literal("Skin-Menü"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        labels.clear();
        List<String> images = ImageManager.listImages();

        int rows = Target.values().length;
        int contentH = rows * ROW_H + 3 * 24 + 10;
        panelTop = Math.max(10, (height - contentH) / 2 - 10);
        int left = (width - PANEL_W) / 2;
        int y = panelTop + 34;

        for (Target t : Target.values()) {
            addRow(t, left + 10, y, images);
            y += ROW_H;
        }

        y += 6;
        int full = PANEL_W - 20;
        addRenderableWidget(Button.builder(dimLabel(), b -> {
            Config c = Config.get();
            c.dim = (c.dim + 20) % 100 == 0 && c.dim >= 80 ? 0 : c.dim + 20;
            if (c.dim > 80) c.dim = 0;
            Config.save();
            b.setMessage(dimLabel());
        }).bounds(left + 10, y, full, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(Component.literal("Bilder-Ordner öffnen"),
                b -> Util.getPlatform().openPath(Config.IMAGES))
                .bounds(left + 10, y, full, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(Component.literal("Fertig"), b -> onClose())
                .bounds(left + 10, y, full, 20).build());
        y += 26;

        panelBottom = y + 14;
    }

    private Component dimLabel() {
        return Component.literal("Hintergrund abdunkeln: " + Config.get().dim + " %");
    }

    private void addRow(Target t, int x, int y, List<String> images) {
        labels.add(new Object[]{Component.literal(t.label), x, y + 6});

        Button nameBtn = Button.builder(nameOf(t), b -> {}).bounds(x + 116, y, 120, 20).build();
        nameBtn.active = false;

        addRenderableWidget(Button.builder(Component.literal("<"), b -> {
            step(t, images, -1);
            nameBtn.setMessage(nameOf(t));
        }).bounds(x + 92, y, 20, 20).build());

        addRenderableWidget(nameBtn);

        addRenderableWidget(Button.builder(Component.literal(">"), b -> {
            step(t, images, 1);
            nameBtn.setMessage(nameOf(t));
        }).bounds(x + 240, y, 20, 20).build());

        addRenderableWidget(Button.builder(Component.literal("✕"), b -> {
            Config.get().selection.remove(t.key);
            Config.save();
            ImageManager.apply(t);
            nameBtn.setMessage(nameOf(t));
        }).bounds(x + 264, y, 16, 20).build());
    }

    private Component nameOf(Target t) {
        String f = Config.get().selection.get(t.key);
        if (f == null || f.isEmpty()) return Component.literal("Standard");
        return Component.literal(font.plainSubstrByWidth(f, 108));
    }

    private void step(Target t, List<String> images, int dir) {
        if (images.isEmpty()) {
            status = "Keine PNGs gefunden – zieh ein Bild ins Fenster!";
            return;
        }
        String cur = Config.get().selection.get(t.key);
        int idx = cur == null ? -1 : images.indexOf(cur);
        idx = Math.floorMod(idx + dir, images.size());
        Config.get().selection.put(t.key, images.get(idx));
        Config.save();
        ImageManager.apply(t);
    }

    /** Bilder per Drag & Drop ins Fenster ziehen. */
    @Override
    public void onFilesDrop(List<Path> paths) {
        int ok = 0;
        for (Path p : paths) {
            if (!p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".png")) continue;
            try {
                Files.createDirectories(Config.IMAGES);
                Files.copy(p, Config.IMAGES.resolve(p.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
                ok++;
            } catch (Exception e) {
                SkinMenuClient.LOGGER.error("Kopieren fehlgeschlagen", e);
            }
        }
        status = ok > 0 ? ok + " Bild(er) importiert" : "Nur PNG-Dateien werden unterstützt";
        rebuildWidgets();
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float delta) {
        // bewusst leer – wir zeichnen den Hintergrund selbst in render()
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        if (!BackgroundRenderer.draw(g, Target.CONFIG_BG, width, height)) {
            g.fill(0, 0, width, height, 0xFF14161C);
        }

        int left = (width - PANEL_W) / 2;
        g.fill(left, panelTop, left + PANEL_W, panelBottom, 0xC0101218);
        g.fill(left, panelTop, left + PANEL_W, panelTop + 1, 0x60FFFFFF);
        g.fill(left, panelBottom - 1, left + PANEL_W, panelBottom, 0x60FFFFFF);
        g.fill(left, panelTop, left + 1, panelBottom, 0x60FFFFFF);
        g.fill(left + PANEL_W - 1, panelTop, left + PANEL_W, panelBottom, 0x60FFFFFF);

        g.drawCenteredString(font, title, width / 2, panelTop + 10, 0xFFFFFFFF);
        for (Object[] l : labels) {
            g.drawString(font, (Component) l[0], (int) l[1], (int) l[2], 0xFFE0E0E0);
        }

        super.render(g, mouseX, mouseY, delta);

        String hint = status.isEmpty() ? "PNG ins Fenster ziehen zum Importieren" : status;
        g.drawCenteredString(font, Component.literal(hint), width / 2, panelBottom + 6, 0xFF9AA0AA);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
