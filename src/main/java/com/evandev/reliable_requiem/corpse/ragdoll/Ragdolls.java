package com.evandev.reliable_requiem.corpse.ragdoll;

import com.evandev.reliable_requiem.Constants;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Making corpse ragdolls: the same body for the same corpse on the server and on every client. */
public final class Ragdolls {
    /** Settling a body takes a few milliseconds, so it runs off the game thread on a copy of the blocks. */
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "Last Rites ragdolls");
        thread.setDaemon(true);
        return thread;
    });
    /** Entity ids of corpses the server just made, which should be seen falling. Client only. */
    private static final IntSet FRESH = new IntOpenHashSet();

    private Ragdolls() {
    }

    public static synchronized void markFresh(int entityId) {
        if (FRESH.size() > 256) FRESH.clear();
        FRESH.add(entityId);
    }

    public static synchronized boolean takeFresh(int entityId) {
        return FRESH.remove(entityId);
    }

    /** The body of this corpse, standing and about to fall. */
    public static CorpseRagdoll start(Entity corpse) {
        UUID id = corpse.getUUID();
        return new CorpseRagdoll(id.getMostSignificantBits() ^ id.getLeastSignificantBits(), corpse.getX(), corpse.getY(), corpse.getZ(), corpse.getYRot());
    }

    /** This corpse's body lying still, worked out in the background; null while the blocks around it aren't loaded. */
    public static @Nullable CompletableFuture<CorpseRagdoll> settle(Level level, Entity corpse) {
        CorpseRagdoll ragdoll = start(corpse);
        int[] r = ragdoll.region();
        if (!level.hasChunksAt(r[0], r[2], r[3], r[5])) return null;
        SnapshotWorld blocks = new SnapshotWorld(new LevelRagdollWorld(level), r);
        return CompletableFuture.supplyAsync(() -> {
            ragdoll.settle(blocks);
            return ragdoll;
        }, WORKER).whenComplete((done, error) -> {
            if (error != null) Constants.LOG.error("Couldn't settle a corpse's body", error);
        });
    }
}
