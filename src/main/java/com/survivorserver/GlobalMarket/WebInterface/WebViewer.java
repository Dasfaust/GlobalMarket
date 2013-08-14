package com.survivorserver.GlobalMarket.WebInterface;

import java.util.UUID;

public class WebViewer {
	
	String name;
	UUID sessionId;
	UUID versionId;
	Long lastConnection;
	
	public WebViewer(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public UUID getSessionId() {
		return sessionId;
	}
	
	public UUID newSessionId() {
		sessionId = UUID.randomUUID();
		return sessionId;
	}
	
	public void connected() {
		lastConnection = System.currentTimeMillis();
	}
	
	public Long getLastConnected() {
		return lastConnection;
	}
	
	public UUID getVersionId() {
		return versionId;
	}
	
	public void setVersionId(UUID ver) {
		versionId = ver;
	}
}
