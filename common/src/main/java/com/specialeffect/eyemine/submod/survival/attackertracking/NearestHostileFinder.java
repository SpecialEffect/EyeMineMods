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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.List;

/**
 * Simple fallback: finds the nearest hostile mob within range.
 */
public class NearestHostileFinder implements AttackerFinder {

    private static final double SEARCH_RADIUS = 16.0;

    private boolean enabled = true;

    @Override
    public String getName() {
        return "NearestHostile";
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
        List<Monster> hostiles = level.getEntitiesOfClass(
                Monster.class,
                searchBox,
                entity -> entity.isAlive()
        );

        if (hostiles.isEmpty()) {
            return null;
        }

        return hostiles.stream()
                .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(player)))
                .orElse(null);
    }
}
