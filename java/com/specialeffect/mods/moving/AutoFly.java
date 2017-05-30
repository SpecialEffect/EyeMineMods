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

import org.lwjgl.input.Keyboard;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
import com.specialeffect.gui.StateOverlay;
import com.specialeffect.messages.ChangeFlyingStateMessage;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.ModUtils;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import scala.swing.TextComponent;

@Mod(modid = AutoFly.MODID, version = ModUtils.VERSION, name = AutoFly.NAME)
public class AutoFly 
extends BaseClassWithCallbacks
implements ChildModWithConfig 
{

	public static final String MODID = "specialeffect.autofly";
	public static final String NAME = "AutoFly";

	private static KeyBinding mFlyManualKB;
	private static KeyBinding mFlyAutoKB;
	private static KeyBinding mFlyUpKB;

	private static int mFlyHeightManual = 2;
	private static int mFlyHeightAuto = 6;
	public static SimpleNetworkWrapper network;

	private static int mIconIndexAuto;
	private static int mIconIndexManual;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);

		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Add key binding to start/stop flying, and automatically fly over hills.");
		ModUtils.setAsParent(event, SpecialEffectMovements.MODID);

		network = NetworkRegistry.INSTANCE.newSimpleChannel(this.NAME);
		network.registerMessage(ChangeFlyingStateMessage.Handler.class, ChangeFlyingStateMessage.class, 1, Side.SERVER);

	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Subscribe to event buses
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
		
		// Subscribe to parent's config changes
    	SpecialEffectMovements.registerForConfigUpdates((ChildModWithConfig) this);
    	
		// Register key bindings
		mFlyManualKB = new KeyBinding("Fly (manual)", Keyboard.KEY_F, "SpecialEffect");
		mFlyAutoKB = new KeyBinding("Fly (auto)", Keyboard.KEY_G, "SpecialEffect");
		mFlyUpKB = new KeyBinding("Fly higher", Keyboard.KEY_UP, "SpecialEffect");
		ClientRegistry.registerKeyBinding(mFlyManualKB);
		ClientRegistry.registerKeyBinding(mFlyAutoKB);
		ClientRegistry.registerKeyBinding(mFlyUpKB);

		// Register an icon for the overlay
		mIconIndexAuto = StateOverlay.registerTextureLeft("specialeffect:icons/fly-auto.png");
		mIconIndexManual = StateOverlay.registerTextureLeft("specialeffect:icons/fly.png");
	}

	public void syncConfig() {
		mFlyHeightManual = SpecialEffectMovements.flyHeightManual;
		mFlyHeightAuto = SpecialEffectMovements.flyHeightAuto;
	}

	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (ModUtils.entityIsMe(event.getEntityLiving())) {
			EntityPlayer player = (EntityPlayer) event.getEntityLiving();
			
			// If auto flying, and about to bump into something, fly more!
			if (mIsFlyingAuto && player.capabilities.allowFlying && player.capabilities.isFlying) {
				BlockPos playerPos = player.getPosition();
				Vec3d lookVec = player.getLookVec();

				// Check all three blocks ahead of player
				for (int yDiff = -1; yDiff < 2; yDiff++) {
					BlockPos blockPosInFrontOfPlayer = new BlockPos(playerPos.getX() + lookVec.xCoord,
							playerPos.getY() + yDiff, playerPos.getZ() + lookVec.zCoord);

					World world = Minecraft.getMinecraft().world;
					Block block = world.getBlockState(blockPosInFrontOfPlayer).getBlock();

					// If there's a block in your way, and you're not already jumping over it...
					if (world.getBlockState(blockPosInFrontOfPlayer).getBlock().getMaterial().blocksMovement() &&
							player.motionY == 0) {
						player.motionY += Math.max(mFlyHeightAuto / 4, 1);
						break; // for yDiff = ...
					}
				}
			}

			// Check flying wasn't forcefully stopped from elsewhere
			if ((mIsFlyingAuto || mIsFlyingManual) &&
					!player.capabilities.isFlying) {
				updateAfterStopFlying();
			}		
			// If flying was turned on elsewhere, make it 'manual'
			if (!mIsFlyingAuto && !mIsFlyingManual &&
					player.capabilities.isFlying) {
				mIsFlyingManual = true;
				updateIcons();
			}

			this.processQueuedCallbacks(event);
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
		queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
			@Override
			public void onLiving(LivingUpdateEvent event) {
//				mIsFlyingAuto = false;
//				mIsFlyingManual = false;
				EntityPlayer player = (EntityPlayer) event.getEntityLiving();
				player.capabilities.isFlying = false;
				AutoFly.network.sendToServer(new ChangeFlyingStateMessage(false, 0));
//				updateIcons();
			}
		}));	
	}	
	
	private void setFlying(final boolean bFlyUp, final boolean isAuto) {
		this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
			@Override
			public void onLiving(LivingUpdateEvent event) {
				mIsFlyingAuto = isAuto;
				mIsFlyingManual = !isAuto;
				
				EntityPlayer player = (EntityPlayer) event.getEntityLiving();
				if (player.capabilities.allowFlying) {
					// stop sneaking (if we are), which prevents flying
					Sneak.stop();

					// start flying
					player.capabilities.isFlying = true;
					int flyHeight = 0;
					if (bFlyUp) {
						if (mIsFlyingAuto) { flyHeight = mFlyHeightAuto; }
						if (mIsFlyingManual) { flyHeight = mFlyHeightManual; }
					}
					//TODO: MoverType SELF or PLAYER?
					player.move(MoverType.SELF, 0, flyHeight, 0);
					AutoFly.network.sendToServer(new ChangeFlyingStateMessage(true, flyHeight));
				}
				else {
					player.sendMessage(new TextComponentString(
							"Player unable to fly"));
				}
				updateIcons();
			}
		}));
	}

	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		if (mFlyManualKB.isPressed()) {			
			if (mIsFlyingManual) {
				this.queueChatMessage("Fly manual: OFF");
				this.stopFlying();
			}
			else {
				this.queueChatMessage("Fly manual: ON");
				boolean doFlyUp = !mIsFlyingAuto;
				this.setFlying(doFlyUp, false);
			}
			
		} else if (mFlyAutoKB.isPressed()) {
			if (mIsFlyingAuto) {
				this.queueChatMessage("Fly auto: OFF");
				this.stopFlying();
			}
			else {
				this.queueChatMessage("Auto-fly: ON");
				boolean doFlyUp = !mIsFlyingManual;
				this.setFlying(doFlyUp, true);
			}
		}
		else if (mFlyUpKB.isPressed()) {
			this.setFlying(true, mIsFlyingAuto);
		}
		this.updateIcons();
	}

}
