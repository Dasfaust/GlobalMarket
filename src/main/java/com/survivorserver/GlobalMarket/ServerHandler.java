package com.survivorserver.GlobalMarket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ServerHandler extends Thread {

	Market market;
	private Socket socket;
	MarketServer server;

	public ServerHandler(MarketServer server, Socket socket, Market market) {
		this.server = server;
		this.market = market;
		this.socket = socket;
	}

	public void run() {
		try {
			handleClient();
			socket.close();
			//market.log.info("Request closed");
		} catch(Exception e) {
			market.log.warning("Could not handle client: " + e.getMessage());
		}
	}

	public void handleClient() throws Exception {
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		String recieved = in.readLine();
		//market.log.info("Request: " + recieved);
		Map<String, Object> reply = new HashMap<String, Object>();
		ObjectMapper mapper = new ObjectMapper();
		JsonFactory factory = mapper.getFactory();
		JsonParser parser = null;
		try {
			parser = factory.createJsonParser(recieved);
		} catch (Exception e) {
			reply.put("failure", "JSON error");
		}
		if (parser != null) {
			JsonNode node = null;
			try {
				node = mapper.readTree(parser);
			} catch (Exception e) {
				reply.put("failure", "JSON error");
			}
			if (node != null) {
				String function = node.get("function").asText();
				List<String> args = node.findValuesAsText("args");
				if (function.equalsIgnoreCase("getAllListings")) {
					reply.put("success", server.storage.getAllListings());
				} else if (function.equalsIgnoreCase("getSessionId")) {
					reply.put("success", server.getSessionId(args.get(0)));
				} else if (function.equalsIgnoreCase("generateSessionId")) {
					reply.put("success", server.generateSessionId(args.get(0)));
				} else if (function.equalsIgnoreCase("getAllMail")) {
					reply.put("success", server.storage.getAllMailFor(args.get(0)));
				} else if (function.equalsIgnoreCase("getBalance")) {
					reply.put("success", market.getEcon().getBalance(args.get(0)));
				} else if (function.equalsIgnoreCase("format")) {
					reply.put("success", market.getEcon().format(Double.parseDouble(args.get(0))));
				} else if (function.equalsIgnoreCase("doPoll")) {
					WebViewer viewer = server.addViewer(args.get(0));
					long started = System.currentTimeMillis();
					waiting:
					while(System.currentTimeMillis() - started <= 29500) {
						if (!viewer.getVersionId().toString().equalsIgnoreCase(server.currentVersion().toString())) {
							viewer.setVersionId(server.currentVersion());
							reply.put("success", "doRefresh");
							break waiting;
						}
					}
					if (reply.isEmpty()) {
						reply.put("failure", "No changes");
					}
					viewer.updateLastSeen();
				} else {
					reply.put("failure", "Function " + function + " not found");
				}
			}
		}
		write(reply);
	}
	
	public void write(Map<String, Object> reply) throws IOException {
		PrintWriter writer = new PrintWriter(socket.getOutputStream());
		writer.println(new ObjectMapper().writeValueAsString(reply));
		writer.flush();
	}
}
