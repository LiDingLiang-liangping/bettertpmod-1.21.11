package com.bettertp;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class WaypointScreen extends Screen {
    private final Screen parent;
    private EditBox nameField;
    private int selectedSlot = -1;

    protected WaypointScreen(Screen parent) {
        super(Component.literal("Waypoint Manager"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearWidgets();
        int cx = this.width / 2;
        int cy = this.height / 2;

        int startX = cx - 4 * 36;
        int startY = cy - 60;

        for (int i = 0; i < 9; i++) {
            int slotX = startX + (i % 9) * 36;
            int slotY = startY + (i / 9) * 36;
            final int idx = i;

            boolean selected = (idx == selectedSlot);
            String label = selected ? "[" + (i+1) + "]" : String.valueOf(i+1);
            this.addRenderableWidget(Button.builder(Component.literal(label), btn -> {
                selectedSlot = idx;
                init();
            }).bounds(slotX, slotY, 32, 32).build());
        }

        int btnY = cy + 60;

        this.addRenderableWidget(Button.builder(Component.literal("+ Add"), btn -> {
            ClientPlayNetworking.send(new WaypointActionPayload(0, -1, ""));
            onClose();
        }).bounds(cx - 160, btnY, 80, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Rename"), btn -> {
            if (selectedSlot >= 0 && nameField != null && !nameField.getValue().isEmpty()) {
                ClientPlayNetworking.send(new WaypointActionPayload(1, selectedSlot, nameField.getValue()));
                onClose();
            }
        }).bounds(cx - 70, btnY, 80, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Delete"), btn -> {
            if (selectedSlot >= 0) {
                ClientPlayNetworking.send(new WaypointActionPayload(2, selectedSlot, ""));
                onClose();
            }
        }).bounds(cx + 20, btnY, 70, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Teleport"), btn -> {
            if (selectedSlot >= 0) {
                ClientPlayNetworking.send(new WaypointActionPayload(3, selectedSlot, ""));
                onClose();
            }
        }).bounds(cx + 100, btnY, 80, 20).build());

        nameField = new EditBox(this.font, cx - 100, btnY + 30, 200, 20, Component.literal("Enter name"));
        nameField.setMaxLength(32);
        if (selectedSlot >= 0) {
            nameField.setValue("Waypoint " + (selectedSlot + 1));
        }
        this.addRenderableWidget(nameField);

        this.addRenderableWidget(Button.builder(Component.literal("<- Back"), btn -> {
            minecraft.setScreen(parent);
        }).bounds(10, 10, 60, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);

        int cx = this.width / 2;
        graphics.drawCenteredString(this.font, Component.literal("Waypoint Manager (Max 9)"), cx, 40, 0xFFFF55);

        if (selectedSlot >= 0) {
            graphics.drawCenteredString(this.font, Component.literal("Selected: Slot " + (selectedSlot + 1)), cx, 60, 0xFFFFFF);
        }
    }
}