/**
 * Copyright (C) 2016-2020 Kirsty McNaught
 *
 * Developed for SpecialEffect, www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * AutoAim - Automatically aims at entities that attack the player.
 *
 * @author Kirsty McNaught and Becky Tyler
 * @version 2.0
 */
package com.specialeffect.eyemine.submod.survival;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import com.specialeffect.eyemine.EyeMine;
import com.specialeffect.eyemine.client.Keybindings;
import com.specialeffect.eyemine.platform.EyeMineConfig;
import com.specialeffect.eyemine.submod.IConfigListener;
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.eyemine.submod.survival.attackertracking.AttackerTracker;
import com.specialeffect.utils.ModUtils;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class AutoAim extends SubMod implements IConfigListener {
    public final String MODID = "autoAim";

    // Keybindings
    public static KeyMapping mAutoAimKB;
    public static KeyMapping mSpeedUpKB;
    public static KeyMapping mSpeedDownKB;

    // The attacker tracking system with modular heuristics
    private final AttackerTracker attackerTracker = new AttackerTracker();

    // Current target we're aiming at
    private LivingEntity targetEntity = null;

    // Auto-aim state
    private boolean autoAimOnDamage;
    private boolean isAiming = false;
    private int ticksAiming = 0;

    // Aiming parameters - cached from config, updated via syncConfig()
    private double turnSpeedMultiplier;
    private boolean debugMode;
    private double maxTargetDistance;
    private double aimStopAngle;

    // Constants for runtime speed adjustment
    private static final double TURN_SPEED_MIN = 0.1;
    private static final double TURN_SPEED_MAX = 2.0;

    @Override
    public void syncConfig() {
        autoAimOnDamage = EyeMineConfig.getAutoAimOnDamage();
        turnSpeedMultiplier = EyeMineConfig.getAutoAimTurnSpeed();
        debugMode = EyeMineConfig.getAutoAimDebug();
        maxTargetDistance = EyeMineConfig.getAutoAimMaxTargetDistance();
        aimStopAngle = EyeMineConfig.getAutoAimStopAngle();
        attackerTracker.setDebugMode(debugMode);
    }

    @Override
    public void onInitializeClient() {
        Keybindings.keybindings.add(mAutoAimKB = new KeyMapping(
                "key.eyemine.auto_aim",
                Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "category.eyemine.category.eyegaze_common"
        ));

        Keybindings.keybindings.add(mSpeedUpKB = new KeyMapping(
                "key.eyemine.auto_aim_speed_up",
                Type.KEYSYM,
                GLFW.GLFW_KEY_EQUAL,
                "category.eyemine.category.eyegaze_common"
        ));

        Keybindings.keybindings.add(mSpeedDownKB = new KeyMapping(
                "key.eyemine.auto_aim_speed_down",
                Type.KEYSYM,
                GLFW.GLFW_KEY_MINUS,
                "category.eyemine.category.eyegaze_common"
        ));

        ClientRawInputEvent.KEY_PRESSED.register(this::onKeyInput);
        ClientTickEvent.CLIENT_PRE.register(this::onClientTick);
    }

    private EventResult onKeyInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
        if (ModUtils.hasActiveGui()) {
            return EventResult.pass();
        }

        if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_F3)) {
            return EventResult.pass();
        }

        if (mAutoAimKB.matches(keyCode, scanCode) && mAutoAimKB.consumeClick()) {
            LocalPlayer player = minecraft.player;
            if (player == null) {
                return EventResult.pass();
            }

            if (isAiming) {
                stopAiming();
                ModUtils.sendPlayerMessage("Auto-aim cancelled");
            } else {
                LivingEntity target = attackerTracker.findAttacker(player);
                if (target != null) {
                    startAiming(target);
                    ModUtils.sendPlayerMessage("Targeting: " + target.getType().getDescriptionId());
                } else {
                    ModUtils.sendPlayerMessage("No target found");
                }
            }
        }

        if (mSpeedUpKB.matches(keyCode, scanCode) && mSpeedUpKB.consumeClick()) {
            turnSpeedMultiplier = Math.min(turnSpeedMultiplier * 1.5, TURN_SPEED_MAX);
            ModUtils.sendPlayerMessage("Turn speed: " + String.format("%.2f", turnSpeedMultiplier));
        }

        if (mSpeedDownKB.matches(keyCode, scanCode) && mSpeedDownKB.consumeClick()) {
            turnSpeedMultiplier = Math.max(turnSpeedMultiplier / 1.5, TURN_SPEED_MIN);
            ModUtils.sendPlayerMessage("Turn speed: " + String.format("%.2f", turnSpeedMultiplier));
        }

        return EventResult.pass();
    }

    public void onClientTick(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }

        attackerTracker.tick(player);

        if (autoAimOnDamage && attackerTracker.wasJustHit(player)) {
            LivingEntity attacker = attackerTracker.findAttacker(player);
            if (attacker != null && attacker != targetEntity) {
                startAiming(attacker);
            }
        }

        if (isAiming && targetEntity != null) {
            aimAtTarget(player);
        }
    }

    private void startAiming(LivingEntity target) {
        targetEntity = target;
        isAiming = true;
        ticksAiming = 0;
        if (debugMode) {
            EyeMine.LOGGER.info("[AutoAim] Started aiming at: {}", target.getType().getDescriptionId());
        }
    }

    private void stopAiming() {
        targetEntity = null;
        isAiming = false;
    }

    private void aimAtTarget(LocalPlayer player) {
        ticksAiming++;
        if (ticksAiming <= 1) {
            return;
        }

        if (targetEntity == null || !targetEntity.isAlive()) {
            if (debugMode) {
                EyeMine.LOGGER.info("[AutoAim] Target lost or dead, stopping");
            }
            stopAiming();
            return;
        }

        Vec3 playerPos = player.getEyePosition(1.0f);
        Vec3 targetPos = targetEntity.getEyePosition(1.0f);
        Vec3 direction = targetPos.subtract(playerPos);
        double distance = direction.length();

        if (distance > maxTargetDistance) {
            if (debugMode) {
                EyeMine.LOGGER.info("[AutoAim] Target too far ({} > {}), stopping",
                        String.format("%.1f", distance), String.format("%.1f", maxTargetDistance));
            }
            stopAiming();
            return;
        }

        double targetYaw = Math.toDegrees(Math.atan2(-direction.x, direction.z));
        double horizontalDist = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        double targetPitch = Math.toDegrees(Math.atan2(-direction.y, horizontalDist));
        targetPitch = Math.max(-89.0, Math.min(89.0, targetPitch));

        float currentYaw = player.getYRot();
        float currentPitch = player.getXRot();

        double deltaYaw = targetYaw - currentYaw;
        while (deltaYaw > 180) deltaYaw -= 360;
        while (deltaYaw < -180) deltaYaw += 360;

        double deltaPitch = targetPitch - currentPitch;

        if (Math.abs(deltaYaw) < aimStopAngle && Math.abs(deltaPitch) < aimStopAngle) {
            if (debugMode) {
                EyeMine.LOGGER.info("[AutoAim] Aimed at target, stopping");
            }
            stopAiming();
            return;
        }

        double turnYaw = deltaYaw * turnSpeedMultiplier;
        double turnPitch = deltaPitch * turnSpeedMultiplier;

        if (debugMode) {
            EyeMine.LOGGER.info("[AutoAim] Turning: yaw={} pitch={} (multiplier={})",
                    String.format("%.1f", turnYaw), String.format("%.1f", turnPitch),
                    String.format("%.2f", turnSpeedMultiplier));
        }

        // In 1.21 Entity.turn() is removed; set rotation fields directly
        player.setYRot((float) (currentYaw + turnYaw));
        player.setXRot((float) Math.max(-90.0, Math.min(90.0, currentPitch + turnPitch)));
    }

    public AttackerTracker getAttackerTracker() {
        return attackerTracker;
    }
}
