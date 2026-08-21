package io.github.capsicum0907.trivium.data;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import io.github.capsicum0907.trivium.PaxelFamily;
import io.github.capsicum0907.trivium.PaxelMaterial;
import io.github.capsicum0907.trivium.Trivium;
import io.github.capsicum0907.trivium.TriviumItems;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * The tags that make a paxel count as a tool to everything that asks by tag.
 *
 * <p>Without them the item works and cannot be enchanted, which is the worst shape a
 * bug can take: nothing reports an error, the anvil and the enchanting table simply
 * decline. Every enchantment in 1.21 names an item tag as the set of things it may
 * sit on — efficiency and silk touch ask for {@code enchantable/mining}, fortune for
 * {@code enchantable/mining_loot}, unbreaking and mending for
 * {@code enchantable/durability} — and an item in none of them is enchantable by
 * nothing at all.
 *
 * <p><b>The paxel joins the three tool tags rather than the enchantment tags.</b>
 * Vanilla builds every {@code enchantable/*} tag out of {@code #minecraft:pickaxes},
 * {@code #axes} and {@code #shovels}, so joining those says the thing that is
 * actually true — a paxel is all three — once, and lets the six enchantment tags
 * follow. It also means a tag added later, by the game or by another mod, reaches
 * the paxel without a change here.
 *
 * <p>The two {@code c:} tags are added by hand because NeoForge fills them with a
 * list of items rather than with the tool tags, so nothing would carry the paxel
 * into them.
 */
public class PaxelItemTags extends ItemTagsProvider {
    /**
     * Tags every paxel joins beyond its families'. {@code c:tools} itself is assembled
     * from these two, so it is not listed.
     */
    private static final List<TagKey<Item>> COMMON = List.of(
            Tags.Items.MINING_TOOL_TOOLS,
            // The paxel carries the axe's attack values, and the axe is in this tag.
            Tags.Items.MELEE_WEAPON_TOOLS);

    public PaxelItemTags(PackOutput output, CompletableFuture<HolderLookup.Provider> registries,
            ExistingFileHelper existingFileHelper) {
        super(output, registries, CompletableFuture.completedFuture(TagsProvider.TagLookup.empty()),
                Trivium.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        for (PaxelFamily family : PaxelFamily.values()) {
            add(family.itemTag());
        }
        for (TagKey<Item> tag : COMMON) {
            add(tag);
        }
    }

    /** Every paxel, so a material cannot be left out of a tag by hand. */
    private void add(TagKey<Item> tag) {
        for (PaxelMaterial material : PaxelMaterial.values()) {
            tag(tag).add(TriviumItems.PAXELS.get(material).get());
        }
    }
}
