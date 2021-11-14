package at.feldim2425.moreoverlays.gui.config;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OptionCategory extends ConfigOptionList.OptionEntry {

    private List<BaseComponent> tooltip;
    private Button btnOpen;

    public OptionCategory(ConfigOptionList list, List<String> path, String name, String comment){
        super(list);
        btnOpen = new Button(0, 0, this.getConfigOptionList().getRowWidth() - 4, 20, new TextComponent(name), (btn) -> {
            list.push(path);
        });

        String[] lines = null;
        if(comment != null){
            lines = comment.split("\\n");
        }

        tooltip = new ArrayList<>(lines.length + 1);
        tooltip.add(new TextComponent(ChatFormatting.RED.toString() + name));
        for(final String line : lines){
            tooltip.add(new TextComponent(ChatFormatting.YELLOW.toString() + line));
        }
    }

    @Override
    public void renderControls(PoseStack matrixStack, int rowTop, int rowLeft, int rowWidth, int itemHeight, int mouseX, int mouseY, boolean mouseOver, float partialTick) {
        btnOpen.render(matrixStack, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderTooltip(PoseStack matrixStack, int rowTop, int rowLeft, int rowWidth, int itemHeight,int mouseX, int mouseY){
        List<FormattedCharSequence> list = Lists.transform(tooltip, BaseComponent::getVisualOrderText);
        this.getConfigOptionList().getScreen().renderTooltip(matrixStack, list, mouseX, mouseY);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return Arrays.asList(this.btnOpen);
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