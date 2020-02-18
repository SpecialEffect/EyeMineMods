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

import net.minecraftforge.event.TickEvent.ClientTickEvent;

public class RepeatedOnLivingCallback implements OnLivingCallback {

	public RepeatedOnLivingCallback(IOnLiving callback, int nTicks) {
		mCallback = callback;
		mTicksRemaining = nTicks;
	}

	@Override
	public void onClientTick(ClientTickEvent event) {
		mCallback.onClientTick(event);
		mTicksRemaining--;
	}

	@Override
	public boolean hasCompleted() {
		return mTicksRemaining <= 0;
	}

	private int mTicksRemaining = 0;
	IOnLiving mCallback;
}
