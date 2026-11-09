package com.evandev.reliable_requiem.corpse.ragdoll;

import org.joml.Matrix3d;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.Random;

/**
 * A corpse's body as a ragdoll of ten rigid bones (head, torso, and each arm and leg split at the elbow or knee) joined
 * by ball and hinge joints with limits. The bones are the player model's boxes and collide with block shapes and with
 * each other. It is solved with extended position based dynamics after Müller et al., "Detailed Rigid Body Simulation
 * with Extended Position Based Dynamics" (2020): many substeps per tick, each correcting positions once.
 * <p>
 * The body starts standing on the corpse's feet and crumples back toward where the head lies, so it ends up centered on
 * the death spot. It is seeded by the corpse's UUID and steps a fixed amount per tick, so every client that sees the
 * same blocks ends up with the same pose.
 * <p>
 * Spaces: "body space" is the standing body in blocks, +x its right, +y up, +z its back, feet at 0. "Model space" is
 * the player model's, in pixels, with x and y flipped and y measured down from the neck.
 */
public final class CorpseRagdoll {
    public static final int TORSO = 0, HEAD = 1, R_ARM = 2, R_FOREARM = 3, L_ARM = 4, L_FOREARM = 5,
            R_THIGH = 6, R_SHIN = 7, L_THIGH = 8, L_SHIN = 9, BONES = 10;
    /** The bone whose model part each bone's part hangs from: the torso, or the upper half of its limb. */
    public static final int[] PARENT = {-1, TORSO, TORSO, R_ARM, TORSO, L_ARM, TORSO, R_THIGH, TORSO, L_THIGH};
    /** Where each bone's model part pivots, in model space. */
    public static final float[][] PIVOT = {
            {0, 0, 0}, {0, 0, 0}, {-5, 2, 0}, {-6, 6, 0}, {5, 2, 0}, {6, 6, 0},
            {-1.9F, 12, 0}, {-1.9F, 18, 0}, {1.9F, 12, 0}, {1.9F, 18, 0}};
    /** The player model's render scale. */
    public static final double SCALE = 0.9375;
    /** How far up the model is shifted when rendered, in blocks before {@link #SCALE}. */
    private static final double MODEL_LIFT = 1.501;
    /** Each bone's box in model space: min x y z, max x y z. */
    private static final double[][] BOX = {
            {-4, 0, -2, 4, 12, 2}, {-4, -8, -4, 4, 0, 4},
            {-8, 0, -2, -4, 6, 2}, {-8, 6, -2, -4, 12, 2}, {4, 0, -2, 8, 6, 2}, {4, 6, -2, 8, 12, 2},
            {-3.9, 12, -2, 0.1, 18, 2}, {-3.9, 18, -2, 0.1, 24, 2}, {-0.1, 12, -2, 3.9, 18, 2}, {-0.1, 18, -2, 3.9, 24, 2}};
    /** Rough share of a body's weight, in kilograms. */
    private static final double[] MASS = {30, 5, 2.5, 2, 2.5, 2, 9, 5.5, 9, 5.5};
    /** Pairs {a, b} where a's surface points are kept out of b's box. Bones joined to each other are left out. */
    private static final int[][] SELF_COLLISION = {
            {R_ARM, TORSO}, {L_ARM, TORSO}, {R_FOREARM, TORSO}, {L_FOREARM, TORSO},
            {R_FOREARM, HEAD}, {L_FOREARM, HEAD}, {R_ARM, HEAD}, {L_ARM, HEAD}, {R_FOREARM, L_FOREARM},
            {R_FOREARM, R_THIGH}, {R_FOREARM, L_THIGH}, {L_FOREARM, R_THIGH}, {L_FOREARM, L_THIGH},
            {R_THIGH, L_THIGH}, {R_SHIN, L_SHIN}, {R_SHIN, L_THIGH}, {L_SHIN, R_THIGH}, {R_SHIN, TORSO}, {L_SHIN, TORSO}};
    /** Boxes kept apart may overlap this much, so bones that touch at rest don't push each other. */
    private static final double SELF_MARGIN = 0.6 / 16 * SCALE;

    private static final int SUBSTEPS = 10;
    private static final double H = 1.0 / SUBSTEPS;
    /** Blocks per tick², a bit gentler than an item's so the collapse can be followed. */
    private static final double GRAVITY = 0.05;
    private static final double STATIC_FRICTION = 0.8;
    private static final double DYNAMIC_FRICTION = 0.6;
    /** Per tick: how fast joints lose their relative spin, and how much water slows the body. */
    private static final double JOINT_DAMPING = 0.4;
    private static final double FLUID_DRAG = 0.15;
    private static final double FLUID_GRAVITY = 0.1;
    /** The torso may drift this far sideways from the death spot (where the corpse's hitbox is)... */
    private static final double TETHER = 1.0;
    /** ...and drop this far below it. */
    private static final double MAX_DROP = 6.0;
    private static final double SETTLED_MOVEMENT = 0.05;
    private static final int SETTLED_TICKS = 10;
    /** Blocks per tick below which a bone counts as creeping, and how much of its motion it keeps each tick. */
    private static final double CREEP_SPEED = 0.015;
    private static final double CREEP_DAMPING = 0.5;
    public static final int MAX_AWAKE_TICKS = 400;
    private static final int MAX_CONTACTS = 512;

    private static final Vector3d[] CENTER = new Vector3d[BONES];
    private static final Vector3d[] HALF = new Vector3d[BONES];
    private static final Vector3d[] PIVOT_BODY = new Vector3d[BONES];
    private static final double[] INV_MASS = new double[BONES];
    private static final Vector3d[] INV_INERTIA = new Vector3d[BONES];
    private static final double[] RADIUS = new double[BONES];
    /** Points on each bone's surface that collide, relative to its center in body space: x y z, ... */
    private static final double[][] SAMPLES = new double[BONES][];
    private static final Joint[] JOINTS;

