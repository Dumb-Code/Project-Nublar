package net.dumbcode.projectnublar.registry;

import net.dumbcode.projectnublar.api.dinosaur.DinoBehaviourData;
import net.dumbcode.projectnublar.api.dinosaur.DinoData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;

/** Registers the {@link EntityDataSerializer} used to sync {@link DinoData} over the network. */
public class DataSerializerInit {

    public static EntityDataSerializer<DinoData> DINO_DATA = new EntityDataSerializer<>() {
        @Override
        public void write(FriendlyByteBuf buf, DinoData dinoData) {
            buf.writeNbt(dinoData.toNBT());
        }

        @Override
        public DinoData read(FriendlyByteBuf buf) {
            return DinoData.fromNBT(buf.readNbt());
        }

        @Override
        public DinoData copy(DinoData dinoData) {
            return dinoData.copy();
        }
    };


    public static void loadClass(){
        EntityDataSerializers.registerSerializer(DINO_DATA);
    }

}
