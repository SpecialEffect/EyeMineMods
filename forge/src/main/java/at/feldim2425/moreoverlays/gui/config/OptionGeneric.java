package at.feldim2425.moreoverlays.gui.config;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.common.ForgeConfigSpec;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class OptionGeneric<V>
        extends OptionValueEntry<V> {

    private EditBox tfConfigEntry;
    
    public OptionGeneric(ConfigOptionList list, ForgeConfigSpec.ConfigValue<V> valSpec, ForgeConfigSpec.ValueSpec spec) {
		super(list, valSpec, spec);
        this.showValidity = true;
        
        this.tfConfigEntry = new EditBox(Minecraft.getInstance().font, OptionValueEntry.TITLE_WIDTH + 5, 2, this.getConfigOptionList().getRowWidth() - OptionValueEntry.TITLE_WIDTH - 5 - OptionValueEntry.CONTROL_WIDTH_VALIDATOR, 16, new TextComponent(""));
        this.overrideUnsaved(this.value.get());
	}

	@Override
    protected void renderControls(PoseStack poseStack, int rowTop, int rowLeft, int rowWidth, int itemHeight, int mouseX, int mouseY,
                                  boolean mouseOver, float partialTick) {
        super.renderControls(poseStack, rowTop, rowLeft, rowWidth, itemHeight, mouseX, mouseY, mouseOver, partialTick);
        this.tfConfigEntry.render(poseStack, mouseX, mouseY, 0);
    }

    @Override
    protected void overrideUnsaved(V value) {
    	if (this.value instanceof ForgeConfigSpec.DoubleValue){
    		DecimalFormat df = new DecimalFormat("###.##");
            this.tfConfigEntry.setValue(df.format(value));
    	}
    	else {
    		this.tfConfigEntry.setValue(value.toString());
    	}
    }

    @Override
    public List<? extends GuiEventListener> children() {
        List<GuiEventListener> childs = new ArrayList<>(super.children());
        childs.add(this.tfConfigEntry);
        return childs;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener focused) {
        if(focused == null){
            this.tfConfigEntry.setFocus(false);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        final boolean flag = super.keyReleased(keyCode, scanCode, modifiers);

        try {
            if(this.spec.getClazz() == String.class){
                this.updateValue((V)this.tfConfigEntry.getValue());
            }
            else if(this.value instanceof ForgeConfigSpec.IntValue){
                this.updateValue((V)Integer.valueOf(this.tfConfigEntry.getValue()));
            }
            else if(this.value instanceof ForgeConfigSpec.DoubleValue){
                this.updateValue((V)Double.valueOf(this.tfConfigEntry.getValue()));
            }
            else if(this.value instanceof ForgeConfigSpec.BooleanValue){
                this.updateValue((V)Boolean.valueOf(this.tfConfigEntry.getValue()));
            }
        }
        catch(NumberFormatException e){
            this.updateValue(null);
        }

        return flag;
    }

    @Nullable
    @Override
    public GuiEventListener getFocused() {
        if(this.tfConfigEntry.isFocused()){
            return this.tfConfigEntry;
        }
        return null;
    }
}