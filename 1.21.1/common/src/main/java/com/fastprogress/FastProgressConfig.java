package com.fastprogress;

import dev.architectury.platform.Platform;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class FastProgressConfig {
    public static final int DEFAULT_MULTIPLIER = 2;
    public static final boolean DEFAULT_AFFECT_STRUCTURES = false;
    public static final boolean DEFAULT_AFFECT_PLACED = false;
    public static final boolean DEFAULT_AFFECT_FORTUNE = true;
    public static final boolean DEFAULT_AFFECT_SILK_TOUCH = true;
    public static final FilterMode DEFAULT_FILTER_MODE = FilterMode.BLACKLIST;
    public static final int MAX_MULTIPLIER = 64;

    private static final Path CONFIG_PATH = Platform.getConfigFolder().resolve("fastprogress.cfg");

    public static FastProgressConfig INSTANCE = new FastProgressConfig();

    public int blockDropMultiplier = DEFAULT_MULTIPLIER;
    public boolean affectStructureBlocks = DEFAULT_AFFECT_STRUCTURES;
    public boolean affectEntityAndPlayerPlacedBlocks = DEFAULT_AFFECT_PLACED;
    public boolean affectFortuneDrops = DEFAULT_AFFECT_FORTUNE;
    public boolean affectSilkTouchDrops = DEFAULT_AFFECT_SILK_TOUCH;
    public FilterMode filterMode = DEFAULT_FILTER_MODE;
    public final Set<ResourceLocation> blockIds = new LinkedHashSet<>();
    public final Set<ResourceLocation> tagIds = new LinkedHashSet<>();

    public enum FilterMode {
        BLACKLIST(0, "Blacklist"),
        WHITELIST(1, "Whitelist");

        private final int id;
        private final String label;

        FilterMode(int id, String label) {
            this.id = id;
            this.label = label;
        }

        public int getId() {
            return id;
        }

        public String getLabel() {
            return label;
        }

        public static FilterMode fromId(int id) {
            return id == 1 ? WHITELIST : BLACKLIST;
        }
    }

    public static FastProgressConfig get() {
        return INSTANCE;
    }

    public static void load() {
        FastProgressConfig loaded = new FastProgressConfig();
        if (Files.exists(CONFIG_PATH)) {
            loaded.loadFrom(CONFIG_PATH);
        } else {
            loaded.saveTo(CONFIG_PATH);
        }
        INSTANCE = loaded;
    }

    public void save() {
        saveTo(CONFIG_PATH);
    }

    public FastProgressConfig copy() {
        FastProgressConfig copy = new FastProgressConfig();
        copy.blockDropMultiplier = blockDropMultiplier;
        copy.affectStructureBlocks = affectStructureBlocks;
        copy.affectEntityAndPlayerPlacedBlocks = affectEntityAndPlayerPlacedBlocks;
        copy.affectFortuneDrops = affectFortuneDrops;
        copy.affectSilkTouchDrops = affectSilkTouchDrops;
        copy.filterMode = filterMode;
        copy.blockIds.addAll(blockIds);
        copy.tagIds.addAll(tagIds);
        return copy;
    }

    public void applyFrom(FastProgressConfig other) {
        blockDropMultiplier = other.blockDropMultiplier;
        affectStructureBlocks = other.affectStructureBlocks;
        affectEntityAndPlayerPlacedBlocks = other.affectEntityAndPlayerPlacedBlocks;
        affectFortuneDrops = other.affectFortuneDrops;
        affectSilkTouchDrops = other.affectSilkTouchDrops;
        filterMode = other.filterMode;
        blockIds.clear();
        tagIds.clear();
        blockIds.addAll(other.blockIds);
        tagIds.addAll(other.tagIds);
    }

    public void sanitize() {
        if (blockDropMultiplier < 1) {
            blockDropMultiplier = 1;
        } else if (blockDropMultiplier > MAX_MULTIPLIER) {
            blockDropMultiplier = MAX_MULTIPLIER;
        }
        if (filterMode == null) {
            filterMode = DEFAULT_FILTER_MODE;
        }
    }

    public boolean isBlockAffected(BlockState state) {
        boolean matches = matchesBlockOrTag(state);
        if (filterMode == FilterMode.WHITELIST) {
            if (blockIds.isEmpty() && tagIds.isEmpty()) {
                return false;
            }
            return matches;
        }
        return !matches;
    }

    public List<String> toEntryList() {
        List<String> entries = new ArrayList<>();
        for (ResourceLocation blockId : blockIds) {
            entries.add(blockId.toString());
        }
        for (ResourceLocation tagId : tagIds) {
            entries.add("#" + tagId);
        }
        return entries;
    }

    public void setEntries(List<String> entries) {
        blockIds.clear();
        tagIds.clear();
        if (entries == null) {
            return;
        }
        for (String raw : entries) {
            if (raw == null) {
                continue;
            }
            String trimmed = raw.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.startsWith("#")) {
                String tagValue = trimmed.substring(1).trim();
                addTagId(tagValue);
            } else {
                addBlockId(trimmed);
            }
        }
    }

    private boolean matchesBlockOrTag(BlockState state) {
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (blockId != null && blockIds.contains(blockId)) {
            return true;
        }
        if (!tagIds.isEmpty()) {
            for (ResourceLocation tagId : tagIds) {
                TagKey<Block> tagKey = TagKey.create(Registries.BLOCK, tagId);
                if (state.is(tagKey)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void loadFrom(Path path) {
        List<String> lines;
        try {
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            return;
        }

        boolean inBlocks = false;
        for (String raw : lines) {
            if (raw == null) {
                continue;
            }
            String line = raw.trim();
            if (line.isEmpty()) {
                continue;
            }

            if (!inBlocks) {
                if (line.startsWith("#")) {
                    continue;
                }
                line = stripInlineComment(line);
                if (line.isEmpty()) {
                    continue;
                }
                if (line.equalsIgnoreCase("Blocks:") || line.equalsIgnoreCase("Blocks")) {
                    inBlocks = true;
                    continue;
                }
                int sepIndex = line.indexOf(':');
                if (sepIndex < 0) {
                    sepIndex = line.indexOf('=');
                }
                if (sepIndex < 0) {
                    continue;
                }
                String key = line.substring(0, sepIndex).trim().toLowerCase(Locale.ROOT);
                String value = line.substring(sepIndex + 1).trim();
                applyKeyValue(key, value);
            } else {
                if (line.startsWith("#")) {
                    if (line.length() > 1 && !Character.isWhitespace(line.charAt(1))) {
                        addTagId(line.substring(1).trim());
                    }
                    continue;
                }
                line = stripInlineComment(line);
                if (line.isEmpty()) {
                    continue;
                }
                addBlockId(line);
            }
        }

        sanitize();
    }

    private void saveTo(Path path) {
        sanitize();
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException ignored) {
            return;
        }

        List<String> lines = new ArrayList<>();
        lines.add("BlockDropMultiplier: " + blockDropMultiplier);
        lines.add("# Determines the factor which the drop rate increase. Default: " + DEFAULT_MULTIPLIER);
        lines.add("");
        lines.add("AffectStructureBlocks: " + affectStructureBlocks);
        lines.add("# Determines if the block drop multiplier affects blocks from generated structures. Default: " + DEFAULT_AFFECT_STRUCTURES);
        lines.add("");
        lines.add("AffectEntityAndPlayerPlacedBlocks: " + affectEntityAndPlayerPlacedBlocks);
        lines.add("# Determines if the block drop multiplier affects blocks placed by Player or Entities. Default: " + DEFAULT_AFFECT_PLACED);
        lines.add("");
        lines.add("AffectFortuneDrops: " + affectFortuneDrops);
        lines.add("# If false, the drop multiplier will NOT apply when the tool has Fortune.");
        lines.add("");
        lines.add("AffectSilkTouchDrops: " + affectSilkTouchDrops);
        lines.add("# If false, the drop multiplier will NOT apply when the tool has Silk Touch.");
        lines.add("");
        lines.add("Filter: " + filterMode.getId());
        lines.add("# 0 = Blacklist ; 1 = Whitelist. Default = " + DEFAULT_FILTER_MODE.getId());
        lines.add("# Blacklist: blocks in the list are NOT affected.");
        lines.add("# Whitelist: ONLY blocks in the list are affected. (If the list is empty, nothing is affected.)");
        lines.add("");
        lines.add("Blocks:");
        lines.add("# One block id per line");
        lines.add("# minecraft:diamond_ore");
        lines.add("# minecraft:ancient_debris");

        if (!blockIds.isEmpty() || !tagIds.isEmpty()) {
            for (ResourceLocation blockId : blockIds) {
                lines.add(blockId.toString());
            }
            for (ResourceLocation tagId : tagIds) {
                lines.add("#" + tagId);
            }
        }

        try {
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            // ignore save failures to keep gameplay stable
        }
    }

    private void applyKeyValue(String key, String value) {
        switch (key) {
            case "blockdropmultiplier":
                blockDropMultiplier = parseInt(value, blockDropMultiplier);
                break;
            case "affectstructureblocks":
                affectStructureBlocks = parseBoolean(value, affectStructureBlocks);
                break;
            case "affectentityandplayerplacedblocks":
                affectEntityAndPlayerPlacedBlocks = parseBoolean(value, affectEntityAndPlayerPlacedBlocks);
                break;
            case "affectfortunedrops":
                affectFortuneDrops = parseBoolean(value, affectFortuneDrops);
                break;
            case "affectsilktouchdrops":
                affectSilkTouchDrops = parseBoolean(value, affectSilkTouchDrops);
                break;
            case "filter":
                filterMode = FilterMode.fromId(parseInt(value, filterMode.getId()));
                break;
            default:
                break;
        }
    }

    private void addBlockId(String value) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id != null) {
            blockIds.add(id);
        }
    }

    private void addTagId(String value) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id != null) {
            tagIds.add(id);
        }
    }

    private static String stripInlineComment(String line) {
        int index = line.indexOf('#');
        if (index < 0) {
            return line;
        }
        return line.substring(0, index).trim();
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private static boolean parseBoolean(String value, boolean fallback) {
        if (value == null) {
            return fallback;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals("true") || normalized.equals("1") || normalized.equals("yes")) {
            return true;
        }
        if (normalized.equals("false") || normalized.equals("0") || normalized.equals("no")) {
            return false;
        }
        return fallback;
    }
}
