package com.evandev.reliable_requiem.corpse.ragdoll;

import java.util.Arrays;

/**
 * A copy of the blocks in a ragdoll's {@link CorpseRagdoll#region()}, so it can be settled on another thread. Boxes come
 * out in the same order as from the world it was copied from, so the result matches simulating on the live world.
 */
public final class SnapshotWorld implements RagdollWorld {
    private final int x0, y0, z0, sizeX, sizeY, sizeZ;
    private final double[][] cells;
    private final boolean[] fluid;

    public SnapshotWorld(RagdollWorld source, int[] region) {
        x0 = region[0];
        y0 = region[1];
        z0 = region[2];
        sizeX = region[3] - x0 + 1;
        sizeY = region[4] - y0 + 1;
        sizeZ = region[5] - z0 + 1;
        cells = new double[sizeX * sizeY * sizeZ][];
        fluid = new boolean[cells.length];
        Boxes one = new Boxes();
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    one.clear();
                    source.collectBoxes(x0 + x, y0 + y, z0 + z, x0 + x, y0 + y, z0 + z, one);
                    int i = index(x, y, z);
                    if (one.size > 0) cells[i] = Arrays.copyOf(one.data, one.size * 6);
                    fluid[i] = source.isFluid(x0 + x, y0 + y, z0 + z);
                }
            }
        }
    }

    private int index(int x, int y, int z) {
        return (x * sizeY + y) * sizeZ + z;
    }

    @Override
    public void collectBoxes(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Boxes out) {
        for (int x = Math.max(minX, x0); x <= Math.min(maxX, x0 + sizeX - 1); x++) {
            for (int y = Math.max(minY, y0); y <= Math.min(maxY, y0 + sizeY - 1); y++) {
                for (int z = Math.max(minZ, z0); z <= Math.min(maxZ, z0 + sizeZ - 1); z++) {
                    double[] c = cells[index(x - x0, y - y0, z - z0)];
                    if (c == null) continue;
                    for (int k = 0; k < c.length; k += 6) out.add(c[k], c[k + 1], c[k + 2], c[k + 3], c[k + 4], c[k + 5]);
                }
            }
        }
    }

    @Override
    public boolean isFluid(int x, int y, int z) {
        x -= x0;
        y -= y0;
        z -= z0;
        return x >= 0 && y >= 0 && z >= 0 && x < sizeX && y < sizeY && z < sizeZ && fluid[index(x, y, z)];
    }

    @Override
    public boolean isLoaded(int x, int y, int z) {
        return true;
    }
}
