package com.specialeffect.eyemine.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
	@Accessor("fps")
	int getFPS();

	@Accessor("missTime")
	void setMissTime(int missTime);

	@Accessor("missTime")
	int getMissTime();
}
