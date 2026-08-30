package com.bettertp;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterTPMod implements ModInitializer {
    public static final String MOD_ID = "bettertp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final ResourceLocation TP_REQUEST_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "tp_request");
    public static final ResourceLocation WAYPOINT_ACTION_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "waypoint_action");
    public static final ResourceLocation HISTORY_ACTION_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "history_action");

    @Override
    public void onInitialize() {
        LOGGER.info("Better TP initialized!");

        PlayerDataAttachment.register();

        PayloadTypeRegistry.playC2S().register(TpRequestPayload.ID, TpRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(WaypointActionPayload.ID, WaypointActionPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(HistoryActionPayload.ID, HistoryActionPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(TpRequestPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                if (player == null) return;
                ServerPlayer target = context.server().getPlayerList().getPlayerByName(payload.targetName());
                if (target != null) {
                    PlayerData data = PlayerDataAttachment.get(player);
                    data.setLastLocation(player.getX(), player.getY(), player.getZ(), player.level().dimension().location().toString());
                    player.teleport(target.level(), target.getX(), target.getY(), target.getZ(), player.getYRot(), player.getXRot());
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(WaypointActionPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                if (player == null) return;
                PlayerData data = PlayerDataAttachment.get(player);
                switch (payload.action()) {
                    case 0 -> data.addWaypoint(player.getX(), player.getY(), player.getZ(), player.level().dimension().location().toString());
                    case 1 -> data.renameWaypoint(payload.index(), payload.name());
                    case 2 -> data.removeWaypoint(payload.index());
                    case 3 -> {
                        if (payload.index() >= 0 && payload.index() < data.getWaypoints().size()) {
                            Waypoint wp = data.getWaypoints().get(payload.index());
                            data.setLastLocation(player.getX(), player.getY(), player.getZ(), player.level().dimension().location().toString());
                            var worldKey = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, ResourceLocation.parse(wp.world));
                            var targetWorld = player.level().getServer().getLevel(worldKey);
                            if (targetWorld != null) {
                                player.teleport(targetWorld, wp.x, wp.y, wp.z, player.getYRot(), player.getXRot());
                            }
                        }
                    }
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(HistoryActionPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                if (player == null) return;
                PlayerData data = PlayerDataAttachment.get(player);
                if (payload.type() == 0 && data.getLastLocation() != null) {
                    Location loc = data.getLastLocation();
                    data.setLastLocation(player.getX(), player.getY(), player.getZ(), player.level().dimension().location().toString());
                    var worldKey = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, ResourceLocation.parse(loc.world));
                    var targetWorld = player.level().getServer().getLevel(worldKey);
                    if (targetWorld != null) {
                        player.teleport(targetWorld, loc.x, loc.y, loc.z, player.getYRot(), player.getXRot());
                    }
                } else if (payload.type() == 1 && data.getLastDeathLocation() != null) {
                    Location loc = data.getLastDeathLocation();
                    var worldKey = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, ResourceLocation.parse(loc.world));
                    var targetWorld = player.level().getServer().getLevel(worldKey);
                    if (targetWorld != null) {
                        player.teleport(targetWorld, loc.x, loc.y, loc.z, player.getYRot(), player.getXRot());
                    }
                }
            });
        });
    }
}