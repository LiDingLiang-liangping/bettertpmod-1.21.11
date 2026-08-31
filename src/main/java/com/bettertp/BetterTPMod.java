package com.bettertp;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.portal.TeleportTransition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterTPMod implements ModInitializer {
    public static final String MOD_ID = "bettertp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier TP_REQUEST_ID = Identifier.fromNamespaceAndPath(MOD_ID, "tp_request");
    public static final Identifier WAYPOINT_ACTION_ID = Identifier.fromNamespaceAndPath(MOD_ID, "waypoint_action");
    public static final Identifier HISTORY_ACTION_ID = Identifier.fromNamespaceAndPath(MOD_ID, "history_action");

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
                    data.setLastLocation(player.getX(), player.getY(), player.getZ(), player.level().dimension().identifier().toString());
                    player.teleport(new TeleportTransition(target.level(), target.position(), player.getDeltaMovement(), player.getYRot(), player.getXRot(), TeleportTransition.PLAY_PORTAL_SOUND));
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(WaypointActionPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                if (player == null) return;
                PlayerData data = PlayerDataAttachment.get(player);
                switch (payload.action()) {
                    case 0 -> data.addWaypoint(player.getX(), player.getY(), player.getZ(), player.level().dimension().identifier().toString());
                    case 1 -> data.renameWaypoint(payload.index(), payload.name());
                    case 2 -> data.removeWaypoint(payload.index());
                    case 3 -> {
                        if (payload.index() >= 0 && payload.index() < data.getWaypoints().size()) {
                            Waypoint wp = data.getWaypoints().get(payload.index());
                            data.setLastLocation(player.getX(), player.getY(), player.getZ(), player.level().dimension().identifier().toString());
                            var worldKey = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, Identifier.parse(wp.world));
                            var targetWorld = player.level().getServer().getLevel(worldKey);
                            if (targetWorld != null) {
                                player.teleport(new TeleportTransition(targetWorld, new net.minecraft.world.phys.Vec3(wp.x, wp.y, wp.z), player.getDeltaMovement(), player.getYRot(), player.getXRot(), TeleportTransition.PLAY_PORTAL_SOUND));
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
                if (payload.actionType() == 0 && data.getLastLocation() != null) {
                    Location loc = data.getLastLocation();
                    data.setLastLocation(player.getX(), player.getY(), player.getZ(), player.level().dimension().identifier().toString());
                    var worldKey = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, Identifier.parse(loc.world));
                    var targetWorld = player.level().getServer().getLevel(worldKey);
                    if (targetWorld != null) {
                        player.teleport(new TeleportTransition(targetWorld, new net.minecraft.world.phys.Vec3(loc.x, loc.y, loc.z), player.getDeltaMovement(), player.getYRot(), player.getXRot(), TeleportTransition.PLAY_PORTAL_SOUND));
                    }
                } else if (payload.actionType() == 1 && data.getLastDeathLocation() != null) {
                    Location loc = data.getLastDeathLocation();
                    var worldKey = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, Identifier.parse(loc.world));
                    var targetWorld = player.level().getServer().getLevel(worldKey);
                    if (targetWorld != null) {
                        player.teleport(new TeleportTransition(targetWorld, new net.minecraft.world.phys.Vec3(loc.x, loc.y, loc.z), player.getDeltaMovement(), player.getYRot(), player.getXRot(), TeleportTransition.PLAY_PORTAL_SOUND));
                    }
                }
            });
        });
    }
}