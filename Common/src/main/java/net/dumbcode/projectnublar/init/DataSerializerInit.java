package net.dumbcode.projectnublar.init;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.DinoData;
import net.dumbcode.projectnublar.registration.EntityDataSerializerHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceLocation;

public class DataSerializerInit {

    public static EntityDataSerializer<DinoData> DINO_DATA = EntityDataSerializerHelper.INSTANCE.register(Constants.modLoc("npc_data"),new EntityDataSerializer<DinoData>() {
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
    });


    public static void loadClass(){}

}
