package com.survivorserver.GlobalMarket.Command;

import java.util.Collection;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.survivorserver.GlobalMarket.Listing;
import com.survivorserver.GlobalMarket.LocaleHandler;
import com.survivorserver.GlobalMarket.Market;
import com.survivorserver.GlobalMarket.SQL.AsyncDatabase;
import com.survivorserver.GlobalMarket.SQL.QueuedStatement;

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
		final String infiniteSeller = conf.getString("infinite.seller");
		market.reloadConfig();
		conf = market.getConfig();
		if (!storageType.equalsIgnoreCase(conf.getString("storage.type"))
				|| !user.equals(conf.getString("storage.mysql_user"))
				|| !pass.equals(conf.getString("storage.mysql_pass"))
				|| !db.equals(conf.getString("storage.mysql_database"))
				|| !addr.equals(conf.getString("storage.mysql_address"))
				|| port != conf.getInt("storage.mysql_port")) {
			AsyncDatabase asyncDb = market.getStorage().getAsyncDb();
			asyncDb.cancel();
			if (asyncDb.isProcessing()) {
				sender.sendMessage(ChatColor.YELLOW + "DB queue is currently running, please wait...");
				while(asyncDb.isProcessing()) {}
			}
			asyncDb.close();
			market.initializeStorage();
		}
		if (!infiniteSeller.equalsIgnoreCase(conf.getString("infinite.seller"))) {
			market.infiniteSeller = market.getConfig().getString("infinite.seller");
			Collection<Listing> infinite = Collections2.filter(market.getStorage().getAllListings(), new Predicate<Listing>() {
				@Override
				public boolean apply(Listing listing) {
					return listing.getSeller().equalsIgnoreCase(infiniteSeller);
				}
			});
			String seller = market.getInfiniteSeller();
			for (Listing listing : infinite) {
				listing.seller = seller;
				market.getStorage().getAsyncDb().addStatement(new QueuedStatement("UPDATE listings SET seller=? WHERE id=?")
				.setValue(seller)
				.setValue(listing.getId()));
			}
			market.getInterfaceHandler().updateAllViewers();
		}
		market.getConfigHandler().reloadLocaleYML();
		locale.setSelected();
		market.buildWorldLinks();
		sender.sendMessage(locale.get("cmd.prefix") + market.getLocale().get("config_reloaded"));
		return true;
	}
}
