/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.mods.utils;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.lwjgl.input.Keyboard;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
import com.specialeffect.messages.ChangeFlyingStateMessage;
import com.specialeffect.utils.ModUtils;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = DebugAverageFps.MODID, 
version = ModUtils.VERSION,
name = DebugAverageFps.NAME)

public class DebugAverageFps
{

	public static final String MODID = "specialeffect.debugfps";
	public static final String NAME = "DebugAverageFps";

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {    
		FMLCommonHandler.instance().bus().register(this);    	
		
		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Log the average FPS, for debugging");
    	ModUtils.setAsParent(event, SpecialEffectUtils.MODID);

	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		// Subscribe to event buses
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);    	
		
		mPrevFps = new LinkedBlockingQueue<Integer>();

	}

	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (ModUtils.entityIsMe(event.entityLiving)) {
			mTickCount++;
			
			int currFps = Minecraft.getDebugFPS();
			mPrevFps.add(currFps);
			
			if (mPrevFps.size() > mAveragingPeriod) {
				mPrevFps.remove();
			}
			
			if (mTickCount == mLoggingPeriod) {
				System.out.println("FPS: "+ this.computeAverage());
				mTickCount = 0;
			}
		}			
	}
	
	private float computeAverage() {
		Iterator<Integer> iter = mPrevFps.iterator();
		float runningAve = 0;
    	while (iter.hasNext()) {
    		runningAve += (float)iter.next()/(float)mPrevFps.size();
    	}
		return runningAve;
	}
	
	private int mTickCount = 0; 
	private int mAveragingPeriod = 100;
	private int mLoggingPeriod = 100;
    private Queue<Integer> mPrevFps;
}
