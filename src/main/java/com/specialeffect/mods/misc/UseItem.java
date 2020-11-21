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

package com.specialeffect.mods.misc;

import org.lwjgl.glfw.GLFW;

import com.specialeffect.mods.EyeMineConfig;
import com.specialeffect.mods.utils.DwellAction;
import com.specialeffect.mods.utils.KeyWatcher;
import com.specialeffect.mods.utils.TargetBlock;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class UseItem 
extends DwellAction {
	
	public UseItem() {
		super("Use item");
	}

	public final String MODID = "useitem";

	public void setup(final FMLCommonSetupEvent event) {

		// Register key bindings
		mUseItemOnceKB = new KeyBinding("Use item", GLFW.GLFW_KEY_KP_0, CommonStrings.EYEGAZE_COMMON);
		ClientRegistry.registerKeyBinding(mUseItemOnceKB);

		mUseItemContinuouslyKB = new KeyBinding("Use item continuously", GLFW.GLFW_KEY_KP_1,
				CommonStrings.EYEGAZE_EXTRA);
		ClientRegistry.registerKeyBinding(mUseItemContinuouslyKB);

		mPrevItemKB = new KeyBinding("Select previous item", GLFW.GLFW_KEY_KP_4, CommonStrings.EYEGAZE_EXTRA);
		ClientRegistry.registerKeyBinding(mPrevItemKB);

		mNextItemKB = new KeyBinding("Select next item", GLFW.GLFW_KEY_KP_5, CommonStrings.EYEGAZE_EXTRA);
		ClientRegistry.registerKeyBinding(mNextItemKB);

		this.syncConfig();		
	}

	private static KeyBinding mUseItemOnceKB;
	private static KeyBinding mUseItemContinuouslyKB;
	private static KeyBinding mPrevItemKB;
	private static KeyBinding mNextItemKB;
	
	// State for 'continuously use'
	private boolean mUsingItem = false;
	
	// State for firing bows
	private long lastTime = 0;
	private int bowTime = 2000; //ms
	private int bowCountdown = 0;
	private boolean needBowFire = false;	

	public void syncConfig() {
		super.syncConfig();
        this.bowTime = (int) (1000*EyeMineConfig.bowDrawTime.get());        
	}
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {
		super.onClientTick(event);
		if (event.phase == Phase.START) {
			long time = System.currentTimeMillis();
			long dt = time - this.lastTime;
			this.lastTime = time;			
			
			// Behaviour for shootable items (bows)
			if (this.bowCountdown > 0 ) {
				this.bowCountdown -= dt;
				if (this.bowCountdown < 1) {
					// Release bow if count-down complete
					final KeyBinding useItemKeyBinding = Minecraft.getInstance().gameSettings.keyBindUseItem;
					KeyBinding.setKeyBindState(useItemKeyBinding.getKey(), false);
					
					// If it was a crossbow we'll need to re-click to actually fire it
					PlayerEntity player = Minecraft.getInstance().player;					
					Item item = player.inventory.getCurrentItem().getItem();
					if (item instanceof CrossbowItem) {
						// Crossbows don't fire on mouse-release, they need another 'click' on the next tick to be shot
						needBowFire = true;
					}
				}
			}
			else {
				if (needBowFire) {
					final KeyBinding useItemKeyBinding = Minecraft.getInstance().gameSettings.keyBindUseItem;
					KeyBinding.onTick(useItemKeyBinding.getKey());
					needBowFire = false;
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {
		
		if (ModUtils.hasActiveGui()) { return; }	    
	    if (event.getAction() != GLFW.GLFW_PRESS) { return; }

	    if (KeyWatcher.f3Pressed) { return; }

		final KeyBinding useItemKeyBinding = Minecraft.getInstance().gameSettings.keyBindUseItem;
		PlayerEntity player = Minecraft.getInstance().player;
		
		if (event.getKey() == mUseItemContinuouslyKB.getKey().getKeyCode()) {
			
			if (mUsingItem) {
				// Turn off
				mUsingItem = false;
		        ModUtils.sendPlayerMessage("Using item: OFF");
				KeyBinding.setKeyBindState(useItemKeyBinding.getKey(), mUsingItem);
			}
			else {
				// Turn on continuous-building
						
				ItemStack itemStack = player.inventory.getCurrentItem();
				if (itemStack == null || itemStack.getItem() == null) {
			        player.sendMessage(new StringTextComponent("Nothing in hand to use"));
			        return;
				}

				mUsingItem = true;				
				KeyBinding.setKeyBindState(useItemKeyBinding.getKey(), mUsingItem);

		        ModUtils.sendPlayerMessage("Using item: ON");
			}
		} else if (mUseItemOnceKB.getKey().getKeyCode() == event.getKey()) {
			
			ItemStack stack = player.inventory.getCurrentItem();
			Item item = stack.getItem();
			
			// Special case for shootable items			
			if (item instanceof CrossbowItem) {
				// Crossbows need charging separately to firing. If already charged, shoot it. 
				// Otherwise start chargin. 				
				if (CrossbowItem.isCharged(stack)) {
					KeyBinding.onTick(useItemKeyBinding.getKey());					
				}						
				else {
					int crossbowTime = 1500;
					ModUtils.sendPlayerMessage("Firing bow");
					KeyBinding.setKeyBindState(useItemKeyBinding.getKey(), true);
					this.bowCountdown = Math.max(crossbowTime, this.bowTime);
				}
			}
			else if (item instanceof BowItem) {
				// Bows need charging + firing all in one go			
				this.bowTime = 1500;
				ModUtils.sendPlayerMessage("Firing bow");
				KeyBinding.setKeyBindState(useItemKeyBinding.getKey(), true);
				this.bowCountdown = this.bowTime;							
			}
			else {
				boolean useDwelling = false;
				if (useDwelling)
					this.dwellOnce();
				else
					this.performAction(null);
			}			
			
		} else if (mPrevItemKB.getKey().getKeyCode() == event.getKey()) {
			player.inventory.changeCurrentItem(1);
		} else if (mNextItemKB.getKey().getKeyCode() == event.getKey()) {
			player.inventory.changeCurrentItem(-1);
		}
	}
	
	@SubscribeEvent
	public void onRenderGameOverlayEvent(final RenderGameOverlayEvent.Post event) {
		super.onRenderGameOverlayEvent(event);
		
		if(event.getType() != ElementType.EXPERIENCE)
		{      
			return;
		}
		
		// If use-item is on, show a warning message
		if (mUsingItem) {
			String msg = "USING";
			Minecraft mc = Minecraft.getInstance();
			int w = mc.mainWindow.getScaledWidth();
			int h = mc.mainWindow.getScaledHeight();
						
			int msgWidth = mc.fontRenderer.getStringWidth(msg);
		    
		    mc.fontRenderer.drawStringWithShadow(msg, w/2 - msgWidth/2, h/2 - 20, 0xffFFFFFF);		    		    
		}
		
	}

	@Override
	public void performAction(TargetBlock block) {
		final KeyBinding useItemKeyBinding = Minecraft.getInstance().gameSettings.keyBindUseItem;
		KeyBinding.onTick(useItemKeyBinding.getKey());			
	}
}
