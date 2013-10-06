package com.survivorserver.GlobalMarket.Legacy;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.survivorserver.GlobalMarket.Market;

public class LegacyConfigHandler {
	
	Market market;
	private FileConfiguration listingsConfig;
	private File listingsFile;
	private FileConfiguration mailConfig;
	private File mailFile;
	private FileConfiguration historyConfig;
	private File historyFile;
	private FileConfiguration queueConfig;
	private File queueFile;
	public boolean save = false;
	public Map<String, Map<File, FileConfiguration>> customConfigs;
	
	public LegacyConfigHandler(Market market) {
		this.market = market;
		customConfigs = new HashMap<String, Map<File, FileConfiguration>>();
		load();
	}
	
	private void load() {
		File currentFile = null;
		try {
			listingsFile = new File(market.getDataFolder(), "listings.yml");
			currentFile = listingsFile;
			if (!listingsFile.exists()) {
				listingsFile.createNewFile();
			}
			listingsConfig = new YamlConfiguration();
			listingsConfig.load(listingsFile);

			mailFile = new File(market.getDataFolder(), "mail.yml");
			currentFile = mailFile;
			if (!mailFile.exists()) {
				mailFile.createNewFile();
			}
			mailConfig = new YamlConfiguration();
			mailConfig.load(mailFile);

			historyFile = new File(market.getDataFolder(), "history.yml");
			currentFile = historyFile;
			if (!historyFile.exists()) {
				historyFile.createNewFile();
			}
			historyConfig = new YamlConfiguration();
			historyConfig.load(historyFile);
			
			queueFile = new File(market.getDataFolder(), "queue.yml");
			currentFile = queueFile;
			if (!queueFile.exists()) {
				queueFile.createNewFile();
			}
			queueConfig = new YamlConfiguration();
			queueConfig.load(queueFile);

			save = true;
		} catch(Exception e) {
			market.log.severe("An error occurred while loading "
					+ currentFile.getName() + ":");
			e.printStackTrace();
			market.log.severe("Can't save files until this issue is resolved");
		}
	}
	
	public boolean canSave() {
		return save;
	}
	
	public FileConfiguration getListingsYML() {
		return listingsConfig;
	}
	
	public FileConfiguration getMailYML() {
		return mailConfig;
	}
	
	public FileConfiguration getHistoryYML() {
		return historyConfig;
	}
	
	public FileConfiguration getQueueYML() {
		return queueConfig;
	}
	
	public File getListingsFile() {
		return listingsFile;
	}
	
	public synchronized String saveListingsToString() {
		return listingsConfig.saveToString();
	}
	
	public File getMailFile() {
		return mailFile;
	}
	
	public synchronized String saveMailToString() {
		return mailConfig.saveToString();
	}
	
	public File getHistoryFile() {
		return historyFile;
	}
	
	public synchronized String saveHistoryToString() {
		return historyConfig.saveToString();
	}
	
	public File getQueueFile() {
		return queueFile;
	}
	
	public synchronized String saveQueueToString() {
		return queueConfig.saveToString();
	}
	
	public void loadCustomConfig(String name) throws Exception {
		Map<File, FileConfiguration> config = new HashMap<File, FileConfiguration>();
		File file = new File(market.getDataFolder(), name + ".yml");
		if (!file.exists()) {
			file.createNewFile();
		}
		YamlConfiguration conf = new YamlConfiguration();
		conf.load(file);
		config.put(file, conf);
		customConfigs.put(name, config);
	}
	
	public FileConfiguration getCustomConfig(String name) {
		if (customConfigs.containsKey(name)) {
			return customConfigs.get(name).values().iterator().next();
		}
		return null;
	}
	
	public void unregisterCustomConfig(String name, boolean save) {
		customConfigs.remove(name);
	}
}
