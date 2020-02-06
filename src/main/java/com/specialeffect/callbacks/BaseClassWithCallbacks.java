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

import java.util.Iterator;
import java.util.LinkedList;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

// A class which handles tick-based callbacks for mods.
// To make a mod with callbacks, inherit from this class 
// and call this.queueOnLivingCallback and then
// this.processQueuedCallbacks when you're in an appropriate tick
// event.
public class BaseClassWithCallbacks {

	public BaseClassWithCallbacks() {
		mOnLivingQueue = new LinkedList<OnLivingCallback>();
	}

	protected void processQueuedCallbacks(LivingUpdateEvent event) {
		//FIXME: do we rely on these being called on the player as opposed to 
		// other living entities?? smells a bit
		synchronized (mOnLivingQueue) {
			Iterator<OnLivingCallback> it = mOnLivingQueue.iterator();
			while (it.hasNext()) {
				OnLivingCallback item = it.next();
				item.onLiving(event);
				if (item.hasCompleted()) {
					it.remove();
				}        		
			}
		}
	}

	protected LinkedList<OnLivingCallback> mOnLivingQueue;

	protected void queueOnLivingCallback(OnLivingCallback onLivingCallback) {
		synchronized (mOnLivingQueue) {
			mOnLivingQueue.add(onLivingCallback);
		}
	}	

}
