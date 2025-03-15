package com.unleqitq.loginProtector;

import lombok.Getter;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class LoginProtector extends JavaPlugin {
	
	@Nullable
	private static LoginProtector instance;
	
	@Getter
	private LoginListener loginListener;
	private ProtectorCommand protectorCommand;
	
	private BukkitTask updateCheckTask;
	
	@Override
	public void onEnable() {
		instance = this;
		cleanup();
		saveDefaultConfig();
		reloadConfig();
		configFix();
		UpdateChecker.scheduleUpdateCheck(task -> updateCheckTask = task);
		loginListener = new LoginListener(this);
		loginListener.setup();
		registerCommands();
	}
	
	public void cleanup() {
		UpdateChecker.VersionInfo currentVersion =
			UpdateChecker.VersionInfo.fromString(getPluginMeta().getVersion());
		for (File file : Objects.requireNonNull(getServer().getPluginsFolder()
			.listFiles((dir, name) -> name.startsWith("LoginProtector-") && name.endsWith(".jar")))) {
			String fileName = file.getName();
			String versionString = fileName.substring(15, fileName.length() - 4);
			try {
				UpdateChecker.VersionInfo version = UpdateChecker.VersionInfo.fromString(versionString);
				if (currentVersion.isNewerThan(version)) {
					if (!file.delete()) {
						getLogger().warning("Failed to delete outdated plugin file: " + file.getName());
					}
				}
			}
			catch (IllegalArgumentException e) {
				getLogger().warning("Failed to parse version from file name: " + file.getName());
			}
		}
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
		if (updateCheckTask != null) {
			try {
				updateCheckTask.cancel();
			}
			catch (IllegalStateException e) {
				// Ignore
			}
		}
		loginListener.shutdown();
		loginListener = null;
		unregisterCommands();
		protectorCommand = null;
		instance = null;
	}
	
	@NotNull
	public static LoginProtector getInstance() {
		if (instance == null) {
			throw new IllegalStateException("Plugin is not enabled");
		}
		return instance;
	}
	
	private void unregisterCommands() {
		SimpleCommandMap commandMap = (SimpleCommandMap) getServer().getCommandMap();
		commandMap.getKnownCommands().remove("loginprotector");
	}
	
}
