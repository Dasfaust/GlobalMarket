package com.survivorserver.GlobalMarket.Legacy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.survivorserver.GlobalMarket.Listing;
import com.survivorserver.GlobalMarket.Mail;
import com.survivorserver.GlobalMarket.Market;

public class LegacyMarketStorage {

    LegacyConfigHandler config;
    Market market;

    public LegacyMarketStorage(LegacyConfigHandler config, Market market) {
        this.config = config;
        this.market = market;
    }

    public synchronized void storeListing(ItemStack item, String player, double price) {
        int id = getListingsIndex() + 1;
        String path = "listings." + id;
        config.getListingsYML().set(path + ".item", item);
        config.getListingsYML().set(path + ".seller", player);
        config.getListingsYML().set(path + ".price", price);
        config.getListingsYML().set(path + ".time", (System.currentTimeMillis() / 1000));
        incrementListingsIndex();
        market.getInterfaceHandler().updateAllViewers();
    }

    public int getNumListings() {
        if (!config.getListingsYML().isSet("listings")) {
            return 0;
        }
        return config.getListingsYML().getConfigurationSection("listings").getKeys(false).size();
    }

    public int getListingsIndex() {
        if (!config.getListingsYML().isSet("index")) {
            config.getListingsYML().set("index", 0);
        }
        return config.getListingsYML().getInt("index");
    }

    public void incrementListingsIndex() {
        int index = getListingsIndex() + 1;
        config.getListingsYML().set("index", index);
    }

    public void removeListing(String user, int id) {
        String path = "listings." + id;
        if (!config.getListingsYML().isSet(path)) {
            return;
        }
        config.getListingsYML().set(path, null);
    }

    public List<Listing> getAllListings() {
        List<Listing> listings = new ArrayList<Listing>();
        if (config.getListingsYML().isSet("listings")) {
            for (String l : config.getListingsYML().getConfigurationSection("listings").getKeys(false)) {
                String path = "listings." + l;
                ItemStack item = config.getListingsYML().getItemStack(path + ".item");
                if (item == null || item.getType() == Material.AIR) {
                    continue;
                }
                Listing listing = new Listing(Integer.parseInt(l), item.clone(), config.getListingsYML().getString(path + ".seller"), config.getListingsYML().getDouble(path + ".price"), config.getListingsYML().getLong(path + ".time"));
                listings.add(listing);
            }
            Collections.reverse(listings);
        }
        return listings;
    }

    public synchronized List<Listing> getListings() {
        List<Listing> listings = new ArrayList<Listing>();
        for (int i = 0; i <= getListingsIndex(); i++) {
            String path = "listings." + i;
            if (!config.getListingsYML().isSet(path)) {
                continue;
            }
            String seller = config.getListingsYML().getString(path + ".seller");
            ItemStack item = config.getListingsYML().getItemStack(path + ".item").clone();
            listings.add(new Listing(i, item, seller, config.getListingsYML().getDouble(path + ".price"), config.getListingsYML().getLong(path + ".time")));
        }
        return listings;
    }

    @SuppressWarnings("deprecation")
    public List<Listing> getAllListings(String search) {
        List<Listing> found = new ArrayList<Listing>();
        for (Listing listing : getAllListings()) {
            ItemStack item = listing.getItem();
            String itemName = market.getItemName(item);
            String seller = listing.getSeller();
            if (itemName.toLowerCase().contains(search.toLowerCase())
                    || isItemId(search, item.getTypeId())
                    || isInDisplayName(search.toLowerCase(), item)
                    || isInEnchants(search.toLowerCase(), item)
                    || isInLore(search.toLowerCase(), item)
                    || seller.toLowerCase().contains(search.toLowerCase())) {
                found.add(listing);
            }
        }
        return found;
    }

    public int getNumListings(String seller) {
        int n = 0;
        for (Listing listing : getAllListings(seller)) {
            if (listing.getSeller().equalsIgnoreCase(seller)) {
                n++;
            }
        }
        return n;
    }

