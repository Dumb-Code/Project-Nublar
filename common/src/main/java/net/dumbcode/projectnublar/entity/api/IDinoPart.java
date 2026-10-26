package net.dumbcode.projectnublar.entity.api;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;

public interface IDinoPart {
    boolean hurtServer(ServerLevel level, DamageSource source, float amount);
    double getX();
    double getY();
    double getZ();
    void setPos(double x, double y, double z);
}
