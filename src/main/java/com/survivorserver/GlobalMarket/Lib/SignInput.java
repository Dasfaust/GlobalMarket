package com.survivorserver.GlobalMarket.Lib;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

public class SignInput {
	
	private ProtocolManager manager;
	private Map<String, InputResult> results;
	
	protected SignInput(Plugin plugin) {
		results = new HashMap<String, InputResult>();
		manager = ProtocolLibrary.getProtocolManager();
		manager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.HIGH, new PacketType[] {PacketType.Play.Client.UPDATE_SIGN, PacketType.Play.Client.CLOSE_WINDOW}) {
			
			@Override
			public void onPacketReceiving(PacketEvent event) {
				Player player = event.getPlayer();
				PacketContainer packet = event.getPacket();
				if (!results.containsKey(player.getName())) {
					return;
				}
				InputResult result = results.remove(player.getName());
				if (packet.getType() == PacketType.Play.Client.CLOSE_WINDOW) {
					result.finished(player, null, true);
				} else {
					String[] input = packet.getStringArrays().getValues().get(0);
					result.finished(player, input, false);
				}
			}
		});
	}
	
	public boolean create(Player player, String[] placeholder, InputResult result) {
		Location loc = player.getLocation();
		int x = loc.getBlockX(), y = 0, z = loc.getBlockZ();
		
		try {
			// Set
			PacketContainer packet = manager.createPacket(PacketType.Play.Server.BLOCK_CHANGE);
			packet.getIntegers().write(0, x).write(1, y).write(2, z).write(3, 0);
			packet.getBlocks().write(0, Material.SIGN_POST);
			manager.sendServerPacket(player, packet);
			
			// Update
			packet = manager.createPacket(PacketType.Play.Server.UPDATE_SIGN);
			packet.getIntegers().write(0, x).write(1, y).write(2, z);
			packet.getStringArrays().write(0, placeholder);
			manager.sendServerPacket(player, packet);
			
			// Edit
			packet = manager.createPacket(PacketType.Play.Server.OPEN_SIGN_ENTITY);
			packet.getIntegers().write(0, x).write(1, y).write(2, z);
			manager.sendServerPacket(player, packet);
			
			if (results.containsKey(player.getName())) {
				results.remove(player.getName());
			}
			
			results.put(player.getName(), result);
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public abstract class InputResult {
		
		public abstract void finished(Player player, String[] input, boolean cancelled);
	}
}
