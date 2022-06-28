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

package com.specialeffect.eyemine.packets.messages;

import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Supplier;

public class ActivateBlockAtPosition {

	private BlockPos blockPos;

	public ActivateBlockAtPosition() {
	}

	public ActivateBlockAtPosition(BlockPos pos) {
		this.blockPos = pos;
	}

	public static ActivateBlockAtPosition decode(FriendlyByteBuf buf) {
		BlockPos blockPos = buf.readBlockPos();
		return new ActivateBlockAtPosition(blockPos);
	}

	public static void encode(ActivateBlockAtPosition pkt, FriendlyByteBuf buf) {
		BlockPos blockPos = pkt.blockPos;
		buf.writeBlockPos(blockPos);
	}

	public static class Handler {
		@SuppressWarnings("deprecation")
		public static void handle(final ActivateBlockAtPosition pkt, Supplier<NetworkManager.PacketContext> context) {
			context.get().queue(() -> {
				Player player = context.get().getPlayer();
				if (player == null) {
					return;
				}

				Level world = player.level;
				BlockState state = world.getBlockState(pkt.blockPos);
				Block block = state.getBlock();

				// NOTE this assumes hit is not used by onBlockActivated: could be a problem with some blocks
				BlockHitResult hit = null;

				// NOTE: should use state.onBlockActivated, but this requires non-null hit, so we suppress warning
				block.use(state, world, pkt.blockPos, player, InteractionHand.MAIN_HAND, hit);
			});
		}
	}
}
