package com.specialeffect.eyemine.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.BoundedDiscrete;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.CollapsibleObject;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.minecraft.util.Mth;

@Config(name = "eyemine")
public class EyeMineConfig implements ConfigData {

	@CollapsibleObject
	public General general = new General();

	public static class General {
		@Comment("Walking speed for walk-with-gaze")
		@BoundedDiscrete(min = 0, max = 2)
		public float customSpeedFactor = 0.6f;

		@Comment("Auto-jump switched on by default")
		public boolean defaultDoAutoJump = true;

		@Comment("Enable mouse-emulation compatibility mode?." +
				" Turn this on if you're using mouse position as an input to EyeMine")
		public boolean usingMouseEmulation = false;

		@Comment("When attacking, do you want a sword selected automatically?" +
				" This only applies in Creative Mode.")
		public boolean autoSelectSword = true;

		@Comment("When mining, do you want pickaxe selected automatically?" +
				" This only applies in Creative Mode.")
		public boolean autoSelectTool = true;
	}

	@CollapsibleObject()
	public Advanced advanced = new Advanced();

	public static class Advanced {
		@Comment("Disable the custom new world screen and the defaults it applies to every world")
		public boolean disableCustomNewWorld = false;

		@Comment("How far away a player needs to be from a chest/table to be able to open it")
		@BoundedDiscrete(min = 1, max = 6)
		public int radiusChests = 5;

		@Comment("How far away a player needs to be from a door to automatically open/close." +
				" Set to zero to turn off automatic door-opening")
		@BoundedDiscrete(min = 0, max = 10)
		public int radiusDoors = 2;

		@Comment("How many ticks to wait before mining again" +
				" Only affects creative mode")
		@BoundedDiscrete(min = 0, max = 50)
		public int ticksBetweenMining = 15;

		@Comment("How much to reduce field of view (degrees) when using ironsights")
		@BoundedDiscrete(min = 0, max = 40)
		public int ironsightsFovReduction = 20;

		@Comment("How much to reduce sensitivity (%) when using ironsights")
		@BoundedDiscrete(min = 0, max = 30)
		public double ironsightsSensitivityReduction = 13;

		@Comment("How long (seconds) to keep bow drawn for when firing with 'Use Item'")
		@BoundedDiscrete(min = 0, max = 5)
		public double bowDrawTime = 1.0;

		@Comment("Opacity of full-screen overlays (look, careful walk) (between 0.0 and 0.2)")
		public float fullscreenOverlayAlpha = 0.1f;

		@Comment("Use simpler mining/placing logic to play on servers without EyeMine installed")
		public boolean serverCompatibilityMode = false;
	}

	@CollapsibleObject()
	public Movement movement = new Movement();

	public static class Movement {
		@Comment("Slow down auto-walk when going round a corner" +
				" You may want to turn this off for survival")
		public boolean slowdownOnCorners = true;

		@Comment("How many ticks to take into account for slowing down while looking around / turning corners. " +
				" (smaller number = faster)")
		@BoundedDiscrete(min = 1, max = 200)
		public int walkingSlowdownFilter = 30;

		@Comment("Continue walking forward when the mouse is stationary?" +
				" Recommended to be turned off for eye gaze control, or turned on for joysticks.")
		public boolean moveWhenMouseStationary = false;

		@Comment("Slow down auto-walk when attacking an entity" +
				" This only applies when your crosshair is over an entity, and makes it easier to chase mobs")
		public boolean slowdownOnAttack = true;

		@Comment("How high to fly (up/down) in manual mode")
		@BoundedDiscrete(min = 1, max = 20)
		public int flyHeightManual = 2;

		@Comment("How high to fly in auto mode")
		@BoundedDiscrete(min = 1, max = 10)
		public int flyHeightAuto = 6;

		@Comment("Descend ladders by looking down while moving. " +
				" Experimental; may cause problems getting on/off ladders.")
		public boolean allowLadderDescent = false;

		// Boats
		@Comment("Slowdown applied to forward motion of boats (lower is slower)")
		@BoundedDiscrete(min = 0, max = 1)
		public double boatSlowdown = 0.5;

		@Comment("Maximum angle (degrees) at which boat will still travel forwards while turning")
		@BoundedDiscrete(min = 1, max = 90)
		public int boatMaxTurnAtSpeed = 30;
	}

	@CollapsibleObject()
	public Dwell dwell = new Dwell();

	public static class Dwell {
		@Comment("Time for dwell to complete (seconds)")
		@BoundedDiscrete(min = 0, max = 5)
		public double dwellTimeSeconds = 1.2;

		@Comment("Time for dwell to lock on (seconds)" +
				" Must be lower than dwellTimeSeconds")
		@BoundedDiscrete(min = 0, max = 1)
		public double dwellLockonTimeSeconds = 0.2;

		@Comment("Show dwell expanding instead of shrinking")
		public boolean dwellShowExpanding = false;

		@Comment("Show dwell by changing transparency instead of growing/shrinking" +
				" This option overrides dwellShowExpanding")
		public boolean dwellShowWithTransparency = false;

		@Comment("Use dwell for 'mine once' (creative only)")
		public boolean useDwellForSingleMine = false;

		@Comment("Use dwell for single 'use item'")
		public boolean useDwellForSingleUseItem = false;
	}

	@Override
	public void validatePostLoad() throws ValidationException {
		general.customSpeedFactor = Mth.clamp(general.customSpeedFactor, 0.25f, 2.0f);

		advanced.fullscreenOverlayAlpha = Mth.clamp(advanced.fullscreenOverlayAlpha, 0.0f, 0.2f);
		advanced.bowDrawTime = Mth.clamp(advanced.bowDrawTime, 0.5, 5.0);

		movement.boatSlowdown = Mth.clamp(movement.boatSlowdown, 0.01, 1.0);

		dwell.dwellTimeSeconds = Mth.clamp(dwell.dwellTimeSeconds, 0.2, 5.0);
	}
}
