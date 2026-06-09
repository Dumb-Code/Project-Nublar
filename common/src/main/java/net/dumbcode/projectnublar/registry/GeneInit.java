package net.dumbcode.projectnublar.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.DeferredSupplier;
import dev.architectury.registry.registries.RegistrySupplier;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.gene.Genes;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class GeneInit {

    public static ResourceKey<Registry<Genes.Gene>> GENE_KEY = ResourceKey.createRegistryKey(Constants.modLoc("gene"));
    public static DeferredRegister<Genes.Gene> GENES = DeferredRegister.create(Constants.MODID, GENE_KEY);

    public static DeferredSupplier<Genes.Gene> AGGRESSION = register("aggression");
    public static DeferredSupplier<Genes.Gene> DEFENSE = register("defense");
    public static DeferredSupplier<Genes.Gene> EAT_RATE = register("eat_rate");
    public static DeferredSupplier<Genes.Gene> HEALTH = register("health");
    public static DeferredSupplier<Genes.Gene> HEALTH_REGEN = register("health_regen");
    public static DeferredSupplier<Genes.Gene> HEAT_RESISTANCE = register("heat_resistance");
    public static DeferredSupplier<Genes.Gene> HERD_SIZE = register("herd_size");
    public static DeferredSupplier<Genes.Gene> PACK_SIZE = register("pack_size");
    public static DeferredSupplier<Genes.Gene> IMMUNITY = register("immunity");
    public static DeferredSupplier<Genes.Gene> INTELLIGENCE = register("intelligence");
    public static DeferredSupplier<Genes.Gene> JUMP = register("jump");
    public static DeferredSupplier<Genes.Gene> NOCTURNAL = register("nocturnal");
    public static DeferredSupplier<Genes.Gene> FERTILITY = register("fertility");
    public static DeferredSupplier<Genes.Gene> SIZE = register("size");
    public static DeferredSupplier<Genes.Gene> SPEED = register("speed");
    public static DeferredSupplier<Genes.Gene> STOMACH_CAPACITY = register("stomach_capacity");
    public static DeferredSupplier<Genes.Gene> STRENGTH = register("strength");
    public static DeferredSupplier<Genes.Gene> TAMABILITY = register("tamability");
    public static DeferredSupplier<Genes.Gene> UNDERWATER_CAPACITY = register("underwater_capacity");
    public static DeferredSupplier<Genes.Gene> COLOR = register("color", 0);
    public static DeferredSupplier<Genes.Gene> GENDER = register("gender");


    public static DeferredSupplier<Genes.Gene> register(String name) {
        return GENES.register(name, () -> new Genes.Gene(name));
    }

    public static DeferredSupplier<Genes.Gene> register(String name, double requirement) {
        return GENES.register(name, () -> new Genes.Gene(name, requirement));
    }

    public static void loadClass() {
        GENES.register();
    }

    //entries is private
    public static List<Genes.Gene> getList() {
        List<Genes.Gene> genes = new ArrayList<>();
        for (RegistrySupplier<Genes.Gene> gene : GENES) {
            genes.add(gene.get());
        }
        return genes;
    }

    public static Codec<Genes.Gene> byNameCodec() {
        Codec<Genes.Gene> nameCodec = ResourceLocation.CODEC.flatXmap((location) -> Optional.ofNullable(GENES.getRegistrar().get(location)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown registry key in " + GENE_KEY + ": " + location)), (gene) -> GENES.getRegistrar().getKey(gene).map(ResourceKey::location).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown registry element in " + GENE_KEY + ":" + gene)));
        Codec<Genes.Gene> idCodec = ExtraCodecs.idResolverCodec((gene) -> GENES.getRegistrar().getKey(gene).isPresent() ? GENES.getRegistrar().getRawId(gene) : -1, value -> GENES.getRegistrar().byRawId(value), -1);
        return ExtraCodecs.orCompressed(nameCodec, idCodec);
    }
}
