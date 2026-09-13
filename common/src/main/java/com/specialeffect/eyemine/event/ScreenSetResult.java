package com.specialeffect.eyemine.event;

import net.minecraft.client.gui.screens.Screen;

public record ScreenSetResult(boolean handled, Screen screen) {
    public static ScreenSetResult pass() { return new ScreenSetResult(false, null); }
    public static ScreenSetResult set(Screen screen) { return new ScreenSetResult(true, screen); }
}
