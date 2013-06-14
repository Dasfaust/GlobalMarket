package com.survivorserver.GlobalMarket.tasks;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.logging.Logger;

import org.bukkit.scheduler.BukkitRunnable;

import com.survivorserver.GlobalMarket.ConfigHandler;

public class SaveTask extends BukkitRunnable {

	ConfigHandler config;
	Logger log;
	
	public SaveTask(Logger log, ConfigHandler config) {
		this.log = log;
		this.config = config;
	}
	
	@Override
	public void run() {
		try {
			Writer out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(config.getListingsFile()), "UTF-8"));
			out.write(config.getListingsYML().saveToString());
			out.close();		
			
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(config.getMailFile()), "UTF-8"));
			out.write(config.getMailYML().saveToString());
			out.close();
			
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(config.getHistoryFile()), "UTF-8"));
			out.write(config.getHistoryYML().saveToString());
			out.close();
			
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(config.getQueueFile()), "UTF-8"));
			out.write(config.getQueueYML().saveToString());
			out.close();
		} catch(Exception e) {
			log.severe("Could not save Market data: ");
			e.printStackTrace();
		}
	}
}
