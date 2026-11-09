package com.evandev.reliable_requiem;

import com.evandev.reliable_requiem.api.IPlayerKeptItems;
//? if <26.1 {
/*import com.evandev.reliable_requiem.compat.AccessoriesCompat;
*///?}
import com.evandev.reliable_requiem.config.ModConfig;
import com.evandev.reliable_requiem.modules.RequiemModules;
import com.evandev.reliable_requiem.platform.Services;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class CommonClass {

    public static void init() {
        ModConfig.get();

        //? if <26.1 {
        /*if (Services.PLATFORM.isModLoaded("accessories")) {
            AccessoriesCompat.register();
        }
        *///?}
    }

    public static void onPlayerClone(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean wasDeath) {
        RequiemModules.onPlayerClone(oldPlayer, newPlayer, wasDeath);

        if (wasDeath) {
            CompoundTag keptAcc = ((IPlayerKeptItems) oldPlayer).reliableRequiem$getKeptAccessories();
            ((IPlayerKeptItems) newPlayer).reliableRequiem$setKeptAccessories(keptAcc);

            Map<Integer, ItemStack> keptItems = ((IPlayerKeptItems) oldPlayer).reliableRequiem$getKeptItems();
            for (Map.Entry<Integer, ItemStack> entry : keptItems.entrySet()) {
                newPlayer.getInventory().setItem(entry.getKey(), entry.getValue());
            }
        }
    }

    public static void onPlayerRespawn(ServerPlayer player) {
        RequiemModules.onPlayerRespawn(player);
        Services.PLATFORM.restoreKeptAccessories(player);
    }

    public static int onExperienceDrop(Player player, int originalDrop) {
        return RequiemModules.calculateDroppedExperience(player, originalDrop);
    }
}
