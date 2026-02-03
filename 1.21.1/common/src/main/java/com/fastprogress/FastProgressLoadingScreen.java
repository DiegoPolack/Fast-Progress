package com.fastprogress;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class FastProgressLoadingScreen extends Screen {
    private final Screen parent;

    public FastProgressLoadingScreen(Screen parent) {
        super(Component.literal("FastProgress"));
        this.parent = parent;
    }

    public Screen getParent() {
        return parent;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(font, "Loading server config...", width / 2, height / 2 - 4, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }
}
