package com.survivorserver.GlobalMarket;

public class LocaleHandler {
	
	ConfigHandler config;
	
	public LocaleHandler(ConfigHandler config) {
		this.config = config;
	}
	
	public String get(String string, Object...args) {
		if (!config.getLocaleYML().isSet(string)) {
			return string;
		}
		return String.format(config.getLocaleYML().getString(string), args);
	}
	
	public String get(String string) {
		if (!config.getLocaleYML().isSet(string)) {
			return string;
		}
		return config.getLocaleYML().getString(string);
	}
}
