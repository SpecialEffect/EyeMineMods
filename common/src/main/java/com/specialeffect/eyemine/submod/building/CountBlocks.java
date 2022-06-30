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
* This class control counting blocks
* 
* @author Kirsty McNaught and Becky Tyler
* @version 1.0
*/
package com.specialeffect.eyemine.submod.building;

import com.specialeffect.eyemine.client.Keybindings;
import com.specialeffect.eyemine.submod.SubMod;
import com.specialeffect.utils.ModUtils;
import com.specialeffect.eyemine.mixin.KeyMappingAccessor;

import java.rmi.server.Skeleton;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
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
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import me.shedaniel.architectury.event.events.GuiEvent;
import me.shedaniel.architectury.event.events.EntityEvent.LivingAttack;
import me.shedaniel.architectury.event.events.BlockEvent;
import me.shedaniel.architectury.event.events.EntityEvent;

public class CountBlocks extends SubMod {
    public final String MODID = "countblocks"; // this needs to be a unique ID

    // Member variables we need
    // - a static KeyMapping for the shortcut key
    public static KeyMapping mNumberBlockKB;

    // - a list of BlockPos positions for all the blocks we are tracking
    public ArrayList<BlockPos> blockPosList;

    // - a boolean indicating whether we are currently counting blocks
    public boolean countingBlocks;

    public void onInitializeClient() {

        // Create the list of BlockPos positions
        blockPosList = new ArrayList<BlockPos>();

        // Initialise the countingBlocks flag
        countingBlocks = false;

        // Register the key binding here
        Keybindings.keybindings.add(mNumberBlockKB = new KeyMapping(
                "Number_Block", // this needs to be a unique name
                Type.KEYSYM, // this is always KEYSYM
                GLFW.GLFW_KEY_K, // this selects the default key. try autocompleting GLFW.GLFW_KEY... to see more
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

        // Register the "place block" event
        BlockEvent.PLACE.register(this::onPlaceBlock);

        // Register the LivingHurt and LivingAttack events
        EntityEvent.LIVING_ATTACK.register(this::onLivingAttack);

        ClientTickEvent.CLIENT_PRE.register(this::onClientTick);

    }

    public InteractionResult onLivingAttack(LivingEntity entity, DamageSource damageSource, float amount) {
        LivingEntity nearEntity;
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

            // player.setYBodyRot(90);
            // player.turn(d, e);

        }
        return null;

    }

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

    private LivingEntity targetEntity = null;

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
        if (mNumberBlockKB.matches(keyCode, scanCode) && mNumberBlockKB.consumeClick()) {
            ModUtils.sendPlayerMessage("Key pressed: " + keyCode);

            // Toggle the value of countingBlocks
            countingBlocks = !countingBlocks;

            // Clear the list of BlockPos positions
            blockPosList.clear();

            LivingEntity nearEntity;
            nearEntity = this.findClosestEntity(minecraft.player);
            targetEntity = nearEntity;

        }
        return InteractionResult.PASS;
    }

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

    /**
     * Whenever a block is placed, send a chat message with the position of the
     * block
     * 
     * @param l        The level the block is being placed in
     * @param position The position of the block that was placed
     * @param state    The block state of the block being placed
     * @param entity   The entity that placed the block.
     * @return The return value is an InteractionResult. This is a value that tells
     *         the game what to do
     *         with the block.
     */
    public InteractionResult onPlaceBlock(Level l, BlockPos position, BlockState state, Entity entity) {
        // Add block's position to list of block positions
        blockPosList.add(position);

        // Send a chat message showing position and number of blocks placed
        ModUtils.sendPlayerMessage("Block " + blockPosList.size() + " placed at " + position); // for debugging

        // This method is called whenever a block is placed
        return InteractionResult.PASS;
    }
}