    static {
        for (int b = 0; b < BONES; b++) {
            double[] box = BOX[b];
            Vector3d lo = toBody(box[0], box[1], box[2]), hi = toBody(box[3], box[4], box[5]);
            CENTER[b] = new Vector3d(lo).add(hi).mul(0.5);
            HALF[b] = new Vector3d(hi).sub(lo).absolute().mul(0.5);
            PIVOT_BODY[b] = toBody(PIVOT[b][0], PIVOT[b][1], PIVOT[b][2]);
            INV_MASS[b] = 1 / MASS[b];
            double x = 2 * HALF[b].x, y = 2 * HALF[b].y, z = 2 * HALF[b].z;
            INV_INERTIA[b] = new Vector3d(12 / (MASS[b] * (y * y + z * z)), 12 / (MASS[b] * (x * x + z * z)), 12 / (MASS[b] * (x * x + y * y)));
            RADIUS[b] = HALF[b].length();
            SAMPLES[b] = surfaceLattice(HALF[b], box[3] - box[0], box[4] - box[1], box[5] - box[2]);
        }
        double d = Math.PI / 180;
        Vector3d up = new Vector3d(0, 1, 0), down = new Vector3d(0, -1, 0), x = new Vector3d(1, 0, 0), z = new Vector3d(0, 0, 1);
        double s50 = StrictMath.sin(50 * d), c50 = StrictMath.cos(50 * d), s35 = StrictMath.sin(35 * d), c35 = StrictMath.cos(35 * d);
        // Shoulder cones lean out to the side, hip cones forward (the front is -z).
        Vector3d rightShoulder = new Vector3d(s50, -c50, 0), leftShoulder = new Vector3d(-s50, -c50, 0), hip = new Vector3d(0, -c35, -s35);
        // The head nods forward further than it tips back.
        Vector3d neck = new Vector3d(0, StrictMath.cos(15 * d), -StrictMath.sin(15 * d));
        JOINTS = new Joint[]{
                Joint.ball(TORSO, HEAD, 0, 0, 0, neck, up, z, 30 * d, 50 * d),
                Joint.ball(TORSO, R_ARM, -5, 2, 0, rightShoulder, down, z, 85 * d, 60 * d),
                Joint.ball(TORSO, L_ARM, 5, 2, 0, leftShoulder, down, z, 85 * d, 60 * d),
                // Elbows bend the forearm to the front, knees the shin to the back.
                Joint.hinge(R_ARM, R_FOREARM, -6, 6, 0, -5 * d, 135 * d),
                Joint.hinge(L_ARM, L_FOREARM, 6, 6, 0, -5 * d, 135 * d),
                Joint.ball(TORSO, R_THIGH, -1.9, 12, 0, hip, down, x, 60 * d, 25 * d),
                Joint.ball(TORSO, L_THIGH, 1.9, 12, 0, hip, down, x, 60 * d, 25 * d),
                Joint.hinge(R_THIGH, R_SHIN, -1.9, 18, 0, -140 * d, 5 * d),
                Joint.hinge(L_THIGH, L_SHIN, 1.9, 18, 0, -140 * d, 5 * d),
        };
    }

    private final Vector3d[] pos = vectors(), prevPos = vectors(), vel = vectors(), omega = vectors(), tickPos = vectors(), windowPos = vectors();
    private final Quaterniond[] rot = quaternions(), prevRot = quaternions(), tickRot = quaternions(), windowRot = quaternions();
    private final boolean[] wet = new boolean[BONES];
    private final RagdollWorld.Boxes boxes = new RagdollWorld.Boxes();
    private final Vector3d anchor;
    /** The blocks this body may touch, min x y z and max x y z: the tether keeps it inside. */
    private final int[] region;
    private int ticks;
    private int stillTicks;
    private int awakeTicks;
    private boolean asleep;
    private boolean placed;
    /** The blocks around a settled body when it settled. */
    private double[] restingOn = new double[0];

    // Contacts with blocks found in the current substep, for the velocity pass.
    private final int[] contactBone = new int[MAX_CONTACTS];
    private final int[] contactSample = new int[MAX_CONTACTS];
    private final double[] contactNormal = new double[MAX_CONTACTS * 3];
    private final double[] contactLambda = new double[MAX_CONTACTS];
    private int contacts;

    // Scratch space, so stepping allocates nothing.
    private final Vector3d n = new Vector3d(), p = new Vector3d(), r1 = new Vector3d(), r2 = new Vector3d();
    private final Vector3d t1 = new Vector3d(), t2 = new Vector3d(), t3 = new Vector3d(), t4 = new Vector3d();
    private final double[] m = new double[9], mPrev = new double[9], mOther = new double[9], points = new double[26 * 3];
    private final double[] faceDepth = new double[6];
    private final boolean[] faceEntered = new boolean[6];
    private final Quaterniond dq = new Quaterniond();

