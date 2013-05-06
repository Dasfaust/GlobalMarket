package com.survivorserver.GlobalMarket;

import java.util.UUID;

public class WebViewer {
	
	String player;
	UUID versionId;
	
	public WebViewer (String player, UUID versionId) {
		this.player = player;
		this.versionId = versionId;
	}
	
	public String getViewer() {
		return player;
	}
	
	public UUID getVersionId() {
		return versionId;
	}
	
	public void setVersionId(UUID versionId) {
		this.versionId = versionId;
	}
}
