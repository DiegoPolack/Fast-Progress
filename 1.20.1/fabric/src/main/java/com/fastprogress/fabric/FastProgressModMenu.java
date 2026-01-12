package com.fastprogress.fabric;

import com.fastprogress.FastProgressConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

public class FastProgressModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            if (!FabricLoader.getInstance().isModLoaded("cloth-config")) {
                return null;
            }
            return FastProgressConfigScreen.create(parent);
        };
    }
}
