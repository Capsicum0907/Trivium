package io.github.capsicum0907.trivium;

import java.util.List;

import io.github.capsicum0907.trivium.data.TestStructures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
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
    public static void minesEveryFamily(GameTestHelper helper) {
        ItemStack paxel = paxel(PaxelMaterial.IRON);
        check(paxel.isCorrectToolForDrops(Blocks.STONE.defaultBlockState()),
                "an iron paxel should mine stone, which is the pickaxe family");
        check(paxel.isCorrectToolForDrops(Blocks.OAK_LOG.defaultBlockState()),
                "an iron paxel should mine oak log, which is the axe family");
        check(paxel.isCorrectToolForDrops(Blocks.DIRT.defaultBlockState()),
                "an iron paxel should mine dirt, which is the shovel family");
        check(paxel.isCorrectToolForDrops(Blocks.HAY_BLOCK.defaultBlockState()),
                "an iron paxel should mine a hay block, which is the hoe family");
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
            int expected = material.tier().getUses() * PaxelFamily.crafted().size();
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
        for (Block block : new Block[] { Blocks.STONE, Blocks.OAK_LOG, Blocks.DIRT, Blocks.SCULK }) {
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
    public static void tillsFarmland(GameTestHelper helper) {
        rightClick(helper, Blocks.GRASS_BLOCK.defaultBlockState());
        helper.assertBlockPresent(Blocks.FARMLAND, SUBJECT);
        helper.succeed();
    }

    /**
     * The half of the tilling decision that is easy to lose. Tilling and flattening
     * answer the same click on the same blocks, so flattening was dropped rather than
     * ordered behind — and dropped at the ability, which is what the hook consults
     * before it will do anything at all.
     *
     * <p>Podzol is the case that says the two are not simply ordered: it flattens and
     * does not till, so an item that merely preferred tilling would still turn it into
     * a path. This one leaves it alone.
     */
    @GameTest(template = TestStructures.PLATFORM)
    public static void neverFlattens(GameTestHelper helper) {
        check(!paxel(PaxelMaterial.IRON).canPerformAction(ItemAbilities.SHOVEL_FLATTEN),
                "a paxel must not claim it can flatten, or the hook will let it");
        rightClick(helper, Blocks.PODZOL.defaultBlockState());
        helper.assertBlockPresent(Blocks.PODZOL, SUBJECT);
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

    /**
     * Which enchantments a paxel accepts, and which it refuses.
     *
     * <p>This is the failure that says nothing: an item in none of the
     * {@code enchantable/*} tags is simply declined by the anvil and never offered by
     * the table, with no error anywhere. It went unnoticed until it was looked for.
     *
     * <p>The refusals matter as much as the acceptances. Joining
     * {@code #minecraft:pickaxes}, {@code #axes} and {@code #shovels} is a wide claim,
     * and the way to show it is not too wide is that the sword's and the armour's
     * enchantments still do not stick.
     */
    @GameTest(template = TestStructures.PLATFORM)
    public static void takesTheEnchantmentsOfTheToolsItReplaces(GameTestHelper helper) {
        record Case(ResourceKey<Enchantment> enchantment, boolean expected, String why) {
        }
        List<Case> cases = List.of(
                new Case(Enchantments.EFFICIENCY, true, "it mines"),
                new Case(Enchantments.SILK_TOUCH, true, "it mines for drops"),
                new Case(Enchantments.FORTUNE, true, "it mines for drops"),
                new Case(Enchantments.UNBREAKING, true, "it wears out"),
                new Case(Enchantments.MENDING, true, "it wears out"),
                new Case(Enchantments.SHARPNESS, true, "it hits with the axe's weight"),
                new Case(Enchantments.SWEEPING_EDGE, false, "it is not a sword"),
                new Case(Enchantments.PROTECTION, false, "it is not worn"));

        Registry<Enchantment> registry = helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        ItemStack paxel = paxel(PaxelMaterial.DIAMOND);
        for (Case testCase : cases) {
            Holder<Enchantment> enchantment = registry.getHolderOrThrow(testCase.enchantment());
            boolean supported = paxel.supportsEnchantment(enchantment);
            check(supported == testCase.expected(),
                    "a diamond paxel should " + (testCase.expected() ? "" : "not ")
                            + "take " + testCase.enchantment().location().getPath()
                            + ", because " + testCase.why());
        }
        helper.succeed();
    }

    /**
     * The other half of the gate. An enchantment names two sets of items: what may
     * hold it at all, and what the table will offer it on. They are separate fields
     * and fail separately, so an item can be enchantable by book and still be a blank
     * at the table.
     */
    @GameTest(template = TestStructures.PLATFORM)
    public static void theTableOffersEnchantments(GameTestHelper helper) {
        Registry<Enchantment> registry = helper.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        ItemStack paxel = paxel(PaxelMaterial.DIAMOND);
        List<EnchantmentInstance> offered = EnchantmentHelper.getAvailableEnchantmentResults(
                30, paxel, registry.holders().map(holder -> (Holder<Enchantment>) holder));

        check(!offered.isEmpty(), "an enchanting table at level 30 should offer a diamond paxel something");
        check(offered.stream().anyMatch(instance -> instance.enchantment.is(Enchantments.EFFICIENCY)),
                "efficiency should be among what the table offers a diamond paxel");
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
