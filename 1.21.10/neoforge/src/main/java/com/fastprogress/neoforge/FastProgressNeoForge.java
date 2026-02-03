package com.fastprogress.neoforge;

import com.fastprogress.FastProgressCommon;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@Mod(FastProgressCommon.MOD_ID)
public class FastProgressNeoForge {
    public FastProgressNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        FastProgressCommon.init();
        NeoForge.EVENT_BUS.addListener(this::onBlockBreak);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            FastProgressNeoForgeClient.init(modContainer);
        }
    }

    private void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        FastProgressCommon.onBlockBroken(player, level, event.getPos(), event.getState(), level.getBlockEntity(event.getPos()));
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        com.fastprogress.FastProgressCommands.register(event.getDispatcher());
    }
}
