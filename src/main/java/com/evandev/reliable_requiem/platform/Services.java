package com.evandev.reliable_requiem.platform;

//? if <26.1 {
/*import com.evandev.reliable_requiem.compat.AccessoriesCompat;
*///?}
//? if forgelike {
/*import com.evandev.reliable_requiem.compat.CuriosCompat;
*///?}
//? if forgelike && <26.1 {
/*import com.evandev.reliable_requiem.compat.HeartCrystalsCompat;
*///?}
//? if fabric && <26.1 {
/*import com.evandev.reliable_requiem.compat.TrinketsCompat;
*///?}
//? if >=26.1 {
import com.evandev.reliable_requiem.compat.TrinketsUpdatedCompat;
//?}
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

//? if fabric {
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
//?}
//? if forge {
/*import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
*///?}
//? if neoforge {
/*import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
*///?}

import java.nio.file.Path;

public class Services {
    public static final Services PLATFORM = new Services();

    public String getPlatformName() {
        //? if fabric {
        return "Fabric";
        //?}
        //? if forge {
        /*return "Forge";
        *///?}
        //? if neoforge {
        /*return "NeoForge";
        *///?}
    }

    public boolean isModLoaded(String modId) {
        //? if fabric {
        return FabricLoader.getInstance().isModLoaded(modId);
        //?}
        //? if forgelike {
        /*return ModList.get().isLoaded(modId);
        *///?}
    }

    public Path getConfigDirectory() {
        //? if fabric {
        return FabricLoader.getInstance().getConfigDir();
        //?}
        //? if forgelike {
        /*return FMLPaths.CONFIGDIR.get();
        *///?}
    }

    /**
     * Handles keeping and dropping items in accessory slots on player death.
     */
    public void handleAccessoryDeath(ServerPlayer player) {
        //? if fabric && <26.1 {
        /*if (isModLoaded("trinkets")) {
            TrinketsCompat.handleAccessoryDeath(player);
        }
        *///?}
        //? if >=26.1 {
        if (isModLoaded("trinkets_updated")) {
            TrinketsUpdatedCompat.handleAccessoryDeath(player);
        }
        //?}
        //? if forgelike {
        /*if (usesCuriosCompat()) {
            CuriosCompat.handleAccessoryDeath(player);
        }
        *///?}
    }

    /**
     * Puts an item from a corpse back into the accessory slot {@code home} it came from. Returns false if it can't.
     */
    public boolean equipAccessory(ServerPlayer player, String home, ItemStack stack) {
        //? if >=26.1 {
        if (isModLoaded("trinkets_updated")) {
            return TrinketsUpdatedCompat.equip(player, home, stack);
        }
        //?}
        return false;
    }

    /**
     * Restores any kept accessories to the player upon cloning.
     */
    public void restoreKeptAccessories(ServerPlayer newPlayer) {
        //? if <26.1 {
        /*if (isModLoaded("accessories")) {
            AccessoriesCompat.restoreKeptAccessories(newPlayer);
        }
        *///?}
        //? if fabric && <26.1 {
        /*if (isModLoaded("trinkets")) {
            TrinketsCompat.restoreKeptAccessories(newPlayer);
        }
        *///?}
        //? if forgelike {
        /*if (usesCuriosCompat()) {
            CuriosCompat.restoreKeptAccessories(newPlayer);
        }
        *///?}
    }

    /**
     * Curios slots are backed by Accessories when the compatibility layer is present, so they are handled by
     * {@link com.evandev.reliable_requiem.compat.AccessoriesCompat} instead.
     */
    private boolean usesCuriosCompat() {
        return isModLoaded("curios") && !isModLoaded("accessories_compat_layer");
    }

    /**
     * Synchronizes health and heart count with Heart Crystals if present.
     */
    public void updateHeartCrystalsHealth(Player player, double maxHealth) {
        //? if forgelike && <26.1 {
        /*if (isModLoaded("heart_crystals")) {
            HeartCrystalsCompat.updateHeartCrystalsHealth(player, maxHealth);
        }
        *///?}
    }
}
