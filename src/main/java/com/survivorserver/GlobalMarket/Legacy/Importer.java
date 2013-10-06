package com.survivorserver.GlobalMarket.Legacy;

import java.io.File;
import java.util.List;
import java.util.Set;

import com.survivorserver.GlobalMarket.Listing;
import com.survivorserver.GlobalMarket.Mail;
import com.survivorserver.GlobalMarket.Market;
import com.survivorserver.GlobalMarket.MarketStorage;

public class Importer {

	public static boolean importNeeded(Market market) {
		File file = new File(market.getDataFolder().getAbsolutePath() + File.separator + "listings.yml");
		return file.exists();
	}
	
	@SuppressWarnings("deprecation")
	public static void importLegacyData(com.survivorserver.GlobalMarket.ConfigHandler config, MarketStorage sql, Market market) {
		LegacyConfigHandler conf = new LegacyConfigHandler(market);
		LegacyMarketStorage storage = new LegacyMarketStorage(conf, market);
		market.log.info("Old YAML storage found, importing your data. Note that history and pricing information will not be imported, as they are incompatbile, sorry!");
		/*
		 * Import listings from legacy YAML storage
		 */
		market.log.info("Importing listings...");
		List<Listing> listings = storage.getAllListings();
		for (int i = 0; i < listings.size(); i++) {
			Listing listing = listings.get(i);
			sql.createListing(listing.getSeller(), listing.getItem(), listing.getPrice());
		}
		/*
		 * Import mail from legacy YAML storage
		 */
		market.log.info("Importing mail...");
		Set<String> players = storage.getAllMailUsers();
		for (String player : players) {
			for (Mail mail : storage.getAllMailFor(player)) {
				sql.createMail(mail.getOwner(), mail.getSender(), mail.getItem(), mail.getPickup());
			}
		}
		/*
		 * Clean up
		 */
		market.log.info("...Done!");
		File newDir = new File(market.getDataFolder().getAbsolutePath() + File.separator + "old");
		if (!newDir.exists()) {
			newDir.mkdir();
		}
		conf.getListingsFile().renameTo(newDir);
		conf.getMailFile().renameTo(newDir);
		conf.getHistoryFile().renameTo(newDir);
		conf.getQueueFile().renameTo(newDir);
		market.log.info("Moved the old data files to plugins/GlobalMarket/old. Happy trading!");
	}
}
