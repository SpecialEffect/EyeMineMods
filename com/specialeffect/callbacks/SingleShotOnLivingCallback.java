package com.specialeffect.callbacks;

import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

public class SingleShotOnLivingCallback implements OnLivingCallback {

	public SingleShotOnLivingCallback(IOnLiving callback) {
		mCallback = callback;
	}
	
	@Override
	public void onLiving(LivingUpdateEvent event) {
		mCallback.onLiving(event);
		mHasCompleted = true;
	}

	@Override
	public boolean hasCompleted() {
		return mHasCompleted;
	}
	
	boolean mHasCompleted = false;
	IOnLiving mCallback;
}
