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

import com.specialeffect.mods.ChildMod;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class DebugAverageFps extends ChildMod
{

	public static final String MODID = "debugfps";
	public static final String NAME = "DebugAverageFps";

	public DebugAverageFps() {
	}
	
	public void setup(final FMLCommonSetupEvent event) {
		mPrevFps = new LinkedBlockingQueue<Integer>();
	}

	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {
		PlayerEntity player = Minecraft.getInstance().player;
    	if (null != player && event.phase == TickEvent.Phase.START) {
			mTickCount++;
			
			int currFps = Minecraft.getDebugFPS();
			mPrevFps.add(currFps);
			
			if (mPrevFps.size() > mAveragingPeriod) {
				mPrevFps.remove();
			}
			
			if (mTickCount == mLoggingPeriod) {
				LOGGER.debug("FPS: "+ this.computeAverage());
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
