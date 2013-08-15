package com.survivorserver.GlobalMarket.Command;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.survivorserver.GlobalMarket.LocaleHandler;
import com.survivorserver.GlobalMarket.Market;

public class PriceCheckCommand extends SubCommand {

	public PriceCheckCommand(Market market, LocaleHandler locale) {
		super(market, locale);
	}

	@Override
	public String getCommand() {
		return "pricecheck";
	}

	@Override
	public String[] getAliases() {
		return new String[] {"price", "pc"};
	}

	@Override
	public String getPermissionNode() {
		return "globalmarket.pricecheck";
	}

	@Override
	public String getHelp() {
		return locale.get("cmd.prefix") + locale.get("cmd.pc_syntax") + " " + locale.get("cmd.pc_descr");
	}

	@Override
	public boolean allowConsoleSender() {
		return false;
	}

	@Override
	public boolean onCommand(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR) {
			ItemStack item = player.getItemInHand();
			sender.sendMessage(market.getPrices().getPricesInformation(item));
		}
		return true;
	}

}
