package com.survivorserver.GlobalMarket;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/*import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;*/

public class MarketServer extends Thread {
	
	Market market;
	MarketStorage storage;
	boolean enabled;
	ServerSocket socket;
	Map<String, UUID> sessions;
	
	public MarketServer(Market market, MarketStorage storage) {
		this.storage = storage;
		this.market = market;
		enabled = market.getConfig().getBoolean("server.enable");
		sessions = new HashMap<String, UUID>();
	}

	@Override
	public void run() {
		/*try {
			socket = new ServerSocket(6789);
		} catch (IOException e) {
			market.log.warning("Could not start server: " + e.getMessage());
			closeSocket();
		}*/
		while(enabled) {
			try {
				socket = new ServerSocket(6789);
			} catch (IOException e) {
				market.log.warning("Could not start server: " + e.getMessage());
				closeSocket();
			}
			try {
				tickServer();
			} catch(Exception e) {
				market.log.warning("Could not tick server: " + e.getMessage());
				closeSocket();
				continue;
			}
			closeSocket();
		}
		closeSocket();
	}
	
	public void tickServer() throws Exception {
	/*	Socket connection = socket.accept();
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		DataOutputStream out = new DataOutputStream(connection.getOutputStream());
		String recieved = in.readLine();
		market.getLogger().info("Request: " + recieved);
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
					reply.put("success", storage.getAllListings());
				} else if (function.equalsIgnoreCase("getSessionId")) {
					reply.put("success", getSessionId(args.get(0)));
				} else if (function.equalsIgnoreCase("generateSessionId")) {
					reply.put("success", generateSessionId(args.get(0)));
				} else if (function.equalsIgnoreCase("getAllMail")) {
					reply.put("success", storage.getAllMailFor(args.get(0)));
				} else {
					reply.put("failure", "Function " + function + " not found");
				}
			}
		}
		out.writeBytes(new ObjectMapper().writeValueAsString(reply) + "\n");*/
	}
	
	public String sendMailToInventory() {
		return "";
	}
	
	public String getSessionId(String player) {
		if (sessions.containsKey(player)) {
			return sessions.get(player).toString();
		}
		UUID uuid = UUID.randomUUID();
		sessions.put(player, UUID.randomUUID());
		return uuid.toString();
	}
	
	public String generateSessionId(String player) {
		if (sessions.containsKey(player)) {
			sessions.remove(player);
		}
		UUID uuid = UUID.randomUUID();
		sessions.put(player, UUID.randomUUID());
		return uuid.toString();
	}
	
	public void closeSocket() {
		try {
			socket.close();
		} catch (IOException e) {
			market.log.warning("Could not close server: " + e.getMessage());
		}
	}
	
	public void setDisabled() {
		enabled = false;
		closeSocket();
	}
}
