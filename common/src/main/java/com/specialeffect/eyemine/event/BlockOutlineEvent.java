package com.specialeffect.eyemine.event;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import net.minecraft.client.renderer.MultiBufferSource;

public interface BlockOutlineEvent<T> {
	Event<BlockOutline> OUTLINE = EventFactory.createLoop(new BlockOutlineEvent.BlockOutline[0]);

	public interface BlockOutline {
		EventResult renderOutline(MultiBufferSource bufferSource, PoseStack poseStack);
	}
}
