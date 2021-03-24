/**
 * Copyright (C) 2016-2020 Kirsty McNaught
 * 
 * Developed for SpecialEffect, www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.mods.moving;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

//import javax.vecmath.Vector2d;

import net.minecraft.util.math.vector.Vector2f;
import org.lwjgl.glfw.GLFW;

import com.specialeffect.gui.StateOverlay;
import com.specialeffect.messages.JumpMessage;
import com.specialeffect.messages.MovePlayerMessage;
import com.specialeffect.mods.EyeMineConfig;
import com.specialeffect.mods.misc.ContinuouslyAttack;
import com.specialeffect.mods.mousehandling.MouseHandler;
import com.specialeffect.mods.utils.KeyWatcher;
import com.specialeffect.mods.ChildMod;
//import com.specialeffect.gui.StateOverlay;
//import com.specialeffect.messages.MovePlayerMessage;
import com.specialeffect.mods.EyeGaze;
import com.specialeffect.overrides.MovementInputFromOptionsOverride;
//import com.specialeffect.mods.misc.ContinuouslyAttack;
//import com.specialeffect.mods.mousehandling.MouseHandler;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

//import net.minecraft.block.LiquidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.minecart.MinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class MoveWithGaze  extends ChildMod implements ChildModWithConfig {
	public final String MODID = "movewithgaze";

	private static KeyBinding mToggleAutoWalkKB;
	private static KeyBinding mIncreaseWalkSpeedKB;
	private static KeyBinding mDecreaseWalkSpeedKB;

	private static int mQueueLength = 50;

	private static boolean mMoveWhenMouseStationary = false;
	public static float mCustomSpeedFactor = 0.8f;

    private int jumpTicks = 0;
    
    private BoatController boatController = new BoatController(0.35,  0.15,  0);	

    
	public MoveWithGaze() {
	}

	public void setup(final FMLCommonSetupEvent event) {

		// setup channel for comms
		this.setupChannel(MODID, 1);

        int id = 0;         
        channel.registerMessage(id++, JumpMessage.class, JumpMessage::encode, 
        		JumpMessage::decode, JumpMessage.Handler::handle);                   	   
        channel.registerMessage(id++, MovePlayerMessage.class, MovePlayerMessage::encode, 
        		MovePlayerMessage::decode, MovePlayerMessage.Handler::handle);                   	   
		        
		// Register key bindings
		mToggleAutoWalkKB = new KeyBinding("Start/stop walking forward", GLFW.GLFW_KEY_H, CommonStrings.EYEGAZE_COMMON);
		ClientRegistry.registerKeyBinding(mToggleAutoWalkKB);
		mIncreaseWalkSpeedKB = new KeyBinding("Turn walk speed up", GLFW.GLFW_KEY_UP, CommonStrings.EYEGAZE_SETTINGS);
		ClientRegistry.registerKeyBinding(mIncreaseWalkSpeedKB);
		mDecreaseWalkSpeedKB = new KeyBinding("Turn walk speed down", GLFW.GLFW_KEY_DOWN,
				CommonStrings.EYEGAZE_SETTINGS);
		ClientRegistry.registerKeyBinding(mDecreaseWalkSpeedKB);

		mPrevLookDirs = new LinkedBlockingQueue<Vector3d>();

		// Register an icon for the overlay
		mIconIndex = StateOverlay.registerTextureLeft("specialeffect:icons/walk.png");
				
	}

	public void syncConfig() {
		mQueueLength =10;
		mMoveWhenMouseStationary = EyeMineConfig.moveWhenMouseStationary.get();
		mCustomSpeedFactor = EyeMineConfig.customSpeedFactor.get().floatValue();
	}

	private static int mIconIndex;

	@SubscribeEvent
    public void onClientTick(ClientTickEvent event) {
		PlayerEntity player = Minecraft.getInstance().player;
    	if (null != player && event.phase == TickEvent.Phase.START) {
    		if (jumpTicks > 0)
			{
				jumpTicks--;
			}
    		
       		// Add current look dir to queue
    		mPrevLookDirs.add(player.getLookVec());
       		while (mPrevLookDirs.size() > mQueueLength) {
       			mPrevLookDirs.remove();
       		}
       		MovementInputFromOptionsOverride ownMovementInput = EyeGaze.ownMovementOverride;
       		
       		// Explanation of strategy:
       		// - when turning a corner, we want to slow down to make it a bit more manageable.
       		// - if it takes time to turn the auto-walk function off (e.g. using an eye gaze with dwell click) then
       		//   you don't want to continue walking. In this case you can opt to not walk on any ticks where the mouse
       		//   hasn't moved at all. This is mainly applicable to gaze input.
       		// - If walking into a wall, don't keep walking fast!
       		
            if (mDoingAutoWalk && 
            	null == Minecraft.getInstance().currentScreen &&	
                (mMoveWhenMouseStationary || MouseHandler.hasPendingEvent())) {

            	double forward = (double)mCustomSpeedFactor; 
            	
        		// Slow down when you're looking really far up/down, or turning round quickly
             	if (EyeMineConfig.slowdownOnCorners.get()) {
	            	double slowDownPitch = slowdownFactorPitch(player);
	            
	            	// Slow down when you've been turning a corner
	            	double slowDownCorners= slowdownFactorViewDirs();
//	            	System.out.println("slowdown corners: "+ slowDownCorners);
	            			            	
					if (!player.isOnLadder()) {
						forward *= Math.min(slowDownCorners, slowDownPitch);
					}
            	}
             	
             	// Don't go "forward" if looking down ladder - let it naturally go down 
             	// This may interfere in funny ways with the vanilla logic for whether or not you're on a ladder
             	// Consider it experimental (it's not turned on by default)
             	if (EyeMineConfig.allowLadderDescent.get() && player.isOnLadder()) {
    				// We're a bit more forgiving when player is on ground, to make sure player can exit the 
    				// ladder okay.
    				if ((player.isOnGround() && player.rotationPitch > 30) ||
    				    (!player.isOnGround() && player.rotationPitch > 0)) {
    	            	ownMovementInput.setWalkOverride(false, 0.0f);            	
    					return;
            		}
             	}
						            	
            	// Slow down if you're facing an animal/mob while attacking
				// (without this it's easy to run past)
            	if (EyeMineConfig.slowdownOnAttack.get()) {
            		if (ContinuouslyAttack.mIsAttacking) { 
            			forward *= slowdownFactorEntity(player);
            		}	
            	}	            											        	

            	// The built-in autojump doesn't work when you're underwater, so we do our own implementation here 
				if (player.isInWater()) {
					
					// Check the blocks around the player
			    	World world = Minecraft.getInstance().world;

					Vector3d posVec = player.getPositionVec();
					Vector3d forwardVec = player.getForward();

					//javax.vecmath.Vector2d is not available... let's use Vector2f and manually normalize the same way javax.vecmath.Vector2d did
					double norm = (double) (1.0/Math.sqrt(forwardVec.x*forwardVec.x + forwardVec.y*forwardVec.y));
					Vector2f forward2d = new Vector2f((float)(forwardVec.x * norm), (float)(forwardVec.y * norm));

					BlockPos blockInFrontPos = new BlockPos(
							posVec.x + forward2d.x,
							posVec.y , //y is UP
							posVec.z + forward2d.y);
					
					BlockPos blockInFrontAbovePos = blockInFrontPos.add(0, 1, 0);
												
			    	Material materialInFront = world.getBlockState(blockInFrontPos).getMaterial();
			    	Material materialAboveInFront = world.getBlockState(blockInFrontAbovePos).getMaterial();
			    			    	
		    		if ((materialInFront != null  && materialInFront.isSolid()) &&
		    			(materialAboveInFront != null  && !materialAboveInFront.isSolid()))
		    		{
		    			if (jumpTicks == 0) {
							channel.sendToServer(new JumpMessage(player.getName().toString()));
							player.jump();	
							
							// only jump every N ticks...
							jumpTicks = 20;
		    			}
		    		}
				}
				
				
				// If riding, we may need to do things differently
				if (player.isPassenger()) {

					Entity riddenEntity = player.getRidingEntity();

					if (null != riddenEntity) {
						if (riddenEntity instanceof BoatEntity) {
							// very special case: you can't steer a boat without keys,
							// so we first steer left/right with keys until the boat
							// and the player's view are aligned, only then move 
							// forward 
							BoatEntity boat = (BoatEntity)riddenEntity;
							if (boat.canPassengerSteer()) {								
								
								float yawError = boat.rotationYaw - player.rotationYaw;			
								yawError %= 360;
								if (yawError < -180) {
									yawError += 360;
								}
								if (yawError > 180) {
									yawError -= 360;
								}
								System.out.println(yawError);
								boatController.pid_step(boat, yawError);
							
								// downscale the forward motion if we've got lots of turning to do first
								float yawErrorAbs = Math.abs(yawError);
								float maxYawForward = EyeMineConfig.boatMaxTurnAtSpeed.get();
								if (yawErrorAbs > maxYawForward*0.2f) {
									forward *= (maxYawForward - yawErrorAbs)/maxYawForward;									
								}
								else if (yawErrorAbs > maxYawForward) {
									forward = 0.0;
								}
								
								// slower in general since boats are quite hard to control
								forward *= EyeMineConfig.boatSlowdown.get();  								
								
							}
						}
						else if (riddenEntity instanceof MinecartEntity) {
							Vector3d motion3d = player.getLookVec();
							Vector3d motionAligned = motion3d.mul(1.0, 0, 1.0);
							motionAligned.normalize();
							
							// Our movement override isn't enough to get cart moving
							// It's critical we add motion to player on the server, not just
							// locally
							player.setMotion(motionAligned);
							channel.sendToServer(new MovePlayerMessage(motionAligned));
						}
						else {
							// Any other ridden entities that don't work with the movement override??
						}
					}
				}
				else {
					boatController.releaseKeys();
				}
            	System.out.println(forward);
        		ownMovementInput.setWalkOverride(mDoingAutoWalk, (float) forward);
            	
			}
            else {
            	boatController.releaseKeys();
            	ownMovementInput.setWalkOverride(false, 0.0f);            	
            }
			
    	}
    }

	private double slowdownFactorPitch(PlayerEntity player) {
		float f = player.rotationPitch;
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

	private double slowdownFactorEntity(PlayerEntity player) {
		EntityRayTraceResult entityResult = ModUtils.getMouseOverEntity();
		if (entityResult != null) {
			Entity hitEntity = entityResult.getEntity();
			if (hitEntity instanceof LivingEntity) {
				LivingEntity liveEntity = (LivingEntity) hitEntity;
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
		Vector3d vectorSum = new Vector3d(0, 0, 0);

		// TODO: Sums could be done incrementally rather than looping over everything
		// each time.
		Iterator<Vector3d> iter = mPrevLookDirs.iterator();
		while (iter.hasNext()) {
			vectorSum = vectorSum.add(iter.next());
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
	private Queue<Vector3d> mPrevLookDirs;

	public static void stop() {
		if (mDoingAutoWalk) {
			mDoingAutoWalk = false;
			StateOverlay.setStateLeftIcon(mIconIndex, mDoingAutoWalk);
			
			// Make sure any overridden key bindings are removed
			final KeyBinding kbLeft = Minecraft.getInstance().gameSettings.keyBindLeft;
			final KeyBinding kbRight = Minecraft.getInstance().gameSettings.keyBindRight;
			KeyBinding.setKeyBindState(kbLeft.getKey(), false);
			KeyBinding.setKeyBindState(kbRight.getKey(), false);
		}
	}
	
	public static boolean isWalking() {
		return mDoingAutoWalk;
	}

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {
		
		if (ModUtils.hasActiveGui()) { return; }
		if (event.getAction() != GLFW.GLFW_PRESS) { return; }

	    if (KeyWatcher.f3Pressed) { return; }

		if (mToggleAutoWalkKB.getKey().getKeyCode() == event.getKey()) {			
			mDoingAutoWalk = !mDoingAutoWalk;
			MouseHandler.setWalking(mDoingAutoWalk);
			StateOverlay.setStateLeftIcon(mIconIndex, mDoingAutoWalk);
			if (!mDoingAutoWalk) {
				MoveWithGaze.stop();
			}
			ModUtils.sendPlayerMessage("Auto walk: " + (mDoingAutoWalk ? "ON" : "OFF"));
		}
		if (mDecreaseWalkSpeedKB.getKey().getKeyCode() == event.getKey()) {
			float newSpeed = (float) Math.max(0.1f, 0.9f * EyeMineConfig.customSpeedFactor.get());
			EyeGaze.saveWalkingSpeed(newSpeed);
			displayCurrentSpeed();
		}
		if (mIncreaseWalkSpeedKB.getKey().getKeyCode() == event.getKey()) {
			float newSpeed = (float) Math.min(2.0f, EyeMineConfig.customSpeedFactor.get() * 1.1f);
			EyeGaze.saveWalkingSpeed(newSpeed);
			displayCurrentSpeed();
		}
	}

	private void displayCurrentSpeed() {
		DecimalFormat myFormatter = new DecimalFormat("#0.00");
		String speedString = myFormatter.format(EyeMineConfig.customSpeedFactor.get());
		ModUtils.sendPlayerMessage("Walking speed: " + speedString);
	}
}
