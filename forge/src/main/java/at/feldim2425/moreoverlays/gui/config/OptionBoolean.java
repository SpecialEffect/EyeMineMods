package at.feldim2425.moreoverlays.gui.config;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class OptionBoolean
        extends OptionValueEntry<Boolean> {

    private Button btnChange;
    private boolean state;
    
    public OptionBoolean(ConfigOptionList list, ForgeConfigSpec.BooleanValue valSpec, ForgeConfigSpec.ValueSpec spec) {
		super(list, valSpec, spec);
        this.showValidity = false;
        
        btnChange = new Button(OptionValueEntry.TITLE_WIDTH + 5, 0,this.getConfigOptionList().getRowWidth() - OptionValueEntry.TITLE_WIDTH - 5 - OptionValueEntry.CONTROL_WIDTH_VALIDATOR,20, new TextComponent(""), this::buttonPressed);
        this.overrideUnsaved(this.value.get());
	}

	@Override
    protected void renderControls(PoseStack poseStack, int rowTop, int rowLeft, int rowWidth, int itemHeight, int mouseX, int mouseY,
                                  boolean mouseOver, float partialTick) {
        super.renderControls(poseStack, rowTop, rowLeft, rowWidth, itemHeight, mouseX, mouseY, mouseOver, partialTick);
        this.btnChange.render(poseStack, mouseX, mouseY, 0);
    }

    @Override
    protected void overrideUnsaved(Boolean value) {
        this.state = value;
        if(this.state){
            this.btnChange.setMessage(new TextComponent("TRUE").withStyle(ChatFormatting.GREEN));
        }
        else {
            this.btnChange.setMessage(new TextComponent("FALSE").withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public List<? extends GuiEventListener> children() {
        List<GuiEventListener> childs = new ArrayList<>(super.children());
        childs.add(this.btnChange);
        return childs;
    }

    private void buttonPressed(Button btn){
        this.overrideUnsaved(!this.state);
        this.updateValue(this.state);
    }

    @Nullable
    @Override
    public GuiEventListener getFocused() {
        return null;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener arg) {

    }
}