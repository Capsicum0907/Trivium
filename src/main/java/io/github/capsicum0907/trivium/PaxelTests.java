package io.github.capsicum0907.trivium;

import java.util.List;

import io.github.capsicum0907.trivium.data.TestStructures;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * What a paxel does, checked without a person having to look.
 *
 * <p>Two of these cover failures that are silent in play. Tier gating is the worse
 * one: get the rule order wrong in the {@link net.minecraft.world.item.component.Tool}
 * component and a wooden paxel quietly drops obsidian, which nobody notices until
 * somebody reports it as a duplication trick.
 *
 * <p>Run with {@code gradlew runGameTestServer}.
 */
@GameTestHolder(Trivium.MODID)
@PrefixGameTestTemplate(false)
public final class PaxelTests {
    /** Inside the platform: standing on the floor with air above, where a path can form. */
    private static final BlockPos SUBJECT = new BlockPos(2, 1, 2);

    private PaxelTests() {
    }

    @GameTest(template = TestStructures.PLATFORM)
    public static void minesAllThreeFamilies(GameTestHelper helper) {
        ItemStack paxel = paxel(PaxelMaterial.IRON);
        check(paxel.isCorrectToolForDrops(Blocks.STONE.defaultBlockState()),
                "an iron paxel should mine stone, which is the pickaxe family");
        check(paxel.isCorrectToolForDrops(Blocks.OAK_LOG.defaultBlockState()),
                "an iron paxel should mine oak log, which is the axe family");
        check(paxel.isCorrectToolForDrops(Blocks.DIRT.defaultBlockState()),
                "an iron paxel should mine dirt, which is the shovel family");
        helper.succeed();
    }

    /**
     * A paxel lasts as long as the tools it was made from, and mines each family at
     * the speed the dedicated tool would. Together those two say the trade is even:
     * what is gained is the inventory slot, not the mileage.
     */
    @GameTest(template = TestStructures.PLATFORM)
    public static void holdsThreeToolsWorth(GameTestHelper helper) {
        for (PaxelMaterial material : PaxelMaterial.values()) {
            ItemStack paxel = paxel(material);
            int expected = material.tier().getUses() * PaxelItem.MINEABLE.size();
            check(paxel.getMaxDamage() == expected,
                    "a " + material.itemName() + " should hold " + expected
                            + " uses, not " + paxel.getMaxDamage());

            ItemStack pickaxe = new ItemStack(material.pickaxe());
            check(paxel.getDestroySpeed(Blocks.STONE.defaultBlockState())
                    == pickaxe.getDestroySpeed(Blocks.STONE.defaultBlockState()),
                    "a " + material.itemName() + " should mine stone as fast as its pickaxe");
        }
        helper.succeed();
    }

    @GameTest(template = TestStructures.PLATFORM)
    public static void mineFasterThanAHand(GameTestHelper helper) {
        ItemStack paxel = paxel(PaxelMaterial.IRON);
        ItemStack empty = ItemStack.EMPTY;
        for (Block block : new Block[] { Blocks.STONE, Blocks.OAK_LOG, Blocks.DIRT }) {
            BlockState state = block.defaultBlockState();
            check(paxel.getDestroySpeed(state) > empty.getDestroySpeed(state),
                    "an iron paxel should break " + block.getName().getString() + " faster than a bare hand");
        }
        helper.succeed();
    }

    /**
     * The deny-rule has to sit ahead of the mine-rules, or the tier stops meaning
     * anything. Both directions are checked: too permissive and too strict are both
     * failures, and only testing one of them would catch only one.
     */
    @GameTest(template = TestStructures.PLATFORM)
    public static void tierGating(GameTestHelper helper) {
        check(!paxel(PaxelMaterial.WOOD).isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()),
                "a wooden paxel must not drop obsidian");
        check(!paxel(PaxelMaterial.WOOD).isCorrectToolForDrops(Blocks.IRON_ORE.defaultBlockState()),
                "a wooden paxel must not drop iron ore");
        check(paxel(PaxelMaterial.STONE).isCorrectToolForDrops(Blocks.IRON_ORE.defaultBlockState()),
                "a stone paxel should drop iron ore");
        check(paxel(PaxelMaterial.DIAMOND).isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()),
                "a diamond paxel should drop obsidian");
        helper.succeed();
    }

    /**
     * The three tools have to be fresh. This is the other half of the durability
     * rule: a paxel holds all three tools' worth, so accepting a worn one would hand
     * back durability that was already spent. Pinned here because dropping the
     * requirement would regenerate the recipe silently and break nothing else.
     */
    @GameTest(template = TestStructures.PLATFORM)
    public static void refusesWornTools(GameTestHelper helper) {
        PaxelMaterial material = PaxelMaterial.IRON;
        ItemStack pickaxe = new ItemStack(material.pickaxe());
        ItemStack axe = new ItemStack(material.axe());
        ItemStack shovel = new ItemStack(material.shovel());

        check(craft(helper, pickaxe, axe, shovel), "three fresh tools should make a paxel");

        ItemStack worn = new ItemStack(material.pickaxe());
        worn.setDamageValue(1);
        check(!craft(helper, worn, axe, shovel), "a worn pickaxe should not make a paxel");
        helper.succeed();
    }

    private static boolean craft(GameTestHelper helper, ItemStack... tools) {
        CraftingInput input = CraftingInput.of(tools.length, 1, List.of(tools));
        return helper.getLevel().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel())
                .isPresent();
    }

    @GameTest(template = TestStructures.PLATFORM)
    public static void stripsALog(GameTestHelper helper) {
        rightClick(helper, Blocks.OAK_LOG.defaultBlockState());
        helper.assertBlockPresent(Blocks.STRIPPED_OAK_LOG, SUBJECT);
        helper.succeed();
    }

    @GameTest(template = TestStructures.PLATFORM)
    public static void flattensAPath(GameTestHelper helper) {
        rightClick(helper, Blocks.GRASS_BLOCK.defaultBlockState());
        helper.assertBlockPresent(Blocks.DIRT_PATH, SUBJECT);
        helper.succeed();
    }

    @GameTest(template = TestStructures.PLATFORM)
    public static void scrapesCopper(GameTestHelper helper) {
        rightClick(helper, Blocks.OXIDIZED_COPPER.defaultBlockState());
        helper.assertBlockPresent(Blocks.WEATHERED_COPPER, SUBJECT);
        helper.succeed();
    }

    @GameTest(template = TestStructures.PLATFORM)
    public static void takesWaxOff(GameTestHelper helper) {
        rightClick(helper, Blocks.WAXED_COPPER_BLOCK.defaultBlockState());
        helper.assertBlockPresent(Blocks.COPPER_BLOCK, SUBJECT);
        helper.succeed();
    }

    @GameTest(template = TestStructures.PLATFORM)
    public static void dousesACampfire(GameTestHelper helper) {
        rightClick(helper, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        helper.assertBlockState(SUBJECT, state -> !state.getValue(CampfireBlock.LIT),
                () -> "a paxel should put a campfire out");
        helper.succeed();
    }

    /** Placing a block and right-clicking it with an iron paxel in hand. */
    private static void rightClick(GameTestHelper helper, BlockState placed) {
        helper.setBlock(SUBJECT, placed);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, paxel(PaxelMaterial.IRON));
        helper.useBlock(SUBJECT, player);
    }

    private static ItemStack paxel(PaxelMaterial material) {
        return new ItemStack(TriviumItems.PAXELS.get(material).get());
    }

    private static void check(boolean condition, String expectation) {
        if (!condition) {
            throw new GameTestAssertException(expectation);
        }
    }
}
