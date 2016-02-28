package me.dasfaust.gm.menus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import me.dasfaust.gm.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.dasfaust.gm.Core;
import me.dasfaust.gm.StorageHelper;
import me.dasfaust.gm.config.Config.Defaults;
import me.dasfaust.gm.storage.abs.MarketObject;
import me.dasfaust.gm.tools.GMLogger;
import me.dasfaust.gm.tools.LocaleHandler;
import me.dasfaust.gm.trade.ListingsHelper;
import me.dasfaust.gm.trade.MarketListing;
import me.dasfaust.gm.trade.StockedItem;
import me.dasfaust.gm.trade.WrappedStack;

public class CreationMenu extends MenuBase<MarketObject>
{
	public static Map<UUID, CreationSession> sessions = new HashMap<UUID, CreationSession>();
	
	public static FunctionButton FUNC_CREATE_LISTING_CANCEL = new FunctionButton()
	{
		@Override
		public String getItemId()
		{
			String configured = Core.instance.config().get(new Config.ConfigDefault<String>("menu_function_items.FUNC_CREATE_LISTING_CANCEL", null, null));
			return configured != null ? configured : Core.isCauldron ? "minecraft:stained_glass_pane:14" : Material.STAINED_GLASS_PANE.toString() + ":14";
		}

		@Override
		public WrappedStack build(MarketViewer viewer)
		{
			return Config.functionItems.get("FUNC_CREATE_LISTING_CANCEL").clone()
			.setDisplayName(LocaleHandler.get().get("menu_creation_cancel"))
			.addLoreLast(Arrays.asList(Core.instance.config().get(
				Defaults.DISABLE_STOCK) ?
				new String[] {LocaleHandler.get().get("menu_creation_cancel_return_listings")} : new String[] {LocaleHandler.get().get("menu_creation_cancel_return_stock")})
			).tag();
		}

		@Override
		public boolean showButton(MarketViewer viewer)
		{
			return true;
		}

		@Override
		public WrappedStack onClick(final Player player, MarketViewer viewer)
		{
			Core.instance.handler().removeViewer(viewer);
			new BukkitRunnable()
			{
				@Override
				public void run()
				{
					if (Core.instance.config().get(Defaults.DISABLE_STOCK))
					{
						Core.instance.handler().initViewer(player, Menus.MENU_LISTINGS);
					}
					else
					{
						Core.instance.handler().initViewer(player, Menus.MENU_STOCK);
					}
				}
			}.runTaskLater(Core.instance, 1);
			return null;
		}
	};
	
	public FunctionButton PRICE = new FunctionButton()
	{
		@Override
		public String getItemId() {
			return null;
		}

		@Override
		public WrappedStack build(MarketViewer viewer)
		{
			WrappedStack stack = 
					Core.instance.config().get(Defaults.DISABLE_STOCK) ?
							sessions.get(viewer.uuid).stack.clone().setAmount(1) : Core.instance.storage().get(sessions.get(viewer.uuid).stock.itemId);
			stack.setDisplayName(LocaleHandler.get().get("menu_creation_price"));
			stack.addLoreLast(Arrays.asList(LocaleHandler.get().get("menu_creation_price_info", Core.instance.econ().format(sessions.get(viewer.uuid).price)).split("\n")));
			return stack.clone().tag();
		}

		@Override
		public boolean showButton(MarketViewer viewer)
		{
			return true;
		}

		@Override
		public WrappedStack onClick(Player player, MarketViewer viewer)
		{
			CreationSession ses = sessions.get(viewer.uuid);
			if (viewer.lastHotbarSlot >= 0)
			{
				int hotbar = viewer.lastHotbarSlot + 1;
				int incr = getValue(hotbar);
				ses.lastIncrement = incr;
				ses.price += incr;
			}
			else
			{
				if (ses.price - ses.lastIncrement >= 1)
				{
					ses.price -= ses.lastIncrement;
				}
				else
				{
					ses.price = 1;
				}
			}
			Menus.MENU_CREATION_LISTING.buildFunctions(viewer);
			return null;
		}
	};
	
