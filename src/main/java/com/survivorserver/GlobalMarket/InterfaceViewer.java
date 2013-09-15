package com.survivorserver.GlobalMarket;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;

public class InterfaceViewer {
	
	String name;
	String player;
	Map<Integer, Integer> boundSlots;
	int currentPage = 1;
	Inventory gui;
	InventoryAction lastAction;
	int lastActionSlot = 0;
	String search;
	int lastClicked;
	String interfaceName;
	int clicks = 0;
	Map<String, Object> customData;
	
	public InterfaceViewer(String name, String player, Inventory gui, String interfaceName) {
		this.name = name;
		this.player = player;
		this.gui = gui;
		this.interfaceName = interfaceName;
		customData = new HashMap<String, Object>();
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
	
	public String getInterface() {
		return interfaceName;
	}
	
	public void setInterface(String name) {
		interfaceName = name;
	}
	
	public void resetActions() {
		setLastAction(null);
		setLastActionSlot(-1);
		setLastItem(-1);
		clicks = 0;
	}
	
	public int getClicks() {
		return clicks;
	}
	
	public void incrementClicks() {
		clicks++;
	}
	
	public void set(String label, Object data) {
		if (customData.containsKey(label)) {
			customData.remove(label);
		}
		customData.put(label, data);
	}
	
	public boolean isSet(String label) {
		return customData.containsKey(label);
	}
	
	public String getString(String label) {
		return (String) customData.get(label);
	}
	
	public int getInt(String label) {
		return (Integer) customData.get(label);
	}
	
	public double getDouble(String label) {
		return (Double) customData.get(label);
	}
	
	public String[] getStringArray(String label) {
		return (String[]) customData.get(label);
	}
}
