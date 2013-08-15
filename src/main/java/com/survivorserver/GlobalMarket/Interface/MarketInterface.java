package com.survivorserver.GlobalMarket.Interface;

import java.util.List;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.survivorserver.GlobalMarket.InterfaceViewer;

public interface MarketInterface {

	String getName();

	String getTitle();

	int getSize();
	
	boolean enableSearch();
	
	boolean doSingleClickActions();
	
	ItemStack prepareItem(MarketItem item, InterfaceViewer viewer, int page, int slot, boolean leftClick, boolean shiftClick);
	
	void handleLeftClickAction(InterfaceViewer viewer, MarketItem item, InventoryClickEvent event);
	
	void handleShiftClickAction(InterfaceViewer viewer, MarketItem item, InventoryClickEvent event);
	
	List<MarketItem> getContents(InterfaceViewer viewer);
	
	List<MarketItem> doSearch(InterfaceViewer viewer, String search);
	
	MarketItem getItem(InterfaceViewer viewer, int id);
	
	boolean identifyItem(ItemMeta meta);
}
