package com.bettertp;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

public class BetterTPScreen extends Screen {
    private GuiMode mode = GuiMode.MAIN;
    private int tpPlayerPage = 0;

    enum GuiMode {
        MAIN, TP_PLAYER, WAYPOINTS, HISTORY
    }

    protected BetterTPScreen() {
        super(Component.literal("Better TP"));
    }

    @Override
    protected void init() {
        this.clearWidgets();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        if (mode == GuiMode.MAIN) {
            this.addRenderableWidget(Button.builder(Component.literal("TP to Player"), btn -> {
                mode = GuiMode.TP_PLAYER;
                tpPlayerPage = 0;
                init();
            }).bounds(centerX - 80, centerY - 40, 160, 24).build());

            this.addRenderableWidget(Button.builder(Component.literal("Waypoints"), btn -> {
                mode = GuiMode.WAYPOINTS;
                init();
            }).bounds(centerX - 80, centerY - 10, 160, 24).build());

            this.addRenderableWidget(Button.builder(Component.literal("History"), btn -> {
                mode = GuiMode.HISTORY;
                init();
            }).bounds(centerX - 80, centerY + 20, 160, 24).build());
        } else if (mode == GuiMode.TP_PLAYER) {
            if (minecraft != null && minecraft.level != null) {
                var players = minecraft.level.players();
                var me = minecraft.player;
                java.util.List<net.minecraft.client.player.AbstractClientPlayer> others = new java.util.ArrayList<>();
                for (var p : players) {
                    if (p != me) others.add((net.minecraft.client.player.AbstractClientPlayer) p);
                }

                int startIdx = tpPlayerPage * 5;
                int endIdx = Math.min(startIdx + 5, others.size());
                int y = centerY - 70;

                for (int i = startIdx; i < endIdx; i++) {
                    var target = others.get(i);
                    String name = target.getName().getString();
                    this.addRenderableWidget(Button.builder(Component.literal(name), btn -> {
                        ClientPlayNetworking.send(new TpRequestPayload(name));
                        onClose();
                    }).bounds(centerX - 80, y, 160, 22).build());
                    y += 26;
                }

                if (tpPlayerPage > 0) {
                    this.addRenderableWidget(Button.builder(Component.literal("<- Prev"), btn -> {
                        tpPlayerPage--;
                        init();
                    }).bounds(centerX - 130, centerY + 70, 100, 20).build());
                }

                if (endIdx < others.size()) {
                    this.addRenderableWidget(Button.builder(Component.literal("Next ->"), btn -> {
                        tpPlayerPage++;
                        init();
                    }).bounds(centerX + 30, centerY + 70, 100, 20).build());
                }
            }
        } else if (mode == GuiMode.WAYPOINTS) {
            minecraft.setScreen(new WaypointScreen(this));
            return;
        } else if (mode == GuiMode.HISTORY) {
            this.addRenderableWidget(Button.builder(Component.literal("Last Teleport Location"), btn -> {
                ClientPlayNetworking.send(new HistoryActionPayload(0));
                onClose();
            }).bounds(centerX - 100, centerY - 25, 200, 24).build());

            this.addRenderableWidget(Button.builder(Component.literal("Last Death Location"), btn -> {
                ClientPlayNetworking.send(new HistoryActionPayload(1));
                onClose();
            }).bounds(centerX - 100, centerY + 10, 200, 24).build());
        }

        if (mode != GuiMode.MAIN) {
            this.addRenderableWidget(Button.builder(Component.literal("<- Back"), btn -> {
                mode = GuiMode.MAIN;
                init();
            }).bounds(10, 10, 60, 20).build());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);

        if (mode == GuiMode.MAIN) {
            graphics.drawCenteredString(this.font, Component.literal("* Better TP Main Menu *"), this.width / 2, this.height / 2 - 70, 0x55FF55);
        } else if (mode == GuiMode.TP_PLAYER) {
            graphics.drawCenteredString(this.font, Component.literal("Select Player to Teleport"), this.width / 2, this.height / 2 - 90, CommonColors.WHITE);
        } else if (mode == GuiMode.HISTORY) {
            graphics.drawCenteredString(this.font, Component.literal("Location History"), this.width / 2, this.height / 2 - 55, CommonColors.WHITE);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}