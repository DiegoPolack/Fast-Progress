package com.fastprogress;

import dev.architectury.networking.NetworkManager;
import me.shedaniel.clothconfig2.gui.AbstractConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class FastProgressClientSync {
    private static FastProgressConfig serverConfig;
    private static boolean waitingForConfig;
    private static Screen lastConfigScreen;
    private static Screen lastParent;
    private static boolean pendingServerRefresh;

    private FastProgressClientSync() {
    }

    public static boolean isMultiplayer() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level != null && !mc.hasSingleplayerServer();
    }

    public static boolean isServerSyncSupported() {
        return NetworkManager.canServerReceive(FastProgressNetworking.REQUEST_CONFIG);
    }

    public static FastProgressConfig getServerConfig() {
        return serverConfig == null ? null : serverConfig.copy();
    }

    public static void registerServerConfigScreen(Screen screen, Screen parent) {
        lastConfigScreen = screen;
        lastParent = parent;
        pendingServerRefresh = false;
    }

    public static boolean canEditServerConfig() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && mc.player.hasPermissions(2);
    }

    public static void requestServerConfig() {
        if (waitingForConfig) {
            return;
        }
        waitingForConfig = true;
        NetworkManager.sendToServer(new FastProgressNetworking.RequestConfigPayload());
    }

    public static void handleSyncConfig(FastProgressNetworking.ConfigData data) {
        waitingForConfig = false;
        serverConfig = data.toConfig();
        Minecraft mc = Minecraft.getInstance();
        Screen current = mc.screen;
        if (current instanceof FastProgressLoadingScreen loading) {
            mc.setScreen(FastProgressConfigScreen.createServer(loading.getParent(), serverConfig));
            return;
        }
        if (current == lastConfigScreen && current instanceof AbstractConfigScreen configScreen) {
            if (configScreen.isEdited()) {
                if (!pendingServerRefresh) {
                    pendingServerRefresh = true;
                    showClientMessage("Server config changed. Press Refresh to update.");
                }
                return;
            }
            if (lastParent != null) {
                mc.setScreen(FastProgressConfigScreen.createServer(lastParent, serverConfig));
            }
        }
    }

    public static void sendUpdate(FastProgressConfig config) {
        NetworkManager.sendToServer(new FastProgressNetworking.UpdateConfigPayload(FastProgressNetworking.ConfigData.fromConfig(config)));
    }

    public static void handleUpdateResult(boolean success, String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && message != null && !message.isBlank()) {
            mc.player.displayClientMessage(Component.literal(message), false);
        }
    }

    public static void requestServerConfigWithLoading(Screen parent) {
        requestServerConfig();
        Minecraft.getInstance().setScreen(new FastProgressLoadingScreen(parent));
    }

    private static void showClientMessage(String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && message != null && !message.isBlank()) {
            mc.player.displayClientMessage(Component.literal(message), false);
        }
    }
}
