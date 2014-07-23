package com.survivorserver.GlobalMarket;

import java.util.Map;

import com.survivorserver.GlobalMarket.Interface.IMenu;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;

import com.survivorserver.GlobalMarket.Lib.SortMethod;

public class InterfaceViewer {
	
    String name;
    String player;
    Map<Integer, Integer> boundSlots;
    int currentPage = 1;
    Inventory gui;
    InventoryAction lastAction;
    int lastActionSlot = -1;
    String search;
    int lastClicked;
    int clicks = 0;
    String world;
    IMenu mInterface;
    int searchSize = 0;
    SortMethod sort;
    int lastLowerSlot = -1;
    String createMessage;

    public InterfaceViewer(String name, String player, Inventory gui, IMenu mInterface, String world) {
        this.name = name;
        this.player = player;
        this.gui = gui;
        this.world = world;
        this.mInterface = mInterface;
        sort = SortMethod.DEFAULT;
    }

    public void setBoundSlots(Map<Integer, Integer> boundSlots) {
        this.boundSlots = boundSlots;
    }

    public Map<Integer, Integer> getBoundSlots() {
        return boundSlots;
    }

    public void setPage(int page) {
        currentPage = page;
    }

    public int getPage() {
        return currentPage;
    }

    public String getName() {
        return name;
    }

    public String getViewer() {
        return player;
    }

    public Inventory getGui() {
        return gui;
    }

    public void setGui(Inventory gui) {
        this.gui = gui;
    }

    public InventoryAction getLastAction() {
        return lastAction;
    }

    public void setLastAction(InventoryAction action) {
        lastAction = action;
    }

    public int getLastActionSlot() {
        return lastActionSlot;
    }

    public void setLastActionSlot(int slot) {
        this.lastActionSlot = slot;
    }

    public void setLastItem(int id) {
        lastClicked = id;
    }

    public int getLastItem() {
        return lastClicked;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getSearch() {
        return search;
    }

    public IMenu getInterface() {
        return mInterface;
    }

    public void resetActions() {
        setLastAction(null);
        setLastActionSlot(-1);
        setLastItem(-1);
        clicks = 0;
        lastLowerSlot = -1;
        createMessage = null;
    }

    public int getClicks() {
        return clicks;
    }

    public void incrementClicks() {
        clicks++;
    }

    public String getWorld() {
        return world;
    }

    public int getSearchSize() {
        return searchSize;
    }

    public void setSearchSize(int searchSize) {
        this.searchSize = searchSize;
    }

    public SortMethod getSort() {
        return sort;
    }

    public void setSort(SortMethod sort) {
        this.sort = sort;
    }

    public String getCreateMessage() {
        return createMessage;
    }

    public void setCreateMessage(String createMessage) {
        this.createMessage = createMessage;
    }

    public void setLastLower(int lastLowerSlot) {
        this.lastLowerSlot = lastLowerSlot;
    }

    public int getLastLowerSlot() {
        return lastLowerSlot;
    }
}
