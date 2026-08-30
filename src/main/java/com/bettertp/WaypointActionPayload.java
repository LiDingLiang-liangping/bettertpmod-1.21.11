package com.bettertp;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record WaypointActionPayload(int action, int index, String name) implements CustomPayload {
    public static final CustomPayload.Id<WaypointActionPayload> ID = new CustomPayload.Id<>(BetterTPMod.WAYPOINT_ACTION_ID);
    public static final PacketCodec<PacketByteBuf, WaypointActionPayload> CODEC = PacketCodec.of(
        (payload, buf) -> {
            buf.writeInt(payload.action());
            buf.writeInt(payload.index());
            buf.writeString(payload.name());
        },
        buf -> new WaypointActionPayload(buf.readInt(), buf.readInt(), buf.readString())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}