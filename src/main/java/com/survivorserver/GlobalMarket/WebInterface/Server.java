package com.survivorserver.GlobalMarket.WebInterface;

import java.net.ServerSocket;

public class Server extends Thread {

	ServerSocket socket;
	WebHandler handler;
	
	public Server(WebHandler handler) {
		this.handler = handler;
	}
	
	public void run() {
		try {
			socket = new ServerSocket(1111);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		while(true) {
			try {
				RequestHandler req = new RequestHandler(handler, socket.accept());
				req.start();
			} catch(Exception e) {
				//e.printStackTrace();
			}
		}
	}
}
