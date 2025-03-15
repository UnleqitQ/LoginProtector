package com.unleqitq.loginProtector;

import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.java.JavaPlugin;

public final class LoginProtector extends JavaPlugin {
	private ProtectorCommand protectorCommand;
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		reloadConfig();
		registerCommands();
	}
	
	private void registerCommands() {
		protectorCommand = new ProtectorCommand(this);
		getServer().getCommandMap().register("loginprotector", protectorCommand);
	}
	
	@Override
	public void onDisable() {
		unregisterCommands();
		protectorCommand = null;
	}
	
	private void unregisterCommands() {
		SimpleCommandMap commandMap = (SimpleCommandMap) getServer().getCommandMap();
		commandMap.getKnownCommands().remove("loginprotector");
	}
	
}
