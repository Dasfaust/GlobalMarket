package com.survivorserver.GlobalMarket;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.survivorserver.GlobalMarket.Interface.MarketInterface;
import com.survivorserver.GlobalMarket.Interface.MarketItem;
import com.survivorserver.GlobalMarket.InterfaceViewer.InterfaceAction;

public class ListingsInterface implements MarketInterface {

	Market market;
	
	public ListingsInterface(Market market) {
		this.market = market;
	}
	
	@Override
	public String getName() {
		return "Listings";
	}

	@Override
	public String getTitle() {
		return market.getLocale().get("interface.listings_title");
	}

	@Override
	public int getSize() {
		return 54;
	}

	@Override
	public boolean enableSearch() {
		return true;
	}

	@Override
	public boolean doSingleClickActions() {
		return false;
	}

	@Override
	public ItemStack prepareItem(MarketItem marketItem, InterfaceViewer viewer, int page, int slot, boolean leftClick, boolean shiftClick) {
		Listing listing = (Listing) marketItem;
		ItemStack item = listing.getItem();
		ItemMeta meta = item.getItemMeta().clone();
		List<String> lore = meta.getLore();
		if (!meta.hasLore()) {
			lore = new ArrayList<String>();
		}
		String price = ChatColor.WHITE + market.getLocale().get("price") + market.getEcon().format(listing.getPrice());
		String seller = ChatColor.WHITE + market.getLocale().get("seller") + ChatColor.GRAY + ChatColor.ITALIC + listing.getSeller();
		lore.add(price);
		lore.add(seller);
		if (!viewer.getViewer().equalsIgnoreCase(listing.seller)) {
			String buyMsg = ChatColor.YELLOW + market.getLocale().get("click_to_buy");
			if (leftClick) {
				if (market.getEcon().has(viewer.getViewer(), listing.price)) {
					buyMsg = ChatColor.GREEN + market.getLocale().get("click_again_to_confirm");
				} else {
					buyMsg = ChatColor.RED + market.getLocale().get("not_enough_money", market.getEcon().currencyNamePlural());
					viewer.resetActions();
				}
			}
			lore.add(buyMsg);
		} else {
			if (leftClick) {
				viewer.setLastAction(InterfaceAction.RIGHTCLICK);
			}
		}
		if (viewer.getViewer().equalsIgnoreCase(listing.seller) || market.getInterfaceHandler().isAdmin(viewer.getViewer())) {
			String removeMsg = ChatColor.DARK_GRAY + market.getLocale().get("shift_click_to_remove");
			if (shiftClick) {
				removeMsg = ChatColor.GREEN + market.getLocale().get("shift_click_again_to_confirm");
			}
			lore.add(removeMsg);
		}
		if (listing.getSeller().equalsIgnoreCase(market.getInfiniteSeller())) {
			lore.add(ChatColor.LIGHT_PURPLE + market.getLocale().get("interface.infinite"));
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}
	
	@Override
	public void handleLeftClickAction(InterfaceViewer viewer, MarketItem item, InventoryClickEvent event) {
		market.getCore().buyListing((Listing) item, (Player) event.getWhoClicked());
		viewer.resetActions();
	}

	@Override
	public void handleShiftClickAction(InterfaceViewer viewer, MarketItem item, InventoryClickEvent event) {
		market.getCore().removeListing((Listing) item, (Player) event.getWhoClicked());
		viewer.resetActions();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MarketItem> getContents(InterfaceViewer viewer) {
		return (List<MarketItem>)(List<?>) market.getStorage().getAllListings();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MarketItem> doSearch(String search) {
		return (List<MarketItem>)(List<?>) market.getStorage().getAllListings(search);
	}

	@Override
	public MarketItem getItem(InterfaceViewer viewer, int id) {
		return market.getStorage().getListing(id);
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
