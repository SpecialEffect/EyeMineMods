package com.specialeffect.eyemine.platform;

public class EyeMineConfig {
    public static boolean getDisableCustomNewWorld() { return Services.CONFIG.getDisableCustomNewWorld(); }
    public static void setCustomSpeedFactor(float speed) { Services.CONFIG.setCustomSpeedFactor(speed); }
    public static boolean getUsingMouseEmulation() { return Services.CONFIG.getUsingMouseEmulation(); }
    public static boolean getAutoSelectTool() { return Services.CONFIG.getAutoSelectTool(); }
    public static boolean getAutoSelectSword() { return Services.CONFIG.getAutoSelectSword(); }
    public static int getTicksBetweenMining() { return Services.CONFIG.getTicksBetweenMining(); }
    public static int getRadiusChests() { return Services.CONFIG.getRadiusChests(); }
    public static double getBowDrawTime() { return Services.CONFIG.getBowDrawTime(); }
    public static float getFullscreenOverlayAlpha() { return Services.CONFIG.getFullscreenOverlayAlpha(); }
    public static int getIronsightsFovReduction() { return Services.CONFIG.getIronsightsFovReduction(); }
    public static double getIronsightsSensitivityReduction() { return Services.CONFIG.getIronsightsSensitivityReduction(); }
    public static boolean getUseDwellForSingleUseItem() { return Services.CONFIG.getUseDwellForSingleUseItem(); }
    public static boolean getUseDwellForSingleMine() { return Services.CONFIG.getUseDwellForSingleMine(); }
    public static boolean getServerCompatibilityMode() { return Services.CONFIG.getServerCompatibilityMode(); }
    public static int getRadiusDoors() { return Services.CONFIG.getRadiusDoors(); }
    public static int getFlyHeightManual() { return Services.CONFIG.getFlyHeightManual(); }
    public static int getFlyHeightAuto() { return Services.CONFIG.getFlyHeightAuto(); }
    public static boolean getDefaultDoAutoJump() { return Services.CONFIG.getDefaultDoAutoJump(); }
    public static boolean getDisableAutoJumpFixes() { return Services.CONFIG.getDisableAutoJumpFixes(); }
    public static boolean getMoveWhenMouseStationary() { return Services.CONFIG.getMoveWhenMouseStationary(); }
    public static boolean getSlowdownOnCorners() { return Services.CONFIG.getSlowdownOnCorners(); }
    public static int getWalkingSlowdownFilter() { return Services.CONFIG.getWalkingSlowdownFilter(); }
    public static boolean getAllowLadderDescent() { return Services.CONFIG.getAllowLadderDescent(); }
    public static float getCustomSpeedFactor() { return Services.CONFIG.getCustomSpeedFactor(); }
    public static float getBoatMaxTurnAtSpeed() { return Services.CONFIG.getBoatMaxTurnAtSpeed(); }
    public static double getBoatSlowdown() { return Services.CONFIG.getBoatSlowdown(); }
    public static double getDwellTimeSeconds() { return Services.CONFIG.getDwellTimeSeconds(); }
    public static double getDwellLockonTimeSeconds() { return Services.CONFIG.getDwellLockonTimeSeconds(); }
    public static boolean getDwellShowWithTransparency() { return Services.CONFIG.getDwellShowWithTransparency(); }
    public static boolean getDwellShowExpanding() { return Services.CONFIG.getDwellShowExpanding(); }
    public static boolean getSlowdownOnAttack() { return Services.CONFIG.getSlowdownOnAttack(); }
    public static int getGazeIdleThreshold() { return Services.CONFIG.getGazeIdleThreshold(); }
}
