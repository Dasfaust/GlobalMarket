package com.survivorserver.GlobalMarket.Interface;

public abstract class IHandler {

    public abstract void updateAllViewers();
    public abstract void updateViewer(String name);
    public abstract void notifyPlayer(String name, String notification);
}
