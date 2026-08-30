package com.bettertp;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class Waypoint {
    public static final Codec<Waypoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("name").forGetter(w -> w.name),
        Codec.DOUBLE.fieldOf("x").forGetter(w -> w.x),
        Codec.DOUBLE.fieldOf("y").forGetter(w -> w.y),
        Codec.DOUBLE.fieldOf("z").forGetter(w -> w.z),
        Codec.STRING.fieldOf("world").forGetter(w -> w.world)
    ).apply(instance, Waypoint::new));

    public String name;
    public double x, y, z;
    public String world;

    public Waypoint(String name, double x, double y, double z, String world) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }
}