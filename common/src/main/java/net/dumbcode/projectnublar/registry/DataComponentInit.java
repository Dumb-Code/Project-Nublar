package net.dumbcode.projectnublar.registry;

import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.DeferredSupplier;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.dinosaur.DNAData;
import net.dumbcode.projectnublar.api.dinosaur.DinoData;
import net.dumbcode.projectnublar.api.dinosaur.DiskStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;

/**
 * The item data components of the mod. They replace the 1.20.1 item NBT: each one is saved with
 * the item and sent to clients. The ids are save contracts.
 *
 * <p>{@code ProjectNublar.init} registers them before the items.
 */
public class DataComponentInit {
    /** The Architectury register that holds the mod's data component types. */
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Constants.MODID, Registries.DATA_COMPONENT_TYPE);

    /** The genome of an egg or a filled test tube ({@code projectnublar:dino_data}). */
    public static final DeferredSupplier<DataComponentType<DinoData>> DINO_DATA = COMPONENTS.register("dino_data",
            () -> DataComponentType.<DinoData>builder()
                    .persistent(DinoData.CODEC).networkSynchronized(DinoData.STREAM_CODEC).build());

    /** One DNA sample: fossil, amber, test tube or syringe ({@code projectnublar:dna_data}). */
    public static final DeferredSupplier<DataComponentType<DNAData>> DNA_DATA = COMPONENTS.register("dna_data",
            () -> DataComponentType.<DNAData>builder()
                    .persistent(DNAData.CODEC).networkSynchronized(DNAData.STREAM_CODEC).build());

    /** The DNA samples on a storage drive ({@code projectnublar:disk_storage}). */
    public static final DeferredSupplier<DataComponentType<DiskStorage>> DISK_STORAGE = COMPONENTS.register("disk_storage",
            () -> DataComponentType.<DiskStorage>builder()
                    .persistent(DiskStorage.CODEC).networkSynchronized(DiskStorage.STREAM_CODEC).build());

    /** The embryo flag of a syringe, the 1.20.1 root {@code Embryo} boolean ({@code projectnublar:embryo}). */
    public static final DeferredSupplier<DataComponentType<Boolean>> EMBRYO = COMPONENTS.register("embryo",
            () -> DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());

    /** The fence post a wire spool is linked to ({@code projectnublar:fence_position}). */
    public static final DeferredSupplier<DataComponentType<BlockPos>> FENCE_POSITION = COMPONENTS.register("fence_position",
            () -> DataComponentType.<BlockPos>builder()
                    .persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC).build());

    /** Registers the components; {@code ProjectNublar.init} calls it once, before {@code ItemInit.loadClass()}. */
    public static void loadClass() {
        COMPONENTS.register();
    }

    /** Not instantiated: the class only holds the static registrations. */
    private DataComponentInit() {
    }
}
