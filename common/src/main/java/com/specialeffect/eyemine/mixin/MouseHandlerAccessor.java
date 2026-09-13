package com.specialeffect.eyemine.mixin;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MouseHandler.class)
public interface MouseHandlerAccessor {
	@Invoker("onMove")
	void invokeOnMove(long l, double d, double e);

	@Invoker("onScroll")
	void invokeOnScroll(long l, double d, double e);

	@Invoker("onButton")
	void invokeOnButton(long l, MouseButtonInfo buttonInfo, int action);
}
