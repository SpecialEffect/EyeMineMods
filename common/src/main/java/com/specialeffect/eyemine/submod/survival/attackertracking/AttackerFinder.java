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

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;

/**
 * Interface for modular attacker detection heuristics.
 *
 * Each implementation represents a different strategy for finding
 * who attacked the player. Implementations should be self-contained
 * and stateless where possible.
 */
public interface AttackerFinder {

    /**
     * Get a human-readable name for this heuristic (for debugging/logging).
     */
    String getName();

    /**
     * Attempt to find the entity that attacked the player.
     *
     * @param player The local player who was attacked
     * @param context Additional context about the attack (damage amount, etc.)
     * @return The suspected attacker, or null if this heuristic can't determine one
     */
    LivingEntity findAttacker(LocalPlayer player, AttackContext context);

    /**
     * Whether this heuristic is currently enabled.
     * Can be used to easily toggle heuristics on/off for testing.
     */
    default boolean isEnabled() {
        return true;
    }
}
