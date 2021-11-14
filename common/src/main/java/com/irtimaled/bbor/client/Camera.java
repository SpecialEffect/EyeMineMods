package com.irtimaled.bbor.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public class Camera {
    private static Vec3 getPos() {
        return Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
    }

    public static double getX() {
        return getPos().x;
    }

    public static double getY() {
        return getPos().y;
    }

    public static double getZ() {
        return getPos().z;
    }
}

