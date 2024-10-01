package net.dumbcode.projectnublar.entity.ik.model;

import java.util.Optional;

public interface ModelAccessor {
    BoneAccessor getBone(String boneName);
}
