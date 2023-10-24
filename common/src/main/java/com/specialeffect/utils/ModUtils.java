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

package com.specialeffect.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class ModUtils {

	// Check if LivingEntity is the current player (and not another
	// player on the network, for instance)
	public static boolean entityIsMe(Entity entity) {
		if (entity instanceof Player player) {
			UUID playerUUID = player.getUUID();
			Player myself = Minecraft.getInstance().player;
			if (null == myself) {
				return false;
			}
			UUID myUUID = myself.getUUID();

			return (playerUUID.equals(myUUID));
		} else {
			return false;
		}
	}

	@SafeVarargs
	public static <T> List<T> joinLists(List<T>... lists) {
		List<T> combined = new ArrayList<>();
		for (List<T> list : lists) {
			combined.addAll(list);
		}
		return combined;
	}

	public static boolean hasActiveGui() {
		// Is there a GUI currently open ?
		// (i.e. false means in-game without gui)
		return (null != Minecraft.getInstance().screen);
	}


	public static int findSlotInContainer(AbstractContainerMenu container, int guiLeft, int guiTop, int x, int y, int slotWidth) {
		// Find index of slot in container that contains the mouse position (x, y)
		// Note that (x,y) is in absolute screen coords (unscaled)
		// and a slot's xPos, yPos are relative to the inner gui with position (guiLeft, guiTop)
		List<Slot> slots = container.slots;
		for (int i = 0; i < slots.size(); i++) {
			Slot slot = slots.get(i);
			if (x > guiLeft + slot.x &&
					x < guiLeft + slot.x + slotWidth &&
					y > guiTop + slot.y &&
					y < guiTop + slot.y + slotWidth) {
				return i;
			}
		}
		return -1;
	}

	// Get the x, y point corresponding to one of 8 compass points
	// 0 = N, 1 = NE, 2 = E, etc ...
	public static Point getCompassPoint(int i) {
		Point p = new Point(0, 0);
		i = i % 8;
		switch (i) {
			case 0 -> p.setLocation(0, +1);
			case 1 -> p.setLocation(+1, +1);
			case 2 -> p.setLocation(+1, 0);
			case 3 -> p.setLocation(+1, -1);
			case 4 -> p.setLocation(0, -1);
			case 5 -> p.setLocation(-1, -1);
			case 6 -> p.setLocation(-1, 0);
			default -> p.setLocation(-1, +1);
		}
		return p;
	}
//
//	public static Point getScaledDisplaySize(Minecraft mc) {
//		Point p = new Point(0, 0);
//		ScaledResolution res = new ScaledResolution(mc);
//		p.setLocation(res.getScaledWidth(), res.getScaledHeight());
//
//		return p;
//
//	}


	public static void drawTexQuad(double x, double y, double width, double height, float alpha) {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuilder();

		int z = 10;
		bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		bufferbuilder.vertex(x, y + height, z).uv(0.0f, 1.0f).endVertex();
		bufferbuilder.vertex(x + width, y + height, z).uv(1.0f, 1.0f).endVertex();
		bufferbuilder.vertex(x + width, y, z).uv(1.0f, 0.0f).endVertex();
		bufferbuilder.vertex(x, y, z).uv(0.0f, 0.0f).endVertex();

		tessellator.end();
	}

	// Find an item in the hotbar which matches the given class
	// (this includes all subclasses)
	public static int findItemInHotbar(Inventory inventory, Predicate<Item> Itempredicate) {
		int itemId = -1;
		int currentItemId = inventory.selected;
		NonNullList<ItemStack> items = inventory.items;
		if (items != null) {
			for (int i = 0; i < Inventory.getSelectionSize(); i++) {
				ItemStack stack = items.get(i);
				if (!stack.isEmpty()) {
					Item item = stack.getItem();
					if (Itempredicate.test(item)) {
						itemId = i;
						// Ideally we'd keep the current item if it
						// happens to match the spec.
						if (itemId == currentItemId) {
							return itemId;
						}
					}
				}
			}
		}
		return itemId;
	}

	// should be run from onliving
	// maybe also needs running from server??
	public static void moveItemToHotbarAndSelect(Inventory inventory, ItemStack item) {
		// stick the item in an arbitrary non-hotbar slot, then let the
		// inventory
		// figure out how best to move it to the hotbar (e.g. to an empty slot).
		int slotId = 12;
		inventory.setItem(slotId, item);
		inventory.pickSlot(slotId);
	}

	public static void sendPlayerMessage(String msg) {
		Player player = Minecraft.getInstance().player;
		if (player != null) {
			player.sendSystemMessage(Component.literal(msg));
		}
	}

	public static void sendPlayerMessage(Component component) {
		Player player = Minecraft.getInstance().player;
		if (player != null) {
			player.sendSystemMessage(component);
		}
	}

	public static EntityHitResult getMouseOverEntity() {
		// Returns the entity the mouse is over, or null
		HitResult result = Minecraft.getInstance().hitResult;
		if (result instanceof EntityHitResult) {
			return (EntityHitResult) result;
		} else {
			return null;
		}
	}

	public static BlockHitResult getMouseOverBlock() {
		// Returns the block the mouse is over, or null
		HitResult result = Minecraft.getInstance().hitResult;
		if (result instanceof BlockHitResult) {
			return (BlockHitResult) result;
		} else {
			return null;
		}
	}

	// Check if there's a block at the given position which
	// blocks movement.
	@SuppressWarnings("unused")
	private boolean doesBlockMovement(BlockPos pos) {
		Level world = Minecraft.getInstance().level;
		return world.getBlockState(pos).blocksMotion();
	}

	@SuppressWarnings("unused")
	private boolean isDirectlyFacingSideHit(Direction sideHit, Vec3 lookVec) {
		double thresh = 0.8;
		switch (sideHit) {
			case NORTH:
				if (lookVec.z > thresh) {
					return true;
				}
				break;
			case EAST:
				if (lookVec.x < -thresh) {
					return true;
				}
				break;
			case SOUTH:
				if (lookVec.z < -thresh) {
					return true;
				}
				break;
			case WEST:
				if (lookVec.x > thresh) {
					return true;
				}
				break;
			default:
				break;
		}
		return false;
	}

	@SuppressWarnings("removal")
	public static BlockPos highestSolidPoint(BlockPos pos) {
		// Gets a spawn-able location above the point
		// Highest solid block that isn't foliage
		Level world = Minecraft.getInstance().level;
		LevelChunk chunk = world.getChunkAt(pos);

		BlockPos blockpos;
		BlockPos blockpos1;
		//TODO: LevelChunk#getHighestSectionPosition() is deprecated and marked for removal
		for (blockpos = new BlockPos(pos.getX(), chunk.getHighestSectionPosition() + 16, pos.getZ()); blockpos.getY() >= 0; blockpos = blockpos1) {
			blockpos1 = blockpos.below();
			BlockState state = chunk.getBlockState(blockpos1);

			if (state.blocksMotion() && !(state.getBlock() instanceof LeavesBlock)) {
				break;
			}
		}

		return blockpos;
	}


}
