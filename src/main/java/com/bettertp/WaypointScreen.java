package com.bettertp;

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
    private int page = 0;

    protected WaypointScreen(Screen parent) {
        super(Text.literal("标记点管理"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearChildren();
        int cx = this.width / 2;
        int cy = this.height / 2;

        // 模拟箱子GUI布局 - 3行9列
        int startX = cx - 4 * 18 - 8;
        int startY = cy - 3 * 18 - 10;

        // 标题
        context = null; // will be set in render

        // 9个标记点槽位
        for (int i = 0; i < 9; i++) {
            int slotX = startX + (i % 9) * 36;
            int slotY = startY + (i / 9) * 36;
            final int idx = i;

            // 这里简化显示，实际应该用自定义渲染
            this.addDrawableChild(ButtonWidget.builder(Text.literal("[" + (i+1) + "]"), btn -> {
                selectedSlot = idx;
                init();
            }).dimensions(slotX, slotY, 32, 32).build());
        }

        // 操作按钮区域
        int btnY = cy + 80;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("+ 添加").formatted(Formatting.GREEN), btn -> {
            var buf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
            buf.writeInt(0);
            buf.writeInt(-1);
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(BetterTPMod.WAYPOINT_ACTION_PACKET, buf);
            close();
        }).dimensions(cx - 160, btnY, 100, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("重命名"), btn -> {
            if (selectedSlot >= 0 && nameField != null && !nameField.getText().isEmpty()) {
                var buf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
                buf.writeInt(1);
                buf.writeInt(selectedSlot);
                buf.writeString(nameField.getText());
                net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(BetterTPMod.WAYPOINT_ACTION_PACKET, buf);
                close();
            }
        }).dimensions(cx - 50, btnY, 80, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("删除").formatted(Formatting.RED), btn -> {
            if (selectedSlot >= 0) {
                var buf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
                buf.writeInt(2);
                buf.writeInt(selectedSlot);
                net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(BetterTPMod.WAYPOINT_ACTION_PACKET, buf);
                close();
            }
        }).dimensions(cx + 40, btnY, 60, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("传送").formatted(Formatting.AQUA), btn -> {
            if (selectedSlot >= 0) {
                var buf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
                buf.writeInt(3);
                buf.writeInt(selectedSlot);
                net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(BetterTPMod.WAYPOINT_ACTION_PACKET, buf);
                close();
            }
        }).dimensions(cx + 110, btnY, 60, 20).build());

        // 名称输入框
        nameField = new TextFieldWidget(this.textRenderer, cx - 100, btnY + 30, 200, 20, Text.literal("输入新名称"));
        nameField.setMaxLength(32);
        if (selectedSlot >= 0) {
            nameField.setText("标记点 " + (selectedSlot + 1));
        }
        this.addDrawableChild(nameField);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("\u2190 返回"), btn -> {
            client.setScreen(parent);
        }).dimensions(10, 10, 60, 20).build());
    }

    private DrawContext context;

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.context = context;
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        int cx = this.width / 2;
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("标记点管理 (最多9个)"), cx, 40, 0xFFFF55);

        if (selectedSlot >= 0) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("已选择: 槽位 " + (selectedSlot + 1)).formatted(Formatting.YELLOW), cx, 60, 0xFFFFFF);
        }
    }
}