/**
 * Copyright (C) 2016-2020 Kirsty McNaught
 * <p>
 * Developed for SpecialEffect, www.specialeffect.org.uk
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.eyemine.submod.misc;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import com.specialeffect.eyemine.client.Keybindings;
import com.specialeffect.eyemine.mixin.ClientLevelAccessor;
import com.specialeffect.eyemine.platform.EyeMineConfig;
import com.specialeffect.eyemine.submod.IConfigListener;
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.utils.ModUtils;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientRawInputEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

import java.util.function.Predicate;

public class OpenTablesChests extends SubMod implements IConfigListener {
	public final String MODID = "opentableschests";

	private static KeyMapping mOpenChestKB;
	private static KeyMapping mOpenCraftingTableKB;

	private int mRadius = 5;

	public void onInitializeClient() {
		// Register key bindings
		Keybindings.keybindings.add(mOpenChestKB = new KeyMapping(
				"key.eyemine.open_chest",
				Type.KEYSYM,
				GLFW.GLFW_KEY_LEFT_BRACKET,
				"category.eyemine.category.eyegaze_extra" // The translation key of the keybinding's category.
		));
		Keybindings.keybindings.add(mOpenCraftingTableKB = new KeyMapping(
				"key.eyemine.open_crafting_table",
				Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT_BRACKET,
				"category.eyemine.category.eyegaze_extra" // The translation key of the keybinding's category.
		));

		ClientRawInputEvent.KEY_PRESSED.register(this::onKeyInput);
	}

	public void syncConfig() {
		this.mRadius = EyeMineConfig.getRadiusChests();
	}

	// Search for closest block of a certain class, within maximum radius
	private static BlockPos findClosestBlockOfType(Predicate<Block> blockPredicate, Player player, ClientLevel level, int radius) {
		BlockPos playerPos = player.blockPosition();
		BlockPos closestBlockPos = null;

		// Find closest chest (within radius)
		double closestDistanceSq = 64; //Most vanilla containers close if you're further away then 64 so that should be the max
		for (int x = -radius; x <= radius; x++) {
			for (int z = -radius; z <= radius; z++) {
				for (int y = -radius; y <= radius; y++) {
					BlockPos blockPos = playerPos.offset(x, y, z);

					// Check if block is appropriate class
					Block block = level.getBlockState(blockPos).getBlock();
					if (blockPredicate.test(block)) {
						double distSq = playerPos.distSqr(new Vec3i(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
						if (distSq <= closestDistanceSq) {
							closestBlockPos = blockPos;
							closestDistanceSq = distSq;
						}
					}
				}
			}
		}

		return closestBlockPos;
	}

	public BlockHitResult getSimulatedHitResult(ClientLevel level, BlockPos pos) {
		Vec3 hitVec = new Vec3(pos.getX(), pos.getY(), pos.getZ());

		Direction fakeDirection = Direction.DOWN;
		for (Direction dir : Direction.values()) {
			if (!level.getBlockState(pos.relative(dir)).isAir()) {
				fakeDirection = dir;
				break;
			}
		}
		return new BlockHitResult(hitVec, fakeDirection, pos, true);
	}

	private EventResult onKeyInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
		if (ModUtils.hasActiveGui()) {
			return EventResult.pass();
		}

		if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 292)) {
			return EventResult.pass();
		}

		final LocalPlayer player = Minecraft.getInstance().player;
		final ClientLevel level = minecraft.level;
		if (mOpenChestKB.matches(keyCode, scanCode) && mOpenChestKB.consumeClick()) {
			BlockPos closestBlockPos = OpenTablesChests.findClosestBlockOfType((block -> block instanceof AbstractChestBlock), player, level, mRadius);

			// Ask server to open
			if (null == closestBlockPos) {
				player.sendSystemMessage(Component.translatable("eyemine.message.open_chest.none"));
			} else {
				BlockState state = level.getBlockState(closestBlockPos);
				BlockHitResult simulatedHit = getSimulatedHitResult(level, closestBlockPos);

				InteractionResult result = state.use(level, player, InteractionHand.MAIN_HAND, simulatedHit);
				if (result.consumesAction()) {
					BlockStatePredictionHandler blockstatepredictionhandler = ((ClientLevelAccessor)level).eyemineGetPredictionHandler().startPredicting();
					int i = blockstatepredictionhandler.currentSequence();
					player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, simulatedHit, i));
				}
			}
		} else if (mOpenCraftingTableKB.matches(keyCode, scanCode) && mOpenCraftingTableKB.consumeClick()) {
			BlockPos closestBlockPos = OpenTablesChests.findClosestBlockOfType((block -> block instanceof CraftingTableBlock), player, level, mRadius);

			// Ask server to open
			if (null == closestBlockPos) {
				player.sendSystemMessage(Component.translatable(
						"eyemine.message.open_crafting_table.none"));
			} else {
				BlockState state = level.getBlockState(closestBlockPos);
				BlockHitResult simulatedHit = getSimulatedHitResult(level, closestBlockPos);

				InteractionResult result = state.use(level, player, InteractionHand.MAIN_HAND, simulatedHit);
				if (result.consumesAction()) {
					BlockStatePredictionHandler blockstatepredictionhandler = ((ClientLevelAccessor)level).eyemineGetPredictionHandler().startPredicting();
					int i = blockstatepredictionhandler.currentSequence();
					player.connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, simulatedHit, i));
				}
			}
		}
		return EventResult.pass();
	}
}
