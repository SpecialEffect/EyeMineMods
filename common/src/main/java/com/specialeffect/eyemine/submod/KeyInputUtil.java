package com.specialeffect.eyemine.submod;

import com.mojang.blaze3d.platform.InputConstants;
import com.specialeffect.eyemine.event.EventResult;
import com.specialeffect.utils.ModUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;

/**
 * Utility to reduce boilerplate in submod key input handlers.
 * Most submods follow the same pattern: check GUI, check F11, check keybinding, run action.
 */
public class KeyInputUtil {

    /**
     * Returns true if key events should be ignored (GUI open or F11 debug key held).
     */
    public static boolean shouldIgnoreKeyInput(Minecraft minecraft) {
        return ModUtils.hasActiveGui() || InputConstants.isKeyDown(minecraft.getWindow(), 292);
    }

    /**
     * Check if a keybinding was just pressed. Call after shouldIgnoreKeyInput().
     */
    public static boolean wasKeyPressed(KeyMapping binding, int keyCode, int scanCode, int modifiers) {
        return binding.matches(new KeyEvent(keyCode, scanCode, modifiers)) && binding.consumeClick();
    }
}
