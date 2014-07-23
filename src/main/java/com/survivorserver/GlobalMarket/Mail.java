package com.survivorserver.GlobalMarket;

import org.bukkit.inventory.ItemStack;

import com.survivorserver.GlobalMarket.Interface.IMarketItem;

public class Mail implements IMarketItem {

    public String owner;
    public int id;
    public int itemId;
    public int amount;
    public double pickup;
    public String sender;
    public String world;
    // Legacy
    ItemStack item;

    public Mail() {
    }

    public Mail(String owner, int id, int itemId, int amount, double pickup, String sender, String world) {
        this.owner = owner;
        this.id = id;
        this.itemId = itemId;
        this.amount = amount;
        this.pickup = pickup;
        this.sender = sender;
        this.world = world;
    }

    /*
     * Legacy constructor
     */
    public Mail(String owner, int id, ItemStack item, double pickup, String sender) {
        this.owner = owner;
        this.id = id;
        this.item = item;
        this.pickup = pickup;
        this.sender = sender;
    }

    @Override
    public int getId() {
        return id;
    }

    public int getItemId() {
        return itemId;
    }

    public int getAmount() {
        return amount;
    }

    public String getOwner() {
        return owner;
    }

    public double getPickup() {
        return pickup;
    }

    public void setPickup(double amount) {
        pickup = amount;
    }

    public String getSender() {
        return sender;
    }

    public String getWorld() {
        return world;
    }

    /**
     * Should only be used by the legacy importer
     * @deprecated
     * @return ItemStack associated with this item
     */
    public ItemStack getItem() {
        return item;
    }
}
