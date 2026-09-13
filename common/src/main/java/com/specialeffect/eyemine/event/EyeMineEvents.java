package com.specialeffect.eyemine.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class EyeMineEvents {
    public static final EventHolder<ClientTickListener> CLIENT_TICK = new EventHolder<>();
    public static final EventHolder<KeyPressedListener> KEY_PRESSED = new EventHolder<>();
    public static final EventHolder<ScreenSetListener> SCREEN_SET = new EventHolder<>();
    public static final EventHolder<RenderHudListener> RENDER_HUD = new EventHolder<>();
    public static final EventHolder<ClientSetupListener> CLIENT_SETUP = new EventHolder<>();
    public static final EventHolder<EntityDeathListener> ENTITY_DEATH = new EventHolder<>();
    public static final EventHolder<EntityAddListener> ENTITY_ADD = new EventHolder<>();
    public static final EventHolder<WorldLoadListener> WORLD_LOAD = new EventHolder<>();
    public static final EventHolder<PlayerTickListener> PLAYER_TICK = new EventHolder<>();
    public static final EventHolder<BlockOutlineListener> BLOCK_OUTLINE = new EventHolder<>();

    @FunctionalInterface
    public interface ClientTickListener {
        void onClientTick(Minecraft minecraft);
    }

    @FunctionalInterface
    public interface KeyPressedListener {
        EventResult onKeyPressed(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers);
    }

    @FunctionalInterface
    public interface ScreenSetListener {
        ScreenSetResult onScreenSet(Screen screen);
    }

    @FunctionalInterface
    public interface RenderHudListener {
        void onRenderHud(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker);
    }

    @FunctionalInterface
    public interface ClientSetupListener {
        void onClientSetup(Minecraft minecraft);
    }

    @FunctionalInterface
    public interface EntityDeathListener {
        EventResult onDeath(LivingEntity entity, DamageSource source);
    }

    @FunctionalInterface
    public interface EntityAddListener {
        EventResult onAdd(Entity entity, Level level);
    }

    @FunctionalInterface
    public interface WorldLoadListener {
        void onWorldLoad(ServerLevel level);
    }

    @FunctionalInterface
    public interface PlayerTickListener {
        void onPlayerTick(Player player);
    }

    @FunctionalInterface
    public interface BlockOutlineListener {
        EventResult renderOutline(MultiBufferSource bufferSource, PoseStack poseStack);
    }
}
