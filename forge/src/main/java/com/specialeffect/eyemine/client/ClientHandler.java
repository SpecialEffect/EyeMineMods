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

package com.specialeffect.eyemine.client;

import at.feldim2425.moreoverlays.gui.ConfigScreen;
import com.specialeffect.eyemine.EyeMine;
import com.specialeffect.eyemine.EyeMineConfig;
import com.specialeffect.eyemine.event.BlockOutlineEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.event.DrawHighlightEvent;

public class ClientHandler {
	public static Screen openSettings(Minecraft mc, Screen modlist){
		return new ConfigScreen(modlist, EyeMineConfig.CLIENT_CONFIG, EyeMine.MOD_ID);
	}

	public static void onOutlineRender(DrawHighlightEvent.HighlightBlock event) {
		 BlockOutlineEvent.OUTLINE.invoker().renderOutline(event.getBuffers(), event.getMatrix());
	}
}
