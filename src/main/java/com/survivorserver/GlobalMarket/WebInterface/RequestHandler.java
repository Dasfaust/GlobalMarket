package com.survivorserver.GlobalMarket.WebInterface;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RequestHandler extends Thread {

	private Socket socket;
	WebHandler handler;
	ObjectMapper mapper;
	JsonFactory factory;
	
	public RequestHandler(WebHandler handler, Socket socket) {
		this.handler = handler;
		this.socket = socket;
		mapper = new ObjectMapper();
		factory = mapper.getFactory();
	}
	
	public void run() {
		try {
			handleRequest();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void handleRequest() throws Exception {
		GZIPInputStream in = new GZIPInputStream(socket.getInputStream());
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String recieved = reader.readLine();
		Map<String, Object> reply = new HashMap<String, Object>();
		try {
			processRequest(decodeJson(recieved), reply);
		} catch(Exception e) {
			 reply.put("error", e.getMessage());
			 e.printStackTrace();
		}
		GZIPOutputStream out = new GZIPOutputStream(socket.getOutputStream());
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
		String r = new ObjectMapper().writeValueAsString(reply);
		handler.market.getLogger().info(r);
		writer.append(r);
		writer.newLine();
		writer.close();
		socket.close();
	}
	
	private JsonNode decodeJson(String request) throws Exception {
		JsonParser parser = factory.createJsonParser(request);
		return mapper.readTree(parser);
	}
	
	private void processRequest(JsonNode node, Map<String, Object> reply) throws Exception {
		String function = node.get("function").asText();
		WebViewer viewer = handler.addViewer(node.get("name").asText());
		viewer.connected();
		//viewer.setViewType(ViewType.valueOf(node.get("view").asText()));
		viewer.setVersionId(null);
		java.lang.reflect.Method method;
		method = handler.getClass().getMethod(function, JsonNode.class);
		reply.put("response", method.invoke(handler, node));
	}
}
