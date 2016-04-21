package com.specialeffect.mods;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.lwjgl.input.Keyboard;

import com.specialeffect.utils.ModUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = MineOne.MODID, version = ModUtils.VERSION, name = MineOne.NAME)
public class MineOne {
	public static final String MODID = "specialeffect.autodestroy";
	public static final String NAME = "AutoDestroy";

	private boolean mDestroying = false;
	private BlockPos mBlockToDestroy;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);

		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Add key binding to start/stop continuously attacking.");
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Subscribe to event buses
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);
		
		// Register key bindings	
		mDestroyKB = new KeyBinding("Auto-Destroy", Keyboard.KEY_T, "SpecialEffect");
		ClientRegistry.registerKeyBinding(mDestroyKB);
	}
	
	private static KeyBinding mDestroyKB;
	
	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (event.entityLiving instanceof EntityPlayer) {
			
			if (mDestroying) {

				// Select the best tool from the inventory
				World world = Minecraft.getMinecraft().theWorld;
	    		EntityPlayer player = (EntityPlayer)event.entityLiving;
				
	    		// Not currently using -> separate out to another key binding?
	    		//chooseBestTool(player.inventory, mBlockToDestroy);
				
				// Check selected item can actually destroy block (only in survival)
	    		if (!player.capabilities.isCreativeMode) {
	    			Block blockIn = world.getBlockState(mBlockToDestroy).getBlock();
	    			if (!ForgeHooks.canHarvestBlock(blockIn, player, world, mBlockToDestroy)) {
	    				System.out.println("Can't destroy this block with current item");
	    				this.stopDestroying();
	    				return;
	    			}
	    		}
				
				// Stop attacking if we're not pointing at the block any more
				// (which means either we've destroyed it, or moved away)
				MovingObjectPosition mov = Minecraft.getMinecraft().objectMouseOver;
				boolean blockDestroyed = (world.getBlockState(mBlockToDestroy).getBlock() instanceof BlockAir);
				boolean movedAway =  false;		
				BlockPos pos = this.getMouseOverBlockPos();
				if (pos != null) {
					movedAway = mBlockToDestroy.distanceSq(pos.getX(), pos.getY(), pos.getZ()) > 0;
				}
				
				if (mov == null || blockDestroyed || movedAway) {
					this.stopDestroying();
				}
			}
		}
	}
	
	private void stopDestroying() {
		final KeyBinding attackBinding = 
				Minecraft.getMinecraft().gameSettings.keyBindAttack;
		KeyBinding.setKeyBindState(attackBinding.getKeyCode(), false);
		mDestroying = false;
	}
	
	// Return the position of the block that the mouse is pointing at.
	// May be null, if pointing at something other than a block.
	private BlockPos getMouseOverBlockPos() {
		BlockPos pos = null;
		MovingObjectPosition mov = Minecraft.getMinecraft().objectMouseOver;
		if (mov != null) {
			pos = mov.getBlockPos(); // may still be null if there's an entity there
		}
		return pos;
	}
	
	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		if(mDestroyKB.isPressed()) {
			mBlockToDestroy = this.getMouseOverBlockPos();
			if (mBlockToDestroy == null) {
				System.out.println("Nothing to attack");
				return;
			}
			else {
				mDestroying = true;

				// Start attacking
				final KeyBinding attackBinding = 
						Minecraft.getMinecraft().gameSettings.keyBindAttack;
				KeyBinding.setKeyBindState(attackBinding.getKeyCode(), true);
			}
		}
	}
	
	private void chooseBestTool(InventoryPlayer inventory, BlockPos blockPos) {
		ItemStack[] items = inventory.mainInventory;
		World world = Minecraft.getMinecraft().theWorld;
        Block block = world.getBlockState(blockPos).getBlock();
        IBlockState state = world.getBlockState(blockPos);
        state = state.getBlock().getActualState(state, world, blockPos);
        
		ItemStack bestItem = null;
		int bestId = 0;
		int bestToolLevel = -1;
        String toolType = block.getHarvestTool(state);
        
        // We'll swap the best item into slot one, and select it.
        ItemStack oldItem0 = items[0];

		for(int i = 0; i < items.length; i++){
			ItemStack stack = items[i];
			if (stack != null) {
		        int toolLevel = stack.getItem().getHarvestLevel(stack, toolType);
		        if (toolLevel > bestToolLevel) {
		        	bestToolLevel = toolLevel;
		        	bestItem = stack;
		        	bestId = i;
		        }
			}
		}
		
		// Swap bestItem into slot 0 and select it
		if (bestId != 0) {
			inventory.setInventorySlotContents(bestId, oldItem0);
			inventory.setInventorySlotContents(0, bestItem);
		}
		inventory.currentItem = 0;
	}
}
