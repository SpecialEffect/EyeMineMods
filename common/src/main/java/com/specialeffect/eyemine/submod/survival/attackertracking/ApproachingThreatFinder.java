/**
 * Copyright (C) 2016-2020 Kirsty McNaught
 *
 * Developed for SpecialEffect, www.specialeffect.org.uk
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */
package com.specialeffect.eyemine.submod.survival.attackertracking;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

/**
 * Finds attackers by detecting entities moving aggressively toward the player.
 */
public class ApproachingThreatFinder implements AttackerFinder {

    private static final double MELEE_RANGE = 5.0;
    private static final double MIN_VELOCITY_ALIGNMENT = 0.3;
    private static final double MIN_APPROACH_SPEED = 0.05;

    private static final double WEIGHT_DISTANCE = 10.0;
    private static final double WEIGHT_VELOCITY_ALIGNMENT = 20.0;
    private static final double WEIGHT_MONSTER = 30.0;
    private static final double WEIGHT_PLAYER = 15.0;
    private static final double WEIGHT_MOB = 5.0;
    private static final double WEIGHT_FACING_PLAYER = 10.0;
    private static final double MIN_LOOK_ALIGNMENT = 0.7;

    private boolean enabled = true;

    @Override
    public String getName() {
        return "ApproachingThreat";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public LivingEntity findAttacker(LocalPlayer player, AttackContext context) {
        if (!context.isFreshHit()) {
            return null;
        }

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }

        Vec3 playerPos = player.position();

        AABB searchBox = player.getBoundingBox().inflate(MELEE_RANGE);
        List<LivingEntity> candidates = level.getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> entity != player && entity.isAlive()
        );

        if (candidates.isEmpty()) {
            return null;
        }

        return candidates.stream()
                .map(entity -> new ScoredEntity(entity, scoreCandidate(entity, playerPos)))
                .filter(scored -> scored.score >= 0)
                .min(Comparator.comparingDouble(scored -> scored.score))
                .map(scored -> scored.entity)
                .orElse(null);
    }

    private double scoreCandidate(LivingEntity entity, Vec3 playerPos) {
        Vec3 entityPos = entity.position();
        Vec3 toPlayer = playerPos.subtract(entityPos);
        double distance = toPlayer.length();

        if (distance < 0.1) {
            return -1;
        }

        Vec3 toPlayerNorm = toPlayer.normalize();
        Vec3 velocity = entity.getDeltaMovement();
        double speed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);

        double score = 0;
        score += distance * WEIGHT_DISTANCE;

        if (speed > MIN_APPROACH_SPEED) {
            double alignment = velocity.normalize().dot(toPlayerNorm);
            if (alignment > MIN_VELOCITY_ALIGNMENT) {
                score -= alignment * WEIGHT_VELOCITY_ALIGNMENT;
            }
        }

        if (entity instanceof Monster) {
            score -= WEIGHT_MONSTER;
        } else if (entity instanceof Player) {
            score -= WEIGHT_PLAYER;
        } else if (entity instanceof Mob) {
            score -= WEIGHT_MOB;
        }

        Vec3 entityLookVec = entity.getLookAngle();
        double lookAlignment = entityLookVec.dot(toPlayerNorm);
        if (lookAlignment > MIN_LOOK_ALIGNMENT) {
            score -= WEIGHT_FACING_PLAYER;
        }

        return score;
    }

    private static class ScoredEntity {
        final LivingEntity entity;
        final double score;

        ScoredEntity(LivingEntity entity, double score) {
            this.entity = entity;
            this.score = score;
        }
    }
}