    /** A body standing on its feet, about to crumple back toward where a corpse with this {@code yaw} has its head. */
    public CorpseRagdoll(long seed, double feetX, double feetY, double feetZ, float yaw) {
        Random random = new Random(seed);
        double rad = Math.toRadians(yaw);
        Vector3d back = new Vector3d(-StrictMath.cos(rad), 0, StrictMath.sin(rad)); // where the head ends up
        Vector3d up = new Vector3d(0, 1, 0);
        Vector3d right = new Vector3d(up).cross(back);
        Quaterniond facing = new Quaterniond().setFromNormalized(new Matrix3d(right, up, back));
        anchor = new Vector3d(feetX + back.x * 0.9, feetY + 0.2, feetZ + back.z * 0.9);
        int ax = floor(anchor.x), ay = floor(anchor.y), az = floor(anchor.z), reach = (int) Math.ceil(TETHER) + 3;
        region = new int[]{ax - reach, ay - (int) Math.ceil(MAX_DROP) - 3, az - reach, ax + reach, ay + 3, az + reach};

        // Start already giving way: hips bent and the upper body leaning back. The knees start locked straight so the
        // legs fall with the body instead of folding under it.
        Vector3d[] body = new Vector3d[BONES];
        Quaterniond[] turn = new Quaterniond[BONES];
        for (int b = 0; b < BONES; b++) {
            body[b] = new Vector3d(CENTER[b]);
            turn[b] = new Quaterniond();
        }
        double hipFlex = Math.toRadians(random.nextDouble() * 6), lean = Math.toRadians(3 + random.nextDouble() * 5);
        bend(body, turn, new int[]{R_SHIN}, PIVOT_BODY[R_SHIN], Math.toRadians(3));
        bend(body, turn, new int[]{L_SHIN}, PIVOT_BODY[L_SHIN], Math.toRadians(3));
        bend(body, turn, new int[]{R_THIGH, R_SHIN}, PIVOT_BODY[R_THIGH], hipFlex);
        bend(body, turn, new int[]{L_THIGH, L_SHIN}, PIVOT_BODY[L_THIGH], hipFlex * random.nextDouble());
        bend(body, turn, new int[]{TORSO, HEAD, R_ARM, R_FOREARM, L_ARM, L_FOREARM}, new Vector3d(0, PIVOT_BODY[R_THIGH].y, 0), lean);
        double lowest = Double.MAX_VALUE;
        for (int b : new int[]{R_SHIN, L_SHIN}) {
            for (int s = 0; s < SAMPLES[b].length; s += 3) {
                Vector3d q = turn[b].transform(new Vector3d(SAMPLES[b][s], SAMPLES[b][s + 1], SAMPLES[b][s + 2])).add(body[b]);
                lowest = Math.min(lowest, q.y);
            }
        }

        // Tip back around the feet, sway a little to one side, and let the arms flop.
        Vector3d spin = new Vector3d(right).mul(0.05 + random.nextDouble() * 0.02).fma((random.nextDouble() - 0.5) * 0.02, back);
        Vector3d feet = new Vector3d(feetX, feetY, feetZ);
        for (int b = 0; b < BONES; b++) {
            facing.transform(body[b].sub(0, lowest - 0.02, 0), pos[b]).add(feet);
            rot[b].set(facing).mul(turn[b]);
            spin.cross(new Vector3d(pos[b]).sub(feet), vel[b]);
            omega[b].set(spin);
            if (b == R_ARM || b == L_ARM || b == R_FOREARM || b == L_FOREARM) {
                omega[b].add((random.nextDouble() - 0.5) * 0.1, (random.nextDouble() - 0.5) * 0.1, (random.nextDouble() - 0.5) * 0.1);
            }
            tickPos[b].set(pos[b]);
            tickRot[b].set(rot[b]);
        }
    }

    public boolean isAsleep() {
        return asleep;
    }

    /** The blocks the body may touch: min x y z, max x y z. */
    public int[] region() {
        return region.clone();
    }

    /** Steps until the body lies still (at most {@link #MAX_AWAKE_TICKS}). */
    public void settle(RagdollWorld world) {
        while (!asleep && awakeTicks <= MAX_AWAKE_TICKS) tick(world);
    }

    /** Where the torso is now. */
    public Vector3d torso() {
        return new Vector3d(pos[TORSO]);
    }

    public void tick(RagdollWorld world) {
        for (int b = 0; b < BONES; b++) {
            tickPos[b].set(pos[b]);
            tickRot[b].set(rot[b]);
        }
        // A settled body only checks every second whether the blocks around it changed, without moving.
        if (asleep && ticks++ % 20 != 0) return;
        if (!world.isLoaded(floor(anchor.x), floor(anchor.y), floor(anchor.z))) return;

        gatherBoxes(world);
        if (asleep) {
            if (boxes.size * 6 == restingOn.length && Arrays.equals(boxes.data, 0, restingOn.length, restingOn, 0, restingOn.length)) return;
            asleep = false;
            stillTicks = 0;
            awakeTicks = 0;
        }
        if (!placed) {
            placed = true;
            climbOutOfBlocks();
        }
        for (int s = 0; s < SUBSTEPS; s++) substep();
        tether();
        // Bones that have nearly stopped lose the rest of their motion quickly, so the body comes to rest instead of
        // creeping and jittering.
        for (int b = 0; b < BONES; b++) {
            if (vel[b].lengthSquared() < CREEP_SPEED * CREEP_SPEED && omega[b].lengthSquared() * RADIUS[b] * RADIUS[b] < CREEP_SPEED * CREEP_SPEED) {
                vel[b].mul(CREEP_DAMPING);
                omega[b].mul(CREEP_DAMPING);
            }
        }

        awakeTicks++;
        // Settled once nothing has moved more than a hair over a stretch of ticks; jitter in place doesn't count.
        if (++stillTicks < SETTLED_TICKS) return;
        stillTicks = 0;
        // A body that never settles (bobbing in water, say) is stopped after a while, the same way everywhere.
        asleep = movedSinceWindow() < SETTLED_MOVEMENT || awakeTicks >= MAX_AWAKE_TICKS;
        if (asleep) {
            rest();
            gatherBoxes(world);
            restingOn = Arrays.copyOf(boxes.data, boxes.size * 6);
        }
    }

    /** The most any bone moved since the last check (counting its turn at its far end); starts a new window. */
    private double movedSinceWindow() {
        double moved = 0;
        for (int b = 0; b < BONES; b++) {
            rot[b].mul(dq.set(windowRot[b]).conjugate(), dq);
            double turned = 2 * Math.sqrt(dq.x * dq.x + dq.y * dq.y + dq.z * dq.z);
            moved = Math.max(moved, pos[b].distance(windowPos[b]) + turned * RADIUS[b]);
            windowPos[b].set(pos[b]);
            windowRot[b].set(rot[b]);
        }
        return moved;
    }

