package at.feldim2425.moreoverlays.gui;

import java.util.ArrayList;
import java.util.List;

import at.feldim2425.moreoverlays.gui.config.ConfigOptionList;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.client.gui.widget.UnicodeGlyphButton;


public class ConfigScreen extends Screen {

    private final String modId;
    private ForgeConfigSpec configSpec;
    private ConfigOptionList optionList;
    private String categoryTitle = null;

    private Button btnReset;
    private Button btnUndo;
    private Button btnBack;

    private List<String> pathCache = new ArrayList<>();
    private String txtUndo = "";
    private String txtReset = "";
    private String txtDone = "";
    
    private Screen modListScreen;

    public ConfigScreen(Screen modListScreen, ForgeConfigSpec spec, String modId) {
        super(new TranslationTextComponent("Config options for "+modId));
        this.modListScreen = modListScreen;
        this.configSpec = spec;
        this.modId = modId;        

        this.txtReset = "Reset to default";
        this.txtUndo = "Undo changes";
        this.txtDone = I18n.format("gui.done");
    }

    @Override
    protected void init() {
    	
    	if (this.optionList == null) {
    		this.optionList = new ConfigOptionList(this.minecraft, this.modId, this);
    		
    		if(pathCache.isEmpty()){
                this.optionList.setConfiguration(configSpec);
            }
            else {
                this.optionList.setConfiguration(configSpec, this.pathCache);
            }            
    	}
        
    	// Lower buttons layout with padding will be:
    	// 2*p | button | p | button | p | button | p | button | 2*p
        int pad = 5;
        int buttonWidth = (Minecraft.getInstance().getMainWindow().getScaledWidth() - 7*pad)/4;
        final int buttonHeight = 20;
        
        // Button positions
        final int buttonY = this.height - 32 + (32-20)/2;
        final int xBack = pad*2;
        final int xDefaultAll = this.width - buttonWidth - pad*2;        
        final int xUndoAll = xDefaultAll - buttonWidth - pad;
                        
        this.btnReset = new UnicodeGlyphButton(xDefaultAll, buttonY, buttonWidth, buttonHeight,
                new StringTextComponent(" " + this.txtReset), ConfigOptionList.RESET_CHAR, 1.0f,
            (btn) -> this.optionList.reset());
        
        this.btnUndo = new UnicodeGlyphButton(xUndoAll, buttonY, buttonWidth, buttonHeight,
                new StringTextComponent(" " + this.txtUndo), ConfigOptionList.UNDO_CHAR, 1.0f,
        		(btn) -> this.optionList.undo());
        
        this.btnBack = new Button(xBack, buttonY, buttonWidth, buttonHeight,  new StringTextComponent(this.txtDone),
                (btn) -> this.back());
        
        this.children.add(this.optionList);
        this.children.add(this.btnReset);
        this.children.add(this.btnUndo);
        this.children.add(this.btnBack);

        this.btnReset.active = false;
        this.btnUndo.active = false;

        this.optionList.updateGui();
    }
    
    private void back() {    	    	
    	this.save();
        if(!this.optionList.getCurrentPath().isEmpty()){
            this.optionList.pop();           
        }
        else {
        	Minecraft.getInstance().displayGuiScreen(modListScreen);
        }
	}

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(matrixStack);
        this.optionList.render(matrixStack, mouseX, mouseY, partialTick);
        this.btnReset.render(matrixStack, mouseX, mouseY, partialTick);
        this.btnUndo.render(matrixStack, mouseX, mouseY, partialTick);
        this.btnBack.render(matrixStack, mouseX, mouseY, partialTick);
        this.drawCenteredString(matrixStack, this.font, this.title.getString(), this.width / 2, 8, 16777215);
        if(this.categoryTitle != null){
            this.drawCenteredString(matrixStack, this.font, this.categoryTitle, this.width / 2, 24, 16777215);
        }
        super.render(matrixStack, mouseX, mouseY, partialTick);
    }

    private void save(){
        this.optionList.save();
        this.configSpec.save();
        this.optionList.undo();
    }

    @Override
    public void tick() {
        super.tick();
        this.btnReset.active = this.optionList.isResettable();
        this.btnUndo.active = this.optionList.isUndoable();
    }
    
    public void updatePath(final List<String> newPath){
        final String key = this.optionList.categoryTitleKey(newPath);
        
        if(key == null){
            this.categoryTitle = null;
        }
        else {
        	String translated = I18n.format(key);
        	this.categoryTitle = translated;
        	if (translated.equals(key)) {
        		final String lastKey = key.substring(key.lastIndexOf('.') + 1);	
        		String categoryComment = this.optionList.comments.getComment(lastKey);
        		if (categoryComment != null) {
        			this.categoryTitle = categoryComment;
        		}
        	}
        }

        pathCache.clear();
        pathCache.addAll(newPath);
    }

    @Override
    public boolean keyPressed(int key, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (key == 256) {
        	this.back();
        	return true;
        }
        else {
            return super.keyPressed(key, p_keyPressed_2_, p_keyPressed_3_);
        }
    }

    public void drawRightAlignedString(MatrixStack matrixStack, FontRenderer fontRenderer, String font, int text, int x, int y) {
        fontRenderer.drawStringWithShadow(matrixStack, font, (float)(text - fontRenderer.getStringWidth(font)), (float)x, y);
    }
}
