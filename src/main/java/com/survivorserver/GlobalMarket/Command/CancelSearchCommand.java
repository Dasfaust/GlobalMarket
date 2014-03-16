package com.survivorserver.GlobalMarket.Command;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.survivorserver.GlobalMarket.Listing;
import com.survivorserver.GlobalMarket.LocaleHandler;
import com.survivorserver.GlobalMarket.Market;
import com.survivorserver.GlobalMarket.Lib.SearchResult;
import com.survivorserver.GlobalMarket.Lib.SortMethod;

public class CancelSearchCommand extends SubCommand {

	public CancelSearchCommand(Market market, LocaleHandler locale) {
		super(market, locale);
	}

	@Override
	public String getCommand() {
		return "cancelsearch";
	}

	@Override
	public String[] getAliases() {
		return new String[] {"canceall"};
	}

	@Override
	public String getPermissionNode() {
		return "globalmarket.admin";
	}

	@Override
	public String getHelp() {
		return locale.get("cmd.prefix") + "/market cancelSearch <search>" + " " + "[Cancells all listings with the given criteria]";
	}

	@Override
	public boolean allowConsoleSender() {
		return true;
	}

	@Override
	public boolean onCommand(CommandSender sender, String[] args) {
		if (args.length == 2) {
			String search = args[1];
			if (search.equalsIgnoreCase("all")) {
				List<Listing> listings = market.getStorage().getAllListings();
				if (listings.size() > 0) {
					for (Listing listing : listings) {
						market.getCore().expireListing(listing);
					}
					sender.sendMessage(ChatColor.GREEN + "" + listings.size() + " listings cancelled");
				} else {
					sender.sendMessage(ChatColor.YELLOW + "There are no listings to cancel");
				}
			} else {
				SearchResult res = market.getStorage().getListings(sender.getName(), SortMethod.DEFAULT, args[1], "");
				if (res.getTotalFound() > 0) {
					for (Listing listing : res.getPage()) {
						market.getCore().expireListing(listing);
					}
					sender.sendMessage(ChatColor.GREEN + "" + res.getTotalFound() + " listings cancelled");
				} else {
					sender.sendMessage(ChatColor.YELLOW + "No results found for \"" + args[1] + "\"");
				}
			}
		} else {
			return false;
		}
		return true;
	}
}
