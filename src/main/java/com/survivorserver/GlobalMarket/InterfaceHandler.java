package com.survivorserver.GlobalMarket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.survivorserver.GlobalMarket.Events.ViewerRemoveEvent;
import com.survivorserver.GlobalMarket.Interface.MarketInterface;
import com.survivorserver.GlobalMarket.Interface.MarketItem;
import com.survivorserver.GlobalMarket.InterfaceViewer.InterfaceAction;

public class InterfaceHandler {

	Market market;
	MarketStorage storage;
	List<InterfaceViewer> viewers;
	Set<MarketInterface> interfaces;
	
	public InterfaceHandler(Market market, MarketStorage storage) {
		this.market = market;
		this.storage = storage;
		viewers = new ArrayList<InterfaceViewer>();
		interfaces = new HashSet<MarketInterface>();
	}
	
	public void registerInterface(MarketInterface gui) {
		interfaces.add(gui);
	}
	
	public void unregisterInterface(MarketInterface gui) {
		interfaces.remove(gui);
	}
	
	public MarketInterface getInterface(String name) {
		for (MarketInterface gui : interfaces) {
			if (gui.getName().equalsIgnoreCase(name)) {
				return gui;
			}
		}
		return null;
	}
	
	public Set<MarketInterface> getInterfaces() {
		return interfaces;
	}
	
	public InterfaceViewer addViewer(String player, Inventory gui, String interfaceName) {
		for (InterfaceViewer viewer : viewers) {
			if (viewer.getViewer().equalsIgnoreCase(player)) {
				gui = null;
				return viewer;
			}
		}
		InterfaceViewer viewer = new InterfaceViewer(player, gui, interfaceName);
		viewers.add(viewer);
		return viewer;
	}
	
	public void addViewer(InterfaceViewer v) {
		for (InterfaceViewer viewer : viewers) {
			if (viewer.getViewer().equalsIgnoreCase(v.getViewer())) {
				viewers.remove(v);
			}
		}
		viewers.add(v);
	}
	
	public InterfaceViewer findViewer(String player) {
		for (InterfaceViewer viewer : viewers) {
			if (viewer.getViewer().equalsIgnoreCase(player)) {
				return viewer;
			}
		}
		return null;
	}
	
	public synchronized void removeViewer(InterfaceViewer viewer) {
		market.getServer().getPluginManager().callEvent(new ViewerRemoveEvent(viewer.getViewer()));
		viewers.remove(viewer);
	}
	
	public synchronized void openGui(InterfaceViewer viewer) {
		market.getServer().getPlayer(viewer.getViewer()).openInventory(viewer.getGui());
	}
	
	public void openInterface(Player player, String search, String marketInterface) {
		MarketInterface gui = getInterface(marketInterface);
		InterfaceViewer viewer = addViewer(player.getName(), market.getServer().createInventory(player, gui.getSize(), gui.getTitle()), marketInterface);
		viewer.setSearch(search);
		refreshInterface(viewer, gui);
		openGui(viewer);
	}
	
	public void refreshInterface(InterfaceViewer viewer, MarketInterface gui) {
		Map<Integer, Integer> boundSlots = new HashMap<Integer, Integer>();
		List<MarketItem> contents = gui.getContents(viewer);
		Inventory inv = viewer.getGui();
		inv.clear();
		ItemStack[] invContents = new ItemStack[viewer.getGui().getSize()];
		if (gui.enableSearch()) {
			setSearch(viewer.getSearch(), invContents);
		}
		if (viewer.getSearch() != null) {
			contents = gui.doSearch(viewer, viewer.getSearch());
		}
		int slot = 0;
		int p = 0;
		int n = viewer.getPage() * (invContents.length - 9);
		for (MarketItem marketItem : contents) {
			if (n > (invContents.length - 9) && p < n - (invContents.length - 9)) {
				p++;
				continue;
			}
			p++;
			if (slot < (invContents.length - 9)) {
				boundSlots.put(slot, marketItem.getId());
				boolean left = false;
				boolean shift = false;
				if (viewer.getLastAction() != null && viewer.getLastAction() == InterfaceAction.LEFTCLICK && viewer.getLastActionSlot() == slot && (viewer.getLastItem() != null && viewer.getLastItem().getId() == marketItem.getId())) {
					left = true;
				}
				if (viewer.getLastAction() != null && viewer.getLastAction() == InterfaceAction.SHIFTCLICK && viewer.getLastActionSlot() == slot && (viewer.getLastItem() != null && viewer.getLastItem().getId() == marketItem.getId())) {
					shift = true;
				}
				invContents[slot] = gui.prepareItem(marketItem, viewer, p, slot, left, shift);
			}
			slot++;
		}
		setCurPage(invContents, viewer);
		if (n < contents.size()) {
			setNextPage(invContents, viewer);
		}
		if (n > (invContents.length - 9)) {
			setPrevPage(invContents, viewer);
		}
		viewer.setBoundSlots(boundSlots);
		inv.setContents(invContents);
	}

	public void setNextPage(ItemStack[] contents, InterfaceViewer viewer) {
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
	
	public void setCurPage(ItemStack[] contents, InterfaceViewer viewer) {
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
	}
	
	public void setPrevPage(ItemStack[] contents, InterfaceViewer viewer) {
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
	
	public void setSearch(String search, ItemStack[] contents) {
		ItemStack searchItem = new ItemStack(Material.PAPER);
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
	
	public void refreshViewer(InterfaceViewer viewer) {
		MarketInterface gui = getInterface(viewer.getInterface());
		refreshInterface(viewer, gui);
	}
	
	public void updateAllViewers() {
		List<InterfaceViewer> inactive = new ArrayList<InterfaceViewer>();
		for (InterfaceViewer viewer : viewers) {
			Player player = market.getServer().getPlayer(viewer.getViewer());
			if (player == null) {
				inactive.add(viewer);
				continue;
			} else if (player.getOpenInventory() == null) {
				inactive.add(viewer);
				continue;
			}
			refreshViewer(viewer);
		}
		if (!inactive.isEmpty()) {
			for (InterfaceViewer viewer : inactive) {
				removeViewer(viewer);
			}
		}
	}
	
	public void closeAllInterfaces() {
		for (InterfaceViewer viewer : viewers) {
			Player player = market.getServer().getPlayer(viewer.getViewer());
			if (player != null) {
				player.closeInventory();
				player.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + market.getLocale().get("interface_closed_due_to_reload"));
			}
		}
	}
	
	public List<InterfaceViewer> getAllViewers() {
		return viewers;
	}
	
	public boolean isAdmin(String name) {
		Player player = market.getServer().getPlayer(name);
		if (player != null) {
			return player.hasPermission("globalmarket.admin");
		}
		return false;
	}
}
