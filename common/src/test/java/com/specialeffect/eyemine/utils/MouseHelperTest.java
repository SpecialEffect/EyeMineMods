package com.specialeffect.eyemine.utils;

import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

class MouseHelperTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void onlyAllowsClientThreadCaptureWithoutAnOverlay(boolean overlayVisible) {
        var minecraft = mock(Minecraft.class);
        Thread clientThread = Thread.currentThread();
        when(minecraft.isSameThread()).thenAnswer(call -> Thread.currentThread() == clientThread);
        when(minecraft.getOverlay()).thenReturn(overlayVisible ? mock(Overlay.class) : null);

        try (var singleton = mockStatic(Minecraft.class)) {
            singleton.when(Minecraft::getInstance).thenReturn(minecraft);
            assertEquals(!overlayVisible, MouseHelper.canChangeMouseCapture());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void rejectsLoadingWorkerBeforeReadingOverlayState(boolean ungrabbed) throws Exception {
        var minecraft = mock(Minecraft.class);
        Thread clientThread = Thread.currentThread();
        when(minecraft.isSameThread()).thenAnswer(call -> Thread.currentThread() == clientThread);
        boolean previousMode = MouseHelper.ungrabbedMouseMode;

        var work = new FutureTask<>(() -> {
            try (var singleton = mockStatic(Minecraft.class)) {
                singleton.when(Minecraft::getInstance).thenReturn(minecraft);
                MouseHelper.setUngrabbedMode(ungrabbed);
                return MouseHelper.canChangeMouseCapture();
            }
        });
        Thread worker = new Thread(work, "resourceLoad-test");
        try {
            worker.start();
            assertFalse(work.get(30, TimeUnit.SECONDS));
            verify(minecraft, never()).getOverlay();
        } finally {
            worker.join(30_000);
            MouseHelper.ungrabbedMouseMode = previousMode;
        }
    }
}
