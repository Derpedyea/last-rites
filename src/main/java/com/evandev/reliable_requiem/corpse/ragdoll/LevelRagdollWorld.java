package com.evandev.reliable_requiem.corpse.ragdoll;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A ragdoll's view of a level: block collision shapes and fluids. Main thread only. */
public final class LevelRagdollWorld implements RagdollWorld {
    private final Level level;
    private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

    public LevelRagdollWorld(Level level) {
        this.level = level;
    }

    @Override
    public void collectBoxes(int x0, int y0, int z0, int x1, int y1, int z1, Boxes out) {
        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                for (int z = z0; z <= z1; z++) {
                    cursor.set(x, y, z);
                    BlockState state = level.getBlockState(cursor);
                    if (state.isAir()) continue;
                    VoxelShape shape = state.getCollisionShape(level, cursor);
                    if (shape.isEmpty()) continue;
                    for (AABB box : shape.toAabbs()) out.add(box.minX + x, box.minY + y, box.minZ + z, box.maxX + x, box.maxY + y, box.maxZ + z);
                }
            }
        }
    }

    @Override
    public boolean isFluid(int x, int y, int z) {
        return !level.getFluidState(cursor.set(x, y, z)).isEmpty();
    }

    @Override
    public boolean isLoaded(int x, int y, int z) {
        return level.hasChunkAt(cursor.set(x, y, z));
    }
}
