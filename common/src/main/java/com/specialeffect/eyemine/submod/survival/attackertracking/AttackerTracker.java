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

import com.specialeffect.eyemine.EyeMine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates multiple AttackerFinder heuristics to determine who attacked the player.
 *
 * Heuristics are tried in priority order. The first one to return a non-null
 * result wins. You can easily reorder, enable/disable, or add new heuristics.
 */
public class AttackerTracker {

    // Ordered list of heuristics to try
    private final List<AttackerFinder> heuristics = new ArrayList<>();

    // State tracking for attack detection
    private int lastArrowCount = 0;
    private int lastHurtTime = 0;
    private int ticksSinceHurt = 0;

    // Debug mode - prints which heuristic found the attacker
    private boolean debugMode = false;

    public AttackerTracker() {
        // Default heuristic order - most specific to least specific
        heuristics.add(new ProjectileTracebackFinder());
        heuristics.add(new ApproachingThreatFinder());
        heuristics.add(new NearestHostileFinder());
        heuristics.add(new NearestEntityFinder());
    }

    public List<AttackerFinder> getHeuristics() {
        return heuristics;
    }

    public AttackerFinder getHeuristic(String name) {
        return heuristics.stream()
                .filter(h -> h.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public void setPriority(String heuristicName, int newIndex) {
        AttackerFinder finder = getHeuristic(heuristicName);
        if (finder != null) {
            heuristics.remove(finder);
            heuristics.add(Math.min(newIndex, heuristics.size()), finder);
        }
    }

    public void addHeuristic(AttackerFinder finder, int priority) {
        heuristics.add(Math.min(priority, heuristics.size()), finder);
    }

    public void removeHeuristic(String name) {
        heuristics.removeIf(h -> h.getName().equals(name));
    }

    public void setDebugMode(boolean debug) {
        this.debugMode = debug;
    }

    /**
     * Call this every client tick to update internal state.
     */
    public void tick(LocalPlayer player) {
        if (player == null) {
            return;
        }

        if (player.hurtTime > 0 && lastHurtTime == 0) {
            ticksSinceHurt = 0;
        } else if (player.hurtTime > 0) {
            ticksSinceHurt++;
        }

        lastHurtTime = player.hurtTime;
    }

    /**
     * Attempt to find who attacked the player.
     */
    public LivingEntity findAttacker(LocalPlayer player) {
        if (player == null) {
            return null;
        }

        int currentArrowCount = player.getArrowCount();
        AttackContext context = new AttackContext(
                lastArrowCount,
                currentArrowCount,
                player.hurtTime,
                ticksSinceHurt
        );

        lastArrowCount = currentArrowCount;

        for (AttackerFinder finder : heuristics) {
            if (!finder.isEnabled()) {
                continue;
            }

            LivingEntity attacker = finder.findAttacker(player, context);
            if (attacker != null) {
                if (debugMode) {
                    EyeMine.LOGGER.info("[AutoAim] Attacker found by: {} -> {}",
                            finder.getName(), attacker.getType().getDescriptionId());
                }
                return attacker;
            }
        }

        if (debugMode && context.isFreshHit()) {
            EyeMine.LOGGER.info("[AutoAim] No attacker found by any heuristic");
        }

        return null;
    }

    /**
     * Check if the player was just hit.
     */
    public boolean wasJustHit(LocalPlayer player) {
        return player != null && player.hurtTime > 0 && ticksSinceHurt < 3;
    }
}
