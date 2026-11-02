# Reliable Requiem

<a href='https://files.minecraftforge.net'><img alt="forge" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/forge_vector.svg"></a>
<a href='https://fabricmc.net'><img alt="fabric" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/fabric_vector.svg"></a>
<a href='https://neoforged.net/'><img alt="neoforge" height="56" src="https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/neoforge_vector.svg"></a>

## About

**Reliable Requiem** is a highly configurable death penalty and death recovery mod, based on the features
of [Corpse Complex](https://modrinth.com/mod/corpse-complex) for 1.16.5. It gives modpack
makers and players control over exactly what happens when you die, from inventory management to respawn
conditions, offering a customizable alternative to the `keepInventory` gamerule.

## Features

### Inventory Control

You can configure percentage chances to keep items in specific inventory sections (Hotbar, Armor, Offhand, and Main
Inventory).

* **Durability Penalty:** Apply a configurable percentage of durability loss to items that are kept through death, with
  an optional minimum durability percentage and a toggle to prevent kept items from breaking.
* **Item Overrides:** Specify exact items to always keep, always drop, or be completely destroyed on death.
* **Tag Support:** Built-in support for the `c:soulbound` enchantment tag and an additional
  `reliable_requiem:retained_on_death` item tag to retain specific items.

### Death Drops

Tired of losing your items to creepers or the despawn timer?

* **Protected Drops:** Make your death drops immune to explosions and prevent them from ever despawning.
* **Item Spread Multiplier:** Adjust the velocity and spread of dropped items and experience orbs on death.

### Respawning

Customize the exact state the player is in when they return to life:

* **Hunger & Saturation:** Choose to keep your food and saturation levels, or enforce minimum and maximum food caps upon
  respawning.
* **Health Reduction on Death:** Configurable maximum health loss upon death down to a set minimum limit to encourage a
  more cautious playstyle.
* **Crystals & Shards:** Breaking Mob Spawners drops **Crystal Shards**. Surround a Ghast Tear with 8 shards to craft
  a **Heart Crystal**, which permanently increases max health by +1 heart when used, up to a configurable maximum limit.
* **Stamina Crystals (Modest Meals):** With Modest Meals installed, surround Redstone with 8 shards to craft a
  **Stamina Crystal**, which permanently increases max stamina by +1 bolt. Max stamina carries over through death and
  can optionally be reduced on death, just like max health.
* **Health Scaling:** Respawn with a percentage of your maximum health or set a hard cap on respawn health.
* **Status Effects:** Configurable options to let positive or negative potion effects persist through death.
* **Experience Tweaks:** Control the exact percentage of XP lost, how much drops per level, and the maximum dropped XP.
* **Randomized Respawn:** Add a configurable random radius to your respawn location to make getting back to your base a
  bit more challenging.

### Memento Mori

An optional, custom post-death debuff. When enabled, players respawn with the **Memento Mori** effect, which temporarily
reduces movement speed and attack damage. The duration and severity of the debuff are configurable.

### Quality of Life Tweaks

* **Death Coordinates:** Optionally display your exact death coordinates directly on the death screen.
* **Bypass Rules:** Disable keep-inventory tweaks entirely for specific dimensions or specific damage sources.

## Configuration

Reliable Requiem includes optional [YACL](https://modrinth.com/mod/yacl) integration so you can tweak
almost every aspect of the mod in-game and instantly apply your changes.

## Credits

Thanks to the excellent [Matcha Flavoured](https://modrinth.com/datapack/matcha-flavoured) datapack for the Crystal
Heart idea and assets. Licensed under [CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/).

## License

[![Code license (GPL-3.0)](https://img.shields.io/badge/code%20license-%20GNU%20GPLv3-green.svg?style=flat-square)](https://github.com/evanbones/Reliable-Requiem/blob/26.1/LICENSE)

---

[![discord-plural](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/social/discord-plural_vector.svg)](https://discord.com/invite/JcGRdT6Pbx) [![github-plural](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/social/github-plural_vector.svg)](https://github.com/evanbones/Reliable-Requiem)
