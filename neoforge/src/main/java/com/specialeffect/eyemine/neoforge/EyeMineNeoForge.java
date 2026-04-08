package com.specialeffect.eyemine.neoforge;

import com.inventory.config.InventoryConfig;
import com.specialeffect.eyemine.EyeMine;
import com.specialeffect.eyemine.EyeMineClient;
import com.specialeffect.eyemine.client.Keybindings;
import com.specialeffect.eyemine.config.EyeMineConfig;
import com.specialeffect.eyemine.client.neoforge.ClientHandler;
import com.specialeffect.eyemine.platform.neoforge.NeoForgeNetworkService;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(EyeMine.MOD_ID)
public class EyeMineNeoForge {
    public EyeMineNeoForge(IEventBus eventBus, ModContainer container, Dist dist) {
        EyeMine.init();

        eventBus.addListener(this::registerPayloads);

        if (dist.isClient()) {
            container.registerConfig(ModConfig.Type.CLIENT, EyeMineConfig.CLIENT_CONFIG, "eyemine-config.toml");
            eventBus.register(EyeMineConfig.class);
            container.registerConfig(ModConfig.Type.CLIENT, InventoryConfig.CLIENT_CONFIG, "eyemine-inventory-config.toml");
            eventBus.register(InventoryConfig.class);

            eventBus.addListener(this::registerKeyMappings);
            eventBus.addListener(ClientHandler::onSetup);

            EyeMineClient.init();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(EyeMine.MOD_ID);
        for (var entry : NeoForgeNetworkService.getPendingRegistrations().values()) {
            registrar.playToServer(
                    entry.type(),
                    (net.minecraft.network.codec.StreamCodec) entry.codec(),
                    (payload, context) -> entry.handlePayload(payload, context)
            );
        }
    }

    private void registerKeyMappings(RegisterKeyMappingsEvent event) {
        for (KeyMapping keyBinding : Keybindings.keybindings) {
            event.register(keyBinding);
        }
    }
}
