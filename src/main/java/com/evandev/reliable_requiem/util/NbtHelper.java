package com.evandev.reliable_requiem.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

//? if >=26.1 {
import net.minecraft.nbt.NbtOps;
//?}

/**
 * Version-independent access to the bits of NBT used for storing kept items.
 */
public final class NbtHelper {

    private NbtHelper() {
    }

    public static Tag saveStack(ItemStack stack, HolderLookup.Provider registries) {
        //? if >=26.1 {
        return ItemStack.OPTIONAL_CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), stack).getOrThrow();
        //?} else if >=1.21 {
        /*return stack.saveOptional(registries);
        *///?} else {
        /*return stack.save(new CompoundTag());
        *///?}
    }

    public static ItemStack loadStack(CompoundTag tag, HolderLookup.Provider registries) {
        //? if >=26.1 {
        return ItemStack.OPTIONAL_CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag).result().orElse(ItemStack.EMPTY);
        //?} else if >=1.21 {
        /*return ItemStack.parseOptional(registries, tag);
        *///?} else {
        /*return ItemStack.of(tag);
        *///?}
    }

    public static boolean hasList(CompoundTag tag, String key) {
        //? if >=26.1 {
        return tag.getList(key).isPresent();
        //?} else {
        /*return tag.contains(key, Tag.TAG_LIST);
        *///?}
    }

    public static ListTag getList(CompoundTag tag, String key) {
        //? if >=26.1 {
        return tag.getListOrEmpty(key);
        //?} else {
        /*return tag.getList(key, Tag.TAG_COMPOUND);
        *///?}
    }

    public static CompoundTag getCompound(ListTag list, int index) {
        //? if >=26.1 {
        return list.getCompoundOrEmpty(index);
        //?} else {
        /*return list.getCompound(index);
        *///?}
    }

    public static CompoundTag getCompound(CompoundTag tag, String key) {
        //? if >=26.1 {
        return tag.getCompoundOrEmpty(key);
        //?} else {
        /*return tag.getCompound(key);
        *///?}
    }

    public static String getString(CompoundTag tag, String key) {
        //? if >=26.1 {
        return tag.getStringOr(key, "");
        //?} else {
        /*return tag.getString(key);
        *///?}
    }

    public static int getInt(CompoundTag tag, String key) {
        //? if >=26.1 {
        return tag.getIntOr(key, 0);
        //?} else {
        /*return tag.getInt(key);
        *///?}
    }

    public static boolean getBoolean(CompoundTag tag, String key) {
        //? if >=26.1 {
        return tag.getBooleanOr(key, false);
        //?} else {
        /*return tag.getBoolean(key);
        *///?}
    }
}
