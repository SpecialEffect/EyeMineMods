package com.specialeffect.mods;

public class Configuration {

	public boolean hasChanged() {
		return true;
	}

	public void save() {
		//noop
	}

	public boolean getBoolean(String string, String categoryBasic, boolean defaultVal, String string2) {
		// TODO Auto-generated method stub
		return defaultVal;
	}

	public int getInt(String string, String categoryAdvanced, int defaultVal, int i, int j, String string2) {
		// TODO Auto-generated method stub
		return defaultVal;
	}

	public float getFloat(String string, String categoryExpert, float defaultVal, float f, float g, String string2) {
		// TODO Auto-generated method stub
		return defaultVal;
	}

	public Object get(String categoryBasic, String string, float defaultVal) {
		return defaultVal;
	}

}
