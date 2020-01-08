package com.specialeffect.overrides;

import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.client.GameSettings;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

//Copied from net.minecraft.util.MovementInputFromOptions, changes to sneak only
@OnlyIn(Dist.CLIENT)
public class MovementInputFromOptionsOverride extends MovementInputFromOptions {
   
   private final GameSettings gameSettings; //shadows parent private var
   private AtomicBoolean mSneakOverride =  new AtomicBoolean(false);
      
   public MovementInputFromOptionsOverride(GameSettings gameSettingsIn) {
	   super(gameSettingsIn);
	   gameSettings = gameSettingsIn;	   
   }
         
   public void setSneakOverride(boolean b) {
   	 mSneakOverride.set(b);
   }

   public void tick(boolean slow, boolean noDampening) {
      this.forwardKeyDown = this.gameSettings.keyBindForward.isKeyDown();
      this.backKeyDown = this.gameSettings.keyBindBack.isKeyDown();
      this.leftKeyDown = this.gameSettings.keyBindLeft.isKeyDown();
      this.rightKeyDown = this.gameSettings.keyBindRight.isKeyDown();
      this.moveForward = this.forwardKeyDown == this.backKeyDown ? 0.0F : (float)(this.forwardKeyDown ? 1 : -1);
      this.moveStrafe = this.leftKeyDown == this.rightKeyDown ? 0.0F : (float)(this.leftKeyDown ? 1 : -1);
      this.jump = this.gameSettings.keyBindJump.isKeyDown();
      this.sneak = this.gameSettings.keyBindSneak.isKeyDown() || this.mSneakOverride.get();
      if (!noDampening && (this.sneak || slow)) {
         this.moveStrafe = (float)((double)this.moveStrafe * 0.3D);
         this.moveForward = (float)((double)this.moveForward * 0.3D);
      }
   }
}