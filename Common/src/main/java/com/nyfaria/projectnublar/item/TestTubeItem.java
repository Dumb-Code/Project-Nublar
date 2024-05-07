package com.nyfaria.projectnublar.item;

import com.nyfaria.projectnublar.api.NublarMath;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TestTubeItem extends Item {
    public TestTubeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltips, TooltipFlag flag) {
        if(stack.hasTag() && stack.getTag().contains("dna_percentage")){
            tooltips.add(Component.literal(NublarMath.round(stack.getOrCreateTag().getDouble("dna_percentage") * 100, 2) + "%"));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        if(stack.hasTag() && stack.getTag().contains("dino")){
            return Component.translatable("item.projectnublar.test_tube2", BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(stack.getTag().getString("dino"))).getDescription());
        }
        return super.getName(stack);
    }
}
