package com.specialeffect.eyemine.event;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.architectury.event.Event;
import me.shedaniel.architectury.event.EventFactory;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionResult;

public interface BlockOutlineEvent<T> {
	Event<BlockOutline> OUTLINE = EventFactory.createLoop(new BlockOutlineEvent.BlockOutline[0]);

	public interface BlockOutline {
		InteractionResult renderOutline(MultiBufferSource bufferSource, PoseStack poseStack);
	}
}
