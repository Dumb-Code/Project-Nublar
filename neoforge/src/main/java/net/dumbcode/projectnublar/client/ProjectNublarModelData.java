package net.dumbcode.projectnublar.client;

import net.dumbcode.projectnublar.block.api.fence.Connection;
import net.minecraftforge.client.model.data.ModelProperty;

import java.util.Set;

public class ProjectNublarModelData {
    public static final ModelProperty<Set<Connection.CompiledRenderData>> CONNECTIONS = new ModelProperty<>();
    public static final ModelProperty<Double> FENCE_POLE_ROTATION_DEGS = new ModelProperty<>();
}
