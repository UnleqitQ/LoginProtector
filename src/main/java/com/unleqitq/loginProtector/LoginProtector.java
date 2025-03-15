package com.unleqitq.loginProtector;

import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.java.JavaPlugin;

public final class LoginProtector extends JavaPlugin {
	
	private LoginListener loginListener;
	private ProtectorCommand protectorCommand;
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		reloadConfig();
		loginListener = new LoginListener(this);
		ProtocolLibrary.getProtocolManager().addPacketListener(loginListener);
		registerCommands();
	}
	
	private void registerCommands() {
		protectorCommand = new ProtectorCommand(this);
		getServer().getCommandMap().register("loginprotector", protectorCommand);
	}
	
	@Override
	public void onDisable() {
		ProtocolLibrary.getProtocolManager().removePacketListener(loginListener);
		loginListener = null;
		unregisterCommands();
		protectorCommand = null;
	}
	
	private void unregisterCommands() {
		SimpleCommandMap commandMap = (SimpleCommandMap) getServer().getCommandMap();
		commandMap.getKnownCommands().remove("loginprotector");
	}
	
}
