package com.skinmenu;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public final class ImageManager {
    public record Loaded(Identifier id, AbstractTexture tex, int w, int h) {}

    private static final Map<Target, Loaded> LOADED = new EnumMap<>(Target.class);

    private ImageManager() {}

    public static Loaded get(Target t) {
        return LOADED.get(t);
    }

    /** Alle .png-Dateien im Bilder-Ordner. */
    public static List<String> listImages() {
        List<String> out = new ArrayList<>();
        try {
            Files.createDirectories(Config.IMAGES);
            try (Stream<Path> s = Files.list(Config.IMAGES)) {
                s.filter(Files::isRegularFile)
                        .map(p -> p.getFileName().toString())
                        .filter(n -> n.toLowerCase(Locale.ROOT).endsWith(".png"))
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .forEach(out::add);
            }
        } catch (Exception e) {
            SkinMenuClient.LOGGER.error("Bilder konnten nicht gelistet werden", e);
        }
        return out;
    }

    public static void applyAll() {
        for (Target t : Target.values()) apply(t);
    }

    public static void apply(Target t) {
        TextureManager tm = Minecraft.getInstance().getTextureManager();

        Loaded old = LOADED.remove(t);
        if (old != null) tm.release(old.id()); // bei Vanilla-IDs lädt MC danach die Originaltextur neu

        String file = Config.get().selection.get(t.key);
        if (file == null || file.isEmpty()) return;
        Path path = Config.IMAGES.resolve(file);
        if (!Files.isRegularFile(path)) return;

        try (InputStream in = Files.newInputStream(path)) {
            NativeImage img = NativeImage.read(in);
            int w = img.getWidth(), h = img.getHeight();
            if (t.isSheet()) {
                img = toSheet(img);
                w = 256;
                h = 256;
            }
            Identifier id = t.isSheet()
                    ? Identifier.withDefaultNamespace(t.vanillaPath)
                    : Identifier.fromNamespaceAndPath("skinmenu", "bg/" + t.key);
            DynamicTexture tex = new DynamicTexture(() -> "skinmenu/" + t.key, img);
            tm.register(id, tex);
            LOADED.put(t, new Loaded(id, tex, w, h));
        } catch (Exception e) {
            SkinMenuClient.LOGGER.error("Bild '{}' konnte nicht geladen werden", file, e);
        }
    }

    /** Nach einem Resourcepack-Reload (F3+T) ist unsere Textur evtl. weg -> neu setzen. */
    public static void verify() {
        TextureManager tm = Minecraft.getInstance().getTextureManager();
        for (Target t : new ArrayList<>(LOADED.keySet())) {
            Loaded l = LOADED.get(t);
            if (l != null && tm.getTexture(l.id()) != l.tex()) apply(t);
        }
    }

    /**
     * Quadratisches Bild  -> wird als 256x256-Sheet im Texturepack-Layout benutzt.
     * Anderes Format      -> wird in den Panel-Bereich (176x166) gestreckt.
     */
    private static NativeImage toSheet(NativeImage src) {
        if (src.getWidth() == 256 && src.getHeight() == 256) return src;
        NativeImage dst = new NativeImage(256, 256, true);
        if (src.getWidth() == src.getHeight()) scaleInto(src, dst, 0, 0, 256, 256);
        else scaleInto(src, dst, 0, 0, 176, 166);
        src.close();
        return dst;
    }

    private static void scaleInto(NativeImage src, NativeImage dst, int dx, int dy, int dw, int dh) {
        for (int y = 0; y < dh; y++) {
            int sy = y * src.getHeight() / dh;
            for (int x = 0; x < dw; x++) {
                int sx = x * src.getWidth() / dw;
                dst.setPixel(dx + x, dy + y, src.getPixel(sx, sy));
            }
        }
    }
}
