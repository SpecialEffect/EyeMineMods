package com.specialeffect.eyegazemod;

public class KeyPressCounter {

	private int mMaxCount = 8;
	private int mTimeoutMs = 500;
	private int mCount = 0;
	private long mLastTimestamp = 0;
	
	public void reset() {
		mCount = 0;
	}
	
	public int getCount() {
		return mCount;
	}
	
	public void increment() {
		long currTime = java.lang.System.currentTimeMillis();

		if (mCount == 0) {
			mLastTimestamp = currTime;
		}		
		if (currTime - mLastTimestamp > mTimeoutMs) {
			mCount = 0;
		}
		else if (mCount < mMaxCount) {
			mCount++;
		}
		
		mLastTimestamp = currTime;
	}

	
			

}
