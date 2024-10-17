package net.dumbcode.projectnublar.entity.ik.parts;

import net.minecraft.world.phys.Vec3;

public class Spring3d {
    public Vec3 start;
    public Vec3 startVelocity;
    public Vec3 end;
    public Vec3 endVelocity;
    public double length;
    public double stiffness;
    public double damping;
    
    public final double GRAVITY = -0.2;
    
    public Spring3d(double length, double stiffness, double damping) {
        this.length = length;
        this.stiffness = stiffness;
        this.damping = damping;
        this.start = Vec3.ZERO;
        this.startVelocity = Vec3.ZERO;
        this.end = Vec3.ZERO;
        this.endVelocity = Vec3.ZERO;
    }
    
    public void setUp(Vec3 start) {
        this.start = start;
        this.end = start.add(0, -this.length, 0);
        this.endVelocity = Vec3.ZERO;
        this.startVelocity = Vec3.ZERO;
    }
    
    public void tick() {
        double dx = this.end.x - this.start.x;
        double dy = this.end.y - this.start.y;
        double dz = this.end.z - this.start.z;
        double distance = this.start.distanceTo(this.end);
        double force = (distance - this.length) * this.stiffness;

        // Normalize the direction
        double directionX = dx / distance;
        double directionY = dy / distance;
        double directionZ = dz / distance;

        // Apply force
        this.endVelocity = this.endVelocity.add(-force * directionX, -force * directionY + GRAVITY, -force * directionZ);

        // Apply damping
        this.endVelocity = this.endVelocity.scale(this.damping);
        this.endVelocity = this.endVelocity.scale(this.damping);

        // Update position
        this.end = this.end.add(this.endVelocity);
    }
}
