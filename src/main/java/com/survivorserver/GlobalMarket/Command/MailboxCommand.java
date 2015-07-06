package com.survivorserver.GlobalMarket.Command;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.survivorserver.GlobalMarket.LocaleHandler;
import com.survivorserver.GlobalMarket.Market;

import java.util.HashSet;

public class MailboxCommand extends SubCommand {

    public MailboxCommand(Market market, LocaleHandler locale) {
        super(market, locale);
    }

    @Override
    public String getCommand() {
        return "mailbox";
    }

    @Override
    public String[] getAliases() {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "globalmarket.util.mailbox";
    }

    @Override
    public String getHelp() {
        return locale.get("cmd.prefix") + locale.get("cmd.mailbox_syntax") + " " + locale.get("cmd.mailbox_descr");
    }

    @Override
    public boolean allowConsoleSender() {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        Location location = null;
        Block block = player.getTargetBlock((HashSet<Byte>) null, 6);
        if (block.getType() == Material.CHEST
                // Trapped chest
                || block.getTypeId() == 146
                || block.getType() == Material.SIGN
                || block.getType() == Material.SIGN_POST
                || block.getType() == Material.WALL_SIGN) {
            location = block.getLocation();
        } else {
            player.sendMessage(ChatColor.RED + locale.get("aim_cursor_at_chest_or_sign"));
            return true;
        }
        String loc = market.locationToString(location);
        if (args.length == 2 && args[1].equalsIgnoreCase("remove")) {
            if (market.getConfig().isSet("mailbox." + loc)) {
                market.getConfig().set("mailbox." + loc, null);
                market.saveConfig();
                player.sendMessage(ChatColor.YELLOW + locale.get("mailbox_removed"));
                return true;
            } else {
                player.sendMessage(ChatColor.RED + locale.get("no_mailbox_found"));
                return true;
            }
        }
        if (market.getConfig().isSet("mailbox." + loc)) {
            sender.sendMessage(ChatColor.RED + locale.get("mailbox_already_exists"));
            return true;
        }
        market.getConfig().set("mailbox." + loc, true);
        market.saveConfig();
        sender.sendMessage(ChatColor.GREEN + locale.get("mailbox_added"));
        return true;
    }
}
