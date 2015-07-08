package me.dasfaust.gm.trade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import redis.clients.johm.Attribute;
import redis.clients.johm.Model;

import com.google.gson.annotations.Expose;

import me.dasfaust.gm.Core;
import me.dasfaust.gm.StorageHelper;
import me.dasfaust.gm.config.Config.Defaults;
import me.dasfaust.gm.menus.CreationMenu.CreationSession;
import me.dasfaust.gm.menus.MarketViewer;
import me.dasfaust.gm.menus.Menus;
import me.dasfaust.gm.storage.abs.MarketObject;
import me.dasfaust.gm.tools.GMLogger;
import me.dasfaust.gm.tools.LocaleHandler;

@Model
public class StockedItem extends MarketObject
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
		stack.setAmount(1);
		Map<Long, MarketListing> listings = Core.instance.storage().getAll(MarketListing.class, StorageHelper.allListingsFor(viewer.uuid, itemId));
		String sellCreate;
		if (listings.isEmpty())
		{
			sellCreate = LocaleHandler.get().get("menu_stock_create");
		}
		else
		{
			MarketListing listing = new ArrayList<MarketListing>(listings.values()).get(0);
			sellCreate = LocaleHandler.get().get("menu_stock_selling", Core.instance.econ().format(listing.price), listing.amount);
		}
		String removeWait = LocaleHandler.get().get("menu_stock_action_withdraw");
		int stockDelay = Core.instance.config().get(Defaults.STOCK_DELAY);
		if (stockDelay > 0)
		{
			long diff = System.currentTimeMillis() - creationTime;
			long minutes = diff / (60 * 1000);
			if (minutes < stockDelay)
			{
				int time = stockDelay - (int) minutes;
				removeWait = LocaleHandler.get().get("menu_stock_arrival", time >= 1 ? time : "<1");
			}
		}
		if (Core.instance.config().get(Defaults.ENABLE_DEBUG))
		{
			stack.addLoreLast(Arrays.asList(new String[] {
				String.format("Object ID: %s", id),
				String.format("Item ID: %s", itemId)
			}));
		}
		stack.addLoreLast(Arrays.asList(new String[] {
				LocaleHandler.get().get("menu_stock_amount_stored", amount),
				sellCreate,
				removeWait
		}));
		return stack;
	}
	
	@Override
	public WrappedStack onClick(MarketViewer viewer, WrappedStack stack)
	{
		GMLogger.debug("StockedItem onClick");
		GMLogger.debug("Clicks: " + viewer.timesClicked);
		if (viewer.timesClicked < 1)
		{
			final Player player = viewer.player();
			if (viewer.lastClickType == ClickType.LEFT)
			{
				ItemStack cur = viewer.player().getItemOnCursor();
				WrappedStack st = getItemStack(viewer, Core.instance.storage());
				if (cur == null || cur.getType() == Material.AIR)
				{
					int stockDelay = Core.instance.config().get(Defaults.STOCK_DELAY);
					if (stockDelay > 0)
					{
						long diff = System.currentTimeMillis() - creationTime;
						if (diff / (60 * 1000) < stockDelay)
						{
							return stack;
						}
					}
					int stackSize = st.bukkit().getMaxStackSize();
					if (amount > stackSize)
					{
						st.setAmount(stackSize);
						StorageHelper.updateStockAmount(this, amount - st.bukkit().getMaxStackSize());
					}
					else
					{
						st.setAmount(amount);
						Map<Long, MarketListing> listings = Core.instance.storage().getAll(MarketListing.class, StorageHelper.allListingsFor(viewer.uuid, itemId));
						for (MarketListing listing : listings.values())
						{
							Core.instance.storage().removeObject(MarketListing.class, listing.id);
						}
						Core.instance.storage().removeObject(StockedItem.class, id);
					}
					player.setItemOnCursor(st.checkNbt().bukkit());
					viewer.buildMenu();
					return null;
				}
				else if (cur.isSimilar(st.bukkit()))
				{
					if (amount >= Core.instance.config().get(Defaults.STOCK_SLOTS_SIZE))
					{
						List<String> lore = stack.getLore();
						lore.set(lore.size() - 1, LocaleHandler.get().get("general_no_space"));
						return stack.setLore(lore);
					}
					int remaining = Core.instance.config().get(Defaults.STOCK_SLOTS_SIZE) - amount;
					if (remaining < viewer.lastStackOnCursor.getAmount())
					{
						viewer.lastStackOnCursor.setAmount(viewer.lastStackOnCursor.getAmount() - remaining);
						StorageHelper.updateStockAmount(this, amount + remaining);
					}
					else
					{
						StorageHelper.updateStockAmount(this, amount + viewer.lastStackOnCursor.getAmount());
						player.setItemOnCursor(new ItemStack(Material.AIR));
					}
					viewer.reset();
					viewer.buildMenu();
					return null;
				}
			}
			else if (viewer.lastClickType == ClickType.RIGHT)
			{
				if (viewer.lastStackClicked.getLore().contains(LocaleHandler.get().get("menu_stock_create")))
				{
					Core.instance.handler().removeViewer(viewer);
					Menus.MENU_CREATION_LISTING.sessions.put(viewer.uuid, new CreationSession(this));
					new BukkitRunnable()
					{
						@Override
						public void run()
						{
							Core.instance.handler().initViewer(player, Menus.MENU_CREATION_LISTING);
						}
					}.runTaskLater(Core.instance, 1);
				}
			}
		}
		return stack;
	}
}
