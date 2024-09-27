package net.dumbcode.projectnublar.entity.ik.parts;

import net.dumbcode.projectnublar.entity.ik.parts.ik_chains.IKChain;
import net.dumbcode.projectnublar.entity.ik.util.ArrayUtil;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class Rope {
    public List<Segment> segments = new ArrayList<>();
    public Vec3 endJoint = Vec3.ZERO;

    public Rope(double length, double segmentLength) {
        double segmentCount = length / segmentLength;
        for (int i = 0; i < segmentCount; i++) {
            this.segments.add(new Segment.Builder().length(segmentLength).build());
        }
    }

    public void tick(Vec3 base) {
        this.getFirst().move(base);

        for (int i = 0; i < this.segments.size() - 1; i++) {
            Segment currentSegment = this.segments.get(i);
            Segment nextSegment = this.segments.get(i + 1);

            nextSegment.move(IKChain.moveSegment(nextSegment.getPosition(), currentSegment.getPosition(), currentSegment.length));
        }
        this.endJoint = IKChain.moveSegment(this.endJoint, this.getLast().getPosition(), this.getLast().length);
    }

    public double maxLength() {
        double totalLength = 0;
        for (Segment segment: this.segments) {
            totalLength += segment.length;
        }
        return totalLength;
    }

    public List<Vec3> getJoints() {
        List<Vec3> joints = new ArrayList<>();
        for (Segment segment : this.segments) {
            joints.add(segment.getPosition());
        }
        joints.add(this.endJoint);
        return joints;
    }

    public Segment getFirst() {
        return ArrayUtil.getFirst(this.segments);
    }

    public Segment getLast() {
        return ArrayUtil.getLast(this.segments);
    }
}
