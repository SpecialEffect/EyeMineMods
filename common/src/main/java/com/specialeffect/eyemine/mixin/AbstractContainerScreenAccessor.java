package com.specialeffect.eyemine.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
	@Accessor("leftPos")
	int getLeftPos();

	@Accessor("topPos")
	int getTopPos();

	@Accessor("imageWidth")
	int getXSize();

	@Accessor("imageHeight")
	int getYSize();
}
