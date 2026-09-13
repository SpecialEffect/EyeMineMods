package com.specialeffect.eyemine.platform.services;

public interface IEyeMineConfigService {
    boolean getDisableCustomNewWorld();
    void setCustomSpeedFactor(float speed);
    boolean getUsingMouseEmulation();
    boolean getAutoSelectTool();
    boolean getAutoSelectSword();
    int getTicksBetweenMining();
    int getRadiusChests();
    double getBowDrawTime();
    float getFullscreenOverlayAlpha();
    int getIronsightsFovReduction();
    double getIronsightsSensitivityReduction();
    boolean getUseDwellForSingleUseItem();
    boolean getUseDwellForSingleMine();
    boolean getServerCompatibilityMode();
    int getRadiusDoors();
    int getFlyHeightManual();
    int getFlyHeightAuto();
    boolean getDefaultDoAutoJump();
    boolean getDisableAutoJumpFixes();
    boolean getMoveWhenMouseStationary();
    boolean getSlowdownOnCorners();
    int getWalkingSlowdownFilter();
    boolean getAllowLadderDescent();
    float getCustomSpeedFactor();
    float getBoatMaxTurnAtSpeed();
    double getBoatSlowdown();
    double getDwellTimeSeconds();
    double getDwellLockonTimeSeconds();
    boolean getDwellShowWithTransparency();
    boolean getDwellShowExpanding();
    boolean getSlowdownOnAttack();
    int getGazeIdleThreshold();
}
