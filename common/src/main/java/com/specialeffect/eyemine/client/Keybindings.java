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

package com.specialeffect.eyemine.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class Keybindings {
	public static final List<KeyMapping> keybindings = new ArrayList<>();

	// Custom key mapping categories for EyeMine
	public static final KeyMapping.Category EYEGAZE_SETTINGS = KeyMapping.Category.register(
			Identifier.fromNamespaceAndPath("eyemine", "eyegaze_settings"));
	public static final KeyMapping.Category EYEGAZE_COMMON = KeyMapping.Category.register(
			Identifier.fromNamespaceAndPath("eyemine", "eyegaze_common"));
	public static final KeyMapping.Category EYEGAZE_EXTRA = KeyMapping.Category.register(
			Identifier.fromNamespaceAndPath("eyemine", "eyegaze_extra"));
	public static final KeyMapping.Category EYEGAZE_ADVANCED = KeyMapping.Category.register(
			Identifier.fromNamespaceAndPath("eyemine", "eyegaze_advanced"));
}