    private void rest() {
        for (int b = 0; b < BONES; b++) {
            vel[b].zero();
            omega[b].zero();
        }
    }

    /** Collects the blocks the body could touch this tick, and which bones are in water. */
    private void gatherBoxes(RagdollWorld world) {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        for (int b = 0; b < BONES; b++) {
            double reach = RADIUS[b] + 0.25 + 2 * (vel[b].length() + omega[b].length() * RADIUS[b]);
            minX = Math.min(minX, pos[b].x - reach);
            minY = Math.min(minY, pos[b].y - reach);
            minZ = Math.min(minZ, pos[b].z - reach);
            maxX = Math.max(maxX, pos[b].x + reach);
            maxY = Math.max(maxY, pos[b].y + reach);
            maxZ = Math.max(maxZ, pos[b].z + reach);
            int x = floor(pos[b].x), y = floor(pos[b].y), z = floor(pos[b].z);
            wet[b] = x >= region[0] && y >= region[1] && z >= region[2] && x <= region[3] && y <= region[4] && z <= region[5] && world.isFluid(x, y, z);
        }
        // Only blocks in the region count, so a copy of the region behaves exactly like the world.
        boxes.clear();
        world.collectBoxes(Math.max(floor(minX), region[0]), Math.max(floor(minY), region[1]), Math.max(floor(minZ), region[2]),
                Math.min(floor(maxX), region[3]), Math.min(floor(maxY), region[4]), Math.min(floor(maxZ), region[5]), boxes);
    }

    /** A death spot can leave the standing body partly inside a slope or a ceiling: raise it until it is clear. */
    private void climbOutOfBlocks() {
        for (int tries = 0; tries < 24 && insideBlocks(); tries++) {
            for (int b = 0; b < BONES; b++) {
                pos[b].y += 1.0 / 16;
                tickPos[b].set(pos[b]);
            }
        }
    }

    private boolean insideBlocks() {
        for (int b = 0; b < BONES; b++) {
            matrix(rot[b], m);
            for (int s = 0; s < SAMPLES[b].length; s += 3) {
                double px = point(b, m, s, 0), py = point(b, m, s, 1), pz = point(b, m, s, 2);
                for (int k = 0; k < boxes.size; k++) if (inside(k, px, py, pz)) return true;
            }
        }
        return false;
    }

    private void substep() {
        for (int b = 0; b < BONES; b++) {
            prevPos[b].set(pos[b]);
            prevRot[b].set(rot[b]);
            vel[b].y -= (wet[b] ? GRAVITY * FLUID_GRAVITY : GRAVITY) * H;
            if (wet[b]) {
                vel[b].mul(1 - FLUID_DRAG * H);
                omega[b].mul(1 - FLUID_DRAG * H);
            }
            pos[b].fma(H, vel[b]);
            rotate(rot[b], omega[b], H);
        }

        for (Joint joint : JOINTS) solveJoint(joint);
        for (int[] pair : SELF_COLLISION) solveSelfCollision(pair[0], pair[1]);
        contacts = 0;
        for (int b = 0; b < BONES; b++) solveBlockContacts(b);

        for (int b = 0; b < BONES; b++) {
            vel[b].set(pos[b]).sub(prevPos[b]).div(H);
            rot[b].mul(dq.set(prevRot[b]).conjugate(), dq);
            omega[b].set(dq.x, dq.y, dq.z).mul(dq.w >= 0 ? 2 / H : -2 / H);
        }
        solveVelocities();
    }

    // --- Joints ---

    private void solveJoint(Joint j) {
        int a = j.parent, b = j.child;
        if (j.hinge) {
            // Keep the hinge axes lined up, then the bend within its range.
            Vector3d a1 = rot[a].transform(t1.set(1, 0, 0)), a2 = rot[b].transform(t2.set(1, 0, 0));
            a1.cross(a2, t3);
            correctRotation(a, b, t3.x, t3.y, t3.z);
            rot[a].transform(t1.set(1, 0, 0));
            limitAngle(a, b, t1, rot[a].transform(t2.set(0, -1, 0)), rot[b].transform(t3.set(0, -1, 0)), j.min, j.max);
        } else {
            // Swing: the child's axis stays within a cone around the parent's.
            Vector3d a1 = rot[a].transform(t1.set(j.axisParent)), a2 = rot[b].transform(t2.set(j.axisChild));
            double angle = StrictMath.acos(Math.max(-1, Math.min(1, a1.dot(a2))));
            if (angle > j.max) {
                a1.cross(a2, t3);
                double len = t3.length();
                if (len > 1.0E-9) {
                    t3.mul((angle - j.max) / len);
                    correctRotation(a, b, t3.x, t3.y, t3.z);
                }
            }
            // Twist about the average of the two axes.
            a1 = rot[a].transform(t1.set(j.axisParent));
            a2 = rot[b].transform(t2.set(j.axisChild));
            Vector3d axis = a1.add(a2);
            if (axis.lengthSquared() > 1.0E-9) {
                axis.normalize();
                limitAngle(a, b, axis, rot[a].transform(t2.set(j.ref)), rot[b].transform(t3.set(j.ref)), -j.twist, j.twist);
            }
        }

        // Pin the two bones together at the joint.
        rot[a].transform(r1.set(j.anchorParent));
        rot[b].transform(r2.set(j.anchorChild));
        double cx = pos[b].x + r2.x - pos[a].x - r1.x, cy = pos[b].y + r2.y - pos[a].y - r1.y, cz = pos[b].z + r2.z - pos[a].z - r1.z;
        correctPosition(a, r1, b, r2, cx, cy, cz);
    }

