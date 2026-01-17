package com.fastprogress;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public final class StructureUtil {
    private StructureUtil() {
    }

    public static boolean isInsideAnyStructure(ServerLevel level, BlockPos pos) {
        StructureManager structureManager = level.structureManager();
        if (structureManager == null) {
            return false;
        }
        Registry<Structure> structureRegistry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        for (Structure structure : structureRegistry) {
            StructureStart start = structureManager.getStructureWithPieceAt(pos, structure);
            if (start == null) {
                continue;
            }
            try {
                if (start.isValid()) {
                    return true;
                }
            } catch (Throwable ignored) {
                if (!start.getPieces().isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }
}
