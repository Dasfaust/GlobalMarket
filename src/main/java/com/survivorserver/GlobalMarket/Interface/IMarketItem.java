package com.survivorserver.GlobalMarket.Interface;

import org.bukkit.inventory.ItemStack;

public interface IMarketItem {
	
    int getId();

    int getItemId();

    int getAmount();

    /**
     * Should only be used by the legacy importer
     * @deprecated
     * @return ItemStack associated with this item
     */
    ItemStack getItem();
}
