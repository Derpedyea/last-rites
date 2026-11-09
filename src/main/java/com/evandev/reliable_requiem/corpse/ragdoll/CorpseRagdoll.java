package com.evandev.reliable_requiem.corpse.ragdoll;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Random;
import java.util.UUID;

/**
 * A corpse's body as a Verlet ragdoll (points joined by distance constraints, after Jakobsen's "Advanced Character
 * Physics"), simulated on the client only. It starts standing on the corpse's feet and falls back toward where the head
 * lies, so it ends up centered on the death spot.
 * <p>
 * The simulation is seeded by the corpse's UUID and steps a fixed amount per tick, so every client that sees the same
 * blocks ends up with the same pose. Players near a fresh corpse watch it fall; anyone who sees it later gets the settled
 * pose straight away.
 */
public final class CorpseRagdoll {
    // Points: shoulder and hip joints (the rigid torso), head center, hand and foot ends.
    private static final int RS = 0, LS = 1, RH = 2, LH = 3, HEAD = 4, RHAND = 5, LHAND = 6, RFOOT = 7, LFOOT = 8, N = 9;
    /** The player model's render scale. */
    private static final float S = 0.9375F;
    /**
     * Standing body in blocks, before {@link #S}: +x the body's right, +y up, +z its back, feet at 0. The joints sit where
     * the model's arms and legs pivot.
     */
    private static final float[][] REST = {
            {0.3125F, 1.375F, 0}, {-0.3125F, 1.375F, 0}, {0.11875F, 0.75F, 0}, {-0.11875F, 0.75F, 0},
            {0, 1.75F, 0}, {0.3125F, 0.75F, 0}, {-0.3125F, 0.75F, 0}, {0.11875F, 0, 0}, {-0.11875F, 0, 0}};
    /** Rest center of the torso points, in the same space. */
    public static final float TORSO_CENTER_Y = 1.0625F * S;
    private static final float NECK_ABOVE_SHOULDERS = 0.125F * S;
    private static final double[] RADIUS = {0.12, 0.12, 0.12, 0.12, 0.23, 0.11, 0.11, 0.11, 0.11};
    private static final double[] INV_MASS = {1, 1, 1, 1, 1, 1.6, 1.6, 1.3, 1.3};
    private static final int SUBSTEPS = 4;
    private static final int ITERATIONS = 6;
    private static final double GRAVITY = 0.08 / (SUBSTEPS * SUBSTEPS);
    private static final double AIR_DAMPING = 0.995;
    private static final double FLUID_DAMPING = 0.85;
    private static final double FRICTION = 0.6;
    /** The torso may drift this far sideways from the death spot (where the corpse's hitbox is)... */
    private static final double TETHER = 1.0;
    /** ...and drop this far below it. */
    private static final double MAX_DROP = 2.0;
    private static final double SETTLED_MOVEMENT = 0.002;
    private static final int SETTLED_TICKS = 10;
    private static final int MAX_FAST_FORWARD_TICKS = 400;

    private static final int[][] LINKS;
    private static final double[][] LINK_RANGE;
    /** Points on the body between the simulated ones that also collide: {indices}, weights, radius. */
    private static final int[][] SAMPLE_POINTS = {{RS, RHAND}, {LS, LHAND}, {RH, RFOOT}, {LH, LFOOT}, {RS, LS, RH, LH}, {RS, RH}, {LS, LH}};
    private static final double[] SAMPLE_RADIUS = {0.11, 0.11, 0.11, 0.11, 0.12, 0.12, 0.12};

