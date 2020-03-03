/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.mods.misc;

import java.awt.Color;

import org.lwjgl.glfw.GLFW;

import com.irtimaled.bbor.client.renderers.AbstractRenderer;

import com.specialeffect.mods.ChildMod;
import com.specialeffect.mods.mousehandling.MouseHandler;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class UseItem extends ChildMod {
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

	}

	private static KeyBinding mUseItemOnceKB;
	private static KeyBinding mUseItemContinuouslyKB;
	private static KeyBinding mPrevItemKB;
	private static KeyBinding mNextItemKB;
	
	// State for 'continuously use'
	private boolean mUsingItem = false;
	private int usingTimer = 0;
	private int usingCooldown = 10; //FIXME: put in user config
	private Vec3d lastLook = new Vec3d(1.0,0.0,0.0);
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {
		if (mUsingItem) {
//			System.out.println(usingTimer);
			// Timer to limit excessive construction  
			// TODO: should we use wallclock time instead of ticks?
			
			// Modulate according to how much player has looked around in the last
			// tick
			PlayerEntity player = Minecraft.getInstance().player;
			Vec3d look = player.getLookVec();			
			boolean lookMoved = lastLook.dotProduct(look) < 0.99; // ~ 8 degrees  

			if (usingTimer > 0 && !lookMoved) {
				usingTimer -= 1;
			}		
							
			// Set mouse in correct state - shouldn't build unless there's an
			// accompanying mouse movement.	
			if (MouseHandler.hasPendingEvent()) {		
				if (usingTimer == 0) {
					System.out.println("use!");
					final KeyBinding useItemKeyBinding = Minecraft.getInstance().gameSettings.keyBindUseItem;
					KeyBinding.onTick(useItemKeyBinding.getKey());				
					usingTimer = usingCooldown;				
				}			
			}
			this.lastLook = look;
		}
	}
	
	@SubscribeEvent
	public void onBlockOutlineRender(DrawBlockHighlightEvent e)
	{
		if (mUsingItem) {
			RayTraceResult raytraceResult = e.getTarget();			
			if(e.getSubID() == 0 && raytraceResult.getType() == RayTraceResult.Type.BLOCK)
			{
				BlockRayTraceResult rayTraceBlock = ModUtils.getMouseOverBlock();
				System.out.println(rayTraceBlock.getPos());

	            if (rayTraceBlock != null) {
	            	Direction faceHit = rayTraceBlock.getFace();
					int alpha = 10;
					Color color = new Color(0.75f, 0.25f, 0.0f);
					AbstractRenderer.renderBlockFace(rayTraceBlock.getPos(), faceHit, color, alpha);
	            }
			}
		}
	}
	
	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {
		if (ModUtils.hasActiveGui()) { return; }
		
		final KeyBinding useItemKeyBinding = Minecraft.getInstance().gameSettings.keyBindUseItem;
		PlayerEntity player = Minecraft.getInstance().player;
		
		if (mUseItemContinuouslyKB.isPressed()) {
			mUsingItem = !mUsingItem;
//			boolean useItemNewState = !useItemKeyBinding.isKeyDown();
//			KeyBinding.setKeyBindState(useItemKeyBinding.getKey(), useItemNewState);
			final String message = "Using item: " + (mUsingItem ? "ON" : "OFF");
	        player.sendMessage(new StringTextComponent(message));
	        
	        // Tune throttling according to what's in your hand
	        if (mUsingItem) {
	        	ItemStack itemStack = player.inventory.getCurrentItem();
		        Item item = player.inventory.getCurrentItem().getItem();
		        
				if (player.inventory.getCurrentItem().getItem() instanceof BlockItem) {
					System.out.println("block item!");
					usingCooldown = 30;
					usingTimer = 0;
					lastLook = player.getLookVec();
				}
				else {
					System.out.println("not a block ...!");
					usingCooldown = 2;
					usingTimer = 0;
					lastLook = player.getLookVec();
				}
	        }

//	        if (player.inventory.currentItem)
		} else if (mUseItemOnceKB.isPressed()) {
			KeyBinding.onTick(useItemKeyBinding.getKey());
		} else if (mPrevItemKB.isPressed()) {
			player.inventory.changeCurrentItem(1);
		} else if (mNextItemKB.isPressed()) {
			player.inventory.changeCurrentItem(-1);
		}
	}
	
	@SubscribeEvent
	public void onRenderGameOverlayEvent(final RenderGameOverlayEvent.Post event) {
		if(event.isCancelable() || event.getType() != ElementType.EXPERIENCE)
		{      
			return;
		}
		
		// If use-item is on, show a warning message
		if (mUsingItem) {
			Minecraft mc = Minecraft.getInstance();
			int w = mc.mainWindow.getScaledWidth();
			int h = mc.mainWindow.getScaledHeight();
						
			String msg = "USING";
			int msgWidth = mc.fontRenderer.getStringWidth(msg);
		    
		    mc.fontRenderer.drawStringWithShadow(msg, w/2 - msgWidth/2, h/2 - 20, 0xffFFFFFF);		    
		    
		}
		
	}
}
