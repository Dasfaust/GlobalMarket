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
		if (config.canSave()) {
			try {
				Writer out = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(config.getListingsFile())));
				out.write(config.getListingsYML().saveToString());
				out.close();		
				
				out = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(config.getMailFile())));
				out.write(config.getMailYML().saveToString());
				out.close();
				
				out = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(config.getHistoryFile())));
				out.write(config.getHistoryYML().saveToString());
				out.close();
				
				out = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(config.getQueueFile())));
				out.write(config.getQueueYML().saveToString());
				out.close();
			} catch(Exception e) {
				log.severe("Could not save Market data: ");
				e.printStackTrace();
			}
		} else {
			log.severe("Could not save Market data. Was it loaded correctly?");
		}
	}
}
