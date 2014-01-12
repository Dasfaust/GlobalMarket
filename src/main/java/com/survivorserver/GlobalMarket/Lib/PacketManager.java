package com.survivorserver.GlobalMarket.Lib;

import com.comphenix.protocol.ProtocolLibrary;
import com.survivorserver.GlobalMarket.Market;

public class PacketManager {
	
	Market market;
	SignInput input;
	BossMessage message;
	
	public PacketManager(Market market) {
		this.market = market;
		input = new SignInput(market);
		message = new BossMessage(market);
	}
	
	public SignInput getSignInput() {
		return input;
	}
	
	public BossMessage getMessage() {
		return message;
	}
	
	public void unregister() {
		ProtocolLibrary.getProtocolManager().removePacketListeners(market);
	}
}
