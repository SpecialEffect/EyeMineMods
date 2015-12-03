package com.specialeffect.eyegazemod;

import java.util.concurrent.CountDownLatch;

import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

public class DelayedOnLivingCallback implements OnLivingCallback {

	public DelayedOnLivingCallback(IOnLiving callback, int nTicks) {
		mWaitTicks = nTicks;
		mCallback = callback;
		mHasCompleted = false;
	}
	
	@Override
	public void onLiving(LivingUpdateEvent event) {
		if (mWaitTicks == 0) {
			mCallback.onLiving(event);
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
