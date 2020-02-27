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

import org.lwjgl.glfw.GLFW;

import com.specialeffect.mods.ChildMod;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings.Input;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class SwapMinePlace  extends ChildMod {
	public final String MODID = "swapmineplace";

	public void setup(final FMLCommonSetupEvent event) {
		// Register key bindings
		mSwapKB = new KeyBinding("Swap mine/place keys", GLFW.GLFW_KEY_F10, CommonStrings.EYEGAZE_EXTRA);
		ClientRegistry.registerKeyBinding(mSwapKB);		
	}

	private static KeyBinding mSwapKB;	

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {	
		if (ModUtils.hasActiveGui()) { return; }
		
		if (mSwapKB.isPressed()) {
			
			Input attackInput = Minecraft.getInstance().gameSettings.keyBindAttack.getKey();
			Input useInput = Minecraft.getInstance().gameSettings.keyBindUseItem.getKey();
			
			Minecraft.getInstance().gameSettings.setKeyBindingCode(Minecraft.getInstance().gameSettings.keyBindAttack, useInput);
			Minecraft.getInstance().gameSettings.setKeyBindingCode(Minecraft.getInstance().gameSettings.keyBindUseItem, attackInput);
			
			// It's important to force a reload
			Minecraft.getInstance().gameSettings.saveOptions();
			Minecraft.getInstance().gameSettings.loadOptions();
			
			ModUtils.sendPlayerMessage("Swapping mine and place keys");			
			
		}
	}
	
	// This event is called by GuiIngameForge during each frame by
	// GuiIngameForge.pre() and GuiIngameForce.post().
	@SubscribeEvent
	public void onRenderGameOverlayEvent(final RenderGameOverlayEvent.Post event) {

		// We draw after the ExperienceBar has drawn.  The event raised by GuiIngameForge.pre()
		// will return true from isCancelable.  If you call event.setCanceled(true) in
		// that case, the portion of rendering which this event represents will be canceled.
		// We want to draw *after* the experience bar is drawn, so we make sure isCancelable() returns
		// false and that the eventType represents the ExperienceBar event.
		if(event.isCancelable() || event.getType() != ElementType.EXPERIENCE)
		{      
			return;
		}
		
		// If these are swapped, show a warning message
		KeyBinding attackBinding = Minecraft.getInstance().gameSettings.keyBindAttack;
		KeyBinding useBinding = Minecraft.getInstance().gameSettings.keyBindUseItem;
		
		if (attackBinding.isDefault() || useBinding.isDefault()) {
			return;
		}		
		else {
			Input attackDefault = attackBinding.getDefault();
			Input useDefault = useBinding.getDefault();
		
			// if there's a straight-up swap, show message
			if (attackBinding.getKey().getKeyCode() == useDefault.getKeyCode() &&
					useBinding.getKey().getKeyCode() == attackDefault.getKeyCode()) {
				Minecraft mc = Minecraft.getInstance();
				int w = mc.mainWindow.getScaledWidth();
				int h = mc.mainWindow.getScaledHeight();
				
				
				String msg1 = "Mine / place";
				int msg1width = mc.fontRenderer.getStringWidth(msg1);
				String msg2 = "are swapped";
				int msg2width = mc.fontRenderer.getStringWidth(msg2);
				
				int delta = (msg1width - msg2width)/2;
			    
			    mc.fontRenderer.drawStringWithShadow(msg1, w - msg2width - delta - 10 , h - 22, 0xffFFFFFF);
			    mc.fontRenderer.drawStringWithShadow(msg2, w - msg2width - 10 , h - 12, 0xffFFFFFF);
			    
			}			
		}
		
	}
}
