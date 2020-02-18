/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.callbacks;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.TickEvent.ClientTickEvent;

public class DelayedOnLivingCallback implements OnLivingCallback {

	public DelayedOnLivingCallback(IOnLiving callback, int nTicks) {
		mWaitTicks = nTicks;
		mCallback = callback;
		mHasCompleted = false;
	}
	
	@Override
	public void onClientTick(ClientTickEvent event) {
		PlayerEntity player = Minecraft.getInstance().player;
		if (mWaitTicks == 0) {
			mCallback.onClientTick(event);
			mHasCompleted = true;
		}
		else {
			mWaitTicks -= 1;
		}
	}

	@Override
    public boolean hasCompleted() {
		return mHasCompleted;
	}

	
	IOnLiving mCallback;
	int mWaitTicks;
	boolean mHasCompleted;
}
