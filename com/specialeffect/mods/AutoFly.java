package com.specialeffect.mods;

import java.util.Iterator;
import java.util.Queue;

import org.lwjgl.input.Keyboard;

import com.specialeffect.callbacks.BaseClassWithCallbacks;
import com.specialeffect.callbacks.IOnLiving;
import com.specialeffect.callbacks.SingleShotOnLivingCallback;
import com.specialeffect.messages.ChangeFlyingStateMessage;
import com.specialeffect.utils.ModUtils;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
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

@Mod(modid = AutoFly.MODID, version = AutoFly.VERSION, name = AutoFly.NAME, guiFactory = "com.specialeffect.gui.GuiFactoryAutoFly")

public class AutoFly extends BaseClassWithCallbacks {

	public static final String MODID = "specialeffect.autofly";
	public static final String VERSION = "0.1";
	public static final String NAME = "AutoFly";

	public static Configuration mConfig;
	private static KeyBinding mFlyUpKB;
	private static KeyBinding mStopFlyingKB;
	private static int mFlyHeight = 5;
	public static SimpleNetworkWrapper network;
	private static boolean mDoFlyOver = true;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(this);

		ModUtils.setupModInfo(event, this.MODID, this.VERSION, this.NAME,
				"Add key binding to start/stop flying, and automatically fly over hills.");

		network = NetworkRegistry.INSTANCE.newSimpleChannel(this.NAME);
		network.registerMessage(ChangeFlyingStateMessage.Handler.class, ChangeFlyingStateMessage.class, 1, Side.SERVER);

		// Set up config
		mConfig = new Configuration(event.getSuggestedConfigurationFile());
		this.syncConfig();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Subscribe to event buses
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);

		// Register key bindings
		mFlyUpKB = new KeyBinding("Fly up", Keyboard.KEY_UP, "SpecialEffect");
		mStopFlyingKB = new KeyBinding("Stop flying", Keyboard.KEY_DOWN, "SpecialEffect");
		ClientRegistry.registerKeyBinding(mFlyUpKB);
		ClientRegistry.registerKeyBinding(mStopFlyingKB);
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if (eventArgs.modID.equals(this.MODID)) {
			syncConfig();
		}
	}

	public static void syncConfig() {
		mFlyHeight = mConfig.getInt("Fly height", Configuration.CATEGORY_GENERAL, mFlyHeight, 1, 20, "How high to fly");
		mDoFlyOver = mConfig.getBoolean("Auto fly higher", Configuration.CATEGORY_GENERAL, mDoFlyOver,
				"Automatically fly higher to get over obstacles");
		if (mConfig.hasChanged()) {
			mConfig.save();
		}
	}

	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (event.entityLiving instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.entityLiving;

			// If flying, and about to bump into something, fly more!
			if (mDoFlyOver && player.capabilities.allowFlying && player.capabilities.isFlying) {
				BlockPos playerPos = player.getPosition();
				Vec3 lookVec = player.getLookVec();

				// Check all three blocks ahead of player
				for (int yDiff = -1; yDiff < 2; yDiff++) {
					BlockPos blockPosInFrontOfPlayer = new BlockPos(playerPos.getX() + lookVec.xCoord,
							playerPos.getY() + yDiff, playerPos.getZ() + lookVec.zCoord);

					World world = Minecraft.getMinecraft().theWorld;
					Block block = world.getBlockState(blockPosInFrontOfPlayer).getBlock();

					if (world.getBlockState(blockPosInFrontOfPlayer).getBlock().getMaterial().blocksMovement()) {
						player.motionY += Math.max(mFlyHeight / 4, 1);
						break; // for yDiff = ...
					}
				}
			}

			this.processQueuedCallbacks(event);
		}
	}

	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		if (mFlyUpKB.isPressed()) {

			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
				@Override
				public void onLiving(LivingUpdateEvent event) {

					EntityPlayer player = (EntityPlayer) event.entityLiving;
					if (player.capabilities.allowFlying) {
						// stop sneaking, which prevents flying
						final KeyBinding sneakBinding = 
								Minecraft.getMinecraft().gameSettings.keyBindSneak;
						if (sneakBinding.isKeyDown()) {
							player.addChatComponentMessage(new ChatComponentText(
									"Turning off sneak in order to fly"));
							KeyBinding.setKeyBindState(sneakBinding.getKeyCode(), false);
						}
						// start flying, and fly upward.
						player.capabilities.isFlying = true;
						player.moveEntity(0, mFlyHeight, 0);
						AutoFly.network.sendToServer(new ChangeFlyingStateMessage(true, mFlyHeight));
					}
					else {
						player.addChatComponentMessage(new ChatComponentText(
								"Player unable to fly"));
					}
				}
			}));
		} else if (mStopFlyingKB.isPressed()) {
			this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
				@Override
				public void onLiving(LivingUpdateEvent event) {
					EntityPlayer player = (EntityPlayer) event.entityLiving;
					player.capabilities.isFlying = false;
					AutoFly.network.sendToServer(new ChangeFlyingStateMessage(false, mFlyHeight));
				}
			}));
		}
	}

}
