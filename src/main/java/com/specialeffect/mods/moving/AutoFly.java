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

import org.lwjgl.glfw.GLFW;

import com.specialeffect.gui.StateOverlay;
import com.specialeffect.messages.ChangeFlyingStateMessage;
import com.specialeffect.mods.ChildMod;
import com.specialeffect.mods.EyeMineConfig;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class AutoFly 

extends ChildMod implements ChildModWithConfig
{

	public static final String MODID = "autofly";
	public static final String NAME = "AutoFly";
	private static final String PROTOCOL_VERSION = Integer.toString(1);

	private static KeyBinding mFlyManualKB;
	private static KeyBinding mFlyAutoKB;
	private static KeyBinding mFlyUpKB;

	private static int mFlyHeightManual = 2;
	private static int mFlyHeightAuto = 6;

	private static int mIconIndexAuto;
	private static int mIconIndexManual;
	
    public static SimpleChannel channel;
	
	public void setup(final FMLCommonSetupEvent event) {

		// setup channel for comms
		channel = NetworkRegistry.newSimpleChannel(
                new ResourceLocation("specialeffect","autofly")
                ,() -> PROTOCOL_VERSION
                , PROTOCOL_VERSION::equals
                , PROTOCOL_VERSION::equals);
        int id = 0;        
        channel.registerMessage(id++, ChangeFlyingStateMessage.class, ChangeFlyingStateMessage::encode, 
        		ChangeFlyingStateMessage::decode, ChangeFlyingStateMessage.Handler::handle);                   	       
		
		// Register key bindings
		mFlyManualKB = new KeyBinding("Start/stop flying (manual)", GLFW.GLFW_KEY_COMMA,CommonStrings.EYEGAZE_EXTRA);
		mFlyAutoKB = new KeyBinding("Start/stop flying (auto)", GLFW.GLFW_KEY_G, CommonStrings.EYEGAZE_COMMON);
		mFlyUpKB = new KeyBinding("Fly higher", GLFW.GLFW_KEY_PERIOD, CommonStrings.EYEGAZE_EXTRA);
		
		ClientRegistry.registerKeyBinding(mFlyManualKB);
		ClientRegistry.registerKeyBinding(mFlyAutoKB);
		ClientRegistry.registerKeyBinding(mFlyUpKB);

		// Register an icon for the overlay
		mIconIndexAuto = StateOverlay.registerTextureLeft("specialeffect:icons/fly-auto.png");
		mIconIndexManual = StateOverlay.registerTextureLeft("specialeffect:icons/fly.png");
	}

	public void syncConfig() {
		mFlyHeightManual = EyeMineConfig.flyHeightManual.get();
		mFlyHeightAuto = EyeMineConfig.flyHeightAuto.get();
	}

	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {
    PlayerEntity player = Minecraft.getInstance().player;
    	if (null != player && event.phase == TickEvent.Phase.START) {
			// If auto flying, and about to bump into something, fly more!
			if (mIsFlyingAuto && player.abilities.allowFlying && player.abilities.isFlying) {
				BlockPos playerPos = player.getPosition();
				Vec3d lookVec = player.getLookVec();

				// Check all three blocks ahead of player
				for (int yDiff = -1; yDiff < 2; yDiff++) {
					BlockPos blockPosInFrontOfPlayer = new BlockPos(playerPos.getX() + lookVec.x,
							playerPos.getY() + yDiff, playerPos.getZ() + lookVec.z);

					World world = Minecraft.getInstance().world;

									
					// If there's a block in your way, and you're not already jumping over it...
					
					Vec3d motion = player.getMotion();
					Vec3d addMotion = new Vec3d(0.0, Math.max(mFlyHeightAuto / 4, 1), 0.0);
					if (world.getBlockState(blockPosInFrontOfPlayer).getMaterial().blocksMovement() &&
							motion.y == 0) {
	    				player.setMotion(motion.add(addMotion));
						break;
					}
				}
			}

			/* FIXME
			// Check flying wasn't forcefully stopped from elsewhere
			if ((mIsFlyingAuto || mIsFlyingManual) &&
					!player.abilities.isFlying) {
				updateAfterStopFlying();
			}		
			// If flying was turned on elsewhere, make it 'manual'
			if (!mIsFlyingAuto && !mIsFlyingManual &&
					player.abilities.isFlying) {
				mIsFlyingManual = true;
				updateIcons();
			}*/
		}
	}
	
	private static boolean mIsFlyingManual = false;
	private static boolean mIsFlyingAuto = false;
	
	private static void updateIcons() {
		StateOverlay.setStateLeftIcon(mIconIndexAuto, mIsFlyingAuto);
		StateOverlay.setStateLeftIcon(mIconIndexManual, mIsFlyingManual);
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
		
		PlayerEntity player = Minecraft.getInstance().player;

		player.abilities.isFlying = false;
		channel.sendToServer(new ChangeFlyingStateMessage(false, 0));
		updateIcons();
	}	
	
	private void setFlying(final boolean bFlyUp, final boolean isAuto) {
		
		PlayerEntity player = Minecraft.getInstance().player;
		
		mIsFlyingAuto = isAuto;
		mIsFlyingManual = !isAuto;
				
		if (!player.abilities.allowFlying) {
			player.sendMessage(new StringTextComponent(
					"Player unable to fly"));
			return;
		}		
	
		// stop sneaking (if we are), which prevents flying
		//FIXME Sneak.stop();

		// start flying
		player.abilities.isFlying = true;
		int flyHeight = 0;
		if (bFlyUp) {
			if (mIsFlyingAuto) { flyHeight = mFlyHeightAuto; }
			if (mIsFlyingManual) { flyHeight = mFlyHeightManual; }
			
			player.move(MoverType.SELF, new Vec3d(0, flyHeight, 0));
		}
		
		channel.sendToServer(new ChangeFlyingStateMessage(true, flyHeight));

		updateIcons();

	}

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {
		if (ModUtils.hasActiveGui()) { return; }
		
		if (mFlyManualKB.isPressed()) {			
			if (mIsFlyingManual) {
				ModUtils.sendPlayerMessage("Fly manual: OFF");
				this.stopFlying();
			}
			else {
				ModUtils.sendPlayerMessage("Fly manual: ON");
				boolean doFlyUp = !mIsFlyingAuto;
				this.setFlying(doFlyUp, false);
			}			
		} else if (mFlyAutoKB.isPressed()) {
			if (mIsFlyingAuto) {
				ModUtils.sendPlayerMessage("Fly auto: OFF");
				this.stopFlying();
			}
			else {
				ModUtils.sendPlayerMessage("Fly auto: ON");
				boolean doFlyUp = !mIsFlyingManual;
				this.setFlying(doFlyUp, true);
			}
		}
		else if (mFlyUpKB.isPressed()) {
			this.setFlying(true, mIsFlyingAuto);
		}
		AutoFly.updateIcons();
	}

}
