package com.evandev.reliable_requiem.corpse;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * A corpse's items, laid out like the player's inventory screen (main rows, hotbar, then equipment), plus the slot
 * each item came from ("home") so it can be put back there. It is the corpse's storage itself, so everyone looking
 * at a corpse sees the same items.
 */
public class CorpseInventory extends SimpleContainer {
    public static final int SIZE = 54;
    /** Accessories (trinkets) start on the bottom row, below the armor row. */
    private static final int ACCESSORY_ROW = 45;

    public record Entry(int index, String home, ItemStack item) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("index").forGetter(Entry::index),
                Codec.STRING.optionalFieldOf("home", "").forGetter(Entry::home),
                ItemStack.CODEC.fieldOf("item").forGetter(Entry::item)
        ).apply(i, Entry::new));
    }

    public static final Codec<List<Entry>> ENTRIES_CODEC = Entry.CODEC.listOf();

    private final LivingEntity corpse;
    private final String[] homes = new String[SIZE];

    public CorpseInventory(LivingEntity corpse) {
        super(SIZE);
        this.corpse = corpse;
    }

    public String home(int index) {
        return homes[index];
    }

    /**
     * Adds an item from the dying player's slot {@code home}. Returns false when there is no room.
     */
    public boolean addFromSlot(String home, ItemStack stack) {
        int preferred = preferredIndex(home);
        int index = preferred >= 0 && getItem(preferred).isEmpty() ? preferred : firstFree(preferred >= 0 ? 0 : ACCESSORY_ROW);
        if (index < 0) return false;
        super.setItem(index, stack);
        homes[index] = home;
        return true;
    }

    /** Inventory slot {@code inv:i} -> chest index; everything else (trinkets) has none and goes to the bottom row. */
    private static int preferredIndex(String home) {
        if (home == null || !home.startsWith("inv:")) return -1;
        int slot = Integer.parseInt(home.substring(4));
        if (slot < 9) return 27 + slot;       // hotbar -> 4th row
        if (slot < 36) return slot - 9;        // main inventory -> rows 1-3
        return slot < SIZE ? slot : -1;        // armor, offhand, body, saddle -> 5th row
    }

    /** First empty slot at or after {@code start}, wrapping around. */
    private int firstFree(int start) {
        for (int n = 0; n < SIZE; n++) {
            int i = (start + n) % SIZE;
            if (getItem(i).isEmpty()) return i;
        }
        return -1;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        // A player put something here by hand: it no longer has a home slot.
        homes[index] = null;
        super.setItem(index, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return corpse.isAlive() && !corpse.isRemoved() && player.distanceToSqr(corpse) <= 64.0;
    }

    @Override
    public void stopOpen(ContainerUser user) {
        super.stopOpen(user);
        Corpses.removeIfEmpty(corpse);
    }

    public List<Entry> save() {
        List<Entry> out = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            ItemStack stack = getItem(i);
            if (!stack.isEmpty()) out.add(new Entry(i, homes[i] == null ? "" : homes[i], stack));
        }
        return out;
    }

    public void load(List<Entry> entries) {
        clearContent();
        java.util.Arrays.fill(homes, null);
        for (Entry e : entries) {
            if (e.index() < 0 || e.index() >= SIZE) continue;
            super.setItem(e.index(), e.item());
            homes[e.index()] = e.home().isEmpty() ? null : e.home();
        }
    }
}
