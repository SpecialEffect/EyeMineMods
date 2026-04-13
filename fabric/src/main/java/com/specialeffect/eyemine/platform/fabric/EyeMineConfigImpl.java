package com.specialeffect.eyemine.platform.fabric;

import com.specialeffect.eyemine.config.EyeMineConfig;
import com.specialeffect.eyemine.platform.services.IEyeMineConfigService;
import me.shedaniel.autoconfig.AutoConfig;

public class EyeMineConfigImpl implements IEyeMineConfigService {
    private EyeMineConfig getConfig() {
        return AutoConfig.getConfigHolder(EyeMineConfig.class).getConfig();
    }

    @Override public boolean getDisableCustomNewWorld() { return getConfig().advanced.disableCustomNewWorld; }
    @Override public void setCustomSpeedFactor(float speed) {
        getConfig().general.customSpeedFactor = speed;
        AutoConfig.getConfigHolder(EyeMineConfig.class).save();
    }
    @Override public boolean getUsingMouseEmulation() { return getConfig().general.usingMouseEmulation; }
    @Override public boolean getAutoSelectTool() { return getConfig().general.autoSelectTool; }
    @Override public boolean getAutoSelectSword() { return getConfig().general.autoSelectSword; }
    @Override public int getRadiusChests() { return getConfig().advanced.radiusChests; }
    @Override public int getTicksBetweenMining() { return getConfig().advanced.ticksBetweenMining; }
    @Override public double getBowDrawTime() { return getConfig().advanced.bowDrawTime; }
    @Override public float getFullscreenOverlayAlpha() { return getConfig().advanced.fullscreenOverlayAlpha; }
    @Override public int getIronsightsFovReduction() { return getConfig().advanced.ironsightsFovReduction; }
    @Override public double getIronsightsSensitivityReduction() { return getConfig().advanced.ironsightsSensitivityReduction; }
    @Override public boolean getUseDwellForSingleUseItem() { return getConfig().dwell.useDwellForSingleUseItem; }
    @Override public boolean getUseDwellForSingleMine() { return getConfig().dwell.useDwellForSingleMine; }
    @Override public int getRadiusDoors() { return getConfig().advanced.radiusDoors; }
    @Override public boolean getServerCompatibilityMode() { return getConfig().advanced.serverCompatibilityMode; }
    @Override public int getFlyHeightManual() { return getConfig().movement.flyHeightManual; }
    @Override public int getFlyHeightAuto() { return getConfig().movement.flyHeightAuto; }
    @Override public boolean getDefaultDoAutoJump() { return getConfig().general.defaultDoAutoJump; }
    @Override public boolean getDisableAutoJumpFixes() { return getConfig().general.disableAutoJumpFixes; }
    @Override public boolean getMoveWhenMouseStationary() { return getConfig().movement.moveWhenMouseStationary; }
    @Override public boolean getSlowdownOnCorners() { return getConfig().movement.slowdownOnCorners; }
    @Override public int getWalkingSlowdownFilter() { return getConfig().movement.walkingSlowdownFilter; }
    @Override public boolean getAllowLadderDescent() { return getConfig().movement.allowLadderDescent; }
    @Override public float getCustomSpeedFactor() { return getConfig().general.customSpeedFactor; }
    @Override public float getBoatMaxTurnAtSpeed() { return getConfig().movement.boatMaxTurnAtSpeed; }
    @Override public double getBoatSlowdown() { return getConfig().movement.boatSlowdown; }
    @Override public double getDwellTimeSeconds() { return getConfig().dwell.dwellTimeSeconds; }
    @Override public double getDwellLockonTimeSeconds() { return getConfig().dwell.dwellLockonTimeSeconds; }
    @Override public boolean getDwellShowWithTransparency() { return getConfig().dwell.dwellShowWithTransparency; }
    @Override public boolean getDwellShowExpanding() { return getConfig().dwell.dwellShowExpanding; }
    @Override public boolean getSlowdownOnAttack() { return getConfig().movement.slowdownOnAttack; }
    @Override public int getGazeIdleThreshold() { return getConfig().movement.gazeIdleThreshold; }
}
