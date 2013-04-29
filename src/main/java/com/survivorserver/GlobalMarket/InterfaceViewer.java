package com.survivorserver.GlobalMarket;

import java.util.Map;

import org.bukkit.inventory.Inventory;

public class InterfaceViewer {
	
	String player;
	Map<Integer, Integer> boundSlots;
	int currentPage = 1;
	Inventory gui;
	InterfaceAction lastAction;
	int lastActionSlot = 0;
	ViewType viewType;
	String search;
	
	public InterfaceViewer(String player, Inventory gui) {
		this.player = player;
		this.gui = gui;
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
	
	public void setViewType(ViewType viewType) {
		this.viewType = viewType;
	}
	
	public ViewType getViewType() {
		return viewType;
	}

	public void setSearch(String search) {
		this.search = search;
	}
	
	public String getSearch() {
		return search;
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
	
	public enum ViewType {
		LISTINGS("listings"), MAIL("mail"),
		REQUESTS("requests");
		
		private String value;
		
		private ViewType(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
	}
}
