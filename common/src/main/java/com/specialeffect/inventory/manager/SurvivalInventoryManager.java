/**
 * Copyright (C) 2016-2020 Kirsty McNaught
 * <p>
 * Developed for SpecialEffect, www.specialeffect.org.uk
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package com.specialeffect.inventory.manager;

import com.specialeffect.eyemine.platform.InventoryConfig;
import com.specialeffect.eyemine.utils.MouseHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Manages a Inventory GUI Inventory.
 */
public class SurvivalInventoryManager {

	// Directly reference a log4j logger.
	private static final Logger LOGGER = LogManager.getLogger();

	private static SurvivalInventoryManager instance = null;

	private final int NUM_TABS = 5;
	private final int NUM_COLS = 9;
	private final int NUM_ROWS = 5;

	/**
	 * Creates a new Inventory Manager with the given container.
	 *
	 * @param /container The container from a crafting GUI
	 */
	private SurvivalInventoryManager() {
	}

	/**
	 * Returns a Inventory Manager Instance operating on the given container
	 * @param playerContainer
	 *
	 * @param playerContainer A container from a GUI
	 * @return manager-singleton
	 */
	public static SurvivalInventoryManager getInstance(int left, int top,
													   int xSize, int ySize,
													   InventoryMenu playerContainer) {
		if (instance == null) {
			instance = new SurvivalInventoryManager();
		}
		instance.updateCoordinates(left, top, xSize, ySize, playerContainer);
		return instance;
	}

	int left = 0;
	int top = 0;

	private int tabHeight = 0; // width between centres of consecutive tabs
	private int itemWidth = 0; // width between centres of consecutive items

	private int topTapYPos = 0;
	private int bottomRowYPos = 0;
	private int topItemYPos = 0;

	private int tabXPos = 0;
	private int leftItemXPos = 0;

	private float xScale = 1.0f;
	private float yScale = 1.0f;

	private int recipeX = 0;
	private int recipeY = 0;

	private int currTab;

	InventoryMenu playerContainer;

	private Slot outputSlot;

	private int slotFirstX = 0;
	private int slotFirstY = 0;
	private int slotDelta = 0;


	private void onTabChanged() {
		// reset to hovering over first item when changing tabs
		itemRow = -1;
		itemCol = -1;
	}

	private void updateCoordinates(int left, int top, int width, int height, InventoryMenu playerContainer) {
		int inventoryWidth = width;
		this.left = left;
		this.top = top;

		this.playerContainer = playerContainer;

		this.tabHeight = (int) (inventoryWidth / 6.9);
		this.itemWidth = (int) (inventoryWidth / 9.5);
		this.topTapYPos = top + tabHeight / 2;
		this.tabXPos = left - inventoryWidth;

		this.topItemYPos = (int) (top + itemWidth * 1.5);
		this.leftItemXPos = (int) (left + itemWidth * 0.9);

		this.recipeX = (int) (inventoryWidth * 0.65);
		this.recipeY = (int) (inventoryWidth * 0.4);

		// Sizes need scaling before turning into click locations
		Minecraft mc = Minecraft.getInstance();
		this.xScale = (float) (mc.getWindow().getWidth()) / (float) mc.getWindow().getGuiScaledWidth();
		this.yScale = (float) (mc.getWindow().getHeight()) / (float) mc.getWindow().getGuiScaledHeight();


		List<Slot> slots = playerContainer.slots;
		int iSlotOutput = playerContainer.getResultSlotIndex();

		for (Slot slot : slots) {

			LOGGER.debug(slot.x + ", " + slot.y);
		}
		this.processSlots();

		int a = 2;

	}

