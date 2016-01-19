package com.specialeffect.callbacks;

import java.util.Iterator;
import java.util.LinkedList;

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

	LinkedList<OnLivingCallback> mOnLivingQueue;

	protected void queueOnLivingCallback(OnLivingCallback onLivingCallback) {
		synchronized (mOnLivingQueue) {
			mOnLivingQueue.add(onLivingCallback);
		}
	}

}
