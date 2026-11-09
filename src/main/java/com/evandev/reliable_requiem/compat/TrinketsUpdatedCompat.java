package com.evandev.reliable_requiem.compat;

//? if >=26.1 {
import com.evandev.reliable_requiem.modules.RequiemModules;
import eu.pb4.trinkets.api.TrinketDropRule;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketSlotReference;
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
                RequiemModules.dropDeathItem(player, stack, homeKey(access.reference()));
                access.set(ItemStack.EMPTY);
            } else if (stack.isEmpty()) {
                access.set(ItemStack.EMPTY);
            }
        });
    }

    private static final String PREFIX = "trinkets:";

    private static String homeKey(TrinketSlotReference ref) {
        return PREFIX + ref.index() + ":" + ref.cosmetic() + ":" + ref.slot();
    }

    /** Puts a corpse item back into the trinket slot it came from, if that slot is still there and empty. */
    public static boolean equip(ServerPlayer player, String home, ItemStack stack) {
        if (!home.startsWith(PREFIX)) return false;
        String[] parts = home.substring(PREFIX.length()).split(":", 3);
        if (parts.length != 3) return false;
        TrinketSlotAccess access;
        try {
            access = TrinketsApi.getAttachment(player)
                    .getSlotAccess(new TrinketSlotReference(parts[2], Integer.parseInt(parts[0]), Boolean.parseBoolean(parts[1])));
        } catch (RuntimeException e) {
            return false;
        }
        if (access == null || !access.isValid() || !access.get().isEmpty()) return false;
        return access.set(stack);
    }
}
//?}
