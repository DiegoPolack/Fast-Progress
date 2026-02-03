package com.fastprogress;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Method;
import java.util.List;

public final class FastProgressConfigScreen {
    private FastProgressConfigScreen() {
    }

    public static Screen create(Screen parent) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.hasSingleplayerServer()) {
            return createLocal(parent, null);
        }

        if (!FastProgressClientSync.isServerSyncSupported()) {
            return createLocal(parent, Component.literal("Server does not support config sync. Editing local config only."));
        }

        FastProgressConfig serverConfig = FastProgressClientSync.getServerConfig();
        if (serverConfig == null) {
            FastProgressClientSync.requestServerConfig();
            return new FastProgressLoadingScreen(parent);
        }

        return createServer(parent, serverConfig);
    }

    static Screen createServer(Screen parent, FastProgressConfig serverConfig) {
        boolean canEdit = FastProgressClientSync.canEditServerConfig();
        Component notice = canEdit
                ? Component.literal("Editing server config.")
                : Component.literal("No permission to edit server config.");
        return buildScreen(parent, serverConfig, notice, true, canEdit);
    }

    private static Screen createLocal(Screen parent, Component notice) {
        return buildScreen(parent, FastProgressConfig.get(), notice, false, true);
    }

    private static Screen buildScreen(Screen parent, FastProgressConfig baseConfig, Component notice, boolean serverMode, boolean allowSave) {
        FastProgressConfig working = baseConfig.copy();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("FastProgress"));

        ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        if (notice != null) {
            general.addEntry(entryBuilder.startTextDescription(notice).build());
        }

        general.addEntry(entryBuilder.startIntField(Component.literal("Block Drop Multiplier"), working.blockDropMultiplier)
                .setDefaultValue(FastProgressConfig.DEFAULT_MULTIPLIER)
                .setMin(1)
                .setMax(FastProgressConfig.MAX_MULTIPLIER)
                .setSaveConsumer(value -> working.blockDropMultiplier = value)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Affect Structure Blocks"), working.affectStructureBlocks)
                .setDefaultValue(FastProgressConfig.DEFAULT_AFFECT_STRUCTURES)
                .setSaveConsumer(value -> working.affectStructureBlocks = value)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Affect Player/Entity Placed Blocks"), working.affectEntityAndPlayerPlacedBlocks)
                .setDefaultValue(FastProgressConfig.DEFAULT_AFFECT_PLACED)
                .setSaveConsumer(value -> working.affectEntityAndPlayerPlacedBlocks = value)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Affect Fortune Drops"), working.affectFortuneDrops)
                .setDefaultValue(FastProgressConfig.DEFAULT_AFFECT_FORTUNE)
                .setSaveConsumer(value -> working.affectFortuneDrops = value)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Affect Silk Touch Drops"), working.affectSilkTouchDrops)
                .setDefaultValue(FastProgressConfig.DEFAULT_AFFECT_SILK_TOUCH)
                .setSaveConsumer(value -> working.affectSilkTouchDrops = value)
                .build());

        general.addEntry(entryBuilder.startEnumSelector(Component.literal("Filter Mode"), FastProgressConfig.FilterMode.class, working.filterMode)
                .setDefaultValue(FastProgressConfig.DEFAULT_FILTER_MODE)
                .setEnumNameProvider(mode -> Component.literal(((FastProgressConfig.FilterMode) mode).getLabel()))
                .setSaveConsumer(value -> working.filterMode = value)
                .build());

        List<String> entries = working.toEntryList();
        general.addEntry(entryBuilder.startStrList(Component.literal("Blocks / Tags"), entries)
                .setSaveConsumer(working::setEntries)
                .build());

        builder.setSavingRunnable(() -> {
            working.sanitize();
            if (serverMode) {
                if (!allowSave) {
                    FastProgressClientSync.handleUpdateResult(false, "No permission to update server config.");
                    return;
                }
                FastProgressClientSync.sendUpdate(working);
            } else {
                FastProgressConfig.INSTANCE.applyFrom(working);
                FastProgressConfig.INSTANCE.save();
            }
        });
        if (serverMode) {
            builder.setAfterInitConsumer(screen -> {
                addRefreshButton(screen, parent);
            });
        }

        Screen screen = builder.build();
        if (serverMode) {
            FastProgressClientSync.registerServerConfigScreen(screen, parent);
        }
        return screen;
    }

    private static void addRefreshButton(Screen screen, Screen parent) {
        Button refreshButton = Button.builder(Component.literal("Refresh from Server"), button -> {
            FastProgressClientSync.requestServerConfigWithLoading(parent);
        }).bounds(screen.width - 154, 6, 150, 20).build();
        try {
            Method addRenderableWidget = Screen.class.getDeclaredMethod("addRenderableWidget", GuiEventListener.class);
            addRenderableWidget.setAccessible(true);
            addRenderableWidget.invoke(screen, refreshButton);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
