package com.survivorserver.GlobalMarket;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import com.survivorserver.GlobalMarket.tasks.SaveTask;

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
	
	public ConfigHandler(Market market) {
		this.market = market;
		load();
	}
	
	private void load() {
		File currentFile = null;
		try {
			listingsFile = new File(market.getDataFolder(), "listings.yml");
			listingsConfig = YamlConfiguration.loadConfiguration(listingsFile);
			currentFile = listingsFile;

			mailFile = new File(market.getDataFolder(), "mail.yml");
			mailConfig = YamlConfiguration.loadConfiguration(mailFile);
			currentFile = mailFile;

			historyFile = new File(market.getDataFolder(), "history.yml");
			historyConfig = YamlConfiguration.loadConfiguration(historyFile);
			currentFile = historyFile;
			
			queueFile = new File(market.getDataFolder(), "queue.yml");
			queueConfig = YamlConfiguration.loadConfiguration(queueFile);
			currentFile = queueFile;
			
			save = true;
		} catch(Exception e) {
			market.log.severe("An error occurred while loading "
					+ currentFile.getName() + ": ");
			market.log.severe("GlobalMarket will not save files until this issue is resolved");
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
		if (localeFile == null) {
			localeFile = new File(market.getDataFolder(), "locale.yml");
			if (!localeFile.exists()) {
				market.saveResource("locale.yml", false);
			}
		}
		
		localeConfig = YamlConfiguration.loadConfiguration(localeFile);
		if (!localeConfig.getString("version").equalsIgnoreCase(market.getDescription().getVersion())) {
			File oldLocale = new File(market.getDataFolder().getName() + "/locale_old.yml");
			if (oldLocale.exists()) {
				oldLocale.delete();
			}
			localeFile.renameTo(new File(market.getDataFolder(), "locale_old.yml"));
			market.saveResource("locale.yml", false);
			localeConfig = YamlConfiguration.loadConfiguration(localeFile);
			market.log.warning("Locale version didn't match, loaded new file and moved the old one to \"local_old.yml\"");
		}
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
}
