package com.survivorserver.GlobalMarket.Tasks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
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
		File currentFile = null;
		if (config.canSave()) {
			try {
				currentFile = config.getListingsFile();
				Writer out = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(currentFile)));
				out.write(config.saveListingsToString());
				out.close();
				
				currentFile = config.getMailFile();
				out = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(currentFile)));
				out.write(config.saveMailToString());
				out.close();
				
				currentFile = config.getHistoryFile();
				out = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(currentFile)));
				out.write(config.saveHistoryToString());
				out.close();
				
				currentFile = config.getQueueFile();
				out = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(currentFile)));
				out.write(config.saveQueueToString());
				out.close();
				
				for (Entry<String, Map<File, FileConfiguration>> entry : config.customConfigs.entrySet()) {
					for (Entry<File, FileConfiguration> ent : entry.getValue().entrySet()) {
						currentFile = ent.getKey();
						out = new BufferedWriter(new OutputStreamWriter(
								new FileOutputStream(currentFile)));
						out.write(ent.getValue().saveToString());
						out.close();
						break;
					}
				}
			} catch(Exception e) {
				log.severe("Could not save "
						+ currentFile.getName() + ":");
				e.printStackTrace();
			}
		} else {
			log.severe("Can't save Market data! Was it loaded correctly?");
		}
	}
}
