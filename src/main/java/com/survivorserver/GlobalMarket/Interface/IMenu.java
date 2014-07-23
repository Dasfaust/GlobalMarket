package com.survivorserver.GlobalMarket.Interface;

import java.util.HashMap;
import java.util.List;

import com.survivorserver.GlobalMarket.MarketStorage;
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

public abstract class IMenu {

    private HashMap<Integer, IFunctionButton> functionBar;

    public IMenu() {
        functionBar = new HashMap<Integer, IFunctionButton>();
    }

    public abstract String getName();

    public abstract String getTitle();

    public abstract int getSize();

    public abstract boolean doSingleClickActions();

    public abstract ItemStack prepareItem(IMarketItem item, InterfaceViewer viewer, int page, int slot, boolean leftClick, boolean shiftClick);

    public abstract void handleLeftClickAction(InterfaceViewer viewer, IMarketItem item, InventoryClickEvent event);

    public abstract void handleShiftClickAction(InterfaceViewer viewer, IMarketItem item, InventoryClickEvent event);

    public abstract List<IMarketItem> getContents(InterfaceViewer viewer);

    public abstract List<IMarketItem> doSearch(InterfaceViewer viewer, String search);

    public abstract IMarketItem getItem(InterfaceViewer viewer, int id);

    public abstract ItemStack getItemStack(InterfaceViewer viewer, IMarketItem item);

    public abstract void onInterfacePrepare(InterfaceViewer viewer, List<IMarketItem> contents, ItemStack[] invContents, Inventory inv);

    public abstract void onInterfaceClose(InterfaceViewer viewer);

    public abstract void onInterfaceOpen(InterfaceViewer viewer);

    public void addDefaultButtons() {
        functionBar.put(45, new IFunctionButton("PrevPage", null, Material.PAPER) {
            @Override
            public void onClick(Player player, InterfaceHandler handler, InterfaceViewer viewer, int slot, InventoryClickEvent event) {
                viewer.setPage(viewer.getPage() - 1);
                viewer.resetActions();
                handler.refreshViewer(viewer, viewer.getInterface().getName());
            }

            @Override
            public void preBuild(InterfaceHandler handler, InterfaceViewer viewer, ItemStack stack, ItemMeta meta, List<String> lore) {
                meta.setDisplayName(ChatColor.WHITE + Market.getMarket().getLocale().get("interface.page", (viewer.getPage() - 1)));
                lore.add(ChatColor.YELLOW + Market.getMarket().getLocale().get("interface.prev_page"));
            }

            @Override
            public boolean showButton(InterfaceHandler handler, InterfaceViewer viewer, boolean hasPrevPage, boolean hasNextPage) {
                return hasPrevPage;
            }
        });

        functionBar.put(49, new IFunctionButton("CurPage", null, Material.PAPER) {
            @Override
            public void onClick(Player player, InterfaceHandler handler, InterfaceViewer viewer, int slot, InventoryClickEvent event) {
                viewer.setPage(viewer.getPage() - 1);
                viewer.resetActions();
                handler.refreshViewer(viewer, viewer.getInterface().getName());
            }

            @Override
            public void preBuild(InterfaceHandler handler, InterfaceViewer viewer, ItemStack stack, ItemMeta meta, List<String> lore) {
                meta.setDisplayName(ChatColor.WHITE + Market.getMarket().getLocale().get("interface.page", (viewer.getPage() - 1)));
                lore.add(ChatColor.YELLOW +  Market.getMarket().getLocale().get("interface.cur_page"));
            }

            @Override
            public boolean showButton(InterfaceHandler handler, InterfaceViewer viewer, boolean hasPrevPage, boolean hasNextPage) {
                return hasNextPage;
            }
        });

        functionBar.put(53, new IFunctionButton("NextPage", null, Material.PAPER) {
            @Override
            public void onClick(Player player, InterfaceHandler handler, InterfaceViewer viewer, int slot, InventoryClickEvent event) {
                viewer.setPage(viewer.getPage() + 1);
                viewer.resetActions();
                handler.refreshViewer(viewer, viewer.getInterface().getName());
            }

            public void preBuild(InterfaceHandler handler, InterfaceViewer viewer, ItemStack stack, ItemMeta meta, List<String> lore) {
                meta.setDisplayName(ChatColor.WHITE + Market.getMarket().getLocale().get("interface.page", (viewer.getPage() - 1)));
                lore.add(ChatColor.YELLOW + Market.getMarket().getLocale().get("interface.next_page"));

            }

            public boolean showButton(InterfaceHandler handler, InterfaceViewer viewer, boolean hasPrevPage, boolean hasNextPage) {
                return true;
            }
        });

        functionBar.put(47, new IFunctionButton("Search", null, Material.PAPER) {
            @Override
            public void onClick(Player player, InterfaceHandler handler, InterfaceViewer viewer, int slot, InventoryClickEvent event) {
                if (viewer.getSearch() == null) {
                    player.closeInventory();
                    Market.getMarket().startSearch(player, viewer.getInterface().getName());
                    handler.removeViewer(viewer);
                } else {
                    viewer.setSearch(null);
                    viewer.resetActions();
                    handler.refreshViewer(viewer, viewer.getInterface().getName());
                }
            }

            @Override
            public void preBuild(InterfaceHandler handler, InterfaceViewer viewer, ItemStack stack, ItemMeta meta, List<String> lore) {
                if (viewer.getSearch() == null) {
                    meta.setDisplayName(ChatColor.WHITE + Market.getMarket().getLocale().get("interface.search"));
                    lore.add(ChatColor.YELLOW + Market.getMarket().getLocale().get("interface.start_search"));
                } else {
                    meta.setDisplayName(ChatColor.WHITE + Market.getMarket().getLocale().get("interface.cancel_search"));
                    lore.add(ChatColor.YELLOW + Market.getMarket().getLocale().get("interface.searching_for", viewer.getSearch()));
                }
            }

            @Override
            public boolean showButton(InterfaceHandler handler, InterfaceViewer viewer, boolean hasPrevPage, boolean hasNextPage) {
                return true;
            }
        });
    }

    public void onUnboundClick(Market market, InterfaceHandler handler, InterfaceViewer viewer, int slot, InventoryClickEvent event) {
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        /*if (event.getAction() == InventoryAction.SWAP_WITH_CURSOR) {
            return;
        }*/
        int clicked = slot;
        if (functionBar.containsKey(clicked)) {
            functionBar.get(clicked).onClick(player, handler, viewer, slot, event);
        }
    }

    public void addFunctionButton(int slot, IFunctionButton button) {
        if (functionBar.containsKey(slot)) {
            functionBar.remove(slot);
        }
        functionBar.put(slot, button);
    }

    public void removeFunctionButton(int slot) {
        functionBar.remove(slot);
    }

    public void buildFunctionBar(Market market, InterfaceHandler handler, InterfaceViewer viewer, ItemStack[] contents, boolean pPage, boolean nPage) {
        int invSize = contents.length - 1;
        for (int i = (invSize - 8); i < invSize; i++) {
            if (functionBar.containsKey(i)) {
                IFunctionButton button = functionBar.get(i);
                contents[i] = button.buildItem(handler, viewer, contents, pPage, nPage);
            }
        }
    }
}
