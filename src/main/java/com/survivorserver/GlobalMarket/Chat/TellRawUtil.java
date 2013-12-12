package com.survivorserver.GlobalMarket.Chat;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TellRawUtil {

	// TODO won't work below 1.7, need to compensate
	public static void announce(Plugin plugin, TellRawMessage message) {
		try {
			String json = new net.minecraft.util.com.google.gson.Gson().toJson(message);
			plugin.getLogger().info(json);
			net.minecraft.server.v1_7_R1.IChatBaseComponent comp = net.minecraft.server.v1_7_R1.ChatSerializer.a(json);
			for (World world : plugin.getServer().getWorlds()) {
				for (Player player : world.getPlayers()) {
					((org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer) player).getHandle().playerConnection.sendPacket(new net.minecraft.server.v1_7_R1.PacketPlayOutChat(comp, true));
				}
			}
		} catch(Exception ignored) {}
	}
	
	// TODO won't work below 1.7, need to compensate
	public static void send(Player player, TellRawMessage message) {
		try {
			String json = new net.minecraft.util.com.google.gson.Gson().toJson(message);
			net.minecraft.server.v1_7_R1.IChatBaseComponent comp = net.minecraft.server.v1_7_R1.ChatSerializer.a(json);
			((org.bukkit.craftbukkit.v1_7_R1.entity.CraftPlayer) player).getHandle().playerConnection.sendPacket(new net.minecraft.server.v1_7_R1.PacketPlayOutChat(comp, true));
		} catch(Exception ignored) {}
	}
}
