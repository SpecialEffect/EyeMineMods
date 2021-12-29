package com.specialeffect.eyemine;

import com.specialeffect.eyemine.config.EyeMineConfig;
import com.specialeffect.eyemine.event.BlockOutlineEvent;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.world.InteractionResult;

public class EyeMineClientFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        AutoConfig.register(EyeMineConfig.class, Toml4jConfigSerializer::new);
        WorldRenderEvents.BLOCK_OUTLINE.register((blockOutline, blockOutlineContext) -> {
            BlockOutlineEvent.OUTLINE.invoker().renderOutline(blockOutline.consumers(), blockOutline.matrixStack());
            return true;
        });

        AutoConfig.getConfigHolder(EyeMineConfig.class).registerSaveListener((manager, data) -> {
            EyeMineClient.refresh();
            return InteractionResult.PASS;
        });
        EyeMineClient.init();
    }
}