package com.survivorserver.GlobalMarket;

import java.sql.ResultSet;

import org.bukkit.ChatColor;
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
	
	public void storePriceInformation(int item, int data, int amount, double price) {
		db.query("INSERT INTO `prices_data`(`item_id`, `item_data`, `enchants`, `amount`, `price`, `time`) VALUES ('" + item + "','" + data + "',' ','" + amount +  "','" + price + "','" + System.currentTimeMillis() + "')");
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
				return ChatColor.GREEN + itemName + " Pricing:\nAverage price per item: " + market.getEcon().format(av) + "\nAverage price per stack: " + market.getEcon().format(av * item.getMaxStackSize());
			}
		} catch (Exception e) {
		}
		return ChatColor.YELLOW + "No data available for " + itemName;
	}
}
