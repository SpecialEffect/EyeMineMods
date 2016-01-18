package com.specialeffect.eyegazemod;

import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

public class RepeatedOnLivingCallback implements OnLivingCallback {

	public RepeatedOnLivingCallback(IOnLiving callback, int nTicks) {
		mCallback = callback;
		mTicksRemaining = nTicks;
	}

	@Override
	public void onLiving(LivingUpdateEvent event) {
		mCallback.onLiving(event);
		mTicksRemaining--;
	}

	@Override
	public boolean hasCompleted() {
		return mTicksRemaining <= 0;
	}

	private int mTicksRemaining = 0;
	IOnLiving mCallback;
}
