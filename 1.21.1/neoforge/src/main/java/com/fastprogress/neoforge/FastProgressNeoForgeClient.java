package com.fastprogress.neoforge;

import com.fastprogress.FastProgressConfigScreen;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public final class FastProgressNeoForgeClient {
    private FastProgressNeoForgeClient() {
    }

    public static void init(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, parent) -> {
            if (!ModList.get().isLoaded("cloth_config")) {
                return parent;
            }
            return FastProgressConfigScreen.create(parent);
        });
    }
}