    static {
        // {a, b, min factor, max factor} of the rest distance; exact for bones, ranges for joint limits.
        double[][] spec = {
                {RS, LS, 1, 1}, {RH, LH, 1, 1}, {RS, RH, 1, 1}, {LS, LH, 1, 1}, {RS, LH, 1, 1}, {LS, RH, 1, 1},
                {RS, RHAND, 1, 1}, {LS, LHAND, 1, 1}, {RH, RFOOT, 1, 1}, {LH, LFOOT, 1, 1},
                // neck: the head tilts and nods a little around the top of the torso
                {HEAD, RS, 0.86, 1.15}, {HEAD, LS, 0.86, 1.15}, {HEAD, RH, 0.9, 1.02}, {HEAD, LH, 0.9, 1.02},
                // hands stay out of the chest, feet don't fold up past the hips or cross far
                {RHAND, LS, 0.55, 10}, {LHAND, RS, 0.55, 10}, {RHAND, RH, 0.3, 10}, {LHAND, LH, 0.3, 10},
                {RFOOT, RS, 0.7, 10}, {LFOOT, LS, 0.7, 10}, {RFOOT, LFOOT, 0.5, 4},
        };
        LINKS = new int[spec.length][2];
        LINK_RANGE = new double[spec.length][2];
        for (int i = 0; i < spec.length; i++) {
            int a = (int) spec[i][0], b = (int) spec[i][1];
            double rest = S * Math.sqrt(sq(REST[a][0] - REST[b][0]) + sq(REST[a][1] - REST[b][1]) + sq(REST[a][2] - REST[b][2]));
            LINKS[i][0] = a;
            LINKS[i][1] = b;
            LINK_RANGE[i][0] = rest * spec[i][2];
            LINK_RANGE[i][1] = rest * spec[i][3];
        }
    }

    /** Entity ids of corpses the server just made, which should be seen falling. */
    private static final IntSet FRESH = new IntOpenHashSet();

    public static void markFresh(int entityId) {
        if (FRESH.size() > 256) FRESH.clear();
        FRESH.add(entityId);
    }

    private final double[] pos = new double[N * 3];
    private final double[] prev = new double[N * 3];
    private final double[] tickStart = new double[N * 3];
    private final boolean[] contact = new boolean[N];
    private final double[] push = new double[3];
    private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
    private final Vec3 anchor;
    private int ticks;
    private int stillTicks;
    private boolean asleep;

    /** A body standing on {@code feet}, about to fall back toward where a corpse with this {@code yaw} has its head. */
    private CorpseRagdoll(UUID seed, Vec3 feet, float yaw) {
        double rad = Math.toRadians(yaw);
        Vec3 back = new Vec3(-Math.cos(rad), 0, Math.sin(rad)); // where the head ends up
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = up.cross(back);
        anchor = feet.add(back.scale(0.9)).add(0, 0.2, 0);

        Random random = new Random(seed.getMostSignificantBits() ^ seed.getLeastSignificantBits());
        double fall = 0.02 + random.nextDouble() * 0.015;
        double sway = (random.nextDouble() - 0.5) * 0.03;
        for (int i = 0; i < N; i++) {
            Vec3 p = feet.add(right.scale(REST[i][0] * S)).add(up.scale(REST[i][1] * S + 0.12)).add(back.scale(REST[i][2] * S));
            set(pos, i, p.x, p.y, p.z);
            // Per substep: the top tips back and sways, and the arms flop a little.
            double h = REST[i][1] / 1.75;
            Vec3 v = back.scale(fall * (h - 0.1)).add(right.scale(sway * h));
            if (i == RHAND || i == LHAND) {
                v = v.add((random.nextDouble() - 0.5) * 0.03, 0.01, (random.nextDouble() - 0.5) * 0.03);
            }
            set(prev, i, p.x - v.x, p.y - v.y, p.z - v.z);
        }
        System.arraycopy(pos, 0, tickStart, 0, pos.length);
    }

    /** The ragdoll for a corpse first seen now: falling if it was just made, else already lying still. */
    public static CorpseRagdoll create(int entityId, UUID seed, Vec3 feet, float yaw, Level level) {
        CorpseRagdoll ragdoll = new CorpseRagdoll(seed, feet, yaw);
        if (!FRESH.remove(entityId)) {
            for (int i = 0; i < MAX_FAST_FORWARD_TICKS && !ragdoll.asleep; i++) ragdoll.tick(level);
        }
        return ragdoll;
    }

    public void tick(Level level) {
        System.arraycopy(pos, 0, tickStart, 0, pos.length);
        // A settled body only checks every second whether the ground under it went away.
        if (asleep && ticks++ % 20 != 0) return;
        if (!level.hasChunkAt(BlockPos.containing(anchor))) return;

        for (int s = 0; s < SUBSTEPS; s++) step(level);
        tether();

        double moved = 0;
        for (int i = 0; i < pos.length; i++) moved = Math.max(moved, Math.abs(pos[i] - tickStart[i]));
        if (moved < SETTLED_MOVEMENT) {
            if (++stillTicks >= SETTLED_TICKS) asleep = true;
        } else {
            stillTicks = 0;
            asleep = false;
        }
    }

