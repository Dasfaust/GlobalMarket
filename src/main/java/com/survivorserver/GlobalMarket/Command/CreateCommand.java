package com.survivorserver.GlobalMarket.Command;

import java.util.ArrayList;
import java.util.List;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.survivorserver.GlobalMarket.LocaleHandler;
import com.survivorserver.GlobalMarket.Market;
import com.survivorserver.GlobalMarket.MarketQueue;
import com.survivorserver.GlobalMarket.MarketStorage;

public class CreateCommand extends SubCommand {

	public CreateCommand(Market market, LocaleHandler locale) {
		super(market, locale);
	}

	@Override
	public String getCommand() {
		return "create";
	}
	
	@Override
	public String[] getAliases() {
		return new String[] {"list", "sell", "add"};
	}
	
	@Override
	public String getPermissionNode() {
		return "globalmarket.create";
	}
	
	@Override
	public String getHelp() {
		return locale.get("cmd.prefix") + locale.get("cmd.create_syntax") + " " + locale.get("cmd.create_descr");
	}
	
	@Override
	public boolean allowConsoleSender() {
		return false;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, String[] args) {
		String prefix = locale.get("cmd.prefix");
		MarketStorage storageHandler = market.getStorage();
		Economy econ = market.getEcon();
		MarketQueue queue = market.getQueue();
		Player player = (Player) sender;
		if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR && args.length >= 2) {
			if (market.itemBlacklisted(player.getItemInHand())) {
				sender.sendMessage(ChatColor.RED + locale.get("item_is_blacklisted"));
				return true;
			}
			double price = 0;
			try {
				price = Double.parseDouble(args[1]);
			} catch(Exception e) {
				player.sendMessage(ChatColor.RED + locale.get("not_a_valid_number", args[1]));
				return true;
			}
			if (price < 0.01) {
				sender.sendMessage(prefix + locale.get("price_too_low"));
				return true;
			}
			double maxPrice = market.getMaxPrice();
			if (maxPrice > 0 && price > maxPrice && !sender.hasPermission("globalmarket.nolimit.maxprice")) {
				sender.sendMessage(prefix + locale.get("price_too_high"));
				return true;
			}
			double fee = market.getCreationFee(player, price);
			if (market.maxListings() > 0 && storageHandler.getNumListings(sender.getName()) >= market.maxListings() && !sender.hasPermission("globalmarket.nolimit.maxlistings")) {
				sender.sendMessage(ChatColor.RED + locale.get("selling_too_many_items"));
				return true;
			}
			List<String> extraArgs = new ArrayList<String>();
			for (int i = 0; i < args.length; i++) {
				if (args[i].startsWith("-")) {
					extraArgs.add(args[i]);
				}
			}
			boolean infinite = false;
			if (extraArgs.contains("-inf") && sender.hasPermission("globalmarket.infinite")) {
				infinite = true;
			}
			int amount = 0;
			if ((args.length == 3 && extraArgs.isEmpty()) || (args.length == 4 && !extraArgs.isEmpty())) {
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
				if (!infinite && player.getItemInHand().getAmount() < amount) {
					player.sendMessage(ChatColor.RED + locale.get("you_dont_have_x_of_this_item", amount));
					return true;
				}
			}
			ItemStack toList = new ItemStack(player.getItemInHand());
			if (fee > 0) {
				if (econ.has(sender.getName(), fee)) {
					econ.withdrawPlayer(sender.getName(), fee);
					storageHandler.incrementSpent(sender.getName(), fee);
					player.sendMessage(ChatColor.GREEN + locale.get("charged_fee", econ.format(fee)));
				} else {
					sender.sendMessage(ChatColor.RED + locale.get("you_cant_pay_this_fee"));
					return true;
				}
			}
			if (amount > 0) {
				if (player.getItemInHand().getAmount() == amount) {
					if (!infinite) {
						player.setItemInHand(new ItemStack(Material.AIR));
					}
				} else {
					if (!infinite) {
						player.getItemInHand().setAmount(player.getItemInHand().getAmount() - amount);
					}
				}
				toList.setAmount(amount);
			} else {
				player.setItemInHand(new ItemStack(Material.AIR));
			}
			if (market.getTradeTime() > 0 && !sender.hasPermission("globalmarket.noqueue")) {
				queue.queueListing(toList, player.getName(), price);
				sender.sendMessage(ChatColor.GREEN + locale.get("item_queued", market.getTradeTime()));
			} else {
				storageHandler.storeListing(toList, infinite ? market.getInfiniteSeller() : player.getName(), price);
				sender.sendMessage(ChatColor.GREEN + locale.get("item_listed"));
			}
			String itemName = market.getItemName(toList);
			storageHandler.storeHistory(player.getName(), locale.get("history.item_listed", itemName, price));
		} else {
			sender.sendMessage(prefix + locale.get("hold_an_item") + " " + locale.get("cmd.create_syntax"));
		}
		return true;
	}
}
