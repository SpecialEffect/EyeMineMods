package com.specialeffect.inventory;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import com.specialeffect.mods.mousehandling.MouseHelperOwn;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.Slot;

/**
 * Manages a Inventory GUI Inventory.
 */
public class ChestInventoryManager {
	
	// Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

	private static ChestInventoryManager instance = null;
    
	private final int NUM_COLS = 9;	
	private int num_rows = 1; // populated from container

	/**
	 * Creates a new Inventory Manager with the given container.
	 *
	 * @param container The container from a crafting GUI
	 */
	private ChestInventoryManager() {
	}

	/**
	 * Returns a Inventory Manager Instance operating on the given container
	 * @param chestContainer 
	 *
	 * @param container A container from a GUI
	 * @return manager-singleton
	 */
	public static ChestInventoryManager getInstance(int left, int top, 
													   int xSize, int ySize, 
													   ChestContainer chestContainer) {
		if (instance == null) {
			instance = new ChestInventoryManager();
		} 
		instance.updateCoordinates(left, top, xSize, ySize, chestContainer);
		return instance;
	}
		
	
	private int guiLeft = 0;
	private int guiTop = 0;
	
	// container coords start from centre of top left slot
	private int containerTop = 0;
	private int containerLeft = 0;
	
	private int itemWidth = 0; // width between centres of consecutive items
		
	private float xScale = 1.0f;
	private float yScale = 1.0f;
	
	
	ChestContainer chestContainer;


	private void updateCoordinates(int left, int top, int width, int height, ChestContainer chestContainer) {
				
		this.chestContainer = chestContainer;	
		this.guiLeft = left;
		this.guiTop = top;

		// Get positions straight from container
		// SLOTS in container are: 
		// {nRows}x9: chest slots
		// 3x9: inventory slots
		// 1x9: hotbar
				
		num_rows = chestContainer.getNumRows();
		
		int w = chestContainer.getSlot(1).xPos - chestContainer.getSlot(0).xPos;
		int x = chestContainer.getSlot(0).xPos;
		int y = chestContainer.getSlot(0).yPos;		
		
		this.itemWidth = w;
		this.containerLeft = left + x + w/2;
		this.containerTop = top + y + w/2;
		
		// Sizes need scaling before turning into click locations
		Minecraft mc = Minecraft.getInstance();
		this.xScale = (float) (mc.mainWindow.getWidth())/(float)mc.mainWindow.getScaledWidth();
		this.yScale = (float) (mc.mainWindow.getHeight())/(float)mc.mainWindow.getScaledHeight();						
	}
	
		
	public void clickItem() {
		int yPos = containerTop + itemRow*itemWidth;
		int xPos = containerLeft + itemCol*itemWidth;		
		
		MouseHelperOwn helper = (MouseHelperOwn)Minecraft.getInstance().mouseHelper;
		helper.leftMouseClickAtPosition(xPos*this.xScale, yPos*this.yScale);
				
	}
	
	public void shiftClickItem() {
		int yPos = containerTop + itemRow*itemWidth;
		int xPos = containerLeft + itemCol*itemWidth;		
		
		MouseHelperOwn helper = (MouseHelperOwn)Minecraft.getInstance().mouseHelper;
		helper.moveCursor(xPos*this.xScale, yPos*this.yScale);
		
		int idx = findSlot(xPos, yPos);
		if (idx > -1) {
			LOGGER.debug("taking slot "+idx);
			chestContainer.transferStackInSlot(Minecraft.getInstance().player, idx);
		}
		else {
			LOGGER.debug("No slot found at ("+xPos+", "+yPos+")");
		}
		
	}
	
	private int findSlot(int x, int y) {
		List<Slot> slots = chestContainer.inventorySlots;
		int w = this.itemWidth;
		for (int i = 0; i < slots.size(); i++) {
			Slot slot = slots.get(i);		
			if (x > guiLeft + slot.xPos &&
				x < guiLeft + slot.xPos + w &&
				y > guiTop + slot.yPos &&
				y < guiTop + slot.yPos + w ){
				return i;
			}
		}
		return -1;		
	}

	public void acceptKey(int key) {

		// Handle key press
		// First 5 tabs on top (not inc search which has it's own key already)
		if (key == InventoryConfig.key0.get()) {
			this.clickItem();	
		} else if (key == InventoryConfig.key1.get()) {
			this.shiftClickItem();
		} 
			
			
		if (key == InventoryConfig.keyNextItemCol.get()) {
			itemCol++;
			itemCol %= NUM_COLS;
			// first position on a page starts at -1, -1
			itemRow = Math.max(itemRow, 0);
			this.hoverItem();
		}
		else if (key == InventoryConfig.keyNextItemRow.get()) {
			itemRow++;
			itemRow %= num_rows;
			// first position on a page starts at -1, -1
			itemCol = Math.max(itemCol, 0); 
			this.hoverItem();
		} 
	}
	
	private int itemRow = -1;
	private int itemCol = -1;
	
	private void scrollDown(int amount) {		
		MouseHelperOwn helper = (MouseHelperOwn)Minecraft.getInstance().mouseHelper;
		helper.scroll(amount);				
	}
	
	private void hoverItem() {		
		int yPos = containerTop + itemRow*itemWidth;
		int xPos = containerLeft + itemCol*itemWidth;
		
		GLFW.glfwSetCursorPos(Minecraft.getInstance().mainWindow.getHandle(), xPos*this.xScale, yPos*this.yScale);		
	}
}