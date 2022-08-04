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

package com.specialeffect.eyemine.client.forge;

import com.specialeffect.eyemine.EyeMineClient;
import com.specialeffect.eyemine.event.BlockOutlineEvent;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ClientHandler {
	public static void setup(final FMLCommonSetupEvent event) {
		EyeMineClient.setupComplete = true;
		EyeMineClient.refresh();
	}

	public static void onOutlineRender(RenderHighlightEvent.Block event) {
		BlockOutlineEvent.OUTLINE.invoker().renderOutline(event.getMultiBufferSource(), event.getPoseStack());
	}
}
