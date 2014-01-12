package com.survivorserver.GlobalMarket.Lib;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

public class BossMessage {

	private Plugin plugin;
	private Map<String, BukkitTask> active;
	private static int MOB_ID = 1234;
	private ProtocolManager manager;
	
	public BossMessage(Plugin plugin) {
		this.plugin = plugin;
		active = new ConcurrentHashMap<String, BukkitTask>();
		manager = ProtocolLibrary.getProtocolManager();
	}
	
	@SuppressWarnings("deprecation")
	public boolean display(Player player, String message, int ticks) {		
		try {
			if (active.containsKey(player.getName())) {
				active.get(player.getName()).cancel();
				PacketContainer packet = manager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
				packet.getIntegerArrays().write(0, new int[] {MOB_ID});
				manager.sendServerPacket(player, packet);
				active.remove(player.getName());
			}
			
			Location loc = player.getLocation();
			
			PacketContainer packet = manager.createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
			packet.getIntegers()
			.write(0, MOB_ID)
			.write(1, (int) EntityType.WITHER.getTypeId())
			.write(2, (int) Math.floor(loc.getBlockX() * 32.0D))
			.write(3, (int) Math.floor((loc.getBlockY() - 4) * 32.0D))
			.write(4, (int) Math.floor(loc.getBlockZ() * 32.0D));
			
			WrappedDataWatcher watcher = new WrappedDataWatcher();
			watcher.setObject(0, (Byte) (byte) 0x20);
			watcher.setObject(6, (Float) (float) 1);
			watcher.setObject(10, (String) message);
			watcher.setObject(11, (Byte) (byte) 1);
			
			packet.getDataWatcherModifier().write(0, watcher);
			
			manager.sendServerPacket(player, packet);
			
			final String name = player.getName();
			active.put(player.getName(), new BukkitRunnable() {
				@Override
				public void run() {
					Player player = plugin.getServer().getPlayer(name);
					if (player != null) {
						PacketContainer packet = manager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
						packet.getIntegerArrays().write(0, new int[] {MOB_ID});
						try {
							manager.sendServerPacket(player, packet);
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						}
					}
					active.remove(name);
				}
			}.runTaskLaterAsynchronously(plugin, ticks));
		} catch(Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public void clearPlayer(Player player) {
		if (active.containsKey(player.getName())) {
			active.get(player.getName()).cancel();
			PacketContainer packet = manager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
			packet.getIntegerArrays().write(0, new int[] {MOB_ID});
			try {
				manager.sendServerPacket(player, packet);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			active.remove(player.getName());
		}
	}
}