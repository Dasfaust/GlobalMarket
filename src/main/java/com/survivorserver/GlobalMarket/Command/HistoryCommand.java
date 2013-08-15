package com.survivorserver.GlobalMarket.Command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.survivorserver.GlobalMarket.LocaleHandler;
import com.survivorserver.GlobalMarket.Market;

public class HistoryCommand extends SubCommand {

	public HistoryCommand(Market market, LocaleHandler locale) {
		super(market, locale);
	}

	@Override
	public String getCommand() {
		return "history";
	}

	@Override
	public String[] getAliases() {
		return null;
	}

	@Override
	public String getPermissionNode() {
		return "globalmarket.history";
	}

	@Override
	public String getHelp() {
		return locale.get("cmd.prefix") + locale.get("cmd.history_syntax") + " " + locale.get("cmd.history_descr");
	}

	@Override
	public boolean allowConsoleSender() {
		return false;
	}

	@Override
	public boolean onCommand(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		market.getCore().showHistory(player);
		sender.sendMessage(ChatColor.GREEN + locale.get("check_your_inventory"));
		return true;
	}

}
