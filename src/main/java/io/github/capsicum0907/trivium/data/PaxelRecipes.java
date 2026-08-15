package io.github.capsicum0907.trivium.data;

import java.util.concurrent.CompletableFuture;

import io.github.capsicum0907.trivium.PaxelMaterial;
import io.github.capsicum0907.trivium.TriviumItems;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

/**
 * A paxel is made from exactly the three tools it replaces, of its own material.
 * The recipe is shapeless because there is no arrangement to remember, and it needs
 * no explanation once seen.
 *
 * <p>The three have to be undamaged. A paxel carries all three tools' worth of
 * durability, so a recipe that accepted worn ones would turn three spent tools into
 * a full one and double what a given amount of ore is worth. Requiring them fresh
 * makes the trade break even: the durability that goes in is the durability that
 * comes out, and what is gained is the inventory slot.
 */
public class PaxelRecipes extends RecipeProvider {
    public PaxelRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        for (PaxelMaterial material : PaxelMaterial.values()) {
            ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, TriviumItems.PAXELS.get(material).get())
                    .requires(undamaged(material.pickaxe()))
                    .requires(undamaged(material.axe()))
                    .requires(undamaged(material.shovel()))
                    // The pickaxe alone is enough to unlock it: having one means the
                    // other two are already within reach of the same material.
                    .unlockedBy("has_pickaxe", has(material.pickaxe()))
                    .save(output);
        }
    }

    /**
     * The tool as it comes off the crafting table. Not strict: an enchanted-but-unused
     * tool still counts, because the enchantment is the player's to lose, not a reason
     * to refuse the craft.
     */
    private static Ingredient undamaged(Item tool) {
        return DataComponentIngredient.of(false,
                DataComponentMap.builder().set(DataComponents.DAMAGE, 0).build(), tool);
    }
}
