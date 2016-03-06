package me.dasfaust.gm.trade;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import com.google.gson.annotations.Expose;

import redis.clients.johm.Attribute;
import redis.clients.johm.Model;
import me.dasfaust.gm.Core;
import me.dasfaust.gm.config.Config.Defaults;
import me.dasfaust.gm.menus.MarketViewer;
import me.dasfaust.gm.menus.Menus;
import me.dasfaust.gm.storage.abs.MarketObject;
import me.dasfaust.gm.storage.abs.StorageHandler;
import me.dasfaust.gm.tools.GMLogger;
import me.dasfaust.gm.tools.LocaleHandler;
import me.dasfaust.gm.trade.ListingsHelper.TransactionException;

@Model
public class MarketListing extends MarketObject
{	
	@Expose
	@Attribute
	public UUID seller;
	
	@Expose
	@Attribute
	public double price;
	
	@Override
	public Map<Long, MarketObject> createMap()
	{
		return new LinkedHashMap<Long, MarketObject>();
	}
	
	@Override
	public WrappedStack onItemCreated(MarketViewer viewer, WrappedStack stack)
	{
		stack.setAmount(amount);
		if (Core.instance.config().get(Defaults.ENABLE_DEBUG))
		{
			stack.addLoreLast(Arrays.asList(new String[] {
				String.format("Object ID: %s", id),
				String.format("Item ID: %s", itemId)
			}));
		}
		stack.addLoreLast(Arrays.asList(new String[] {
				LocaleHandler.get().get("menu_listings_seller", Core.instance.storage().findPlayer(seller)),
				LocaleHandler.get().get("menu_listings_price", Core.instance.econ().format(price)),
				LocaleHandler.get().get("menu_listings_action_buy"),
				LocaleHandler.get().get("menu_action_remove")
		}));
		return stack;
	}
	
	@Override
	public WrappedStack onClick(MarketViewer viewer, WrappedStack stack)
	{
		GMLogger.debug("MarketListing onClick");
		GMLogger.debug("Clicks: " + viewer.timesClicked);
		Player player = viewer.player();
		if (viewer.lastClickType == ClickType.SHIFT_LEFT)
		{
			if (!(viewer.uuid.equals(seller) || player.hasPermission("globalmarket.listingsadmin")))
			{
				List<String> lore = stack.getLore();
				lore.set(lore.size() - 1, LocaleHandler.get().get("general_no_permission"));
				stack.setLore(lore);
				viewer.reset();
				return stack;
			}
			if (viewer.timesClicked < 1)
			{
				List<String> lore = stack.getLore();
				lore.set(lore.size() - 1, LocaleHandler.get().get("menu_action_remove_confirm"));
				stack.setLore(lore);
				return stack;
			}
			Core.instance.storage().removeObject(MarketListing.class, id);
			//player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_HIT, 1, 1);
			if (Core.instance.config().get(Defaults.DISABLE_STOCK))
			{
				player.setItemOnCursor(Core.instance.storage().get(itemId).setAmount(amount).checkNbt().bukkit());
			}
			Core.instance.handler().rebuildAllMenus(Menus.MENU_LISTINGS);
		}
		else if (viewer.lastClickType == ClickType.LEFT)
		{
			if (viewer.uuid.equals(seller))
			{
				List<String> lore = stack.getLore();
				lore.set(lore.size() - 2, LocaleHandler.get().get("general_already_owned"));
				stack.setLore(lore);
				viewer.reset();
				return stack;
			}
			if (viewer.timesClicked < 1)
			{
				List<String> lore = stack.getLore();
				lore.set(lore.size() - 2, LocaleHandler.get().get("menu_listings_action_buy_confirm"));
				stack.setLore(lore);
				return stack;
			}
			try
			{
				ListingsHelper.buy(this, viewer.uuid);
				viewer.reset();
			}		
			catch(TransactionException e)
			{
				List<String> lore = stack.getLore();
				lore.set(lore.size() - 2, ChatColor.RED + e.getLocalizedMessage());
				stack.setLore(lore);
				viewer.reset();
				//player.playSound(player.getLocation(), Sound.BLOCK_WOOD_BREAK, 1, 1);
				return stack;
			}
			//player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
			if (Core.instance.config().get(Defaults.DISABLE_STOCK))
			{
				player.setItemOnCursor(Core.instance.storage().get(itemId).setAmount(amount).checkNbt().bukkit());
			}
		}
		return null;
	}
	
	@Override
	public void onTick(StorageHandler storage)
	{
		if (Core.instance.config().get(Defaults.DISABLE_STOCK))
		{
			// TODO: there's not really much we can do here. If the player is offline, there is nowhere for the item to go
			return;
		}
		int expireTime = Core.instance.config().get(Defaults.LISTINGS_EXPIRE_TIME);
		if (expireTime > 0)
		{
			long diff = System.currentTimeMillis() - creationTime;
			if (diff / (60 * 60 * 1000) > expireTime)
			{
				GMLogger.debug(String.format("Listing expired. ID: %s, itemId: %s", id, itemId));
				Core.instance.storage().removeObject(MarketListing.class, id);
				Core.instance.handler().rebuildAllMenus(Menus.MENU_LISTINGS);
			}
		}
	}
}
