# Trivium

English | [日本語](README.ja.md)

Adds a paxel: one tool that does the work of a pickaxe, an axe and a shovel. It mines
everything the three of them mine and does on a right-click what all three of them do.
It reaches what a hoe reaches and tills what a hoe tills.

- Six materials: wood, stone, iron, gold, diamond and netherite. Each digs at the speed
  of its own material's tools.
- Right-click strips logs, scrapes copper, removes wax, tills farmland and douses a
  campfire.
- It does not flatten a path. That answers the same click as tilling, and tilling is kept.
- Made from a pickaxe, an axe and a shovel of the same material, all three undamaged.
- Its durability is all three tools' worth.
- A wooden paxel burns in a furnace.
- The mining enchantments apply, because a paxel is a pickaxe, an axe and a shovel.
- Blocks other mods put in the vanilla mining tables are covered too.

## Target

| | |
|---|---|
| Minecraft | 1.21.1 |
| Loader | NeoForge 21.1.248 |
| Java | 21 |

## Build

```
run.bat                         # compile and launch a dev client
gradlew build                   # produce the jar
gradlew runGameTestServer       # run every game test, headless, then exit
gradlew runData                 # regenerate models, recipes, tags, language and test structures
python tools/make_textures.py   # regenerate the item sprites
```

`JAVA_HOME` must point at a JDK 21, or `java` must be on `PATH`.

## License

MIT.
