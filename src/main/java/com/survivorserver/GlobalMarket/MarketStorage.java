package com.survivorserver.GlobalMarket;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.survivorserver.GlobalMarket.MarketQueue.QueueType;

public class MarketStorage {

	ConfigHandler config;
	Market market;
	
	public MarketStorage(ConfigHandler config, Market market) {
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
		market.interfaceHandler.updateAllViewers();
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
	
	public void removeListing(int id) {
		String path = "listings." + id;
		if (!config.getListingsYML().isSet(path)) {
			return;
		}
		config.getListingsYML().set(path, null);
	}
	
	public List<Listing> getAllListings() {
		List<Listing> listings = new ArrayList<Listing>();
		for (String l : config.getListingsYML().getConfigurationSection("listings").getKeys(false)) {
			String path = "listings." + l;
			Listing listing = new Listing(market, Integer.parseInt(l), config.getListingsYML().getItemStack(path + ".item").clone(), config.getListingsYML().getString(path + ".seller"), config.getListingsYML().getDouble(path + ".price"), config.getListingsYML().getLong(path + ".time"));
			listings.add(listing);
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
			listings.add(new Listing(market, i, item, seller, config.getListingsYML().getDouble(path + ".price"), config.getListingsYML().getLong(path + ".time")));
		}
		return listings;
	}
	
	public List<Listing> getAllListings(String search) {
		List<Listing> listings = new ArrayList<Listing>();
		for (int i = getListingsIndex(); i >= 1; i--) {
			String path = "listings." + i;
			if (!config.getListingsYML().isSet(path)) {
				continue;
			}
			String seller = config.getListingsYML().getString(path + ".seller");
			ItemStack item = config.getListingsYML().getItemStack(path + ".item").clone();
			// TODO: make this pretty
			String itemName = item.getType().toString();
			if (!market.useBukkitNames()) {
				net.milkbowl.vault.item.ItemInfo itemInfo = net.milkbowl.vault.item.Items.itemById(item.getTypeId());
				if (itemInfo != null) {
					itemName = itemInfo.getName();
				}
			}
			if (itemName.toLowerCase().contains(search.toLowerCase())
					|| isItemId(search, item.getTypeId())
					|| isInDisplayName(search.toLowerCase(), item)
					|| isInEnchants(search.toLowerCase(), item)
					|| isInLore(search.toLowerCase(), item)
					|| seller.toLowerCase().contains(search.toLowerCase())) {
				listings.add(new Listing(market, i, item, seller, config.getListingsYML().getDouble(path + ".price"), config.getListingsYML().getLong(path + ".time")));
			}
		}
		return listings;
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
		return new Listing(market, id, config.getListingsYML().getItemStack(path + ".item").clone(), config.getListingsYML().getString(path + ".seller"), config.getListingsYML().getDouble(path + ".price"), config.getListingsYML().getLong(path + ".time"));
	}
	
	public void storeMail(ItemStack item, String player, boolean notify) {
		int id = getMailIndex(player) + 1;
		String path = player + "." + id;
		config.getMailYML().set(path + ".item", item);
		config.getMailYML().set(path + ".time", (System.currentTimeMillis() / 1000));
		incrementMailIndex(player);
		if (notify) {
			Player reciever = market.getServer().getPlayer(player);
			if (reciever != null) {
				reciever.sendMessage(ChatColor.GREEN + market.getLocale().get("you_have_new_mail"));
			}
		}
	}
	
	public void storePayment(ItemStack item, String player, double amount, boolean notify) {
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta meta = (BookMeta) book.getItemMeta();
		if (meta == null) {
			meta = (BookMeta) market.getServer().getItemFactory().getItemMeta(book.getType());
		}
		meta.setTitle(market.getLocale().get("transaction_log.item_name"));
		double cut = new BigDecimal(market.getCut(amount)).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
		// TODO: make this pretty
		String itemName = item.getType().toString();
		if (!market.useBukkitNames()) {
			net.milkbowl.vault.item.ItemInfo itemInfo = net.milkbowl.vault.item.Items.itemById(item.getTypeId());
			if (itemInfo != null) {
				itemName = itemInfo.getName();
			}
		}
		String logStr = market.getLocale().get("transaction_log.title") + "\n\n" +
						market.getLocale().get("transaction_log.item_sold", itemName + "x" + item.getAmount()) + "\n\n" +
						market.getLocale().get("transaction_log.sale_price", amount) + "\n\n" +
						market.getLocale().get("transaction_log.market_cut", cut) +  "\n\n" +
						market.getLocale().get("transaction_log.amount_recieved", (amount-cut));
		meta.setPages(logStr);
		book.setItemMeta(meta);
		int id = getMailIndex(player) + 1;
		String path = player + "." + id;
		config.getMailYML().set(path + ".item", book);
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
	
	public double getPaymentAmount(int id, String player) {
		if (!config.getMailYML().isSet(player + "." + id + ".amount")) {
			return 0;
		}
		return config.getMailYML().getDouble(player + "." + id + ".amount");
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
	
	public Map<Integer, ItemStack> getAllMailFor(String player) {
		Map<Integer, ItemStack> mail = new HashMap<Integer, ItemStack>();
		for (int i = 1; i <= getMailIndex(player); i++) {
			String path = player + "." + i;
			if (!config.getMailYML().isSet(path)) {
				continue;
			}
			mail.put(i, config.getMailYML().getItemStack(path + ".item").clone());
		}
		return mail;
	}
	
	public ItemStack getMailItem(String player, int id) {
		if (config.getMailYML().isSet(player + "." + id)) {
			return config.getMailYML().getItemStack(player + "." + id + ".item").clone();
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
	
	public int getNumHistory(String player) {
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
	}
	
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
	
	public void storeQueueItem(QueueType type, Object... args) {
		int id = getQueueIndex();
		String path = "queue." + id;
		config.getQueueYML().set(path + ".type", type.toString());
		for (int i = 0; i < args.length; i++) {
			config.getQueueYML().set(path + "." + i, args[i]);
		}
		config.getQueueYML().set(path + ".time", System.currentTimeMillis());
		incrementQueueIndex();
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