    /** Keeps the angle from n1 (on bone a) to n2 (on bone b) about {@code axis} within min to max. */
    private void limitAngle(int a, int b, Vector3d axis, Vector3d n1, Vector3d n2, double min, double max) {
        n1.fma(-n1.dot(axis), axis);
        n2.fma(-n2.dot(axis), axis);
        if (n1.lengthSquared() < 1.0E-12 || n2.lengthSquared() < 1.0E-12) return;
        n1.normalize();
        n2.normalize();
        double sin = n1.cross(n2, t4).dot(axis), cos = n1.dot(n2);
        double angle = StrictMath.atan2(sin, cos);
        double excess = angle < min ? angle - min : angle > max ? angle - max : 0;
        if (excess != 0) correctRotation(a, b, axis.x * excess, axis.y * excess, axis.z * excess);
    }

    // --- Collisions ---

    /** Pushes a's surface points out of b's box. */
    private void solveSelfCollision(int a, int b) {
        double hx = HALF[b].x - SELF_MARGIN, hy = HALF[b].y - SELF_MARGIN, hz = HALF[b].z - SELF_MARGIN;
        matrix(rot[b], mOther);
        double dx = pos[a].x - pos[b].x, dy = pos[a].y - pos[b].y, dz = pos[a].z - pos[b].z, reach = RADIUS[a];
        if (Math.abs(mOther[0] * dx + mOther[3] * dy + mOther[6] * dz) > hx + reach || Math.abs(mOther[1] * dx + mOther[4] * dy + mOther[7] * dz) > hy + reach
                || Math.abs(mOther[2] * dx + mOther[5] * dy + mOther[8] * dz) > hz + reach) return;
        matrix(rot[a], m);
        double[] samples = SAMPLES[a];
        for (int s = 0; s < samples.length; s += 3) {
            double px = pos[a].x + mul(m, 0, samples, s), py = pos[a].y + mul(m, 1, samples, s), pz = pos[a].z + mul(m, 2, samples, s);
            double ox = px - pos[b].x, oy = py - pos[b].y, oz = pz - pos[b].z;
            double lx = mOther[0] * ox + mOther[3] * oy + mOther[6] * oz, ly = mOther[1] * ox + mOther[4] * oy + mOther[7] * oz, lz = mOther[2] * ox + mOther[5] * oy + mOther[8] * oz;
            double ix = hx - Math.abs(lx), iy = hy - Math.abs(ly), iz = hz - Math.abs(lz);
            if (ix <= 0 || iy <= 0 || iz <= 0) continue;
            // Out through the nearest face of b, in b's frame turned back to the world.
            double cx, cy, cz;
            if (ix <= iy && ix <= iz) {
                double k = Math.signum(lx) * ix;
                cx = mOther[0] * k; cy = mOther[3] * k; cz = mOther[6] * k;
            } else if (iy <= iz) {
                double k = Math.signum(ly) * iy;
                cx = mOther[1] * k; cy = mOther[4] * k; cz = mOther[7] * k;
            } else {
                double k = Math.signum(lz) * iz;
                cx = mOther[2] * k; cy = mOther[5] * k; cz = mOther[8] * k;
            }
            r1.set(px - pos[a].x, py - pos[a].y, pz - pos[a].z);
            r2.set(ox, oy, oz);
            correctPosition(a, r1, b, r2, cx, cy, cz);
            matrix(rot[a], m);
            matrix(rot[b], mOther);
        }
    }

    private void solveBlockContacts(int b) {
        matrix(rot[b], m);
        double ex = Math.abs(m[0]) * HALF[b].x + Math.abs(m[1]) * HALF[b].y + Math.abs(m[2]) * HALF[b].z;
        double ey = Math.abs(m[3]) * HALF[b].x + Math.abs(m[4]) * HALF[b].y + Math.abs(m[5]) * HALF[b].z;
        double ez = Math.abs(m[6]) * HALF[b].x + Math.abs(m[7]) * HALF[b].y + Math.abs(m[8]) * HALF[b].z;
        double[] box = boxes.data, samples = SAMPLES[b];
        boolean placedPoints = false, prevReady = false;
        for (int k = 0; k < boxes.size; k++) {
            int o = k * 6;
            if (pos[b].x + ex <= box[o] || pos[b].x - ex >= box[o + 3] || pos[b].y + ey <= box[o + 1] || pos[b].y - ey >= box[o + 4]
                    || pos[b].z + ez <= box[o + 2] || pos[b].z - ez >= box[o + 5]) continue;
            if (!placedPoints) {
                placePoints(b);
                placedPoints = true;
            }
            for (int s = 0; s < samples.length; s += 3) {
                double px = points[s], py = points[s + 1], pz = points[s + 2];
                if (!(px > box[o] && px < box[o + 3] && py > box[o + 1] && py < box[o + 4] && pz > box[o + 2] && pz < box[o + 5])) continue;
                if (!prevReady) {
                    matrix(prevRot[b], mPrev);
                    prevReady = true;
                }
                double qx = prevPos[b].x + mul(mPrev, 0, samples, s);
                double qy = prevPos[b].y + mul(mPrev, 1, samples, s);
                double qz = prevPos[b].z + mul(mPrev, 2, samples, s);
                double depth = exit(k, px, py, pz, qx, qy, qz, n);
                double nx = n.x, ny = n.y, nz = n.z;
                r1.set(px - pos[b].x, py - pos[b].y, pz - pos[b].z);
                double lambda = correctPosition(b, r1, -1, null, nx * depth, ny * depth, nz * depth);

                // Static friction: undo the sliding along the surface when the push into it is strong enough.
                placePoints(b);
                px = points[s];
                py = points[s + 1];
                pz = points[s + 2];
                double sx = px - qx, sy = py - qy, sz = pz - qz;
                double along = sx * nx + sy * ny + sz * nz;
                sx -= along * nx;
                sy -= along * ny;
                sz -= along * nz;
                double slide = Math.sqrt(sx * sx + sy * sy + sz * sz);
                if (slide > 1.0E-9) {
                    r1.set(px - pos[b].x, py - pos[b].y, pz - pos[b].z);
                    double w = inverseMass(b, r1, t3.set(sx / slide, sy / slide, sz / slide));
                    if (slide / w < STATIC_FRICTION * lambda) {
                        correctPosition(b, r1, -1, null, -sx, -sy, -sz);
                        placePoints(b);
                    }
                }

                if (contacts < MAX_CONTACTS) {
                    contactBone[contacts] = b;
                    contactSample[contacts] = s;
                    contactNormal[contacts * 3] = nx;
                    contactNormal[contacts * 3 + 1] = ny;
                    contactNormal[contacts * 3 + 2] = nz;
                    contactLambda[contacts] = lambda;
                    contacts++;
                }
            }
        }
    }

