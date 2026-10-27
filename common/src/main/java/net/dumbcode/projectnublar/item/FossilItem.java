package net.dumbcode.projectnublar.item;

import net.dumbcode.projectnublar.api.dinosaur.DNAData;
import net.dumbcode.projectnublar.item.api.DNADataItem;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.registry.DataComponentInit;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class FossilItem extends DNADataItem {
    public FossilItem(Properties properties) {
        super(properties);
    }

    /**
     * "{species} {piece}" in the quality colour. A fossil saved before the fossil port can carry a
     * quality or piece that no longer exists; it falls back to the plain item name.
     */
    @Override
    public Component getName(ItemStack stack) {
        // read only: component values are shared between stack copies
        DNAData data = stack.get(DataComponentInit.DNA_DATA.get());
        if (data != null) {
            if (data.getQuality() == null || data.getFossilPiece() == null) {
                return super.getName(stack);
            }
            return Component.translatable("item." + Constants.MODID + ".fossil", data.getFormattedType(),
                            Component.translatable("piece.projectnublar." + data.getFossilPiece().name()))
                    .withStyle(data.getQuality().getColor());
        }
        return super.getName(stack);
    }
}
