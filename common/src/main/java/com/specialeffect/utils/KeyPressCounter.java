/**
 * Copyright (C) 2016-2020 Kirsty McNaught
 * <p>
 * Developed for SpecialEffect, www.specialeffect.org.uk
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.utils;

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
		long currTime = System.currentTimeMillis();

		if (mCount == 0) {
			mLastTimestamp = currTime;
		}
		if (currTime - mLastTimestamp > mTimeoutMs) {
			mCount = 0;
		} else if (mCount < mMaxCount) {
			mCount++;
		}

		mLastTimestamp = currTime;
	}
}
