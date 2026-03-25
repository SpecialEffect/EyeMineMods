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

/**
 * Context information about an attack, gathered from client-side data.
 *
 * Since we're client-only, we can't rely on DamageSource from events.
 * Instead, we track observable changes in the player's state.
 */
public class AttackContext {

    /** Player's arrow count before this tick */
    public final int previousArrowCount;

    /** Player's current arrow count */
    public final int currentArrowCount;

    /** Player's hurtTime value (>0 means recently hurt) */
    public final int hurtTime;

    /** Time in ticks since we started tracking this attack */
    public final int ticksSinceHurt;

    public AttackContext(int previousArrowCount, int currentArrowCount, int hurtTime, int ticksSinceHurt) {
        this.previousArrowCount = previousArrowCount;
        this.currentArrowCount = currentArrowCount;
        this.hurtTime = hurtTime;
        this.ticksSinceHurt = ticksSinceHurt;
    }

    /**
     * Did the player just get hit by an arrow?
     */
    public boolean wasArrowHit() {
        return currentArrowCount > previousArrowCount;
    }

    /**
     * Is this a fresh hit (just happened)?
     */
    public boolean isFreshHit() {
        return hurtTime > 0 && ticksSinceHurt < 5;
    }
}
