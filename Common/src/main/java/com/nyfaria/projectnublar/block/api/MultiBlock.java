package com.nyfaria.projectnublar.block.api;

import net.minecraft.world.level.block.state.properties.IntegerProperty;

public interface MultiBlock {
    public static IntegerProperty ROWS = IntegerProperty.create("rows", 0, 2);
    public static IntegerProperty COLUMNS = IntegerProperty.create("columns", 0, 1);
    public static IntegerProperty DEPTH = IntegerProperty.create("depth", 0, 1);

}
