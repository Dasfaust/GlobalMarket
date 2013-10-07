package com.survivorserver.GlobalMarket.Command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import com.survivorserver.GlobalMarket.LocaleHandler;
import com.survivorserver.GlobalMarket.Market;
import com.survivorserver.GlobalMarket.SQL.AsyncDatabase;

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
		FileConfiguration conf = market.getConfig();
		String storageType = conf.getString("storage.type");
		String user = conf.getString("storage.mysql_user");
		String pass = conf.getString("storage.mysql_pass");
		String db = conf.getString("storage.mysql_database");
		String addr = conf.getString("storage.mysql_address");
		int port = conf.getInt("storage.mysql_port");
		market.reloadConfig();
		conf = market.getConfig();
		if (!storageType.equalsIgnoreCase(conf.getString("storage.type"))
				|| !user.equals(conf.getString("storage.mysql_user"))
				|| !pass.equals(conf.getString("storage.mysql_pass"))
				|| !db.equals(conf.getString("storage.mysql_database"))
				|| !addr.equals(conf.getString("storage.mysql_address"))
				|| port != conf.getInt("storage.mysql_port")) {
			AsyncDatabase asyncDb = market.getStorage().getAsyncDb();
			if (asyncDb.isProcessing()) {
				sender.sendMessage(ChatColor.YELLOW + "DB queue is currently running, haulting...");
				while(asyncDb.isProcessing()) { 
					market.setSyncHault(true);
				}
				market.setSyncHault(false);
			}
			asyncDb.processQueue(true);
			asyncDb.close();
			market.initializeStorage();
		}
		market.infiniteSeller = market.getConfig().getString("infinite.seller");
		market.getConfigHandler().reloadLocaleYML();
		locale.setSelected();
		market.buildWorldLinks();
		sender.sendMessage(locale.get("cmd.prefix") + market.getLocale().get("config_reloaded"));
		return true;
	}
}
