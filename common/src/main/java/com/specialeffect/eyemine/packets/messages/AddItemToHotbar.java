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
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record AddItemToHotbar(ItemStack item, int slotId) implements CustomPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, AddItemToHotbar> CODEC = StreamCodec.composite(
			ItemStack.STREAM_CODEC,
			p -> p.item,
			ByteBufCodecs.INT,
			p -> p.slotId,
			AddItemToHotbar::new
	);
	public static final CustomPacketPayload.Type<AddItemToHotbar> ID = new CustomPacketPayload.Type<>(
			ResourceLocation.fromNamespaceAndPath(EyeMine.MOD_ID, "add_item_to_hotbar"));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return ID;
	}

	public AddItemToHotbar(ItemStack item) {
		this(item, -1);
	}

	public static class Handler {
		public static void handle(final AddItemToHotbar pkt, NetworkManager.PacketContext context) {
			context.queue(() -> {
				Player player = context.getPlayer();
				if (player == null) {
					return;
				}

				Inventory inventory = player.getInventory();
				int slot = pkt.slotId;
				if (slot < 0) {
					slot = inventory.getSuitableHotbarSlot();
				}
				inventory.setItem(slot, pkt.item);
				inventory.selected = slot;
			});
		}
	}
}