    private void step(Level level) {
        for (int i = 0; i < N; i++) {
            int k = i * 3;
            cursor.set(pos[k], pos[k + 1], pos[k + 2]);
            boolean fluid = !level.getFluidState(cursor).isEmpty();
            double damping = fluid ? FLUID_DAMPING : AIR_DAMPING;
            double gravity = fluid ? GRAVITY * 0.15 : GRAVITY;
            for (int a = 0; a < 3; a++) {
                double v = (pos[k + a] - prev[k + a]) * damping;
                prev[k + a] = pos[k + a];
                pos[k + a] += v - (a == 1 ? gravity : 0);
            }
            contact[i] = false;
        }

        for (int it = 0; it < ITERATIONS; it++) {
            for (int c = 0; c < LINKS.length; c++) solveLink(LINKS[c][0], LINKS[c][1], LINK_RANGE[c][0], LINK_RANGE[c][1]);
            for (int i = 0; i < N; i++) {
                int k = i * 3;
                if (pushOut(level, pos[k], pos[k + 1], pos[k + 2], RADIUS[i])) {
                    pos[k] += push[0];
                    pos[k + 1] += push[1];
                    pos[k + 2] += push[2];
                    contact[i] = true;
                }
            }
            for (int sp = 0; sp < SAMPLE_POINTS.length; sp++) {
                int[] idx = SAMPLE_POINTS[sp];
                double x = 0, y = 0, z = 0;
                for (int i : idx) {
                    x += pos[i * 3];
                    y += pos[i * 3 + 1];
                    z += pos[i * 3 + 2];
                }
                if (pushOut(level, x / idx.length, y / idx.length, z / idx.length, SAMPLE_RADIUS[sp])) {
                    for (int i : idx) {
                        pos[i * 3] += push[0];
                        pos[i * 3 + 1] += push[1];
                        pos[i * 3 + 2] += push[2];
                        contact[i] = true;
                    }
                }
            }
        }

        // Touching the ground drags: kill most of the velocity of points in contact.
        for (int i = 0; i < N; i++) {
            if (!contact[i]) continue;
            int k = i * 3;
            for (int a = 0; a < 3; a++) prev[k + a] = pos[k + a] - (pos[k + a] - prev[k + a]) * (1 - FRICTION);
        }
    }

