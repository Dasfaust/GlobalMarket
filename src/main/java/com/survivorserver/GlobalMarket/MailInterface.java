package com.survivorserver.GlobalMarket;

import java.util.ArrayList;
import java.util.List;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.survivorserver.GlobalMarket.HistoryHandler.MarketAction;
import com.survivorserver.GlobalMarket.Interface.MarketInterface;
import com.survivorserver.GlobalMarket.Interface.MarketItem;

public class MailInterface extends MarketInterface {

	protected Market market;
	private MarketStorage storage;
	
	public MailInterface(Market market) {
		this.market = market;
		storage = market.getStorage();
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
	public boolean doSingleClickActions() {
		return true;
	}

	@Override
	public ItemStack prepareItem(MarketItem mailItem, InterfaceViewer viewer, int page, int slot, boolean leftClick, boolean shiftClick) {
		Mail mail = (Mail) mailItem;
		ItemStack item = storage.getItem(mail.getItemId(), mail.getAmount());
		ItemMeta meta = item.getItemMeta();
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
						if (leftClick || shiftClick) {
							lore.add(ChatColor.RED + market.getLocale().get("interface.transaction_error"));
						} else {
							lore.add(ChatColor.WHITE + market.getLocale().get("amount") + market.getEcon().format(amount));
						}
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
		Economy econ = market.getEcon();
		Player player = (Player) event.getWhoClicked();
		double amount = ((Mail) item).getPickup();
		if (amount > 0) {
			EconomyResponse response = econ.depositPlayer(player.getName(), amount);
			if (!response.transactionSuccess()) {
				if (response.type == ResponseType.NOT_IMPLEMENTED) {
					market.log.severe(econ.getName() + " may not be compatible with GlobalMarket. It does not support the depositPlayer() function.");
				} else {
					market.log.severe("Recieved failed economy response from " + econ.getName() + ": " + response.errorMessage);
				}
				return;
			}
			player.sendMessage(ChatColor.GREEN + market.getLocale().get("picked_up_your_earnings", market.getEcon().format(market.getEcon().getBalance(player.getName()))));
			market.getStorage().nullifyMailPayment(item.getId());
			if (market.enableHistory()) {
				if (viewer.getName().equalsIgnoreCase(viewer.getViewer())) {
					market.getHistory().storeHistory(viewer.getName(), "You", MarketAction.EARNINGS_RETRIEVED, item.getItemId(), item.getAmount(), amount);
				} else {
					market.getHistory().storeHistory(viewer.getName(), viewer.getViewer(), MarketAction.EARNINGS_RETRIEVED, item.getItemId(), item.getAmount(), amount);
				}
			}
		}
		Inventory inv = player.getInventory();
		if (inv.firstEmpty() >= 0) {
			market.getCore().retrieveMail((Mail) item, viewer, player);
			viewer.resetActions();
		} else {
			viewer.resetActions();
		}
	}

	@Override
	public void handleShiftClickAction(InterfaceViewer viewer, MarketItem item, InventoryClickEvent event) {
		handleLeftClickAction(viewer, item, event);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MarketItem> getContents(InterfaceViewer viewer) {
		return (List<MarketItem>)(List<?>) market.getStorage().getMail(viewer.getName(), viewer.getPage(), getSize() - 9, viewer.getWorld());
	}

	@Override
	public List<MarketItem> doSearch(InterfaceViewer viewer, String search) {
		return null;
	}

	@Override
	public MarketItem getItem(InterfaceViewer viewer, int id) {
		return market.getStorage().getMail(id);
	}

	@Override
	public boolean identifyItem(ItemMeta meta) {
		if (meta.hasLore()) {
			for (String lore : meta.getLore()) {
				if (lore.contains(market.getLocale().get("price")) || lore.contains(market.getLocale().get("click_to_retrieve"))) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void onInterfacePrepare(InterfaceViewer viewer, List<MarketItem> contents, ItemStack[] invContents, Inventory inv) {
	}
	
	@Override
	public int getTotalNumberOfItems(InterfaceViewer viewer) {
		return market.getStorage().getNumMail(viewer.getName(), viewer.getWorld());
	}
	
	@Override
	public ItemStack getItemStack(InterfaceViewer viewer, MarketItem item) {
		return market.getStorage().getItem(item.getItemId(), item.getAmount());
	}
	
	@Override
	public void buildFunctionBar(Market market, InterfaceHandler handler, InterfaceViewer viewer, ItemStack[] contents, boolean pPage, boolean nPage) {
		super.buildFunctionBar(market, handler, viewer, contents, pPage, nPage);
		
		// Unset search
		contents[contents.length - 7] = null;
	}
}
