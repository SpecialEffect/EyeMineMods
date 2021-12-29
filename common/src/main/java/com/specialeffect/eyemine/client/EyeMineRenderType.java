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
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class EyeMineRenderType extends RenderType {
	public EyeMineRenderType(String nameIn, VertexFormat formatIn, Mode drawModeIn, int bufferSizeIn, boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
		super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
	}

	public static RenderType dwellRenderType() {
		return RenderType.create("eyemine:dwell", DefaultVertexFormat.POSITION_COLOR, Mode.QUADS, 2097152, false, false, getDwellState());
	}

	private static CompositeState getDwellState() {
		return CompositeState.builder()
				.setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
				.setLightmapState(LIGHTMAP)
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setOutputState(TRANSLUCENT_TARGET)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				.setCullState(NO_CULL)
				.setDepthTestState(NO_DEPTH_TEST)
				.createCompositeState(true);
	}

	public static RenderType cubeRenderType() {
		return RenderType.create("eyemine:cube", DefaultVertexFormat.POSITION_COLOR, Mode.QUADS, 2097152, false, false, getCubeState());
	}

	private static CompositeState getCubeState() {
		return CompositeState.builder()
				.setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
				.setLightmapState(LIGHTMAP)
				.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
				.setOutputState(TRANSLUCENT_TARGET)
				.setLayeringState(VIEW_OFFSET_Z_LAYERING)
				.setCullState(NO_CULL)
				.setDepthTestState(NO_DEPTH_TEST)
				.createCompositeState(true);
	}
}
