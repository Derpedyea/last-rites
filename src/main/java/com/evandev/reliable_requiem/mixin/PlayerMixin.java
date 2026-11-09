package com.evandev.reliable_requiem.mixin;

import com.evandev.reliable_requiem.CommonClass;
import com.evandev.reliable_requiem.api.IPlayerKeptItems;
import com.evandev.reliable_requiem.modules.RequiemModules;
import com.evandev.reliable_requiem.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

//? if >=26.1 {
import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//?} else {
/*import com.evandev.reliable_requiem.Constants;
import com.evandev.reliable_requiem.api.IRequiemItem;
import com.evandev.reliable_requiem.config.ModConfig;
import com.evandev.reliable_requiem.util.NbtHelper;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
*///?}
//? if >=1.21 {
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
//?} else {
/*import net.minecraft.world.item.enchantment.EnchantmentHelper;
*///?}

@Mixin(Player.class)
public abstract class PlayerMixin implements IPlayerKeptItems {

    @Unique
    private Map<Integer, ItemStack> reliableRequiem$keptItems = new HashMap<>();

    @Unique
    private BlockPos reliableRequiem$lastDeathPos;

    @Unique
    private ResourceKey<Level> reliableRequiem$lastDeathDimension;

    @Unique
    private String reliableRequiem$lastDamageSource = "";

    @Unique
    private CompoundTag reliableRequiem$keptAccessories = new CompoundTag();

    @Override
    public Map<Integer, ItemStack> reliableRequiem$getKeptItems() {
        return reliableRequiem$keptItems;
    }

    @Override
    public void reliableRequiem$setKeptItems(Map<Integer, ItemStack> items) {
        this.reliableRequiem$keptItems = items;
    }

    @Override
    public BlockPos reliableRequiem$getLastDeathPos() {
        return reliableRequiem$lastDeathPos;
    }

    @Override
    public void reliableRequiem$setLastDeathPos(BlockPos pos) {
        this.reliableRequiem$lastDeathPos = pos;
    }

    @Override
    public ResourceKey<Level> reliableRequiem$getLastDeathDimension() {
        return reliableRequiem$lastDeathDimension;
    }

    @Override
    public void reliableRequiem$setLastDeathDimension(ResourceKey<Level> dimension) {
        this.reliableRequiem$lastDeathDimension = dimension;
    }

    @Override
    public String reliableRequiem$getLastDamageSource() {
        return reliableRequiem$lastDamageSource;
    }

    @Override
    public void reliableRequiem$setLastDamageSource(String damageSourceId) {
        this.reliableRequiem$lastDamageSource = damageSourceId;
    }

    @Override
    public CompoundTag reliableRequiem$getKeptAccessories() {
        return this.reliableRequiem$keptAccessories;
    }

    @Override
    public void reliableRequiem$setKeptAccessories(CompoundTag tag) {
        this.reliableRequiem$keptAccessories = tag;
    }

    @Inject(method = "die", at = @At("HEAD"))
    private void onDie(DamageSource source, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        this.reliableRequiem$setLastDeathPos(player.blockPosition());
        this.reliableRequiem$setLastDeathDimension(player.level().dimension());
        this.reliableRequiem$setLastDamageSource(source.getMsgId());
    }

    //? if <26.1 {
    /*@Inject(
            method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At("RETURN")
    )
    private void reliableRequiem$tagDeathDrops(ItemStack itemStack, boolean randomly, boolean thrownFromHand, CallbackInfoReturnable<ItemEntity> cir) {
        ItemEntity droppedItem = cir.getReturnValue();
        Player player = (Player) (Object) this;

        if (droppedItem != null && player.isDeadOrDying()) {
            ((IRequiemItem) droppedItem).reliableRequiem$setDroppedOnDeath(true);

            if (ModConfig.get().enabled) {
                double multiplier = ModConfig.get().itemSpreadMultiplier;
                if (multiplier != 1.0) {
                    Vec3 delta = droppedItem.getDeltaMovement();
                    droppedItem.setDeltaMovement(delta.x * multiplier, delta.y * multiplier, delta.z * multiplier);
                }
            }
        }
    }
    *///?}

