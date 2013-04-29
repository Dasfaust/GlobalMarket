package com.survivorserver.GlobalMarket;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public class Converter {
	
	MarketStorage storage;
	//MySQLFunc sql;
	
	public Converter(MarketStorage storage) {
		this.storage = storage;
		//this.sql = sql;
	}
	
	/*public void convert() {
		
		// Listings
		MySQLResult mr = sql.Query("SELECT * FROM `market_listings`");
		while (mr.next()) {
			String itemtable[] = mr.getString("item").split(":");
			String name = itemtable[0];
			short damage = Short.parseShort(itemtable[1]);
			String seller = mr.getString("seller");
			long time = Long.parseLong(mr.getString("created"));
			String enchants = mr.getString("enchantments");
			int amount = Integer.parseInt(mr.getString("amount"));
			double price = Double.parseDouble(mr.getString("buyout"));
			if (name.length() > 0) {
				ItemStack item = new ItemStack(Material.getMaterial(name), amount, damage);
				if (!enchants.equalsIgnoreCase("none")) {
					item.addUnsafeEnchantments(getEnchants(enchants));
				}
				if (itemtable.length == 3) {
					setItemName(item, unescape(itemtable[2].replaceAll("_", " ")));
				}
				
				storage.storeListing(item, seller, price, time);
			}
		}
		
		// Mail
		mr = sql.Query("SELECT * FROM `market_mail`");
		while (mr.next()) {
			String itemtable[] = mr.getString("item").split(":");
			String name = itemtable[0];
			short damage = Short.parseShort(itemtable[1]);
			String player = mr.getString("player");
			if (player.length() < 2) {
				continue;
			}
			String enchants = mr.getString("enchantments");
			int amount = Integer.parseInt(mr.getString("amount"));
			if (name.length() > 0) {
				Material mat = Material.getMaterial(name);
				if (mat == null) {
					continue;
				}
				ItemStack item = new ItemStack(mat, amount, damage);
				if (!enchants.equalsIgnoreCase("none")) {
					item.addUnsafeEnchantments(getEnchants(enchants));
				}
				if (itemtable.length == 3) {
					setItemName(item, unescape(itemtable[2].replaceAll("_", " ")));
				}
				
				storage.storeMail(item, player, false);
			}
		}
		
		// Storage
		mr = sql.Query("SELECT * FROM `market_storage`");
		while (mr.next()) {
			String itemtable[] = mr.getString("item").split(":");
			String name = itemtable[0];
			short damage = Short.parseShort(itemtable[1]);
			String player = mr.getString("player");
			if (player.length() < 2) {
				continue;
			}
			String enchants = mr.getString("enchantments");
			int amount = Integer.parseInt(mr.getString("amount"));
			if (name.length() > 0) {
				ItemStack item = new ItemStack(Material.getMaterial(name), amount, damage);
				if (!enchants.equalsIgnoreCase("none")) {
					item.addUnsafeEnchantments(getEnchants(enchants));
				}
				if (itemtable.length == 3) {
					setItemName(item, unescape(itemtable[2].replaceAll("_", " ")));
				}
				
				storage.storeMail(item, player, false);
			}
		}
	}*/
	
	public Map<Enchantment, Integer> getEnchants(String e) {
		Map<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>();
		if (e.contains(",")) {
			String en[] = e.split(",");
			for (int i = 0; i < en.length; i++) {
				if (en[i].contains(":")) {
					String enchant[] = en[i].split(":");
					if (enchant.length == 2) {
						enchants.put(Enchantment.getByName(enchant[0]), Integer.parseInt(enchant[1]));
					}
				}
			}
		}
		return enchants;
	}
	
	public String unescape(String str) {
		str = str.replaceAll("&apos;", "\'");
		str = str.replaceAll("&quot;", "\"");
		str = str.replaceAll("&rsquo;", "\'");
		str = str.replaceAll("&lt;", "<");
		str = str.replaceAll("&gt", ">");
		return str;
	}
	
	public ItemStack setItemName(ItemStack item2, String name) {
		ItemMeta meta = item2.getItemMeta();
		meta.setDisplayName(name);
		item2.setItemMeta(meta);
		return item2;
	}
}
