# Trivium

One tool where three would be. A **paxel** mines everything a pickaxe, an axe and a
shovel each mine, and strips, scrapes, waxes off and douses as they do. It digs what
a hoe digs and tills what a hoe tills as well, without being made of one.

*Trivium* is Latin for the place where three roads meet. The three are the tools it
is made from; the hoe came later and did not rename it.

> **Status: released, 1.0.0.** Loads in a client, and thirteen game tests pass headlessly.

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
component carrying a mine-rule per family instead of one — no dispatch, no tool-type
check, and blocks that other mods put in `mineable/pickaxe`, `mineable/axe`,
`mineable/shovel` or `mineable/hoe` are covered without a line of per-mod code.

The tier's deny-rule has to come first in that list. `Tool.isCorrectForDrops`
answers with the first matching rule that has an opinion, so a mine-rule placed
ahead of it would let a wooden paxel drop obsidian.

**What it does on right-click is not reimplemented.** Stripping a log, scraping
copper, removing wax, tilling farmland and dousing a campfire all resolve through
`BlockState.getToolModifiedState`, which is the hook the vanilla items call and the
one other mods override. `PaxelItem` only decides which of them to offer, in what
order, and how to announce the result — the behaviours themselves stay where they
were.

**It tills, and it does not flatten.** Those two answer the same click on the same
blocks: grass, dirt, coarse dirt and rooted dirt are in both vanilla tables, both
want room above, and neither cares which face was hit. Nothing in the click tells
them apart, so one of them had to go rather than be ordered behind the other.
Farmland is worth more than a path, and a path is a shovel away.

Dropping it is done at the ability, not at the order. `canPerformAction` reports
digging each family plus exactly the right-clicks the item offers, and
`getToolModifiedState` asks `canPerformAction` before it will do anything — so
leaving `shovel_flatten` out is not an order that hides flattening but an item that
cannot flatten, and other mods are told the same thing the item does. The list is
read off the behaviours rather than copied from the vanilla tools' sets, because a
claim with nothing behind it is a lie the game repeats on the mod's behalf.

One deliberate difference from the vanilla items: the effect runs on the server
only, and the sound is played to everyone rather than predicted on the client.
Vanilla's axe changes the block on both sides so the swinging player hears it
instantly; here the player waits for the round trip. It is one consistent path
instead of two that have to agree, at the cost of a frame or two.

**One table, six items.** `PaxelMaterial` is the only place a material is written
down. Registration, models, recipes and language all iterate it, so a new variant
is a new row and cannot fall out of step with itself. `PaxelFamily` is the same
arrangement along the other axis: the three tools a paxel is, each row naming the
block tag the family digs and the item tag its own tools carry.

**It joins the three tool tags, and the enchantments follow.** A paxel is in
`#minecraft:pickaxes`, `#axes` and `#shovels`. That is not a convenience: every
enchantment in 1.21 names an item tag as what it may sit on, and vanilla builds
`enchantable/mining`, `enchantable/mining_loot`, `enchantable/durability` and the
rest out of exactly those three tags. Saying the true thing once — a paxel is all
three — is what makes efficiency, fortune, silk touch, unbreaking and mending
apply, and lets a tag added later reach the paxel without a change here.

This is the shape a bug takes when nothing reports it. Before the tags existed the
item worked in every visible way and simply could not be enchanted: the anvil
declined, the table offered nothing, and no error was printed anywhere. Two of the
game tests exist to keep it that way, and they check both halves of the gate —
what may hold an enchantment at all, and what the table will offer — because those
are separate fields that fail separately.

Joining a tag is a wide claim, so here is everything in the game that reads those
three tags, counted rather than assumed. Across all 5,613 data files in 1.21.1
there are five: the four `enchantable/*` tags above, and `breaks_decorated_pots`,
which lists every tool there is and means a paxel now cracks a decorated pot into
its sherds instead of shattering it. No trade, loot table or advancement reads
them. The claim reaches exactly as far as it should.

The two `c:` tags — `tools/mining_tool` and `tools/melee_weapon` — are written out
by hand, because NeoForge fills those with a list of items rather than with the
tool tags, so nothing would carry a paxel into them. Nothing in the game reads
them; they are a signal to other mods, and which of the two a paxel belongs in is
a judgment rather than something the source settles. It mines, and it hits with
the axe's weight, so it is in both.

**A paxel lasts as long as the tools it is made from.** It is crafted from a
pickaxe, an axe and a shovel, so it holds three tools' worth of durability and mines
each of those families exactly as fast as that family's tool would. Breaking a block
still costs one point, so the trade is even in blocks broken: what is gained is the
inventory slot and never having to switch, not the mileage. The multiplier is not
written down — it is the number of families the recipe asks for.

