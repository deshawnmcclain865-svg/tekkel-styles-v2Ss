package com.skinmenu.mixin;

import com.skinmenu.BackgroundRenderer;
import com.skinmenu.ConfigScreen;
import com.skinmenu.Target;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void skinmenu$customBackground(GuiGraphics g, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        if (self instanceof ConfigScreen) return;
        if (BackgroundRenderer.draw(g, Target.SCREEN_BG, self.width, self.height)) ci.cancel();
    }
}
