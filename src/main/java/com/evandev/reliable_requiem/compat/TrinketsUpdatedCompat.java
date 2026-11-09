package com.evandev.reliable_requiem.compat;

//? if >=26.1 {
import com.evandev.reliable_requiem.modules.RequiemModules;
import eu.pb4.trinkets.api.TrinketDropRule;
import eu.pb4.trinkets.api.TrinketsApi;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Trinkets Updated copies every trinket slot to the respawned player, so kept trinkets simply stay where they are;
 * only dropped and destroyed ones need to be removed here.
 */
public class TrinketsUpdatedCompat {

    public static void handleAccessoryDeath(ServerPlayer player) {
        TrinketsApi.getAttachment(player).forEach((access, stack) -> {
            if (stack.isEmpty()) return;

            TrinketDropRule rule = TrinketsApi.getDropRule(stack, access, player, false);
            if (rule == TrinketDropRule.KEEP) return;
            if (rule == TrinketDropRule.DESTROY) {
                access.set(ItemStack.EMPTY);
                return;
            }

            if (RequiemModules.processItemOnDeath(stack, player, -1)) {
                RequiemModules.dropDeathItem(player, stack);
                access.set(ItemStack.EMPTY);
            } else if (stack.isEmpty()) {
                access.set(ItemStack.EMPTY);
            }
        });
    }
}
//?}
