package com.fastprogress.forge;

import com.fastprogress.FastProgressCommon;
import dev.architectury.platform.forge.EventBuses;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(FastProgressCommon.MOD_ID)
public class FastProgressForge {
    public FastProgressForge() {
        EventBuses.registerModEventBus(FastProgressCommon.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        FastProgressCommon.init();
        MinecraftForge.EVENT_BUS.addListener(this::onBlockBreak);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> FastProgressForgeClient::init);
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
}
