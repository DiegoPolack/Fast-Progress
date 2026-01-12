package com.fastprogress;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public final class FastProgressCommon {
    public static final String MOD_ID = "fastprogress";

    private FastProgressCommon() {
    }

    public static void init() {
        FastProgressConfig.load();
    }

    public static void onBlockBroken(ServerPlayer player, ServerLevel level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        FastProgressConfig config = FastProgressConfig.get();
        PlacedBlocksState placedState = PlacedBlocksState.get(level);
        boolean wasPlaced = placedState.isPlaced(pos);

        if (config.blockDropMultiplier <= 1) {
            if (wasPlaced) {
                placedState.unmark(pos);
            }
            return;
        }

        if (player.isCreative()) {
            if (wasPlaced) {
                placedState.unmark(pos);
            }
            return;
        }

        if (wasPlaced && !config.affectEntityAndPlayerPlacedBlocks) {
            placedState.unmark(pos);
            return;
        }

        if (!config.affectStructureBlocks && StructureUtil.isInsideAnyStructure(level, pos)) {
            if (wasPlaced) {
                placedState.unmark(pos);
            }
            return;
        }

        if (!config.isBlockAffected(state)) {
            if (wasPlaced) {
                placedState.unmark(pos);
            }
            return;
        }

        ItemStack tool = player.getMainHandItem();
        boolean hasFortune = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool) > 0;
        boolean hasSilkTouch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0;

        if (!config.affectFortuneDrops && hasFortune) {
            if (wasPlaced) {
                placedState.unmark(pos);
            }
            return;
        }

        if (!config.affectSilkTouchDrops && hasSilkTouch) {
            if (wasPlaced) {
                placedState.unmark(pos);
            }
            return;
        }

        List<ItemStack> drops = Block.getDrops(state, level, pos, blockEntity, player, tool);
        if (!drops.isEmpty()) {
            for (int i = 1; i < config.blockDropMultiplier; i++) {
                for (ItemStack drop : drops) {
                    Block.popResource(level, pos, drop.copy());
                }
            }
        }

        if (wasPlaced) {
            placedState.unmark(pos);
        }
    }
}
