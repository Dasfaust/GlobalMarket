package com.survivorserver.GlobalMarket;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.survivorserver.GlobalMarket.Interface.MarketInterface;
import com.survivorserver.GlobalMarket.Interface.MarketItem;
import com.survivorserver.GlobalMarket.Lib.SearchResult;

public class ListingsInterface extends MarketInterface {

	protected Market market;
	private MarketStorage storage;
	
	public ListingsInterface(Market market) {
		this.market = market;
		storage = market.getStorage();
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
		ItemStack item = storage.getItem(listing.getItemId(), listing.getAmount());
		ItemMeta meta = item.getItemMeta().clone();
		
		boolean isSeller = viewer.getViewer().equalsIgnoreCase(listing.getSeller());
		boolean isAdmin = market.getInterfaceHandler().isAdmin(viewer.getViewer());
		
		List<String> lore = meta.getLore();
		if (!meta.hasLore()) {
			lore = new ArrayList<String>();
		}
		String price = ChatColor.WHITE + market.getLocale().get("price") + market.getEcon().format(listing.getPrice());
		String seller = ChatColor.WHITE + market.getLocale().get("seller") + ChatColor.GRAY + ChatColor.ITALIC + listing.getSeller();
		lore.add(price);
		lore.add(seller);
		
		// Don't want people buying their own listings
		if (isSeller && leftClick) {
			viewer.resetActions();
		}
		
		// Or canceling listings they don't have permissions to
		if (!isSeller && shiftClick) {
			if (!isAdmin) {
				viewer.resetActions();
			}
		}
		
		if (!isSeller) {
			String buyMsg = ChatColor.YELLOW + market.getLocale().get("click_to_buy");
			if (leftClick) {
				if (market.getEcon().has(viewer.getViewer(), listing.getPrice())) {
					if (viewer.getClicks() >= 2) {
						buyMsg = ChatColor.RED + market.getLocale().get("interface.transaction_error");
					} else {
						buyMsg = ChatColor.GREEN + market.getLocale().get("click_again_to_confirm");
					}
				} else {
					buyMsg = ChatColor.RED + market.getLocale().get("not_enough_money", market.getEcon().currencyNamePlural());
				}
			}
			lore.add(buyMsg);
		}
		
		if (isSeller || isAdmin) {
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
		if (market.getCore().buyListing((Listing) item, (Player) event.getWhoClicked(), viewer, true, true, true)) {
			viewer.resetActions();
		}
	}

	@Override
	public void handleShiftClickAction(InterfaceViewer viewer, MarketItem item, InventoryClickEvent event) {
		viewer.resetActions();
		market.getCore().removeListing((Listing) item, (Player) event.getWhoClicked());
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MarketItem> getContents(InterfaceViewer viewer) {
		return (List<MarketItem>)(List<?>) market.getStorage().getListings(viewer.getPage(), getSize() - 9, viewer.getWorld());
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MarketItem> doSearch(InterfaceViewer viewer, String search) {
		SearchResult result = market.getStorage().getListings(viewer.getPage(), getSize() - 9, search, viewer.getWorld());
		viewer.setSearchSize(result.getTotalFound());
		return (List<MarketItem>)(List<?>) result.getPage();
	}

	@Override
	public MarketItem getItem(InterfaceViewer viewer, int id) {
		return market.getStorage().getListing(id);
	}

	@Override
	public boolean identifyItem(ItemMeta meta) {
		if (meta.hasDisplayName()) {
			String name = meta.getDisplayName();
			if (name.contains(market.getLocale().get("interface.page").replace(" %s", ""))) {
				return true;
			}
			if (name.contains(market.getLocale().get("interface.search"))) {
				return true;
			}
			if (name.contains(market.getLocale().get("interface.cancel_search"))) {
				return true;
			}
		}
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
		return viewer.getSearch() == null ? market.getStorage().getNumListings(viewer.getWorld()) : viewer.getSearchSize();
	}

	@Override
	public ItemStack getItemStack(InterfaceViewer viewer, MarketItem item) {
		return market.getStorage().getItem(item.getItemId(), item.getAmount());
	}
}
