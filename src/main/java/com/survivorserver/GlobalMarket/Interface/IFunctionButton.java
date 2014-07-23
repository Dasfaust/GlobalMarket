package com.survivorserver.GlobalMarket.Interface;

import com.survivorserver.GlobalMarket.InterfaceHandler;
import com.survivorserver.GlobalMarket.InterfaceViewer;
import com.survivorserver.GlobalMarket.Market;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class IFunctionButton {

    public String title;
    public String[] description;
    public Material mat;

    public IFunctionButton(String title, String[] description, Material mat) {
        this.title = title;
        this.description = description;
        this.mat = mat;
    }

    public ItemStack buildItem(InterfaceHandler handler, InterfaceViewer viewer, ItemStack[] contents, boolean hasPrevPage, boolean hasNextPage) {
        ItemStack stack = null;
        if (showButton(handler, viewer, hasPrevPage, hasNextPage)) {
            stack = new ItemStack(mat);
            ItemMeta meta = stack.getItemMeta();
            if (meta == null) {
                meta = Market.getMarket().getServer().getItemFactory().getItemMeta(mat);
            }
            meta.setDisplayName(title);
            List<String> lore = new ArrayList<String>();
            if (description != null) {
                lore.addAll(Arrays.asList(description));
            }
            preBuild(handler, viewer, stack, meta, lore);
            meta.setLore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public abstract boolean showButton(InterfaceHandler handler, InterfaceViewer viewer, boolean hasPrevPage, boolean hasNextPage);

    public abstract void preBuild(InterfaceHandler handler, InterfaceViewer viewer, ItemStack stack, ItemMeta meta, List<String> lore);

    public abstract void onClick(Player player, InterfaceHandler handler, InterfaceViewer viewer, int slot, InventoryClickEvent event);
}
