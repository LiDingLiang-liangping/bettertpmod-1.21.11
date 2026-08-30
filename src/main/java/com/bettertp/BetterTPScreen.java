package com.bettertp;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BetterTPScreen extends Screen {
    private GuiMode mode = GuiMode.MAIN;
    private int tpPlayerPage = 0;

    enum GuiMode {
        MAIN, TP_PLAYER, WAYPOINTS, HISTORY
    }

    protected BetterTPScreen() {
        super(Text.literal("Better TP"));
    }

    @Override
    protected void init() {
        this.clearChildren();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        if (mode == GuiMode.MAIN) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("TP to Player"), btn -> {
                mode = GuiMode.TP_PLAYER;
                tpPlayerPage = 0;
                init();
            }).dimensions(centerX - 80, centerY - 40, 160, 24).build());

            this.addDrawableChild(ButtonWidget.builder(Text.literal("Waypoints"), btn -> {
                mode = GuiMode.WAYPOINTS;
                init();
            }).dimensions(centerX - 80, centerY - 10, 160, 24).build());

            this.addDrawableChild(ButtonWidget.builder(Text.literal("History"), btn -> {
                mode = GuiMode.HISTORY;
                init();
            }).dimensions(centerX - 80, centerY + 20, 160, 24).build());
        } else if (mode == GuiMode.TP_PLAYER) {
            if (client != null && client.world != null) {
                var players = client.world.getPlayers();
                var me = client.player;
                java.util.List<net.minecraft.client.network.AbstractClientPlayerEntity> others = new java.util.ArrayList<>();
                for (var p : players) {
                    if (p != me) others.add(p);
                }

                int startIdx = tpPlayerPage * 5;
                int endIdx = Math.min(startIdx + 5, others.size());
                int y = centerY - 70;

                for (int i = startIdx; i < endIdx; i++) {
                    var target = others.get(i);
                    String name = target.getName().getString();
                    this.addDrawableChild(ButtonWidget.builder(Text.literal(name), btn -> {
                        ClientPlayNetworking.send(new TpRequestPayload(name));
                        close();
                    }).dimensions(centerX - 80, y, 160, 22).build());
                    y += 26;
                }

                if (tpPlayerPage > 0) {
                    this.addDrawableChild(ButtonWidget.builder(Text.literal("<- Prev").formatted(Formatting.LIGHT_PURPLE), btn -> {
                        tpPlayerPage--;
                        init();
                    }).dimensions(centerX - 130, centerY + 70, 100, 20).build());
                }

                if (endIdx < others.size()) {
                    this.addDrawableChild(ButtonWidget.builder(Text.literal("Next ->").formatted(Formatting.LIGHT_PURPLE), btn -> {
                        tpPlayerPage++;
                        init();
                    }).dimensions(centerX + 30, centerY + 70, 100, 20).build());
                }
            }
        } else if (mode == GuiMode.WAYPOINTS) {
            client.setScreen(new WaypointScreen(this));
            return;
        } else if (mode == GuiMode.HISTORY) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("Last Teleport Location"), btn -> {
                ClientPlayNetworking.send(new HistoryActionPayload(0));
                close();
            }).dimensions(centerX - 100, centerY - 25, 200, 24).build());

            this.addDrawableChild(ButtonWidget.builder(Text.literal("Last Death Location"), btn -> {
                ClientPlayNetworking.send(new HistoryActionPayload(1));
                close();
            }).dimensions(centerX - 100, centerY + 10, 200, 24).build());
        }

        if (mode != GuiMode.MAIN) {
            this.addDrawableChild(ButtonWidget.builder(Text.literal("<- Back"), btn -> {
                mode = GuiMode.MAIN;
                init();
            }).dimensions(10, 10, 60, 20).build());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        if (mode == GuiMode.MAIN) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("* Better TP Main Menu *"), this.width / 2, this.height / 2 - 70, 0x55FF55);
        } else if (mode == GuiMode.TP_PLAYER) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Select Player to Teleport"), this.width / 2, this.height / 2 - 90, 0xFFFFFF);
        } else if (mode == GuiMode.HISTORY) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Location History"), this.width / 2, this.height / 2 - 55, 0xFFFFFF);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}