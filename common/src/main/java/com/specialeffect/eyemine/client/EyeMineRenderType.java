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

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/**
 * Custom RenderTypes for EyeMine's in-world rendering (dwell indicators, block outlines).
 * In MC 26.1.1, uses RenderTypes utility for translucent position-color rendering.
 */
public class EyeMineRenderType {

    public static RenderType dwellRenderType() {
        // Use the debug quads type for translucent position-color quads
        // TODO: If this doesn't render correctly, create a proper custom RenderType
        // using the new RenderSetup/RenderPipeline API
        return RenderTypes.debugQuads();
    }

    public static RenderType cubeRenderType() {
        return dwellRenderType();
    }
}
