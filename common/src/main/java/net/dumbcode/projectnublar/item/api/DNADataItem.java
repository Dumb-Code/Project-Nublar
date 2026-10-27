package net.dumbcode.projectnublar.item.api;

import net.dumbcode.projectnublar.api.dinosaur.DNAData;
import net.dumbcode.projectnublar.api.dinosaur.DinoData;
import net.dumbcode.projectnublar.registry.DataComponentInit;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/** An item that can carry a DNA sample or a genome (fossil, amber, test tube, syringe) and shows it in its tooltip. */
public class DNADataItem extends Item {

    public DNADataItem(Properties properties) {
        super(properties);
    }

    /**
     * True when the stack holds any sample data: {@link DataComponentInit#DNA_DATA},
     * {@link DataComponentInit#DINO_DATA} or {@link DataComponentInit#EMBRYO}. The test tube and
     * syringe checks use it where 1.20.1 checked {@code hasTag()} (ruling P74).
     */
    public static boolean hasSampleData(ItemStack stack) {
        return stack.has(DataComponentInit.DNA_DATA.get())
                || stack.has(DataComponentInit.DINO_DATA.get())
                || stack.has(DataComponentInit.EMBRYO.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltips, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltips, flag);
        if (stack.has(DataComponentInit.DNA_DATA.get())) {
            DNAData.createTooltip(stack, tooltips);
        } else if (stack.has(DataComponentInit.DINO_DATA.get())) {
            // A copy: the tooltip fills the gene cache of the data it reads (P120).
            DinoData.fromStack(stack).createToolTip(tooltips);
        }
    }
}
