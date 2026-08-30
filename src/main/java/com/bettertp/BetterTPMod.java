package com.bettertp;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterTPMod implements ModInitializer {
    public static final String MOD_ID = "bettertp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier TP_REQUEST_ID = Identifier.of(MOD_ID, "tp_request");
    public static final Identifier WAYPOINT_ACTION_ID = Identifier.of(MOD_ID, "waypoint_action");
    public static final Identifier HISTORY_ACTION_ID = Identifier.of(MOD_ID, "history_action");

    @Override
    public void onInitialize() {
        LOGGER.info("Better TP initialized!");

        PlayerDataAttachment.register();

        // 注册 payload 类型
        PayloadTypeRegistry.playC2S().register(TpRequestPayload.ID, TpRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(WaypointActionPayload.ID, WaypointActionPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(HistoryActionPayload.ID, HistoryActionPayload.CODEC);

        // 处理 TP 请求
        ServerPlayNetworking.registerGlobalReceiver(TpRequestPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                if (player == null) return;
                ServerPlayerEntity target = context.server().getPlayerManager().getPlayer(payload.targetName());
                if (target != null) {
                    PlayerData data = PlayerDataAttachment.get(player);
                    data.setLastLocation(player.getX(), player.getY(), player.getZ(), player.getWorld().getRegistryKey().getValue().toString());
                    player.teleport(target.getWorld(), target.getX(), target.getY(), target.getZ(), player.getYaw(), player.getPitch());
                }
            });
        });

        // 处理标记点操作
        ServerPlayNetworking.registerGlobalReceiver(WaypointActionPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                if (player == null) return;
                PlayerData data = PlayerDataAttachment.get(player);
                switch (payload.action()) {
                    case 0 -> data.addWaypoint(player.getX(), player.getY(), player.getZ(), player.getWorld().getRegistryKey().getValue().toString());
                    case 1 -> data.renameWaypoint(payload.index(), payload.name());
                    case 2 -> data.removeWaypoint(payload.index());
                    case 3 -> {
                        if (payload.index() >= 0 && payload.index() < data.getWaypoints().size()) {
                            Waypoint wp = data.getWaypoints().get(payload.index());
                            data.setLastLocation(player.getX(), player.getY(), player.getZ(), player.getWorld().getRegistryKey().getValue().toString());
                            var worldKey = net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.WORLD, Identifier.of(wp.world));
                            var targetWorld = player.getServer().getWorld(worldKey);
                            if (targetWorld != null) {
                                player.teleport(targetWorld, wp.x, wp.y, wp.z, player.getYaw(), player.getPitch());
                            }
                        }
                    }
                }
            });
        });

        // 处理历史操作
        ServerPlayNetworking.registerGlobalReceiver(HistoryActionPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                if (player == null) return;
                PlayerData data = PlayerDataAttachment.get(player);
                if (payload.type() == 0 && data.getLastLocation() != null) {
                    Location loc = data.getLastLocation();
                    data.setLastLocation(player.getX(), player.getY(), player.getZ(), player.getWorld().getRegistryKey().getValue().toString());
                    var worldKey = net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.WORLD, Identifier.of(loc.world));
                    var targetWorld = player.getServer().getWorld(worldKey);
                    if (targetWorld != null) {
                        player.teleport(targetWorld, loc.x, loc.y, loc.z, player.getYaw(), player.getPitch());
                    }
                } else if (payload.type() == 1 && data.getLastDeathLocation() != null) {
                    Location loc = data.getLastDeathLocation();
                    var worldKey = net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.WORLD, Identifier.of(loc.world));
                    var targetWorld = player.getServer().getWorld(worldKey);
                    if (targetWorld != null) {
                        player.teleport(targetWorld, loc.x, loc.y, loc.z, player.getYaw(), player.getPitch());
                    }
                }
            });
        });
    }
}