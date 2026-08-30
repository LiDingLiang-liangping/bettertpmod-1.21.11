package com.bettertp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TpRequestPayload(String targetName) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<TpRequestPayload> ID = new CustomPacketPayload.Type<>(BetterTPMod.TP_REQUEST_ID);
    public static final StreamCodec<FriendlyByteBuf, TpRequestPayload> CODEC = StreamCodec.of(
        (buf, payload) -> buf.writeUtf(payload.targetName()),
        buf -> new TpRequestPayload(buf.readUtf())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}