	private void processSlots() {
		// parse the container to get positions and sizes of slots 

		/* slot order for survival inventory is:
		 * - crafting output slot (0)
		 * - 4 crafting input slots (1..4)
		 * - 4 player armour slots (5..8)
		 * - all the other slots (...)
		 * - player shield slot
		 */

		List<Slot> slots = playerContainer.slots;

		this.outputSlot = slots.get(playerContainer.getResultSlotIndex());

		this.slotFirstX = slots.get(10).x;
		this.slotFirstY = slots.get(10).y;
		this.slotDelta = slots.get(10).x - slots.get(9).x;

		// offset by half a slot
		this.slotFirstX += this.slotDelta / 2;
		this.slotFirstY += this.slotDelta / 2;
	} 
	    /*
		
		for (Slot slot : slots) {	
			LOGGER.debug(slot.xPos+ ", "+ slot.yPos);
		}
	  
		// Parse the list of slots to work out the location of things
		List<Slot> slots = playerContainer.inventorySlots;
		int iSlotOutput = playerContainer.getResultSlotIndex();
		for (Slot slot : slots) {
//			if (slot instanceof )
			LOGGER.debug(slot.xPos+ ", "+ slot.yPos);
		}
	}*/

	public void clickRecipeBook() {
		LOGGER.debug("Recipe book");
		int xPos = left + recipeX;
		int yPos = top + recipeY;
		MouseHandler helper = Minecraft.getInstance().mouseHandler;
		MouseHelper.instance().leftMouseClickAtPosition(helper, xPos * this.xScale, yPos * this.yScale);
	}

	public void hoverOutput() {
		LOGGER.debug("output");

		int xPos = left + recipeX;
		int yPos = top + recipeY;
		xPos = xPos + itemWidth * 3;
		yPos = (int) (yPos - itemWidth * 2.2);

		MouseHandler helper = Minecraft.getInstance().mouseHandler;
		MouseHelper.instance().moveCursor(helper, xPos * this.xScale, yPos * this.yScale);


		playerContainer.quickMoveStack(Minecraft.getInstance().player, playerContainer.getResultSlotIndex());

	}

	public void changePage(boolean forward) {
		LOGGER.debug("Page " + forward);

		int yPos = (int) (top + 5.5 * tabHeight);
		int xPos = forward ? xPos = left - 100 : left - 50;

		MouseHandler helper = Minecraft.getInstance().mouseHandler;
		MouseHelper.instance().moveCursor(helper, xPos * this.xScale, yPos * this.yScale);
		MouseHelper.instance().leftMouseClickAtPosition(helper, xPos * this.xScale, yPos * this.yScale);
	}

	public void toggleCraftable() {
		LOGGER.debug("craftable");

		int yPos = (int) (top + tabHeight / 2);
		int xPos = left - 20;

		MouseHandler helper = Minecraft.getInstance().mouseHandler;
		MouseHelper.instance().moveCursor(helper, xPos * this.xScale, yPos * this.yScale);
		MouseHelper.instance().leftMouseClickAtPosition(helper, xPos * this.xScale, yPos * this.yScale);
	}

	public void clickItem() {
		int yPos = topItemYPos;
		int xPos = leftItemXPos;

		MouseHandler helper = Minecraft.getInstance().mouseHandler;
		MouseHelper.instance().leftMouseClickAtPosition(helper, xPos * this.xScale, yPos * this.yScale);

		//GLFW.glfwSetCursorPos(Minecraft.getInstance().mainWindow.getHandle(), xPos*this.xScale, yPos*this.yScale);		
	}

	public void shiftClickItem() {
		int yPos = topItemYPos;
		int xPos = leftItemXPos;

		MouseHandler helper = Minecraft.getInstance().mouseHandler;
		MouseHelper.instance().leftShiftMouseClickAtPosition(helper, xPos * this.xScale, yPos * this.yScale);

	}

