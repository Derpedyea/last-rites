package com.evandev.reliable_requiem.corpse;

import com.evandev.reliable_requiem.Constants;
import com.evandev.reliable_requiem.api.ICorpse;
import com.evandev.reliable_requiem.config.ModConfig;
import com.evandev.reliable_requiem.mixin.InteractionAccessor;
import com.evandev.reliable_requiem.mixin.MannequinAccessor;
import com.evandev.reliable_requiem.modules.RequiemModules;
import com.evandev.reliable_requiem.platform.Services;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Interaction;
import net.minecraft.util.Mth;
import com.evandev.reliable_requiem.corpse.ragdoll.CorpseRagdoll;
import com.evandev.reliable_requiem.corpse.ragdoll.Ragdolls;
import org.joml.Vector3d;
import java.util.concurrent.CompletableFuture;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Death drops go into a corpse instead of onto the ground.
 * <p>
 * While a player's death loot is being dropped ({@link #begin} .. {@link #finish}), every item Reliable Requiem decides
 * to drop and the dropped experience are collected here; {@link #finish} then puts them into a vanilla mannequin
 * wearing the player's skin, lying where they died. Vanilla mannequins render on any client, so the corpse needs
 * nothing client-side.
 */
public final class Corpses {
    private static ServerPlayer collecting;
    private static final List<String> homes = new ArrayList<>();
    private static final List<ItemStack> stacks = new ArrayList<>();
    private static int xp;

    private Corpses() {
    }

    public static boolean enabled() {
        return ModConfig.get().corpsesEnabled;
    }

    public static void begin(ServerPlayer player) {
        // keepInventory keeps everything on the player, so there is nothing for a corpse to hold.
        collecting = enabled() && !RequiemModules.isKeepInventory(player) ? player : null;
        homes.clear();
        stacks.clear();
        xp = 0;
    }

    /** Takes a death drop from {@code home} (e.g. {@code inv:5}) if a corpse is being filled for this player. */
    public static boolean collect(Player player, String home, ItemStack stack) {
        if (collecting == null || player != collecting || stack.isEmpty()) return false;
        homes.add(home);
        stacks.add(stack.copy());
        return true;
    }

    /** Takes the experience the player would drop; returns what should still drop as orbs. */
    public static int collectXp(Player player, int amount) {
        if (collecting == null || player != collecting) return amount;
        xp += amount;
        return 0;
    }

    public static void finish(ServerPlayer player) {
        if (collecting != player) return;
        collecting = null;
        if (stacks.isEmpty() && xp <= 0) return;

        ServerLevel level = player.level();
        Mannequin corpse = Mannequin.create(EntityTypes.MANNEQUIN, level);
        MannequinAccessor m = (MannequinAccessor) corpse;
        m.lastRites$setProfile(ResolvableProfile.createResolved(player.getGameProfile()));
        m.lastRites$setImmovable(true);
        m.lastRites$setHideDescription(true);
        corpse.setPose(Pose.SLEEPING);
        corpse.setNoGravity(true);
        corpse.setPermanentlyInvulnerable(true);
        // The name is the client's corpse marker and the menu title; the hitbox shows it, centered over the body.
        corpse.setCustomName(Component.translatableWithFallback(NAME_KEY, "%s's corpse", player.getName()));

        Vec3 pos = player.position();
        double y = pos.y;
        if (y < level.getMinY()) {
            // Died in the void: lie on the top block of this column, or just above the bottom of the world.
            int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) Math.floor(pos.x), (int) Math.floor(pos.z));
            y = top > level.getMinY() ? top : level.getMinY() + 1;
        }
        // A lying body is drawn from its feet, so the feet go half a body back to center it where the player died.
        // Clients get the yaw as a byte, and their ragdolls must start from the same one as the server's.
        float yaw = Mth.unpackDegrees(Mth.packDegrees(player.getYRot()));
        Vec3 head = headDirection(yaw);
        corpse.snapTo(pos.x - head.x * HALF_BODY, y, pos.z - head.z * HALF_BODY, yaw, 0.0F);
        // Clients take a spawned mannequin's body rotation from its head rotation, so the two must match.
        corpse.setYBodyRot(yaw);
        corpse.setYHeadRot(yaw);

        ICorpse data = (ICorpse) corpse;
        data.lastRites$makeCorpse(player.getUUID(), player.getGameProfile().name(), level.getGameTime());
        data.lastRites$setXp(xp);
        CorpseInventory inv = data.lastRites$getInventory();
        for (int i = 0; i < stacks.size(); i++) {
            if (!inv.addFromSlot(homes.get(i), stacks.get(i))) {
                // More items than a double chest holds: the rest drops like before.
                player.spawnAtLocation(level, stacks.get(i));
            }
        }
        stacks.clear();
        homes.clear();
        xp = 0;

        level.addFreshEntity(corpse);
        spawnHitbox(level, corpse);
        moveHitboxOntoBody(level, corpse);
        Services.PLATFORM.announceFreshCorpse(corpse);
        Constants.LOG.info("Corpse of {} at {} {} {} in {}", player.getGameProfile().name(),
                (int) Math.floor(pos.x), (int) Math.floor(y), (int) Math.floor(pos.z), level.dimension().identifier());
    }

    public static final String NAME_KEY = "entity.last_rites.corpse";
    private static final String HITBOX_TAG = "last_rites.corpse_hitbox=";
    /** Half the length of a lying player model. */
    private static final double HALF_BODY = 0.9;

    /** Where a lying body's head points from its feet. */
    private static Vec3 headDirection(float yaw) {
        double rad = Math.toRadians(yaw);
        return new Vec3(-Math.cos(rad), 0.0, Math.sin(rad));
    }

    /** True for the name every corpse gets; clients use it to tell corpses from plain mannequins. */
    public static boolean isCorpseName(@Nullable Component name) {
        return name != null && name.getContents() instanceof TranslatableContents t && NAME_KEY.equals(t.getKey());
    }

    /**
     * A lying mannequin's own hitbox is a 0.2 block cube at its feet, so the corpse gets an invisible interaction entity
     * over its body. It also shows the corpse's name, since the mannequin's own name tag would float over its feet.
     */
    private static void spawnHitbox(ServerLevel level, Mannequin corpse) {
        Interaction hitbox = EntityTypes.INTERACTION.create(level, EntitySpawnReason.TRIGGERED);
        if (hitbox == null) return;
        Vec3 h = headDirection(corpse.getYRot()).scale(HALF_BODY);
        hitbox.snapTo(corpse.getX() + h.x, corpse.getY(), corpse.getZ() + h.z, 0.0F, 0.0F);
        InteractionAccessor a = (InteractionAccessor) hitbox;
        a.lastRites$setWidth(1.2F);
        a.lastRites$setHeight(0.5F);
        a.lastRites$setResponse(true);
        hitbox.setCustomName(corpse.getCustomName());
        hitbox.setCustomNameVisible(true);
        hitbox.addTag(HITBOX_TAG + corpse.getStringUUID());
        level.addFreshEntity(hitbox);
        ((ICorpse) corpse).lastRites$setHitbox(hitbox.getUUID());
    }

    /**
     * The body falls wherever the blocks take it (clients simulate the same fall), so once the server has worked out where
     * it comes to rest, the hitbox moves over its torso.
     */
    private static void moveHitboxOntoBody(ServerLevel level, Mannequin corpse) {
        CompletableFuture<CorpseRagdoll> settling = Ragdolls.settle(level, corpse);
        if (settling == null) return;
        settling.thenAcceptAsync(ragdoll -> {
            UUID id = ((ICorpse) corpse).lastRites$getHitbox();
            Entity hitbox = id != null && corpse.isAlive() ? level.getEntity(id) : null;
            if (hitbox == null) return;
            Vector3d torso = ragdoll.torso();
            hitbox.setPos(torso.x, torso.y - 0.2, torso.z);
        }, level.getServer());
    }

    /** Right-click on a corpse hitbox: open the corpse. Returns null for interaction entities that aren't hitboxes. */
    public static InteractionResult interactHitbox(Interaction hitbox, Player player, InteractionHand hand) {
        String tag = hitbox.entityTags().stream().filter(t -> t.startsWith(HITBOX_TAG)).findFirst().orElse(null);
        if (tag == null) return null;
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!(hitbox.level() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        Entity corpse;
        try {
            corpse = level.getEntity(UUID.fromString(tag.substring(HITBOX_TAG.length())));
        } catch (IllegalArgumentException e) {
            corpse = null;
        }
        if (!(corpse instanceof LivingEntity living) || !isCorpse(corpse) || !corpse.isAlive()) {
            hitbox.discard(); // its corpse is gone
            return InteractionResult.PASS;
        }
        return interact(living, player);
    }

    private static void removeHitbox(LivingEntity corpse) {
        UUID id = ((ICorpse) corpse).lastRites$getHitbox();
        if (id != null && corpse.level() instanceof ServerLevel level) {
            Entity hitbox = level.getEntity(id);
            if (hitbox instanceof Interaction) hitbox.discard();
        }
    }

    public static boolean isCorpse(Entity entity) {
        return entity instanceof ICorpse c && c.lastRites$getOwner() != null;
    }

    public static InteractionResult interact(LivingEntity corpse, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.SUCCESS;
        ICorpse data = (ICorpse) corpse;
        if (!canOpen(data, serverPlayer)) {
            serverPlayer.sendSystemMessage(lockedMessage(data, serverPlayer.level().getGameTime()), true);
            return InteractionResult.FAIL;
        }

        if (data.lastRites$getXp() > 0) {
            serverPlayer.giveExperiencePoints(data.lastRites$getXp());
            data.lastRites$setXp(0);
        }

        CorpseInventory inv = data.lastRites$getInventory();
        if (serverPlayer.isShiftKeyDown()) {
            restore(inv, serverPlayer);
            removeIfEmpty(corpse);
        } else if (!removeIfEmpty(corpse)) {
            serverPlayer.openMenu(new SimpleMenuProvider((id, playerInv, p) -> ChestMenu.sixRows(id, playerInv, inv),
                    corpse.getDisplayName()));
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean canOpen(ICorpse data, ServerPlayer player) {
        if (player.getUUID().equals(data.lastRites$getOwner())) return true;
        ModConfig config = ModConfig.get();
        if (config.corpseOpsBypassLock && player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) return true;
        if (config.corpseLockSeconds < 0) return false;
        return player.level().getGameTime() - data.lastRites$getDeathTime() >= config.corpseLockSeconds * 20L;
    }

    private static Component lockedMessage(ICorpse data, long now) {
        int lock = ModConfig.get().corpseLockSeconds;
        if (lock < 0) {
            return Component.translatableWithFallback("message.last_rites.corpse_owner_only",
                    "Only %s can open this corpse", data.lastRites$getOwnerName());
        }
        long left = Math.max(0, lock * 20L - (now - data.lastRites$getDeathTime())) / 20;
        String time = left >= 60 ? (left / 60) + "m " + (left % 60) + "s" : left + "s";
        return Component.translatableWithFallback("message.last_rites.corpse_locked",
                "%s's corpse unlocks in %s", data.lastRites$getOwnerName(), time);
    }

    /** Puts every item back into the slot it came from, or anywhere in the inventory (dropping what doesn't fit). */
    private static void restore(CorpseInventory inv, ServerPlayer player) {
        List<ItemStack> leftovers = new ArrayList<>();
        for (int i = 0; i < CorpseInventory.SIZE; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            String home = inv.home(i);
            inv.setItem(i, ItemStack.EMPTY);
            if (!putHome(player, home, stack)) leftovers.add(stack);
        }
        for (ItemStack stack : leftovers) {
            RequiemModules.returnItem(player, stack);
        }
        inv.setChanged();
    }

    private static boolean putHome(ServerPlayer player, String home, ItemStack stack) {
        if (home == null) return false;
        if (home.startsWith("inv:")) {
            int slot = Integer.parseInt(home.substring(4));
            if (slot >= player.getInventory().getContainerSize() || !player.getInventory().getItem(slot).isEmpty()) {
                return false;
            }
            player.getInventory().setItem(slot, stack);
            return true;
        }
        return Services.PLATFORM.equipAccessory(player, home, stack);
    }

    /** Removes a corpse that has nothing left in it. */
    public static boolean removeIfEmpty(LivingEntity corpse) {
        ICorpse data = (ICorpse) corpse;
        if (corpse.isRemoved() || !data.lastRites$getInventory().isEmpty() || data.lastRites$getXp() > 0) return false;
        removeHitbox(corpse);
        corpse.discard();
        return true;
    }

    /** A corpse that dies anyway (creative player, /kill, void) drops what it holds. */
    public static void spill(LivingEntity corpse, ServerLevel level) {
        ICorpse data = (ICorpse) corpse;
        removeHitbox(corpse);
        CorpseInventory inv = data.lastRites$getInventory();
        for (int i = 0; i < CorpseInventory.SIZE; i++) {
            ItemStack stack = inv.removeItemNoUpdate(i);
            if (!stack.isEmpty()) corpse.spawnAtLocation(level, stack);
        }
        if (data.lastRites$getXp() > 0) {
            ExperienceOrb.award(level, corpse.position(), data.lastRites$getXp());
            data.lastRites$setXp(0);
        }
    }
}
