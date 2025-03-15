package com.unleqitq.loginProtector;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Builder;
import lombok.Singular;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

public class DiscordWebhookHelper {
	
	private DiscordWebhookHelper() {
		throw new IllegalStateException("Utility class");
	}
	
	private static int sendWebhook(String webhookUrl, @NotNull JsonObject body) {
		try {
			HttpURLConnection connection =
				(HttpURLConnection) new URI(webhookUrl).toURL().openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			try (OutputStream os = connection.getOutputStream()) {
				byte[] input = body.toString().getBytes(StandardCharsets.UTF_8);
				os.write(input, 0, input.length);
			}
			int responseCode = connection.getResponseCode();
			connection.disconnect();
			return responseCode;
		}
		catch (IOException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static int sendWebhookMessage(
		String webhookUrl, @Nullable String username, @NotNull String message) {
		JsonObject body = new JsonObject();
		body.addProperty("content", message);
		if (username != null) {
			body.addProperty("username", username);
		}
		return sendWebhook(webhookUrl, body);
	}
	
	public static int sendWebhookEmbed(
		String webhookUrl, @Nullable String username, @NotNull Embed @NotNull [] embeds) {
		JsonObject body = new JsonObject();
		JsonArray embedsArray = new JsonArray();
		for (Embed embed : embeds) {
			embedsArray.add(embed.toJson());
		}
		body.add("embeds", embedsArray);
		if (username != null) {
			body.addProperty("username", username);
		}
		return sendWebhook(webhookUrl, body);
	}
	
	@Builder
	public record Embed(
		@Nullable String title,
		@Nullable String description,
		@Nullable String url,
		@Nullable Instant timestamp,
		@Nullable Color color,
		@Nullable EmbedFooter footer,
		@Nullable EmbedAuthor author,
		@Singular
		List<@NotNull EmbedField> fields
	) {
		
		@NotNull
		public JsonObject toJson() {
			JsonObject json = new JsonObject();
			json.addProperty("type", "rich");
			if (title != null) {
				json.addProperty("title", title);
			}
			if (description != null) {
				json.addProperty("description", description);
			}
			if (url != null) {
				json.addProperty("url", url);
			}
			if (timestamp != null) {
				json.addProperty("timestamp", timestamp.toString());
			}
			if (color != null) {
				json.addProperty("color", color.getRGB() & 0xFFFFFF);
			}
			if (footer != null) {
				json.add("footer", footer.toJson());
			}
			if (author != null) {
				json.add("author", author.toJson());
			}
			if (fields != null) {
				JsonArray fieldsArray = new JsonArray();
				for (EmbedField field : fields) {
					fieldsArray.add(field.toJson());
				}
				json.add("fields", fieldsArray);
			}
			return json;
		}
		
	}
	
	@Builder
	public record EmbedFooter(@NotNull String text, @Nullable String iconUrl) {
		
		@NotNull
		public JsonObject toJson() {
			JsonObject json = new JsonObject();
			json.addProperty("text", text);
			if (iconUrl != null) {
				json.addProperty("icon_url", iconUrl);
			}
			return json;
		}
		
	}
	
	@Builder
	public record EmbedAuthor(
		@NotNull String name, @Nullable String url, @Nullable String iconUrl
	) {
		
		@NotNull
		public JsonObject toJson() {
			JsonObject json = new JsonObject();
			json.addProperty("name", name);
			if (url != null) {
				json.addProperty("url", url);
			}
			if (iconUrl != null) {
				json.addProperty("icon_url", iconUrl);
			}
			return json;
		}
		
	}
	
	@Builder
	public record EmbedField(
		@NotNull String name, @NotNull String value, boolean inline
	) {
		
		@NotNull
		public JsonObject toJson() {
			JsonObject json = new JsonObject();
			json.addProperty("name", name);
			json.addProperty("value", value);
			json.addProperty("inline", inline);
			return json;
		}
		
	}
	
}
