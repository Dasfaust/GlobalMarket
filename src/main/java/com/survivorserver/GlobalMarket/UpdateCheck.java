package com.survivorserver.GlobalMarket;

import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class UpdateCheck extends BukkitRunnable {

    Market market;
    String player;
    
    public UpdateCheck(Market market, String player) {
            this.market = market;
            this.player = player;
            this.runTaskLaterAsynchronously(market, 1);
    }
    
    @Override
    public void run() {
		try {
            String version = market.getDescription().getVersion();
            InputStream stream = new URL("http://dev.bukkit.org/bukkit-plugins/global-market/files.rss").openConnection().getInputStream();
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
            NodeList nodes = doc.getElementsByTagName("item").item(0).getChildNodes();
            if (Integer.parseInt(version.replace("-SNAPSHOT", "").replace(".", "")) < Integer.parseInt(nodes.item(1).getTextContent()
            		.replace("GlobalMarket v", "").replace(".", ""))) {
                Player p = market.getServer().getPlayer(player);
                if (p != null) {
                	p.sendMessage(market.prefix + nodes.item(1).getTextContent() + " has been released! Your version is " + version);
                }
            }
        } catch (Exception ignored) { }
    }
}