    private void solveLink(int a, int b, double min, double max) {
        int ka = a * 3, kb = b * 3;
        double dx = pos[kb] - pos[ka], dy = pos[kb + 1] - pos[ka + 1], dz = pos[kb + 2] - pos[ka + 2];
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1.0E-9 || (len >= min && len <= max)) return;
        double target = len < min ? min : max;
        double wa = INV_MASS[a], wb = INV_MASS[b];
        double f = (len - target) / (len * (wa + wb));
        pos[ka] += dx * f * wa;
        pos[ka + 1] += dy * f * wa;
        pos[ka + 2] += dz * f * wa;
        pos[kb] -= dx * f * wb;
        pos[kb + 1] -= dy * f * wb;
        pos[kb + 2] -= dz * f * wb;
    }

    /** Puts into {@link #push} how far a ball at x y z needs to move to get out of blocks; false if it isn't in any. */
    private boolean pushOut(Level level, double x, double y, double z, double r) {
        push[0] = push[1] = push[2] = 0;
        boolean hit = false;
        int x0 = Mth.floor(x - r), x1 = Mth.floor(x + r);
        int y0 = Mth.floor(y - r), y1 = Mth.floor(y + r);
        int z0 = Mth.floor(z - r), z1 = Mth.floor(z + r);
        for (int bx = x0; bx <= x1; bx++) {
            for (int by = y0; by <= y1; by++) {
                for (int bz = z0; bz <= z1; bz++) {
                    cursor.set(bx, by, bz);
                    BlockState state = level.getBlockState(cursor);
                    if (state.isAir()) continue;
                    VoxelShape shape = state.getCollisionShape(level, cursor);
                    if (shape.isEmpty()) continue;
                    for (AABB box : shape.toAabbs()) {
                        double px = x + push[0], py = y + push[1], pz = z + push[2];
                        double minX = box.minX + bx, minY = box.minY + by, minZ = box.minZ + bz;
                        double maxX = box.maxX + bx, maxY = box.maxY + by, maxZ = box.maxZ + bz;
                        double cx = Mth.clamp(px, minX, maxX), cy = Mth.clamp(py, minY, maxY), cz = Mth.clamp(pz, minZ, maxZ);
                        double dx = px - cx, dy = py - cy, dz = pz - cz;
                        double d2 = dx * dx + dy * dy + dz * dz;
                        if (d2 >= r * r) continue;
                        hit = true;
                        if (d2 > 1.0E-12) {
                            double d = Math.sqrt(d2), k = (r - d) / d;
                            push[0] += dx * k;
                            push[1] += dy * k;
                            push[2] += dz * k;
                        } else {
                            // Center inside the box: climb out of the top. Sideways would often be into the next block.
                            push[1] += maxY - py + r;
                        }
                    }
                }
            }
        }
        return hit;
    }

    /** Keeps the torso over the death spot and from falling far below it (a corpse floating in the air). */
    private void tether() {
        double cx = 0, cy = 0, cz = 0;
        for (int i = RS; i <= LH; i++) {
            cx += pos[i * 3] / 4;
            cy += pos[i * 3 + 1] / 4;
            cz += pos[i * 3 + 2] / 4;
        }
        double dx = cx - anchor.x, dz = cz - anchor.z;
        double d = Math.sqrt(dx * dx + dz * dz);
        double kx = d > TETHER ? dx * (d - TETHER) / d : 0, kz = d > TETHER ? dz * (d - TETHER) / d : 0;
        double ky = Math.min(0, cy - (anchor.y - MAX_DROP));
        if (kx == 0 && ky == 0 && kz == 0) return;
        for (int i = 0; i < N; i++) {
            pos[i * 3] -= kx;
            pos[i * 3 + 1] -= ky;
            pos[i * 3 + 2] -= kz;
        }
    }

    /** The body at {@code partialTick} between the last two ticks, relative to {@code origin}. */
    public RagdollPose pose(float partialTick, Vec3 origin, RagdollPose out) {
        Vector3f[] p = new Vector3f[N];
        for (int i = 0; i < N; i++) {
            int k = i * 3;
            p[i] = new Vector3f(
                    (float) (Mth.lerp(partialTick, tickStart[k], pos[k]) - origin.x),
                    (float) (Mth.lerp(partialTick, tickStart[k + 1], pos[k + 1]) - origin.y),
                    (float) (Mth.lerp(partialTick, tickStart[k + 2], pos[k + 2]) - origin.z));
        }
        Vector3f shoulders = new Vector3f(p[RS]).add(p[LS]).mul(0.5F);
        Vector3f hips = new Vector3f(p[RH]).add(p[LH]).mul(0.5F);
        Vector3f up = new Vector3f(shoulders).sub(hips).normalize();
        Vector3f right = new Vector3f(p[RS]).sub(p[LS]).add(p[RH]).sub(p[LH]);
        right.sub(new Vector3f(up).mul(right.dot(up))).normalize();
        Vector3f back = new Vector3f(right).cross(up);
        Matrix3f frame = new Matrix3f(right, up, back);

        out.center.set(shoulders).add(hips).mul(0.5F);
        out.rotation.setFromNormalized(frame);
        Vector3f neck = new Vector3f(up).mul(NECK_ABOVE_SHOULDERS).add(shoulders);
        limb(frame, neck, p[HEAD], -1, out.head);
        limb(frame, p[RS], p[RHAND], 1, out.rightArm);
        limb(frame, p[LS], p[LHAND], 1, out.leftArm);
        limb(frame, p[RH], p[RFOOT], 1, out.rightLeg);
        limb(frame, p[LH], p[LFOOT], 1, out.leftLeg);
        return out;
    }

    /**
     * Model part angles that point a part from {@code from} to {@code to}. Parts hang along model +y ({@code restY} 1),
     * the head sits along -y. Model space is the body frame with x and y flipped.
     */
    private static void limb(Matrix3f frame, Vector3f from, Vector3f to, float restY, Vector3f angles) {
        Vector3f world = new Vector3f(to).sub(from);
        Vector3f local = frame.transformTranspose(world, new Vector3f());
        Vector3f model = new Vector3f(-local.x, -local.y, local.z);
        if (model.lengthSquared() < 1.0E-8F) {
            angles.zero();
            return;
        }
        new Quaternionf().rotationTo(new Vector3f(0, restY, 0), model.normalize()).getEulerAnglesZYX(angles);
    }

    private static void set(double[] a, int i, double x, double y, double z) {
        a[i * 3] = x;
        a[i * 3 + 1] = y;
        a[i * 3 + 2] = z;
    }

    private static double sq(double v) {
        return v * v;
    }
}
