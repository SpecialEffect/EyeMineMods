package com.specialeffect.eyemine.platform.fabric;

import com.specialeffect.eyemine.config.InventoryConfig;
import com.specialeffect.eyemine.platform.services.IInventoryConfigService;
import me.shedaniel.autoconfig.AutoConfig;

public class InventoryConfigImpl implements IInventoryConfigService {
    private InventoryConfig getConfig() {
        return AutoConfig.getConfigHolder(InventoryConfig.class).getConfig();
    }

    @Override public int getKey0() { return getConfig().configKeys.key0; }
    @Override public int getKey1() { return getConfig().configKeys.key1; }
    @Override public int getKey2() { return getConfig().configKeys.key2; }
    @Override public int getKey3() { return getConfig().configKeys.key3; }
    @Override public int getKey4() { return getConfig().configKeys.key4; }
    @Override public int getKey5() { return getConfig().configKeys.key5; }
    @Override public int getKey6() { return getConfig().configKeys.key6; }
    @Override public int getKey7() { return getConfig().configKeys.key7; }
    @Override public int getKey8() { return getConfig().configKeys.key8; }
    @Override public int getKey9() { return getConfig().configKeys.key9; }
    @Override public int getKeySurvPrevTab() { return getConfig().survival.keySurvPrevTab; }
    @Override public int getKeySurvNextTab() { return getConfig().survival.keySurvNextTab; }
    @Override public int getKeySurvRecipes() { return getConfig().survival.keySurvRecipes; }
    @Override public int getKeySurvCraftable() { return getConfig().survival.keySurvCraftable; }
    @Override public int getkeySurvPrevPage() { return getConfig().survival.keySurvPrevPage; }
    @Override public int getkeySurvNextPage() { return getConfig().survival.keySurvNextPage; }
    @Override public int getKeySurvOutput() { return getConfig().survival.keySurvOutput; }
    @Override public int getKeyPrev() { return getConfig().navKeys.keyPrev; }
    @Override public int getKeyNext() { return getConfig().navKeys.keyNext; }
    @Override public int getKeyNextItemRow() { return getConfig().navKeys.keyNextItemRow; }
    @Override public int getKeyNextItemCol() { return getConfig().navKeys.keyNextItemCol; }
    @Override public int getKeyScrollUp() { return getConfig().navKeys.keyScrollUp; }
    @Override public int getKeyScrollDown() { return getConfig().navKeys.keyScrollDown; }
    @Override public int getKeySearch() { return getConfig().navKeys.keySearch; }
    @Override public int getKeyDrop() { return getConfig().navKeys.keyDrop; }
}
