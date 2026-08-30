package com.bettertp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record WaypointActionPayload(int action, int index, String name) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<WaypointActionPayload> ID = new CustomPacketPayload.Type<>(BetterTPMod.WAYPOINT_ACTION_ID);
    public static final StreamCodec<FriendlyByteBuf, WaypointActionPayload> CODEC = StreamCodec.of(
        (buf, payload) -> {
            buf.writeInt(payload.action());
            buf.writeInt(payload.index());
            buf.writeUtf(payload.name());
        },
        buf -> new WaypointActionPayload(buf.readInt(), buf.readInt(), buf.readUtf())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}