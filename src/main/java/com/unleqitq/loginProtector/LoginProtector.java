package com.unleqitq.loginProtector;

import org.bukkit.plugin.java.JavaPlugin;

public final class LoginProtector extends JavaPlugin {
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		reloadConfig();
	}
	
	@Override
	public void onDisable() {
	}
	
}
