package com.survivorserver.GlobalMarket;

import java.util.Map;

import org.bukkit.inventory.Inventory;

import com.survivorserver.GlobalMarket.Interface.MarketItem;

public class InterfaceViewer {
	
	String player;
	Map<Integer, Integer> boundSlots;
	int currentPage = 1;
	Inventory gui;
	InterfaceAction lastAction;
	int lastActionSlot = 0;
	String search;
	MarketItem lastClicked;
	String interfaceName;
	
	public InterfaceViewer(String player, Inventory gui, String interfaceName) {
		this.player = player;
		this.gui = gui;
		this.interfaceName = interfaceName;
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
	
	public String getViewer() {
		return player;
	}
	
	public Inventory getGui() {
		return gui;
	}
	
	public void setGui(Inventory gui) {
		this.gui = gui;
	}
	
	public InterfaceAction getLastAction() {
		return lastAction;
	}
	
	public void setLastAction(InterfaceAction action) {
		lastAction = action;
	}
	
	public int getLastActionSlot() {
		return lastActionSlot;
	}
	
	public void setLastActionSlot(int slot) {
		this.lastActionSlot = slot;
	}
	
	public void setLastItem(MarketItem item) {
		lastClicked = item;
	}
	
	public MarketItem getLastItem() {
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
		setLastItem(null);
	}
	
	public enum InterfaceAction {
		LEFTCLICK("leftclick"), RIGHTCLICK("rightclick"),
		MIDDLECLICK("middleclick"), SHIFTCLICK("shiftclick");
		
		private String value;
		
		private InterfaceAction(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
	}
}
