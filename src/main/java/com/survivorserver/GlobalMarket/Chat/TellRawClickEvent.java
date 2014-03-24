package com.survivorserver.GlobalMarket.Chat;

public class TellRawClickEvent {

    public static String ACTION_RUN_COMMAND = "run_command";
    public static String ACTION_SUGGEST_COMMAND = "suggest_command";
    public static String ACTION_OPEN_URL = "open_url";

    public String action;
    public String value;

    public TellRawClickEvent setAction(String action) {
        this.action = action;
        return this;
    }

    public TellRawClickEvent setValue(String value) {
        this.value = value;
        return this;
    }
}
