package com.survivorserver.GlobalMarket;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.survivorserver.GlobalMarket.InterfaceViewer.InterfaceAction;
import com.survivorserver.GlobalMarket.Interface.MarketInterface;
import com.survivorserver.GlobalMarket.Interface.MarketItem;

public class MailInterface implements MarketInterface {

	Market market;
	
	public MailInterface(Market market) {
		this.market = market;
	}
	
	@Override
	public String getName() {
		return "Mail";
	}

	@Override
	public String getTitle() {
		return market.getLocale().get("interface.mail_title");
	}

	@Override
	public int getSize() {
		return 54;
	}

	@Override
	public boolean enableSearch() {
		return false;
	}

	@Override
	public boolean doSingleClickActions() {
		return true;
	}

	@Override
	public ItemStack prepareItem(MarketItem mailItem, InterfaceViewer viewer, int page, int slot, boolean leftClick, boolean shiftClick) {
		Mail mail = (Mail) mailItem;
		ItemStack item = mailItem.getItem();
		ItemMeta meta = item.getItemMeta().clone();
		List<String> lore = meta.getLore();
		if (!meta.hasLore()) {
			lore = new ArrayList<String>();
		}
		if (mail.getSender() != null) {
			lore.add(ChatColor.RESET + market.getLocale().get("interface.mail_from") + ChatColor.GRAY + ChatColor.ITALIC + mail.getSender());
		}
		if (meta instanceof BookMeta) {
			BookMeta bookMeta = (BookMeta) meta;
			if (bookMeta.hasTitle()) {
				if (bookMeta.getTitle().equalsIgnoreCase(market.getLocale().get("transaction_log.item_name"))) {
					double amount = mail.getPickup();
					if (amount > 0) {
						lore.add(ChatColor.WHITE + market.getLocale().get("amount") + market.getEcon().format(amount));
					}
				}
			}
		}
		String instructions = ChatColor.YELLOW + market.getLocale().get("click_to_retrieve");
		if (leftClick || shiftClick) {
			instructions = ChatColor.RED + market.getLocale().get("full_inventory");
			viewer.resetActions();
		}
		lore.add(instructions);
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	@Override
	public void handleLeftClickAction(InterfaceViewer viewer, MarketItem item, InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		double amount = ((Mail) item).getPickup();
		if (amount > 0) {
			market.getEcon().depositPlayer(player.getName(), amount);
			player.sendMessage(ChatColor.GREEN + market.getLocale().get("picked_up_your_earnings", market.getEcon().format(market.getEcon().getBalance(player.getName()))));
			market.getStorage().nullifyPayment(item.getId(), viewer.getViewer());
		}
		Inventory inv = player.getInventory();
		if (inv.firstEmpty() >= 0) {
			market.getCore().retrieveMail((Mail) item, player);
			viewer.resetActions();
		} else {
			viewer.setLastItem(item);
			viewer.setLastAction(InterfaceAction.LEFTCLICK);
			viewer.setLastActionSlot(event.getSlot());
		}
	}

	@Override
	public void handleShiftClickAction(InterfaceViewer viewer, MarketItem item, InventoryClickEvent event) {
		handleLeftClickAction(viewer, item, event);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MarketItem> getContents(InterfaceViewer viewer) {
		return (List<MarketItem>)(List<?>) market.getStorage().getAllMailFor(viewer.getViewer());
	}

	@Override
	public List<MarketItem> doSearch(String search) {
		return null;
	}

	@Override
	public MarketItem getItem(InterfaceViewer viewer, int id) {
		return market.getStorage().getMailItem(viewer.getViewer(), id);
	}

	@Override
	public boolean identifyItem(ItemMeta meta) {
		for (String lore : meta.getLore()) {
			if (lore.contains(market.getLocale().get("price")) || lore.contains(market.getLocale().get("click_to_retrieve"))) {
				return true;
			}
		}
		return false;
	}
}