    /** Puts bone b's sample points in the world into {@link #points}. */
    private void placePoints(int b) {
        matrix(rot[b], m);
        double[] samples = SAMPLES[b];
        double x = pos[b].x, y = pos[b].y, z = pos[b].z;
        for (int s = 0; s < samples.length; s += 3) {
            points[s] = x + mul(m, 0, samples, s);
            points[s + 1] = y + mul(m, 1, samples, s);
            points[s + 2] = z + mul(m, 2, samples, s);
        }
    }

    private boolean inside(int k, double x, double y, double z) {
        double[] box = boxes.data;
        int o = k * 6;
        return x > box[o] && x < box[o + 3] && y > box[o + 1] && y < box[o + 4] && z > box[o + 2] && z < box[o + 5];
    }

    /** Face order when depths tie: up first, down last. Faces are -x +x -y +y -z +z. */
    private static final int[] FACE_ORDER = {3, 0, 1, 4, 5, 2};

    /**
     * The shortest way out of box k for a point at p that was at q a substep ago: through a face it came in by when
     * possible, and never into a neighboring block. Puts the direction in {@code normal} and returns the distance.
     */
    private double exit(int k, double px, double py, double pz, double qx, double qy, double qz, Vector3d normal) {
        double[] box = boxes.data;
        int o = k * 6;
        faceDepth[0] = px - box[o];
        faceDepth[1] = box[o + 3] - px;
        faceDepth[2] = py - box[o + 1];
        faceDepth[3] = box[o + 4] - py;
        faceDepth[4] = pz - box[o + 2];
        faceDepth[5] = box[o + 5] - pz;
        faceEntered[0] = qx <= box[o];
        faceEntered[1] = qx >= box[o + 3];
        faceEntered[2] = qy <= box[o + 1];
        faceEntered[3] = qy >= box[o + 4];
        faceEntered[4] = qz <= box[o + 2];
        faceEntered[5] = qz >= box[o + 5];
        int best = -1;
        for (int pass = 0; pass < 2 && best < 0; pass++) {
            double bestDepth = Double.MAX_VALUE;
            for (int f : FACE_ORDER) {
                if (pass == 0 && !faceEntered[f]) continue;
                if (faceDepth[f] >= bestDepth || faceBlocked(k, f, px, py, pz, faceDepth[f])) continue;
                best = f;
                bestDepth = faceDepth[f];
            }
        }
        if (best < 0) best = 3;
        int axis = best / 2;
        double sign = (best & 1) == 0 ? -1 : 1;
        normal.set(axis == 0 ? sign : 0, axis == 1 ? sign : 0, axis == 2 ? sign : 0);
        return faceDepth[best];
    }

    /** Whether leaving box k through face f would land the point in another block. */
    private boolean faceBlocked(int k, int f, double px, double py, double pz, double depth) {
        int axis = f / 2;
        double step = ((f & 1) == 0 ? -1 : 1) * (depth + 1.0E-3);
        double x = px + (axis == 0 ? step : 0), y = py + (axis == 1 ? step : 0), z = pz + (axis == 2 ? step : 0);
        for (int j = 0; j < boxes.size; j++) if (j != k && inside(j, x, y, z)) return true;
        return false;
    }

    /** Dynamic friction and no bounce at block contacts, then damping at the joints. */
    private void solveVelocities() {
        for (int c = 0; c < contacts; c++) {
            int b = contactBone[c], s = contactSample[c];
            double nx = contactNormal[c * 3], ny = contactNormal[c * 3 + 1], nz = contactNormal[c * 3 + 2];
            rot[b].transform(r1.set(SAMPLES[b][s], SAMPLES[b][s + 1], SAMPLES[b][s + 2]));
            Vector3d v = omega[b].cross(r1, t1).add(vel[b]);
            double vn = v.x * nx + v.y * ny + v.z * nz;
            double tx = v.x - nx * vn, ty = v.y - ny * vn, tz = v.z - nz * vn;
            double dvx = -nx * vn, dvy = -ny * vn, dvz = -nz * vn;
            double vt = Math.sqrt(tx * tx + ty * ty + tz * tz);
            if (vt > 1.0E-9) {
                double f = Math.min(DYNAMIC_FRICTION * contactLambda[c] / H, vt) / vt;
                dvx -= tx * f;
                dvy -= ty * f;
                dvz -= tz * f;
            }
            double dv = Math.sqrt(dvx * dvx + dvy * dvy + dvz * dvz);
            if (dv < 1.0E-12) continue;
            Vector3d dir = t3.set(dvx / dv, dvy / dv, dvz / dv);
            double w = inverseMass(b, r1, dir);
            p.set(dir).mul(dv / w);
            vel[b].fma(INV_MASS[b], p);
            r1.cross(p, t3);
            omega[b].add(invInertia(b, t3, t4));
        }

        double k = Math.min(JOINT_DAMPING * H, 1);
        for (Joint j : JOINTS) {
            int a = j.parent, b = j.child;
            t1.set(omega[b]).sub(omega[a]).mul(k);
            double len = t1.length();
            if (len < 1.0E-12) continue;
            t1.div(len);
            invInertia(a, t1, t2);
            invInertia(b, t1, t3);
            double impulse = len / (t1.dot(t2) + t1.dot(t3));
            omega[a].fma(impulse, t2);
            omega[b].fma(-impulse, t3);
        }
    }

