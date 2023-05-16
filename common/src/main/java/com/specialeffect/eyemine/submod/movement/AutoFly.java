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

package com.specialeffect.eyemine.submod.movement;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import com.specialeffect.eyemine.client.Keybindings;
import com.specialeffect.eyemine.client.gui.crosshair.StateOverlay;
import com.specialeffect.eyemine.packets.PacketHandler;
import com.specialeffect.eyemine.packets.messages.ChangeFlyingStateMessage;
import com.specialeffect.eyemine.platform.EyeMineConfig;
import com.specialeffect.eyemine.submod.IConfigListener;
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.utils.ModUtils;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class AutoFly extends SubMod implements IConfigListener {
	private static KeyMapping mFlyManualKB;
	private static KeyMapping mFlyAutoKB;
	private static KeyMapping mFlyUpKB;
	private static KeyMapping mFlyDownKB;

	private static int mFlyHeightManual = 2;
	private static int mFlyHeightAuto = 6;

	private static int mIconIndexAuto;
	private static int mIconIndexManual;

	public void onInitializeClient() {
		// Register key bindings
		Keybindings.keybindings.add(mFlyManualKB = new KeyMapping(
				"key.eyemine.toggle_manual_flying",
				Type.KEYSYM,
				GLFW.GLFW_KEY_COMMA,
				"category.eyemine.category.eyegaze_extra" // The translation key of the keybinding's category.
		));
		Keybindings.keybindings.add(mFlyAutoKB = new KeyMapping(
				"key.eyemine.toggle_auto_flying",
				Type.KEYSYM,
				GLFW.GLFW_KEY_G,
				"category.eyemine.category.eyegaze_extra" // The translation key of the keybinding's category.
		));
		Keybindings.keybindings.add(mFlyUpKB = new KeyMapping(
				"key.eyemine.fly_higher",
				Type.KEYSYM,
				GLFW.GLFW_KEY_PERIOD,
				"category.eyemine.category.eyegaze_extra" // The translation key of the keybinding's category.
		));
		Keybindings.keybindings.add(mFlyDownKB = new KeyMapping(
				"key.eyemine.fly_lower",
				Type.KEYSYM,
				GLFW.GLFW_KEY_APOSTROPHE,
				"category.eyemine.category.eyegaze_extra" // The translation key of the keybinding's category.
		));

		ClientTickEvent.CLIENT_PRE.register(this::onClientTick);
		ClientRawInputEvent.KEY_PRESSED.register(this::onKeyInput);

		// Register an icon for the overlay
		mIconIndexAuto = StateOverlay.registerTextureLeft("eyemine:textures/icons/fly-auto.png");
		mIconIndexManual = StateOverlay.registerTextureLeft("eyemine:textures/icons/fly.png");
	}

	public void syncConfig() {
		mFlyHeightManual = EyeMineConfig.getFlyHeightManual();
		mFlyHeightAuto = EyeMineConfig.getFlyHeightAuto();
	}

	public void onClientTick(Minecraft event) {
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		if (null != player) {
			// If auto flying, and about to bump into something, fly more!
			if (mIsFlyingAuto && player.getAbilities().mayfly && player.getAbilities().flying) {
				BlockPos playerPos = player.blockPosition();
				Vec3 lookVec = player.getLookAngle();

				Level level = minecraft.level;
				// Check all three blocks ahead of player
				for (int yDiff = -1; yDiff < 2; yDiff++) {
					BlockPos blockPosInFrontOfPlayer = BlockPos.containing(playerPos.getX() + lookVec.x,
							playerPos.getY() + yDiff, playerPos.getZ() + lookVec.z);

					// If there's a block in your way, and you're not already jumping over it...

					Vec3 motion = player.getDeltaMovement();
					Vec3 addMotion = new Vec3(0.0, Math.max(mFlyHeightAuto / 4, 1), 0.0);
					if (level.getBlockState(blockPosInFrontOfPlayer).getMaterial().blocksMotion() &&
							motion.y == 0) {
						player.setDeltaMovement(motion.add(addMotion));
						break;
					}
				}
			}

			// Check flying wasn't forcefully stopped from elsewhere
			if ((flying()) &&
					!player.getAbilities().flying) {
				updateAfterStopFlying();
			}
			// If flying was turned on elsewhere, make it 'manual'
			if (!mIsFlyingAuto && !mIsFlyingManual &&
					player.getAbilities().flying) {
				mIsFlyingManual = true;
				updateIcons();
			}
		}
	}

	private static boolean mIsFlyingManual = false;
	private static boolean mIsFlyingAuto = false;

	private static void updateIcons() {
		StateOverlay.setStateLeftIcon(mIconIndexAuto, mIsFlyingAuto);
		StateOverlay.setStateLeftIcon(mIconIndexManual, mIsFlyingManual);
	}

	public static boolean flying() {
		return (mIsFlyingAuto || mIsFlyingManual);
	}

	// Update state if flying was stopped from elsewhere
	private void updateAfterStopFlying() {
		mIsFlyingAuto = false;
		mIsFlyingManual = false;
		updateIcons();
	}

	private void stopFlying() {
		mIsFlyingAuto = false;
		mIsFlyingManual = false;

		Player player = Minecraft.getInstance().player;

		player.getAbilities().flying = false;
		PacketHandler.CHANNEL.sendToServer(new ChangeFlyingStateMessage(false, 0));
		updateIcons();
	}

	private void setFlying(final boolean bFlyUp, final boolean isAuto) {
		Player player = Minecraft.getInstance().player;

		mIsFlyingAuto = isAuto;
		mIsFlyingManual = !isAuto;

		if (!player.getAbilities().mayfly) {
			player.sendSystemMessage(Component.literal(
					"Player unable to fly"));
			return;
		}

		// stop sneaking (if we are), which prevents flying
		Sneak.stop();

		// start flying
		player.getAbilities().flying = true;
		int flyHeight = 0;
		if (bFlyUp) {
			if (mIsFlyingAuto) {
				flyHeight = mFlyHeightAuto;
			}
			if (mIsFlyingManual) {
				flyHeight = mFlyHeightManual;
			}

			player.move(MoverType.SELF, new Vec3(0, flyHeight, 0));
		}

		PacketHandler.CHANNEL.sendToServer(new ChangeFlyingStateMessage(true, flyHeight));

		updateIcons();

	}

	private void flyDown() {
		Player player = Minecraft.getInstance().player;

		if (!player.getAbilities().mayfly) {
			player.sendSystemMessage(Component.literal(
					"Player unable to fly"));
			return;
		}
		if (!mIsFlyingAuto && !mIsFlyingManual) {
			player.sendSystemMessage(Component.literal(
					"Player not flying"));
			return;
		}

		// fly upward
		int flyHeight = 0;
		if (mIsFlyingAuto) {
			flyHeight = mFlyHeightAuto;
		}
		if (mIsFlyingManual) {
			flyHeight = mFlyHeightManual;
		}

		player.move(MoverType.SELF, new Vec3(0, -flyHeight, 0));

	}

	private EventResult onKeyInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
		if (ModUtils.hasActiveGui()) {
			return EventResult.pass();
		}

		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 292)) {
			return EventResult.pass();
		}

		if (mFlyManualKB.matches(keyCode, scanCode) && mFlyManualKB.consumeClick()) {
			if (mIsFlyingManual) {
				ModUtils.sendPlayerMessage("Fly manual: OFF");
				this.stopFlying();
			} else {
				ModUtils.sendPlayerMessage("Fly manual: ON");
				boolean doFlyUp = !mIsFlyingAuto;
				this.setFlying(doFlyUp, false);
			}
		} else if (mFlyAutoKB.matches(keyCode, scanCode) && mFlyAutoKB.consumeClick()) {
			if (mIsFlyingAuto) {
				ModUtils.sendPlayerMessage("Fly auto: OFF");
				this.stopFlying();
			} else {
				ModUtils.sendPlayerMessage("Fly auto: ON");
				boolean doFlyUp = !mIsFlyingManual;
				this.setFlying(doFlyUp, true);
			}
		} else if (mFlyUpKB.matches(keyCode, scanCode) && mFlyUpKB.consumeClick()) {
			this.setFlying(true, mIsFlyingAuto);
		} else if (mFlyDownKB.matches(keyCode, scanCode) && mFlyDownKB.consumeClick()) {
			flyDown();
		}
		AutoFly.updateIcons();

		return EventResult.pass();
	}

}
