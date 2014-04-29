package com.survivorserver.GlobalMarket.Lib;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MCPCPHelper {

    public static ItemStack wrapItemStack(Inventory inv, int slot) {
        me.dasfaust.GlobalMarket.MarketCompanion inst = me.dasfaust.GlobalMarket.MarketCompanion.getInstance();
        return inst.getWrappedForgeItemStack(getNMSInventory(inv), slot);
    }

    public static ItemStack wrapItemStack(ItemStack stack) {
        me.dasfaust.GlobalMarket.MarketCompanion inst = me.dasfaust.GlobalMarket.MarketCompanion.getInstance();
        return inst.wrap(getNMSStack(stack));
    }

    public static void addItemToInventory(ItemStack stack, Inventory inv, int slot) {
        if (!(stack instanceof me.dasfaust.GlobalMarket.WrappedItemStack)) {
            stack = wrapItemStack(stack);
        }
        me.dasfaust.GlobalMarket.MarketCompanion inst = me.dasfaust.GlobalMarket.MarketCompanion.getInstance();
        inst.setInventorySlot(getNMSInventory(inv), stack, slot);
    }

    public static void addItemToInventory(String playerName, ItemStack stack) {
        if (!(stack instanceof me.dasfaust.GlobalMarket.WrappedItemStack)) {
            stack = wrapItemStack(stack);
        }
        me.dasfaust.GlobalMarket.MarketCompanion inst = me.dasfaust.GlobalMarket.MarketCompanion.getInstance();
        inst.addToPlayerInventory(playerName, 0, stack);
    }

    public static void setInventoryContents(Inventory inv, ItemStack[] contents) {
        me.dasfaust.GlobalMarket.MarketCompanion inst = me.dasfaust.GlobalMarket.MarketCompanion.getInstance();
        for (int i = 0; i < contents.length; i++) {
            ItemStack stack = contents[i];
            if (stack != null) {
                if (!(stack instanceof me.dasfaust.GlobalMarket.WrappedItemStack)) {
                    contents[i] = inst.wrap(getNMSStack(stack));
                }
            }
        }
        inst.setInventoryContents(getNMSInventory(inv), contents);
    }

    public static ItemStack deserialize(String str) {
        return me.dasfaust.GlobalMarket.WrappedItemStack.unserializeJSON(str);
    }

    public static String serialize(ItemStack item) {
        me.dasfaust.GlobalMarket.WrappedItemStack stack = ((me.dasfaust.GlobalMarket.WrappedItemStack) item).clone();
        stack.setAmount(1);
        return stack.serializeJSON();
    }

    // TODO: some type of abstraction to support 1.7+
    public static Object getNMSStack(ItemStack item) {
        return org.bukkit.craftbukkit.v1_6_R3.inventory.CraftItemStack.asNMSCopy(item);
    }

    // TODO: some type of abstraction to support 1.7+
    public static Object getNMSInventory(Inventory inv) {
        return ((org.bukkit.craftbukkit.v1_6_R3.inventory.CraftInventory) inv).getInventory();
    }
}
