

package net.dumbcode.projectnublar.datagen;

import com.mojang.serialization.JsonOps;
import net.dumbcode.projectnublar.api.gene.GeneData;
import net.dumbcode.projectnublar.api.gene.Genes;
import net.dumbcode.projectnublar.registry.GeneInit;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.JsonCodecProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GeneDataProvider extends JsonCodecProvider<GeneData> {


    public GeneDataProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, existingFileHelper, "minecraft", JsonOps.INSTANCE, PackType.SERVER_DATA, "gene_data", GeneData.CODEC, getGeneData());
    }

    public static Map<ResourceLocation, GeneData> getGeneData() {
        Map<ResourceLocation, GeneData> geneData = new HashMap<>();
        geneData.put(loc(EntityType.ALLAY), new GeneData(Map.of(

        ), Map.of("base", List.of())));
        geneData.put(loc(EntityType.AXOLOTL), new GeneData(Map.of(), Map.of("base", List.of())));
        geneData.put(loc(EntityType.BAT), new GeneData(Map.of(
                GeneInit.SIZE.get(), 0.75d,
                GeneInit.NOCTURNAL.get(), 1.25d
        ), Map.of("base", List.of(0x1C1912))));
        geneData.put(loc(EntityType.BEE), new GeneData(Map.of(
                GeneInit.SPEED.get(), 1.3d,
                GeneInit.SIZE.get(), 0.5d,
                GeneInit.HEALTH.get(), 0.5d
        ), Map.of("base", List.of(0xE6C15E, 0x5A3023))));
//        geneData.put(loc(EntityType.BLAZE), new GeneData(Map.of(), Map.of("base", List.of())));
        geneData.put(loc(EntityType.CAT), new GeneData(Map.of(
                GeneInit.INTELLIGENCE.get(), 1.5d,
                GeneInit.TAMABILITY.get(), 1.45d,
                GeneInit.SIZE.get(), 0.6d
        ), Map.of("base", List.of())));

        geneData.put(loc(EntityType.CAVE_SPIDER), new GeneData(Map.of(
                GeneInit.SIZE.get(), 0.5d
        ), Map.of("base", List.of(0x153833))));
        geneData.put(loc(EntityType.CHICKEN), new GeneData(Map.of(
                GeneInit.SIZE.get(), 0.5d
        ), Map.of("base", List.of(0xB2B2B2, 0xD40409))));
        geneData.put(loc(EntityType.COD), new GeneData(Map.of(
                GeneInit.SIZE.get(), 0.5d,
                GeneInit.UNDERWATER_CAPACITY.get(), 1.3d,
                GeneInit.HEALTH.get(), 0.5d
        ), Map.of("base", List.of(0xAF9878, 0x775B49))));
        geneData.put(loc(EntityType.COW), new GeneData(Map.of(
                GeneInit.HERD_SIZE.get(), 1.5d
        ), Map.of("base", List.of())));
//        geneData.put(loc(EntityType.CREEPER), new GeneData(Map.of(), Map.of("base", List.of())));
        geneData.put(loc(EntityType.DOLPHIN), new GeneData(Map.of(
                GeneInit.UNDERWATER_CAPACITY.get(), 1.75d,
                GeneInit.SPEED.get(), 1.35d
        ), Map.of("base", List.of(0x73737D))));
        geneData.put(loc(EntityType.DONKEY), new GeneData(Map.of(
                GeneInit.SPEED.get(), 0.8d,
                GeneInit.STRENGTH.get(), 1.25d,
                GeneInit.INTELLIGENCE.get(), 1.25d
        ), Map.of("base", List.of(0x8A7666))));
//        geneData.put(loc(EntityType.DROWNED), new GeneData(Map.of(), Map.of("base", List.of())));
//        geneData.put(loc(EntityType.ELDER_GUARDIAN), new GeneData(Map.of(), Map.of("base", List.of())));
//        geneData.put(loc(EntityType.ENDER_DRAGON), new GeneData(Map.of(), Map.of("base", List.of())));
//        geneData.put(loc(EntityType.ENDERMAN), new GeneData(Map.of(), Map.of("base", List.of())));
//        geneData.put(loc(EntityType.ENDERMITE), new GeneData(Map.of(), Map.of("base", List.of())));
//        geneData.put(loc(EntityType.EVOKER), new GeneData(Map.of(), Map.of("base", List.of())));
        geneData.put(loc(EntityType.FOX), new GeneData(Map.of(
                GeneInit.SPEED.get(), 1.7d,
                GeneInit.INTELLIGENCE.get(), 1.3d
        ), Map.of("base", List.of(0xB2B2B2, 0xE37C21))));
//        geneData.put(loc(EntityType.GHAST), new GeneData(Map.of(), Map.of("base", List.of())));
//        geneData.put(loc(EntityType.GIANT), new GeneData(Map.of(), Map.of("base", List.of())));
        geneData.put(loc(EntityType.GLOW_SQUID), new GeneData(Map.of(), Map.of("base", List.of())));
        geneData.put(loc(EntityType.GOAT), new GeneData(Map.of(), Map.of("base", List.of())));
//        geneData.put(loc(EntityType.GUARDIAN), new GeneData(Map.of(), Map.of("base", List.of())));
        geneData.put(loc(EntityType.HOGLIN), new GeneData(Map.of(
                GeneInit.HEAT_RESISTANCE.get(), 1.5d,
                GeneInit.STRENGTH.get(), 1.5d,
                GeneInit.SIZE.get(), 1.75d,
                GeneInit.AGGRESSION.get(), 1.85d,
                GeneInit.TAMABILITY.get(), 0.5d,
                GeneInit.DEFENSE.get(), 1.3d
        ), Map.of("base", List.of(0x8B6046))));
        geneData.put(loc(EntityType.HORSE), new GeneData(Map.of(
                GeneInit.JUMP.get(), 1.3d,
                GeneInit.TAMABILITY.get(), 1.4d
        ), Map.of(
                "white", List.of(0xB2B2B2),
                "creamy", List.of(0x926633),
                "chestnut", List.of(0x8A461B),
                "brown", List.of(0x53250D),
                "black", List.of(0x24252D),
                "gray", List.of(0x5F5F5F),
                "dark_brown", List.of(0x2F1A0F)
        )));
//        geneData.put(loc(EntityType.HUSK), new GeneData(Map.of(), Map.of("base", List.of())));
//        geneData.put(loc(EntityType.ILLUSIONER), new GeneData(Map.of(), Map.of("base", List.of())));
//        geneData.put(loc(EntityType.IRON_GOLEM), new GeneData(Map.of(), Map.of("base", List.of())));
        geneData.put(loc(EntityType.LLAMA), new GeneData(Map.of(
                GeneInit.STOMACH_CAPACITY.get(), 1.3d,
                GeneInit.DEFENSE.get(), 1.25d,
                GeneInit.TAMABILITY.get(), 1.25d
        ), Map.of("base", List.of(0xB2B2B2))));
//        geneData.put(loc(EntityType.MAGMA_CUBE), new GeneData(Map.of(), Map.of("base", List.of())));
        geneData.put(loc(EntityType.MOOSHROOM), new GeneData(Map.of(), Map.of(
                "brown", List.of(0xB68767, 0xB0B0B0),
                "red", List.of(0xA41012, 0xB0B0B0)
        )));
        geneData.put(loc(EntityType.MULE), new GeneData(Map.of(
                GeneInit.SPEED.get(), 0.8d,
                GeneInit.FERTILITY.get(), 0.5d,
                GeneInit.INTELLIGENCE.get(), 1.2d,
                GeneInit.TAMABILITY.get(), 1.3d
        ), Map.of("base", List.of(0x502C1A))));
        geneData.put(loc(EntityType.OCELOT), new GeneData(Map.of(
                GeneInit.SPEED.get(), 1.5d,
                GeneInit.TAMABILITY.get(), 0.7d,
                GeneInit.SIZE.get(), 0.4d
        ), Map.of("base", List.of(0xFDD976, 0x8C5329))));
        geneData.put(loc(EntityType.PANDA), new GeneData(Map.of(
                GeneInit.SPEED.get(), 0.8d,
                GeneInit.STOMACH_CAPACITY.get(), 1.75d,
                GeneInit.STRENGTH.get(), 1.25d,
                GeneInit.DEFENSE.get(), 0.5,
                GeneInit.EAT_RATE.get(), 0.9d,
                GeneInit.FERTILITY.get(), 0.7d
        ), Map.of("base", List.of(0xB2B2B2, 0x222222))));
        geneData.put(loc(EntityType.PARROT), new GeneData(Map.of(
                GeneInit.INTELLIGENCE.get(), 1.25d
        ), Map.of(
                "red_blue", List.of(0xEB0100, 0xE8C100),
                "blue", List.of(0x112DEC, 0xE8C100),
                "green", List.of(0x9CDA00),
                "yellow_blue", List.of(0x12CCFD, 0xE8C100),
                "gray", List.of(0xAFAFAF, 0xE8C100)
        )));
//        geneData.put(loc(EntityType.PHANTOM), new GeneData(Map.of(), Map.of("base", List.of())));
        geneData.put(loc(EntityType.PIG), new GeneData(Map.of(
                GeneInit.IMMUNITY.get(), 1.25d,
                GeneInit.DEFENSE.get(), 1.2d
        ), Map.of("base", List.of(0xB2B2B2))));
//        geneData.put(loc(EntityType.PIGLIN), new GeneData(Map.of(), Map.of("base", List.of())));
//        geneData.put(loc(EntityType.PIGLIN_BRUTE), new GeneData(Map.of(), Map.of("base", List.of())));
//        geneData.put(loc(EntityType.PILLAGER), new GeneData(Map.of(), Map.of("base", List.of())));
        geneData.put(loc(EntityType.POLAR_BEAR), new GeneData(Map.of(
                GeneInit.SIZE.get(), 1.5d,
                GeneInit.SPEED.get(), 1.5d,
                GeneInit.HEALTH.get(), 1.35d,
                GeneInit.STRENGTH.get(), 1.5d,
                GeneInit.DEFENSE.get(), 1.75d
        ), Map.of("base", List.of(0xB2B2B2))));
        geneData.put(loc(EntityType.PUFFERFISH), new GeneData(Map.of(
                GeneInit.UNDERWATER_CAPACITY.get(), 1.3d,
                GeneInit.HEALTH.get(), 0.5d,
                GeneInit.SIZE.get(), 0.5d
        ), Map.of("base", List.of(0xC2B091, 0xE3970B))));
        geneData.put(loc(EntityType.RABBIT), new GeneData(Map.of(
                GeneInit.SPEED.get(), 1.5d,
                GeneInit.SIZE.get(), 0.75d
        ), Map.of(
                "brown", List.of(0x826F58),
                "white", List.of(0xB2B2B2),
                "black", List.of(0x131313),
                "white_splotched", List.of(0xB2B2B2, 0x131313),
                "gold", List.of(0xF9EAAF),
                "salt", List.of(0x7F6D58),
                "evil", List.of(0xB2B2B2)
        )));
//        geneData.put(loc(EntityType.RAVAGER), new GeneData(Map.of(), Map.of("base", List.of())));
        geneData.put(loc(EntityType.SALMON), new GeneData(Map.of(
                GeneInit.SIZE.get(), 0.5d,
                GeneInit.UNDERWATER_CAPACITY.get(), 1.3d,
                GeneInit.HEALTH.get(), 0.5d
        ), Map.of("base", List.of(0xA83A38, 0x4C6E52))));
        geneData.put(loc(EntityType.SHEEP), new GeneData(Map.of(
                GeneInit.INTELLIGENCE.get(), 0.75d,
                GeneInit.HERD_SIZE.get(), 1.5d
        ), Map.of("base", List.of(0xB4947D, 0xB2B2B2))));
//        geneData.put(loc(EntityType.SHULKER), new GeneData(Map.of(), Map.of("base", List.of())));
//        geneData.put(loc(EntityType.SILVERFISH), new GeneData(Map.of(), Map.of("base", List.of())));
//        geneData.put(loc(EntityType.SKELETON), new GeneData(Map.of(), Map.of("base", List.of())));
//        geneData.put(loc(EntityType.SKELETON_HORSE), new GeneData(Map.of(), Map.of("base", List.of())));
//        geneData.put(loc(EntityType.SLIME), new GeneData(Map.of(), Map.of("base", List.of())));
//        geneData.put(loc(EntityType.SNOW_GOLEM), new GeneData(Map.of(), Map.of("base", List.of())));
        geneData.put(loc(EntityType.SPIDER), new GeneData(Map.of(
                GeneInit.NOCTURNAL.get(), 0.75d
        ), Map.of("base", List.of(0x4E443C))));
        geneData.put(loc(EntityType.SQUID), new GeneData(Map.of(
                GeneInit.UNDERWATER_CAPACITY.get(), 1.5d,
                GeneInit.HEALTH.get(), 0.7d,
                GeneInit.HEALTH_REGEN.get(), 1.25d
        ), Map.of("base", List.of(0x132737, 0x536B7F))));
//        geneData.put(loc(EntityType.STRAY), new GeneData(Map.of(), Map.of("base", List.of())));
//        geneData.put(loc(EntityType.STRIDER), new GeneData(Map.of(), Map.of("base", List.of())));
//        geneData.put(loc(EntityType.TRADER_LLAMA), new GeneData(Map.of(), Map.of("base", List.of())));
        geneData.put(loc(EntityType.TROPICAL_FISH), new GeneData(Map.of(
                GeneInit.SIZE.get(), 0.5d,
                GeneInit.UNDERWATER_CAPACITY.get(), 1.3d,
                GeneInit.HEALTH.get(), 0.5d
        ), Map.ofEntries(
                Map.entry(DyeColor.BLACK.getName(), List.of(DyeColor.BLACK.getFireworkColor())),
                Map.entry(DyeColor.BLUE.getName(), List.of(DyeColor.BLUE.getFireworkColor())),
                Map.entry(DyeColor.BROWN.getName(), List.of(DyeColor.BROWN.getFireworkColor())),
                Map.entry(DyeColor.CYAN.getName(), List.of(DyeColor.CYAN.getFireworkColor())),
                Map.entry(DyeColor.GRAY.getName(), List.of(DyeColor.GRAY.getFireworkColor())),
                Map.entry(DyeColor.GREEN.getName(), List.of(DyeColor.GREEN.getFireworkColor())),
                Map.entry(DyeColor.LIGHT_BLUE.getName(), List.of(DyeColor.LIGHT_BLUE.getFireworkColor())),
                Map.entry(DyeColor.LIGHT_GRAY.getName(), List.of(DyeColor.LIGHT_GRAY.getFireworkColor())),
                Map.entry(DyeColor.LIME.getName(), List.of(DyeColor.LIME.getFireworkColor())),
                Map.entry(DyeColor.MAGENTA.getName(), List.of(DyeColor.MAGENTA.getFireworkColor())),
                Map.entry(DyeColor.ORANGE.getName(), List.of(DyeColor.ORANGE.getFireworkColor())),
                Map.entry(DyeColor.PINK.getName(), List.of(DyeColor.PINK.getFireworkColor())),
                Map.entry(DyeColor.PURPLE.getName(), List.of(DyeColor.PURPLE.getFireworkColor())),
                Map.entry(DyeColor.RED.getName(), List.of(DyeColor.RED.getFireworkColor())),
                Map.entry(DyeColor.WHITE.getName(), List.of(DyeColor.WHITE.getFireworkColor())),
                Map.entry(DyeColor.YELLOW.getName(), List.of(DyeColor.YELLOW.getFireworkColor()))
        )));
        geneData.put(loc(EntityType.TURTLE), new GeneData(Map.of(
                GeneInit.UNDERWATER_CAPACITY.get(), 2.25d,
                GeneInit.HEALTH.get(), 0.75d,
                GeneInit.IMMUNITY.get(), 2.25d,
                GeneInit.SPEED.get(), 0.5d,
                GeneInit.DEFENSE.get(), 1.5d
        ), Map.of("base", List.of(0xBFB37F, 0x28340A))));
//        geneData.put(loc(EntityType.VEX), new GeneData(Map.of(), Map.of("base", List.of())));
//        geneData.put(loc(EntityType.VILLAGER), new GeneData(Map.of(), Map.of("base", List.of())));
//        geneData.put(loc(EntityType.VINDICATOR), new GeneData(Map.of(), Map.of("base", List.of())));
//        geneData.put(loc(EntityType.WANDERING_TRADER), new GeneData(Map.of(), Map.of("base", List.of())));
//        geneData.put(loc(EntityType.WITCH), new GeneData(Map.of(), Map.of("base", List.of())));
//        geneData.put(loc(EntityType.WITHER), new GeneData(Map.of(), Map.of("base", List.of())));
//        geneData.put(loc(EntityType.WITHER_SKELETON), new GeneData(Map.of(), Map.of("base", List.of())));
        geneData.put(loc(EntityType.WOLF), new GeneData(Map.of(
                GeneInit.INTELLIGENCE.get(), 1.75d,
                GeneInit.TAMABILITY.get(), 2.75d,
                GeneInit.SPEED.get(), 1.25d,
                GeneInit.PACK_SIZE.get(),1.75d
        ), Map.of("base", List.of(0xB2B2B2))));
//        geneData.put(loc(EntityType.ZOGLIN), new GeneData(Map.of(), Map.of("base", List.of())));
//        geneData.put(loc(EntityType.ZOMBIE), new GeneData(Map.of(), Map.of("base", List.of())));
//        geneData.put(loc(EntityType.ZOMBIE_HORSE), new GeneData(Map.of(), Map.of("base", List.of())));
//        geneData.put(loc(EntityType.ZOMBIE_VILLAGER), new GeneData(Map.of(), Map.of("base", List.of())));
//        geneData.put(loc(EntityType.ZOMBIFIED_PIGLIN), new GeneData(Map.of(), Map.of("base", List.of())));
//        geneData.put(loc(EntityType.ZOMBIE_VILLAGER), new GeneData(Map.of(), Map.of("base", List.of())));
        return geneData;
    }

    public static ResourceLocation loc(EntityType<?> type) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(type);
    }
}
