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

package com.specialeffect.eyemine.submod.mining;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import com.specialeffect.eyemine.client.Keybindings;
import com.specialeffect.eyemine.mixin.KeyMappingAccessor;
import com.specialeffect.eyemine.platform.EyeMineConfig;
import com.specialeffect.eyemine.submod.utils.DwellAction;
import com.specialeffect.eyemine.submod.utils.TargetBlock;
import com.specialeffect.utils.ModUtils;
import me.shedaniel.architectury.event.events.client.ClientRawInputEvent;
import me.shedaniel.architectury.event.events.client.ClientTickEvent;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFW;

public class MineOne extends DwellAction {
	public MineOne() {
		super("Mine one"); 
	}

	public final String MODID = "autodestroy";

	private boolean mDestroying = false;
	private BlockPos mBlockToDestroy;
	private static KeyMapping mDestroyKB;

	public void onInitializeClient() {
		Keybindings.keybindings.add(mDestroyKB = new KeyMapping(
				"key.eyemine.mine_singular",
				Type.KEYSYM,
				GLFW.GLFW_KEY_N,
				"category.eyemine.category.eyegaze_common" // The translation key of the keybinding's category.
		));

		ClientTickEvent.CLIENT_PRE.register(this::onClientTick);
		ClientRawInputEvent.KEY_PRESSED.register(this::onKeyInput);
	}

	@Override
	public void onClientTick(Minecraft minecraft) {
		super.onClientTick(minecraft);

		LocalPlayer player = minecraft.player;
		if (player != null) {
			if (mDestroying) {
				// Select the best tool from the inventory
				ClientLevel level = minecraft.level;

    			// Swords can't destroy blocks: warn user
    			if (player.getMainHandItem().getItem() instanceof SwordItem) {
    				String message = "Can't destroy blocks with a sword, please select another item";
			        player.sendMessage(new TextComponent(message), Util.NIL_UUID);

    				this.stopDestroying();
    				return;
    			}

				// Stop attacking if we're not pointing at the block any more
				// (which means either we've destroyed it, or moved away)
				HitResult mov = minecraft.hitResult;
				boolean blockDestroyed = level != null && mBlockToDestroy != null && (level.getBlockState(mBlockToDestroy).isAir());
				boolean movedAway =  false;
				BlockPos pos = this.getMouseOverBlockPos();
				if (pos != null) {
					movedAway = mBlockToDestroy.distSqr((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), false) > 0.5;
				}

				if (mov == null || blockDestroyed || movedAway) {
					this.stopDestroying();
				}
			}
		}
	}

	private void startDestroying() {
		mDestroying = true;

		final KeyMapping attackBinding = Minecraft.getInstance().options.keyAttack;
		KeyMapping.set(((KeyMappingAccessor)attackBinding).getActualKey(), true);
	}

	private void stopDestroying() {
		final KeyMapping attackBinding = Minecraft.getInstance().options.keyAttack;
		KeyMapping.set(((KeyMappingAccessor)attackBinding).getActualKey(), false);

		mDestroying = false;
	}

	// Return the position of the block that the mouse is pointing at.
	// May be null, if pointing at something other than a block.
	private BlockPos getMouseOverBlockPos() {
		BlockPos pos = null;
		BlockHitResult rayTraceBlock = ModUtils.getMouseOverBlock();
		if (rayTraceBlock != null) {
			pos = rayTraceBlock.getBlockPos();
		}
		return pos;
	}

	private InteractionResult onKeyInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
		if (ModUtils.hasActiveGui()) { return InteractionResult.PASS; }

		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 292)) { return InteractionResult.PASS; }

		if (mDestroyKB.matches(keyCode, scanCode) && mDestroyKB.consumeClick()) {
			// turn off continuous mining
			ContinuouslyMine.stop();

			// start dwell if appropriate
			LocalPlayer player = Minecraft.getInstance().player;
			boolean useDwelling = player.isCreative() && EyeMineConfig.getUseDwellForSingleUseItem();
			if (useDwelling) {
				this.dwellOnce();
			}
			else {
				// start mining the block you're facing
				mBlockToDestroy = this.getMouseOverBlockPos();
				if (mBlockToDestroy == null) {
					LOGGER.debug("Nothing to attack");
					return InteractionResult.PASS;
				}
				else {
					this.startDestroying();
				}
			}
		}
		return InteractionResult.PASS;
	}

	@Override
	public void performAction(TargetBlock block) {
		final KeyMapping attackBinding = Minecraft.getInstance().options.keyAttack;
		KeyMapping.click(((KeyMappingAccessor)attackBinding).getActualKey());
	}
}