	public void acceptKey(int key) {

		// Handle key press
		// 
		LOGGER.debug(InventoryConfig.getKeySurvRecipes());
		LOGGER.debug(key);
		if (key == InventoryConfig.getKeySurvRecipes()) {
			this.clickRecipeBook();
		} else if (key == InventoryConfig.getKeySurvPrevTab()) {
			this.switchToTab(validateTabIdx(currTab - 1));
		} else if (key == InventoryConfig.getKeySurvNextTab()) {
			this.switchToTab(validateTabIdx(currTab + 1));
		} else if (key == InventoryConfig.getkeySurvPrevPage()) {
			this.changePage(true);
		} else if (key == InventoryConfig.getkeySurvNextPage()) {
			this.changePage(false);
		} else if (key == InventoryConfig.getKeySurvCraftable()) {
			this.toggleCraftable();
		} else if (key == InventoryConfig.getKeySurvOutput()) {
			this.hoverOutput();
		}
	}

	private int itemRow = -1;
	private int itemCol = -1;

	private void scrollDown(int amount) {
		MouseHandler helper = Minecraft.getInstance().mouseHandler;
		MouseHelper.instance().scroll(helper, amount);
	}

	private void hoverItem() {
		int yPos = topItemYPos + itemRow * itemWidth;
		int xPos = leftItemXPos + itemCol * itemWidth;

		GLFW.glfwSetCursorPos(Minecraft.getInstance().getWindow().getWindow(), xPos * this.xScale, yPos * this.yScale);
	}

	private void switchToTab(int iTab) {
		LOGGER.debug("switch tab " + iTab);
		// Set up (x, y) for specified tab 
		int xPos = -1;
		int yPos = -1;
		switch (iTab) {
			case -1 -> {
				// this is proxy for "drop by clicking outside inventory"
				xPos = tabXPos - tabHeight;
				yPos = topTapYPos;
			}
			case 0 -> {
				xPos = tabXPos;
				yPos = topTapYPos;
			}
			case 1 -> {
				xPos = tabXPos;
				yPos = topTapYPos + tabHeight;
			}
			case 2 -> {
				xPos = tabXPos;
				yPos = topTapYPos + 2 * tabHeight;
			}
			case 3 -> {
				xPos = tabXPos;
				yPos = topTapYPos + 3 * tabHeight;
			}
			case 4 -> {
				xPos = tabXPos;
				yPos = topTapYPos + 4 * tabHeight;
			}
			case 5 -> {
				xPos = tabXPos;
				yPos = topTapYPos + 6 * tabHeight;
			}
			case 6 -> {
				xPos = tabXPos;
				yPos = bottomRowYPos;
			}
			case 7 -> {
				xPos = tabXPos + tabHeight;
				yPos = bottomRowYPos;
			}
			case 8 -> {
				xPos = tabXPos + 2 * tabHeight;
				yPos = bottomRowYPos;
			}
			case 9 -> {
				xPos = tabXPos + 3 * tabHeight;
				yPos = bottomRowYPos;
			}
			case 10 -> {
				xPos = tabXPos + 4 * tabHeight;
				yPos = bottomRowYPos;
			}
			case 11 -> {
				xPos = tabXPos + 6 * tabHeight;
				yPos = bottomRowYPos;
			}
			default -> LOGGER.debug("Unknown tab requested");
		}

		// Select the tab via a mouse action
		if (xPos > -1) {
			//FIXME: test with eye-gaze mouse emulation, no stray cursor movements interfere
			// Do we need synchronisation in the mouse helper??
			MouseHandler helper = Minecraft.getInstance().mouseHandler;
			MouseHelper.instance().moveCursor(helper, xPos * this.xScale, yPos * this.yScale);
			MouseHelper.instance().leftMouseClickAtPosition(helper, xPos * this.xScale, yPos * this.yScale);

			// we want to trigger 'tabChanged' if user has explicitly selected
			// the same tab again (otherwise this gets missed)
			this.onTabChanged();

			currTab = iTab;
		}
	}

	// Ensure index in range, wrap if necessary
	private int validateTabIdx(int idx) {
		idx += NUM_TABS; // ensure positive
		idx %= NUM_TABS; // modulo into range	
		return idx;
	}

}