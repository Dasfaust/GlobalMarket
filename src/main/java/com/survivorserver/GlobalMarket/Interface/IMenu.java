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
        addFunctionButton(45, new IFunctionButton("PrevPage", null, Material.PAPER) {
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

        addFunctionButton(49, new IFunctionButton("CurPage", null, Material.PAPER) {
            @Override
            public void onClick(Player player, InterfaceHandler handler, InterfaceViewer viewer, int slot, InventoryClickEvent event) {
                viewer.setPage(viewer.getPage() - 1);
                viewer.resetActions();
                handler.refreshViewer(viewer, viewer.getInterface().getName());
            }

            @Override
            public void preBuild(InterfaceHandler handler, InterfaceViewer viewer, ItemStack stack, ItemMeta meta, List<String> lore) {
                meta.setDisplayName(ChatColor.WHITE + Market.getMarket().getLocale().get("interface.page", (viewer.getPage())));
                lore.add(ChatColor.YELLOW +  Market.getMarket().getLocale().get("interface.cur_page"));
            }

            @Override
            public boolean showButton(InterfaceHandler handler, InterfaceViewer viewer, boolean hasPrevPage, boolean hasNextPage) {
                return true;
            }
        });

        addFunctionButton(53, new IFunctionButton("NextPage", null, Material.PAPER) {
            @Override
            public void onClick(Player player, InterfaceHandler handler, InterfaceViewer viewer, int slot, InventoryClickEvent event) {
                viewer.setPage(viewer.getPage() + 1);
                viewer.resetActions();
                handler.refreshViewer(viewer, viewer.getInterface().getName());
            }

            public void preBuild(InterfaceHandler handler, InterfaceViewer viewer, ItemStack stack, ItemMeta meta, List<String> lore) {
                meta.setDisplayName(ChatColor.WHITE + Market.getMarket().getLocale().get("interface.page", (viewer.getPage() + 1)));
                lore.add(ChatColor.YELLOW + Market.getMarket().getLocale().get("interface.next_page"));

            }

            public boolean showButton(InterfaceHandler handler, InterfaceViewer viewer, boolean hasPrevPage, boolean hasNextPage) {
                return hasNextPage;
            }
        });
    }

    public void onUnboundClick(Market market, InterfaceHandler handler, InterfaceViewer viewer, int slot, InventoryClickEvent event) {
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        int clicked = slot;
        if (functionBar.containsKey(clicked)) {
            functionBar.get(clicked).onClick(player, handler, viewer, slot, event);
        }
    }

    public void buildFunctionBar(InterfaceHandler handler, InterfaceViewer viewer, ItemStack[] contents, boolean pPage, boolean nPage) {
        int invSize = viewer.getGui().getSize();
        for (int i = (invSize - 9); i < invSize; i++) {
            if (functionBar.containsKey(i)) {
                IFunctionButton button = functionBar.get(i);
                contents[i] = button.buildItem(handler, viewer, contents, pPage, nPage);
            }
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
}
