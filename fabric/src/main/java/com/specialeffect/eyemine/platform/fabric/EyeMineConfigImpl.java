package com.specialeffect.eyemine.platform.fabric;

import com.specialeffect.eyemine.config.EyeMineConfig;
import me.shedaniel.autoconfig.AutoConfig;

public class EyeMineConfigImpl {
	public static boolean getDisableCustomNewWorld() {
		EyeMineConfig config = AutoConfig.getConfigHolder(EyeMineConfig.class).getConfig();
		return config.advanced.disableCustomNewWorld;
	}

	public static void setCustomSpeedFactor(float speed) {
		EyeMineConfig config = AutoConfig.getConfigHolder(EyeMineConfig.class).getConfig();
		config.general.customSpeedFactor = speed;
	}

	public static boolean getUsingMouseEmulation() {
		EyeMineConfig config = AutoConfig.getConfigHolder(EyeMineConfig.class).getConfig();
		return config.general.usingMouseEmulation;
	}

	public static boolean getAutoSelectTool() {
		EyeMineConfig config = AutoConfig.getConfigHolder(EyeMineConfig.class).getConfig();
		return config.general.autoSelectTool;
	}

	public static boolean getAutoSelectSword() {
		EyeMineConfig config = AutoConfig.getConfigHolder(EyeMineConfig.class).getConfig();
		return config.general.autoSelectSword;
	}

	public static int getRadiusChests() {
		EyeMineConfig config = AutoConfig.getConfigHolder(EyeMineConfig.class).getConfig();
		return config.advanced.radiusChests;
	}

	public static int getTicksBetweenMining() {
		EyeMineConfig config = AutoConfig.getConfigHolder(EyeMineConfig.class).getConfig();
		return config.advanced.ticksBetweenMining;
	}

	public static double getBowDrawTime() {
		EyeMineConfig config = AutoConfig.getConfigHolder(EyeMineConfig.class).getConfig();
		return config.advanced.bowDrawTime;
	}

	public static float getFullscreenOverlayAlpha() {
		EyeMineConfig config = AutoConfig.getConfigHolder(EyeMineConfig.class).getConfig();
		return config.advanced.fullscreenOverlayAlpha;
	}

	public static int getIronsightsFovReduction() {
		EyeMineConfig config = AutoConfig.getConfigHolder(EyeMineConfig.class).getConfig();
		return config.advanced.ironsightsFovReduction;
	}

	public static double getIronsightsSensitivityReduction() {
		EyeMineConfig config = AutoConfig.getConfigHolder(EyeMineConfig.class).getConfig();
		return config.advanced.ironsightsSensitivityReduction;
	}

	public static boolean getUseDwellForSingleUseItem() {
		EyeMineConfig config = AutoConfig.getConfigHolder(EyeMineConfig.class).getConfig();
		return config.dwell.useDwellForSingleUseItem;
	}

	public static boolean getUseDwellForSingleMine() {
		EyeMineConfig config = AutoConfig.getConfigHolder(EyeMineConfig.class).getConfig();
		return config.dwell.useDwellForSingleMine;
	}

	public static int getRadiusDoors() {
		EyeMineConfig config = AutoConfig.getConfigHolder(EyeMineConfig.class).getConfig();
		return config.advanced.radiusDoors;
	}

	public static boolean getServerCompatibilityMode() {
		EyeMineConfig config = AutoConfig.getConfigHolder(EyeMineConfig.class).getConfig();
		return config.advanced.serverCompatibilityMode;
	}

	public static int getFlyHeightManual() {
		EyeMineConfig config = AutoConfig.getConfigHolder(EyeMineConfig.class).getConfig();
		return config.movement.flyHeightManual;
	}

	public static int getFlyHeightAuto() {
		EyeMineConfig config = AutoConfig.getConfigHolder(EyeMineConfig.class).getConfig();
		return config.movement.flyHeightAuto;
	}

	public static boolean getDefaultDoAutoJump() {
		EyeMineConfig config = AutoConfig.getConfigHolder(EyeMineConfig.class).getConfig();
		return config.general.defaultDoAutoJump;
	}

	public static boolean getDisableAutoJumpFixes() {
		EyeMineConfig config = AutoConfig.getConfigHolder(EyeMineConfig.class).getConfig();
		return config.general.disableAutoJumpFixes;
	}

	public static boolean getMoveWhenMouseStationary() {
		EyeMineConfig config = AutoConfig.getConfigHolder(EyeMineConfig.class).getConfig();
		return config.movement.moveWhenMouseStationary;
	}

	public static boolean getSlowdownOnCorners() {
		EyeMineConfig config = AutoConfig.getConfigHolder(EyeMineConfig.class).getConfig();
		return config.movement.slowdownOnCorners;
	}

	public static int getWalkingSlowdownFilter() {
		EyeMineConfig config = AutoConfig.getConfigHolder(EyeMineConfig.class).getConfig();
		return config.movement.walkingSlowdownFilter;
	}

	public static boolean getAllowLadderDescent() {
		EyeMineConfig config = AutoConfig.getConfigHolder(EyeMineConfig.class).getConfig();
		return config.movement.allowLadderDescent;
	}

	public static float getCustomSpeedFactor() {
		EyeMineConfig config = AutoConfig.getConfigHolder(EyeMineConfig.class).getConfig();
		return config.general.customSpeedFactor;
	}

	public static float getBoatMaxTurnAtSpeed() {
		EyeMineConfig config = AutoConfig.getConfigHolder(EyeMineConfig.class).getConfig();
		return config.movement.boatMaxTurnAtSpeed;
	}

	public static double getBoatSlowdown() {
		EyeMineConfig config = AutoConfig.getConfigHolder(EyeMineConfig.class).getConfig();
		return config.movement.boatSlowdown;
	}

	public static double getDwellTimeSeconds() {
		EyeMineConfig config = AutoConfig.getConfigHolder(EyeMineConfig.class).getConfig();
		return config.dwell.dwellTimeSeconds;
	}

	public static double getDwellLockonTimeSeconds() {
		EyeMineConfig config = AutoConfig.getConfigHolder(EyeMineConfig.class).getConfig();
		return config.dwell.dwellLockonTimeSeconds;
	}

	public static boolean getDwellShowWithTransparency() {
		EyeMineConfig config = AutoConfig.getConfigHolder(EyeMineConfig.class).getConfig();
		return config.dwell.dwellShowWithTransparency;
	}

	public static boolean getDwellShowExpanding() {
		EyeMineConfig config = AutoConfig.getConfigHolder(EyeMineConfig.class).getConfig();
		return config.dwell.dwellShowExpanding;
	}

	public static boolean getSlowdownOnAttack() {
		EyeMineConfig config = AutoConfig.getConfigHolder(EyeMineConfig.class).getConfig();
		return config.movement.slowdownOnAttack;
	}
}
