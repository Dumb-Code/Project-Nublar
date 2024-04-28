package com.nyfaria.projectnublar.item;

import com.nyfaria.projectnublar.api.Quality;
import com.nyfaria.projectnublar.init.EntityInit;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FossilItem extends Item {
    public FossilItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("quality.projectnublar." + stack.getTag().getString("quality")).withStyle(Quality.byName(stack.getTag().getString("quality")).getColor()));
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.projectnublar.fossil", BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(stack.getTag().getString("dino"))).getDescription(), Component.translatable("piece.projectnublar." + stack.getTag().getString("piece"))).withStyle(Quality.byName(stack.getTag().getString("quality")).getColor());
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stack.getOrCreateTag().putString("piece", "foot");
        stack.getTag().putString("quality", "fragmented");
        stack.getTag().putString("dino", "projectnublar:tyrannosaurus_rex");
        return stack;
    }
}
