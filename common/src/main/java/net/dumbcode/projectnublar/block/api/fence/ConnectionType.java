package net.dumbcode.projectnublar.block.api.fence;

import com.google.common.collect.Maps;
import net.minecraft.resources.Identifier;

import java.util.Map;

public interface ConnectionType {
    Map<Identifier, ConnectionType> registryMap = Maps.newHashMap(); //todo: move to a registry?
    default void register() {
        registryMap.put(this.getRegistryName(), this);
    }
    double[] getOffsets();
    int getHeight();
    float getRadius();
    float getCableWidth();
    float getRotationOffset();
    float getHalfSize();
    int getLightLevel();
    Identifier getRegistryName();

    static ConnectionType getType(Identifier id) {
        return ConnectionType.registryMap.getOrDefault(id, EnumConnectionType.LOW_SECURITY);
    }
}