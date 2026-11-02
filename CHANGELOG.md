# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.1.0] - 2026-11-02

### Added

- Added compatibility with (the currently unreleased) Modest Meals.
    - Added the Stamina Crystal, crafted from 8 Crystal Shards surrounding Redstone.
        - Using one permanently increases max stamina by 1 bolt, up to a configurable limit.
        - Only available when Modest Meals is installed.
    - Max stamina is now kept through death.
    - Added a Stamina config category.

### Changed

- Renamed Crystal Hearts to Heart Crystals. Existing items are kept.
- Heart Crystals are now crafted from 8 Crystal Shards surrounding a Ghast Tear.
- Mob spawners now drop 2 Crystal Shards by default (previously 1).
- Updated the Heart Crystal and Crystal Shard textures (@Baisylia).

## [2.0.0] - 2026-10-01

### Added

- Ported to 26.3.
- Added `minKeptDurability` config option to set the minimum durability percentage kept items can be reduced to by death
  durability loss.
- Added `allowDurabilityBreak` config option to toggle whether death durability loss can break kept items (default
  `true`).

### Fixed

- Fixed the accessories screen desyncing after death when a kept accessory adds extra slots, which could also kick
  players from servers.
- Fixed Curios slots being handled twice on death when the Accessories Compatibility Layer is installed.

## [1.3.0] - 2026-08-15

### Added

- Added compatibility with Heart Crystals.

## [1.2.0] - 2026-08-06

### Added

- Added configurable health loss upon death.
- Added Crystal Shards and Crystal Hearts for restoring health, inspired by Matcha Flavoured.
- Added more config options related to health loss/restoration.

### Changed

- Changed `spread` config options from a toggle to a multiplier.

### Fixed

- Fixed conditional cloth config registration on Fabric.

## [1.1.1] - 2026-07-10

### Fixed

- Fixed desync with Accessories.

## [1.1.0] - 2026-07-07

### Added

- Added support for keeping Curios/Trinkets on death.

### Fixed

- Fixed client/server desync with Memento Mori.

## [1.0.3] - 2026-05-26

### Fixed

- Fixed items meant to be kept on death being lost if you exit to title screen.
- Items meant to be kept on death no longer visually disappear when you die.

## [1.0.2] - 2026-04-29

### Fixed

- Fixed missing refmap crash.

## [1.0.1] - 2026-04-29

### Fixed

- Fixed Forge crash on 1.20.1.

## [1.0.0] - 2026-04-22

- Initial release.