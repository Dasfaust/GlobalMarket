package com.survivorserver.GlobalMarket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.survivorserver.GlobalMarket.Events.InterfaceCreateEvent;
import com.survivorserver.GlobalMarket.Events.ViewerRemoveEvent;
import com.survivorserver.GlobalMarket.InterfaceViewer.InterfaceAction;
import com.survivorserver.GlobalMarket.InterfaceViewer.ViewType;

public class InterfaceHandler {

	Market market;
	MarketStorage storage;
	List<InterfaceViewer> viewers;
	int maxListingsPerPage = 45;
	UUID versionId;
	
	public InterfaceHandler(Market market, MarketStorage storage) {
		this.market = market;
		this.storage = storage;
		viewers = new ArrayList<InterfaceViewer>();
		setVersionId();
	}
	
	public void setVersionId() {
		versionId = UUID.randomUUID();
	}
	
	public UUID getVersionId() {
		return versionId;
	}
	
	public InterfaceViewer addViewer(String player, Inventory gui) {
		for (InterfaceViewer viewer : viewers) {
			if (viewer.getViewer().equalsIgnoreCase(player)) {
				gui = null;
				return viewer;
			}
		}
		InterfaceViewer viewer = new InterfaceViewer(player, gui);
		viewers.add(viewer);
		return viewer;
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
	
	public void prepareListings(InterfaceViewer viewer) {
		Map<Integer, Integer> boundSlots = new HashMap<Integer, Integer>();
		List<Listing> listings = storage.getAllListings();
		if (viewer.getSearch() != null) {
			listings = storage.getAllListings(viewer.getSearch(), listings);
		}
		market.getServer().getPluginManager().callEvent(new InterfaceCreateEvent(viewer, listings));
		Inventory gui = viewer.getGui();
		gui.clear();
		ItemStack[] contents = new ItemStack[54];
		setSearch(viewer.getSearch(), contents);
		int slot = 0;
		int p = 0;
		int n = viewer.getPage() * maxListingsPerPage;
		for (Listing listing : listings) {
			if (n > maxListingsPerPage && p < n - maxListingsPerPage) {
				p++;
				continue;
			}
			p++;
			if (slot < maxListingsPerPage) {
				boundSlots.put(slot, listing.getId());
				ItemStack item = listing.getItem();
				if (item == null || item.getType() == Material.AIR) {
					storage.removeListing(viewer.getViewer(), listing.getId());
					market.log.warning("The item in listing " + listing.getId() + " is null, removing");
					continue;
				}
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
					if (viewer.getLastAction() != null && viewer.getLastAction() == InterfaceAction.LEFTCLICK && viewer.getLastActionSlot() == slot && (viewer.getLastListing() != null && viewer.getLastListing().getId() == listing.getId())) {
						if (market.getEcon().has(viewer.getViewer(), listing.price)) {
							buyMsg = ChatColor.GREEN + market.getLocale().get("click_again_to_confirm");
						} else {
							buyMsg = ChatColor.RED + market.getLocale().get("not_enough_money", market.getEcon().currencyNamePlural());
							viewer.setLastAction(InterfaceAction.RIGHTCLICK);
						}
					}
					lore.add(buyMsg);
				}
				if (viewer.getViewer().equalsIgnoreCase(listing.seller) || isAdmin(viewer.getViewer())) {
					String removeMsg = ChatColor.DARK_GRAY + market.getLocale().get("shift_click_to_remove");
					if (viewer.getLastAction() != null && viewer.getLastAction() == InterfaceAction.SHIFTCLICK && viewer.getLastActionSlot() == slot && (viewer.getLastListing() != null && viewer.getLastListing().getId() == listing.getId())) {
						removeMsg = ChatColor.GREEN + market.getLocale().get("shift_click_again_to_confirm");
					}
					lore.add(removeMsg);
				}
				if (listing.getSeller().equalsIgnoreCase(market.getInfiniteSeller())) {
					lore.add(ChatColor.LIGHT_PURPLE + market.getLocale().get("interface.infinite"));
				}
				meta.setLore(lore);
				item.setItemMeta(meta);
				contents[slot] = item;
			}
			slot++;
		}
		setCurPage(contents, viewer);
		if (n < listings.size()) {
			setNextPage(contents, viewer);
		}
		if (n > maxListingsPerPage) {
			setPrevPage(contents, viewer);
		}
		viewer.setBoundSlots(boundSlots);
		gui.setContents(contents);
	}
	
