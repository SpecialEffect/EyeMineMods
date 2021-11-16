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

package com.specialeffect.eyemine.submod.misc;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.platform.InputConstants.Type;
import com.mojang.blaze3d.vertex.PoseStack;
import com.specialeffect.eyemine.client.Keybindings;
import com.specialeffect.eyemine.mixin.KeyMappingAccessor;
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.utils.ModUtils;
import me.shedaniel.architectury.event.events.GuiEvent;
import me.shedaniel.architectury.event.events.client.ClientRawInputEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import org.lwjgl.glfw.GLFW;

public class SwapMinePlace extends SubMod {
	public void onInitializeClient() {
		// Register key bindings
		Keybindings.keybindings.add(mSwapKB = new KeyMapping(
				"key.eyemine.swap_mine_place",
				Type.KEYSYM,
				GLFW.GLFW_KEY_F10,
				"category.eyemine.category.eyegaze_extra" // The translation key of the keybinding's category.
		));

		ClientRawInputEvent.KEY_PRESSED.register(this::onKeyInput);
		GuiEvent.RENDER_HUD.register(this::onRenderGameOverlayEvent);
	}

	private static KeyMapping mSwapKB;

	private InteractionResult onKeyInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
		if (ModUtils.hasActiveGui()) { return InteractionResult.PASS; }

		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 292)) { return InteractionResult.PASS; }

		if (mSwapKB.matches(keyCode, scanCode) && mSwapKB.consumeClick()) {
			
			Key attackInput = ((KeyMappingAccessor)Minecraft.getInstance().options.keyAttack).getActualKey();
			Key useInput = ((KeyMappingAccessor)Minecraft.getInstance().options.keyUse).getActualKey();
			
			Minecraft.getInstance().options.setKey(Minecraft.getInstance().options.keyAttack, useInput);
			Minecraft.getInstance().options.setKey(Minecraft.getInstance().options.keyUse, attackInput);
			
			// It's important to force a reload
			Minecraft.getInstance().options.save();
			Minecraft.getInstance().options.load();
			
			ModUtils.sendPlayerMessage("Swapping mine and place keys");			
			
		}
		return InteractionResult.PASS;
	}

	public void onRenderGameOverlayEvent(PoseStack poseStack, float partialTicks) {
		// If these are swapped, show a warning message
		KeyMapping attackBinding = Minecraft.getInstance().options.keyAttack;
		KeyMapping useBinding = Minecraft.getInstance().options.keyUse;
		
		if (attackBinding.isDefault() || useBinding.isDefault()) {
			return;
		}		
		else {
			Key attackDefault = attackBinding.getDefaultKey();
			Key useDefault = useBinding.getDefaultKey();
		
			// if there's a straight-up swap, show message
			if (((KeyMappingAccessor)attackBinding).getActualKey().getValue() == useDefault.getValue() &&
					((KeyMappingAccessor)useBinding).getActualKey().getValue() == attackDefault.getValue()) {
				Minecraft mc = Minecraft.getInstance();
				int w = mc.getWindow().getGuiScaledWidth();
				int h = mc.getWindow().getGuiScaledHeight();
				PoseStack matrixStack = poseStack;
				
				
				String msg1 = "Mine / place";
				int msg1width = mc.font.width(msg1);
				String msg2 = "are swapped";
				int msg2width = mc.font.width(msg2);
				
				int delta = (msg1width - msg2width)/2;
			    
			    mc.font.drawShadow(matrixStack, msg1, w - msg2width - delta - 10 , h - 22, 0xffFFFFFF);
			    mc.font.drawShadow(matrixStack, msg2, w - msg2width - 10 , h - 12, 0xffFFFFFF);
			    
			}			
		}
		
	}
}
