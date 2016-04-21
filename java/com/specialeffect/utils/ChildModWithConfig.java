package com.specialeffect.utils;

// An interface that allows a parent mod to pass
// config changes down to the children
public interface ChildModWithConfig {
	public void syncConfig();
}
