package com.bettertp;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlayerData {
    public static final Codec<PlayerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Waypoint.CODEC.listOf().fieldOf("waypoints").forGetter(d -> d.waypoints),
        Location.CODEC.optionalFieldOf("last_location").forGetter(d -> Optional.ofNullable(d.lastLocation)),
        Location.CODEC.optionalFieldOf("last_death").forGetter(d -> Optional.ofNullable(d.lastDeathLocation))
    ).apply(instance, (waypoints, lastLoc, lastDeath) -> {
        PlayerData data = new PlayerData();
        data.waypoints = new ArrayList<>(waypoints);
        data.lastLocation = lastLoc.orElse(null);
        data.lastDeathLocation = lastDeath.orElse(null);
        return data;
    }));

    private List<Waypoint> waypoints = new ArrayList<>();
    private Location lastLocation = null;
    private Location lastDeathLocation = null;

    public List<Waypoint> getWaypoints() { return waypoints; }
    public Location getLastLocation() { return lastLocation; }
    public Location getLastDeathLocation() { return lastDeathLocation; }

    public void setLastLocation(double x, double y, double z, String world) {
        this.lastLocation = new Location(x, y, z, world);
    }

    public void setLastDeathLocation(double x, double y, double z, String world) {
        this.lastDeathLocation = new Location(x, y, z, world);
    }

    public void addWaypoint(double x, double y, double z, String world) {
        if (waypoints.size() < 9) {
            waypoints.add(new Waypoint("Unnamed", x, y, z, world));
        }
    }

    public void renameWaypoint(int index, String name) {
        if (index >= 0 && index < waypoints.size()) {
            waypoints.get(index).name = name;
        }
    }

    public void removeWaypoint(int index) {
        if (index >= 0 && index < waypoints.size()) {
            waypoints.remove(index);
        }
    }
}