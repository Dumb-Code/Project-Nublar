package net.dumbcode.projectnublar.item.api;

import net.dumbcode.projectnublar.api.DNAData;
import net.dumbcode.projectnublar.api.DinoData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DNADataItem extends Item {

    public DNADataItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltips, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltips, flag);
        if (stack.hasTag()) {
            if(stack.getTag().contains("DNAData")) {
                DNAData.createTooltip(stack, tooltips);
            } else if(stack.getTag().contains("DinoData")) {
                DinoData.fromNBT(stack.getTag().getCompound("DinoData")).createToolTip(tooltips);
            }
        }
    }
}
