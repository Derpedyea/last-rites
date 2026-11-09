package com.evandev.reliable_requiem.compat;

//? if forgelike && <26.1 {
/*import com.rosemods.heart_crystals.core.other.HCEvents;
import com.rosemods.heart_crystals.core.other.HCPlayerInfo;
import net.minecraft.world.entity.player.Player;

public class HeartCrystalsCompat {

    public static void updateHeartCrystalsHealth(Player player, double maxHealth) {
        int heartCount = (int) Math.round(maxHealth / 2.0);
        //? if >=1.21 {
        HCPlayerInfo.setHeartSet(player, true);
        HCPlayerInfo.setHeartCount(player, heartCount);
        //?} else {
        /^HCPlayerInfo.PlayerHealthInfo info = HCPlayerInfo.getPlayerHealthInfo(player);
        if (info != null) {
            info.healthSet = true;
            info.heartCount = heartCount;
        }
        ^///?}
        HCEvents.setMaxHealthAttribute(heartCount * 2, player);
    }
}
*///?}
