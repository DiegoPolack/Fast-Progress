package com.fastprogress.forge;

import com.fastprogress.FastProgressConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;

public final class FastProgressForgeClient {
    private FastProgressForgeClient() {
    }

    public static void init() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(FastProgressForgeClient::createConfigScreen));
    }

    private static Screen createConfigScreen(Minecraft minecraft, Screen parent) {
        if (!ModList.get().isLoaded("cloth_config")) {
            return parent;
        }
        return FastProgressConfigScreen.create(parent);
    }
}
