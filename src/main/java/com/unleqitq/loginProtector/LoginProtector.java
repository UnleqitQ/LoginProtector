package com.unleqitq.loginProtector;

import lombok.Getter;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class LoginProtector extends JavaPlugin {
	
	@Getter
	private LoginListener loginListener;
	private ProtectorCommand protectorCommand;
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		reloadConfig();
		configFix();
		loginListener = new LoginListener(this);
		loginListener.setup();
		registerCommands();
	}
	
	public void configFix() {
		boolean save = false;
		if (!getConfig().contains("webhook", true)) {
			getConfig().set("webhook", "DISCORD_WEBHOOK_URL");
			save = true;
		}
		if (!getConfig().contains("logged-usernames", true)) {
			getConfig().set("logged-usernames", new ArrayList<String>());
			save = true;
		}
		if (!getConfig().contains("analysis.interval", true)) {
			getConfig().set("analysis.interval", 5);
			getConfig().setInlineComments(
				"analysis.interval",
				List.of("seconds between each analysis task run")
			);
			save = true;
		}
		if (!getConfig().contains("analysis.timeout", true)) {
			getConfig().set("analysis.timeout", 30);
			getConfig().setInlineComments(
				"analysis.timeout",
				List.of("seconds before a login attempt is considered timed out")
			);
			save = true;
		}
		
		if (save) {
			saveConfig();
		}
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
