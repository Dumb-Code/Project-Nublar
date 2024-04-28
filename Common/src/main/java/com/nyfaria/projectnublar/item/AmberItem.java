package com.nyfaria.projectnublar.item;

import com.nyfaria.projectnublar.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AmberItem extends Item {
    public AmberItem(Properties $$0) {
        super($$0);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("dna_percentage." + Constants.MODID,  (int)(stack.getOrCreateTag().getDouble("dna_percentage") * 100) + "%"));
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item." + Constants.MODID + ".amber", BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(stack.getTag().getString("dino"))).getDescription());
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stack.getOrCreateTag().putDouble("dna_percentage", 0.8);
        stack.getTag().putString("dino", "projectnublar:tyrannosaurus_rex");
        return stack;
    }
}
