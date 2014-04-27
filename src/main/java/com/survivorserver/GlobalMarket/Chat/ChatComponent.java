package com.survivorserver.GlobalMarket.Chat;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.survivorserver.GlobalMarket.Market;

public class ChatComponent {

    private Market market;
    private TellRawComponent tellraw;

    public ChatComponent(Market market) {
        this.market = market;

        try {
            Class.forName("net.minecraft.server.v1_7_R2.ChatSerializer");
            tellraw = new TellRawComponent();
        } catch(Exception ignored) {}
    }

    public void announce(TellRawMessage message) {
        for (World world : market.getServer().getWorlds()) {
            for (Player player : world.getPlayers()) {
                if (tellraw != null) {
                    tellraw.send(player, message);
                } else {
                    player.sendMessage(buildMessage(message));
                }
            }
        }
    }

    public void announce(TellRawMessage message, String perm) {
        for (World world : market.getServer().getWorlds()) {
            for (Player player : world.getPlayers()) {
                if (player.hasPermission(perm)) {
                    if (tellraw != null) {
                        tellraw.send(player, message);
                    } else {
                        player.sendMessage(buildMessage(message));
                    }
                }
            }
        }
    }

    public void send(Player player, TellRawMessage message) {
        if (tellraw != null) {
            tellraw.send(player, message);
        } else {
            player.sendMessage(buildMessage(message));
        }
    }

    public String buildMessage(TellRawMessage msg) {
        StringBuilder builder = new StringBuilder();
        format(msg, builder);
        if (msg.extra != null) {
            buildExtra(msg, builder);
        }
        return ChatColor.translateAlternateColorCodes('&', builder.toString());
    }

    private void buildExtra(TellRawMessage msg, StringBuilder builder) {
        if (msg.extra != null) {
            for (TellRawMessage ext : msg.extra) {
                format(ext, builder);
            }
            for (TellRawMessage ext : msg.extra) {
                if (ext.extra != null) {
                    for (TellRawMessage child : ext.extra) {
                        format(child, builder);
                    }
                }
            }
        }
    }

    private void format(TellRawMessage msg, StringBuilder builder) {
        if (msg.color != null) {
            if (msg.color.equalsIgnoreCase("blue")) {
                builder.append("&9");
            } else if (msg.color.equalsIgnoreCase("black")) {
                builder.append("&0");
            } else if (msg.color.equalsIgnoreCase("dark_blue")) {
                builder.append("&1");
            } else if (msg.color.equalsIgnoreCase("dark_green")) {
                builder.append("&2");
            } else if (msg.color.equalsIgnoreCase("dark_aqua")) {
                builder.append("&3");
            } else if (msg.color.equalsIgnoreCase("dark_red")) {
                builder.append("&4");
            } else if (msg.color.equalsIgnoreCase("dark_purple")) {
                builder.append("&5");
            } else if (msg.color.equalsIgnoreCase("dark_gray")) {
                builder.append("&8");
            } else if (msg.color.equalsIgnoreCase("gold")) {
                builder.append("&6");
            } else if (msg.color.equalsIgnoreCase("gray")) {
                builder.append("&7");
            } else if (msg.color.equalsIgnoreCase("green")) {
                builder.append("&a");
            } else if (msg.color.equalsIgnoreCase("white")) {
                builder.append("&f");
            } else if (msg.color.equalsIgnoreCase("red")) {
                builder.append("&c");
            } else if (msg.color.equalsIgnoreCase("light_purple")) {
                builder.append("&d");
            } else if (msg.color.equalsIgnoreCase("yellow")) {
                builder.append("&e");
            }
        }
        if (msg.bold) {
            builder.append("&l");
        }
        if (msg.italic) {
            builder.append("&o");
        }
        if (msg.underlined) {
            builder.append("&n");
        }
        if (msg.obfuscated) {
            builder.append("&k");
        }
        if (msg.strikethrough) {
            builder.append("&m");
        }
        builder.append(msg.text);
    }

    public String jsonStack(ItemStack item) {
        if (tellraw != null) {
            return tellraw.gson.toJson(new TellRawItemStack(item)).replaceAll("\"", "");
        }
        return "";
    }

    public class TellRawComponent {

        org.bukkit.craftbukkit.libs.com.google.gson.Gson gson;

        public TellRawComponent() {
            gson = new org.bukkit.craftbukkit.libs.com.google.gson.Gson();
        }

        public void send(Player player, TellRawMessage msg) {
            /*String json = gson.toJson(msg);
            net.minecraft.server.v1_7_R3.IChatBaseComponent comp = net.minecraft.server.v1_7_R3.ChatSerializer.a(json);
            ((org.bukkit.craftbukkit.v1_7_R3.entity.CraftPlayer) player).getHandle().playerConnection.sendPacket(new net.minecraft.server.v1_7_R3.PacketPlayOutChat(comp, true));*/
        }
    }
}
