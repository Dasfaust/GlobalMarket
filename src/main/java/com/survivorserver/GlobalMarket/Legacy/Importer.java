package com.survivorserver.GlobalMarket.Legacy;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.inventory.ItemStack;

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
        try {
            LegacyConfigHandler conf = new LegacyConfigHandler(market);
            LegacyMarketStorage storage = new LegacyMarketStorage(conf, market);
            String world = market.getServer().getWorlds().get(0).getName();
            market.log.info("Old YAML storage found, importing your data. Note that history and pricing information will not be imported, as they are incompatbile, sorry!");
            /*
             * Import listings from legacy YAML storage
             */
            market.log.info("Importing listings...");
            List<Listing> listings = storage.getAllListings();
            for (int i = 0; i < listings.size(); i++) {
                Listing listing = listings.get(i);
                sql.createListing(listing.getSeller(), listing.getItem(), listing.getPrice(), world);
            }
            /*
             * Import mail from legacy YAML storage
             */
            market.log.info("Importing mail...");
            Set<String> players = storage.getAllMailUsers();
            for (String player : players) {
                for (Mail mail : storage.getAllMailFor(player)) {
                    sql.createMail(mail.getOwner(), mail.getSender(), mail.getItem(), mail.getPickup(), world);
                }
            }
            market.log.info("Importing queue... (Queue will be cleared)");
            Map<Integer, List<Object>> queue = storage.getAllQueueItems();
            if (!queue.isEmpty()) {
                for (Entry<Integer, List<Object>> set : queue.entrySet()) {
                    List<Object> item = set.getValue();
                    String type = (String) item.get(0);
                    if (type.equalsIgnoreCase("listing_create")) {
                        sql.createListing((String) item.get(2), (ItemStack) item.get(1), (Double) item.get(3), world);
                    } else if (type.equalsIgnoreCase("mail_to")) {
                        String from = null;
                        if (item.size() == 5) {
                            from = (String) item.get(3);
                        }
                        sql.createMail((String) item.get(2), from, (ItemStack) item.get(1), 0, world);
                    }
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
            conf.getListingsFile().renameTo(new File(newDir.getAbsolutePath() + File.separator + "listings.yml"));
            conf.getMailFile().renameTo(new File(newDir.getAbsolutePath() + File.separator + "mail.yml"));
            conf.getHistoryFile().renameTo(new File(newDir.getAbsolutePath() + File.separator + "history.yml"));
            conf.getQueueFile().renameTo(new File(newDir.getAbsolutePath() + File.separator + "queue.yml"));
            File prices = new File(market.getDataFolder().getAbsolutePath() + File.separator + "prices.db");
            if (prices.exists()) {
                prices.renameTo(new File(newDir.getAbsolutePath() + File.separator + "prices.db"));
            }
            market.log.info("Moved the old data files to 'plugins/GlobalMarket/old'. Happy trading!");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
