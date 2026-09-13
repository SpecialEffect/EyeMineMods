package com.specialeffect.eyemine.platform.neoforge;

import com.specialeffect.eyemine.config.EyeMineConfig;
import com.specialeffect.eyemine.platform.services.IEyeMineConfigService;

public class EyeMineConfigImpl implements IEyeMineConfigService {
    @Override public boolean getDisableCustomNewWorld() { return EyeMineConfig.disableCustomNewWorld.get(); }
    @Override public void setCustomSpeedFactor(float speed) {
        EyeMineConfig.customSpeedFactor.set((double) speed);
        EyeMineConfig.customSpeedFactor.save();
    }
    @Override public boolean getUsingMouseEmulation() { return EyeMineConfig.usingMouseEmulation.get(); }
    @Override public boolean getAutoSelectTool() { return EyeMineConfig.autoSelectTool.get(); }
    @Override public boolean getAutoSelectSword() { return EyeMineConfig.autoSelectSword.get(); }
    @Override public int getRadiusChests() { return EyeMineConfig.radiusChests.get(); }
    @Override public int getTicksBetweenMining() { return EyeMineConfig.ticksBetweenMining.get(); }
    @Override public double getBowDrawTime() { return EyeMineConfig.bowDrawTime.get(); }
    @Override public float getFullscreenOverlayAlpha() { return EyeMineConfig.fullscreenOverlayAlpha.get().floatValue(); }
    @Override public int getIronsightsFovReduction() { return EyeMineConfig.ironsightsFovReduction.get(); }
    @Override public double getIronsightsSensitivityReduction() { return EyeMineConfig.ironsightsSensitivityReduction.get().floatValue(); }
    @Override public boolean getUseDwellForSingleUseItem() { return EyeMineConfig.useDwellForSingleUseItem.get(); }
    @Override public boolean getUseDwellForSingleMine() { return EyeMineConfig.useDwellForSingleMine.get(); }
    @Override public int getRadiusDoors() { return EyeMineConfig.radiusDoors.get(); }
    @Override public boolean getServerCompatibilityMode() { return EyeMineConfig.serverCompatibilityMode.get(); }
    @Override public int getFlyHeightManual() { return EyeMineConfig.flyHeightManual.get(); }
    @Override public int getFlyHeightAuto() { return EyeMineConfig.flyHeightAuto.get(); }
    @Override public boolean getDefaultDoAutoJump() { return EyeMineConfig.defaultDoAutoJump.get(); }
    @Override public boolean getDisableAutoJumpFixes() { return EyeMineConfig.disableAutoJumpFixes.get(); }
    @Override public boolean getMoveWhenMouseStationary() { return EyeMineConfig.moveWhenMouseStationary.get(); }
    @Override public boolean getSlowdownOnCorners() { return EyeMineConfig.slowdownOnCorners.get(); }
    @Override public int getWalkingSlowdownFilter() { return EyeMineConfig.walkingSlowdownFilter.get(); }
    @Override public boolean getAllowLadderDescent() { return EyeMineConfig.allowLadderDescent.get(); }
    @Override public float getCustomSpeedFactor() { return EyeMineConfig.customSpeedFactor.get().floatValue(); }
    @Override public float getBoatMaxTurnAtSpeed() { return EyeMineConfig.boatMaxTurnAtSpeed.get().floatValue(); }
    @Override public double getBoatSlowdown() { return EyeMineConfig.boatSlowdown.get(); }
    @Override public double getDwellTimeSeconds() { return EyeMineConfig.dwellTimeSeconds.get(); }
    @Override public double getDwellLockonTimeSeconds() { return EyeMineConfig.dwellLockonTimeSeconds.get(); }
    @Override public boolean getDwellShowWithTransparency() { return EyeMineConfig.dwellShowWithTransparency.get(); }
    @Override public boolean getDwellShowExpanding() { return EyeMineConfig.dwellShowExpanding.get(); }
    @Override public boolean getSlowdownOnAttack() { return EyeMineConfig.slowdownOnAttack.get(); }
    @Override public int getGazeIdleThreshold() { return EyeMineConfig.gazeIdleThreshold.get(); }
}
