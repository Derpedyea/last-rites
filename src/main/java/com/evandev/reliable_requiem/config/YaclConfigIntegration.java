package com.evandev.reliable_requiem.config;

import com.evandev.reliable_requiem.compat.ModestMealsCompat;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.DoubleFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class YaclConfigIntegration {

    private static final String PREFIX = "config.reliable_requiem.";

    public static Screen createScreen(Screen parent) {
        ModConfig config = ModConfig.get();
        ModConfig defaults = new ModConfig();

        ConfigCategory.Builder general = category("general");
        general.option(bool("enabled", defaults.enabled, () -> config.enabled, val -> config.enabled = val));

        ConfigCategory.Builder hunger = category("hunger");
        hunger.option(bool("keepFood", defaults.keepFood, () -> config.keepFood, val -> config.keepFood = val));
        hunger.option(bool("keepSaturation", defaults.keepSaturation, () -> config.keepSaturation, val -> config.keepSaturation = val));
        hunger.option(intField("minFood", defaults.minFood, 0, 20, () -> config.minFood, val -> config.minFood = val));
        hunger.option(intField("maxFood", defaults.maxFood, 0, 20, () -> config.maxFood, val -> config.maxFood = val));

        ConfigCategory.Builder experience = category("experience");
        experience.option(doubleField("lostXpPercent", defaults.lostXpPercent, 0.0, 1.0, () -> config.lostXpPercent, val -> config.lostXpPercent = val));
        experience.option(intField("droppedXpPerLevel", defaults.droppedXpPerLevel, 0, Integer.MAX_VALUE, () -> config.droppedXpPerLevel, val -> config.droppedXpPerLevel = val));
        experience.option(intField("maxDroppedXp", defaults.maxDroppedXp, 0, Integer.MAX_VALUE, () -> config.maxDroppedXp, val -> config.maxDroppedXp = val));

        ConfigCategory.Builder inventory = category("inventory");
        inventory.option(doubleField("keepMainInventoryChance", defaults.keepMainInventoryChance, 0.0, 1.0, () -> config.keepMainInventoryChance, val -> config.keepMainInventoryChance = val));
        inventory.option(doubleField("keepHotbarChance", defaults.keepHotbarChance, 0.0, 1.0, () -> config.keepHotbarChance, val -> config.keepHotbarChance = val));
        inventory.option(doubleField("keepArmorChance", defaults.keepArmorChance, 0.0, 1.0, () -> config.keepArmorChance, val -> config.keepArmorChance = val));
        inventory.option(doubleField("keepOffhandChance", defaults.keepOffhandChance, 0.0, 1.0, () -> config.keepOffhandChance, val -> config.keepOffhandChance = val));
        inventory.option(doubleField("keepAccessoriesChance", defaults.keepAccessoriesChance, 0.0, 1.0, () -> config.keepAccessoriesChance, val -> config.keepAccessoriesChance = val));
        inventory.option(doubleField("keepDurabilityLoss", defaults.keepDurabilityLoss, 0.0, 1.0, () -> config.keepDurabilityLoss, val -> config.keepDurabilityLoss = val));
        inventory.option(doubleField("minKeptDurability", defaults.minKeptDurability, 0.0, 1.0, () -> config.minKeptDurability, val -> config.minKeptDurability = val));
        inventory.option(bool("allowDurabilityBreak", defaults.allowDurabilityBreak, () -> config.allowDurabilityBreak, val -> config.allowDurabilityBreak = val));
        inventory.option(doubleField("defaultDestroyChance", defaults.defaultDestroyChance, 0.0, 1.0, () -> config.defaultDestroyChance, val -> config.defaultDestroyChance = val));
        inventory.option(intField("dropDespawnTime", defaults.dropDespawnTime, 0, Integer.MAX_VALUE, () -> config.dropDespawnTime, val -> config.dropDespawnTime = val));
        inventory.option(bool("neverDespawnDeathDrops", defaults.neverDespawnDeathDrops, () -> config.neverDespawnDeathDrops, val -> config.neverDespawnDeathDrops = val));
        inventory.option(doubleField("itemSpreadMultiplier", defaults.itemSpreadMultiplier, 0.0, 10.0, () -> config.itemSpreadMultiplier, val -> config.itemSpreadMultiplier = val));
        inventory.option(bool("explosionResistantDeathDrops", defaults.explosionResistantDeathDrops, () -> config.explosionResistantDeathDrops, val -> config.explosionResistantDeathDrops = val));
        inventory.group(stringList("keptItemOverrides", () -> config.keptItemOverrides, val -> config.keptItemOverrides = val));
        inventory.group(stringList("droppedItemOverrides", () -> config.droppedItemOverrides, val -> config.droppedItemOverrides = val));
        inventory.group(stringList("destroyedItemOverrides", () -> config.destroyedItemOverrides, val -> config.destroyedItemOverrides = val));
        inventory.group(stringList("bypassKeepInventoryDimensions", () -> config.bypassKeepInventoryDimensions, val -> config.bypassKeepInventoryDimensions = val));
        inventory.group(stringList("bypassDamageSources", () -> config.bypassKeepInventoryDamageSources, val -> config.bypassKeepInventoryDamageSources = val));

        ConfigCategory.Builder effects = category("effects");
        effects.option(bool("keepPositiveEffects", defaults.keepPositiveEffects, () -> config.keepPositiveEffects, val -> config.keepPositiveEffects = val));
        effects.option(bool("keepNegativeEffects", defaults.keepNegativeEffects, () -> config.keepNegativeEffects, val -> config.keepNegativeEffects = val));

        ConfigCategory.Builder healthAndSpawn = category("health_and_respawning");
        healthAndSpawn.option(bool("scaleRespawnHealth", defaults.scaleRespawnHealth, () -> config.scaleRespawnHealth, val -> config.scaleRespawnHealth = val));
        healthAndSpawn.option(doubleField("respawnHealthPercent", defaults.respawnHealthPercent, 0.1, 1.0, () -> config.respawnHealthPercent, val -> config.respawnHealthPercent = val));
        healthAndSpawn.option(doubleField("maxRespawnHealth", defaults.maxRespawnHealth, 1.0, Double.MAX_VALUE, () -> config.maxRespawnHealth, val -> config.maxRespawnHealth = val));
        healthAndSpawn.option(bool("enableHealthReductionOnDeath", defaults.enableHealthReductionOnDeath, () -> config.enableHealthReductionOnDeath, val -> config.enableHealthReductionOnDeath = val));
        healthAndSpawn.option(doubleField("healthLossPerDeath", defaults.healthLossPerDeath, 0.0, Double.MAX_VALUE, () -> config.healthLossPerDeath, val -> config.healthLossPerDeath = val));
        healthAndSpawn.option(doubleField("minMaxHealth", defaults.minMaxHealth, 1.0, Double.MAX_VALUE, () -> config.minMaxHealth, val -> config.minMaxHealth = val));
        healthAndSpawn.option(doubleField("maxMaxHealth", defaults.maxMaxHealth, 1.0, Double.MAX_VALUE, () -> config.maxMaxHealth, val -> config.maxMaxHealth = val));
        healthAndSpawn.option(doubleField("crystalHeartHealthAmount", defaults.crystalHeartHealthAmount, 0.5, Double.MAX_VALUE, () -> config.crystalHeartHealthAmount, val -> config.crystalHeartHealthAmount = val));
        healthAndSpawn.option(bool("enableSpawnerShardDrop", defaults.enableSpawnerShardDrop, () -> config.enableSpawnerShardDrop, val -> config.enableSpawnerShardDrop = val));
        healthAndSpawn.option(doubleField("spawnerShardDropChance", defaults.spawnerShardDropChance, 0.0, 1.0, () -> config.spawnerShardDropChance, val -> config.spawnerShardDropChance = val));
        healthAndSpawn.option(intField("spawnerShardDropCount", defaults.spawnerShardDropCount, 1, 64, () -> config.spawnerShardDropCount, val -> config.spawnerShardDropCount = val));
        healthAndSpawn.option(intField("respawnRandomRadius", defaults.respawnRandomRadius, 0, Integer.MAX_VALUE, () -> config.respawnRandomRadius, val -> config.respawnRandomRadius = val));

        ConfigCategory.Builder stamina = category("stamina");
        stamina.option(bool("enableStaminaReductionOnDeath", defaults.enableStaminaReductionOnDeath, () -> config.enableStaminaReductionOnDeath, val -> config.enableStaminaReductionOnDeath = val));
        stamina.option(doubleField("staminaLossPerDeath", defaults.staminaLossPerDeath, 0.0, Double.MAX_VALUE, () -> config.staminaLossPerDeath, val -> config.staminaLossPerDeath = val));
        stamina.option(doubleField("minStaminaCapacity", defaults.minStaminaCapacity, 2.0, Double.MAX_VALUE, () -> config.minStaminaCapacity, val -> config.minStaminaCapacity = val));
        stamina.option(doubleField("maxStaminaCapacity", defaults.maxStaminaCapacity, 2.0, Double.MAX_VALUE, () -> config.maxStaminaCapacity, val -> config.maxStaminaCapacity = val));
        stamina.option(doubleField("crystalBoltStaminaAmount", defaults.crystalBoltStaminaAmount, 0.5, Double.MAX_VALUE, () -> config.crystalBoltStaminaAmount, val -> config.crystalBoltStaminaAmount = val));

        ConfigCategory.Builder memento = category("memento_mori");
        memento.option(bool("applyMementoMori", defaults.applyMementoMori, () -> config.applyMementoMori, val -> config.applyMementoMori = val));
        memento.option(intField("mementoDuration", defaults.mementoDuration, 0, Integer.MAX_VALUE, () -> config.mementoDuration, val -> config.mementoDuration = val));
        memento.option(doubleField("mementoSpeedModifier", defaults.mementoSpeedModifier, -Double.MAX_VALUE, Double.MAX_VALUE, () -> config.mementoSpeedModifier, val -> config.mementoSpeedModifier = val));
        memento.option(doubleField("mementoDamageModifier", defaults.mementoDamageModifier, -Double.MAX_VALUE, Double.MAX_VALUE, () -> config.mementoDamageModifier, val -> config.mementoDamageModifier = val));

        ConfigCategory.Builder corpses = category("corpses");
        corpses.option(bool("corpsesEnabled", defaults.corpsesEnabled, () -> config.corpsesEnabled, val -> config.corpsesEnabled = val));
        corpses.option(intField("corpseLockSeconds", defaults.corpseLockSeconds, -1, Integer.MAX_VALUE, () -> config.corpseLockSeconds, val -> config.corpseLockSeconds = val));
        corpses.option(bool("corpseOpsBypassLock", defaults.corpseOpsBypassLock, () -> config.corpseOpsBypassLock, val -> config.corpseOpsBypassLock = val));

        ConfigCategory.Builder misc = category("miscellaneous");
        misc.option(bool("restrictRespawning", defaults.restrictRespawning, () -> config.restrictRespawning, val -> config.restrictRespawning = val));
        misc.option(bool("displayDeathCoordinates", defaults.displayDeathCoordinates, () -> config.displayDeathCoordinates, val -> config.displayDeathCoordinates = val));

        YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder()
                .title(Component.translatable(PREFIX + "title"))
                .category(general.build())
                .category(corpses.build())
                .category(hunger.build())
                .category(experience.build())
                .category(inventory.build())
                .category(effects.build())
                .category(healthAndSpawn.build());
        if (ModestMealsCompat.isLoaded()) {
            builder.category(stamina.build());
        }
        return builder
                .category(memento.build())
                .category(misc.build())
                .save(ModConfig::save)
                .build()
                .generateScreen(parent);
    }

    private static ConfigCategory.Builder category(String name) {
        return ConfigCategory.createBuilder().name(Component.translatable(PREFIX + "category." + name));
    }

    private static <T> Option.Builder<T> option(String key) {
        return Option.<T>createBuilder()
                .name(Component.translatable(PREFIX + key))
                .description(OptionDescription.of(Component.translatable(PREFIX + key + ".tooltip")));
    }

    private static Option<Boolean> bool(String key, boolean defaultValue, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return YaclConfigIntegration.<Boolean>option(key)
                .binding(defaultValue, getter, setter)
                .controller(TickBoxControllerBuilder::create)
                .build();
    }

    private static Option<Integer> intField(String key, int defaultValue, int min, int max, Supplier<Integer> getter, Consumer<Integer> setter) {
        return YaclConfigIntegration.<Integer>option(key)
                .binding(defaultValue, getter, setter)
                .controller(opt -> IntegerFieldControllerBuilder.create(opt).range(min, max))
                .build();
    }

    private static Option<Double> doubleField(String key, double defaultValue, double min, double max, Supplier<Double> getter, Consumer<Double> setter) {
        return YaclConfigIntegration.<Double>option(key)
                .binding(defaultValue, getter, setter)
                .controller(opt -> DoubleFieldControllerBuilder.create(opt).range(min, max))
                .build();
    }

    private static ListOption<String> stringList(String key, Supplier<List<String>> getter, Consumer<List<String>> setter) {
        //? if >=26.1 {
        return ListOption.<String>createBuilder(String.class)
        //?} else {
        /*return ListOption.<String>createBuilder()
        *///?}
                .name(Component.translatable(PREFIX + key))
                .description(OptionDescription.of(Component.translatable(PREFIX + key + ".tooltip")))
                .binding(new ArrayList<>(), getter, val -> setter.accept(new ArrayList<>(val)))
                .controller(StringControllerBuilder::create)
                .initial("")
                .build();
    }
}
