package at.feldim2425.moreoverlays.gui.config;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OptionCategory extends ConfigOptionList.OptionEntry {

    private List<TextComponent> tooltip;
    private Button btnOpen;

    public OptionCategory(ConfigOptionList list, List<String> path, String name, String comment){
        super(list);
        btnOpen = new Button(0, 0, this.getConfigOptionList().getRowWidth() - 4, 20, new StringTextComponent(name), (btn) -> {
            list.push(path);
        });

        String[] lines = null;
        if(comment != null){
            lines = comment.split("\\n");
        }

        tooltip = new ArrayList<>(lines.length + 1);
        tooltip.add(new StringTextComponent(TextFormatting.RED.toString() + name));
        for(final String line : lines){
            tooltip.add(new StringTextComponent(TextFormatting.YELLOW.toString() + line));
        }
    }

    @Override
    public void renderControls(MatrixStack matrixStack, int rowTop, int rowLeft, int rowWidth, int itemHeight, int mouseX, int mouseY, boolean mouseOver, float partialTick) {
        btnOpen.render(matrixStack, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderTooltip(MatrixStack matrixStack, int rowTop, int rowLeft, int rowWidth, int itemHeight,int mouseX, int mouseY){
        List<IReorderingProcessor> list = Lists.transform(tooltip, ITextComponent::func_241878_f);
        this.getConfigOptionList().getScreen().renderTooltip(matrixStack, list, mouseX, mouseY);
    }

    @Override
    public List<? extends IGuiEventListener> getEventListeners() {
        return Arrays.asList(this.btnOpen);
    }
}