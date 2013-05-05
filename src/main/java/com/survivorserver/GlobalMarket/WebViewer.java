package com.survivorserver.GlobalMarket;

public class WebViewer {
	
	String player;
	boolean changed;
	
	public WebViewer (String player) {
		this.player = player;
		changed = false;
	}
	
	public String getViewer() {
		return player;
	}
	
	public boolean hasChanged() {
		return changed;
	}
	
	public void doUpdate() {
		changed = true;
	}
}
