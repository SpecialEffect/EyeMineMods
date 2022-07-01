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
* This class control autoAim
* 
* @author Kirsty McNaught and Becky Tyler
* @version 1.0
*/
package com.specialeffect.eyemine.submod.survival;

import com.specialeffect.eyemine.client.Keybindings;
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.utils.ModUtils;
import java.util.function.Predicate;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;

import org.lwjgl.glfw.GLFW;

import me.shedaniel.architectury.event.events.client.ClientRawInputEvent;
import me.shedaniel.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import me.shedaniel.architectury.event.events.EntityEvent;

public class AutoAim extends SubMod {
    public final String MODID = "autoAim"; // this needs to be a unique ID

    // Member variables we need
    // - a static KeyMapping for the shortcut key
    public static KeyMapping mAutoAimKB;

    private LivingEntity nearEntity;
    private LivingEntity targetEntity = null;

    /**
     * We register the keybinding, register the key input event, register the living
     * attack event, and
     * register the client tick event
     */
    public void onInitializeClient() {

        // Register the key binding here
        Keybindings.keybindings.add(mAutoAimKB = new KeyMapping(
                "Auto Aim", // this needs to be a unique name
                Type.KEYSYM, // this is always KEYSYM
                GLFW.GLFW_KEY_O, // this selects the default key. try autocompleting GLFW.GLFW_KEY... to see more
                                 // options
                "category.eyemine.category.eyegaze_common" // this sets the translation key for the name of the category
                                                           // in the controls list
                                                           // (we use eyegaze_common, eyegaze_extra and eyegaze_settings
                                                           // depending on the mod)
        ));

        // by adding to Keybindings.keybindings and
        // registering function with ClientRawInputEvent.Key_PRESSED
        // (look at PickBlock class for reference)
        ClientRawInputEvent.KEY_PRESSED.register(this::onKeyInput);

        // Register the LivingHurt and LivingAttack events
        EntityEvent.LIVING_ATTACK.register(this::onLivingAttack);

        ClientTickEvent.CLIENT_PRE.register(this::onClientTick);

    }

    /**
     * This method cheques weather the player is being attacked
     * 
     * @param entity       The entity that was attacked
     * @param damageSource The source of the damage.
     * @param amount       The amount of damage that was dealt
     * @return The return value is an InteractionResult.
     */
    public InteractionResult onLivingAttack(LivingEntity entity, DamageSource damageSource, float amount) {

        // If entity is a LocalPlayer then it is us being attacked
        // The damage source is sometimes "DamageSource (generic)" or Damage Source
        // (magic) or sometimes an
        // EntityDamageSource like "Arrow" which has a position we can query
        // The number is probably an amount of damage
        if (entity instanceof LocalPlayer) {
            System.out.println(entity + " " + damageSource + " " + amount);
            LocalPlayer player = (LocalPlayer) entity;
            this.findClosestEntity(player);
            nearEntity = this.findClosestEntity(player);
            Vec3 pos = nearEntity.position();

        }
        return null;
    }

    /**
     * Find the closest entity to the player that is not the player
     * 
     * @param player The player that is using the aimbot
     * @return The closest entity to the player.
     */
    private LivingEntity findClosestEntity(Player player) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;

        Predicate<LivingEntity> predicate = i -> !(i.equals(player));
        TargetingConditions conditions = new TargetingConditions();
        conditions.selector(predicate);

        return level.getNearestEntity(LivingEntity.class, conditions, player,
                player.getX(), player.getY(), player.getZ(),
                player.getBoundingBox().inflate(10));

    }

    /**
     * If the key is pressed, find the closest entity to the player and set it as
     * the target entity
     * 
     * @param minecraft The Minecraft instance
     * @param keyCode   The key code of the key that was pressed.
     * @param scanCode  The scan code of the key that was pressed.
     * @param action    0 = key down, 1 = key up, 2 = repeat
     * @param modifiers
     * @return The InteractionResult.PASS is being returned.
     */
    private InteractionResult onKeyInput(Minecraft minecraft, int keyCode, int scanCode, int action, int modifiers) {
        // This method gets called when *any* key is pressed

        // Skip if there is a GUI visible
        if (ModUtils.hasActiveGui()) {
            return InteractionResult.PASS;
        }

        // Skip if F3 is held down (this is used for debugging)
        if (InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 292)) {
            return InteractionResult.PASS;
        }

        // If statement for when the key pressed is the one we are using.
        // Inside the if statement we will need to
        // - turn counting on or off
        // - empty the list of blocks
        if (mAutoAimKB.matches(keyCode, scanCode) && mAutoAimKB.consumeClick()) {
            ModUtils.sendPlayerMessage("Key pressed: " + keyCode);

            nearEntity = this.findClosestEntity(minecraft.player);
            targetEntity = nearEntity;

        }
        return InteractionResult.PASS;
    }

    /**
     * It finds the closest entity to the player, and then turns the player to face
     * it
     * 
     * @param event The event that was fired.
     */
    public void onClientTick(Minecraft event) {

        if (targetEntity == null) {
            System.out.println("can't find entitys");
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        float playerYaw = player.yRot;
        Vec3 playerPos = player.position();

        Vec3 entityPos = targetEntity.position();

        Vec3 targetDirection = playerPos.subtract(entityPos);
        double xDiff = targetDirection.x;
        double zDiff = targetDirection.z;
        double targetYaw = Math.atan2(zDiff, xDiff); // in radians
        targetYaw = targetYaw * 360 / (2 * Math.PI) + 90;
        double turnYaw = targetYaw - playerYaw; // possibly out by 90 degrees??

        System.out.println(
                targetEntity.getType().getDescriptionId() + " " + entityPos + "(player at " + playerPos + ")");
        System.out.println(playerYaw + " " + targetYaw + " " + turnYaw);

        System.out.println(findClosestEntity(minecraft.player));
        minecraft.player.turn(turnYaw, 0.0);

        // Turn off once we are facing entity
        if (Math.abs(turnYaw) < 1.0) {
            targetEntity = null;
        }

    }
}
