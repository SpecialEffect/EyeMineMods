package com.specialeffect.eyemine;

import com.specialeffect.eyemine.config.EyeMineConfig;
import com.specialeffect.eyemine.event.BlockOutlineEvent;
import com.specialeffect.eyemine.submod.IConfigListener;
import com.specialeffect.eyemine.submod.SubMod;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.world.InteractionResult;

public class EyeMineClientFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        AutoConfig.register(EyeMineConfig.class, Toml4jConfigSerializer::new);

        EyeMine.clientInit();

        WorldRenderEvents.BLOCK_OUTLINE.register((blockOutline, blockOutlineContext) -> {
            BlockOutlineEvent.OUTLINE.invoker().renderOutline(blockOutline.consumers(), blockOutline.matrixStack());
            return true;
        });

        AutoConfig.getConfigHolder(EyeMineConfig.class).registerSaveListener((manager, data) -> {
            for(SubMod subMod : EyeMine.subModList) {
                if(subMod instanceof IConfigListener) {
                    ((IConfigListener) subMod).syncConfig();
                }
            }
            return InteractionResult.SUCCESS;
        });
    }
}