    /** Keeps the torso over the death spot and from falling far below it (a corpse floating in the air). */
    private void tether() {
        Vector3d c = pos[TORSO];
        double dx = c.x - anchor.x, dz = c.z - anchor.z;
        double d = Math.sqrt(dx * dx + dz * dz);
        double kx = d > TETHER ? dx * (d - TETHER) / d : 0, kz = d > TETHER ? dz * (d - TETHER) / d : 0;
        double ky = Math.min(0, c.y - (anchor.y - MAX_DROP));
        if (kx == 0 && ky == 0 && kz == 0) return;
        for (int b = 0; b < BONES; b++) {
            pos[b].sub(kx, ky, kz);
            if (ky != 0) vel[b].y = Math.max(vel[b].y, 0);
            if (kx != 0 || kz != 0) vel[b].mul(0.5, 1, 0.5);
        }
    }

    // --- XPBD core ---

    /**
     * Moves the point at r1 on bone a by (cx, cy, cz) and the point at r2 on bone b (-1 for a block) the opposite way,
     * shared by their generalized inverse masses. Returns the Lagrange multiplier (the size of the push).
     */
    private double correctPosition(int a, Vector3d r1, int b, Vector3d r2, double cx, double cy, double cz) {
        double c = Math.sqrt(cx * cx + cy * cy + cz * cz);
        if (c < 1.0E-12) return 0;
        Vector3d dir = t3.set(cx / c, cy / c, cz / c);
        double w = inverseMass(a, r1, dir) + (b >= 0 ? inverseMass(b, r2, dir) : 0);
        double lambda = c / w;
        p.set(dir).mul(lambda);
        push(a, r1, 1);
        if (b >= 0) push(b, r2, -1);
        return lambda;
    }

    /** Applies the positional impulse {@link #p} at r on bone b, with this sign. */
    private void push(int b, Vector3d r, double sign) {
        pos[b].fma(sign * INV_MASS[b], p);
        r.cross(p, t1);
        rotate(rot[b], invInertia(b, t1, t2), sign);
    }

    /** Turns bone a by the rotation vector (cx, cy, cz) and bone b the opposite way, shared by their inertia. */
    private void correctRotation(int a, int b, double cx, double cy, double cz) {
        double angle = Math.sqrt(cx * cx + cy * cy + cz * cz);
        if (angle < 1.0E-12) return;
        Vector3d axis = n.set(cx / angle, cy / angle, cz / angle);
        Vector3d ia = invInertia(a, axis, r1), ib = invInertia(b, axis, r2);
        double lambda = angle / (axis.dot(ia) + axis.dot(ib));
        rotate(rot[a], ia, lambda);
        rotate(rot[b], ib, -lambda);
    }

    /** 1/m + (r × n) · I⁻¹ (r × n): how easily a push along n at r moves bone b. */
    private double inverseMass(int b, Vector3d r, Vector3d dir) {
        r.cross(dir, t1);
        invInertia(b, t1, t2);
        return INV_MASS[b] + t1.dot(t2);
    }

    /** out = I⁻¹ v for bone b, in world space. */
    private Vector3d invInertia(int b, Vector3d v, Vector3d out) {
        rot[b].transformInverse(v, out).mul(INV_INERTIA[b]);
        return rot[b].transform(out);
    }

    /** q += ½ [v·scale, 0] q, normalized: turns q by the small rotation vector v·scale. */
    private static void rotate(Quaterniond q, Vector3d v, double scale) {
        double ax = v.x * scale * 0.5, ay = v.y * scale * 0.5, az = v.z * scale * 0.5;
        double x = q.x, y = q.y, z = q.z, w = q.w;
        q.set(x + ax * w + ay * z - az * y, y + ay * w + az * x - ax * z, z + az * w + ax * y - ay * x, w - ax * x - ay * y - az * z).normalize();
    }

    // --- Pose ---

    /** The body at {@code partialTick} between the last two ticks, with the torso placed relative to the entity at origin. */
    public RagdollPose pose(float partialTick, double originX, double originY, double originZ, RagdollPose out) {
        Vector3d[] at = new Vector3d[BONES];
        Quaterniond[] turn = new Quaterniond[BONES];
        for (int b = 0; b < BONES; b++) {
            at[b] = new Vector3d(tickPos[b]).lerp(pos[b], partialTick);
            turn[b] = new Quaterniond(tickRot[b]).nlerp(rot[b], partialTick);
        }
        out.center.set((float) (at[TORSO].x - originX), (float) (at[TORSO].y - originY), (float) (at[TORSO].z - originZ));
        out.rotation.set(turn[TORSO]);

        // Each bone's model part in the torso's model space...
        Quaterniond toTorso = new Quaterniond(turn[TORSO]).conjugate();
        Vector3d[] pivot = new Vector3d[BONES];
        Quaterniond[] partRot = new Quaterniond[BONES];
        for (int b = 1; b < BONES; b++) {
            Vector3d v = turn[b].transform(new Vector3d(PIVOT_BODY[b]).sub(CENTER[b])).add(at[b]).sub(at[TORSO]);
            pivot[b] = toModel(toTorso.transform(v).add(CENTER[TORSO]));
            Quaterniond q = new Quaterniond(toTorso).mul(turn[b]);
            partRot[b] = new Quaterniond(-q.x, -q.y, q.z, q.w); // the same turn with x and y flipped
        }
        // ...then relative to the part it hangs from.
        Quaternionf single = new Quaternionf();
        Vector3f euler = new Vector3f();
        for (int b = 1; b < BONES; b++) {
            int parent = PARENT[b];
            Vector3d at2 = pivot[b];
            Quaterniond q = partRot[b];
            if (parent != TORSO) {
                Quaterniond inverse = new Quaterniond(partRot[parent]).conjugate();
                at2 = inverse.transform(new Vector3d(pivot[b]).sub(pivot[parent]));
                q = inverse.mul(partRot[b]);
            }
            single.set(q).getEulerAnglesZYX(euler);
            float[] part = out.parts[b];
            part[0] = (float) at2.x;
            part[1] = (float) at2.y;
            part[2] = (float) at2.z;
            part[3] = euler.x;
            part[4] = euler.y;
            part[5] = euler.z;
        }
        return out;
    }

