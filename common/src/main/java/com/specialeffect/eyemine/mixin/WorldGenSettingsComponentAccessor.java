package com.specialeffect.eyemine.mixin;

import net.minecraft.client.gui.screens.worldselection.WorldGenSettingsComponent;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldGenSettingsComponent.class)
public interface WorldGenSettingsComponentAccessor {
	@Accessor("settings")
	WorldGenSettings getSettings();
}
