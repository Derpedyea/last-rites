package com.evandev.reliable_requiem.corpse.ragdoll;

import java.util.Arrays;

/** The blocks a ragdoll lies on. Kept apart from Minecraft's level so the simulation can be run on its own. */
public interface RagdollWorld {
    /** Adds the collision boxes of the blocks from x0 y0 z0 to x1 y1 z1 (inclusive), in world coordinates. */
    void collectBoxes(int x0, int y0, int z0, int x1, int y1, int z1, Boxes out);

    boolean isFluid(int x, int y, int z);

    boolean isLoaded(int x, int y, int z);

    /** Axis-aligned boxes, six doubles each: min x y z, max x y z. */
    final class Boxes {
        double[] data = new double[6 * 32];
        int size;

        public void add(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
            if ((size + 1) * 6 > data.length) data = Arrays.copyOf(data, data.length * 2);
            int k = size++ * 6;
            data[k] = minX;
            data[k + 1] = minY;
            data[k + 2] = minZ;
            data[k + 3] = maxX;
            data[k + 4] = maxY;
            data[k + 5] = maxZ;
        }

        void clear() {
            size = 0;
        }
    }
}
