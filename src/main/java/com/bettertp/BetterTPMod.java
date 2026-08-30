package com.bettertp;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterTPMod implements ModInitializer {
    public static final String MOD_ID = "bettertp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier TP_REQUEST_PACKET = Identifier.of(MOD_ID, "tp_request");
    public static final Identifier WAYPOINT_ACTION_PACKET = Identifier.of(MOD_ID, "waypoint_action");
    public static final Identifier HISTORY_ACTION_PACKET = Identifier.of(MOD_ID, "history_action");

    @Override
    public void onInitialize() {
        LOGGER.info("Better TP initialized!");

        PlayerDataAttachment.register();

        ServerPlayNetworking.registerGlobalReceiver(TP_REQUEST_PACKET, (server, player, handler, buf, responseSender) -> {
            String targetName = buf.readString();
            server.execute(() -> {
                if (player == null) return;
                ServerPlayerEntity target = server.getPlayerManager().getPlayer(targetName);
                if (target != null) {
                    PlayerData data = PlayerDataAttachment.get(player);
                    data.setLastLocation(player.getX(), player.getY(), player.getZ(), player.getWorld().getRegistryKey().getValue().toString());
                    player.teleport(target.getServerWorld(), target.getX(), target.getY(), target.getZ(), player.getYaw(), player.getPitch());
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(WAYPOINT_ACTION_PACKET, (server, player, handler, buf, responseSender) -> {
            int action = buf.readInt();
            int index = buf.readInt();
            String name = action == 1 ? buf.readString() : "";
            server.execute(() -> {
                if (player == null) return;
                PlayerData data = PlayerDataAttachment.get(player);
                switch (action) {
                    case 0 -> data.addWaypoint(player.getX(), player.getY(), player.getZ(), player.getWorld().getRegistryKey().getValue().toString());
                    case 1 -> data.renameWaypoint(index, name);
                    case 2 -> data.removeWaypoint(index);
                    case 3 -> {
                        if (index >= 0 && index < data.getWaypoints().size()) {
                            Waypoint wp = data.getWaypoints().get(index);
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

        ServerPlayNetworking.registerGlobalReceiver(HISTORY_ACTION_PACKET, (server, player, handler, buf, responseSender) -> {
            int type = buf.readInt();
            server.execute(() -> {
                if (player == null) return;
                PlayerData data = PlayerDataAttachment.get(player);
                if (type == 0 && data.getLastLocation() != null) {
                    Location loc = data.getLastLocation();
                    data.setLastLocation(player.getX(), player.getY(), player.getZ(), player.getWorld().getRegistryKey().getValue().toString());
                    var worldKey = net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.WORLD, Identifier.of(loc.world));
                    var targetWorld = player.getServer().getWorld(worldKey);
                    if (targetWorld != null) {
                        player.teleport(targetWorld, loc.x, loc.y, loc.z, player.getYaw(), player.getPitch());
                    }
                } else if (type == 1 && data.getLastDeathLocation() != null) {
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