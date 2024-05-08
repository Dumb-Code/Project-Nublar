package com.nyfaria.projectnublar.api;

import com.nyfaria.projectnublar.registration.RegistryObject;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

public record TraceFossilCollection(Map<Block, RegistryObject<Block>> stoneMap) {
    public static Map<Block, RegistryObject<Block>> TRACE_FOSSILS;

    public static TraceFossilCollection register(String traceName){
        Map<Block,RegistryObject<Block>> map = new HashMap<>();
        return new TraceFossilCollection(map);
    }

}
