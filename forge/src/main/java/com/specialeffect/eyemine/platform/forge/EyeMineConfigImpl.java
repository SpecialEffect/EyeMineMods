package com.specialeffect.eyemine.platform.forge;

import com.specialeffect.eyemine.EyeMineConfig;

public class EyeMineConfigImpl {
    public static void setCustomSpeedFactor(float speed) {
        EyeMineConfig.customSpeedFactor.set((double)speed);
    }

    public static boolean getUsingMouseEmulation() {
        return EyeMineConfig.usingMouseEmulation.get();
    }

    public static boolean getAutoSelectTool() {
        return EyeMineConfig.autoSelectTool.get();
    }

    public static int getTicksBetweenMining() {
        return EyeMineConfig.ticksBetweenMining.get();
    }

    public static double getBowDrawTime() {
        return EyeMineConfig.bowDrawTime.get();
    }

    public static float getFullscreenOverlayAlpha() {
        return EyeMineConfig.fullscreenOverlayAlpha.get().floatValue();
    }

    public static int getIronsightsFovReduction() {
        return EyeMineConfig.ironsightsFovReduction.get();
    }

    public static double getIronsightsSensitivityReduction() {
        return EyeMineConfig.ironsightsSensitivityReduction.get().floatValue();
    }

    public static boolean getUseDwellForSingleUseItem() {
        return EyeMineConfig.useDwellForSingleUseItem.get();
    }

    public static int getRadiusDoors() {
        return EyeMineConfig.radiusDoors.get();
    }

    public static boolean getServerCompatibilityMode() {
        return EyeMineConfig.serverCompatibilityMode.get();
    }

    public static int getFlyHeightManual() {
        return EyeMineConfig.flyHeightManual.get();
    }

    public static int getFlyHeightAuto() {
        return EyeMineConfig.flyHeightAuto.get();
    }

    public static boolean getDefaultDoAutoJump() {
        return EyeMineConfig.defaultDoAutoJump.get();
    }

    public static boolean getMoveWhenMouseStationary() {
        return EyeMineConfig.moveWhenMouseStationary.get();
    }

    public static boolean getSlowdownOnCorners() {
        return EyeMineConfig.slowdownOnCorners.get();
    }

    public static boolean getAllowLadderDescent() {
        return EyeMineConfig.allowLadderDescent.get();
    }

    public static float getCustomSpeedFactor() {
        return EyeMineConfig.customSpeedFactor.get().floatValue();
    }

    public static float getBoatMaxTurnAtSpeed() {
        return EyeMineConfig.boatMaxTurnAtSpeed.get().floatValue();
    }

    public static double getBoatSlowdown() {
        return EyeMineConfig.boatSlowdown.get();
    }

    public static double getDwellTimeSeconds() {
        return EyeMineConfig.dwellTimeSeconds.get();
    }

    public static double getDwellLockonTimeSeconds() {
        return EyeMineConfig.dwellLockonTimeSeconds.get();
    }

    public static boolean getDwellShowWithTransparency() {
        return EyeMineConfig.dwellShowWithTransparency.get();
    }

    public static boolean getDwellShowExpanding() {
        return EyeMineConfig.dwellShowExpanding.get();
    }
}