    @Inject(method = "dropEquipment", at = @At("HEAD"), cancellable = true)
    //? if >=26.1 {
    private void onDropEquipment(ServerLevel level, CallbackInfo ci) {
    //?} else {
    /*private void onDropEquipment(CallbackInfo ci) {
    *///?}
        Player player = (Player) (Object) this;
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        if (RequiemModules.isKeepInventory(serverPlayer)) {
            return;
        }

        Services.PLATFORM.handleAccessoryDeath(serverPlayer);

        Inventory inv = player.getInventory();
        Map<Integer, ItemStack> kept = new HashMap<>();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {

                //? if >=1.21 {
                if (EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
                //?} else {
                /*if (EnchantmentHelper.hasVanishingCurse(stack)) {
                *///?}
                    inv.setItem(i, ItemStack.EMPTY);
                    continue;
                }

                if (!RequiemModules.processItemOnDeath(stack, serverPlayer, i)) {
                    if (!stack.isEmpty()) {
                        kept.put(i, stack.copy());
                    } else {
                        inv.setItem(i, ItemStack.EMPTY);
                    }
                } else {
                    RequiemModules.dropDeathItem(player, stack);
                    inv.setItem(i, ItemStack.EMPTY);
                }
            }
        }

        reliableRequiem$setKeptItems(kept);