    public Listing getListing(int id) {
        String path = "listings." + id;
        return new Listing(id, config.getListingsYML().getItemStack(path + ".item").clone(), config.getListingsYML().getString(path + ".seller"), config.getListingsYML().getDouble(path + ".price"), config.getListingsYML().getLong(path + ".time"));
    }

    public void storeMail(ItemStack item, String player, String sender, boolean notify) {
        int id = getMailIndex(player) + 1;
        String path = player + "." + id;
        config.getMailYML().set(path + ".item", item);
        config.getMailYML().set(path + ".sender", sender);
        config.getMailYML().set(path + ".time", (System.currentTimeMillis() / 1000));
        incrementMailIndex(player);
        if (notify) {
            Player reciever = market.getServer().getPlayer(player);
            if (reciever != null) {
                reciever.sendMessage(ChatColor.GREEN + market.getLocale().get("you_have_new_mail"));
            }
        }
    }

    public void storePayment(ItemStack item, String player, double amount, String buyer, boolean notify) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        if (meta == null) {
            meta = (BookMeta) market.getServer().getItemFactory().getItemMeta(book.getType());
        }
        meta.setTitle(market.getLocale().get("transaction_log.item_name"));
        double cut = 0;
        String itemName = market.getItemName(item);
        String logStr = market.getLocale().get("transaction_log.title") + "\n\n" +
                        market.getLocale().get("transaction_log.item_sold", itemName) + "\n\n" +
                        market.getLocale().get("transaction_log.sale_price", amount) + "\n\n" +
                        market.getLocale().get("transaction_log.market_cut", cut) +  "\n\n" +
                        market.getLocale().get("transaction_log.amount_recieved", (amount-cut));
        meta.setPages(logStr);
        book.setItemMeta(meta);
        int id = getMailIndex(player) + 1;
        String path = player + "." + id;
        config.getMailYML().set(path + ".item", book);
        config.getMailYML().set(path + ".sender", buyer);
        config.getMailYML().set(path + ".time", (System.currentTimeMillis() / 1000));
        config.getMailYML().set(path + ".amount", (amount-cut));
        incrementMailIndex(player);
        if (notify) {
            Player reciever = market.getServer().getPlayer(player);
            if (reciever != null) {
                reciever.sendMessage(ChatColor.GREEN + market.getLocale().get("listing_purchased_mailbox", itemName));
            }
        }
    }

    public void nullifyPayment(int id, String player) {
        if (!config.getMailYML().isSet(player + "." + id + ".amount")) {
            return;
        }
        config.getMailYML().set(player + "." + id + ".amount", null);
    }

    public int getMailIndex(String player) {
        if (!config.getMailYML().isSet("index." + player)) {
            config.getMailYML().set("index." + player, 0);
        }
        return config.getMailYML().getInt("index." + player);
    }

    public void incrementMailIndex(String player) {
        int index = getMailIndex(player) + 1;
        config.getMailYML().set("index." + player, index);
    }

    public int getNumMail(String player) {
        if (!config.getMailYML().isSet(player)) {
            return 0;
        }
        return config.getMailYML().getConfigurationSection(player).getKeys(false).size();
    }

    public Set<String> getAllMailUsers() {
        return config.getMailYML().isSet("index") ? config.getMailYML().getConfigurationSection("index").getKeys(false) : new HashSet<String>();
    }

    public List<Mail> getAllMailFor(String player) {
        List<Mail> mail = new ArrayList<Mail>();
        for (int i = 1; i <= getMailIndex(player); i++) {
            String path = player + "." + i;
            if (!config.getMailYML().isSet(path)) {
                continue;
            }
            String sender = null;
            if (config.getMailYML().isSet(path + ".sender")) {
                sender = config.getMailYML().getString(path + ".sender");
            }
            double pickup = -1;
            if (config.getMailYML().isSet(path + ".amount")) {
                pickup = config.getMailYML().getDouble(path + ".amount");
            }
            mail.add(new Mail(player, i, config.getMailYML().getItemStack(path + ".item").clone(), pickup, sender));
        }
        return mail;
    }

    public Mail getMailItem(String player, int id) {
        String path = player + "." + id;
        if (config.getMailYML().isSet(path)) {
            String sender = null;
            if (config.getMailYML().isSet(path + ".sender")) {
                sender = config.getMailYML().getString(path + ".sender");
            }
            double pickup = -1;
            if (config.getMailYML().isSet(path + ".amount")) {
                pickup = config.getMailYML().getDouble(path + ".amount");
            }
            return new Mail(player, id, config.getMailYML().getItemStack(path + ".item").clone(), pickup, sender);
        }
        return null;
    }

    public void removeMail(String player, int id) {
        String path = player + "." + id;
        if (!config.getMailYML().isSet(path)) {
            return;
        }
        config.getMailYML().set(path, null);
    }

    /*public int getNumHistory(String player) {
        if (!config.getHistoryYML().isSet(player)) {
            return 0;
        }
        return config.getHistoryYML().getConfigurationSection(player).getKeys(false).size();
    }

    public void storeHistory(String player, String info) {
        int id = getNumHistory(player) + 1;
        config.getHistoryYML().set(player + "." + id + ".info", info);
        config.getHistoryYML().set(player + "." + id + ".time", (System.currentTimeMillis() / 1000));
    }

    public Map<String, Long> getHistory(String player, int stop) {
        Map<String, Long> history = new HashMap<String, Long>();
        int p = 0;
        for (int i = getNumHistory(player); i > 0; i--) {
            history.put(config.getHistoryYML().getString(player + "." + i + ".info"), config.getHistoryYML().getLong(player + "." + i + ".time"));
            p++;
            if (p >= stop) {
                break;
            }
        }
        if (history.isEmpty()) {
            history.put("No history! ...yet ;)", 0L);
        }
        return history;
    }

    public void incrementSpent(String player, double amount) {
        config.getHistoryYML().set("spent." + player, getSpent(player) + amount);
    }

    public double getSpent(String player) {
        if (!config.getHistoryYML().isSet("spent." + player)) {
            return 0;
        }
        return config.getHistoryYML().getDouble("spent." + player);
    }

    public void incrementEarned(String player, double amount) {
        config.getHistoryYML().set("earned." + player, getEarned(player) + amount);
    }

    public double getEarned(String player) {
        if (!config.getHistoryYML().isSet("earned." + player)) {
            return 0;
        }
        return config.getHistoryYML().getDouble("earned." + player);
    }*/

    public int getQueueIndex() {
        if (!config.getQueueYML().isSet("index")) {
            config.getQueueYML().set("index", 0);
        }
        return config.getQueueYML().getInt("index");
    }

    public void incrementQueueIndex() {
        int index = getQueueIndex() + 1;
        config.getQueueYML().set("index", index);
    }

    public synchronized Map<Integer, List<Object>> getAllQueueItems() {
        Map<Integer, List<Object>> items = new HashMap<Integer, List<Object>>();
        for (int i = 0; i < getQueueIndex(); i++) {
            if (config.getQueueYML().isSet("queue." + i)) {
                List<Object> obs = new ArrayList<Object>();
                ConfigurationSection section = config.getQueueYML().getConfigurationSection("queue." + i);
                for (String key : section.getKeys(false)) {
                    obs.add(section.get(key));
                }
                items.put(i, obs);
            }
        }
        return items;
    }

    public synchronized void removeQueueItem(int id) {
        config.getQueueYML().set("queue." + id, null);
    }

    public boolean isItemId(String search, int typeId) {
        if (search.equalsIgnoreCase(Integer.toString(typeId))) {
            return true;
        }
        return false;
    }

    public boolean isInDisplayName(String search, ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName().toLowerCase().contains(search);
        }
        return false;
    }

    public boolean isInEnchants(String search, ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasEnchants()) {
            for (Entry<Enchantment, Integer> entry : item.getItemMeta().getEnchants().entrySet()) {
                if (entry.getKey().getName().toLowerCase().contains(search)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isInLore(String search, ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            for (String l : item.getItemMeta().getLore()) {
                if (l.toLowerCase().contains(search)) {
                    return true;
                }
            }
        }
        return false;
    }
}
