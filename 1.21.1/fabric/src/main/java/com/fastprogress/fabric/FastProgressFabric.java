package com.fastprogress.fabric;

import com.fastprogress.FastProgressCommon;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class FastProgressFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FastProgressCommon.init();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                com.fastprogress.FastProgressCommands.register(dispatcher));
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (level.isClientSide()) {
                return;
            }
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return;
            }
            if (!(level instanceof ServerLevel serverLevel)) {
                return;
            }
            FastProgressCommon.onBlockBroken(serverPlayer, serverLevel, pos, state, blockEntity);
        });
    }
}