	public FunctionButton AMOUNT = new FunctionButton()
	{
		@Override
		public String getItemId() {
			return null;
		}

		@Override
		public WrappedStack build(MarketViewer viewer)
		{
			WrappedStack stack = 
					Core.instance.config().get(Defaults.DISABLE_STOCK) ?
							sessions.get(viewer.uuid).stack.clone().setAmount(1) : Core.instance.storage().get(sessions.get(viewer.uuid).stock.itemId);
			stack.setDisplayName(LocaleHandler.get().get("menu_creation_amount"));
			int amount = sessions.get(viewer.uuid).amount;
			stack.addLoreLast(Arrays.asList(LocaleHandler.get().get("menu_creation_amount_info", amount).split("\n")));
			stack.setAmount(amount);
			return stack.clone().tag();
		}

		@Override
		public boolean showButton(MarketViewer viewer)
		{
			return true;
		}

		@Override
		public WrappedStack onClick(Player player, MarketViewer viewer)
		{
			CreationSession ses = sessions.get(viewer.uuid);
			if (viewer.lastHotbarSlot >= 0)
			{
				int hotbar = viewer.lastHotbarSlot + 1;
				int incr = getValue(hotbar);
				ses.lastIncrement = incr;
				if (ses.amount + incr >= viewer.lastStackClicked.bukkit().getMaxStackSize())
				{
					ses.amount = viewer.lastStackClicked.bukkit().getMaxStackSize();
				}
				else
				{
					ses.amount += hotbar;
				}
				int am = (Core.instance.config().get(Defaults.DISABLE_STOCK) ? ses.stack.getAmount() : ses.stock.amount);
				if (ses.amount > am)
				{
					ses.amount = am;
				}
			}
			else
			{
				GMLogger.debug("Decrement: " + ses.lastIncrement);
				if (ses.amount - ses.lastIncrement >= 1)
				{
					ses.amount -= ses.lastIncrement;
				}
				else
				{
					ses.amount = 1;
				}
			}
			Menus.MENU_CREATION_LISTING.buildFunctions(viewer);
			return null;
		}
	};
	
