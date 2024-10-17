package net.dumbcode.projectnublar.entity.ik.parts;

public class Spring1d {
    public final double GRAVITY = -0.2;
    public double end;
    public double endVelocity;
    public double length;
    public double stiffness;
    public double damping;

    public Spring1d(double length, double stiffness, double damping) {
        this.length = length;
        this.end = length;
        this.endVelocity = 0;
        this.stiffness = stiffness;
        this.damping = damping;
    }

    public void tick() {
        double force = this.getForce();

        this.endVelocity -= force;

        this.endVelocity = this.endVelocity * this.damping;
        this.endVelocity = this.endVelocity * this.damping;

        this.end += this.endVelocity;
    }

    public double getForce() {
        return (this.end - this.length) * this.stiffness;
    }
}
