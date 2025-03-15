package com.unleqitq.loginProtector;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ProtectorCommand extends Command implements PluginIdentifiableCommand {
	
	private final LoginProtector plugin;
	
	public ProtectorCommand(LoginProtector plugin) {
		super("loginprotector");
		this.plugin = plugin;
		setPermission("loginprotector.admin");
	}
	
	@Override
	public @NotNull Plugin getPlugin() {
		return plugin;
	}
	
	@Override
	public boolean execute(
		@NotNull CommandSender sender, @NotNull String label,
		String @NotNull [] args
	) {
		return switch (args.length == 0 ? "help" : args[0].toLowerCase()) {
			case "reload" -> {
				plugin.reload();
				sender.sendMessage(Component.text("LoginProtector")
					.color(NamedTextColor.AQUA)
					.append(Component.text("» ").color(NamedTextColor.GRAY))
					.append(Component.text("Config reloaded").color(NamedTextColor.GREEN)));
				yield true;
			}
			case "version" -> {
				sender.sendMessage(Component.text("LoginProtector")
					.color(NamedTextColor.AQUA)
					.append(Component.text("» ").color(NamedTextColor.GRAY))
					.append(Component.text("Version: " + plugin.getPluginMeta().getVersion())
						.color(NamedTextColor.GREEN)));
				yield true;
			}
			case "edit" -> onEditCommand(sender, Arrays.copyOfRange(args, 1, args.length));
			case "help" -> {
				sender.sendMessage(Component.text("LoginProtector")
					.color(NamedTextColor.AQUA)
					.append(Component.text("» ").color(NamedTextColor.GRAY))
					.append(Component.text("Commands:").color(NamedTextColor.GREEN)));
				sender.sendMessage(Component.text("» ")
					.color(NamedTextColor.GRAY)
					.append(Component.text("/loginprotector reload").color(NamedTextColor.GREEN))
					.append(Component.text(" - Reloads the config").color(NamedTextColor.GRAY)));
				sender.sendMessage(Component.text("» ")
					.color(NamedTextColor.GRAY)
					.append(Component.text("/loginprotector version").color(NamedTextColor.GREEN))
					.append(Component.text(" - Shows the plugin version").color(NamedTextColor.GRAY)));
				sender.sendMessage(Component.text("» ")
					.color(NamedTextColor.GRAY)
					.append(Component.text("/loginprotector edit ...").color(NamedTextColor.GREEN))
					.append(Component.text(" - Edit the config").color(NamedTextColor.GRAY)));
				yield true;
			}
			default -> false;
		};
	}
	
	private boolean onEditCommand(CommandSender sender, String @NotNull [] args) {
		return switch (args.length == 0 ? "help" : args[0].toLowerCase()) {
			case "usernames" -> onEditUsernamesCommand(sender, Arrays.copyOfRange(args, 1, args.length));
			case "webhook" -> onEditWebhookCommand(sender, Arrays.copyOfRange(args, 1, args.length));
			case "analysis" -> onEditAnalysisCommand(sender, Arrays.copyOfRange(args, 1, args.length));
			case "help" -> {
				sender.sendMessage(Component.text("LoginProtector")
					.color(NamedTextColor.AQUA)
					.append(Component.text("» ").color(NamedTextColor.GRAY))
					.append(Component.text("Edit config:").color(NamedTextColor.GREEN)));
				sender.sendMessage(Component.text("» ")
					.color(NamedTextColor.GRAY)
					.append(Component.text("/loginprotector edit usernames ...").color(NamedTextColor.GREEN))
					.append(Component.text(" - Edit the usernames list").color(NamedTextColor.GRAY)));
				sender.sendMessage(Component.text("» ")
					.color(NamedTextColor.GRAY)
					.append(Component.text("/loginprotector edit webhook <url>").color(NamedTextColor.GREEN))
					.append(Component.text(" - Edit the webhook").color(NamedTextColor.GRAY)));
				sender.sendMessage(Component.text("» ")
					.color(NamedTextColor.GRAY)
					.append(Component.text("/loginprotector edit analysis ...").color(NamedTextColor.GREEN))
					.append(Component.text(" - Edit the analysis settings").color(NamedTextColor.GRAY)));
				yield true;
			}
			default -> false;
		};
	}
	
	private boolean onEditAnalysisCommand(CommandSender sender, String @NotNull [] args) {
		return switch (args.length == 0 ? "help" : args[0].toLowerCase()) {
			case "interval" -> {
				if (args.length != 2) {
					sender.sendMessage(Component.text("LoginProtector")
						.color(NamedTextColor.AQUA)
						.append(Component.text("» ").color(NamedTextColor.GRAY))
						.append(Component.text("Missing argument").color(NamedTextColor.RED)));
					yield false;
				}
				try {
					int interval = Integer.parseInt(args[1]);
					if (interval < 1) {
						sender.sendMessage(Component.text("LoginProtector")
							.color(NamedTextColor.AQUA)
							.append(Component.text("» ").color(NamedTextColor.GRAY))
							.append(Component.text("Invalid interval").color(NamedTextColor.RED)));
						yield false;
					}
					plugin.getConfig().set("analysis.interval", interval);
					plugin.saveConfig();
					plugin.getLoginListener().reload();
					sender.sendMessage(Component.text("LoginProtector")
						.color(NamedTextColor.AQUA)
						.append(Component.text("» ").color(NamedTextColor.GRAY))
						.append(Component.text("Interval updated").color(NamedTextColor.GREEN)));
					yield true;
				}
				catch (NumberFormatException e) {
					sender.sendMessage(Component.text("LoginProtector")
						.color(NamedTextColor.AQUA)
						.append(Component.text("» ").color(NamedTextColor.GRAY))
						.append(Component.text("Invalid interval").color(NamedTextColor.RED)));
					yield false;
				}
			}
			case "timeout" -> {
				if (args.length != 2) {
					sender.sendMessage(Component.text("LoginProtector")
						.color(NamedTextColor.AQUA)
						.append(Component.text("» ").color(NamedTextColor.GRAY))
						.append(Component.text("Missing argument").color(NamedTextColor.RED)));
					yield false;
				}
				try {
					int timeout = Integer.parseInt(args[1]);
					if (timeout < 1) {
						sender.sendMessage(Component.text("LoginProtector")
							.color(NamedTextColor.AQUA)
							.append(Component.text("» ").color(NamedTextColor.GRAY))
							.append(Component.text("Invalid timeout").color(NamedTextColor.RED)));
						yield false;
					}
					plugin.getConfig().set("analysis.timeout", timeout);
					plugin.saveConfig();
					sender.sendMessage(Component.text("LoginProtector")
						.color(NamedTextColor.AQUA)
						.append(Component.text("» ").color(NamedTextColor.GRAY))
						.append(Component.text("Timeout updated").color(NamedTextColor.GREEN)));
					yield true;
				}
				catch (NumberFormatException e) {
					sender.sendMessage(Component.text("LoginProtector")
						.color(NamedTextColor.AQUA)
						.append(Component.text("» ").color(NamedTextColor.GRAY))
						.append(Component.text("Invalid timeout").color(NamedTextColor.RED)));
					yield false;
				}
			}
			case "help" -> {
				sender.sendMessage(Component.text("LoginProtector")
					.color(NamedTextColor.AQUA)
					.append(Component.text("» ").color(NamedTextColor.GRAY))
					.append(Component.text("Edit analysis:").color(NamedTextColor.GREEN)));
				sender.sendMessage(Component.text("» ")
					.color(NamedTextColor.GRAY)
					.append(Component.text("/loginprotector edit analysis interval <seconds>")
						.color(NamedTextColor.GREEN))
					.append(Component.text(" - Edit the analysis interval").color(NamedTextColor.GRAY)));
				sender.sendMessage(Component.text("» ")
					.color(NamedTextColor.GRAY)
					.append(Component.text("/loginprotector edit analysis timeout <seconds>")
						.color(NamedTextColor.GREEN))
					.append(Component.text(" - Edit the analysis timeout").color(NamedTextColor.GRAY)));
				yield true;
			}
			default -> false;
		};
	}
	
	private boolean onEditUsernamesCommand(CommandSender sender, String @NotNull [] args) {
		return switch (args.length == 0 ? "help" : args[0].toLowerCase()) {
			case "add" -> {
				if (args.length != 2) {
					sender.sendMessage(Component.text("LoginProtector")
						.color(NamedTextColor.AQUA)
						.append(Component.text("» ").color(NamedTextColor.GRAY))
						.append(Component.text("Missing argument").color(NamedTextColor.RED)));
					yield false;
				}
				List<String> usernames = plugin.getConfig().getStringList("logged-usernames");
				usernames.add(args[1]);
				plugin.getConfig().set("logged-usernames", usernames);
				plugin.saveConfig();
				sender.sendMessage(Component.text("LoginProtector")
					.color(NamedTextColor.AQUA)
					.append(Component.text("» ").color(NamedTextColor.GRAY))
					.append(Component.text("Username added").color(NamedTextColor.GREEN)));
				yield true;
			}
			case "remove" -> {
				if (args.length != 2) {
					sender.sendMessage(Component.text("LoginProtector")
						.color(NamedTextColor.AQUA)
						.append(Component.text("» ").color(NamedTextColor.GRAY))
						.append(Component.text("Missing argument").color(NamedTextColor.RED)));
					yield false;
				}
				try {
					int index = Integer.parseInt(args[1]);
					List<String> usernames = plugin.getConfig().getStringList("logged-usernames");
					if (index < 1 || index > usernames.size()) {
						sender.sendMessage(Component.text("LoginProtector")
							.color(NamedTextColor.AQUA)
							.append(Component.text("» ").color(NamedTextColor.GRAY))
							.append(Component.text("Invalid index").color(NamedTextColor.RED)));
						yield false;
					}
					usernames.remove(index - 1);
					plugin.getConfig().set("logged-usernames", usernames);
					plugin.saveConfig();
					sender.sendMessage(Component.text("LoginProtector")
						.color(NamedTextColor.AQUA)
						.append(Component.text("» ").color(NamedTextColor.GRAY))
						.append(Component.text("Username removed").color(NamedTextColor.GREEN)));
					yield true;
				}
				catch (NumberFormatException e) {
					sender.sendMessage(Component.text("LoginProtector")
						.color(NamedTextColor.AQUA)
						.append(Component.text("» ").color(NamedTextColor.GRAY))
						.append(Component.text("Invalid index").color(NamedTextColor.RED)));
					yield false;
				}
			}
			case "list" -> {
				List<String> usernames = plugin.getConfig().getStringList("logged-usernames");
				sender.sendMessage(Component.text("LoginProtector")
					.color(NamedTextColor.AQUA)
					.append(Component.text("» ").color(NamedTextColor.GRAY))
					.append(Component.text("Usernames:")
						.color(NamedTextColor.GREEN)
						.append(Component.text(usernames.isEmpty() ? " None" : ""))));
				for (int i = 0; i < usernames.size(); i++) {
					sender.sendMessage(Component.text("» ")
						.color(NamedTextColor.GRAY)
						.append(Component.text(i + 1 + ": " + usernames.get(i)).color(NamedTextColor.GREEN)));
				}
				yield true;
			}
			case "help" -> {
				sender.sendMessage(Component.text("LoginProtector")
					.color(NamedTextColor.AQUA)
					.append(Component.text("» ").color(NamedTextColor.GRAY))
					.append(Component.text("Edit usernames:").color(NamedTextColor.GREEN)));
				sender.sendMessage(Component.text("» ")
					.color(NamedTextColor.GRAY)
					.append(Component.text("/loginprotector edit usernames add <username>")
						.color(NamedTextColor.GREEN))
					.append(Component.text(" - Add a username").color(NamedTextColor.GRAY)));
				sender.sendMessage(Component.text("» ")
					.color(NamedTextColor.GRAY)
					.append(Component.text("/loginprotector edit usernames remove <index>")
						.color(NamedTextColor.GREEN))
					.append(Component.text(" - Remove a username").color(NamedTextColor.GRAY)));
				sender.sendMessage(Component.text("» ")
					.color(NamedTextColor.GRAY)
					.append(Component.text("/loginprotector edit usernames list").color(NamedTextColor.GREEN))
					.append(Component.text(" - List all usernames").color(NamedTextColor.GRAY)));
				yield true;
			}
			default -> false;
		};
	}
	
	private boolean onEditWebhookCommand(CommandSender sender, String @NotNull [] args) {
		if (args.length != 1) {
			sender.sendMessage(Component.text("LoginProtector")
				.color(NamedTextColor.AQUA)
				.append(Component.text("» ").color(NamedTextColor.GRAY))
				.append(Component.text("Missing argument").color(NamedTextColor.RED)));
			return false;
		}
		plugin.getConfig().set("webhook", args[0]);
		plugin.saveConfig();
		sender.sendMessage(Component.text("LoginProtector")
			.color(NamedTextColor.AQUA)
			.append(Component.text("» ").color(NamedTextColor.GRAY))
			.append(Component.text("Webhook updated").color(NamedTextColor.GREEN)));
		return true;
	}
	
	@Override
	public @NotNull List<String> tabComplete(
		@NotNull CommandSender sender, @NotNull String label,
		@NotNull String[] args
	) {
		if (args.length == 1) {
			return Stream.of("reload", "version", "edit", "help")
				.filter(s -> s.startsWith(args[0].toLowerCase()))
				.toList();
		}
		if (args.length > 1 && args[0].equalsIgnoreCase("edit")) {
			if (args.length == 2) {
				return Stream.of("usernames", "webhook", "analysis", "help")
					.filter(s -> s.startsWith(args[1].toLowerCase()))
					.toList();
			}
			switch (args[1].toLowerCase()) {
				case "usernames" -> {
					if (args.length == 3) {
						return Stream.of("add", "remove", "list", "help")
							.filter(s -> s.startsWith(args[2].toLowerCase()))
							.toList();
					}
				}
				case "analysis" -> {
					if (args.length == 3) {
						return Stream.of("interval", "timeout", "help")
							.filter(s -> s.startsWith(args[2].toLowerCase()))
							.toList();
					}
				}
			}
		}
		return List.of();
	}
	
}
