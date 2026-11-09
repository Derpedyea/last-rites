### Added

- Corpses: items and experience dropped on death go into a corpse (a mannequin wearing the player's skin) where they died.
    - The body collapses as a ragdoll centered where the player died, with ashen skin (drawn by the mod on the client).
    - Right-click opens it; sneak + right-click puts every item back in the slot it came from, including Trinkets slots.
    - Owner-only for `corpseLockSeconds` (default 30 minutes of game time), operators can always open it.
    - Corpses can't be hurt or pushed and never despawn; an empty corpse disappears.
    - New config category: `corpsesEnabled`, `corpseLockSeconds`, `corpseOpsBypassLock`.

### Changed

- Forked from Reliable Requiem 2.1.0 as Last Rites: mod id `last_rites`, config file `config/last_rites.json`.
  Item and effect ids are unchanged.
- Builds only the Minecraft 26.3 Fabric target.
