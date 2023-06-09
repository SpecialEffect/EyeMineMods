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

package com.specialeffect.eyemine.client.gui.crosshair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public interface ICrosshairOverlay {
	void renderOverlay(GuiGraphics guiGraphics, Minecraft minecraft);
}
