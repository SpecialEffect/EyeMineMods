/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.mods.mining;

import org.lwjgl.glfw.GLFW;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
//import com.specialeffect.messages.AddItemToHotbar;
import com.specialeffect.mods.EyeGaze;
import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.java.games.input.Keyboard;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;


@Mod(MineOne.MODID)
public class MineOne 
extends BaseClassWithCallbacks 
implements ChildModWithConfig
{
	public static final String MODID = "autodestroy";
	public static final String NAME = "AutoDestroy";
	//FIXME for 1.14 public static SimpleNetworkWrapper network;
	

	private boolean mDestroying = false;
	private BlockPos mBlockToDestroy;
	private static KeyBinding mDestroyKB;
	
	// Mine one doesn't now respect the user config for auto-selecting pickaxe, since
	// it's typically used when building and you don't want to have to keep switching
	// back to your building material. 
	private boolean mAutoSelectTool = false;
	private boolean mWaitingForPickaxe = false;
	
	public MineOne() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
	}
	
	private void setup(final FMLCommonSetupEvent event) {
		
		// preinit
		MinecraftForge.EVENT_BUS.register(this);

		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Add key binding to start/stop continuously attacking.");
		ModUtils.setAsParent(event, EyeGaze.MODID);

		//FIXME network = NetworkRegistry.INSTANCE.newSimpleChannel(this.NAME);
		//FIXME network.registerMessage(AddItemToHotbar.Handler.class, AddItemToHotbar.class, 0, Side.SERVER);
		
		// Register for config changes from parent
		EyeGaze.registerForConfigUpdates((ChildModWithConfig)this);
		
		// Register key bindings	
		mDestroyKB = new KeyBinding("Mine one block", GLFW.GLFW_KEY_N, CommonStrings.EYEGAZE_COMMON);
		ClientRegistry.registerKeyBinding(mDestroyKB);
				
	}
		
	public void syncConfig() {		
	}
	
	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (ModUtils.entityIsMe(event.getEntityLiving())) {
			this.processQueuedCallbacks(event);

			if (mDestroying) {

				// Select the best tool from the inventory
				World world = Minecraft.getInstance().world;
	    		PlayerEntity player = (PlayerEntity)event.getEntityLiving();
	    		if (player.isCreative() && 
						mAutoSelectTool) {
	    			boolean havePickaxe = choosePickaxe(player.inventory);
	    			if (havePickaxe) {
	    				mWaitingForPickaxe = false;
	    			}
	    			else if(!mWaitingForPickaxe) 
	    			{
	    				requestCreatePickaxe();
		    			mWaitingForPickaxe = true;		    		
	    			}
	    		}
	    		else {			
	    			
	    			// Swords can't destroy blocks: warn user
	    			if (player.getHeldItemMainhand().getItem() instanceof SwordItem) {
	    				String message = "Can't destroy blocks with a sword, please select another item";
				        player.sendMessage(new StringTextComponent(message));
				        
	    				this.stopDestroying();
	    				return;
	    			}
	    		}
				
				// Stop attacking if we're not pointing at the block any more
				// (which means either we've destroyed it, or moved away)
				RayTraceResult mov = Minecraft.getInstance().objectMouseOver;
				Block block = world.getBlockState(mBlockToDestroy).getBlock();
				boolean blockDestroyed = (world.getBlockState(mBlockToDestroy).getBlock() instanceof AirBlock);
				boolean movedAway =  false;		
				BlockPos pos = this.getMouseOverBlockPos();
				if (pos != null) {														
					movedAway = mBlockToDestroy.distanceSq((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), false) > 0.5;					
				}
				
				if (mov == null || blockDestroyed || movedAway) {
					if (movedAway) {
						this.stopDestroying();
					}
					if (blockDestroyed) {
						this.stopDestroying();
					}
					if (mov == null) {
						this.stopDestroying();
					}
				}
			}
		}
	}
	
	private void startDestroying() {
		mDestroying = true;
		
		final KeyBinding attackBinding = 
				Minecraft.getInstance().gameSettings.keyBindAttack;
		KeyBinding.setKeyBindState(attackBinding.getKey(), true);	

	}
	
	private void stopDestroying() {
		final KeyBinding attackBinding = 
				Minecraft.getInstance().gameSettings.keyBindAttack;
		KeyBinding.setKeyBindState(attackBinding.getKey(), false);
		mDestroying = false;
	}
	
	// Return the position of the block that the mouse is pointing at.
	// May be null, if pointing at something other than a block.
	private BlockPos getMouseOverBlockPos() {
		BlockPos pos = null;		
		BlockRayTraceResult rayTraceBlock = (BlockRayTraceResult)Minecraft.getInstance().objectMouseOver;
		if (rayTraceBlock != null) {			
			pos = rayTraceBlock.getPos();			
		}
		return pos;
	}
	
	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {          	
		if(mDestroyKB.isPressed()) {
			// turn off continuous mining
			ContinuouslyMine.stop();
			
			// mine the block you're facing
			mBlockToDestroy = this.getMouseOverBlockPos();
			if (mBlockToDestroy == null) {
				System.out.println("Nothing to attack");
				return;
			}
			else {
				this.startDestroying();
			}
		}
	}
	
	// returns true if successful
	static boolean choosePickaxe(PlayerInventory inventory) {
		
		// In creative mode, we can either select a pickaxe from the hotbar 
		// or just rustle up a new one		
		if (inventory.getCurrentItem().getItem() instanceof PickaxeItem)
		{
			return true;
		}
		else
		{
			int pickaxeId = ModUtils.findItemInHotbar(inventory, PickaxeItem.class);
			if (pickaxeId > -1) {
				inventory.currentItem = pickaxeId;
				return true;
			}
			else {
				return false;
			}
		}		
	}
	
	static void requestCreatePickaxe() {
		// Ask server to put new item in hotbar
		//FIXME MineOne.network.sendToServer(new AddItemToHotbar(new ItemStack(Items.DIAMOND_PICKAXE)));
	}	
}