    /** Where the torso's center sits in the standing body, for placing the model around it. */
    public static float torsoCenterY() {
        return (float) CENTER[TORSO].y;
    }

    // --- Helpers ---

    private static Vector3d toBody(double x, double y, double z) {
        return new Vector3d(-x / 16 * SCALE, (MODEL_LIFT - y / 16) * SCALE, z / 16 * SCALE);
    }

    private static Vector3d toModel(Vector3d v) {
        return new Vector3d(-v.x / SCALE * 16, (MODEL_LIFT - v.y / SCALE) * 16, v.z / SCALE * 16);
    }

    /** Points on a box's surface no more than four pixels apart, given its size in pixels. */
    private static double[] surfaceLattice(Vector3d half, double sizeX, double sizeY, double sizeZ) {
        int nx = Math.max(2, (int) Math.ceil(sizeX / 4) + 1), ny = Math.max(2, (int) Math.ceil(sizeY / 4) + 1), nz = Math.max(2, (int) Math.ceil(sizeZ / 4) + 1);
        double[] out = new double[nx * ny * nz * 3];
        int count = 0;
        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                for (int k = 0; k < nz; k++) {
                    if (i != 0 && i != nx - 1 && j != 0 && j != ny - 1 && k != 0 && k != nz - 1) continue;
                    out[count++] = half.x * (2.0 * i / (nx - 1) - 1);
                    out[count++] = half.y * (2.0 * j / (ny - 1) - 1);
                    out[count++] = half.z * (2.0 * k / (nz - 1) - 1);
                }
            }
        }
        return Arrays.copyOf(out, count);
    }

    /** Turns some bones of a standing body about a pivot, around the body's x axis. */
    private static void bend(Vector3d[] body, Quaterniond[] turn, int[] bones, Vector3d pivot, double angle) {
        Quaterniond q = new Quaterniond(StrictMath.sin(angle / 2), 0, 0, StrictMath.cos(angle / 2));
        for (int b : bones) {
            q.transform(body[b].sub(pivot)).add(pivot);
            q.mul(turn[b], turn[b]);
        }
    }

    /** Row-major rotation matrix of a unit quaternion. */
    private static void matrix(Quaterniond q, double[] out) {
        double x = q.x, y = q.y, z = q.z, w = q.w;
        out[0] = 1 - 2 * (y * y + z * z);
        out[1] = 2 * (x * y - z * w);
        out[2] = 2 * (x * z + y * w);
        out[3] = 2 * (x * y + z * w);
        out[4] = 1 - 2 * (x * x + z * z);
        out[5] = 2 * (y * z - x * w);
        out[6] = 2 * (x * z - y * w);
        out[7] = 2 * (y * z + x * w);
        out[8] = 1 - 2 * (x * x + y * y);
    }

    private static double mul(double[] m, int row, double[] v, int s) {
        return m[row * 3] * v[s] + m[row * 3 + 1] * v[s + 1] + m[row * 3 + 2] * v[s + 2];
    }

    /** One coordinate of sample s of bone b, in the world. */
    private double point(int b, double[] m, int s, int axis) {
        return pos[b].get(axis) + mul(m, axis, SAMPLES[b], s);
    }

    private static int floor(double v) {
        int i = (int) v;
        return v < i ? i - 1 : i;
    }

    private static Vector3d[] vectors() {
        Vector3d[] a = new Vector3d[BONES];
        for (int i = 0; i < BONES; i++) a[i] = new Vector3d();
        return a;
    }

    private static Quaterniond[] quaternions() {
        Quaterniond[] a = new Quaterniond[BONES];
        for (int i = 0; i < BONES; i++) a[i] = new Quaterniond();
        return a;
    }

    /** A joint between two bones, with its anchor and axes in each bone's own space. */
    private static final class Joint {
        final int parent, child;
        final boolean hinge;
        final Vector3d anchorParent, anchorChild;
        /** Ball joints: the parent's cone axis, the child's bone axis, and an axis across both for measuring twist. */
        final Vector3d axisParent, axisChild, ref;
        /** Hinges: the bend range. Ball joints: max is the cone's half angle. */
        final double min, max;
        final double twist;

        private Joint(int parent, int child, boolean hinge, double x, double y, double z, Vector3d axisParent, Vector3d axisChild,
                      Vector3d ref, double min, double max, double twist) {
            this.parent = parent;
            this.child = child;
            this.hinge = hinge;
            Vector3d at = toBody(x, y, z);
            anchorParent = new Vector3d(at).sub(CENTER[parent]);
            anchorChild = new Vector3d(at).sub(CENTER[child]);
            this.axisParent = axisParent;
            this.axisChild = axisChild;
            this.ref = ref;
            this.min = min;
            this.max = max;
            this.twist = twist;
        }

        static Joint ball(int parent, int child, double x, double y, double z, Vector3d cone, Vector3d bone, Vector3d ref, double swing, double twist) {
            return new Joint(parent, child, false, x, y, z, cone, bone, ref, 0, swing, twist);
        }

        static Joint hinge(int parent, int child, double x, double y, double z, double min, double max) {
            return new Joint(parent, child, true, x, y, z, null, null, null, min, max, 0);
        }
    }
}
