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
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.List;

/**
 * Last resort fallback: finds the nearest living entity of any type.
 */
public class NearestEntityFinder implements AttackerFinder {

    private static final double SEARCH_RADIUS = 10.0;

    private boolean enabled = true;

    @Override
    public String getName() {
        return "NearestEntity";
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
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }

        AABB searchBox = player.getBoundingBox().inflate(SEARCH_RADIUS);
        List<LivingEntity> entities = level.getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> entity != player && entity.isAlive()
        );

        if (entities.isEmpty()) {
            return null;
        }

        return entities.stream()
                .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(player)))
                .orElse(null);
    }
}
