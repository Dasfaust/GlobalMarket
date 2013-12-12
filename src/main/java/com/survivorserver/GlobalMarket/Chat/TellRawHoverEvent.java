package com.survivorserver.GlobalMarket.Chat;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TellRawHoverEvent {

	public static String ACTION_SHOW_TEXT = "show_text";
	public static String ACTION_SHOW_ITEM = "show_item";
	public static String ACTION_SHOW_ACHIEVEMENT = "show_achievement";
	
	public String action;
	public String value;
	
	public TellRawHoverEvent setAction(String action) {
		this.action = action;
		return this;
	}
	
	public TellRawHoverEvent setValue(String value) {
		this.value = value;
		return this;
	}
	
	@SuppressWarnings("deprecation")
	public TellRawHoverEvent setValue(ItemStack item) {
		StringBuilder sb = new StringBuilder();
		sb.append("{id:");
		sb.append(item.getTypeId());
		sb.append(",damage:");
		sb.append(item.getDurability());
		if (item.hasItemMeta()) {
			ItemMeta meta = item.getItemMeta();
			sb.append(",tag:{display:{");
			boolean named = false;
			if (meta.hasDisplayName()) {
				sb.append("Name:");
				sb.append('"');
				sb.append(meta.getDisplayName());
				sb.append('"');
				named = true;
			}
			if (meta.hasLore()) {
				if (named) {
					sb.append(",");
				}
				sb.append("Lore:[");
				List<String> lore = meta.getLore();
				for (int i = 0; i < lore.size(); i++) {
					String str = lore.get(i);
					sb.append('"');
					sb.append(str);
					sb.append('"');
					if (i < lore.size() - 1) {
						sb.append(",");
					}
				}
			}
			if (meta.hasEnchants()) {
				if (meta.hasDisplayName() || meta.hasLore()) {
					sb.append(",");
				}
				sb.append("ench:[");
				int i = 1;
				Set<Entry<Enchantment, Integer>> entrySet = meta.getEnchants().entrySet();
				for (Entry<Enchantment, Integer> entry : entrySet) {
					sb.append("{id:");
					sb.append(entry.getKey().getId());
					sb.append(",lvl:");
					sb.append(entry.getValue());
					sb.append("}");
					if (i < entrySet.size()) {
						sb.append(",");
					}
					i++;
				}
				sb.append("]");
			}
			sb.append("}}");
		}
		sb.append("}");
		this.value = sb.toString();
		return this;
	}
}
