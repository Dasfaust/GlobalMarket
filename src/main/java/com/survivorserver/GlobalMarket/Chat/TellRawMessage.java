package com.survivorserver.GlobalMarket.Chat;

public class TellRawMessage {	
	
    public String text;
    public String color;
    public boolean bold;
    public boolean italic;
    public boolean underlined;
    public boolean strikethrough;
    public boolean obfuscated;
    public TellRawClickEvent clickEvent;
    public TellRawHoverEvent hoverEvent;
    public TellRawMessage[] extra;

    public TellRawMessage setText(String text) {
        this.text = text;
        return this;
    }

    public TellRawMessage setColor(String color) {
        this.color = color;
        return this;
    }

    public TellRawMessage setClick(TellRawClickEvent event) {
        this.clickEvent = event;
        return this;
    }

    public TellRawMessage setHover(TellRawHoverEvent event) {
        this.hoverEvent = event;
        return this;
    }

    public TellRawMessage setExtra(TellRawMessage[] extra) {
        this.extra = extra;
        return this;
    }

    public TellRawMessage setBold(boolean bold) {
        this.bold = bold;
        return this;
    }
}
