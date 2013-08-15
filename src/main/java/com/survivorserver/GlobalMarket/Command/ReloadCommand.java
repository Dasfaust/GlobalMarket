package com.survivorserver.GlobalMarket.Command;

import org.bukkit.command.CommandSender;

import com.survivorserver.GlobalMarket.LocaleHandler;
import com.survivorserver.GlobalMarket.Market;

public class ReloadCommand extends SubCommand {
	
	public ReloadCommand(Market market, LocaleHandler locale) {
		super(market, locale);
	}

	@Override
	public String getCommand() {
		return "reload";
	}

	@Override
	public String[] getAliases() {
		return null;
	}
	
	@Override
	public String getPermissionNode() {
		return "globalmarket.admin";
	}

	@Override
	public String getHelp() {
		return locale.get("cmd.prefix") + locale.get("cmd.reload_syntax") + " " + locale.get("cmd.reload_descr");
	}

	@Override
	public boolean allowConsoleSender() {
		return true;
	}

	@Override
	public boolean onCommand(CommandSender sender, String[] args) {
		market.reloadConfig();
		market.getConfigHandler().reloadLocaleYML();
		locale.setSelected();
		market.infiniteSeller = market.getConfig().getString("infinite.seller");
		sender.sendMessage(locale.get("cmd.prefix") + market.getLocale().get("config_reloaded"));
		return true;
	}
}
