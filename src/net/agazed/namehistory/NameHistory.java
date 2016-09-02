package net.agazed.namehistory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

public class NameHistory extends JavaPlugin {

	public void onEnable() {
		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			getLogger().info("Failed to submit metrics!");
		}
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("namehistory")) {
			if (args.length != 1) {
				sender.sendMessage(ChatColor.RED + "Correct usage: /namehistory <player>");
				return true;
			}
			OfflinePlayer target = getServer().getOfflinePlayer(args[0]);
			sender.sendMessage(ChatColor.GRAY + "Attempting to get username history of " + target.getName() + "...");
			request(target.getUniqueId().toString(), sender);
			return true;
		}
		return true;
	}

	public void request(String uuid, CommandSender sender) {
		try {
			URL url = new URL("https://api.mojang.com/user/profiles/" + uuid.replace("-", "") + "/names");
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
			Reader r = new InputStreamReader(con.getInputStream());
			BufferedReader br = new BufferedReader(r);
			JsonParser jp = new JsonParser();
			JsonArray array = (JsonArray) jp.parse(br.readLine());
			for (Object objects : array) {
				JsonObject object = (JsonObject) objects;
				String date;
				if (object.get("changedToAt") != null) {
					SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy");
					sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
					date = sdf.format(Long.parseLong(object.get("changedToAt").toString()));
				} else {
					date = "Original";
				}
				String name = object.get("name").toString().replace("\"", "");
				sender.sendMessage(ChatColor.DARK_AQUA + "- " + name + ChatColor.GRAY + " (" + date + ")");
			}
		} catch (Exception e) {
			sender.sendMessage(ChatColor.RED + "Username does not exist!");
		}
	}
}