        ci.cancel();
    }

    //? if >=26.1 {
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void reliableRequiem$saveAdditionalData(ValueOutput output, CallbackInfo ci) {
        Player player = (Player) (Object) this;

        Map<String, ItemStack> serializedKeptItems = new HashMap<>();
        this.reliableRequiem$keptItems.forEach((slot, stack) -> serializedKeptItems.put(String.valueOf(slot), stack));
        output.store("ReliableRequiem_KeptItems", Codec.unboundedMap(Codec.STRING, ItemStack.CODEC), serializedKeptItems);

        if (this.reliableRequiem$keptAccessories != null && !this.reliableRequiem$keptAccessories.isEmpty()) {
            output.store("ReliableRequiem_KeptAccessories", CompoundTag.CODEC, this.reliableRequiem$keptAccessories);
        }

        if (this.reliableRequiem$lastDeathPos != null) {
            output.putLong("ReliableRequiem_DeathPos", this.reliableRequiem$lastDeathPos.asLong());
        }
        if (this.reliableRequiem$lastDeathDimension != null) {
            output.store("ReliableRequiem_DeathDim", ResourceKey.codec(Registries.DIMENSION), this.reliableRequiem$lastDeathDimension);
        }
        if (this.reliableRequiem$lastDamageSource != null) {
            output.putString("ReliableRequiem_DamageSource", this.reliableRequiem$lastDamageSource);
        }

        AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            output.putDouble("ReliableRequiem_MaxHealth", maxHealthAttr.getBaseValue());
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void reliableRequiem$loadAdditionalData(ValueInput input, CallbackInfo ci) {
        Player player = (Player) (Object) this;

        this.reliableRequiem$keptItems.clear();
        input.read("ReliableRequiem_KeptItems", Codec.unboundedMap(Codec.STRING, ItemStack.CODEC))
                .ifPresent(map -> map.forEach((slot, stack) -> {
                    try {
                        this.reliableRequiem$keptItems.put(Integer.parseInt(slot), stack);
                    } catch (NumberFormatException ignored) {
                    }
                }));

        this.reliableRequiem$keptAccessories = input.read("ReliableRequiem_KeptAccessories", CompoundTag.CODEC).orElseGet(CompoundTag::new);

        input.getLong("ReliableRequiem_DeathPos").ifPresent(pos -> this.reliableRequiem$lastDeathPos = BlockPos.of(pos));
        input.read("ReliableRequiem_DeathDim", ResourceKey.codec(Registries.DIMENSION))
                .ifPresent(dimension -> this.reliableRequiem$lastDeathDimension = dimension);
        input.getString("ReliableRequiem_DamageSource").ifPresent(source -> this.reliableRequiem$lastDamageSource = source);

        double savedMaxHealth = input.getDoubleOr("ReliableRequiem_MaxHealth", 0.0);
        AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null && savedMaxHealth > 0) {
            maxHealthAttr.setBaseValue(savedMaxHealth);
        }
    }
    //?} else {
    /*@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void reliableRequiem$saveAdditionalData(CompoundTag compound, CallbackInfo ci) {
        Player player = (Player) (Object) this;

        ListTag keptItemsList = new ListTag();
        for (Map.Entry<Integer, ItemStack> entry : this.reliableRequiem$keptItems.entrySet()) {
            CompoundTag slotTag = new CompoundTag();
            slotTag.putInt("Slot", entry.getKey());
            slotTag.put("Item", NbtHelper.saveStack(entry.getValue(), player.level().registryAccess()));
            keptItemsList.add(slotTag);
        }
        compound.put("ReliableRequiem_KeptItems", keptItemsList);

        if (this.reliableRequiem$keptAccessories != null && !this.reliableRequiem$keptAccessories.isEmpty()) {
            compound.put("ReliableRequiem_KeptAccessories", this.reliableRequiem$keptAccessories);
        }

        if (this.reliableRequiem$lastDeathPos != null) {
            compound.putLong("ReliableRequiem_DeathPos", this.reliableRequiem$lastDeathPos.asLong());
        }
        if (this.reliableRequiem$lastDeathDimension != null) {
            compound.putString("ReliableRequiem_DeathDim", this.reliableRequiem$lastDeathDimension.identifier().toString());
        }
        if (this.reliableRequiem$lastDamageSource != null) {
            compound.putString("ReliableRequiem_DamageSource", this.reliableRequiem$lastDamageSource);
        }

        AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            compound.putDouble("ReliableRequiem_MaxHealth", maxHealthAttr.getBaseValue());
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void reliableRequiem$loadAdditionalData(CompoundTag compound, CallbackInfo ci) {
        Player player = (Player) (Object) this;

        this.reliableRequiem$keptItems.clear();
        if (NbtHelper.hasList(compound, "ReliableRequiem_KeptItems")) {
            ListTag listTag = NbtHelper.getList(compound, "ReliableRequiem_KeptItems");
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag slotTag = NbtHelper.getCompound(listTag, i);
                int slot = NbtHelper.getInt(slotTag, "Slot");
                ItemStack stack = NbtHelper.loadStack(NbtHelper.getCompound(slotTag, "Item"), player.level().registryAccess());
                if (!stack.isEmpty()) {
                    this.reliableRequiem$keptItems.put(slot, stack);
                }
            }
        }

        if (compound.contains("ReliableRequiem_KeptAccessories")) {
            this.reliableRequiem$keptAccessories = NbtHelper.getCompound(compound, "ReliableRequiem_KeptAccessories");
        } else {
            this.reliableRequiem$keptAccessories = new CompoundTag();
        }

        if (compound.contains("ReliableRequiem_DeathPos")) {
            this.reliableRequiem$lastDeathPos = BlockPos.of(compound.getLong("ReliableRequiem_DeathPos"));
        }
        if (compound.contains("ReliableRequiem_DeathDim")) {
            this.reliableRequiem$lastDeathDimension = ResourceKey.create(
                    Registries.DIMENSION,
                    Constants.parseId(compound.getString("ReliableRequiem_DeathDim"))
            );
        }
        if (compound.contains("ReliableRequiem_DamageSource")) {
            this.reliableRequiem$lastDamageSource = compound.getString("ReliableRequiem_DamageSource");
        }
        if (compound.contains("ReliableRequiem_MaxHealth")) {
            double savedMaxHealth = compound.getDouble("ReliableRequiem_MaxHealth");
            AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealthAttr != null && savedMaxHealth > 0) {
                maxHealthAttr.setBaseValue(savedMaxHealth);
            }
        }
    }
    *///?}

    //? if >=26.1 {
    @Inject(method = "getBaseExperienceReward", at = @At("RETURN"), cancellable = true)
    protected void onGetExperienceReward(ServerLevel level, CallbackInfoReturnable<Integer> cir) {
    //?} else if >=1.21 {
    /*@Inject(method = "getBaseExperienceReward", at = @At("RETURN"), cancellable = true)
    protected void onGetExperienceReward(CallbackInfoReturnable<Integer> cir) {
    *///?} else {
    /*@Inject(method = "getExperienceReward", at = @At("RETURN"), cancellable = true)
    protected void onGetExperienceReward(CallbackInfoReturnable<Integer> cir) {
    *///?}
        Player player = (Player) (Object) this;
        int originalDrop = cir.getReturnValue();
        cir.setReturnValue(CommonClass.onExperienceDrop(player, originalDrop));
    }
}
