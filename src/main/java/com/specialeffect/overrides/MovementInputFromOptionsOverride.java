package com.specialeffect.overrides;

import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.MovementInputFromOptions;

// Copied from net.minecraft.util.MovementInputFromOptions, changes to sneak only
public class MovementInputFromOptionsOverride extends MovementInputFromOptions
{
    private final GameSettings gameSettings;
    private AtomicBoolean mSneakOverride =  new AtomicBoolean(false);
    
    public MovementInputFromOptionsOverride(GameSettings gameSettingsIn)
    {
    	super(gameSettingsIn);
        this.gameSettings = gameSettingsIn;
    }
    
    public void setSneakOverride(boolean b) {
    	mSneakOverride.set(b);
    }

    public void updatePlayerMoveState()
    {
        this.moveStrafe = 0.0F;
        this.moveForward = 0.0F;

        if (this.gameSettings.keyBindForward.isKeyDown())
        {
            ++this.moveForward;
            this.forwardKeyDown = true;
        }
        else
        {
            this.forwardKeyDown = false;
        }

        if (this.gameSettings.keyBindBack.isKeyDown())
        {
            --this.moveForward;
            this.backKeyDown = true;
        }
        else
        {
            this.backKeyDown = false;
        }

        if (this.gameSettings.keyBindLeft.isKeyDown())
        {
            ++this.moveStrafe;
            this.leftKeyDown = true;
        }
        else
        {
            this.leftKeyDown = false;
        }

        if (this.gameSettings.keyBindRight.isKeyDown())
        {
            --this.moveStrafe;
            this.rightKeyDown = true;
        }
        else
        {
            this.rightKeyDown = false;
        }

        this.jump = this.gameSettings.keyBindJump.isKeyDown();
        this.sneak = this.gameSettings.keyBindSneak.isKeyDown() || mSneakOverride.get();

        if (this.sneak)
        {
            this.moveStrafe = (float)((double)this.moveStrafe * 0.3D);
            this.moveForward = (float)((double)this.moveForward * 0.3D);
        }
    }
}