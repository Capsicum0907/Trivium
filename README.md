# Trivium

One tool where three would be. A **paxel** mines everything a pickaxe, an axe and a
shovel each mine, and strips, scrapes, waxes off, flattens and douses as they do.

*Trivium* is Latin for the place where three roads meet.

> **Status: stage 2.** Loads in a client, and nine game tests pass headlessly.

## Target

| | |
|---|---|
| Minecraft | 1.21.1 |
| Loader | NeoForge 21.1.248 |
| Java | 21 |

## Design

Two properties are worth stating up front, because they are why this mod is as
small as it is.

**What a paxel mines is data, not code.** Since 1.20.5 a tool's reach lives in the
`Tool` data component as a list of rules over block tags. A paxel is therefore one
component carrying three mine-rules instead of one — no dispatch, no tool-type
check, and blocks that other mods put in `mineable/pickaxe`, `mineable/axe` or
`mineable/shovel` are covered without a line of per-mod code.

The tier's deny-rule has to come first in that list. `Tool.isCorrectForDrops`
answers with the first matching rule that has an opinion, so a mine-rule placed
ahead of it would let a wooden paxel drop obsidian.

**What it does on right-click is not reimplemented.** Stripping a log, scraping
copper, removing wax, flattening a path and dousing a campfire all resolve through
`BlockState.getToolModifiedState`, which is the hook the vanilla items call and the
one other mods override. `PaxelItem` only decides the order to try them in and how
to announce the result — the behaviours themselves stay where they were.

`canPerformAction` reports the union of the three tools' abilities. That is the
gate other code consults, not the thing that performs the work; both are needed,
and they are not the same mechanism.

One deliberate difference from the vanilla items: the effect runs on the server
only, and the sound is played to everyone rather than predicted on the client.
Vanilla's axe changes the block on both sides so the swinging player hears it
instantly; here the player waits for the round trip. It is one consistent path
instead of two that have to agree, at the cost of a frame or two.

**One table, six items.** `PaxelMaterial` is the only place a material is written
down. Registration, models, recipes and language all iterate it, so a new variant
is a new row and cannot fall out of step with itself.

**A paxel is worth every tool it replaces.** It mines three families and is crafted
from one tool per family, so it holds three tools' worth of durability and mines
each family exactly as fast as that family's tool would. Breaking a block still
costs one point, so the trade is even in blocks broken: what is gained is the
inventory slot and never having to switch, not the mileage. The multiplier is not
written down — it is the number of families, so a fourth would move it on its own.

That is also why the recipe demands the three tools be **undamaged**. A worn tool
would otherwise buy back a full paxel, which would make a given amount of ore worth
twice what it is.

Durability has to be expressed as a `Tier` (`PaxelTier`) rather than as an item
property, because `TieredItem` writes it from `tier.getUses()` after the properties
are handed over and would overwrite anything set beforehand.

There is no config. Data components are baked when the item is registered, long
before a server config is read, so a setting for reach or speed would silently do
nothing.

## Build

```
run.bat                  # compile and launch a dev client - double-clickable
gradlew build            # produce the jar
gradlew runGameTestServer # run every game test, headless, then exit
gradlew runData          # regenerate models, recipes, language and test structures
python tools/make_textures.py   # regenerate the item sprites
```

`JAVA_HOME` must point at a JDK 21, or `java` must be on `PATH`.

Nothing under `src/generated/resources` is edited by hand, and neither are the item
PNGs — `tools/make_textures.py` draws all six from one shape and one palette table.
The sprites are placeholder art, drawn to be consistent rather than to be good.

## Roadmap

- [x] **0** — scaffold, registry, creative tab, datagen
- [x] **1** — the paxel: mining reach, right-click behaviours, recipes, textures
- [x] **2** — checked. The client loads all six with no missing model or texture,
  and nine game tests cover the three tag families, mining speed against each
  dedicated tool, durability, tier gating in both directions, and all five
  right-click behaviours. Left open: enchantments, which nothing has exercised yet
- [ ] **3** — open questions below

## Open questions

- Whether the hoe belongs in it. Three is what the name says, and a hoe's till is a
  different kind of action from digging, but a four-way tool is the more common ask.
- Whether a paxel should be repairable from any of the three tools, or only from its
  material as it is now.
- Interaction with [Fodina](../Fodina): Fodina decides a tool type from the *block's*
  mining tag rather than from a table of tools, so a paxel should need no special
  case there. Expected, not yet verified. The two mods have no dependency either way.

## License

Not decided yet. Until it is, the metadata says All Rights Reserved.
