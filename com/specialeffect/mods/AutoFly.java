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
	private static KeyBinding mFlyManualKB;
	private static KeyBinding mFlyAutoKB;
	private static KeyBinding mFlyUpKB;

	private static int mFlyHeightManual = 2;
	private static int mFlyHeightAuto = 6;
	public static SimpleNetworkWrapper network;

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
		mFlyManualKB = new KeyBinding("Fly (manual)", Keyboard.KEY_F, "SpecialEffect");
		mFlyAutoKB = new KeyBinding("Fly (auto)", Keyboard.KEY_G, "SpecialEffect");
		mFlyUpKB = new KeyBinding("Fly higher", Keyboard.KEY_UP, "SpecialEffect");
		ClientRegistry.registerKeyBinding(mFlyManualKB);
		ClientRegistry.registerKeyBinding(mFlyAutoKB);
		ClientRegistry.registerKeyBinding(mFlyUpKB);

		// Register an icon for the overlay
		mIconIndexAuto = StateOverlay.registerTextureLeft("specialeffect:icons/fly-auto.png");
		mIconIndexManual = StateOverlay.registerTextureLeft("specialeffect:icons/fly.png");
	}

	private int mIconIndexAuto;
	private int mIconIndexManual;
	
	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if (eventArgs.modID.equals(this.MODID)) {
			syncConfig();
		}
	}

	public static void syncConfig() {
		mFlyHeightManual = mConfig.getInt("Fly height manual", Configuration.CATEGORY_GENERAL, mFlyHeightManual, 1, 20, "How high to fly in manual mode");
		mFlyHeightAuto = mConfig.getInt("Fly height auto", Configuration.CATEGORY_GENERAL, mFlyHeightAuto, 1, 20, "How high to auto mode");
		if (mConfig.hasChanged()) {
			mConfig.save();
		}
	}

	@SubscribeEvent
	public void onLiving(LivingUpdateEvent event) {
		if (event.entityLiving instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.entityLiving;

			// If auto flying, and about to bump into something, fly more!
			if (mIsFlyingAuto && player.capabilities.allowFlying && player.capabilities.isFlying) {
				BlockPos playerPos = player.getPosition();
				Vec3 lookVec = player.getLookVec();

				// Check all three blocks ahead of player
				for (int yDiff = -1; yDiff < 2; yDiff++) {
					BlockPos blockPosInFrontOfPlayer = new BlockPos(playerPos.getX() + lookVec.xCoord,
							playerPos.getY() + yDiff, playerPos.getZ() + lookVec.zCoord);

					World world = Minecraft.getMinecraft().theWorld;
					Block block = world.getBlockState(blockPosInFrontOfPlayer).getBlock();

					// If there's a block in your way, and you're not already jumping over it...
					if (world.getBlockState(blockPosInFrontOfPlayer).getBlock().getMaterial().blocksMovement() &&
							player.motionY == 0) {
						player.motionY += Math.max(mFlyHeightAuto / 4, 1);
						break; // for yDiff = ...
					}
				}
			}

			this.processQueuedCallbacks(event);
		}
	}
	
	private boolean mIsFlyingManual;
	private boolean mIsFlyingAuto;
	
	private void updateIcons() {
		StateOverlay.setStateLeftIcon(mIconIndexAuto, mIsFlyingAuto);
		StateOverlay.setStateLeftIcon(mIconIndexManual, mIsFlyingManual);
	}
	
	private void stopFlying() {
		mIsFlyingAuto = false;
		mIsFlyingManual = false;
		this.queueOnLivingCallback(new SingleShotOnLivingCallback(new IOnLiving() {
			@Override
			public void onLiving(LivingUpdateEvent event) {
				EntityPlayer player = (EntityPlayer) event.entityLiving;
				player.capabilities.isFlying = false;
				AutoFly.network.sendToServer(new ChangeFlyingStateMessage(false, 0));
			}
		}));
		this.updateIcons();
	}	
	
	private void setFlying(final boolean bFlyUp) {
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
					// start flying
					player.capabilities.isFlying = true;
					int flyHeight = 0;
					if (bFlyUp) {
						if (mIsFlyingAuto) { flyHeight = mFlyHeightAuto; }
						if (mIsFlyingManual) { flyHeight = mFlyHeightManual; }
					}
					player.moveEntity(0, flyHeight, 0);
					AutoFly.network.sendToServer(new ChangeFlyingStateMessage(true, flyHeight));
				}
				else {
					player.addChatComponentMessage(new ChatComponentText(
							"Player unable to fly"));
				}
			}
		}));
	}

	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event) {
		if (mFlyManualKB.isPressed()) {			
			if (mIsFlyingManual) {
				this.queueChatMessage("Fly manual: OFF");
				this.stopFlying();
			}
			else {
				this.queueChatMessage("Fly manual: ON");
				boolean doFlyUp = !mIsFlyingAuto;
				mIsFlyingManual = true;
				mIsFlyingAuto = false;
				this.setFlying(doFlyUp);
			}
			
		} else if (mFlyAutoKB.isPressed()) {
			if (mIsFlyingAuto) {
				this.queueChatMessage("Fly auto: OFF");
				this.stopFlying();
			}
			else {
				this.queueChatMessage("Auto-fly: ON");
				boolean doFlyUp = !mIsFlyingManual;
				mIsFlyingAuto = true;
				mIsFlyingManual = false;		
				this.setFlying(doFlyUp);
			}
		}
		else if (mFlyUpKB.isPressed()) {
			this.setFlying(true);
		}
		this.updateIcons();
	}

}
