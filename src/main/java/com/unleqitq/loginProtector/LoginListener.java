package com.unleqitq.loginProtector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LoginListener extends PacketAdapter {
	
	private static final Logger log = LoggerFactory.getLogger(LoginListener.class);
	
	private final Map<InetAddress, Pair<LoginData, Long>> loginAttempts = new HashMap<>();
	private BukkitTask task;
	
	public LoginListener(Plugin plugin) {
		super(plugin, PacketType.Login.Client.START, PacketType.Login.Client.ENCRYPTION_BEGIN);
	}
	
	public void setup() {
		ProtocolLibrary.getProtocolManager().addPacketListener(this);
		task = getPlugin().getServer().getScheduler().runTaskTimerAsynchronously(
			getPlugin(), this::update, 0, plugin.getConfig().getLong("analysis.interval", 5) * 20L
		);
	}
	
	public void reload() {
		task.cancel();
		task = getPlugin().getServer().getScheduler().runTaskTimerAsynchronously(
			getPlugin(), this::update, 0, plugin.getConfig().getLong("analysis.interval", 5) * 20L
		);
	}
	
	public void shutdown() {
		ProtocolLibrary.getProtocolManager().removePacketListener(this);
		task.cancel();
	}
	
	@Override
	public void onPacketReceiving(PacketEvent event) {
		if (event.getPacketType() == PacketType.Login.Client.START) {
			PacketContainer packet = event.getPacket();
			UUID uuid = packet.getUUIDs().read(0);
			Player tempPlayer = event.getPlayer();
			InetSocketAddress address = tempPlayer.getAddress();
			assert address != null;
			InetAddress ip = address.getAddress();
			String username = packet.getStrings().read(0);
			if (ip != null) {
				loginAttempts.put(
					ip, Pair.of(new LoginData(uuid, username, ip), System.currentTimeMillis()));
			}
			String webhook = getPlugin().getConfig().getString("webhook");
			List<String> loggedUsernames =
				getPlugin().getConfig().getStringList("logged-usernames"); // Usernames are patterns
			if (loggedUsernames.stream().noneMatch(username::matches)) {
				return;
			}
			if (ip == null) {
				log.warn(
					"Player {} with UUID {} has a null IP address (host: {})", username, uuid,
					address.getHostString()
				);
				DiscordWebhookHelper.Embed embed = DiscordWebhookHelper.Embed.builder()
					.title("Login attempt")
					.color(Color.RED)
					.timestamp(Instant.now())
					.field(
						DiscordWebhookHelper.EmbedField.builder().name("Username").value(username).inline(true)
							.build())
					.field(DiscordWebhookHelper.EmbedField.builder()
						.name("UUID")
						.value(uuid.toString())
						.inline(true)
						.build())
					.field(DiscordWebhookHelper.EmbedField.builder()
						.name("Host")
						.value(address.getHostString())
						.inline(true)
						.build())
					.footer(DiscordWebhookHelper.EmbedFooter.builder().text("LoginProtector")
						.build())
					.author(DiscordWebhookHelper.EmbedAuthor.builder().name("LoginProtector")
						.build())
					.build();
				try {
					int response = DiscordWebhookHelper.sendWebhookEmbed(
						webhook, null,
						new DiscordWebhookHelper.Embed[] {embed}
					);
				}
				catch (RuntimeException e) {
					log.error("Failed to send webhook: {}", e.getMessage());
				}
				return;
			}
			DiscordWebhookHelper.Embed embed = DiscordWebhookHelper.Embed.builder()
				.title("Login attempt")
				.color(Color.RED)
				.timestamp(Instant.now())
				.field(DiscordWebhookHelper.EmbedField.builder()
					.name("IP")
					.value(ip.getHostAddress())
					.inline(true)
					.build())
				.field(
					DiscordWebhookHelper.EmbedField.builder().name("Username").value(username).inline(true)
						.build())
				.field(
					DiscordWebhookHelper.EmbedField.builder().name("UUID").value(uuid.toString()).inline(true)
						.build())
				.footer(DiscordWebhookHelper.EmbedFooter.builder().text("LoginProtector")
					.build())
				.author(DiscordWebhookHelper.EmbedAuthor.builder().name("LoginProtector")
					.build())
				.build();
			try {
				int response = DiscordWebhookHelper.sendWebhookEmbed(
					webhook, null,
					new DiscordWebhookHelper.Embed[] {embed}
				);
			}
			catch (RuntimeException e) {
				log.error("Failed to send webhook: {}", e.getMessage());
			}
		}
		else if (event.getPacketType() == PacketType.Login.Client.ENCRYPTION_BEGIN) {
			InetSocketAddress address = event.getPlayer().getAddress();
			assert address != null;
			InetAddress ip = address.getAddress();
			if (ip != null) {
				loginAttempts.remove(ip);
			}
		}
	}
	
	public void update() {
		long now = System.currentTimeMillis();
		long timeout = plugin.getConfig().getLong("analysis.timeout", 30) * 1000;
		List<Pair<LoginData, Long>> suspicious = loginAttempts.values().stream()
			.filter(pair -> now - pair.right() > timeout)
			.toList();
		for (Pair<LoginData, Long> pair : suspicious) {
			LoginData data = pair.left();
			log.warn(
				"Player {} with UUID {} has been trying to login for {} seconds from {}",
				data.username(), data.uuid(), (now - pair.right()) / 1000, data.address()
			);
			String webhook = getPlugin().getConfig().getString("webhook");
			DiscordWebhookHelper.Embed embed = DiscordWebhookHelper.Embed.builder()
				.title("Suspicious login attempt")
				.description("This player has started the login process but has not completed it.")
				.color(Color.RED)
				.timestamp(Instant.now())
				.field(DiscordWebhookHelper.EmbedField.builder()
					.name("IP")
					.value(data.address().getHostAddress())
					.inline(true)
					.build())
				.field(
					DiscordWebhookHelper.EmbedField.builder().name("Username").value(data.username()).inline(true)
						.build())
				.field(
					DiscordWebhookHelper.EmbedField.builder().name("UUID").value(data.uuid().toString()).inline(true)
						.build())
				.field(DiscordWebhookHelper.EmbedField.builder()
					.name("Elapsed time")
					.value(String.valueOf((now - pair.right()) / 1000))
					.inline(true)
					.build())
				.footer(DiscordWebhookHelper.EmbedFooter.builder().text("LoginProtector")
					.build())
				.author(DiscordWebhookHelper.EmbedAuthor.builder().name("LoginProtector")
					.build())
				.build();
			try {
				int response = DiscordWebhookHelper.sendWebhookEmbed(
					webhook, null,
					new DiscordWebhookHelper.Embed[] {embed}
				);
			}
			catch (RuntimeException e) {
				log.error("Failed to send webhook: {}", e.getMessage());
			}
			loginAttempts.remove(data.address());
		}
	}
	
	private record LoginData(UUID uuid, String username, InetAddress address) {}
	
}
