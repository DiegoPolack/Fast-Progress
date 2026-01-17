package com.fastprogress;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class PlacedBlocksState extends SavedData {
    private static final String DATA_NAME = "fastprogress_placed_blocks";
    private static final Codec<PlacedBlocksState> CODEC = Codec.LONG.listOf().xmap(
            list -> {
                LongSet set = new LongOpenHashSet(list.size());
                for (Long value : list) {
                    if (value != null) {
                        set.add(value);
                    }
                }
                return new PlacedBlocksState(set);
            },
            state -> state.placedPositions.longStream().boxed().toList()
    );
    private static final SavedDataType<PlacedBlocksState> TYPE =
            new SavedDataType<>(DATA_NAME, PlacedBlocksState::new, CODEC, DataFixTypes.LEVEL);

    private final LongSet placedPositions;

    private PlacedBlocksState() {
        this(new LongOpenHashSet());
    }

    private PlacedBlocksState(LongSet placedPositions) {
        this.placedPositions = placedPositions;
    }

    public static PlacedBlocksState get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public void markPlaced(BlockPos pos) {
        if (placedPositions.add(pos.asLong())) {
            setDirty();
        }
    }

    public boolean isPlaced(BlockPos pos) {
        return placedPositions.contains(pos.asLong());
    }

    public void unmark(BlockPos pos) {
        if (placedPositions.remove(pos.asLong())) {
            setDirty();
        }
    }

}
