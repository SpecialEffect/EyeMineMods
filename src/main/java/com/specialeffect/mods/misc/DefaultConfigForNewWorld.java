package com.specialeffect.mods.misc;

import java.lang.reflect.Field;

import com.specialeffect.messages.AddItemToHotbar;
import com.specialeffect.messages.SendCommandMessage;
import com.specialeffect.mods.ChildMod;
import com.specialeffect.utils.ModUtils;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.AirItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.NonNullList;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.GameRules.BooleanValue;
import net.minecraft.world.IWorld;
import net.minecraft.world.spawner.WorldEntitySpawner;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;

//@EventBusSubscriber(bus=Bus.FORGE)
public class DefaultConfigForNewWorld extends ChildMod {
    public final String MODID = "specialeffect.defaultconfigworld";

    
    private boolean firstOnLivingTick = true;

	private boolean haveEquippedPlayer = false;   
	
	private static boolean alwaysDayTimeSetting = false;
	private static boolean alwaysSunnySetting = false;
	private static boolean keepInventorySetting = false;
    
	public static void setNewWorldOptions(boolean daytime, boolean sunny, boolean keepInventory) {
		// allow "new world" gui to cache user preferences ready for new world creation
		alwaysDayTimeSetting = daytime;
		alwaysSunnySetting = sunny;
		keepInventorySetting = keepInventory;
	}
	
    public void setup(final FMLCommonSetupEvent event) {
        
        this.setupChannel(MODID, 1);
        
        int id = 0;
        channel.registerMessage(id++, AddItemToHotbar.class, AddItemToHotbar::encode, 
        		AddItemToHotbar::decode, AddItemToHotbar.Handler::handle);        

        channel.registerMessage(id++, SendCommandMessage.class, SendCommandMessage::encode, 
        		SendCommandMessage::decode, SendCommandMessage.Handler::handle);        

    }

    @SubscribeEvent
    public void onSpawn(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity != null && entity instanceof PlayerEntity) {
        	PlayerEntity player = (PlayerEntity)entity;
            if (ModUtils.entityIsMe(player)) {
                firstOnLivingTick = true;
            }
        }
    }
    
    @SubscribeEvent
    public void onLiving(LivingUpdateEvent event) {
        if (ModUtils.entityIsMe(event.getEntityLiving())) {
        	PlayerEntity player = (PlayerEntity) event.getEntityLiving();

            // First onliving tick, we check inventory and fill it with default set
            // of items if it's empty
            if (firstOnLivingTick && !haveEquippedPlayer) {
                firstOnLivingTick = false;
            
                if (player.isCreative()) {
                    NonNullList<ItemStack> inventory = player.inventory.mainInventory;
                    boolean hasSomeItems = false;
                    for (ItemStack itemStack : inventory) {
                        if (itemStack != null && !(itemStack.getItem() instanceof AirItem) ) {
                            hasSomeItems = true;
                            break;
                        }
                    }
    
                    if (!hasSomeItems) {
                        equipPlayer(player.inventory);
                    }                    
                }
                haveEquippedPlayer = true;
            }
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        
    	LOGGER.debug("onWorldLoad: " + alwaysDayTimeSetting + ", " + alwaysSunnySetting + ", " + keepInventorySetting);
    	     
        IWorld world = event.getWorld();
        MinecraftServer server = world.getWorld().getServer();
        WorldInfo info = world.getWorldInfo();
        GameRules rules = info.getGameRulesInstance();
		 	   
        if (info.getGameTime() < 10) {
        	// First time loading, set rules according to user preference		
        	if (info.getGameType() == GameType.CREATIVE) {
				rules.get(GameRules.DO_DAYLIGHT_CYCLE).set(!alwaysDayTimeSetting, server);		
				rules.get(GameRules.DO_WEATHER_CYCLE).set(!alwaysSunnySetting, server);				
				rules.get(GameRules.KEEP_INVENTORY).set(keepInventorySetting, server);
			
				// Extra settings as a result of the above
				if (alwaysDayTimeSetting) {
				    // we've just turned off daylightcycle while time = morning... 
			        // we prefer full daylight!
					info.setDayTime(2000);
				}
        	}
        }	         
    }

    private void printGameRules(GameRules rules) {
        System.out.println("Game rules:");        
        
        // We use reflaction to 
        Field[] fields = rules.getClass().getFields();
        for(Field f : fields){        	           
			try {
				Object v = f.get(rules);
				
				if (v instanceof GameRules.RuleKey<?>) {
	        	   GameRules.RuleKey<?> key = (GameRules.RuleKey<?>)v;        	   
	        	   LOGGER.debug(key + ": " + rules.get(key).toString());
		        }    
			} catch (Exception e) {
				e.printStackTrace();
			}        
        }        
    }
    
    private void sendCommand(String cmd ) {
        channel.sendToServer(new SendCommandMessage(cmd));
    }
    
    private void equipPlayer(PlayerInventory inventory) {
        // Ask server to put new item in hotbar     
        channel.sendToServer(new AddItemToHotbar(
                new ItemStack(Blocks.BRICKS)));
        channel.sendToServer(new AddItemToHotbar(
                new ItemStack(Blocks.SANDSTONE)));
        channel.sendToServer(new AddItemToHotbar(
                new ItemStack(Blocks.GLASS_PANE)));
        channel.sendToServer(new AddItemToHotbar(
                new ItemStack(Blocks.MOSSY_COBBLESTONE)));
        
        channel.sendToServer(new AddItemToHotbar(
                new ItemStack(Blocks.TORCH), 6));
        channel.sendToServer(new AddItemToHotbar(
                new ItemStack(Items.DIAMOND_PICKAXE), 7));
        channel.sendToServer(new AddItemToHotbar(
                new ItemStack(Items.DIAMOND_SWORD), 8));
        
        inventory.currentItem = 0;
    }
}