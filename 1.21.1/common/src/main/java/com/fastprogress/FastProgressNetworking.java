package com.fastprogress;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.architectury.networking.simple.SimpleNetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public final class FastProgressNetworking {
    private static final SimpleNetworkManager CHANNEL = SimpleNetworkManager.create(FastProgressCommon.MOD_ID);
    private static boolean INITIALIZED;

    public static final MessageType REQUEST_CONFIG = CHANNEL.registerC2S("request_config", RequestConfigMessage::new);
    public static final MessageType SYNC_CONFIG = CHANNEL.registerS2C("sync_config", SyncConfigMessage::new);
    public static final MessageType UPDATE_CONFIG = CHANNEL.registerC2S("update_config", UpdateConfigMessage::new);
    public static final MessageType UPDATE_RESULT = CHANNEL.registerS2C("update_result", UpdateResultMessage::new);

    private FastProgressNetworking() {
    }

    public static void init() {
        if (INITIALIZED) {
            return;
        }
        INITIALIZED = true;
        if (Platform.getEnvironment() == Env.SERVER) {
            NetworkManager.registerS2CPayloadType(SYNC_CONFIG.getId());
            NetworkManager.registerS2CPayloadType(UPDATE_RESULT.getId());
        }
    }

    public static void broadcastServerConfig(MinecraftServer server) {
        if (server == null) {
            return;
        }
        ConfigData data = ConfigData.fromConfig(FastProgressConfig.get());
        SyncConfigMessage message = new SyncConfigMessage(data);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (NetworkManager.canPlayerReceive(player, SYNC_CONFIG.getId())) {
                message.sendTo(player);
            }
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

    public static final class RequestConfigMessage extends BaseC2SMessage {
        public RequestConfigMessage() {
        }

        public RequestConfigMessage(RegistryFriendlyByteBuf buf) {
        }

        @Override
        public MessageType getType() {
            return REQUEST_CONFIG;
        }

        @Override
        public void write(RegistryFriendlyByteBuf buf) {
        }

        @Override
        public void handle(NetworkManager.PacketContext context) {
            if (!(context.getPlayer() instanceof ServerPlayer player)) {
                return;
            }
            ConfigData data = ConfigData.fromConfig(FastProgressConfig.get());
            new SyncConfigMessage(data).sendTo(player);
        }
    }

    public static final class SyncConfigMessage extends BaseS2CMessage {
        private final ConfigData data;

        public SyncConfigMessage(ConfigData data) {
            this.data = data;
        }

        public SyncConfigMessage(RegistryFriendlyByteBuf buf) {
            this.data = readConfig(buf);
        }

        @Override
        public MessageType getType() {
            return SYNC_CONFIG;
        }

        @Override
        public void write(RegistryFriendlyByteBuf buf) {
            writeConfig(buf, data);
        }

        @Override
        public void handle(NetworkManager.PacketContext context) {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> FastProgressClientSync.handleSyncConfig(data));
        }
    }

    public static final class UpdateConfigMessage extends BaseC2SMessage {
        private final ConfigData data;

        public UpdateConfigMessage(ConfigData data) {
            this.data = data;
        }

        public UpdateConfigMessage(RegistryFriendlyByteBuf buf) {
            this.data = readConfig(buf);
        }

        @Override
        public MessageType getType() {
            return UPDATE_CONFIG;
        }

        @Override
        public void write(RegistryFriendlyByteBuf buf) {
            writeConfig(buf, data);
        }

        @Override
        public void handle(NetworkManager.PacketContext context) {
            if (!(context.getPlayer() instanceof ServerPlayer player)) {
                return;
            }
            if (!player.hasPermissions(2)) {
                new UpdateResultMessage(false, "No permission to update server config.").sendTo(player);
                return;
            }
            FastProgressConfig config = FastProgressConfig.get();
            data.applyTo(config);
            config.save();
            new UpdateResultMessage(true, "Server config updated.").sendTo(player);
            broadcastServerConfig(player.getServer());
        }
    }

    public static final class UpdateResultMessage extends BaseS2CMessage {
        private final boolean success;
        private final String message;

        public UpdateResultMessage(boolean success, String message) {
            this.success = success;
            this.message = message == null ? "" : message;
        }

        public UpdateResultMessage(RegistryFriendlyByteBuf buf) {
            this.success = buf.readBoolean();
            this.message = buf.readUtf();
        }

        @Override
        public MessageType getType() {
            return UPDATE_RESULT;
        }

        @Override
        public void write(RegistryFriendlyByteBuf buf) {
            buf.writeBoolean(success);
            buf.writeUtf(message);
        }

        @Override
        public void handle(NetworkManager.PacketContext context) {
            EnvExecutor.runInEnv(Env.CLIENT, () -> () -> FastProgressClientSync.handleUpdateResult(success, message));
        }
    }
}
