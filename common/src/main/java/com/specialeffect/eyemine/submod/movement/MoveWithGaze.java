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
import com.specialeffect.eyemine.client.MainClientHandler;
import com.specialeffect.eyemine.client.gui.crosshair.StateOverlay;
import com.specialeffect.eyemine.mixin.KeyMappingAccessor;
import com.specialeffect.eyemine.platform.EyeMineConfig;
import com.specialeffect.eyemine.submod.IConfigListener;
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.eyemine.submod.misc.ContinuouslyAttack;
import com.specialeffect.eyemine.submod.mouse.MouseHandlerMod;
import com.specialeffect.eyemine.utils.KeyboardInputHelper;
import com.specialeffect.utils.ModUtils;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.text.DecimalFormat;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class MoveWithGaze extends SubMod implements IConfigListener {
	public final String MODID = "movewithgaze";

	private static KeyMapping mToggleAutoWalkKB;
	private static KeyMapping mIncreaseWalkSpeedKB;
	private static KeyMapping mDecreaseWalkSpeedKB;

	private static int mQueueLength = 50;

	private static boolean mMoveWhenMouseStationary = false;
	public static float mCustomSpeedFactor = 0.8f;

	private int jumpTicks = 0;

	private final BoatController boatController = new BoatController(0.35, 0.15, 0);

	public MoveWithGaze() {
	}

	public void onInitializeClient() {
		// Register key bindings
		Keybindings.keybindings.add(mToggleAutoWalkKB = new KeyMapping(
				"key.eyemine.toggle_walking_forward",
				Type.KEYSYM,
				GLFW.GLFW_KEY_H,
				"category.eyemine.category.eyegaze_common" // The translation key of the keybinding's category.
		));

		Keybindings.keybindings.add(mIncreaseWalkSpeedKB = new KeyMapping(
				"key.eyemine.increase_walk_speed",
				Type.KEYSYM,
				GLFW.GLFW_KEY_UP,
				"category.eyemine.category.eyegaze_settings" // The translation key of the keybinding's category.
		));
		Keybindings.keybindings.add(mDecreaseWalkSpeedKB = new KeyMapping(
				"key.eyemine.decrease_walk_speed",
				Type.KEYSYM,
				GLFW.GLFW_KEY_DOWN,
				"category.eyemine.category.eyegaze_settings" // The translation key of the keybinding's category.
		));

		mPrevLookDirs = new LinkedBlockingQueue<>();

		// Register an icon for the overlay
		mIconIndex = StateOverlay.registerTextureLeft("eyemine:textures/icons/walk.png");

		ClientTickEvent.CLIENT_PRE.register(this::onClientTick);
		ClientRawInputEvent.KEY_PRESSED.register(this::onKeyInput);
	}

	@Override
	public void syncConfig() {
		mQueueLength = EyeMineConfig.getWalkingSlowdownFilter();
		mMoveWhenMouseStationary = EyeMineConfig.getMoveWhenMouseStationary();
		mCustomSpeedFactor = EyeMineConfig.getCustomSpeedFactor();
	}

	private static int mIconIndex;

	public void onClientTick(Minecraft event) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null) {
			if (jumpTicks > 0) {
				jumpTicks--;
			}

			// Add current look dir to queue
			mPrevLookDirs.add(player.getLookAngle());
			while (mPrevLookDirs.size() > mQueueLength) {
				mPrevLookDirs.remove();
			}

			// Explanation of strategy:
			// - when turning a corner, we want to slow down to make it a bit more manageable.
			// - if it takes time to turn the auto-walk function off (e.g. using an eye gaze with dwell click) then
			//   you don't want to continue walking. In this case you can opt to not walk on any ticks where the mouse
			//   hasn't moved at all. This is mainly applicable to gaze input.
			// - If walking into a wall, don't keep walking fast!

			if (mDoingAutoWalk && null == minecraft.screen && (mMoveWhenMouseStationary || MouseHandlerMod.hasPendingEvent())) {
				double forward = (double) mCustomSpeedFactor;

				// Slow down when you're looking really far up/down, or turning round quickly
				if (EyeMineConfig.getSlowdownOnCorners()) {
					double slowDownPitch = slowdownFactorPitch(player);

					// Slow down when you've been turning a corner
					double slowDownCorners = slowdownFactorViewDirs();
//	            	System.out.println("slowdown corners: "+ slowDownCorners);

					if (!player.onClimbable()) {
						forward *= Math.min(slowDownCorners, slowDownPitch);
					}
				}

				// Don't go "forward" if looking down ladder - let it naturally go down
				// This may interfere in funny ways with the vanilla logic for whether or not you're on a ladder
				// Consider it experimental (it's not turned on by default)
				if (EyeMineConfig.getAllowLadderDescent() && player.onClimbable()) {
					// We're a bit more forgiving when player is on ground, to make sure player can exit the
					// ladder okay.
					if ((player.onGround() && player.getXRot() > 30) ||
							(!player.onGround() && player.getXRot() > 0)) {
						KeyboardInputHelper.setWalkOverride(false, 0.0f);
						return;
					}
				}

				// Slow down if you're facing an animal/mob while attacking
				// (without this it's easy to run past)
				if (EyeMineConfig.getSlowdownOnAttack()) {
					if (ContinuouslyAttack.mIsAttacking) {
						forward *= slowdownFactorEntity(player);
					}
				}

				// The built-in autojump doesn't work when you're underwater, so we do our own implementation here
				if (player.isInWater()) {
					// Check the blocks around the player
					Level level = minecraft.level;

					Vec3 posVec = player.position();
					Vec3 forwardVec = player.getForward();

					//javax.vecmath.Vector2d is not available... let's use Vector2f and manually normalize the same way javax.vecmath.Vector2d did
					double norm = (double) (1.0 / Math.sqrt(forwardVec.x * forwardVec.x + forwardVec.y * forwardVec.y));
					Vec2 forward2d = new Vec2((float) (forwardVec.x * norm), (float) (forwardVec.y * norm));

					BlockPos blockInFrontPos = BlockPos.containing(
							posVec.x + forward2d.x,
							posVec.y, //y is UP
							posVec.z + forward2d.y);

					BlockPos blockInFrontAbovePos = blockInFrontPos.offset(0, 1, 0);

					BlockState stateInFront = level.getBlockState(blockInFrontPos);
					BlockState stateAboveInFront = level.getBlockState(blockInFrontAbovePos);

					if ((stateInFront != null && stateInFront.isSolid()) && (stateAboveInFront != null && !stateAboveInFront.isSolid())) {
						if (jumpTicks == 0) {
							player.connection.send(new ServerboundPlayerInputPacket(player.xxa, player.zza, true, player.input.shiftKeyDown));
							player.jumpFromGround();

							// only jump every N ticks...
							jumpTicks = 20;
						}
					}
				}


				// If riding, we may need to do things differently
				if (player.isPassenger()) {
					Entity riddenEntity = player.getVehicle();

					if (null != riddenEntity) {
						if (riddenEntity instanceof Boat boat) {
							// very special case: you can't steer a boat without keys,
							// so we first steer left/right with keys until the boat
							// and the player's view are aligned, only then move 
							// forward 
							if (boat.isControlledByLocalInstance()) {
								float yawError = boat.getYRot() - player.getYRot();
								yawError %= 360;
								if (yawError < -180) {
									yawError += 360;
								}
								if (yawError > 180) {
									yawError -= 360;
								}
								LOGGER.debug(yawError);
								boatController.pid_step(boat, yawError);

								// downscale the forward motion if we've got lots of turning to do first
								float yawErrorAbs = Math.abs(yawError);
								float maxYawForward = EyeMineConfig.getBoatMaxTurnAtSpeed();
								if (yawErrorAbs > maxYawForward * 0.2f) {
									forward *= (maxYawForward - yawErrorAbs) / maxYawForward;
								} else if (yawErrorAbs > maxYawForward) {
									forward = 0.0;
								}

								// slower in general since boats are quite hard to control
								forward *= EyeMineConfig.getBoatSlowdown();

							}
						} else if (riddenEntity instanceof Minecart) {
							Vec3 motion3d = player.getLookAngle();
							Vec3 motionAligned = motion3d.multiply(1.0, 0, 1.0);
							motionAligned.normalize();

							// Our movement override isn't enough to get cart moving
							// It's critical we add motion to player on the server, not just
							// locally
							player.setDeltaMovement(motionAligned);
							player.connection.send(new ServerboundMoveVehiclePacket(riddenEntity)); //TEST IF WORKS

						} else {
							// Any other ridden entities that don't work with the movement override??
						}
					}
				} else {
					boatController.releaseKeys();
				}
				LOGGER.debug(forward);
				KeyboardInputHelper.setWalkOverride(mDoingAutoWalk, (float) forward);

			} else {
				boatController.releaseKeys();
				KeyboardInputHelper.setWalkOverride(false, 0.0f);
			}
		}
	}

	private double slowdownFactorPitch(Player player) {
		float f = player.getXRot();
		if (f < -75 || f > 75) {
			return 0.15f;
		} else if (f < -60 || f > 60) {
			return 0.3f;
		} else if (f < -30 || f > 40) {
			return 0.5f;
		} else {
			return 1.0f;
		}
	}

	private double slowdownFactorEntity(Player player) {
		EntityHitResult entityResult = ModUtils.getMouseOverEntity();
		if (entityResult != null) {
			Entity hitEntity = entityResult.getEntity();
			if (hitEntity instanceof LivingEntity liveEntity) {
				if (liveEntity != null) {
					return 0.2f;
				}
			}
		}
		return 1.0f;
	}

	private double slowdownFactorViewDirs() {
		// Scale forward-distance by the normal congruency of the last X view-dirs.
		// We use normal congruency over several ticks to:
		// - smooth out noise, and
		// - smooth out effect over time (e.g. keep slow-ish for a couple of ticks after
		// movement).
		double scalarLength = mPrevLookDirs.size();
		Vec3 vectorSum = new Vec3(0, 0, 0);

		// TODO: Sums could be done incrementally rather than looping over everything
		// each time.
		for (Vec3 mPrevLookDir : mPrevLookDirs) {
			vectorSum = vectorSum.add(mPrevLookDir);
		}
		double vectorLength = vectorSum.length();
		double normalCongruency = vectorLength / scalarLength;

		// If in auto-walk mode, walk forward an amount scaled by the view change (less
		// if looking around)
		double thresh = 0.9; // below this, no movement
		double slowdownFactor = Math.max(0, (normalCongruency - thresh) / (1.0 - thresh));
		return slowdownFactor;
	}

	private static boolean mDoingAutoWalk = false;
	private Queue<Vec3> mPrevLookDirs;

	public static void stop() {
		if (mDoingAutoWalk) {
			mDoingAutoWalk = false;
			StateOverlay.setStateLeftIcon(mIconIndex, mDoingAutoWalk);

			// Make sure any overridden key bindings are removed
			final KeyMapping kbLeft = Minecraft.getInstance().options.keyLeft;
			final KeyMapping kbRight = Minecraft.getInstance().options.keyRight;
			KeyMapping.set(((KeyMappingAccessor) kbLeft).getActualKey(), false);
			KeyMapping.set(((KeyMappingAccessor) kbRight).getActualKey(), false);
		}
	}

	public static boolean isWalking() {
		return mDoingAutoWalk;
	}

	private EventResult onKeyInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
		if (ModUtils.hasActiveGui()) {
			return EventResult.pass();
		}

		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 292)) {
			return EventResult.pass();
		}

		if (mToggleAutoWalkKB.matches(keyCode, scanCode) && mToggleAutoWalkKB.consumeClick()) {
			mDoingAutoWalk = !mDoingAutoWalk;
			MouseHandlerMod.setWalking(mDoingAutoWalk);
			StateOverlay.setStateLeftIcon(mIconIndex, mDoingAutoWalk);
			if (!mDoingAutoWalk) {
				MoveWithGaze.stop();
			}
			ModUtils.sendPlayerMessage("Auto walk: " + (mDoingAutoWalk ? "ON" : "OFF"));
		}
		if (mDecreaseWalkSpeedKB.matches(keyCode, scanCode) && mDecreaseWalkSpeedKB.consumeClick()) {
			float newSpeed = (float) Math.max(0.1d, 0.9d * EyeMineConfig.getCustomSpeedFactor());
			MainClientHandler.saveWalkingSpeed(newSpeed);
			displayCurrentSpeed();
		}
		if (mIncreaseWalkSpeedKB.matches(keyCode, scanCode) && mIncreaseWalkSpeedKB.consumeClick()) {
			float newSpeed = (float) Math.min(2.0d, EyeMineConfig.getCustomSpeedFactor() * 1.1d);
			MainClientHandler.saveWalkingSpeed(newSpeed);
			displayCurrentSpeed();
		}
		return EventResult.pass();
	}

	private void displayCurrentSpeed() {
		DecimalFormat myFormatter = new DecimalFormat("#0.00");
		String speedString = myFormatter.format(EyeMineConfig.getCustomSpeedFactor());
		ModUtils.sendPlayerMessage("Walking speed: " + speedString);
	}
}
