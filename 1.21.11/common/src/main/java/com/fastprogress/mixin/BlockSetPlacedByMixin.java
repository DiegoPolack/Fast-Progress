package com.fastprogress.mixin;

import com.fastprogress.PlacedBlocksState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockSetPlacedByMixin {
    @Inject(method = "setPlacedBy", at = @At("TAIL"))
    private void fastprogress$markPlaced(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack, CallbackInfo ci) {
        if (level.isClientSide() || placer == null) {
            return;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        PlacedBlocksState.get(serverLevel).markPlaced(pos);
    }
}
