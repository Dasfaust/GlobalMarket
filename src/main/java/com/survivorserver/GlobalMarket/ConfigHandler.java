package com.survivorserver.GlobalMarket;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.survivorserver.GlobalMarket.Tasks.SaveTask;

public class ConfigHandler {
	
	Market market;
	private FileConfiguration listingsConfig;
	private File listingsFile;
	private FileConfiguration mailConfig;
	private File mailFile;
	private FileConfiguration historyConfig;
	private File historyFile;
	private FileConfiguration localeConfig;
	private File localeFile;
	private FileConfiguration queueConfig;
	private File queueFile;
	public boolean save = false;
	public Map<String, Map<File, FileConfiguration>> customConfigs;
	
	public ConfigHandler(Market market) {
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
	
	public void save() {
		new SaveTask(market.getLogger(), this).run();
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
	
	public void reloadLocaleYML() {
		localeFile = new File(market.getDataFolder(), "locale.yml");
		localeConfig = YamlConfiguration.loadConfiguration(localeFile);
		InputStream defaults = market.getResource("locale.yml");
		YamlConfiguration def = YamlConfiguration.loadConfiguration(defaults);
		localeConfig.addDefaults(def);
		localeConfig.options().copyDefaults(true);
		localeConfig.set("version", def.get("version"));
		saveLocaleYML();
	}
	
	public FileConfiguration getLocaleYML() {
		if (localeConfig == null) {
			reloadLocaleYML();
		}
		return localeConfig;
	}
	
	public void saveLocaleYML() {
		if (localeConfig == null) {
			return;
		}
		try {
			getLocaleYML().save(localeFile);
		} catch(Exception e) {
			market.getLogger().log(Level.SEVERE, "Coult not save locale: ", e);
		}
	}
	
	public FileConfiguration getQueueYML() {
		return queueConfig;
	}
	
	public File getListingsFile() {
		return listingsFile;
	}
	
	public File getMailFile() {
		return mailFile;
	}
	
	public File getHistoryFile() {
		return historyFile;
	}
	
	public File getQueueFile() {
		return queueFile;
	}
	
	public void loadCustomConfig(String name) throws Exception {
		Map<File, FileConfiguration> config = new HashMap<File, FileConfiguration>();
		File file = new File(market.getDataFolder(), name + ".yml");
		if (!file.exists()) {
			file.createNewFile();
		}
		FileConfiguration conf = new YamlConfiguration();
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
		if (save) {
			save();
		}
		customConfigs.remove(name);
	}
}
