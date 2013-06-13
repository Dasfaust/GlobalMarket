package com.survivorserver.GlobalMarket.tasks;

import java.io.FileWriter;

import org.bukkit.scheduler.BukkitRunnable;

import com.survivorserver.GlobalMarket.ConfigHandler;

public class SaveTask extends BukkitRunnable {

	ConfigHandler config;
	String listings;
	String mail;
	String history;
	String queue;
	
	public SaveTask(ConfigHandler config, String listings, String mail, String history, String queue) {
		this.config = config;
		this.listings = listings;
		this.mail = mail;
		this.history = history;
		this.queue = queue;
	}
	
	@Override
	public void run() {
		try {
			FileWriter writer = new FileWriter(config.getListingsFile());
			writer.write(listings);
			writer.close();
			
			writer = new FileWriter(config.getMailFile());
			writer.write(mail);
			writer.close();
			
			writer = new FileWriter(config.getHistoryFile());
			writer.write(history);
			writer.close();
			
			writer = new FileWriter(config.getQueueFile());
			writer.write(queue);
			writer.close();
		} catch(Exception e) {
			System.out.println("Could not save Market data: " + e.getMessage());
		}
	}
}
