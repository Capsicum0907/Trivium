package io.github.capsicum0907.trivium;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

/**
 * The families of block a paxel digs, and where each one came from.
 *
 * <p>Each row names the same family twice, from either side. {@link #mineable()} is
 * what the <em>block</em> says — the tag that decides whether this family may break
 * it. {@link #itemTag()} is what the <em>item</em> says — the tag vanilla assembles
 * {@code enchantable/mining}, {@code enchantable/durability} and the rest out of.
 * A paxel needs both: the first to dig, the second to be treated as a tool by
 * everything that asks by tag rather than by class.
 *
 * <p><b>Not every family is paid for.</b> Three of them name the tool the recipe
 * asks for; the hoe names none. It is covered anyway, as a gift. That distinction is
 * the whole of the rule elsewhere — {@link #crafted()} is what the recipe iterates
 * and what {@link PaxelTier} multiplies durability by, so the hoe widens what the
 * item reaches without changing what it costs or how long it lasts.
 */
public enum PaxelFamily {
    PICKAXE(BlockTags.MINEABLE_WITH_PICKAXE, ItemTags.PICKAXES, ItemAbilities.PICKAXE_DIG, PaxelMaterial::pickaxe),
    AXE(BlockTags.MINEABLE_WITH_AXE, ItemTags.AXES, ItemAbilities.AXE_DIG, PaxelMaterial::axe),
    SHOVEL(BlockTags.MINEABLE_WITH_SHOVEL, ItemTags.SHOVELS, ItemAbilities.SHOVEL_DIG, PaxelMaterial::shovel),
    HOE(BlockTags.MINEABLE_WITH_HOE, ItemTags.HOES, ItemAbilities.HOE_DIG, null);

    private final TagKey<Block> mineable;
    private final TagKey<Item> itemTag;
    private final ItemAbility dig;
    @Nullable
    private final Function<PaxelMaterial, Item> ingredient;

    PaxelFamily(TagKey<Block> mineable, TagKey<Item> itemTag, ItemAbility dig,
            @Nullable Function<PaxelMaterial, Item> ingredient) {
        this.mineable = mineable;
        this.itemTag = itemTag;
        this.dig = dig;
        this.ingredient = ingredient;
    }

    /** The tag a block carries when this family is the one that breaks it. */
    public TagKey<Block> mineable() {
        return mineable;
    }

    /** The tag the family's own tools carry, and which a paxel joins. */
    public TagKey<Item> itemTag() {
        return itemTag;
    }

    /** Digging, as an announced ability. The family's other abilities are not implied. */
    public ItemAbility dig() {
        return dig;
    }

    /** The tool of this family the recipe asks for, when it asks for one. */
    public Optional<Item> ingredient(PaxelMaterial material) {
        return Optional.ofNullable(ingredient).map(tool -> tool.apply(material));
    }

    /**
     * The families a paxel is made from — the ones it is paid for and lasts as long as.
     * Read off the rows rather than listed again, so a family cannot be in the recipe
     * without lengthening the tool, or lengthen it without being in the recipe.
     */
    public static List<PaxelFamily> crafted() {
        return CRAFTED;
    }

    private static final List<PaxelFamily> CRAFTED =
            Stream.of(values()).filter(family -> family.ingredient != null).toList();
}
