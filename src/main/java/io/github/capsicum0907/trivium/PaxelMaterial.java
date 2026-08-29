package io.github.capsicum0907.trivium;

import java.util.function.Supplier;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

/**
 * Every paxel variant, and everything that differs between them. Nothing else in
 * the mod may name a material: registration, models, recipes and language all
 * iterate this table, so adding a variant is adding a row.
 *
 * <p>The attack values are the axe's rather than the pickaxe's or the shovel's.
 * A paxel is the heaviest of the three, and taking the strongest profile is the
 * one choice that does not need a rule to explain it.
 */
public enum PaxelMaterial {
    WOOD("wooden", Tiers.WOOD, 6.0F, -3.2F, false, 200, () -> Items.WOODEN_PICKAXE, () -> Items.WOODEN_AXE, () -> Items.WOODEN_SHOVEL),
    STONE("stone", Tiers.STONE, 7.0F, -3.2F, false, 0, () -> Items.STONE_PICKAXE, () -> Items.STONE_AXE, () -> Items.STONE_SHOVEL),
    IRON("iron", Tiers.IRON, 6.0F, -3.1F, false, 0, () -> Items.IRON_PICKAXE, () -> Items.IRON_AXE, () -> Items.IRON_SHOVEL),
    GOLD("golden", Tiers.GOLD, 6.0F, -3.0F, false, 0, () -> Items.GOLDEN_PICKAXE, () -> Items.GOLDEN_AXE, () -> Items.GOLDEN_SHOVEL),
    DIAMOND("diamond", Tiers.DIAMOND, 5.0F, -3.0F, false, 0, () -> Items.DIAMOND_PICKAXE, () -> Items.DIAMOND_AXE, () -> Items.DIAMOND_SHOVEL),
    NETHERITE("netherite", Tiers.NETHERITE, 5.0F, -3.0F, true, 0, () -> Items.NETHERITE_PICKAXE, () -> Items.NETHERITE_AXE, () -> Items.NETHERITE_SHOVEL);

    private final String prefix;
    private final Tier tier;
    private final float attackDamage;
    private final float attackSpeed;
    private final boolean fireResistant;
    private final int burnTime;
    private final Supplier<Item> pickaxe;
    private final Supplier<Item> axe;
    private final Supplier<Item> shovel;

    PaxelMaterial(String prefix, Tier tier, float attackDamage, float attackSpeed, boolean fireResistant,
            int burnTime, Supplier<Item> pickaxe, Supplier<Item> axe, Supplier<Item> shovel) {
        this.prefix = prefix;
        this.tier = tier;
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
        this.fireResistant = fireResistant;
        this.burnTime = burnTime;
        this.pickaxe = pickaxe;
        this.axe = axe;
        this.shovel = shovel;
    }

    /** The registry path, following the vanilla naming it sits beside: {@code golden_paxel}, not {@code gold_paxel}. */
    public String itemName() {
        return prefix + "_paxel";
    }

    /** The display name, assembled the same way so the two can never drift apart. */
    public String displayName() {
        return Character.toUpperCase(prefix.charAt(0)) + prefix.substring(1) + " Paxel";
    }

    public Tier tier() {
        return tier;
    }

    public float attackDamage() {
        return attackDamage;
    }

    public float attackSpeed() {
        return attackSpeed;
    }

    public boolean fireResistant() {
        return fireResistant;
    }

    /**
     * Ticks this burns for in a furnace; zero for the materials that are not fuel.
     *
     * <p>A wooden paxel is wood, and the game burns every other wooden tool, so not
     * burning is the surprise — vanilla's fuel table names the wooden tools one at a
     * time rather than reading a tag, which is why an item from outside is missed
     * however wooden it is. The number here is the wooden tools' own, and a game test
     * holds it to that rather than to the figure written down, so a change in vanilla
     * shows up as a failure instead of a quiet disagreement.
     *
     * <p>Zero rather than -1 for the rest, tempting as -1 is to mean "no opinion, ask
     * the game": a negative burn time is rejected where it is read, so a stone paxel
     * throws rather than falling back to anything.
     */
    public int burnTime() {
        return burnTime;
    }

    /** The three tools this paxel replaces, which are also what it is made from. */
    public Item pickaxe() {
        return pickaxe.get();
    }

    public Item axe() {
        return axe.get();
    }

    public Item shovel() {
        return shovel.get();
    }
}
