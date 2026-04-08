package com.specialeffect.eyemine.mixin;

import com.specialeffect.eyemine.event.EyeMineEvents;
import com.specialeffect.eyemine.event.ScreenSetResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric-specific mixin to forward screen open events to EyeMineEvents.SCREEN_SET.
 * NeoForge has ScreenEvent.Opening for this; Fabric needs a mixin.
 */
@Mixin(Minecraft.class)
public class FabricSetScreenMixin {

    @Inject(method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"))
    private void eyemine$onSetScreen(Screen screen, CallbackInfo ci) {
        for (var listener : EyeMineEvents.SCREEN_SET.getListeners()) {
            ScreenSetResult result = listener.onScreenSet(screen);
            if (result.handled()) break;
        }
    }
}
