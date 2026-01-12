package com.fastprogress;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class PlacedBlocksState extends SavedData {
    private static final String DATA_NAME = "fastprogress_placed_blocks";
    private static final String TAG_POSITIONS = "positions";

    private final LongSet placedPositions = new LongOpenHashSet();

    public static PlacedBlocksState get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(PlacedBlocksState::load, PlacedBlocksState::new, DATA_NAME);
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

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLongArray(TAG_POSITIONS, placedPositions.toLongArray());
        return tag;
    }

    private static PlacedBlocksState load(CompoundTag tag) {
        PlacedBlocksState state = new PlacedBlocksState();
        long[] values = tag.getLongArray(TAG_POSITIONS);
        for (long value : values) {
            state.placedPositions.add(value);
        }
        return state;
    }
}
