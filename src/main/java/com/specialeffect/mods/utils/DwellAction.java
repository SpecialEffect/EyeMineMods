/**
 * Copyright (C) 2016 Kirsty McNaught, SpecialEffect
 * www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.mods.utils;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.irtimaled.bbor.client.renderers.AbstractRenderer;

import com.specialeffect.utils.ChildModWithConfig;
import com.specialeffect.mods.ChildMod;
import com.specialeffect.mods.EyeMineConfig;
import com.specialeffect.mods.mousehandling.MouseHandler;
import com.specialeffect.utils.ModUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public abstract class DwellAction 
extends ChildMod implements ChildModWithConfig {
	public final String MODID = "dwellbuild";
	
	public DwellAction(String name) {
		this.actionName = name;
	}
	
	public DwellAction(String name, int labelOffset) {
		this.actionName = name;
		this.labelOffset = labelOffset;
	}

	public void setup(final FMLCommonSetupEvent event) {
		this.syncConfig();
	}

	protected boolean mDwelling = false;
	
	private long lastTime = 0;
	private int dwellTimeInit = 200; // ms
	private int dwellTimeComplete = 1000; // ms
	private int dwellTimeDecay = 200;
	
	public String actionName = "DWELL"; 
	private int labelOffset = 0;
		
	private Map<TargetBlock, DwellState> liveTargets = new HashMap<>();

	protected void setDwelling(boolean isDwelling) {
		mDwelling = isDwelling;
		if (!isDwelling) {
			this.liveTargets.clear();
		}		
	}
		
	// Subclasses define action to be performed on dwell.
	public abstract void performAction(TargetBlock block);
		
	public void syncConfig() {
        this.dwellTimeComplete = (int) (1000*EyeMineConfig.dwellTimeSeconds.get());
        this.dwellTimeInit = (int) (1000*EyeMineConfig.dwellLockonTimeSeconds.get());
        this.dwellTimeDecay = (int) (this.dwellTimeComplete/3.5);    
	}
	
	@SubscribeEvent
	public void onClientTick(ClientTickEvent event) {
		if (event.phase == Phase.START) {
			long time = System.currentTimeMillis();
			long dt = time - this.lastTime;
			this.lastTime = time;							
			
			if (mDwelling && !ModUtils.hasActiveGui()) {
				if (MouseHandler.hasPendingEvent() || EyeMineConfig.moveWhenMouseStationary.get()) {

					// What are we currently targeting?
					BlockRayTraceResult rayTraceBlock = ModUtils.getMouseOverBlock(); 							
					TargetBlock currentTarget = (rayTraceBlock == null) ? null : new TargetBlock(rayTraceBlock);	
					
					// If it's not in the hashmap yet, it means we hit here before the render event - just wait until next tick
					if (!liveTargets.containsKey(currentTarget)) {
						return;
					}
					
					// Update all dwell times: the current target increments, others decrement and decay
					for (Map.Entry<TargetBlock, DwellState> entry : liveTargets.entrySet()) {
						TargetBlock key = entry.getKey();						
						boolean active = key.equals(currentTarget);
						DwellState dwellState = entry.getValue(); 
					    dwellState.update(dt, active);
					}
					
					// Remove all that have decayed fully
					liveTargets.entrySet().removeIf(e -> e.getValue().shouldDiscard());
					
					// Place block if dwell complete	
					if (currentTarget != null && liveTargets.get(currentTarget).hasCompleted()) {			
						this.performAction(currentTarget);						
						liveTargets.remove(currentTarget);		
					}
					// TODO: what if can't use item ? Currently this results in flashing again and again
					// Add this to dwell state?
				}
			}
		}
	}
	
	private void renderCentralisedDwell(TargetBlock target, DwellState dwellState, boolean expanding) {
		Color color = new Color(0.75f, 0.25f, 0.0f);

		// size of square proportional to dwell progress
		double dDwell = dwellState.getDwellProportionSinceLockon();
		
		// opacity proportional to decay progress
		int usualOpacity = 125;
		int iOpacity = (int) (usualOpacity*(0.25F + 0.75F*(1.0f - dwellState.getDecayProportion())));
		
		AbstractRenderer.renderBlockFaceCentralisedDwell(target.pos, target.direction, color, dDwell, iOpacity);	
	}
	
	private void renderOpacityDwell(TargetBlock target, DwellState dwellState) {
		Color color = new Color(0.75f, 0.25f, 0.0f);
		double maxAlpha = 0.85*255.0; 
		
		// Opacity increases with dwell amount
		double dAlpha = maxAlpha*(dwellState.getDwellProportion());
		int iAlpha = (int)dAlpha;
		
		AbstractRenderer.renderBlockFace(target.pos, target.direction, color, iAlpha);
	}
	
	@SubscribeEvent
	public void onBlockOutlineRender(DrawBlockHighlightEvent e)
	{
				
		if (Minecraft.getInstance().currentScreen != null) {
			this.liveTargets.clear();
			return;
		}
						
		if (mDwelling) {
			
			// Add current block to live targets if required
			// (we only want to add from within this method, so we avoid un-buildable surfaces, 
			// a.k.a. "MISS" ray trace results)
			if (e.getTarget().getType() == RayTraceResult.Type.MISS) {
				return;
			}
			
			BlockRayTraceResult rayTraceBlock = ModUtils.getMouseOverBlock();
			TargetBlock currentTarget = (rayTraceBlock == null) ? null : new TargetBlock(rayTraceBlock);
			if (currentTarget != null && !liveTargets.containsKey(currentTarget)) {
				liveTargets.put(currentTarget, 
						new DwellState(this.dwellTimeComplete, this.dwellTimeInit, this.dwellTimeDecay));
			}
			
			// Update dwell visualisation for all targets
			for (Map.Entry<TargetBlock, DwellState> entry : liveTargets.entrySet()) {
										
				DwellState dwellState = entry.getValue(); 				
				if (dwellState.shouldRender()) {
					TargetBlock target = entry.getKey();
					
					boolean doCentralised = !EyeMineConfig.dwellShowWithTransparency.get();
					boolean expanding = EyeMineConfig.dwellShowExpanding.get();
					
					if (doCentralised) {
						this.renderCentralisedDwell(target, dwellState, expanding);	
					}
					else {
						this.renderOpacityDwell(target, dwellState);
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onRenderGameOverlayEvent(final RenderGameOverlayEvent.Post event) {

		if(event.getType() != ElementType.CROSSHAIRS)
		{      
			return;
		}
		
		// If dwell is on, show a warning message		
		if (mDwelling) {
			String msg = actionName;
		
			Minecraft mc = Minecraft.getInstance();
			int w = mc.mainWindow.getScaledWidth();
			int h = mc.mainWindow.getScaledHeight();
						
			int msgWidth = mc.fontRenderer.getStringWidth(msg);
		    
		    mc.fontRenderer.drawStringWithShadow(msg, w/2 - msgWidth/2, h/2 - 20 - labelOffset, 0xffFFFFFF);		    
		    
		}
		
	}
}
