package com.evandev.reliable_requiem.compat;

//? if forgelike {
/*import com.evandev.reliable_requiem.api.IPlayerKeptItems;
import com.evandev.reliable_requiem.modules.RequiemModules;
import com.evandev.reliable_requiem.util.NbtHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Map;

public class CuriosCompat {

    public static void handleAccessoryDeath(ServerPlayer player) {
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            ListTag keptCuriosList = new ListTag();
            Map<String, ICurioStacksHandler> curiosMap = handler.getCurios();

            curiosMap.forEach((identifier, stacksHandler) -> {
                // Standard stacks
                processStackHandler(player, identifier, stacksHandler.getStacks(), false, keptCuriosList);
                // Cosmetic stacks
                processStackHandler(player, identifier, stacksHandler.getCosmeticStacks(), true, keptCuriosList);
            });

            if (!keptCuriosList.isEmpty()) {
                CompoundTag keptAcc = ((IPlayerKeptItems) player).reliableRequiem$getKeptAccessories();
                if (keptAcc == null) {
                    keptAcc = new CompoundTag();
                }
                keptAcc.put("Curios", keptCuriosList);
                ((IPlayerKeptItems) player).reliableRequiem$setKeptAccessories(keptAcc);
            }
        });
    }

    private static void processStackHandler(ServerPlayer player, String identifier, IDynamicStackHandler stackHandler, boolean cosmetic, ListTag keptCuriosList) {
        for (int i = 0; i < stackHandler.getSlots(); i++) {
            ItemStack stack = stackHandler.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            if (!RequiemModules.processItemOnDeath(stack, player, -1)) {
                if (!stack.isEmpty()) {
                    CompoundTag curioTag = new CompoundTag();
                    curioTag.putString("Identifier", identifier);
                    curioTag.putBoolean("Cosmetic", cosmetic);
                    curioTag.putInt("Index", i);
                    curioTag.put("Item", NbtHelper.saveStack(stack, player.level().registryAccess()));
                    keptCuriosList.add(curioTag);
                }
                stackHandler.setStackInSlot(i, ItemStack.EMPTY);
            } else {
                RequiemModules.dropDeathItem(player, stack);
                stackHandler.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    public static void restoreKeptAccessories(ServerPlayer player) {
        CompoundTag keptAcc = ((IPlayerKeptItems) player).reliableRequiem$getKeptAccessories();
        if (keptAcc == null || !NbtHelper.hasList(keptAcc, "Curios")) return;

        ListTag curiosList = NbtHelper.getList(keptAcc, "Curios");
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            Map<String, ICurioStacksHandler> curiosMap = handler.getCurios();

            for (int i = 0; i < curiosList.size(); i++) {
                CompoundTag curioTag = NbtHelper.getCompound(curiosList, i);
                String identifier = NbtHelper.getString(curioTag, "Identifier");
                boolean cosmetic = NbtHelper.getBoolean(curioTag, "Cosmetic");
                int index = NbtHelper.getInt(curioTag, "Index");
                ItemStack stack = NbtHelper.loadStack(NbtHelper.getCompound(curioTag, "Item"), player.level().registryAccess());

                if (stack.isEmpty()) continue;

                IDynamicStackHandler stackHandler = null;
                ICurioStacksHandler stacksHandler = curiosMap.get(identifier);
                if (stacksHandler != null) {
                    stackHandler = cosmetic ? stacksHandler.getCosmeticStacks() : stacksHandler.getStacks();
                }

                if (stackHandler != null && index >= 0 && index < stackHandler.getSlots() && stackHandler.getStackInSlot(index).isEmpty()) {
                    stackHandler.setStackInSlot(index, stack);
                } else {
                    RequiemModules.returnItem(player, stack);
                }
            }
        });

        keptAcc.remove("Curios");
        ((IPlayerKeptItems) player).reliableRequiem$setKeptAccessories(keptAcc);
    }
}
*///?}
