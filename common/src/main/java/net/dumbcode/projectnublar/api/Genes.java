package net.dumbcode.projectnublar.api;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.dumbcode.projectnublar.ProjectNublar;
import net.dumbcode.projectnublar.init.GeneInit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

import java.awt.*;

public class Genes {
    public static Codec<Gene> CODEC = Codec.STRING.xmap(Genes::byName, Gene::name);
    public static Multimap<Gene, Pair<EntityType<?>, Double>> GENE_STORAGE = HashMultimap.create();

    public static void addToGene(Gene gene, EntityType<?> type, double value) {
        GENE_STORAGE.put(gene, Pair.of(type, value));
    }

    public static Gene byName(String name) {
        for (Gene gene : GeneInit.getList()) {
            if (gene.name().equals(name)) {
                return gene;
            }
        }
        return null;
    }

    public record Gene(String name, double doubleValue) {

        public Gene(String name) {
            this(name, 1);
        }

        public Component getTooltip(Double value) {
            return name.equals("gender") ? getGenderComponent(name(),value) : getGenericComponent(name(),value);
        }

        private Component getGenderComponent(String pName,Double value) {
            String gender = value == 2D ? "Female" : "Male";
            return Component.literal(ProjectNublar.checkReplace(pName)).append(Component.literal(": ")).append(Component.literal(gender).withStyle(value == 2D ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.BLUE));
        }
        private Component getGenericComponent(String pName, Double value) {
           return Component.literal(ProjectNublar.checkReplace(pName)).append(Component.literal(": ")).append(Component.literal(String.valueOf(value.intValue())).withStyle(value > 0 ? ChatFormatting.GREEN : ChatFormatting.RED).append(Component.literal("%")));
        }

        public Component getTooltip() {
            return Component.literal(ProjectNublar.checkReplace(name()));
        }


        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Gene gene) {
                return gene.name().equals(name());
            }
            return false;
        }
    }
}
