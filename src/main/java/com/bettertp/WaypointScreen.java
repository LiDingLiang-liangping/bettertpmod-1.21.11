package com.bettertp;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class WaypointScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget nameField;
    private int selectedSlot = -1;

    protected WaypointScreen(Screen parent) {
        super(Text.literal("Waypoint Manager"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearChildren();
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
            this.addDrawableChild(ButtonWidget.builder(Text.literal(label).formatted(selected ? Formatting.YELLOW : Formatting.WHITE), btn -> {
                selectedSlot = idx;
                init();
            }).dimensions(slotX, slotY, 32, 32).build());
        }

        int btnY = cy + 60;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("+ Add").formatted(Formatting.GREEN), btn -> {
            ClientPlayNetworking.send(new WaypointActionPayload(0, -1, ""));
            close();
        }).dimensions(cx - 160, btnY, 80, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Rename"), btn -> {
            if (selectedSlot >= 0 && nameField != null && !nameField.getText().isEmpty()) {
                ClientPlayNetworking.send(new WaypointActionPayload(1, selectedSlot, nameField.getText()));
                close();
            }
        }).dimensions(cx - 70, btnY, 80, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Delete").formatted(Formatting.RED), btn -> {
            if (selectedSlot >= 0) {
                ClientPlayNetworking.send(new WaypointActionPayload(2, selectedSlot, ""));
                close();
            }
        }).dimensions(cx + 20, btnY, 70, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Teleport").formatted(Formatting.AQUA), btn -> {
            if (selectedSlot >= 0) {
                ClientPlayNetworking.send(new WaypointActionPayload(3, selectedSlot, ""));
                close();
            }
        }).dimensions(cx + 100, btnY, 80, 20).build());

        nameField = new TextFieldWidget(this.textRenderer, cx - 100, btnY + 30, 200, 20, Text.literal("Enter name"));
        nameField.setMaxLength(32);
        if (selectedSlot >= 0) {
            nameField.setText("Waypoint " + (selectedSlot + 1));
        }
        this.addDrawableChild(nameField);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("<- Back"), btn -> {
            client.setScreen(parent);
        }).dimensions(10, 10, 60, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        int cx = this.width / 2;
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Waypoint Manager (Max 9)"), cx, 40, 0xFFFF55);

        if (selectedSlot >= 0) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Selected: Slot " + (selectedSlot + 1)).formatted(Formatting.YELLOW), cx, 60, 0xFFFFFF);
        }
    }
}