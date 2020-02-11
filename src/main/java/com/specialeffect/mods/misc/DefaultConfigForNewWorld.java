package com.specialeffect.mods.misc;

import com.specialeffect.messages.AddItemToHotbar;
import com.specialeffect.messages.SendCommandMessage;
import com.specialeffect.mods.ChildMod;
import com.specialeffect.utils.ModUtils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.AirItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import net.minecraft.world.GameRules;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;

//@EventBusSubscriber(bus=Bus.FORGE)
public class DefaultConfigForNewWorld extends ChildMod {
    public final String MODID = "specialeffect.defaultconfigworld";

    private boolean firstWorldLoad = false;
    private boolean firstOnLivingTick = true;

	private boolean haveEquippedPlayer = false;   
    
    
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
            // The first time the world loads, we set our preferred game rules
            // Users may override them manually later.
            if (firstWorldLoad) {   
                /* FIXME if (player.isCreative()) {
                    WorldServer worldServer = DimensionManager.getWorld(0); // default world
                    if (worldServer.getTotalWorldTime() < 10) {
                        GameRules gameRules = worldServer.getGameRules();
                        printGameRules(gameRules);
                        setDefaultGameRules(gameRules);
                    }
                }*/
                firstWorldLoad = false;
            }
        }
    }

    @SubscribeEvent
    public void onWorldLoad(FMLServerStartedEvent event) {
        // Note first time world loads, we'll make changes on next
        // onliving tick
    	
    	//FIXME
        firstWorldLoad = true;
//        WorldServer worldServer = DimensionManager.getWorld(0); // default world
//        if (worldServer.getTotalWorldTime() < 10) {
//            firstWorldLoad = true;
//        }
    }

    private void setDefaultGameRules(GameRules rules) {
        /* FIXME

        rules.setOrCreateGameRule("doWeatherCycle", "False");
        rules.setOrCreateGameRule("keepInventory", "True");

        // we've just turned off daylightcycle while time = morning... 
        // we prefer full daylight!
        sendCommand("/time set day");
         */
    }

    private void printGameRules(GameRules rules) {
        System.out.println("Game rules:");        
        /*FIXME String[] keys = rules.getRules();
        for (String key : keys) {
            System.out.println(key + ": " + rules.getString(key));
        }*/
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