package net.dumbcode.projectnublar.registry;

import io.netty.buffer.ByteBuf;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.dinosaur.DinoData;
import net.dumbcode.projectnublar.platform.Services;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;

import java.util.Optional;
import java.util.UUID;

/**
 * The mod's {@link EntityDataSerializer}s. {@link #DINO_DATA} syncs {@link DinoData}; {@link #COMPOUND_TAG} and
 * {@link #OPTIONAL_UUID} replace the vanilla serializers of the same names, which 26.2 removed (ruling P40). Each
 * sends its value as 1.20.1 did: an NBT compound, or a present flag and the UUID's two longs.
 */
public class DataSerializerInit {

    /** {@link DinoData} as its NBT compound; a copy is a deep copy. */
    public static final EntityDataSerializer<DinoData> DINO_DATA = new EntityDataSerializer<>() {
        @Override
        public StreamCodec<ByteBuf, DinoData> codec() {
            return ByteBufCodecs.COMPOUND_TAG.map(DinoData::fromNBT, DinoData::toNBT);
        }

        @Override
        public DinoData copy(DinoData dinoData) {
            return dinoData.copy();
        }
    };

    /** A {@link CompoundTag}; a copy is a deep copy, as with 1.20.1's {@code EntityDataSerializers.COMPOUND_TAG}. */
    public static final EntityDataSerializer<CompoundTag> COMPOUND_TAG = new EntityDataSerializer<>() {
        @Override
        public StreamCodec<ByteBuf, CompoundTag> codec() {
            return ByteBufCodecs.COMPOUND_TAG;
        }

        @Override
        public CompoundTag copy(CompoundTag tag) {
            return tag.copy();
        }
    };

    /** An optional {@link UUID}, kept by reference as with 1.20.1's {@code EntityDataSerializers.OPTIONAL_UUID}. */
    public static final EntityDataSerializer<Optional<UUID>> OPTIONAL_UUID =
            EntityDataSerializer.forValueType(ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC));

    /**
     * Registers the serializers through the loader ({@code IPlatformHelper.registerEntityDataSerializer}):
     * NeoForge and Fabric API refuse mod serializers in vanilla's list and keep their own id-keyed
     * registries. {@code ProjectNublar.init} calls it once on both loaders.
     */
    public static void loadClass(){
        Services.PLATFORM.registerEntityDataSerializer(Constants.modLoc("dino_data"), DINO_DATA);
        Services.PLATFORM.registerEntityDataSerializer(Constants.modLoc("compound_tag"), COMPOUND_TAG);
        Services.PLATFORM.registerEntityDataSerializer(Constants.modLoc("optional_uuid"), OPTIONAL_UUID);
    }

}
