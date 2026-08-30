package com.bettertp;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public class PlayerDataAttachment {
    public static final AttachmentType<PlayerData> PLAYER_DATA = AttachmentRegistry.createPersistent(
        Identifier.of(BetterTPMod.MOD_ID, "player_data"),
        PlayerData.CODEC
    );

    public static PlayerData get(PlayerEntity player) {
        return player.getAttachedOrCreate(PLAYER_DATA, PlayerData::new);
    }

    public static void register() {
        // Fabric auto-registers
    }
}