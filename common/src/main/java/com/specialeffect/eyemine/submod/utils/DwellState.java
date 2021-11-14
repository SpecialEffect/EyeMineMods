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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class DwellState {
	// Directly reference a log4j logger.	
    @SuppressWarnings("unused")
	protected static final Logger LOGGER = LogManager.getLogger();
    
	// DWEL PROPERTIES
	final long dwellTime; 		// How long does a dwell take? (ms)
	final long lockonTime;		// Lock on time < dwell time
	
	// How long should dwell stay visible after interaction? (ms)
	final long decayTime;
	
	public DwellState(long dwellTime, long lockonTime, long decayTime) {
		this.dwellTime = dwellTime;
		this.decayTime = decayTime;
		if (dwellTime < lockonTime) {
			LOGGER.error("Specified lock on time is greater than dwell time, reverting to immediate lock on");
			this.lockonTime = 0;
		}
		else {
			this.lockonTime = lockonTime;	
		}
	}
	
	// Current state
	private long dwellAmount = 0;
	private long decayAmount = 0;
	
	public double getDwellProportion() {
		return (double)dwellAmount/(double)dwellTime;
	}
	
	public double getDwellProportionSinceLockon() {
		return Math.max(0.0, (double)(dwellAmount - lockonTime)/(double)dwellTime);
	}
	
	public double getDecayProportion() {
		return (double)decayAmount/(double)decayTime;
	}
	
	public boolean hasCompleted() {
		return this.getDwellProportion() > 1.0;
	}
	
	public boolean shouldRender() {
		return (dwellAmount > lockonTime);
	}
	
	public void update(long dt, boolean active) {
		if (active) {
			dwellAmount += dt;
			decayAmount = 0;							
		} else {
			decayAmount += dt;					
		}
	}
	
	public boolean shouldDiscard() {
		return (decayAmount > decayTime);
	}
}