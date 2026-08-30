package com.bettertp;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class Location {
    public static final Codec<Location> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.DOUBLE.fieldOf("x").forGetter(l -> l.x),
        Codec.DOUBLE.fieldOf("y").forGetter(l -> l.y),
        Codec.DOUBLE.fieldOf("z").forGetter(l -> l.z),
        Codec.STRING.fieldOf("world").forGetter(l -> l.world)
    ).apply(instance, Location::new));

    public double x, y, z;
    public String world;

    public Location(double x, double y, double z, String world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }
}