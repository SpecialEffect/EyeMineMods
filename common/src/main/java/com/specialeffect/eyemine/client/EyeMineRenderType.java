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

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import org.lwjgl.opengl.GL11;

public class EyeMineRenderType extends RenderType {
	public EyeMineRenderType(String nameIn, VertexFormat formatIn, int drawModeIn, int bufferSizeIn, boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
		super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
	}

	public static RenderType dwellRenderType() {
		return RenderType.create("eyemine:dwell", DefaultVertexFormat.POSITION_COLOR, GL11.GL_QUADS, 2097152, true, false, getDwellState());
	}

	private static CompositeState getDwellState() {
		return CompositeState.builder()
				.setLightmapState(LIGHTMAP)
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setOutputState(TRANSLUCENT_TARGET)
				.createCompositeState(false);
	}

	public static RenderType cubeRenderType() {
		return RenderType.create("eyemine:cube", DefaultVertexFormat.POSITION_COLOR, GL11.GL_QUADS, 2097152, true, false, getCubeState());
	}

	private static CompositeState getCubeState() {
		return CompositeState.builder()
				.setLightmapState(LIGHTMAP)
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setOutputState(TRANSLUCENT_TARGET)
				.createCompositeState(false);
	}
}
