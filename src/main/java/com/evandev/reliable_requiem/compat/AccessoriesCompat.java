package com.evandev.reliable_requiem.compat;

//? if <26.1 {
/*import com.evandev.reliable_requiem.api.IPlayerKeptItems;
import com.evandev.reliable_requiem.modules.RequiemModules;
import com.evandev.reliable_requiem.util.NbtHelper;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.events.OnDeathCallback;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;
import java.util.List;

public class AccessoriesCompat {

    private static boolean registered = false;

    public static void register() {
        if (registered) return;
        registered = true;
        OnDeathCallback.EVENT.register(AccessoriesCompat::onDeath);
    }

    private static TriState onDeath(TriState currentState, LivingEntity entity, AccessoriesCapability capability, DamageSource source, List<ItemStack> droppedStacks) {
        if (!(entity instanceof ServerPlayer player)) return TriState.DEFAULT;
        if (RequiemModules.isKeepInventory(player)) return TriState.DEFAULT;

        ListTag keptList = new ListTag();
        Iterator<ItemStack> iterator = droppedStacks.iterator();
        while (iterator.hasNext()) {
            ItemStack stack = iterator.next();
            if (stack.isEmpty()) continue;

            if (!RequiemModules.processItemOnDeath(stack, player, -1)) {
                iterator.remove();

                if (!stack.isEmpty()) {
                    CompoundTag tag = new CompoundTag();
                    tag.put("Item", NbtHelper.saveStack(stack, player.level().registryAccess()));
                    keptList.add(tag);
                }
            }
        }

        if (!keptList.isEmpty()) {
            CompoundTag keptAcc = ((IPlayerKeptItems) player).reliableRequiem$getKeptAccessories();
            if (keptAcc == null) {
                keptAcc = new CompoundTag();
            }
            keptAcc.put("Accessories", keptList);
            ((IPlayerKeptItems) player).reliableRequiem$setKeptAccessories(keptAcc);
        }

        return TriState.DEFAULT;
    }

    public static void restoreKeptAccessories(ServerPlayer player) {
        CompoundTag keptAcc = ((IPlayerKeptItems) player).reliableRequiem$getKeptAccessories();
        if (keptAcc == null || !NbtHelper.hasList(keptAcc, "Accessories")) return;

        ListTag accessoriesList = NbtHelper.getList(keptAcc, "Accessories");
        AccessoriesCapability capability = AccessoriesCapability.get(player);

        for (int i = 0; i < accessoriesList.size(); i++) {
            CompoundTag tag = NbtHelper.getCompound(accessoriesList, i);
            ItemStack stack = NbtHelper.loadStack(NbtHelper.getCompound(tag, "Item"), player.level().registryAccess());
            if (stack.isEmpty()) continue;

            if (capability != null) {
                capability.attemptToEquipAccessory(stack);
            }

            if (!stack.isEmpty()) {
                RequiemModules.returnItem(player, stack);
            }
        }

        keptAcc.remove("Accessories");
        ((IPlayerKeptItems) player).reliableRequiem$setKeptAccessories(keptAcc);
    }
}
*///?}
