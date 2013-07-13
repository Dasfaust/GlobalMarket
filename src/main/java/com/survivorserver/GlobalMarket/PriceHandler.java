package com.survivorserver.GlobalMarket;

import java.sql.ResultSet;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;


public class PriceHandler {
	
	Market market;
	DBWriter db;
	
	public PriceHandler(Market market) {
		this.market = market;
		db = new DBWriter(market.getLogger(), "", "", "", "prices", 0, true, market.getDataFolder().getAbsolutePath());
		db.connect();
		db.query("CREATE TABLE IF NOT EXISTS prices_data (item_id INT, item_data INT, enchants TEXT, amount INT, price DOUBLE, time BIGINT)");
	}
	
	public void storePriceInformation(ItemStack item, double price) {
		int id = item.getTypeId();
		int data = item.getData().getData();
		if (item.getType().getMaxDurability() > 0) {
			data = 0;
		}
		String ench = "";
		if (item.hasItemMeta() && item.getItemMeta().hasEnchants()) {
			ench = enchantsToString(item.getItemMeta().getEnchants());
		}
		int amount = item.getAmount();
		db.query("INSERT INTO `prices_data`(`item_id`, `item_data`, `enchants`, `amount`, `price`, `time`) VALUES ('" + id + "','" + data + "','" + ench + "','" + amount +  "','" + price + "','" + System.currentTimeMillis() / 1000 + "')");
	}
	
	public String getPricesInformation(ItemStack item) {
		String itemName = item.getType().toString();
		if (!market.useBukkitNames()) {
			net.milkbowl.vault.item.ItemInfo itemInfo = net.milkbowl.vault.item.Items.itemById(item.getTypeId());
			if (itemInfo != null) {
				itemName = itemInfo.getName();
			}
		}
		try {
			ResultSet rs = db.query("SELECT * FROM prices_data WHERE item_id=" + item.getTypeId() + " AND item_data=" + item.getData().getData());
			if (rs.isBeforeFirst()) {
				int totalAmount = 0;
				double totalPrice = 0;
				while(rs.next()) {
					totalAmount += Integer.valueOf(rs.getString("amount"));
					totalPrice += Double.valueOf(rs.getString("price"));
				}
				double av = totalPrice / totalAmount;
				return ChatColor.GREEN + market.getLocale().get("prices.header", itemName) + "\n"
						+ market.getLocale().get("prices.price_per_item", market.getEcon().format(av)) + "\n"
						+ market.getLocale().get("prices.price_per_stack", market.getEcon().format(av * item.getMaxStackSize()));
			}
		} catch (Exception e) {
		}
		return ChatColor.YELLOW + market.getLocale().get("prices.no_data", itemName);
	}
	
	public String enchantsToString(Map<Enchantment, Integer> enchants) {
		String ench = "";
		for (Entry<Enchantment, Integer> set : enchants.entrySet()) {
			ench += "" + set.getKey() + ":" + set.getValue() + ",";
		}
		return ench;
	}
}