	public static FunctionButton FUNC_CREATE_LISTING_CREATE = new FunctionButton()
	{
		@Override
		public String getItemId()
		{
			String configured = Core.instance.config().get(new Config.ConfigDefault<String>("menu_function_items.FUNC_CREATE_LISTING_CREATE", null, null));
			return configured != null ? configured : Core.isCauldron ? "minecraft:stained_glass_pane:5" : Material.STAINED_GLASS_PANE.toString() + ":5";
		}

		@Override
		public WrappedStack build(MarketViewer viewer)
		{
			return Config.functionItems.get("FUNC_CREATE_LISTING_CREATE").clone()
			.setDisplayName(LocaleHandler.get().get("menu_creation_create"))
			.addLoreLast(Arrays.asList(new String[] {LocaleHandler.get().get("menu_creation_create_info")})).tag();
		}

		@Override
		public boolean showButton(MarketViewer viewer)
		{
			return true;
		}

		@Override
		public WrappedStack onClick(final Player player, MarketViewer viewer)
		{
			CreationSession ses = sessions.get(viewer.uuid);
			
			if (Core.instance.config().get(Defaults.DISABLE_STOCK))
			{
				if (!player.getInventory().containsAtLeast(ses.stack.checkNbt().bukkit(), ses.amount)
						|| !player.getInventory().removeItem(ses.stack.checkNbt().setAmount(ses.amount).bukkit()).isEmpty())
				{
					List<String> lore = viewer.lastStackClicked.getLore();
					lore.set(lore.size() - 1, LocaleHandler.get().get("general_not_in_inventory"));
					viewer.lastStackClicked.setLore(lore);
					return viewer.lastStackClicked;
				}
			}
			else
			{
				if (Core.instance.storage().getAll(StockedItem.class, StorageHelper.allStockFor(viewer.uuid, ses.stock.itemId)).isEmpty())
				{
					List<String> lore = viewer.lastStackClicked.getLore();
					lore.set(lore.size() - 1, ChatColor.RED + LocaleHandler.get().get("general_no_stock"));
					viewer.lastStackClicked.setLore(lore);
					return viewer.lastStackClicked;
				}
			}
			MarketListing listing = new MarketListing();
			listing.amount = ses.amount;
			listing.price = ListingsHelper.round(ses.price);
			listing.itemId = Core.instance.config().get(Defaults.DISABLE_STOCK) ? Core.instance.storage().store(ses.stack) : ses.stock.itemId;
			listing.seller = viewer.uuid;
			listing.world = player.getWorld().getUID();
			listing.creationTime = System.currentTimeMillis();
			Core.instance.storage().store(listing);
			Core.instance.handler().removeViewer(viewer);
			player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);
			new BukkitRunnable()
			{
				@Override
				public void run()
				{
					if (Core.instance.config().get(Defaults.DISABLE_STOCK))
					{
						Core.instance.handler().initViewer(player, Menus.MENU_LISTINGS);
					}
					else
					{
						Core.instance.handler().initViewer(player, Menus.MENU_STOCK);
					}
				}
			}.runTaskLater(Core.instance, 1);
			return null;
		}
	};
	
	public CreationMenu()
	{
		functions.put(10, FUNC_CREATE_LISTING_CANCEL);
		functions.put(12, PRICE);
		functions.put(14, AMOUNT);
		functions.put(16, FUNC_CREATE_LISTING_CREATE);
	}
	
	@Override
	public ClickType getResetClick()
	{
		return ClickType.UNKNOWN;
	}
	
	@Override
	public String getTitle()
	{
		return LocaleHandler.get().get("menu_creation_title");
	}

	@Override
	public boolean isStatic()
	{
		return true;
	}

	public int getSize()
	{
		return 27;
	}
	
	public int[] getFunctionSlots()
	{
		return new int[] {10, 12, 14, 16};
	}
	
	@Override
	public Map<Long, MarketObject> getObjects(MarketViewer viewer)
	{
		return null;
	}

	@Override
	public MarketObject getObject(long id)
	{
		return null;
	}

	@Override
	public Class<?> getObjectType()
	{
		return MarketObject.class;
	}

	@Override
	public void onOpen(MarketViewer viewer)
	{
		
	}
	
	@Override
	public void onUnboundClick(MarketViewer viewer, InventoryClickEvent event)
	{
		
	}
	
	@Override
	public void onClose(MarketViewer viewer)
	{
		sessions.remove(viewer.uuid);
	}
	
	public static class CreationSession
	{
		public StockedItem stock;
		public WrappedStack stack;
		public int amount = 1;
		public double price = 1;
		public int lastIncrement = 0;
		
		public CreationSession(StockedItem stock)
		{
			this.stock = stock;
		}
		
		public CreationSession(WrappedStack stack)
		{
			this.stack = stack;
		}
	}
	
	private int getValue(int hotbar)
	{
		switch(hotbar)
		{
			case 1:
				return Core.instance.config().get(Defaults.CREATION_MENU_INCREMENTS_1);
			case 2:
				return Core.instance.config().get(Defaults.CREATION_MENU_INCREMENTS_2);
			case 3:
				return Core.instance.config().get(Defaults.CREATION_MENU_INCREMENTS_3);
			case 4:
				return Core.instance.config().get(Defaults.CREATION_MENU_INCREMENTS_4);
			case 5:
				return Core.instance.config().get(Defaults.CREATION_MENU_INCREMENTS_5);
			case 6:
				return Core.instance.config().get(Defaults.CREATION_MENU_INCREMENTS_6);
			case 7:
				return Core.instance.config().get(Defaults.CREATION_MENU_INCREMENTS_7);
			case 8:
				return Core.instance.config().get(Defaults.CREATION_MENU_INCREMENTS_8);
			case 9:
				return Core.instance.config().get(Defaults.CREATION_MENU_INCREMENTS_9);
		}
		return 0;
	}
}
