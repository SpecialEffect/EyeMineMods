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

package com.specialeffect.eyemine.submod.utils;

import com.specialeffect.eyemine.mixin.MinecraftAccessor;
import com.specialeffect.eyemine.submod.SubMod;
import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class DebugAverageFps extends SubMod {
	public final String MODID = "debugfps";

	private int mTickCount = 0;
	private int mAveragingPeriod = 100;
	private int mLoggingPeriod = 100;
	private Queue<Integer> mPrevFps;

	public DebugAverageFps() {
	}

	public void onInitializeClient() {
		mPrevFps = new LinkedBlockingQueue<>();

		ClientTickEvent.CLIENT_PRE.register(this::onClientTick);
	}

	public void onClientTick(Minecraft event) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null) {
			mTickCount++;
			
			int currFps = ((MinecraftAccessor)Minecraft.getInstance()).getFPS();
			mPrevFps.add(currFps);
			
			if (mPrevFps.size() > mAveragingPeriod) {
				mPrevFps.remove();
			}
			
			if (mTickCount == mLoggingPeriod) {
				LOGGER.debug("FPS: " + this.computeAverage());
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
}
