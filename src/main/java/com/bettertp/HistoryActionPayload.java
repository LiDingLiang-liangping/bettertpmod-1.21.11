package com.bettertp;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record HistoryActionPayload(int type) implements CustomPayload {
    public static final CustomPayload.Id<HistoryActionPayload> ID = new CustomPayload.Id<>(BetterTPMod.HISTORY_ACTION_ID);
    public static final PacketCodec<PacketByteBuf, HistoryActionPayload> CODEC = PacketCodec.of(
        (payload, buf) -> buf.writeInt(payload.type()),
        buf -> new HistoryActionPayload(buf.readInt())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}