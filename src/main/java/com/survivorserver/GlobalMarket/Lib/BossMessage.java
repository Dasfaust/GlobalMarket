package com.survivorserver.GlobalMarket.Lib;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.survivorserver.GlobalMarket.Lib.PacketWrapper.MC1_7.WrapperPlayServerSpawnEntityLiving;
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

            WrappedDataWatcher watcher = new WrappedDataWatcher();
            watcher.setObject(0, (byte) 0x20);
            watcher.setObject(6, (float) 1);
            watcher.setObject(10, message);
            watcher.setObject(11, (byte) 1);

            WrapperPlayServerSpawnEntityLiving spawnEntity = new WrapperPlayServerSpawnEntityLiving();
            spawnEntity.setEntityID(MOB_ID);
            spawnEntity.setType(EntityType.ENDER_DRAGON);
            spawnEntity.setX(loc.getX());
            spawnEntity.setY(loc.getY() - 200);
            spawnEntity.setZ(loc.getZ());
            spawnEntity.setMetadata(watcher);

            manager.sendServerPacket(player, spawnEntity.getHandle());

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