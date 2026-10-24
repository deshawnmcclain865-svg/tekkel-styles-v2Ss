package com.skinmenu;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SkinMenuClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("skinmenu");
    private static KeyMapping openKey;
    private static int tick;

    @Override
    public void onInitializeClient() {
        Config.load();

        openKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.skinmenu.open",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_L,
                KeyMapping.Category.register(Identifier.fromNamespaceAndPath("skinmenu", "main"))));

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> ImageManager.applyAll());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.consumeClick()) {
                if (client.screen == null) client.setScreen(new ConfigScreen(null));
            }
            if (++tick % 20 == 0) ImageManager.verify();
        });
    }
}
