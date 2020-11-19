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

package com.specialeffect.mods.mining;

import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;

import com.specialeffect.messages.GatherBlockMessage;
import com.specialeffect.mods.ChildMod;
import com.specialeffect.mods.utils.KeyWatcher;
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class GatherDrops  extends ChildMod
{

	public final String MODID = "gatherdrops";

	private static KeyBinding mGatherKB;

	private static GatherDrops instance;
	
	public GatherDrops() {
		instance = this;
	}
	
	public void setup(final FMLCommonSetupEvent event) {
		    	
		this.setupChannel(MODID, 1);

		int id = 0;        
        channel.registerMessage(id++, GatherBlockMessage.class, GatherBlockMessage::encode, 
        		GatherBlockMessage::decode, GatherBlockMessage.Handler::handle);        

		// Register key bindings	
		mGatherKB = new KeyBinding("Gather dropped items", GLFW.GLFW_KEY_KP_MULTIPLY, CommonStrings.EYEGAZE_EXTRA);
		ClientRegistry.registerKeyBinding(mGatherKB);
	}

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {      
        
        if (ModUtils.hasActiveGui()) { return; }	    
	    if (event.getAction() != GLFW.GLFW_PRESS) { return; }

	    if (KeyWatcher.f3Pressed) { return; }

    	if(mGatherKB.getKey().getKeyCode() == event.getKey()) {
    		PlayerEntity player = Minecraft.getInstance().player;
			gatherBlocks(player);
		}
	}
	
	public static void gatherBlocks(PlayerEntity player) {
		World world = Minecraft.getInstance().world;

		BlockPos playerPos = player.getPosition();
		double dx, dy, dz;
		dx = dy = dz = 5;

		AxisAlignedBB aaBb = new AxisAlignedBB(playerPos.subtract(new Vec3i(dx, dy, dz)), 
				playerPos.add(new Vec3i(dx, dy, dz)));
		ArrayList<ItemEntity> items = (ArrayList<ItemEntity>)world.getEntitiesWithinAABB(ItemEntity.class,aaBb);

		if(items != null && !items.isEmpty()) {
			LOGGER.debug("gathering " + items.size() + " nearby items");
			// Ask server to move items
			for (int i = 0; i < items.size(); i++) {
                instance.channel.sendToServer(new GatherBlockMessage(items.get(i).getEntityId()));
			}
		}
	}

}
