package com.survivorserver.GlobalMarket;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.survivorserver.GlobalMarket.SQL.Database;
import com.survivorserver.GlobalMarket.SQL.StorageMethod;

public class ConfigHandler {

	private Market market;
	private FileConfiguration localeConfig;
	private File localeFile;
	
	public ConfigHandler(Market market) {
		this.market = market;
	}
	
	public Database createConnection() {
		if (getStorageMethod() == StorageMethod.MYSQL) {
			return new Database(market.getLogger(),
					market.getConfig().getString("storage.mysql_user"),
					market.getConfig().getString("storage.mysql_pass"),
					market.getConfig().getString("storage.mysql_address"),
					market.getConfig().getString("storage.mysql_database"),
					market.getConfig().getInt("storage.mysql_port"));
		} else {
			return new Database(market.getLogger(), "", "", "", "data", market.getDataFolder().getAbsolutePath());
		}
	}
	
	public StorageMethod getStorageMethod() {
		return StorageMethod.valueOf(market.getConfig().getString("storage.type").toUpperCase());
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
			market.getLogger().log(Level.SEVERE, "Could not save locale: ", e);
		}
	}
}
