package com.survivorserver.GlobalMarket.tasks;

import java.io.FileWriter;

import org.bukkit.scheduler.BukkitRunnable;

import com.survivorserver.GlobalMarket.ConfigHandler;

public class SaveTask extends BukkitRunnable {

	ConfigHandler config;
	
	public SaveTask(ConfigHandler config) {
		this.config = config;
	}
	
	@Override
	public void run() {
		try {
			FileWriter writer = new FileWriter(config.getListingsFile());
			writer.write(config.getListingsYML().saveToString());
			writer.close();
			
			writer = new FileWriter(config.getMailFile());
			writer.write(config.getMailYML().saveToString());
			writer.close();
			
			writer = new FileWriter(config.getHistoryFile());
			writer.write(config.getHistoryYML().saveToString());
			writer.close();
			
			writer = new FileWriter(config.getQueueFile());
			writer.write(config.getQueueYML().saveToString());
			writer.close();
		} catch(Exception e) {
			System.out.println("Could not save Market data: " + e.getMessage());
		}
	}
}
