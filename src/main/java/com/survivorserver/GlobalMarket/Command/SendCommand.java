package com.survivorserver.GlobalMarket.Command;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.survivorserver.GlobalMarket.LocaleHandler;
import com.survivorserver.GlobalMarket.Market;
import com.survivorserver.GlobalMarket.MarketQueue;
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
		MarketQueue queue = market.getQueue();
		Player player = (Player) sender;
		if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR && args.length >= 2) {
			if (market.blacklistMail()) {
				if (market.itemBlacklisted(player.getItemInHand())) {
					sender.sendMessage(ChatColor.RED + locale.get("item_is_blacklisted_from_mail"));
					return true;
				}
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
			if (args.length == 3) {
				int amount = 0;
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
				ItemStack toList = new ItemStack(player.getItemInHand());
				if (player.getItemInHand().getAmount() == amount) {
					player.setItemInHand(new ItemStack(Material.AIR));
				} else {
					player.getItemInHand().setAmount(player.getItemInHand().getAmount() - amount);
				}
				toList.setAmount(amount);
				if (market.getTradeTime() > 0 && !sender.hasPermission("globalmarket.noqueue")) {
					queue.queueMail(toList, args[1], sender.getName());
					sender.sendMessage(prefix + locale.get("item_will_send", market.getTradeTime()));
				} else {
					storageHandler.storeMail(toList, args[1], sender.getName(), true);
					sender.sendMessage(prefix + locale.get("item_sent"));
				}
			} else {
				ItemStack toList = new ItemStack(player.getItemInHand());
				if (market.getTradeTime() > 0 && !sender.hasPermission("globalmarket.noqueue")) {
					queue.queueMail(toList, args[1], sender.getName());
					sender.sendMessage(prefix + locale.get("item_will_send", market.getTradeTime()));
				} else {
					storageHandler.storeMail(toList, args[1], sender.getName(), true);
					sender.sendMessage(prefix + locale.get("item_sent"));
				}
				player.setItemInHand(new ItemStack(Material.AIR));
			}
		} else {
			sender.sendMessage(prefix + locale.get("hold_an_item") + " " + locale.get("cmd.send_syntax"));
		}
		return true;
	}
}
