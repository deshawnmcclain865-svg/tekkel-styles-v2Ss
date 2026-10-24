package com.skinmenu;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class Config {
    public static final Path DIR = FabricLoader.getInstance().getConfigDir().resolve("skinmenu");
    public static final Path IMAGES = DIR.resolve("images");
    private static final Path FILE = DIR.resolve("config.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Config instance = new Config();

    /** target.key -> Dateiname in config/skinmenu/images */
    public Map<String, String> selection = new HashMap<>();
    /** Abdunklung der Hintergründe in Prozent (0-100) */
    public int dim = 40;

    public static Config get() {
        return instance;
    }

    public static void load() {
        try {
            Files.createDirectories(IMAGES);
            if (Files.exists(FILE)) {
                try (Reader r = Files.newBufferedReader(FILE)) {
                    Config c = GSON.fromJson(r, Config.class);
                    if (c != null) {
                        if (c.selection == null) c.selection = new HashMap<>();
                        instance = c;
                    }
                }
            }
        } catch (Exception e) {
            SkinMenuClient.LOGGER.error("Config konnte nicht geladen werden", e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(DIR);
            try (Writer w = Files.newBufferedWriter(FILE)) {
                GSON.toJson(instance, w);
            }
        } catch (Exception e) {
            SkinMenuClient.LOGGER.error("Config konnte nicht gespeichert werden", e);
        }
    }
}
