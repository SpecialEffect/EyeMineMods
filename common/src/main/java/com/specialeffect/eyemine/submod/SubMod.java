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

package com.specialeffect.eyemine.submod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SubMod {

    // Directly reference a log4j logger.
	protected static final Logger LOGGER = LogManager.getLogger();

	public void onInitializeClient() {
		// can be overridden if mod has setup to do
	}
}
