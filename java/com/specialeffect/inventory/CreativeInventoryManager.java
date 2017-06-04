package com.specialeffect.inventory;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.KeyEvent;

import com.specialeffect.utils.ModUtils;

import de.skate702.craftingkeys.CraftingKeys;
import de.skate702.craftingkeys.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

/**
 * Manages a Inventory GUI Inventory.
 */
public class CreativeInventoryManager {

	private static CreativeInventoryManager instance = null;

    private Robot robot;

	/**
	 * Creates a new Inventory Manager with the given container.
	 *
	 * @param container The container from a crafting GUI
	 */
	private CreativeInventoryManager() {
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns a Inventory Manager Instance operating on the given container
	 *
	 * @param container A container from a GUI
	 * @return manager-singleton
	 */
	public static CreativeInventoryManager getInstance(int left, int top, 
													   int xSize, int ySize) {
		if (instance == null) {
			instance = new CreativeInventoryManager();
		} 
		instance.updateCoordinates(left, top, xSize, ySize);
		return instance;
	}
	
	private int topRowYPos = 0;
	private int tabWidth = 0;
	private int bottomRowYPos = 0;
	private int leftColXPos = 0;
	
	private float xScale = 1.0f;
	private float yScale = 1.0f;
	
	private void updateCoordinates(int left, int top, int width, int height) {
		int inventoryWidth = width;
		this.tabWidth = (int) (inventoryWidth/6.9);
		this.bottomRowYPos = top - tabWidth/2;
		this.topRowYPos = top + height + tabWidth/2;
		this.leftColXPos = left + tabWidth/2;
		
		// Sizes need scaling before turning into click locations
		Minecraft mc = Minecraft.getMinecraft();
		Point size = ModUtils.getScaledDisplaySize(mc);
		this.xScale = (float) (mc.displayWidth)/(float)size.getX();
		this.yScale = (float) (mc.displayHeight)/(float)size.getY();			
		
	}

	public void acceptKey() {

		// Poll keyboard
		Config.pollKeyPresses();

		// Handle key press
		int xPos = -1;
		int yPos = -1;
		if (Config.isKey0Pressed()) {
			xPos = leftColXPos;
			yPos = topRowYPos;
		} else if (Config.isKey1Pressed()) {
			xPos = leftColXPos+tabWidth;
			yPos = topRowYPos;
		} else if (Config.isKey2Pressed()) {
			xPos = leftColXPos+2*tabWidth;
			yPos = topRowYPos;
		} else if (Config.isKey3Pressed()) {
			xPos = leftColXPos+3*tabWidth;
			yPos = topRowYPos;
		} else if (Config.isKey4Pressed()) {
			xPos = leftColXPos+4*tabWidth;
			yPos = topRowYPos;
		} else if (Config.isKey5Pressed()) {
			xPos = leftColXPos;
			yPos = bottomRowYPos;
		} else if (Config.isKey6Pressed()) {
			xPos = leftColXPos+tabWidth;;
			yPos = bottomRowYPos;
		} else if (Config.isKey7Pressed()) {
			xPos = leftColXPos+2*tabWidth;;
			yPos = bottomRowYPos;
		} else if (Config.isKey8Pressed()) {
			xPos = leftColXPos+3*tabWidth;;
			yPos = bottomRowYPos;
		} else if (Config.isKey9Pressed()) {
			xPos = leftColXPos+4*tabWidth;
			yPos = bottomRowYPos;
		} else if (Config.isKeyTakePressed()) {
			// TODO: Take first item?
		}

		if (xPos > -1) {
			org.lwjgl.input.Mouse.setCursorPosition((int)(xPos*this.xScale),
													(int)(yPos*this.yScale));
			// NB: We use lwjgl to move the mouse because it uses coordinates
			// relative to minecraft window. We use a java robot to click because
			// I don't know how else to.
			robot.mousePress(KeyEvent.BUTTON1_MASK);
			robot.mouseRelease(KeyEvent.BUTTON1_MASK);
		}		
	}
}