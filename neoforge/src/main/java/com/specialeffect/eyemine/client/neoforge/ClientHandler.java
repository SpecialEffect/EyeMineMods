/**
 * Copyright (C) 2016-2020 Kirsty McNaught
 * <p>
 * Developed for SpecialEffect, www.specialeffect.org.uk
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.eyemine.client.neoforge;

import com.specialeffect.eyemine.EyeMine;
import com.specialeffect.eyemine.EyeMineClient;
import com.specialeffect.eyemine.event.EyeMineEvents;
import com.specialeffect.eyemine.event.EventResult;
import com.specialeffect.eyemine.event.ScreenSetResult;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = EyeMine.MOD_ID, value = Dist.CLIENT)
public class ClientHandler {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        for (var listener : EyeMineEvents.CLIENT_TICK.getListeners()) {
            listener.onClientTick(mc);
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        for (var listener : EyeMineEvents.KEY_PRESSED.getListeners()) {
            EventResult result = listener.onKeyPressed(mc, event.getKey(), event.getScanCode(), event.getAction(), event.getModifiers());
            if (result.isPresent()) break;
        }
    }

    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        for (var listener : EyeMineEvents.SCREEN_SET.getListeners()) {
            ScreenSetResult result = listener.onScreenSet(event.getNewScreen());
            if (result.handled()) {
                if (result.screen() != null) {
                    event.setNewScreen(result.screen());
                }
                break;
            }
        }
    }

    @SubscribeEvent
    public static void onRenderHud(RenderGuiLayerEvent.Post event) {
        for (var listener : EyeMineEvents.RENDER_HUD.getListeners()) {
            listener.onRenderHud(event.getGuiGraphics(), event.getPartialTick());
        }
    }

    @SubscribeEvent
    public static void onBlockOutline(net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent event) {
        if (!EyeMineEvents.BLOCK_OUTLINE.getListeners().isEmpty()) {
            // Add a custom renderer that delegates to EyeMine's block outline listeners
            event.addCustomRenderer((outlineState, bufferSource, poseStack, isTranslucent, levelRenderState) -> {
                for (var listener : EyeMineEvents.BLOCK_OUTLINE.getListeners()) {
                    EventResult result = listener.renderOutline(bufferSource, poseStack);
                    if (result.isPresent()) return true;
                }
                return false;
            });
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        for (var listener : EyeMineEvents.ENTITY_DEATH.getListeners()) {
            EventResult result = listener.onDeath(event.getEntity(), event.getSource());
            if (result.isPresent()) break;
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        for (var listener : EyeMineEvents.ENTITY_ADD.getListeners()) {
            EventResult result = listener.onAdd(event.getEntity(), event.getLevel());
            if (result.isPresent()) break;
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        for (var listener : EyeMineEvents.PLAYER_TICK.getListeners()) {
            listener.onPlayerTick(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            for (var listener : EyeMineEvents.WORLD_LOAD.getListeners()) {
                listener.onWorldLoad(serverLevel);
            }
        }
    }

    public static void onSetup(FMLCommonSetupEvent event) {
        EyeMineClient.setupComplete = true;
        EyeMineClient.refresh();
        Minecraft mc = Minecraft.getInstance();
        for (var listener : EyeMineEvents.CLIENT_SETUP.getListeners()) {
            listener.onClientSetup(mc);
        }
    }
}
