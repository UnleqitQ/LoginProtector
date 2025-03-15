package com.unleqitq.loginProtector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class LoginListener extends PacketAdapter {
	
	private static final Logger log = LoggerFactory.getLogger(LoginListener.class);
	
	public LoginListener(Plugin plugin) {
		super(plugin, PacketType.Login.Client.START);
	}
	
	@Override
	public void onPacketReceiving(PacketEvent event) {
		if (event.getPacketType() == PacketType.Login.Client.START) {
			PacketContainer packet = event.getPacket();
			String username = packet.getStrings().read(0);
			String webhook = getPlugin().getConfig().getString("webhook");
			List<String> loggedUsernames =
				getPlugin().getConfig().getStringList("logged-usernames"); // Usernames are patterns
			if (loggedUsernames.stream().noneMatch(username::matches)) {
				return;
			}
			UUID uuid = packet.getUUIDs().read(0);
			Player tempPlayer = event.getPlayer();
			InetSocketAddress address = tempPlayer.getAddress();
			assert address != null;
			InetAddress ip = address.getAddress();
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
	}
	
}
