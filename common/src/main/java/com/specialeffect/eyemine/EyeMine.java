package com.specialeffect.eyemine;

import com.specialeffect.eyemine.packets.PacketHandler;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EyeMine {
    public static final String MOD_ID = "eyemine";

    public static final Logger LOGGER = LogManager.getLogger();

    public static void init() {
        PacketHandler.init();
    }
}
