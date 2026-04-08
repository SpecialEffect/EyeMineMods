package com.specialeffect.eyemine.platform;

import com.specialeffect.eyemine.packets.NetworkService;
import com.specialeffect.eyemine.platform.services.IEyeMineConfigService;
import com.specialeffect.eyemine.platform.services.IInventoryConfigService;
import com.specialeffect.eyemine.platform.services.IPlatformHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ServiceLoader;

public class Services {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
    public static final IEyeMineConfigService CONFIG = load(IEyeMineConfigService.class);
    public static final IInventoryConfigService INVENTORY_CONFIG = load(IInventoryConfigService.class);
    public static final NetworkService NETWORK = load(NetworkService.class);

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        LOGGER.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
