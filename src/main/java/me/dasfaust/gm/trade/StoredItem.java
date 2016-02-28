package me.dasfaust.gm.trade;

import com.google.gson.annotations.Expose;
import me.dasfaust.gm.Core;
import me.dasfaust.gm.StorageHelper;
import me.dasfaust.gm.config.Config;
import me.dasfaust.gm.menus.CreationMenu;
import me.dasfaust.gm.menus.MarketViewer;
import me.dasfaust.gm.menus.Menus;
import me.dasfaust.gm.storage.abs.MarketObject;
import me.dasfaust.gm.tools.GMLogger;
import me.dasfaust.gm.tools.LocaleHandler;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.johm.Attribute;
import redis.clients.johm.Model;

import java.util.*;

@Model
public class StoredItem extends MarketObject
{
    @Expose
    @Attribute
    public UUID owner;

    @Override
    public Map<Long, MarketObject> createMap()
    {
        return new HashMap<Long, MarketObject>();
    }

    @Override
    public WrappedStack onItemCreated(MarketViewer viewer, WrappedStack stack)
    {
        double fee = Core.instance.config().get(Config.Defaults.STORAGE_WITHDRAW_AMOUNT);
        stack.setAmount(amount);
        if (Core.instance.config().get(Config.Defaults.ENABLE_DEBUG))
        {
            stack.addLoreLast(Arrays.asList(new String[] {
                    String.format("Object ID: %s", id),
                    String.format("Item ID: %s", itemId)
            }));
        }
        stack.addLoreLast(Arrays.asList(new String[] {
                ChatColor.GREEN + "<click to withdraw>",
                ChatColor.GREEN + "Price: " + Core.instance.econ().format(fee)
        }));
        return stack;
    }

    @Override
    public WrappedStack onClick(MarketViewer viewer, WrappedStack stack)
    {
        GMLogger.debug("StoredItem onClick");
        GMLogger.debug("Clicks: " + viewer.timesClicked);
        if (viewer.lastInventoryAction == InventoryAction.SWAP_WITH_CURSOR)
        {
            return stack;
        }
        if (Core.instance.econ().has(viewer.player(), Core.instance.config().get(Config.Defaults.STORAGE_WITHDRAW_AMOUNT)))
        {
            Core.instance.econ().withdrawPlayer(viewer.player(), Core.instance.config().get(Config.Defaults.STORAGE_WITHDRAW_AMOUNT));
            viewer.player().setItemOnCursor(Core.instance.storage().get(itemId).setAmount(amount).checkNbt().bukkit());
            Core.instance.storage().removeObject(StoredItem.class, id);
            viewer.reset();
            viewer.buildMenu();
            return null;
        }
        List<String> lore = stack.getLore();
        lore.set(lore.size() - 2, ChatColor.RED + "<don't have enough bits!>");
        stack.setLore(lore);
        viewer.reset();
        return stack;
    }
}
