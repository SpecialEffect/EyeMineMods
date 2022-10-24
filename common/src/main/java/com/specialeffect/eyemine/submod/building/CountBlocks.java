/**
 * Copyright (C) 2016-2020 Kirsty McNaught
 * <p>
 * Developed for SpecialEffect, www.specialeffect.org.uk
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * <p>
 * This class control counting blocks
 *
 * @author Kirsty McNaught and Becky Tyler
 * @version 1.0
 */
package com.specialeffect.eyemine.submod.building;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import com.mojang.blaze3d.vertex.PoseStack;
import com.specialeffect.eyemine.client.Keybindings;
import com.specialeffect.eyemine.event.BlockOutlineEvent;
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.utils.ModUtils;
import me.shedaniel.architectury.event.events.BlockEvent;
import me.shedaniel.architectury.event.events.client.ClientRawInputEvent;
import me.shedaniel.architectury.utils.IntValue;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class CountBlocks extends SubMod {
	public final String MODID = "countblocks"; // this needs to be a unique ID

	// Member variables we need
	// - a static KeyMapping for the shortcut key
	public static KeyMapping mNumberBlockKB;

	// - a list of BlockPos positions for all the blocks we are tracking
	public ArrayList<BlockPos> blockPosList;

	// - a boolean indicating whether we are currently counting blocks
	public boolean countingBlocks;

	public void onInitializeClient() {

		// Create the list of BlockPos positions
		blockPosList = new ArrayList<BlockPos>();

		// Initialise the countingBlocks flag
		countingBlocks = false;

		// Register the key binding here
		Keybindings.keybindings.add(mNumberBlockKB = new KeyMapping(
				"key.eyemine.count_blocks",
				Type.KEYSYM, // this is always KEYSYM
				GLFW.GLFW_KEY_K, // this selects the default key. try autocompleting GLFW.GLFW_KEY... to see more
				// options
				"category.eyemine.category.eyegaze_common" // this sets the translation key for the name of the category
				// in the controls list
				// (we use eyegaze_common, eyegaze_extra and eyegaze_settings
				// depending on the mod)
		));

		// by adding to Keybindings.keybindings and
		// registering function with ClientRawInputEvent.Key_PRESSED
		// (look at PickBlock class for reference)
		ClientRawInputEvent.KEY_PRESSED.register(this::onKeyInput);

		// Register the "place block" event
		BlockEvent.PLACE.register(this::onPlaceBlock);

		// Register the "break block" event
		BlockEvent.BREAK.register(this::onBreakBlock);

		// Register the "block outline" event
		BlockOutlineEvent.OUTLINE.register(this::onBlockOutlineRender);
	}

	private InteractionResult onKeyInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
		// This method gets called when *any* key is pressed

		// Skip if there is a GUI visible
		if (ModUtils.hasActiveGui()) {
			return InteractionResult.PASS;
		}

		// Skip if F3 is held down (this is used for debugging)
		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 292)) {
			return InteractionResult.PASS;
		}

		// If statement for when the key pressed is the one we are using.
		// Inside the if statement we will need to
		// - turn counting on or off
		// - empty the list of blocks
		if (mNumberBlockKB.matches(keyCode, scanCode) && mNumberBlockKB.consumeClick()) {
			ModUtils.sendPlayerMessage("Key pressed: " + keyCode);

			// Toggle the value of countingBlocks
			countingBlocks = !countingBlocks;

			// Clear the list of BlockPos positions
			blockPosList.clear();

		}
		return InteractionResult.PASS;
	}

	/**
	 * Whenever a block is placed, send a chat message with the position of the
	 * block
	 *
	 * @param level    The level the block is being placed in
	 * @param position The position of the block that was placed
	 * @param state    The block state of the block being placed
	 * @param entity   The entity that placed the block.
	 * @return The return value is an InteractionResult. This is a value that tells
	 * the game what to do
	 * with the block.
	 */
	public InteractionResult onPlaceBlock(Level level, BlockPos position, BlockState state, Entity entity) {
		// Add block's position to list of block positions
		blockPosList.add(position);

		// Send a chat message showing position and number of blocks placed
		ModUtils.sendPlayerMessage("Block " + blockPosList.size() + " placed at " + position); // for debugging

		// This method is called whenever a block is placed
		return InteractionResult.PASS;
	}

	/**
	 * Whenever a block is broken, send a chat message with the position of the
	 * block
	 *
	 * @param level    The level the block is being broken in
	 * @param position The position of the block that was broken
	 * @param state    The block state of the block being broken
	 * @param entity   The entity that broke the block.
	 * @param xp       The experience gained from the block being broken.
	 * @return The return value is an InteractionResult. This is a value that tells
	 * the game what to do
	 * with the block.
	 */
	public InteractionResult onBreakBlock(Level level, BlockPos position, BlockState state, ServerPlayer entity, @Nullable IntValue xp) {
		// Remove block's position from the list of block positions
		blockPosList.remove(position);

		// Send a chat message showing position and number of blocks broken
		ModUtils.sendPlayerMessage("Block " + blockPosList.size() + " broken at " + position); // for debugging

		// This method is called whenever a block is placed
		return InteractionResult.PASS;
	}

	public InteractionResult onBlockOutlineRender(MultiBufferSource bufferSource, PoseStack poseStack) {
		final Minecraft minecraft = Minecraft.getInstance();
		final LocalPlayer player = minecraft.player;
		final Level level = minecraft.level;

		// Only render stuff if countingBlocks is enabled and there's positions in the list
		if (player != null && level != null && countingBlocks && !blockPosList.isEmpty()) {
			poseStack.pushPose();
			final Camera camera = minecraft.gameRenderer.getMainCamera();
			Vec3 cameraPos = camera.getPosition();
			//Subtract the camera's position
			poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

			final Font font = minecraft.font;
			final float size = 0.02f;
			final int yellowColor = DyeColor.YELLOW.getTextColor();

			//Get the first position in the list
			BlockPos firstPosition = blockPosList.get(0);
			for (BlockPos pos : blockPosList) {
				//Get the distance from the first position and add 1 to it (So that the starting block doesn't start at 0)
				int distance = firstPosition.distManhattan(pos) + 1;
				String text = String.valueOf(distance);
				poseStack.pushPose();

				//Position the text centered above the blocks
				poseStack.translate(pos.getX() + 0.45D, pos.getY() + 1.125D, pos.getZ() + 0.45D);
				//Make the text always face the player
				poseStack.mulPose(camera.rotation());
				//Size down the text so that it's not giant
				poseStack.scale(-size, -size, -size);
				//Make it 50% bigger
				poseStack.scale(1.5F, 1.5F, 1.5F);
				//Offset the text by half it's size to keep it centered
				poseStack.translate(font.width(text) / 2, 0, 0);
				//Draw the text with shadow in the world
				font.drawShadow(poseStack, text, 0, 0, yellowColor);

				poseStack.popPose();
			}
			poseStack.popPose();
		}
		return InteractionResult.PASS;
	}
}
