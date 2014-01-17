package com.survivorserver.GlobalMarket.Interface;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.survivorserver.GlobalMarket.InterfaceHandler;
import com.survivorserver.GlobalMarket.InterfaceViewer;
import com.survivorserver.GlobalMarket.Market;

public abstract class MarketInterface {

	public abstract String getName();

	public abstract String getTitle();

	public abstract int getSize();
	
	public abstract boolean doSingleClickActions();
	
	public abstract ItemStack prepareItem(MarketItem item, InterfaceViewer viewer, int page, int slot, boolean leftClick, boolean shiftClick);
	
	public abstract void handleLeftClickAction(InterfaceViewer viewer, MarketItem item, InventoryClickEvent event);
	
	public abstract void handleShiftClickAction(InterfaceViewer viewer, MarketItem item, InventoryClickEvent event);
	
	public abstract List<MarketItem> getContents(InterfaceViewer viewer);
	
	public abstract List<MarketItem> doSearch(InterfaceViewer viewer, String search);
	
	public abstract MarketItem getItem(InterfaceViewer viewer, int id);
	
	public abstract ItemStack getItemStack(InterfaceViewer viewer, MarketItem item);
	
	public abstract int getTotalNumberOfItems(InterfaceViewer viewer);
	
	public abstract boolean identifyItem(ItemMeta meta);
	
	public abstract void onInterfacePrepare(InterfaceViewer viewer, List<MarketItem> contents, ItemStack[] invContents, Inventory inv);
	
	public void onUnboundClick(Market market, InterfaceHandler handler, InterfaceViewer viewer, int slot, InventoryClickEvent event, int invSize) {
		Player player = (Player) event.getWhoClicked();
		
		// Searching
		if (slot == invSize - 7) {
			if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
				if (viewer.getSearch() == null) {
					player.closeInventory();
					market.startSearch(player, viewer.getInterface().getName());
					handler.removeViewer(viewer);
					return;
				} else {
					// Cancel search
					viewer.setSearch(null);
					viewer.resetActions();
					handler.refreshViewer(viewer, viewer.getInterface().getName());
					return;
				}
			}
		}
		
		// Next page
		if (slot == invSize - 1) {
			if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
				viewer.setPage(viewer.getPage() + 1);
				viewer.resetActions();
				handler.refreshViewer(viewer, viewer.getInterface().getName());
				return;
			}
		}
		
		// Previous page
		if (slot == invSize - 9) {
			if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
				viewer.setPage(viewer.getPage() - 1);
				viewer.resetActions();
				handler.refreshViewer(viewer, viewer.getInterface().getName());
				return;
			}
		}
	}
	
	public void buildFunctionBar(Market market, InterfaceHandler handler, InterfaceViewer viewer, ItemStack[] contents, boolean pPage, boolean nPage) {
		if (pPage) {
			// Prev page
			ItemStack prevPage = new ItemStack(Material.PAPER, viewer.getPage() - 1);
			ItemMeta prevMeta = prevPage.getItemMeta();
			if (prevMeta == null) {
				prevMeta = market.getServer().getItemFactory().getItemMeta(prevPage.getType());
			}
			prevMeta.setDisplayName(ChatColor.WHITE + market.getLocale().get("interface.page", (viewer.getPage() - 1)));
			List<String> prevLore = new ArrayList<String>();
			prevLore.add(ChatColor.YELLOW + market.getLocale().get("interface.prev_page"));
			prevMeta.setLore(prevLore);
			prevPage.setItemMeta(prevMeta);
			contents[contents.length - 9] = prevPage;
		}
		
		if (nPage) {
			// Next page
			ItemStack nextPage = new ItemStack(Material.PAPER, viewer.getPage() + 1);
			ItemMeta nextMeta = nextPage.getItemMeta();
			if (nextMeta == null) {
				nextMeta = market.getServer().getItemFactory().getItemMeta(nextPage.getType());
			}
			nextMeta.setDisplayName(ChatColor.WHITE + market.getLocale().get("interface.page", (viewer.getPage() + 1)));
			List<String> nextLore = new ArrayList<String>();
			nextLore.add(ChatColor.YELLOW + market.getLocale().get("interface.next_page"));
			nextMeta.setLore(nextLore);
			nextPage.setItemMeta(nextMeta);
			contents[contents.length - 1] = nextPage;
		}
		
		// Current page
		ItemStack curPage = new ItemStack(Material.PAPER, viewer.getPage());
        ItemMeta curMeta = curPage.getItemMeta();
        if (curMeta == null) {
        	curMeta = market.getServer().getItemFactory().getItemMeta(curPage.getType());
        }
        curMeta.setDisplayName(ChatColor.WHITE + market.getLocale().get("interface.page", viewer.getPage()));
        List<String> curLore = new ArrayList<String>();
        curLore.add(ChatColor.YELLOW + market.getLocale().get("interface.cur_page"));
        curMeta.setLore(curLore);
        curPage.setItemMeta(curMeta);
        contents[contents.length - 5] = curPage;
		
		// Search
		ItemStack searchItem = new ItemStack(Material.EMPTY_MAP);
		String search = viewer.getSearch();
		if (search == null) {
			ItemMeta meta = searchItem.getItemMeta();
			if (meta == null) {
				meta = market.getServer().getItemFactory().getItemMeta(searchItem.getType());
			}
			meta.setDisplayName(ChatColor.WHITE + market.getLocale().get("interface.search"));
			List<String> lore = new ArrayList<String>();
			lore.add(ChatColor.YELLOW + market.getLocale().get("interface.start_search"));
			meta.setLore(lore);
			searchItem.setItemMeta(meta);
			contents[contents.length - 7] = searchItem;
		} else {
			ItemMeta meta = searchItem.getItemMeta();
			if (meta == null) {
				meta = market.getServer().getItemFactory().getItemMeta(searchItem.getType());
			}
			meta.setDisplayName(ChatColor.WHITE + market.getLocale().get("interface.cancel_search"));
			List<String> lore = new ArrayList<String>();
			lore.add(ChatColor.YELLOW + market.getLocale().get("interface.searching_for", search));
			meta.setLore(lore);
			searchItem.setItemMeta(meta);
			contents[contents.length - 7] = searchItem;
		}
	}
}
