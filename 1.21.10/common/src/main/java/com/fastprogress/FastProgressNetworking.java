package com.fastprogress;

import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public final class FastProgressNetworking {
    private static boolean INITIALIZED;

    public static final CustomPacketPayload.Type<RequestConfigPayload> REQUEST_CONFIG =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(FastProgressCommon.MOD_ID, "request_config"));
    public static final CustomPacketPayload.Type<SyncConfigPayload> SYNC_CONFIG =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(FastProgressCommon.MOD_ID, "sync_config"));
    public static final CustomPacketPayload.Type<UpdateConfigPayload> UPDATE_CONFIG =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(FastProgressCommon.MOD_ID, "update_config"));
    public static final CustomPacketPayload.Type<UpdateResultPayload> UPDATE_RESULT =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(FastProgressCommon.MOD_ID, "update_result"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestConfigPayload> REQUEST_CONFIG_CODEC =
            StreamCodec.of((buf, payload) -> {
            }, buf -> new RequestConfigPayload());
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncConfigPayload> SYNC_CONFIG_CODEC =
            StreamCodec.of(FastProgressNetworking::writeSyncConfig, FastProgressNetworking::readSyncConfig);
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateConfigPayload> UPDATE_CONFIG_CODEC =
            StreamCodec.of(FastProgressNetworking::writeUpdateConfig, FastProgressNetworking::readUpdateConfig);
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateResultPayload> UPDATE_RESULT_CODEC =
            StreamCodec.of(FastProgressNetworking::writeUpdateResult, FastProgressNetworking::readUpdateResult);

    private FastProgressNetworking() {
    }

    public static void init() {
        if (INITIALIZED) {
            return;
        }
        INITIALIZED = true;
        NetworkManager.registerReceiver(NetworkManager.c2s(), REQUEST_CONFIG, REQUEST_CONFIG_CODEC, FastProgressNetworking::handleRequestConfig);
        NetworkManager.registerReceiver(NetworkManager.c2s(), UPDATE_CONFIG, UPDATE_CONFIG_CODEC, FastProgressNetworking::handleUpdateConfig);
        NetworkManager.registerReceiver(NetworkManager.s2c(), SYNC_CONFIG, SYNC_CONFIG_CODEC, FastProgressNetworking::handleSyncConfig);
        NetworkManager.registerReceiver(NetworkManager.s2c(), UPDATE_RESULT, UPDATE_RESULT_CODEC, FastProgressNetworking::handleUpdateResult);
    }

    public static void broadcastServerConfig(MinecraftServer server) {
        if (server == null) {
            return;
        }
        ConfigData data = ConfigData.fromConfig(FastProgressConfig.get());
        SyncConfigPayload payload = new SyncConfigPayload(data);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (NetworkManager.canPlayerReceive(player, SYNC_CONFIG)) {
                NetworkManager.sendToPlayer(player, payload);
            }
        }
    }

    public record RequestConfigPayload() implements CustomPacketPayload {
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return REQUEST_CONFIG;
        }
    }

    public record SyncConfigPayload(ConfigData data) implements CustomPacketPayload {
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return SYNC_CONFIG;
        }
    }

    public record UpdateConfigPayload(ConfigData data) implements CustomPacketPayload {
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return UPDATE_CONFIG;
        }
    }

    public record UpdateResultPayload(boolean success, String message) implements CustomPacketPayload {
        @Override
        public Type<? extends CustomPacketPayload> type() {
            return UPDATE_RESULT;
        }
    }

    public static final class ConfigData {
        public final int multiplier;
        public final boolean affectStructureBlocks;
        public final boolean affectPlacedBlocks;
        public final boolean affectFortuneDrops;
        public final boolean affectSilkTouchDrops;
        public final int filterMode;
        public final List<ResourceLocation> blockIds;
        public final List<ResourceLocation> tagIds;

        public ConfigData(int multiplier,
                          boolean affectStructureBlocks,
                          boolean affectPlacedBlocks,
                          boolean affectFortuneDrops,
                          boolean affectSilkTouchDrops,
                          int filterMode,
                          List<ResourceLocation> blockIds,
                          List<ResourceLocation> tagIds) {
            this.multiplier = multiplier;
            this.affectStructureBlocks = affectStructureBlocks;
            this.affectPlacedBlocks = affectPlacedBlocks;
            this.affectFortuneDrops = affectFortuneDrops;
            this.affectSilkTouchDrops = affectSilkTouchDrops;
            this.filterMode = filterMode;
            this.blockIds = blockIds;
            this.tagIds = tagIds;
        }

        public static ConfigData fromConfig(FastProgressConfig config) {
            return new ConfigData(
                    config.blockDropMultiplier,
                    config.affectStructureBlocks,
                    config.affectEntityAndPlayerPlacedBlocks,
                    config.affectFortuneDrops,
                    config.affectSilkTouchDrops,
                    config.filterMode.getId(),
                    new ArrayList<>(config.blockIds),
                    new ArrayList<>(config.tagIds)
            );
        }

        public FastProgressConfig toConfig() {
            FastProgressConfig config = new FastProgressConfig();
            applyTo(config);
            return config;
        }

        public void applyTo(FastProgressConfig config) {
            config.blockDropMultiplier = multiplier;
            config.affectStructureBlocks = affectStructureBlocks;
            config.affectEntityAndPlayerPlacedBlocks = affectPlacedBlocks;
            config.affectFortuneDrops = affectFortuneDrops;
            config.affectSilkTouchDrops = affectSilkTouchDrops;
            config.filterMode = FastProgressConfig.FilterMode.fromId(filterMode);
            config.blockIds.clear();
            config.tagIds.clear();
            config.blockIds.addAll(blockIds);
            config.tagIds.addAll(tagIds);
            config.sanitize();
        }
    }

    private static void writeConfig(RegistryFriendlyByteBuf buf, ConfigData data) {
        buf.writeVarInt(data.multiplier);
        buf.writeBoolean(data.affectStructureBlocks);
        buf.writeBoolean(data.affectPlacedBlocks);
        buf.writeBoolean(data.affectFortuneDrops);
        buf.writeBoolean(data.affectSilkTouchDrops);
        buf.writeVarInt(data.filterMode);
        writeIdList(buf, data.blockIds);
        writeIdList(buf, data.tagIds);
    }

    private static ConfigData readConfig(RegistryFriendlyByteBuf buf) {
        int multiplier = buf.readVarInt();
        boolean affectStructureBlocks = buf.readBoolean();
        boolean affectPlacedBlocks = buf.readBoolean();
        boolean affectFortuneDrops = buf.readBoolean();
        boolean affectSilkTouchDrops = buf.readBoolean();
        int filterMode = buf.readVarInt();
        List<ResourceLocation> blockIds = readIdList(buf);
        List<ResourceLocation> tagIds = readIdList(buf);
        return new ConfigData(
                multiplier,
                affectStructureBlocks,
                affectPlacedBlocks,
                affectFortuneDrops,
                affectSilkTouchDrops,
                filterMode,
                blockIds,
                tagIds
        );
    }

    private static void writeIdList(RegistryFriendlyByteBuf buf, List<ResourceLocation> ids) {
        buf.writeVarInt(ids.size());
        for (ResourceLocation id : ids) {
            buf.writeUtf(id.toString());
        }
    }

    private static List<ResourceLocation> readIdList(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<ResourceLocation> ids = new ArrayList<>(Math.max(0, size));
        for (int i = 0; i < size; i++) {
            ResourceLocation id = ResourceLocation.tryParse(buf.readUtf());
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }

    private static void handleRequestConfig(RequestConfigPayload payload, NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (!(context.getPlayer() instanceof ServerPlayer player)) {
                return;
            }
            ConfigData data = ConfigData.fromConfig(FastProgressConfig.get());
            NetworkManager.sendToPlayer(player, new SyncConfigPayload(data));
        });
    }

    private static void handleSyncConfig(SyncConfigPayload payload, NetworkManager.PacketContext context) {
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> FastProgressClientSync.handleSyncConfig(payload.data()));
    }

    private static void handleUpdateConfig(UpdateConfigPayload payload, NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (!(context.getPlayer() instanceof ServerPlayer player)) {
                return;
            }
            if (!player.hasPermissions(2)) {
                NetworkManager.sendToPlayer(player, new UpdateResultPayload(false, "No permission to update server config."));
                return;
            }
            FastProgressConfig config = FastProgressConfig.get();
            payload.data().applyTo(config);
            config.save();
            NetworkManager.sendToPlayer(player, new UpdateResultPayload(true, "Server config updated."));
            if (player.level() instanceof ServerLevel level) {
                broadcastServerConfig(level.getServer());
            }
        });
    }

    private static void handleUpdateResult(UpdateResultPayload payload, NetworkManager.PacketContext context) {
        EnvExecutor.runInEnv(Env.CLIENT, () -> () -> FastProgressClientSync.handleUpdateResult(payload.success(), payload.message()));
    }

    private static void writeSyncConfig(RegistryFriendlyByteBuf buf, SyncConfigPayload payload) {
        writeConfig(buf, payload.data());
    }

    private static SyncConfigPayload readSyncConfig(RegistryFriendlyByteBuf buf) {
        return new SyncConfigPayload(readConfig(buf));
    }

    private static void writeUpdateConfig(RegistryFriendlyByteBuf buf, UpdateConfigPayload payload) {
        writeConfig(buf, payload.data());
    }

    private static UpdateConfigPayload readUpdateConfig(RegistryFriendlyByteBuf buf) {
        return new UpdateConfigPayload(readConfig(buf));
    }

    private static void writeUpdateResult(RegistryFriendlyByteBuf buf, UpdateResultPayload payload) {
        buf.writeBoolean(payload.success());
        buf.writeUtf(payload.message() == null ? "" : payload.message());
    }

    private static UpdateResultPayload readUpdateResult(RegistryFriendlyByteBuf buf) {
        boolean success = buf.readBoolean();
        String message = buf.readUtf();
        return new UpdateResultPayload(success, message);
    }
}
