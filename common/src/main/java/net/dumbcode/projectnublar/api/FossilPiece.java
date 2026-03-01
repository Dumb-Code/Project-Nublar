package net.dumbcode.projectnublar.api;

import net.dumbcode.projectnublar.api.fossil.FossilBlockStates;
import net.dumbcode.projectnublar.api.fossil.Quality;
import net.dumbcode.projectnublar.init.DinosaurInit;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record FossilPiece(String name, String path){
    public String getName() {
        return name;
    }
    public String getPath() {
        return path;
    }


    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("part", name.toLowerCase());
        tag.putString("path", path);
        return tag;
    }
    public static FossilPiece fromNBT(CompoundTag tag) {
        return new FossilPiece(tag.getString("part"), tag.getString("path"));
    }
}
