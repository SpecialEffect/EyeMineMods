/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.mods.moving;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.lwjgl.glfw.GLFW;

import com.specialeffect.gui.JoystickControlOverlay;
import com.specialeffect.gui.StateOverlay;
import com.specialeffect.mods.ChildMod;
import com.specialeffect.mods.EyeGaze;
import com.specialeffect.mods.EyeMineConfig;
import com.specialeffect.mods.mousehandling.MouseHandler;
import com.specialeffect.mods.mousehandling.MouseHelperOwn;
import com.specialeffect.overrides.MovementInputFromOptionsOverride;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class MoveWithGaze2 
extends ChildMod
implements ChildModWithConfig
{
	public static final String MODID = "specialeffect.movewithgaze2";
    public static final String NAME = "MoveWithGaze2";

    private static KeyBinding mToggleAutoWalkKB;
    
    private static boolean mMoveWhenMouseStationary = false;
    private static float mCustomSpeedFactor = 0.8f;

	public void setup(final FMLCommonSetupEvent event) {
    	mOverlay = new JoystickControlOverlay(Minecraft.getInstance());
		MinecraftForge.EVENT_BUS.register(mOverlay);

    	// Register key bindings	
    	mToggleAutoWalkKB = new KeyBinding("Start/stop walking (simple mode)", GLFW.GLFW_KEY_B, CommonStrings.EYEGAZE_COMMON);
        ClientRegistry.registerKeyBinding(mToggleAutoWalkKB);
        
		// Register an icon for the overlay
		mIconIndex = StateOverlay.registerTextureLeft("specialeffect:icons/legacy-mode.png");
    }
    
    private static int mIconIndex;
    
	private static JoystickControlOverlay mOverlay;

	public static void stop() {
		if (mDoingAutoWalk) {
			mDoingAutoWalk = false;
			StateOverlay.setStateLeftIcon(mIconIndex, false);        	
    		mOverlay.setVisible(false);
		}
    }
	
	public void syncConfig() {
        mMoveWhenMouseStationary = EyeMineConfig.moveWhenMouseStationary.get();
        mCustomSpeedFactor = EyeMineConfig.customSpeedFactor.get().floatValue();
	}
	
	// Some hard-coded fudge factors for maximums.
	// TODO: make configurable?
	private float mMaxForward = 1.5f;
	private float mMaxBackward = 0.5f;
	
	private int mTicksToLockOn=5; // wait this # of ticks before acting in a new region
    private int ticksForward=0; 
    private int ticksBackward=0; 
    
    @SubscribeEvent
    public void onLiving(LivingUpdateEvent event) {
    	if (ModUtils.entityIsMe(event.getEntityLiving())) {

    		if (mDoingAutoWalk && 
            		null == Minecraft.getInstance().currentScreen && // no gui visible
            		(mMoveWhenMouseStationary || MouseHandler.hasPendingEvent()) ) {
    			
    			MovementInputFromOptionsOverride ownMovementInput = EyeGaze.ownMovementOverride;
    			
    			MouseHelperOwn helper = (MouseHelperOwn)Minecraft.getInstance().mouseHelper;
    			double lastMouseY = helper.lastYVelocity;
    			
    			// Y gives distance to walk forward/back.
    			float walkForwardAmount = 0.0f;
    			float h = (float)Minecraft.getInstance().mainWindow.getScaledHeight();    			    		
    			float h6 = h/6.0f;
    			
    			if (lastMouseY < -h6) {
    				// top of screen: forward!
    				if (ticksForward > mTicksToLockOn) {    				
    					walkForwardAmount = (float) (mMaxForward*(-lastMouseY-h6)/h6);
    					ticksBackward = 0;
    				}
    				else {
    					ticksForward++;	    					
    				}
    			}
    			else if (lastMouseY > h6) {
    				// backward
    				
    				if (ticksBackward > mTicksToLockOn) {    				
    					// backward!
    					walkForwardAmount = (float) (-mMaxBackward*(lastMouseY - h6)/h6);
    					ticksForward = 0;
    				}
    				else {
    					ticksBackward++;
    				}
    			}
    			
    			// scaled by mCustomSpeedFactor 
    			walkForwardAmount *= 0.15;
    			walkForwardAmount *= mCustomSpeedFactor;
				ownMovementInput.setWalkOverride(true, walkForwardAmount);			

    		}
    	}
    }
    
	private static boolean mDoingAutoWalk = false;

    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {

        if (ModUtils.hasActiveGui()) { return; }        
        if (event.getAction() != GLFW.GLFW_PRESS) { return; }
        
        if(mToggleAutoWalkKB.isPressed()) {
        	mDoingAutoWalk = !mDoingAutoWalk;        	
        	MouseHandler.setLegacyWalking(mDoingAutoWalk);
        	
        	mOverlay.setVisible(mDoingAutoWalk);
			StateOverlay.setStateLeftIcon(mIconIndex, mDoingAutoWalk);
			ModUtils.sendPlayerMessage("Auto walk: " + (mDoingAutoWalk ? "ON" : "OFF"));
        }
    }
}