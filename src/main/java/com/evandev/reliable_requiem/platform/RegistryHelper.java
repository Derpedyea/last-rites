package com.evandev.reliable_requiem.platform;

import com.evandev.reliable_requiem.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import java.util.function.Function;
import java.util.function.Supplier;

//? if >=1.21 {
import net.minecraft.core.Holder;
//?}

//? if fabric {
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
//?} else {
/*import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
*///?}

//? if neoforge {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
*///?} else if forge {
/*import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
*///?}

//? if fabric && <26.1 {
/*import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
*///?} else if fabric {
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
//?}

public class RegistryHelper {
    //? if !fabric {
    /*private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Constants.MOD_ID);
    private static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Constants.MOD_ID);
    private static final Map<ResourceKey<CreativeModeTab>, List<Supplier<? extends ItemLike>>> TAB_ENTRIES = new HashMap<>();

    public static void init(IEventBus bus) {
        ITEMS.register(bus);
        MOB_EFFECTS.register(bus);
        bus.addListener(RegistryHelper::onBuildCreativeTabs);
    }

    private static void onBuildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        List<Supplier<? extends ItemLike>> items = TAB_ENTRIES.get(event.getTabKey());
        if (items != null) {
            for (Supplier<? extends ItemLike> item : items) {
                event.accept(item.get());
            }
        }
    }
    *///?}

    public static Supplier<Item> registerItem(String name, Function<Item.Properties, ? extends Item> factory) {
        //? if fabric {
        Item item = Registry.register(BuiltInRegistries.ITEM, Constants.id(name), factory.apply(itemProperties(name)));
        return () -> item;
        //?} else {
        /*return ITEMS.register(name, () -> (Item) factory.apply(itemProperties(name)));
        *///?}
    }

    private static Item.Properties itemProperties(String name) {
        //? if >=26.1 {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Constants.id(name)));
        //?} else {
        /*return new Item.Properties();
        *///?}
    }

    //? if >=1.21 {
    public static Holder<MobEffect> registerEffect(String name, Supplier<? extends MobEffect> effect) {
        //? if fabric {
        MobEffect value = effect.get();
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Constants.id(name), value);
        //?} else {
        /*return MOB_EFFECTS.register(name, () -> (MobEffect) effect.get());
        *///?}
    }
    //?} else {
    /*public static Supplier<MobEffect> registerEffect(String name, Supplier<? extends MobEffect> effect) {
        //? if fabric {
        /^MobEffect registered = Registry.register(BuiltInRegistries.MOB_EFFECT, Constants.id(name), effect.get());
        return () -> registered;
        ^///?} else {
        return MOB_EFFECTS.register(name, () -> (MobEffect) effect.get());
        //?}
    }
    *///?}

    public static void registerToTab(ResourceKey<CreativeModeTab> tab, Supplier<? extends ItemLike> item) {
        //? if fabric && <26.1 {
        /*ItemGroupEvents.modifyEntriesEvent(tab).register(content -> content.accept(item.get()));
        *///?} else if fabric {
        CreativeModeTabEvents.modifyOutputEvent(tab).register(content -> content.accept(item.get()));
        //?} else {
        /*TAB_ENTRIES.computeIfAbsent(tab, k -> new ArrayList<>()).add(item);
        *///?}
    }
}
