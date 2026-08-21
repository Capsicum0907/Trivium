package io.github.capsicum0907.trivium;

import java.util.Set;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

/**
 * The three tools a paxel is. Everything that is true of a family and not of a
 * material lives here, so a fourth arm would be a row rather than an edit in four
 * places.
 *
 * <p>Each row names the same family twice, from either side. {@link #mineable()} is
 * what the <em>block</em> says — the tag that decides whether this family may break
 * it. {@link #itemTag()} is what the <em>item</em> says — the tag vanilla assembles
 * {@code enchantable/mining}, {@code enchantable/durability} and the rest out of.
 * A paxel needs both: the first to dig, the second to be treated as a tool by
 * everything that asks by tag rather than by class.
 *
 * <p>The count of rows is load-bearing. {@link PaxelTier} multiplies durability by
 * it, so the rule "a paxel is worth every tool it replaces" is not a number written
 * down anywhere.
 */
public enum PaxelFamily {
    PICKAXE(BlockTags.MINEABLE_WITH_PICKAXE, ItemTags.PICKAXES, ItemAbilities.DEFAULT_PICKAXE_ACTIONS),
    AXE(BlockTags.MINEABLE_WITH_AXE, ItemTags.AXES, ItemAbilities.DEFAULT_AXE_ACTIONS),
    SHOVEL(BlockTags.MINEABLE_WITH_SHOVEL, ItemTags.SHOVELS, ItemAbilities.DEFAULT_SHOVEL_ACTIONS);

    private final TagKey<Block> mineable;
    private final TagKey<Item> itemTag;
    private final Set<ItemAbility> abilities;

    PaxelFamily(TagKey<Block> mineable, TagKey<Item> itemTag, Set<ItemAbility> abilities) {
        this.mineable = mineable;
        this.itemTag = itemTag;
        this.abilities = abilities;
    }

    /** The tag a block carries when this family is the one that breaks it. */
    public TagKey<Block> mineable() {
        return mineable;
    }

    /** The tag the family's own tools carry, and which a paxel joins. */
    public TagKey<Item> itemTag() {
        return itemTag;
    }

    /** What the family's tool claims it can do, in the sense {@code canPerformAction} answers. */
    public Set<ItemAbility> abilities() {
        return abilities;
    }

    /** How many tools a paxel stands in for. */
    public static int count() {
        return values().length;
    }
}
