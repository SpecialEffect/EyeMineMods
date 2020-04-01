package com.specialeffect.gui;

import java.util.Random;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.JsonOps;
import com.specialeffect.mods.EyeGaze;
import com.specialeffect.mods.misc.DefaultConfigForNewWorld;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.CreateWorldScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.FileUtil;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CustomCreateWorldScreen extends Screen {
   private final Screen parentScreen;
   private TextFieldWidget worldNameField;
   private String saveDirName;
   private boolean alreadyGenerated;
   private Button btnCreateWorld;
   private String worldName;
   public CompoundNBT chunkProviderSettingsJson = new CompoundNBT();
   
   // Buttons for EyeMine options
   private BooleanButton btnDaytime;
   private BooleanButton btnSunny;
   private BooleanButton btnInventory;

   public CustomCreateWorldScreen(Screen p_i46320_1_) {
      super(new TranslationTextComponent("selectWorld.create"));
      this.parentScreen = p_i46320_1_;
      this.worldName = I18n.format("selectWorld.newWorld");
   }

   public void tick() {
      this.worldNameField.tick();
   }

   protected void init() {
      this.minecraft.keyboardListener.enableRepeatEvents(true);
      this.worldNameField = new TextFieldWidget(this.font, this.width / 2 - 100, 50, 200, 20, I18n.format("selectWorld.enterName"));
      this.worldNameField.setText(this.worldName);
      this.worldNameField.func_212954_a((p_214319_1_) -> {
         this.worldName = p_214319_1_;
         this.btnCreateWorld.active = !this.worldNameField.getText().isEmpty();
         this.calcSaveDirName();
      });
      this.children.add(this.worldNameField);
      
      this.btnCreateWorld = this.addButton(new Button(this.width / 2 - 155, this.height - 28, 150, 20, I18n.format("selectWorld.create"), (p_214318_1_) -> {
         this.createWorld();
      }));
      
      // Buttons for EyeMine options, contain their own boolean state
      
      String sDaytime = "Always daytime";
      String sSunny = "Always sunny";
      String sInventory = "Keep inventory after dying";

      int w = (int) (this.font.getStringWidth(sInventory)*1.5); 
      btnDaytime = new BooleanButton(this.font, sDaytime, true, this.width/2, 110, w);
      btnSunny = new BooleanButton(this.font, sSunny, true, this.width/2, 130, w);
      btnInventory = new BooleanButton(this.font, sInventory, true, this.width/2, 150, w);               
      
      this.addButton(btnDaytime.getButton());
      this.addButton(btnSunny.getButton());
      this.addButton(btnInventory.getButton());
      
      // More options -> back to the usual minecraft screens
      this.addButton(new Button(this.width / 2 - 75, 177, 150, 20, I18n.format("More Minecraft Options"), (p_214321_1_) -> {
          EyeGaze.allowMoreOptions = true;
          DefaultConfigForNewWorld.setNewWorldOptions(btnDaytime.getValue(), btnSunny.getValue(), btnInventory.getValue());
          Minecraft.getInstance().displayGuiScreen(new CreateWorldScreen(this));
       }));

      this.addButton(new Button(this.width / 2 + 5, this.height - 28, 150, 20, I18n.format("gui.cancel"), (p_214317_1_) -> {
         this.minecraft.displayGuiScreen(this.parentScreen);
      }));
      this.func_212928_a(this.worldNameField);
      this.calcSaveDirName();
      
      
   }
   
   class BooleanButton {
	   private int x = 0;
	   private int y = 0;
	   private int w = 0;
	   private boolean val;
	   private String label;
	   private Button btn;
	   	   
	   BooleanButton(FontRenderer font, String label, boolean defaultVal, 
			    		int xPosCentre, int yPos, int width) {		   
		   this.label = label;
		   this.val = defaultVal;
		   this.x = xPosCentre-width/2;
		   this.y = yPos;
		   this.w = width;
		   
		   btn = new Button(x, y, w, 20, this.getLabel(val), (b) -> {
		   		  this.toggle(b); 
		   	   });		   		   
	   }
	   
	   public Button getButton() {
		   return btn;
	   }	   
	   
	   private String getLabel(boolean b) {
		   String msgBool = b ? "ON" : "OFF";		   
		   return label + ": " + msgBool; 
	   }
	   
	   private void toggle(Button b) {
		   val = !val;
		   this.btn.setMessage(this.getLabel(val));
	   }
	   
	   boolean getValue() {
		   return val;
	   }
   }
      
   /**
    * Determine a save-directory name from the world name
    */
   private void calcSaveDirName() {
      this.saveDirName = this.worldNameField.getText().trim();
      if (this.saveDirName.length() == 0) {
         this.saveDirName = "World";
      }

      try {
         this.saveDirName = FileUtil.func_214992_a(this.minecraft.getSaveLoader().func_215781_c(), this.saveDirName, "");
      } catch (Exception var4) {
         this.saveDirName = "World";

         try {
            this.saveDirName = FileUtil.func_214992_a(this.minecraft.getSaveLoader().func_215781_c(), this.saveDirName, "");
         } catch (Exception exception) {
            throw new RuntimeException("Could not create save folder", exception);
         }
      }

   }     

   public void removed() {
      this.minecraft.keyboardListener.enableRepeatEvents(false);
   }

   private void createWorld() {
      this.minecraft.displayGuiScreen((Screen)null);
      
      DefaultConfigForNewWorld.setNewWorldOptions(btnDaytime.getValue(), btnSunny.getValue(), btnInventory.getValue());
      
      if (!this.alreadyGenerated) {
         this.alreadyGenerated = true;
         long i = (new Random()).nextLong();
         WorldType.DEFAULT.onGUICreateWorldPress();

         boolean generateStructures = true;
         boolean hardcoreMode = false;
         WorldSettings worldsettings = new WorldSettings(i, GameType.CREATIVE, generateStructures, hardcoreMode, WorldType.DEFAULT);
         worldsettings.setGeneratorOptions(Dynamic.convert(NBTDynamicOps.INSTANCE, JsonOps.INSTANCE, this.chunkProviderSettingsJson));
         
         // TODO: more options here?                 
         worldsettings.enableCommands();

         this.minecraft.launchIntegratedServer(this.saveDirName, this.worldNameField.getText().trim(), worldsettings);
      }
   }

   public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
      if (super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_)) {
         return true;
      } else if (p_keyPressed_1_ != 257 && p_keyPressed_1_ != 335) {
         return false;
      } else {
         this.createWorld();
         return true;
      }
   }

   public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
      this.renderBackground();
      this.drawCenteredString(this.font, this.title.getFormattedText(), this.width / 2, 15, -1);

      this.drawString(this.font, I18n.format("selectWorld.enterName"), this.width / 2 - 100, 37, -6250336);
      this.drawString(this.font, I18n.format("selectWorld.resultFolder") + " " + this.saveDirName, this.width / 2 - 100, 75, -6250336);
      this.worldNameField.render(p_render_1_, p_render_2_, p_render_3_);

      this.drawCenteredString(this.font, "Extra EyeMine Options (creative only):", this.width / 2, 95, -1);
      
      super.render(p_render_1_, p_render_2_, p_render_3_);
   }

}