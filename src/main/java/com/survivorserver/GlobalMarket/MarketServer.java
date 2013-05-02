package com.survivorserver.GlobalMarket;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.jar.JarFile;

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
		try {
			checkLibraries();
			loadLibraries();
		} catch(Exception e) {
			market.log.warning("Could not load Jackson: " + e.getMessage());
			e.printStackTrace();
			enabled = false;
			return;
		}
		sessions = new HashMap<String, UUID>();
	}

	@Override
	public void run() {
		try {
			socket = new ServerSocket(6789);
		} catch (IOException e) {
			market.log.warning("Could not start server: " + e.getMessage());
			closeSocket();
		}
		while(enabled) {
			try {
				new ServerHandler(this, socket.accept(), market).start();
			} catch (IOException e) {
				market.log.warning("Could not accept client socket: " + e.getMessage());
			}
		}
		closeSocket();
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
	
	public void checkLibraries() throws Exception {
		List<String> libraries = new ArrayList<String>();
		libraries.add("jackson-core");
		libraries.add("jackson-databind");
		libraries.add("jackson-annotations");
		for (String library : libraries) {
			File file = new File("lib/" + library + "-2.1.4.jar");
			if (!file.exists()) {
				market.log.info("Downloading " + library + "-2.1.4.jar...");
				URL site = new URL("http://repo1.maven.org/maven2/com/fasterxml/jackson/core/" + library + "/2.1.4/" + library + "-2.1.4.jar");
				ReadableByteChannel channel = Channels.newChannel(site.openStream());
				FileOutputStream fos = new FileOutputStream("lib/" + library + "-2.1.4.jar");
				fos.getChannel().transferFrom(channel,  0,  1 << 24);
				fos.close();
			}
		}
	}
	
	public void loadLibraries() throws Exception {
		List<JarFile> libraries = new ArrayList<JarFile>();
		File core = new File("lib/jackson-core-2.1.4.jar");
		File databind = new File("lib/jackson-databind-2.1.4.jar");
		File annotations = new File("lib/jackson-annotations-2.1.4.jar");
		if (!core.exists() || !databind.exists() || !annotations.exists()) {
			throw new FileNotFoundException();
		}
		libraries.add(new JarFile(core));
		libraries.add(new JarFile(databind));
		libraries.add(new JarFile(annotations));
		for (JarFile library : libraries) {
			URL[] urls = { new URL("jar:" + new File(library.getName()).toURI().toURL().toExternalForm() + "!/") };
			for (int i = 0; i < urls.length; i++) {
				URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
	        	Class<URLClassLoader> sysclass = URLClassLoader.class;
	        	Method method = sysclass.getDeclaredMethod("addURL", new Class[] { URL.class });
	        	method.setAccessible(true);
	        	method.invoke(sysloader, new Object[] { urls[i] });
			}
		}
	}
}
