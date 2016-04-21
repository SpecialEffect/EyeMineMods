package com.specialeffect.mods;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Queue;

import org.lwjgl.input.Keyboard;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
import com.specialeffect.messages.ActivateBlockAtPosition;
import com.specialeffect.messages.ChangeFlyingStateMessage;
import com.specialeffect.messages.PickBlockMessage;
import com.specialeffect.messages.UseDoorAtPositionMessage;
import com.specialeffect.utils.ModUtils;
import com.specialeffect.utils.OpenableBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
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
import scala.actors.threadpool.LinkedBlockingQueue;

@Mod(modid = GatherDrops.MODID, 
version = ModUtils.VERSION,
name = GatherDrops.NAME)
public class GatherDrops extends BaseClassWithCallbacks
{

	public static final String MODID = "specialeffect.GatherDrops";
	public static final String NAME = "GatherDrops";

	public static Configuration mConfig;
	private static KeyBinding mGatherKB;
	private static KeyBinding mOpenCraftingTableKB;	

	public static SimpleNetworkWrapper network;

	private static int mRadius = 5;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {    
		FMLCommonHandler.instance().bus().register(this);    	

		ModUtils.setupModInfo(event, this.MODID, this.NAME,
				"Add key binding to gather nearby dropped items.");

		network = NetworkRegistry.INSTANCE.newSimpleChannel(this.NAME);
		network.registerMessage(PickBlockMessage.Handler.class, 
				PickBlockMessage.class, 0, Side.SERVER);

		// Set up config
		mConfig = new Configuration(event.getSuggestedConfigurationFile());
		this.syncConfig();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		// Subscribe to event buses
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);    	

		// Register key bindings	
		mGatherKB = new KeyBinding("Gather items", Keyboard.KEY_X, "SpecialEffect");
		ClientRegistry.registerKeyBinding(mGatherKB);
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if(eventArgs.modID.equals(this.MODID)) {
			syncConfig();
		}
	}

	public static void syncConfig() {
		if (mConfig.hasChanged()) {
			mConfig.save();
		}
	}

	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if(event.entityLiving instanceof EntityPlayer) {
			this.processQueuedCallbacks(event);
		}			
	}

	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		if(mGatherKB.isPressed()) {

			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
				@Override
				public void onLiving(LivingUpdateEvent event) {
					EntityPlayer player = (EntityPlayer) event.entityLiving;
					World world = Minecraft.getMinecraft().theWorld;

					BlockPos playerPos = player.getPosition();
					double dx, dy, dz;
					dx = dy = dz = 5;

					AxisAlignedBB aaBb = new AxisAlignedBB(playerPos.subtract(new Vec3i(dx, dy, dz)), 
							playerPos.add(new Vec3i(dx, dy, dz)));
					ArrayList<EntityItem> items = (ArrayList<EntityItem>)world.getEntitiesWithinAABB(EntityItem.class,aaBb);

					if(items != null && !items.isEmpty()) {
						System.out.println("gathering " + items.size() + " nearby items");
						// Ask server to move items
						for (int i = 0; i < items.size(); i++) {
							GatherDrops.network.sendToServer(
									new PickBlockMessage(items.get(i).getEntityId()));
						}
					}
				}
			}));
		}
	}

}
