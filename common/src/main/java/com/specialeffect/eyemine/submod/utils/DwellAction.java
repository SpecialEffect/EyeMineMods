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

package com.specialeffect.eyemine.submod.utils;

import com.irtimaled.bbor.client.renderers.AbstractRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.specialeffect.eyemine.client.EyeMineRenderType;
import com.specialeffect.eyemine.event.BlockOutlineEvent;
import com.specialeffect.eyemine.platform.EyeMineConfig;
import com.specialeffect.eyemine.submod.IConfigListener;
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.eyemine.submod.mouse.MouseHandlerMod;
import com.specialeffect.utils.ModUtils;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientGuiEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public abstract class DwellAction extends SubMod implements IConfigListener {
	public final String MODID = "dwellbuild";

	protected boolean mDwelling = false;
	protected boolean showLabel = true;

	private long lastTime = 0;
	private int dwellTimeInit = 200; // ms
	private int dwellTimeComplete = 1000; // ms
	private int dwellTimeDecay = 200;
	private boolean oneShot = false;

	//Cache config option
	private boolean moveWhenMouseStationary = false;
	boolean doCentralised = true;
	boolean expanding = false;

	public String actionName = "DWELL";
	private int labelOffset = 0;

	private final Map<TargetBlock, DwellState> liveTargets = new HashMap<>();

	public DwellAction(String name) {
		this.actionName = name;
	}

	public DwellAction(String name, int labelOffset) {
		this.actionName = name;
		this.labelOffset = labelOffset;
	}

	public void onInitializeClient() {
		ClientGuiEvent.RENDER_HUD.register(this::onRenderGameOverlayEvent);
		BlockOutlineEvent.OUTLINE.register(this::onBlockOutlineRender);
	}

	protected void setDwelling(boolean isDwelling) {
		mDwelling = isDwelling;
		if (!isDwelling) {
			this.liveTargets.clear();
		}
	}

	protected void dwellOnce() {
		oneShot = true;
		setDwelling(true);
		showLabel = false;
	}

	@Override
	public void syncConfig() {
		this.dwellTimeComplete = (int) (1000 * EyeMineConfig.getDwellTimeSeconds());
		this.dwellTimeInit = (int) (1000 * EyeMineConfig.getDwellLockonTimeSeconds());
		this.dwellTimeDecay = (int) (this.dwellTimeComplete / 3.5);

		//Cache values
		this.moveWhenMouseStationary = EyeMineConfig.getMoveWhenMouseStationary();
		this.doCentralised = !EyeMineConfig.getDwellShowWithTransparency();
		this.expanding = EyeMineConfig.getDwellShowExpanding();
	}

	// Subclasses define action to be performed on dwell.
	public abstract void performAction(TargetBlock block);

	public void onClientTick(Minecraft minecraft) {
		LocalPlayer player = minecraft.player;
		if (player != null) {
			long time = System.currentTimeMillis();
			long dt = time - lastTime;
			lastTime = time;

			if (mDwelling && !ModUtils.hasActiveGui()) {
				if (MouseHandlerMod.hasPendingEvent() || moveWhenMouseStationary) {
					// What are we currently targeting?
					BlockHitResult rayTraceBlock = ModUtils.getMouseOverBlock();
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
						System.out.println("Performing");
						this.performAction(currentTarget);
						liveTargets.remove(currentTarget);
						if (oneShot) {
							setDwelling(false);
							showLabel = true;
						}
					}
					// TODO: what if can't use item ? Currently this results in flashing again and again
					// Add this to dwell state?
				}
			}
		}
	}

	private void renderCentralisedDwell(PoseStack poseStack, VertexConsumer vertexConsumer, TargetBlock target, DwellState dwellState, boolean expanding) {
		Color color = new Color(0.75f, 0.25f, 0.0f);

		// size of square proportional to dwell progress
		double dDwell = dwellState.getDwellProportionSinceLockon();
		if (expanding) {
			dDwell = 1.0f - dDwell;
		}

		// opacity proportional to decay progress
		int usualOpacity = 125;
		int iOpacity = (int) (usualOpacity * (0.25F + 0.75F * (1.0f - dwellState.getDecayProportion())));

		AbstractRenderer.renderBlockFaceCentralisedDwell(poseStack, vertexConsumer, target.pos, target.direction, color, dDwell, iOpacity);
	}

	private void renderOpacityDwell(PoseStack poseStack, VertexConsumer vertexConsumer, TargetBlock target, DwellState dwellState) {
		Color color = new Color(0.75f, 0.25f, 0.0f);
		double maxAlpha = 0.85D * 255.0D;

		// Opacity increases with dwell amount
		double dAlpha = maxAlpha * (dwellState.getDwellProportion());
		int iAlpha = (int) dAlpha;

		AbstractRenderer.renderBlockFace(poseStack, vertexConsumer, target.pos, target.direction, color, iAlpha);
	}

	public EventResult onBlockOutlineRender(MultiBufferSource bufferSource, PoseStack poseStack) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.screen != null) {
			liveTargets.clear();
			return EventResult.pass();
		}

		if (mDwelling) {
			// Add current block to live targets if required
			// (we only want to add from within this method, so we avoid un-buildable surfaces,
			// a.k.a. "MISS" ray trace results)
			if (minecraft.hitResult != null && minecraft.hitResult.getType() == Type.MISS) {
				return EventResult.pass();
			}

			BlockHitResult rayTraceBlock = ModUtils.getMouseOverBlock();
			TargetBlock currentTarget = (rayTraceBlock == null) ? null : new TargetBlock(rayTraceBlock);
			if (currentTarget != null && !liveTargets.containsKey(currentTarget)) {
				liveTargets.put(currentTarget, new DwellState(dwellTimeComplete, dwellTimeInit, dwellTimeDecay));
			}

			// Update dwell visualisation for all targets
			for (Map.Entry<TargetBlock, DwellState> entry : liveTargets.entrySet()) {
				DwellState dwellState = entry.getValue();
				if (dwellState.shouldRender()) {
					TargetBlock target = entry.getKey();

					poseStack.pushPose();

					RenderType dwellType = EyeMineRenderType.dwellRenderType();
					VertexConsumer vertexConsumer = bufferSource.getBuffer(dwellType);

					if (doCentralised) {
						this.renderCentralisedDwell(poseStack, vertexConsumer, target, dwellState, expanding);
					} else {
						this.renderOpacityDwell(poseStack, vertexConsumer, target, dwellState);
					}

					if (bufferSource instanceof MultiBufferSource.BufferSource) {
						((MultiBufferSource.BufferSource) bufferSource).endBatch(dwellType);
					}
					poseStack.popPose();
				}
			}
		}
		return EventResult.pass();
	}

	public void onRenderGameOverlayEvent(GuiGraphics guiGraphics, float partialTicks) {
		// If dwell is on, show a warning message
		if (mDwelling && showLabel) {
			Minecraft minecraft = Minecraft.getInstance();
			final String msg = actionName;

			float w = (float) minecraft.getWindow().getGuiScaledWidth();
			float h = (float) minecraft.getWindow().getGuiScaledHeight();

			final Font font = minecraft.font;
			float msgWidth = (float) font.width(msg);

			guiGraphics.drawString(font, msg, (int) (w / 2.0f - msgWidth / 2.0f), (int)(h / 2.0f - 20 - labelOffset), 0xffFFFFFF);
		}
	}
}
