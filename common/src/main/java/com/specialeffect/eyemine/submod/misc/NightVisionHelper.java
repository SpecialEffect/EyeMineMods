/**
 * Copyright (C) 2016-2020 Kirsty McNaught
 * <p>
 * Developed for SpecialEffect, www.specialeffect.org.uk
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.eyemine.submod.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.utils.ModUtils;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.BlockHitResult;

public class NightVisionHelper extends SubMod {
	public static final String MODID = "nightvisionhelper";
	public static final String NAME = "NightVisionHelper";

	private int mDarkTicksAccum = 0;
	private int mShowMessageTicksAccum = 0;
	private int mTicksLoaded = 0;

	private static boolean mShowMessage;
	private static boolean mDisabled;
	private static boolean mTemporarilyDisabled;

	private float mLightnessThreshold;
	private int mTicksThreshold;

	public static void cancelAndHide() {
		mDisabled = true;
		mShowMessage = false;
	}

	public void onInitializeClient() {
		LifecycleEvent.SERVER_LEVEL_LOAD.register(this::onWorldLoad);
		EntityEvent.LIVING_DEATH.register(this::onDeath);
		EntityEvent.ADD.register(this::onSpawn);
		ClientTickEvent.CLIENT_PRE.register(this::onClientTick);
		ClientRawInputEvent.KEY_PRESSED.register(this::onKeyInput);
		ClientGuiEvent.RENDER_HUD.register(this::onRenderExperienceBar);
	}

	private void onWorldLoad(ServerLevel serverLevel) {
		this.resetState();

		// If player has overridden brightness beyond what's allowed in the settings menu, 
		// (i.e. by hacking options.txt) then they are a power user and we'll leave them to it			
		if (Minecraft.getInstance().options.gamma().get() > 1.01D) {
			NightVisionHelper.cancelAndHide();
		}
	}

	private void resetState() {
		// Reset state
		mDisabled = false;
		mShowMessage = false;
		mDarkTicksAccum = 0;
		mShowMessageTicksAccum = 0;
		mTicksLoaded = 0;
		mLightnessThreshold = 0.2f;
		mTicksThreshold = 2 * 20;
	}

	private EventResult onDeath(LivingEntity entity, DamageSource damageSource) {
		if (ModUtils.entityIsMe(entity)) {
			this.resetState();
			mTemporarilyDisabled = true;
		}
		return EventResult.pass();
	}

	private EventResult onSpawn(Entity entity, Level level) {
		if (ModUtils.entityIsMe(entity)) {
			this.resetState();
			mTemporarilyDisabled = false;
		}
		return EventResult.pass();
	}

	public void onClientTick(Minecraft event) {
		Minecraft minecraft = Minecraft.getInstance();
		// Don't apply logic while in loading screen / other UIs
		if (minecraft.screen != null) {
			return;
		}

		final LocalPlayer player = Minecraft.getInstance().player;
		if (player != null) {
			if (!player.isCreative()) {
				// We won't worry about survival players, they know what they're doing
				return;
			}

			final ClientLevel level = minecraft.level;

			// We'll reduce (make stricter) the threshold for showing a warning message once the world
			// has been loaded a while. We're mainly trying to catch the "reloaded into pitch black and
			// don't know what's going on" failure mode.
			if (mTicksLoaded > 20 * 20) {
				mLightnessThreshold = 0.13f;
				mTicksThreshold = 10 * 20;
			} else {
				mTicksLoaded++;
			}

			// If message is visible, keep alive for minimum time
			if (mShowMessage) {
				mShowMessageTicksAccum++;
				if (mDisabled && mShowMessageTicksAccum > 5 * 20) {
					mShowMessage = false;
				}
			}

			if (mDisabled || mTemporarilyDisabled) {
				return;
			}

			BlockHitResult result = ModUtils.getMouseOverBlock();
			if (result != null) {
				BlockPos pos = result.getBlockPos().relative(result.getDirection());
				BlockPos posPlayer = player.blockPosition();

				// This occurs when chunks are loaded on server but not yet propagated - in this case the brightness info can't be trusted
				if (level.getChunk(posPlayer) instanceof EmptyLevelChunk) {
					resetState();
					return;
				}

				// Get lightness of block(s) we're looking at and where player is
				boolean isDark = isDark(level, pos, level.random);

				// If it's really dark, put message up to remind of night vision
				if (isDark) {
					mDarkTicksAccum++;
				} else if (mDarkTicksAccum > 0) {
					mDarkTicksAccum--;
				}

				if (mDarkTicksAccum >= mTicksThreshold) {
					mShowMessage = true;
				}
			}
		}
	}

	public static boolean isDark(Level level, BlockPos blockPos, RandomSource randomSource) {
		if (level.getBrightness(LightLayer.SKY, blockPos) > randomSource.nextInt(32)) {
			return false;
		} else {
			DimensionType dimensionType = level.dimensionType();
			int i = dimensionType.monsterSpawnBlockLightLimit();
			if (i < 15 && level.getBrightness(LightLayer.BLOCK, blockPos) > i) {
				return false;
			} else {
				int j = level.isThundering() ? level.getMaxLocalRawBrightness(blockPos, 10) : level.getMaxLocalRawBrightness(blockPos);
				return j <= dimensionType.monsterSpawnLightTest().sample(randomSource);
			}
		}
	}

	public void onRenderExperienceBar(GuiGraphics guiGraphics, float partialTicks) {
		if (mShowMessage) {
			Minecraft mc = Minecraft.getInstance();

			int w = mc.getWindow().getGuiScaledWidth();
			int h = mc.getWindow().getGuiScaledHeight();

			String msg1 = "You are in the dark!";
			String msg2 = "To turn on night vision, use the EyeMine keyboard or press F12";
			String msg3 = "To reset to start location, press Home";

			Font font = mc.font;

			int y = h / 5;
			drawCenteredString(guiGraphics, font, msg1, w / 2, y, 0xffffff);
			drawCenteredString(guiGraphics, font, msg2, w / 2, y + 20, 0xffffff);
			drawCenteredString(guiGraphics, font, msg3, w / 2, y + 40, 0xffffff);
		}
	}

	private void drawCenteredString(GuiGraphics guiGraphics, Font font, String msg, int x, int y, int c) { //TODO: check if we can just use GuiGraphics#drawCenteredString
		int stringWidth = font.width(msg);
		guiGraphics.drawString(font, msg, x - stringWidth / 2, y, c);
	}

	private EventResult onKeyInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
		// Any key dismisses the message (eventually, after minimum time)
		if (mShowMessage) {
			mDisabled = true;
		}
		return EventResult.pass();
	}
}
