package com.fastprogress;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class FastProgressConfigScreen {
    private FastProgressConfigScreen() {
    }

    public static Screen create(Screen parent) {
        FastProgressConfig current = FastProgressConfig.get();
        FastProgressConfig working = current.copy();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("FastProgress"));

        ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

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
            FastProgressConfig.INSTANCE.applyFrom(working);
            FastProgressConfig.INSTANCE.save();
        });

        return builder.build();
    }
}