**The hoe is a gift, and the table says so.** `PaxelFamily` has four rows but only
three name a tool the recipe wants. The hoe row names none, which is the whole of
the rule: `crafted()` is what the recipe iterates and what durability multiplies by,
`values()` is what mining, tags and abilities iterate. So a paxel reaches leaves,
hay, sculk and moss, and tills, without costing a fourth tool or lasting a third
longer. Nothing had to be written twice for that to hold, and a family cannot be in
the recipe without lengthening the tool, or lengthen it without being in the recipe.

That is also why the recipe demands the three tools be **undamaged**. A worn tool
would otherwise buy back a full paxel, which would make a given amount of ore worth
twice what it is.

Durability has to be expressed as a `Tier` (`PaxelTier`) rather than as an item
property, because `TieredItem` writes it from `tier.getUses()` after the properties
are handed over and would overwrite anything set beforehand.

**A paxel is not upgraded, it is assembled.** There is no smithing recipe: a
netherite paxel is three netherite tools, the same as every other material is three
of its own. The cost of knowing that is real and is accepted here — an enchanted
diamond paxel cannot be carried across to netherite the way an enchanted diamond
pickaxe can, because vanilla smithing takes exactly one item as its addition, so the
only recipe it could express would hand over three tools' worth of netherite for one
ingot. Paying the ingots and re-enchanting is the honest half of that trade.

There is no config. Data components are baked when the item is registered, long
before a server config is read, so a setting for reach or speed would silently do
nothing.

## Build

```
run.bat                  # compile and launch a dev client - double-clickable
gradlew build            # produce the jar
gradlew runGameTestServer # run every game test, headless, then exit
gradlew runData          # regenerate models, recipes, tags, language and test structures
python tools/make_textures.py   # regenerate the item sprites
```

`JAVA_HOME` must point at a JDK 21, or `java` must be on `PATH`.

Nothing under `src/generated/resources` is edited by hand, and neither are the item
PNGs — `tools/make_textures.py` draws all six from one shape and one palette table.

**The silhouette is the genre's, not an invention.** Ten paxel mods were opened and
their sprites read pixel by pixel. All ten draw the same skeleton — a blade above, an
arm hooking down its right side, a thin handle on the diagonal — the four most
downloaded included. Azure Paxels draws it heavier, with the pick
grown until it fills most of the square, and Piercing Paxels stands a sword up
through the middle of it — but both keep the three parts and their arrangement
underneath. There is no second way of drawing this item in use. A tool that does not look like its genre is harder to recognise
than one that does, and being novel here would cost the player something and buy
nothing. What is Trivium's own is the squared foot at the end of the arm and the
drawn-out left point on the blade. The pixels come from the script; none were
copied.

**The tones are vanilla's, and the shading is a rule, not a hand.** Each material's
ramp was sampled out of that material's own pickaxe, axe and shovel, because a paxel
is seen in a row of slots beside those tools and borrowing their exact tones is what
makes it look like it belongs. The rule was read off the same textures by counting
pixels: every edge is dark, the edge that turns away below-right is darkest, and the
light sits two steps inside the lit edge rather than on it. The shaft is three across
with a core that alternates by row — a shaft in one flat colour is what reads as
plastic however good the head is. An iron paxel is now 9 colours over 75 pixels,
where vanilla's iron pickaxe is 9 over 68; it was 4 over 68 before.

## Roadmap

- [x] **0** — scaffold, registry, creative tab, datagen
- [x] **1** — the paxel: mining reach, right-click behaviours, recipes, textures
- [x] **2** — checked. The client loads all six with no missing model or texture,
  and thirteen game tests cover the four tag families, mining speed against each
  dedicated tool, durability, tier gating in both directions, every right-click
  behaviour including that it never flattens, and which enchantments the item takes
  and refuses
- [ ] **3** — the licence, and whatever the sprites still want. Every question under
  *Decided* below was open here until it was answered; nothing about how the item
  behaves is

## Decided

- ~~Whether the hoe belongs in it.~~ Decided: it does, as a family that is dug and
  tilled but not paid for. Tilling replaced flattening rather than joining it, for
  the reason above. The name stayed.
- ~~Whether a paxel should be repairable from any of the three tools.~~ Decided: no,
  the material only, as `TieredItem` already does through the tier's ingredient.
- ~~Interaction with [Fodina](../Fodina).~~ Read and closed. `ToolType.forBlock`
  keys off the *block's* `mineable/*` tag, and the only question asked of the held
  item is `ItemStack.isCorrectToolForDrops`, which reads the `Tool` component a
  paxel already carries. Nothing in Fodina dispatches on an item class. No special
  case is needed on either side, and the two mods still have no dependency.
  Confirmed in a running client on 2026-08-23: a paxel drives Fodina's bulk break
  with nothing configured, and the paxels now appear in Fodina's item groups for
  the three tool tags.

## License

Not decided yet. Until it is, the metadata says All Rights Reserved.
