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

import com.specialeffect.eyemine.EyeMine;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public record ActivateBlockAtPosition(BlockPos pos) implements CustomPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, ActivateBlockAtPosition> CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC,
			p -> p.pos,
			ActivateBlockAtPosition::new
	);
	public static final CustomPacketPayload.Type<ActivateBlockAtPosition> ID = new CustomPacketPayload.Type<>(
			ResourceLocation.fromNamespaceAndPath(EyeMine.MOD_ID, "activate_block_at_position"));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}

	public static class Handler {
		@SuppressWarnings("deprecation")
		public static void handle(final ActivateBlockAtPosition pkt, NetworkManager.PacketContext context) {
			context.queue(() -> {
				Player player = context.getPlayer();
				if (player == null) {
					return;
				}

				Level level = player.level();
				BlockState state = level.getBlockState(pkt.pos);
				Block block = state.getBlock();

				// NOTE this assumes hit is not used by onBlockActivated: could be a problem with some blocks
				BlockHitResult hit = null;

				// NOTE: should use state.onBlockActivated, but this requires non-null hit, so we suppress warning
				block.useItemOn(player.getItemInHand(InteractionHand.MAIN_HAND), state, level, pkt.pos, player, InteractionHand.MAIN_HAND, hit);
			});
		}
	}
}
