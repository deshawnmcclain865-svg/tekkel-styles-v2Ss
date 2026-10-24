package com.skinmenu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;

public final class BackgroundRenderer {
    private BackgroundRenderer() {}

    /** Zeichnet das Bild bildschirmfüllend (cover) + Abdunklung. false = kein eigenes Bild gesetzt. */
    public static boolean draw(GuiGraphics g, Target target, int w, int h) {
        ImageManager.Loaded l = ImageManager.get(target);
        if (l == null) return false;

        double scale = Math.max((double) w / l.w(), (double) h / l.h());
        int srcW = Math.min(l.w(), (int) Math.ceil(w / scale));
        int srcH = Math.min(l.h(), (int) Math.ceil(h / scale));
        float u = (l.w() - srcW) / 2f;
        float v = (l.h() - srcH) / 2f;

        g.blit(RenderPipelines.GUI_TEXTURED, l.id(), 0, 0, u, v, w, h, srcW, srcH, l.w(), l.h());

        int dim = Math.max(0, Math.min(100, Config.get().dim));
        if (dim > 0) g.fill(0, 0, w, h, (dim * 255 / 100) << 24);
        return true;
    }
}
