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

package com.specialeffect.overrides;

import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.client.GameSettings;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

//Copied from net.minecraft.util.MovementInputFromOptions, + changes to override some things
@OnlyIn(Dist.CLIENT)
public class MovementInputFromOptionsOverride extends MovementInputFromOptions {
   
   private final GameSettings gameSettings; //shadows parent private var
   private AtomicBoolean mSneakOverride =  new AtomicBoolean(false);
   private AtomicBoolean mWalkForwardOverride =  new AtomicBoolean(false);
   private float mOverrideWalkSpeed = 1.0f; 
   
   public MovementInputFromOptionsOverride(GameSettings gameSettingsIn) {
	   super(gameSettingsIn);
	   gameSettings = gameSettingsIn;	   
   }
         
   public void setSneakOverride(boolean b) {
   	 mSneakOverride.set(b);
   }
   
   public void setWalkOverride(boolean b, float walkSpeed) {
	   mWalkForwardOverride.set(b);
	   mOverrideWalkSpeed = walkSpeed; // TODO: is concurrency an issue?
   }
   
   public void tick(boolean slow, boolean noDampening) {
      this.forwardKeyDown = this.gameSettings.keyBindForward.isKeyDown() || this.mWalkForwardOverride.get();
      this.backKeyDown = this.gameSettings.keyBindBack.isKeyDown();
      this.leftKeyDown = this.gameSettings.keyBindLeft.isKeyDown();
      this.rightKeyDown = this.gameSettings.keyBindRight.isKeyDown();
      this.moveForward = this.forwardKeyDown == this.backKeyDown ? 0.0F : (float)(this.forwardKeyDown ? 1 : -1);
      if (this.mWalkForwardOverride.get()) {
    	  this.moveForward = this.mOverrideWalkSpeed;
    	  // TODO: do we still want as drastic a sneak-slowing-down with eyemine??
      }
      this.moveStrafe = this.leftKeyDown == this.rightKeyDown ? 0.0F : (float)(this.leftKeyDown ? 1 : -1);
      this.jump = this.gameSettings.keyBindJump.isKeyDown();
      this.sneak = this.gameSettings.keyBindSneak.isKeyDown() || this.mSneakOverride.get();
      if (!noDampening && (this.sneak || slow)) {
         this.moveStrafe = (float)((double)this.moveStrafe * 0.3D);
         this.moveForward = (float)((double)this.moveForward * 0.3D);
      }
   }
}