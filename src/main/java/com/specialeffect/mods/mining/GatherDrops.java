/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
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
import com.specialeffect.utils.CommonStrings;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class GatherDrops  extends ChildMod
{

	public static final String MODID = "gatherdrops";
	public static final String NAME = "GatherDrops";
    private static final String PROTOCOL_VERSION = Integer.toString(1);

	private static KeyBinding mGatherKB;

    public static SimpleChannel channel;

	public void setup(final FMLCommonSetupEvent event) {
		    	
		channel = NetworkRegistry.newSimpleChannel(
                new ResourceLocation("specialeffect","gatherdrops")
                ,() -> PROTOCOL_VERSION
                , PROTOCOL_VERSION::equals
                , PROTOCOL_VERSION::equals);
        int id = 0;
        
        channel.registerMessage(id++, GatherBlockMessage.class, GatherBlockMessage::encode, 
        		GatherBlockMessage::decode, GatherBlockMessage.Handler::handle);        

		// Register key bindings	
		mGatherKB = new KeyBinding("Gather dropped items", GLFW.GLFW_KEY_KP_MULTIPLY, CommonStrings.EYEGAZE_EXTRA);
		ClientRegistry.registerKeyBinding(mGatherKB);
	}

	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (ModUtils.entityIsMe(event.getEntityLiving())) {
			
		}			
	}

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {      
        if (ModUtils.hasActiveGui()) { return; }
        
    	if(mGatherKB.isPressed()) {
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
                channel.sendToServer(new GatherBlockMessage(items.get(i).getEntityId()));
			}
		}
	}

}
