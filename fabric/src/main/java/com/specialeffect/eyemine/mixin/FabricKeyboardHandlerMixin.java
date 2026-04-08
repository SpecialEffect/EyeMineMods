package com.specialeffect.eyemine.mixin;

import com.specialeffect.eyemine.event.EyeMineEvents;
import com.specialeffect.eyemine.event.EventResult;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric-specific mixin to forward raw key events to EyeMineEvents.KEY_PRESSED.
 * NeoForge has InputEvent.Key for this; Fabric needs a mixin.
 */
@Mixin(KeyboardHandler.class)
public class FabricKeyboardHandlerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "keyPress(JILnet/minecraft/client/input/KeyEvent;)V", at = @At("HEAD"))
    private void eyemine$onKeyPress(long windowHandle, int action, KeyEvent keyEvent, CallbackInfo ci) {
        for (var listener : EyeMineEvents.KEY_PRESSED.getListeners()) {
            EventResult result = listener.onKeyPressed(
                    minecraft, keyEvent.key(), keyEvent.scancode(), action, keyEvent.modifiers());
            if (result.isPresent()) break;
        }
    }
}
