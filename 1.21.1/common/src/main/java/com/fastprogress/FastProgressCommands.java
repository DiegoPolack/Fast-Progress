package com.fastprogress;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Locale;
import java.util.stream.Collectors;

public final class FastProgressCommands {
    private FastProgressCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("fastprogress")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("show").executes(ctx -> show(ctx.getSource())))
                .then(Commands.literal("reload").executes(ctx -> reload(ctx.getSource())))
                .then(Commands.literal("multiplier")
                        .then(Commands.argument("value", IntegerArgumentType.integer(1, FastProgressConfig.MAX_MULTIPLIER))
                                .executes(ctx -> setMultiplier(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "value")))))
                .then(Commands.literal("affectStructureBlocks")
                        .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(ctx -> setAffectStructure(ctx.getSource(), BoolArgumentType.getBool(ctx, "value")))))
                .then(Commands.literal("affectPlacedBlocks")
                        .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(ctx -> setAffectPlaced(ctx.getSource(), BoolArgumentType.getBool(ctx, "value")))))
                .then(Commands.literal("affectFortuneDrops")
                        .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(ctx -> setAffectFortune(ctx.getSource(), BoolArgumentType.getBool(ctx, "value")))))
                .then(Commands.literal("affectSilkTouchDrops")
                        .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(ctx -> setAffectSilk(ctx.getSource(), BoolArgumentType.getBool(ctx, "value")))))
                .then(Commands.literal("filter")
                        .then(Commands.argument("value", StringArgumentType.word())
                                .suggests((ctx, builder) -> suggestFilter(builder))
                                .executes(ctx -> setFilter(ctx.getSource(), StringArgumentType.getString(ctx, "value")))))
                .then(Commands.literal("block")
                        .then(Commands.literal("add")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(ctx -> blockAdd(ctx.getSource(), StringArgumentType.getString(ctx, "id")))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("id", StringArgumentType.word())
                                        .executes(ctx -> blockRemove(ctx.getSource(), StringArgumentType.getString(ctx, "id")))))
                        .then(Commands.literal("list").executes(ctx -> blockList(ctx.getSource()))));

        var rootNode = dispatcher.register(root);
        dispatcher.register(Commands.literal("fp")
                .requires(source -> source.hasPermission(2))
                .redirect(rootNode));
    }

    private static int show(CommandSourceStack source) {
        FastProgressConfig config = FastProgressConfig.get();
        config.sanitize();
        source.sendSuccess(() -> Component.literal("FastProgress config:"), false);
        source.sendSuccess(() -> Component.literal("Multiplier: " + config.blockDropMultiplier), false);
        source.sendSuccess(() -> Component.literal("AffectStructureBlocks: " + config.affectStructureBlocks), false);
        source.sendSuccess(() -> Component.literal("AffectPlacedBlocks: " + config.affectEntityAndPlayerPlacedBlocks), false);
        source.sendSuccess(() -> Component.literal("AffectFortuneDrops: " + config.affectFortuneDrops), false);
        source.sendSuccess(() -> Component.literal("AffectSilkTouchDrops: " + config.affectSilkTouchDrops), false);
        source.sendSuccess(() -> Component.literal("Filter: " + config.filterMode.getLabel() + " (" + config.filterMode.getId() + ")"), false);
        source.sendSuccess(() -> Component.literal("Blocks: " + formatList(config.blockIds)), false);
        source.sendSuccess(() -> Component.literal("Tags: " + formatList(config.tagIds)), false);
        return 1;
    }

    private static int reload(CommandSourceStack source) {
        FastProgressConfig.load();
        source.sendSuccess(() -> Component.literal("FastProgress config reloaded."), false);
        FastProgressNetworking.broadcastServerConfig(source.getServer());
        source.sendSuccess(() -> Component.literal("Fast Progress: server config updated."), false);
        return 1;
    }

    private static int setMultiplier(CommandSourceStack source, int value) {
        FastProgressConfig config = FastProgressConfig.get();
        config.blockDropMultiplier = value;
        return saveAndNotify(source, "Multiplier set to " + config.blockDropMultiplier + ".");
    }

    private static int setAffectStructure(CommandSourceStack source, boolean value) {
        FastProgressConfig config = FastProgressConfig.get();
        config.affectStructureBlocks = value;
        return saveAndNotify(source, "AffectStructureBlocks set to " + value + ".");
    }

    private static int setAffectPlaced(CommandSourceStack source, boolean value) {
        FastProgressConfig config = FastProgressConfig.get();
        config.affectEntityAndPlayerPlacedBlocks = value;
        return saveAndNotify(source, "AffectPlacedBlocks set to " + value + ".");
    }

    private static int setAffectFortune(CommandSourceStack source, boolean value) {
        FastProgressConfig config = FastProgressConfig.get();
        config.affectFortuneDrops = value;
        return saveAndNotify(source, "AffectFortuneDrops set to " + value + ".");
    }

    private static int setAffectSilk(CommandSourceStack source, boolean value) {
        FastProgressConfig config = FastProgressConfig.get();
        config.affectSilkTouchDrops = value;
        return saveAndNotify(source, "AffectSilkTouchDrops set to " + value + ".");
    }

    private static int setFilter(CommandSourceStack source, String raw) {
        String value = raw.toLowerCase(Locale.ROOT);
        FastProgressConfig.FilterMode mode;
        if ("0".equals(value) || "blacklist".equals(value)) {
            mode = FastProgressConfig.FilterMode.BLACKLIST;
        } else if ("1".equals(value) || "whitelist".equals(value)) {
            mode = FastProgressConfig.FilterMode.WHITELIST;
        } else {
            source.sendFailure(Component.literal("Invalid filter mode. Use blacklist|whitelist or 0|1."));
            return 0;
        }
        FastProgressConfig config = FastProgressConfig.get();
        config.filterMode = mode;
        return saveAndNotify(source, "Filter set to " + mode.getLabel() + " (" + mode.getId() + ").");
    }

    private static int blockAdd(CommandSourceStack source, String rawId) {
        ResourceLocation id = ResourceLocation.tryParse(rawId);
        if (id == null) {
            source.sendFailure(Component.literal("Invalid block id: " + rawId));
            return 0;
        }
        if (!BuiltInRegistries.BLOCK.containsKey(id)) {
            source.sendFailure(Component.literal("Unknown block id: " + id));
            return 0;
        }
        FastProgressConfig config = FastProgressConfig.get();
        if (!config.blockIds.add(id)) {
            source.sendFailure(Component.literal("Block already in list: " + id));
            return 0;
        }
        return saveAndNotify(source, "Added block: " + id);
    }

    private static int blockRemove(CommandSourceStack source, String rawId) {
        ResourceLocation id = ResourceLocation.tryParse(rawId);
        if (id == null) {
            source.sendFailure(Component.literal("Invalid block id: " + rawId));
            return 0;
        }
        FastProgressConfig config = FastProgressConfig.get();
        if (!config.blockIds.remove(id)) {
            source.sendFailure(Component.literal("Block not in list: " + id));
            return 0;
        }
        return saveAndNotify(source, "Removed block: " + id);
    }

    private static int blockList(CommandSourceStack source) {
        FastProgressConfig config = FastProgressConfig.get();
        if (config.blockIds.isEmpty() && config.tagIds.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No blocks or tags configured."), false);
            return 1;
        }
        source.sendSuccess(() -> Component.literal("Blocks: " + formatList(config.blockIds)), false);
        source.sendSuccess(() -> Component.literal("Tags: " + formatList(config.tagIds)), false);
        return 1;
    }

    private static int saveAndNotify(CommandSourceStack source, String message) {
        FastProgressConfig config = FastProgressConfig.get();
        config.sanitize();
        config.save();
        source.sendSuccess(() -> Component.literal(message), false);
        FastProgressNetworking.broadcastServerConfig(source.getServer());
        source.sendSuccess(() -> Component.literal("Fast Progress: server config updated."), false);
        return 1;
    }

    private static String formatList(Iterable<ResourceLocation> values) {
        String joined = java.util.stream.StreamSupport.stream(values.spliterator(), false)
                .map(ResourceLocation::toString)
                .collect(Collectors.joining(", "));
        return joined.isEmpty() ? "(empty)" : joined;
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestFilter(SuggestionsBuilder builder) {
        builder.suggest("blacklist");
        builder.suggest("whitelist");
        builder.suggest("0");
        builder.suggest("1");
        return builder.buildFuture();
    }
}
