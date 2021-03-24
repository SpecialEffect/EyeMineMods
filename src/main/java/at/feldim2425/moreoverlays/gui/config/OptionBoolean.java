package at.feldim2425.moreoverlays.gui.config;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class OptionBoolean
        extends OptionValueEntry<Boolean> {

    private Button btnChange;
    private boolean state;
    
    public OptionBoolean(ConfigOptionList list, ForgeConfigSpec.BooleanValue valSpec, ForgeConfigSpec.ValueSpec spec) {
		super(list, valSpec, spec);
        this.showValidity = false;
        
        btnChange = new Button(OptionValueEntry.TITLE_WIDTH + 5, 0,this.getConfigOptionList().getRowWidth() - OptionValueEntry.TITLE_WIDTH - 5 - OptionValueEntry.CONTROL_WIDTH_VALIDATOR,20, new StringTextComponent(""), this::buttonPressed);
        this.overrideUnsaved(this.value.get());
	}

	@Override
    protected void renderControls(MatrixStack matrixStack, int rowTop, int rowLeft, int rowWidth, int itemHeight, int mouseX, int mouseY,
                                  boolean mouseOver, float partialTick) {
        super.renderControls(matrixStack, rowTop, rowLeft, rowWidth, itemHeight, mouseX, mouseY, mouseOver, partialTick);
        this.btnChange.render(matrixStack, mouseX, mouseY, 0);
    }

    @Override
    protected void overrideUnsaved(Boolean value) {
        this.state = value;
        if(this.state){
            this.btnChange.setMessage(new StringTextComponent("TRUE").mergeStyle(TextFormatting.GREEN));
        }
        else {
            this.btnChange.setMessage(new StringTextComponent("FALSE").mergeStyle(TextFormatting.RED));
        }
    }

    @Override
    public List<? extends IGuiEventListener> getEventListeners() {
        List<IGuiEventListener> childs = new ArrayList<>(super.getEventListeners());
        childs.add(this.btnChange);
        return childs;
    }

    private void buttonPressed(Button btn){
        this.overrideUnsaved(!this.state);
        this.updateValue(this.state);
    }
    
}