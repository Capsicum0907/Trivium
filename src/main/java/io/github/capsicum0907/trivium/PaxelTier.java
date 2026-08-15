package io.github.capsicum0907.trivium;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

/**
 * A vanilla tier seen through the paxel's terms: everything the same except how
 * long it lasts.
 *
 * <p><b>A paxel is worth every tool it replaces.</b> It covers
 * {@link PaxelItem#MINEABLE} families of block and is crafted from one tool per
 * family, so it holds that many tools' worth of use. Breaking a block still costs one
 * point, so the number of blocks a paxel breaks is the number the three tools between
 * them would have broken: the trade is even, and what is gained is the inventory slot
 * and never having to switch, not the mileage.
 *
 * <p>The number is not written down anywhere: it follows the count of families, so
 * a fourth would move it on its own.
 *
 * <p>This has to be a tier rather than a property on the item: {@code TieredItem}
 * writes the durability from {@code tier.getUses()} after the properties are
 * handed to it, so anything set beforehand is overwritten.
 */
public record PaxelTier(Tier base) implements Tier {
    @Override
    public int getUses() {
        return base.getUses() * PaxelItem.MINEABLE.size();
    }

    @Override
    public float getSpeed() {
        return base.getSpeed();
    }

    @Override
    public float getAttackDamageBonus() {
        return base.getAttackDamageBonus();
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return base.getIncorrectBlocksForDrops();
    }

    @Override
    public int getEnchantmentValue() {
        return base.getEnchantmentValue();
    }

    @Override
    public Ingredient getRepairIngredient() {
        return base.getRepairIngredient();
    }
}
