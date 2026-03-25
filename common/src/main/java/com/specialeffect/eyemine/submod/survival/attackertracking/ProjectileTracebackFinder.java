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
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

/**
 * Finds attackers by tracing back the trajectory of nearby projectiles.
 */
public class ProjectileTracebackFinder implements AttackerFinder {

    private static final double PROJECTILE_SEARCH_RADIUS = 3.0;
    private static final double MAX_SHOOTER_DISTANCE = 60.0;
    private static final double MIN_ALIGNMENT = 0.7;

    private boolean enabled = true;

    @Override
    public String getName() {
        return "ProjectileTraceback";
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

        AABB searchBox = player.getBoundingBox().inflate(PROJECTILE_SEARCH_RADIUS);
        List<Projectile> projectiles = level.getEntitiesOfClass(Projectile.class, searchBox);

        if (projectiles.isEmpty()) {
            return null;
        }

        // Try stopped arrows first (just hit player)
        for (Projectile projectile : projectiles) {
            if (projectile instanceof AbstractArrow) {
                Vec3 vel = projectile.getDeltaMovement();
                if (vel.lengthSqr() > 0.01) {
                    continue;
                }
            }
            LivingEntity shooter = traceBackToShooter(player, projectile, level);
            if (shooter != null) {
                return shooter;
            }
        }

        // Fallback to flying projectiles
        for (Projectile projectile : projectiles) {
            if (projectile instanceof AbstractArrow) {
                Vec3 vel = projectile.getDeltaMovement();
                if (vel.lengthSqr() <= 0.01) {
                    continue;
                }
            }
            LivingEntity shooter = traceBackToShooter(player, projectile, level);
            if (shooter != null) {
                return shooter;
            }
        }

        return null;
    }

    private LivingEntity traceBackToShooter(LocalPlayer player, Projectile projectile, ClientLevel level) {
        Vec3 projectilePos = projectile.position();
        float yaw = projectile.getYRot();
        float pitch = projectile.getXRot();

        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);

        double dx = -Math.sin(yawRad) * Math.cos(pitchRad);
        double dy = -Math.sin(pitchRad);
        double dz = Math.cos(yawRad) * Math.cos(pitchRad);

        Vec3 backDirection = new Vec3(-dx, -dy, -dz).normalize();

        AABB searchBox = player.getBoundingBox().inflate(MAX_SHOOTER_DISTANCE);
        List<LivingEntity> candidates = level.getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> entity != player && entity.isAlive()
        );

        if (candidates.isEmpty()) {
            return null;
        }

        return candidates.stream()
                .map(entity -> new ScoredEntity(entity, scoreCandidate(entity, projectilePos, backDirection)))
                .filter(scored -> scored.score >= 0)
                .min(Comparator.comparingDouble(scored -> scored.score))
                .map(scored -> scored.entity)
                .orElse(null);
    }

    private double scoreCandidate(LivingEntity entity, Vec3 projectilePos, Vec3 backDirection) {
        Vec3 toEntity = entity.getEyePosition(1.0f).subtract(projectilePos);
        double distance = toEntity.length();

        if (distance < 1.0 || distance > MAX_SHOOTER_DISTANCE) {
            return -1;
        }

        double alignment = toEntity.normalize().dot(backDirection);

        if (alignment < MIN_ALIGNMENT) {
            return -1;
        }

        return (1.0 - alignment) * 100 + distance * 0.1;
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
