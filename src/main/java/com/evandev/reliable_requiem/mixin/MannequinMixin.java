package com.evandev.reliable_requiem.mixin;

import com.evandev.reliable_requiem.api.ICorpse;
import com.evandev.reliable_requiem.corpse.CorpseInventory;
import com.evandev.reliable_requiem.corpse.Corpses;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(Mannequin.class)
public abstract class MannequinMixin extends Avatar implements ICorpse {
    @Unique
    private UUID lastRites$owner;
    @Unique
    private String lastRites$ownerName = "";
    @Unique
    private long lastRites$deathTime;
    @Unique
    private int lastRites$xp;
    @Unique
    private CorpseInventory lastRites$inventory;
    @Unique
    private UUID lastRites$hitbox;

    protected MannequinMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public UUID lastRites$getOwner() {
        return lastRites$owner;
    }

    @Override
    public String lastRites$getOwnerName() {
        return lastRites$ownerName;
    }

    @Override
    public long lastRites$getDeathTime() {
        return lastRites$deathTime;
    }

    @Override
    public int lastRites$getXp() {
        return lastRites$xp;
    }

    @Override
    public void lastRites$setXp(int xp) {
        lastRites$xp = xp;
    }

    @Override
    public CorpseInventory lastRites$getInventory() {
        if (lastRites$inventory == null) lastRites$inventory = new CorpseInventory(this);
        return lastRites$inventory;
    }

    @Override
    public void lastRites$makeCorpse(UUID owner, String ownerName, long deathTime) {
        lastRites$owner = owner;
        lastRites$ownerName = ownerName;
        lastRites$deathTime = deathTime;
    }

    @Override
    public UUID lastRites$getHitbox() {
        return lastRites$hitbox;
    }

    @Override
    public void lastRites$setHitbox(UUID hitbox) {
        lastRites$hitbox = hitbox;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        if (lastRites$owner != null && hand == InteractionHand.MAIN_HAND) {
            return Corpses.interact(this, player);
        }
        return super.interact(player, hand, location);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void lastRites$save(ValueOutput output, CallbackInfo ci) {
        if (lastRites$owner == null) return;
        ValueOutput corpse = output.child("last_rites:corpse");
        corpse.store("owner", UUIDUtil.CODEC, lastRites$owner);
        corpse.putString("owner_name", lastRites$ownerName);
        corpse.putLong("death_time", lastRites$deathTime);
        corpse.putInt("xp", lastRites$xp);
        if (lastRites$hitbox != null) corpse.store("hitbox", UUIDUtil.CODEC, lastRites$hitbox);
        corpse.store("items", CorpseInventory.ENTRIES_CODEC, lastRites$getInventory().save());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void lastRites$load(ValueInput input, CallbackInfo ci) {
        input.child("last_rites:corpse").ifPresent(corpse -> {
            lastRites$owner = corpse.read("owner", UUIDUtil.CODEC).orElse(null);
            lastRites$ownerName = corpse.getStringOr("owner_name", "");
            lastRites$deathTime = corpse.getLongOr("death_time", 0L);
            lastRites$xp = corpse.getIntOr("xp", 0);
            lastRites$hitbox = corpse.read("hitbox", UUIDUtil.CODEC).orElse(null);
            corpse.read("items", CorpseInventory.ENTRIES_CODEC).ifPresent(lastRites$getInventory()::load);
        });
    }
}
