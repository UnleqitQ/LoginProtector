package com.unleqitq.loginProtector;

import lombok.Getter;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.java.JavaPlugin;

public final class LoginProtector extends JavaPlugin {
	
	@Getter
	private LoginListener loginListener;
	private ProtectorCommand protectorCommand;
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		reloadConfig();
		loginListener = new LoginListener(this);
		loginListener.setup();
		registerCommands();
	}
	
	private void registerCommands() {
		protectorCommand = new ProtectorCommand(this);
		getServer().getCommandMap().register("loginprotector", protectorCommand);
	}
	
	public void reload() {
		reloadConfig();
		loginListener.reload();
	}
	
	@Override
	public void onDisable() {
		loginListener.shutdown();
		loginListener = null;
		unregisterCommands();
		protectorCommand = null;
	}
	
	private void unregisterCommands() {
		SimpleCommandMap commandMap = (SimpleCommandMap) getServer().getCommandMap();
		commandMap.getKnownCommands().remove("loginprotector");
	}
	
}
