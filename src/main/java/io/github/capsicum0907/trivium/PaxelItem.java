package io.github.capsicum0907.trivium;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

/**
 * A pickaxe, an axe and a shovel in one item.
 *
 * <p>Two properties are worth stating up front, because they are why this class is
 * as short as it is:
 *
 * <p><b>What it mines is data, not code.</b> Since 1.20.5 a tool's reach lives in
 * the {@link Tool} data component as a list of rules over block tags. A paxel is
 * therefore one component carrying three mine-rules instead of one — no dispatch,
 * and blocks that other mods put in those tags are covered for free.
 *
 * <p><b>What it does on right-click is not reimplemented.</b> Stripping, scraping,
 * wax removal, flattening and dousing all resolve through
 * {@link BlockState#getToolModifiedState}, which is the hook the game itself calls
 * and the one other mods override. This class only decides the order to try them
 * in and how to announce the result.
 *
 * <p>Extends {@link TieredItem} rather than {@code DiggerItem} because that class
 * writes a single-tag {@link Tool} component over whatever it is handed, which is
 * exactly the thing a paxel has to replace.
 */
public class PaxelItem extends TieredItem {
    /** The tags a paxel mines. Order is irrelevant here; overlap between them is harmless. */
    public static final List<TagKey<Block>> MINEABLE = List.of(
            BlockTags.MINEABLE_WITH_PICKAXE,
            BlockTags.MINEABLE_WITH_AXE,
            BlockTags.MINEABLE_WITH_SHOVEL);

    /**
     * Announced abilities: the union of what the three tools each claim. This is the
     * gate other code consults ("can this item strip a log?"); it does not perform
     * anything on its own.
     */
    private static final Set<ItemAbility> ABILITIES = union(
            ItemAbilities.DEFAULT_PICKAXE_ACTIONS,
            ItemAbilities.DEFAULT_AXE_ACTIONS,
            ItemAbilities.DEFAULT_SHOVEL_ACTIONS);

    /**
     * One right-click behaviour: which ability to ask the block for, how to announce
     * it, and when it may be tried at all.
     *
     * @param sound      played to everyone, or none when {@code null}
     * @param levelEvent a client visual effect id, or 0 for none
     */
    private record RightClick(ItemAbility ability, @Nullable SoundEvent sound, int levelEvent,
            Predicate<UseOnContext> allowed) {
    }

    /**
     * The order right-clicks are tried in. First match wins, as in the vanilla items
     * this is assembled from — the axe tries strip, then scrape, then wax off; the
     * shovel tries flatten, then douse.
     */
    private static final List<RightClick> RIGHT_CLICKS = List.of(
            new RightClick(ItemAbilities.AXE_STRIP, SoundEvents.AXE_STRIP, 0, PaxelItem::axeAllowed),
            new RightClick(ItemAbilities.AXE_SCRAPE, SoundEvents.AXE_SCRAPE, 3005, PaxelItem::axeAllowed),
            new RightClick(ItemAbilities.AXE_WAX_OFF, SoundEvents.AXE_WAX_OFF, 3004, PaxelItem::axeAllowed),
            new RightClick(ItemAbilities.SHOVEL_FLATTEN, SoundEvents.SHOVEL_FLATTEN, 0, PaxelItem::flattenAllowed),
            new RightClick(ItemAbilities.SHOVEL_DOUSE, null, 1009, PaxelItem::shovelAllowed));

    public PaxelItem(PaxelMaterial material) {
        this(new PaxelTier(material.tier()), material);
    }

    private PaxelItem(Tier tier, PaxelMaterial material) {
        super(tier, properties(tier, material));
    }

    private static Item.Properties properties(Tier tier, PaxelMaterial material) {
        Item.Properties properties = new Item.Properties()
                .attributes(net.minecraft.world.item.DiggerItem.createAttributes(
                        tier, material.attackDamage(), material.attackSpeed()))
                .component(DataComponents.TOOL, toolFor(tier));
        return material.fireResistant() ? properties.fireResistant() : properties;
    }

    /**
     * The tier's deny-rule must come first. {@link Tool#isCorrectForDrops} answers with
     * the first matching rule that has an opinion, so a mine-rule placed ahead of it
     * would let a wooden paxel drop obsidian.
     */
    private static Tool toolFor(Tier tier) {
        List<Tool.Rule> rules = new ArrayList<>();
        rules.add(Tool.Rule.deniesDrops(tier.getIncorrectBlocksForDrops()));
        for (TagKey<Block> tag : MINEABLE) {
            rules.add(Tool.Rule.minesAndDrops(tag, tier.getSpeed()));
        }
        return new Tool(rules, 1.0F, 1);
    }

    @SafeVarargs
    private static Set<ItemAbility> union(Set<ItemAbility>... sets) {
        Set<ItemAbility> all = new HashSet<>();
        for (Set<ItemAbility> set : sets) {
            all.addAll(set);
        }
        return Set.copyOf(all);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return ABILITIES.contains(itemAbility);
    }

    // Both copied from DiggerItem, which this class cannot extend: hitting something
    // with a tool costs two durability rather than the one a block costs.
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return true;
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(2, attacker, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        RightClick match = null;
        BlockState modified = null;
        for (RightClick candidate : RIGHT_CLICKS) {
            if (!candidate.allowed().test(context)) {
                continue;
            }
            // simulate = false on both sides, as the vanilla items do. Asking once
            // matters: the call fires BlockToolModificationEvent, and a listener that
            // ignores the simulate flag would run twice for one click.
            BlockState result = state.getToolModifiedState(context, candidate.ability(), false);
            if (result != null) {
                match = candidate;
                modified = result;
                break;
            }
        }

        if (match == null) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            apply(context, level, pos, match, modified);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private void apply(UseOnContext context, Level level, BlockPos pos, RightClick match, BlockState modified) {
        Player player = context.getPlayer();
        if (match.sound() != null) {
            // null as the source excludes nobody: the effect only runs server side, so
            // the player who clicked has not predicted it and must be told as well.
            level.playSound(null, pos, match.sound(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        if (match.levelEvent() != 0) {
            level.levelEvent(null, match.levelEvent(), pos, 0);
        }

        level.setBlock(pos, modified, Block.UPDATE_ALL_IMMEDIATE);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, modified));

        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, context.getItemInHand());
        }
        if (player != null) {
            context.getItemInHand().hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
        }
    }

    /**
     * An axe passes when the off hand holds a shield and the player is not sneaking,
     * so that raising the shield does not strip the log being looked at.
     */
    private static boolean axeAllowed(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return true;
        }
        boolean shieldIntent = context.getHand() == InteractionHand.MAIN_HAND
                && player.getOffhandItem().is(Items.SHIELD)
                && !player.isSecondaryUseActive();
        return !shieldIntent;
    }

    /** A shovel does nothing to the underside of a block: the path would face into it. */
    private static boolean shovelAllowed(UseOnContext context) {
        return context.getClickedFace() != Direction.DOWN;
    }

    /** A path also needs room above it, or the block placed there would be buried. */
    private static boolean flattenAllowed(UseOnContext context) {
        return shovelAllowed(context)
                && context.getLevel().getBlockState(context.getClickedPos().above()).isAir();
    }
}
