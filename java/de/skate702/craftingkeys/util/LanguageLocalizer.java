package de.skate702.craftingkeys.util;

import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.LanguageMap;

public class LanguageLocalizer {
    /**
     * If another language than de_DE or en_US is selected the English one is used...
     *
     * @param unloc The unlocalized string which is in the definded in the language file
     * @return localized The localized String
     */
    public static String localize(String unloc) {
    	LanguageMap map = new LanguageMap();
    	return map.translateKey(unloc);
    }
}
