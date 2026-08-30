package com.bettertp;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record TpRequestPayload(String targetName) implements CustomPayload {
    public static final CustomPayload.Id<TpRequestPayload> ID = new CustomPayload.Id<>(BetterTPMod.TP_REQUEST_ID);
    public static final PacketCodec<PacketByteBuf, TpRequestPayload> CODEC = PacketCodec.of(
        (payload, buf) -> buf.writeString(payload.targetName()),
        buf -> new TpRequestPayload(buf.readString())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}