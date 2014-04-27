package com.survivorserver.GlobalMarket.Command;

import java.util.List;

import com.survivorserver.GlobalMarket.Lib.MCPCPHelper;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.survivorserver.GlobalMarket.LocaleHandler;
import com.survivorserver.GlobalMarket.Market;
import com.survivorserver.GlobalMarket.MarketStorage;

public class SendCommand extends SubCommand {

    public SendCommand(Market market, LocaleHandler locale) {
        super(market, locale);
    }

    @Override
    public String getCommand() {
        return "send";
    }

    @Override
    public String[] getAliases() {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "globalmarket.send";
    }

    @Override
    public String getHelp() {
        return locale.get("cmd.prefix") + locale.get("cmd.send_syntax") + " " + locale.get("cmd.send_descr");
    }

    @Override
    public boolean allowConsoleSender() {
        return false;
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        String prefix = locale.get("cmd.prefix");
        MarketStorage storageHandler = market.getStorage();
        Player player = (Player) sender;
        String world = player.getWorld().getName();
        int sendDistance = market.getMailboxRadius();
        if (sendDistance > 0) {
            boolean near = false;
            Location playerLoc = player.getLocation();
            List<Location> locs = market.getMailboxLocations();
            for (Location loc : locs) {
                if (loc.getWorld().getName().equalsIgnoreCase(playerLoc.getWorld().getName())) {
                    if (loc.distance(playerLoc) <= sendDistance) {
                        near = true;
                        break;
                    }
                }
            }
            if (!near) {
                player.sendMessage(ChatColor.RED + locale.get("not_near_mailbox"));
                return true;
            }
        }
        if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR && args.length >= 2) {
            if (player.getGameMode() == GameMode.CREATIVE && !market.allowCreative(player)) {
                player.sendMessage(ChatColor.RED + locale.get("not_allowed_while_in_creative"));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(prefix + locale.get("cmd.send_syntax"));
                return true;
            }
            if (args[1].equalsIgnoreCase(player.getName())) {
                sender.sendMessage(prefix + locale.get("cant_mail_to_self"));
                return true;
            }
            OfflinePlayer off = market.getServer().getOfflinePlayer(args[1]);
            if (!off.hasPlayedBefore()) {
                sender.sendMessage(prefix + locale.get("player_not_found", args[1]));
                return true;
            }
            args[1] = off.getName();
            int maxMail = market.getMaxMail(args[1], player.getWorld().getName());
            if (maxMail > 0) {
                if (market.getStorage().getNumMail(args[1], player.getWorld().getName()) >= maxMail) {
                    player.sendMessage(ChatColor.RED + locale.get("full_mailbox_other", args[1]));
                    return true;
                }
            }
            ItemStack toList = player.getItemInHand().clone();
            if (market.mcpcpSupportEnabled()) {
                toList = MCPCPHelper.wrapItemStack(player.getInventory(), player.getInventory().getHeldItemSlot());
            }
            if (market.blacklistMail()) {
                if (market.itemBlacklisted(toList)) {
                    sender.sendMessage(ChatColor.RED + locale.get("item_is_blacklisted_from_mail"));
                    return true;
                }
            }
            int mailTime = market.getMailTime(player);
            if (args.length == 3) {
                int amount;
                try {
                    amount = Integer.parseInt(args[2]);
                } catch(Exception e) {
                    player.sendMessage(ChatColor.RED + locale.get("not_a_valid_number", args[2]));
                    return true;
                }
                if (amount <= 0) {
                    player.sendMessage(ChatColor.RED + locale.get("not_a_valid_amount", args[2]));
                    return true;
                }
                if (player.getItemInHand().getAmount() < amount) {
                    player.sendMessage(ChatColor.RED + locale.get("you_dont_have_x_of_this_item", amount));
                    return true;
                }
                if (player.getItemInHand().getAmount() == amount) {
                    player.setItemInHand(new ItemStack(Material.AIR));
                } else {
                    player.getItemInHand().setAmount(player.getItemInHand().getAmount() - amount);
                }
                toList.setAmount(amount);
                if (mailTime > 0) {
                    market.getStorage().queueMail(args[1], sender.getName(), toList, world);
                    sender.sendMessage(prefix + locale.get("item_will_send", mailTime));
                } else {
                    storageHandler.createMail(args[1], sender.getName(), toList, 0, world);
                    sender.sendMessage(prefix + locale.get("item_sent"));
                    market.notifyPlayer(args[1], market.getLocale().get("you_have_new_mail"));
                }
            } else {
                if (mailTime > 0) {
                    market.getStorage().queueMail(args[1], sender.getName(), toList, world);
                    sender.sendMessage(prefix + locale.get("item_will_send", mailTime));
                } else {
                    storageHandler.createMail(args[1], sender.getName(), toList, 0, world);
                    sender.sendMessage(prefix + locale.get("item_sent"));
                    market.notifyPlayer(args[1], market.getLocale().get("you_have_new_mail"));
                }
                player.setItemInHand(new ItemStack(Material.AIR));
            }
        } else {
            sender.sendMessage(prefix + locale.get("hold_an_item") + " " + locale.get("cmd.send_syntax"));
        }
        return true;
    }
}
