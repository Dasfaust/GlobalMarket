package com.survivorserver.GlobalMarket.Command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.survivorserver.GlobalMarket.LocaleHandler;
import com.survivorserver.GlobalMarket.Market;

public class MailCommand extends SubCommand {

	public MailCommand(Market market, LocaleHandler locale) {
		super(market, locale);
	}

	@Override
	public String getCommand() {
		return "mail";
	}

	@Override
	public String[] getAliases() {
		return null;
	}

	@Override
	public String getPermissionNode() {
		return "globalmarket.quicklist";
	}

	@Override
	public String getHelp() {
		return locale.get("cmd.prefix") + locale.get("cmd.mail_syntax") + " " + locale.get("cmd.mail_descr");
	}

	@Override
	public boolean allowConsoleSender() {
		return false;
	}

	@Override
	public boolean onCommand(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		market.getInterfaceHandler().openInterface(player, null, "Mail");
		return true;
	}

}
