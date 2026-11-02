package com.evandev.reliable_requiem.config;

import com.evandev.reliable_requiem.Constants;
import com.evandev.reliable_requiem.platform.Services;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = Services.PLATFORM.getConfigDirectory().resolve("reliable_requiem.json").toFile();
    private static ModConfig INSTANCE;

    @SerializedName("enabled")
    public boolean enabled = true;

    // Hunger Settings
    public boolean keepFood = false;
    public boolean keepSaturation = false;
    public int minFood = 0;
    public int maxFood = 20;

    // Experience Settings
    public double lostXpPercent = 1.0;
    public int droppedXpPerLevel = 7;
    public int maxDroppedXp = 100;

    // Inventory Settings (Chances to keep: 1.0 = 100%, 0.0 = 0%)
    public double keepMainInventoryChance = 0.0;
    public double keepHotbarChance = 0.0;
    public double keepArmorChance = 0.0;
    public double keepOffhandChance = 0.0;
    public double keepAccessoriesChance = 0.0;
    public double defaultDestroyChance = 0.0;
    public int dropDespawnTime = 300;
    public double keepDurabilityLoss = 0.10;
    public double minKeptDurability = 0.0;
    public boolean allowDurabilityBreak = true;
    public boolean neverDespawnDeathDrops = false;
    public double itemSpreadMultiplier = 1.0;
    public boolean explosionResistantDeathDrops = false;

    // Item Overrides
    public List<String> keptItemOverrides = new ArrayList<>();
    public List<String> droppedItemOverrides = new ArrayList<>();
    public List<String> destroyedItemOverrides = new ArrayList<>();

    // Death Conditions / Overrides
    public List<String> bypassKeepInventoryDimensions = new ArrayList<>();
    public List<String> bypassKeepInventoryDamageSources = new ArrayList<>();

    // Memento Mori Settings
    public boolean applyMementoMori = false;
    public int mementoDuration = 600;
    public double mementoSpeedModifier = -0.20;
    public double mementoDamageModifier = -2.0;

    // Effects Settings
    public boolean keepPositiveEffects = false;
    public boolean keepNegativeEffects = false;

    // Respawn Health Settings
    public boolean scaleRespawnHealth = false;
    public double respawnHealthPercent = 1.0;
    public double maxRespawnHealth = 20.0;
    public boolean enableHealthReductionOnDeath = false;
    public double healthLossPerDeath = 2.0;
    public double minMaxHealth = 10.0;
    public double maxMaxHealth = 30.0;
    public double crystalHeartHealthAmount = 2.0;
    public boolean enableSpawnerShardDrop = true;
    public double spawnerShardDropChance = 1.0;
    public int spawnerShardDropCount = 2;

    // Stamina Settings (Modest Meals)
    public boolean enableStaminaReductionOnDeath = false;
    public double staminaLossPerDeath = 2.0;
    public double minStaminaCapacity = 10.0;
    public double maxStaminaCapacity = 30.0;
    public double crystalBoltStaminaAmount = 2.0;

    // Miscellaneous Settings
    public boolean restrictRespawning = false;
    public int respawnRandomRadius = 0;
    public boolean displayDeathCoordinates = true;

    public static ModConfig get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, ModConfig.class);
            } catch (Exception e) {
                Constants.LOG.error("Failed to load reliable_requiem.json", e);
                INSTANCE = new ModConfig();
                save();
            }
        } else {
            INSTANCE = new ModConfig();
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            Constants.LOG.error("Failed to save reliable_requiem.json", e);
        }
    }
}