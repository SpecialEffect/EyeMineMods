package com.specialeffect.eyemine;

import com.specialeffect.eyemine.config.EyeMineConfig;
import com.specialeffect.eyemine.config.InventoryConfig;
import com.specialeffect.eyemine.event.EyeMineEvents;
import com.specialeffect.eyemine.event.EventResult;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;

public class EyeMineClientFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        AutoConfig.register(EyeMineConfig.class, Toml4jConfigSerializer::new);
        AutoConfig.register(InventoryConfig.class, Toml4jConfigSerializer::new);

        AutoConfig.getConfigHolder(EyeMineConfig.class).registerSaveListener((manager, data) -> {
            EyeMineClient.refresh();
            return InteractionResult.PASS;
        });

        EyeMineClient.init();

        for (KeyMapping keyBinding : com.specialeffect.eyemine.client.Keybindings.keybindings) {
            KeyMappingHelper.registerKeyMapping(keyBinding);
        }

        registerEventForwarders();

        EyeMineClient.setupComplete = true;
        EyeMineClient.refresh();
        for (var listener : EyeMineEvents.CLIENT_SETUP.getListeners()) {
            listener.onClientSetup(Minecraft.getInstance());
        }
    }

    private void registerEventForwarders() {
        // Client tick
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            for (var listener : EyeMineEvents.CLIENT_TICK.getListeners()) {
                listener.onClientTick(client);
            }
        });

        // HUD rendering
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("eyemine", "overlay"),
                (graphics, deltaTracker) -> {
                    for (var listener : EyeMineEvents.RENDER_HUD.getListeners()) {
                        listener.onRenderHud(graphics, deltaTracker);
                    }
                }
        );

        // Block outline rendering
        LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, outlineState) -> {
            if (outlineState != null) {
                for (var listener : EyeMineEvents.BLOCK_OUTLINE.getListeners()) {
                    EventResult result = listener.renderOutline(context.bufferSource(), context.poseStack());
                    if (result.isPresent()) return false;
                }
            }
            return true;
        });

        // Key input forwarded via FabricKeyboardHandlerMixin

        // Screen set forwarded via FabricSetScreenMixin

        // Server-side: Entity death
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
            for (var listener : EyeMineEvents.ENTITY_DEATH.getListeners()) {
                EventResult result = listener.onDeath(entity, source);
                if (result == EventResult.INTERRUPT_FALSE) return false;
            }
            return true;
        });

        // Server-side: World load
        ServerLevelEvents.LOAD.register((server, level) -> {
            for (var listener : EyeMineEvents.WORLD_LOAD.getListeners()) {
                listener.onWorldLoad(level);
            }
        });
    }
}
