package com.survivorserver.GlobalMarket.Chat;

public class TellRawHoverEvent {

	public static String ACTION_SHOW_TEXT = "show_text";
	public static String ACTION_SHOW_ITEM = "show_item";
	public static String ACTION_SHOW_ACHIEVEMENT = "show_achievement";
	
	public String action;
	public String value;
	
	public TellRawHoverEvent setAction(String action) {
		this.action = action;
		return this;
	}
	
	public TellRawHoverEvent setValue(String value) {
		this.value = value;
		return this;
	}
}