	public void showListings(Player player, String search) {
		InterfaceViewer viewer = addViewer(player.getName(), market.getServer().createInventory(player, 54, market.getLocale().get("interface.listings_title")));
		viewer.setViewType(ViewType.LISTINGS);
		viewer.setSearch(search);
		prepareListings(viewer);
		openGui(viewer);
	}
	
	public void prepareMail(InterfaceViewer viewer) {
		Map<Integer, Integer> boundSlots = new HashMap<Integer, Integer>();
		Inventory gui = viewer.getGui();
		gui.clear();
		Map<Integer, ItemStack> mail = storage.getAllMailFor(viewer.getViewer());
		ItemStack[] contents = new ItemStack[54];
		int slot = 0;
		int p = 0;
		int n = viewer.getPage() * maxListingsPerPage;
		for (Entry<Integer, ItemStack> entry : mail.entrySet()) {
			if (n > maxListingsPerPage && p < n - maxListingsPerPage) {
				p++;
				continue;
			}
			p++;
			if (slot < maxListingsPerPage) {
				boundSlots.put(slot, entry.getKey());
				if (entry.getValue() == null || entry.getValue().getType() == Material.AIR) {
					storage.removeMail(viewer.getViewer(), entry.getKey());
					market.log.warning("The item in " + viewer.getViewer() + "'s mail id " + entry.getKey() + " is null");
					continue;
				}
				ItemStack item = new ItemStack(entry.getValue());
				ItemMeta meta = item.getItemMeta().clone();
				List<String> lore = meta.getLore();
				if (!meta.hasLore()) {
					lore = new ArrayList<String>();
				}
				if (meta instanceof BookMeta) {
					BookMeta bookMeta = (BookMeta) meta;
					if (bookMeta.hasTitle()) {
						if (bookMeta.getTitle().equalsIgnoreCase(market.getLocale().get("transaction_log.item_name"))) {
							double amount = storage.getPaymentAmount(entry.getKey(), viewer.getViewer());
							if (amount > 0) {
								lore.add(ChatColor.WHITE + market.getLocale().get("amount") + market.getEcon().format(amount));
							}
						}
					}
				}
				String instructions = ChatColor.YELLOW + market.getLocale().get("click_to_retrieve");
				if (viewer.getLastAction() != null && viewer.getLastAction() == InterfaceAction.LEFTCLICK && viewer.getLastActionSlot() == slot) {
					instructions = ChatColor.RED + market.getLocale().get("full_inventory");
				}
				lore.add(instructions);
				meta.setLore(lore);
				item.setItemMeta(meta);
				contents[slot] = item;
			}
			slot++;
		}
		setCurPage(contents, viewer);
		if (n < storage.getNumMail(viewer.getViewer())) {
			setNextPage(contents, viewer);
		}
		if (n > maxListingsPerPage) {
			setPrevPage(contents, viewer);
		}
		viewer.setBoundSlots(boundSlots);
		gui.setContents(contents);
	}
	
	public void showMail(Player player) {
		InterfaceViewer viewer = addViewer(player.getName(), market.getServer().createInventory(player, 54, market.getLocale().get("interface.mail_title")));
		viewer.setViewType(ViewType.MAIL);
		prepareMail(viewer);
		openGui(viewer);
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
		contents[53] = nextPage;
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
		contents[49] = curPage;
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
		contents[45] = prevPage;
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
			contents[47] = searchItem;
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
			contents[47] = searchItem;
		}
	}
	
	public void refreshViewer(InterfaceViewer viewer) {
		if (viewer.getViewType() == ViewType.MAIL) {
			prepareMail(viewer);
		} else {
			prepareListings(viewer);
		}
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
			if (viewer.getViewType() == ViewType.MAIL) {
				prepareMail(viewer);
			} else {
				prepareListings(viewer);
			}
		}
		if (!inactive.isEmpty()) {
			for (InterfaceViewer viewer : inactive) {
				removeViewer(viewer);
			}
		}
		setVersionId();
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
