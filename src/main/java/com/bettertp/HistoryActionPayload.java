package com.bettertp;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record HistoryActionPayload(int actionType) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<HistoryActionPayload> ID = new CustomPacketPayload.Type<>(BetterTPMod.HISTORY_ACTION_ID);
    public static final StreamCodec<FriendlyByteBuf, HistoryActionPayload> CODEC = StreamCodec.of(
        (buf, payload) -> buf.writeInt(payload.actionType()),
        buf -> new HistoryActionPayload(buf.readInt())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}