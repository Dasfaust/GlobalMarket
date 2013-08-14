package com.survivorserver.GlobalMarket.WebInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.survivorserver.GlobalMarket.Market;

public class WebHandler {

	Market market;
	Server server;
	List<WebViewer> viewers;
	
	public WebHandler(Market market) {
		this.market = market;
		if (!LibraryManager.loadLibraries(market)) {
			market.getLogger().warning("Can't start web interface server!");
			return;
		}
		if (market.serverEnabled()) {
			server = new Server(this);
			server.start();
		}
		viewers = new ArrayList<WebViewer>();
	}
	
	public void stopServer() {
		if (server != null && server.socket.isBound()) {
			try {
				server.socket.close();
			} catch (IOException ignored) { }
		}
	}
	
	public Market getMarket() {
		return market;
	}
	
	public WebViewer addViewer(String player) {
		for (WebViewer viewer : viewers) {
			if (viewer.getName().equalsIgnoreCase(player)) {
				return viewer;
			}
		}
		WebViewer viewer = new WebViewer(player);
		viewers.add(viewer);
		return viewer;
	}
	
	public WebViewer findViewer(String player) {
		for (WebViewer viewer : viewers) {
			if (viewer.getName().equalsIgnoreCase(player)) {
				return viewer;
			}
		}
		return null;
	}
	
	public UUID generateNewSession(JsonNode node) {
		return findViewer(node.get("name").asText()).newSessionId();
	}
	
	public boolean isSessionValid(String name, String session) {
		WebViewer viewer = findViewer(name);
		UUID s = UUID.fromString(session);
		UUID c = viewer.getSessionId();
		if (c != null) {
			if (c.equals(s)) {
				return true;
			}
		}
		return false;
	}
	
	public String poll(JsonNode node) {
		String name = node.get("name").asText();
		String session = node.get("session").asText();
		WebViewer viewer = findViewer(name);
		while(System.currentTimeMillis() - viewer.getLastConnected() <= 29000) {
			if (!isSessionValid(name, session)) {
				return "Invalid session";
			}
			/*if (!viewer.getVersionId().equals(market.getInterfaceHandler().getVersionId())) {
				return "Market updated";
			}*/
		}
		return null;
	}
}
