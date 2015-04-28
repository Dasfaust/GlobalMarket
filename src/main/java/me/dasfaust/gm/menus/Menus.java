package me.dasfaust.gm.menus;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Iterables;

import me.dasfaust.gm.BlacklistHandler;
import me.dasfaust.gm.Core;
import me.dasfaust.gm.StorageHelper;
import me.dasfaust.gm.config.Config.Defaults;
import me.dasfaust.gm.menus.CreationMenu.CreationSession;
import me.dasfaust.gm.menus.MenuBase.FunctionButton;
import me.dasfaust.gm.storage.abs.StorageHandler;
import me.dasfaust.gm.tools.GMLogger;
import me.dasfaust.gm.tools.LocaleHandler;
import me.dasfaust.gm.trade.MarketListing;
import me.dasfaust.gm.trade.StockedItem;
import me.dasfaust.gm.trade.WrappedStack;

public class Menus
{	
	public static MenuBase<MarketListing> MENU_LISTINGS = new MenuBase<MarketListing>()
	{
		@Override
		public String getTitle()
		{
			return LocaleHandler.get().get("menu_listings_title");
		}

		@Override
		public boolean isStatic()
		{
			return false;
		}

		@Override
		public Map<Long, MarketListing> getObjects(MarketViewer viewer)
		{
			return Core.instance.storage().getAll(MarketListing.class);
		}

		@Override
		public MarketListing getObject(long id)
		{
			return Core.instance.storage().get(MarketListing.class, id);
		}

		@Override
		public Class<?> getObjectType()
		{
			return MarketListing.class;
		}
	};
	
	public static MenuBase<StockedItem> MENU_STOCK = new MenuBase<StockedItem>()
	{	
		class StockSlot extends StockedItem
		{
			public StockSlot(long id)
			{
				this.id = id;
			}
			
			@Override
			public WrappedStack getItemStack(MarketViewer viewer, StorageHandler storage)
			{
				return new WrappedStack(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15));
			}
			
			@Override
			public WrappedStack onItemCreated(MarketViewer viewer, WrappedStack stack)
			{
				stack.setDisplayName(LocaleHandler.get().get("menu_stock_slot"));
				stack.addLoreLast(Arrays.asList(new String[] {
						LocaleHandler.get().get("menu_stock_amount", Core.instance.config().get(Defaults.STOCK_SLOTS_SIZE)),
						LocaleHandler.get().get("menu_stock_action_swap")
				}));
				return stack.clone();
			}
			
			@Override
			public WrappedStack onClick(MarketViewer viewer, WrappedStack stack)
			{
				GMLogger.info("StockSlot onClick");
				GMLogger.info("Clicks: " + viewer.timesClicked);
				if (viewer.lastStackOnCursor != null)
				{
					if (BlacklistHandler.check(viewer.lastStackOnCursor))
					{
						List<String> lore = stack.getLore();
						lore.set(lore.size() - 1, LocaleHandler.get().get("general_item_blacklisted"));
						stack.setLore(lore);
						viewer.reset();
						return stack;
					}
					Player player = viewer.player();
					long itemId = Core.instance.storage().store(viewer.lastStackOnCursor);
					Map<Long, StockedItem> existing = Core.instance.storage().getAll(StockedItem.class, StorageHelper.allStockFor(viewer.uuid, itemId));
					if (!existing.isEmpty())
					{
						StockedItem stock = Iterables.get(existing.values(), 0);
						if (stock.amount >= Core.instance.config().get(Defaults.STOCK_SLOTS_SIZE))
						{
							List<String> lore = stack.getLore();
							lore.set(lore.size() - 1, LocaleHandler.get().get("general_no_space"));
							viewer.reset();
							return stack.setLore(lore);
						}
						int remaining = Core.instance.config().get(Defaults.STOCK_SLOTS_SIZE) - stock.amount;
						if (remaining < viewer.lastStackOnCursor.getAmount())
						{
							viewer.lastStackOnCursor.setAmount(viewer.lastStackOnCursor.getAmount() - remaining);
							StorageHelper.updateStockAmount(stock, stock.amount + remaining);
						}
						else
						{
							StorageHelper.updateStockAmount(stock, stock.amount + viewer.lastStackOnCursor.getAmount());
							player.setItemOnCursor(new ItemStack(Material.AIR));
						}
						viewer.reset();
						viewer.buildMenu();
						return null;
					}
					else
					{
						GMLogger.debug("Inserting new stock item");
						StockedItem stock = new StockedItem();
						stock.itemId = itemId;
						stock.amount = viewer.lastStackOnCursor.getAmount();
						stock.creationTime = System.currentTimeMillis();
						stock.world = player.getWorld().getUID();
						stock.owner = player.getUniqueId();
						player.setItemOnCursor(new ItemStack(Material.AIR));
						Core.instance.storage().store(stock);
						viewer.reset();
						viewer.buildMenu();
						return null;
					}
				}
				return stack;
			}
		}
		
		@Override
		public String getTitle()
		{
			return LocaleHandler.get().get("menu_stock_title");
		}

		@Override
		public boolean isStatic()
		{
			return false;
		}

		@Override
		public ClickType getResetClick()
		{
			return ClickType.UNKNOWN;
		}
		
		@Override
		public Map<Long, StockedItem> getObjects(MarketViewer viewer)
		{
			int maxStock = Core.instance.config().get(Defaults.STOCK_SLOTS);
			Map<Long, StockedItem> map = new LinkedHashMap<Long, StockedItem>();
			map.putAll(Core.instance.storage().getAll(StockedItem.class, StorageHelper.allStockFor(viewer.uuid)));
			long slot = -1;
			while(map.size() < maxStock)
			{
				map.put(slot, new StockSlot(slot));
				slot--;
			}
			return map;
		}

		@Override
		public StockedItem getObject(long id)
		{
			if (id < 0)
			{
				return new StockSlot(id);
			}
			return Core.instance.storage().get(StockedItem.class, id);
		}

		@Override
		public Class<?> getObjectType()
		{
			return StockedItem.class;
		}
	};
	
	public static CreationMenu MENU_CREATION_LISTING = new CreationMenu();
	
	public static FunctionButton FUNC_PREVPAGE = new FunctionButton()
	{
		@Override
		public WrappedStack build(MarketViewer viewer)
		{
			WrappedStack stack = new WrappedStack(new ItemStack(Material.EMPTY_MAP));
			stack.setDisplayName(LocaleHandler.get().get("menu_nav_prev_page"));
			stack.setLore(Arrays.asList(new String[] {
					LocaleHandler.get().get("menu_nav_prev_page_info", viewer.currentPage - 1)
			}));
			return stack;
		}

		@Override
		public boolean showButton(MarketViewer viewer)
		{
			return viewer.prevPage;
		}

		@Override
		public WrappedStack onClick(Player player, MarketViewer viewer)
		{
			viewer.currentPage--;
			viewer.buildMenu();
			return null;
		}
	};
	
	public static FunctionButton FUNC_NEXTPAGE = new FunctionButton()
	{
		@Override
		public WrappedStack build(MarketViewer viewer)
		{
			WrappedStack stack = new WrappedStack(new ItemStack(Material.EMPTY_MAP));
			stack.setDisplayName(LocaleHandler.get().get("menu_nav_next_page"));
			stack.setLore(Arrays.asList(new String[] {
					LocaleHandler.get().get("menu_nav_next_page_info", viewer.currentPage + 1)
			}));
			return stack;
		}

		@Override
		public boolean showButton(MarketViewer viewer)
		{
			return viewer.nextPage;
		}

		@Override
		public WrappedStack onClick(Player player, MarketViewer viewer)
		{
			viewer.currentPage++;
			viewer.buildMenu();
			return null;
		}
	};
	
	public static FunctionButton FUNC_NAVIGATION = new FunctionButton()
	{

		@Override
		public WrappedStack build(MarketViewer viewer)
		{
			WrappedStack stack;
			if (viewer.menu == MENU_LISTINGS)
			{
				stack = new WrappedStack(new ItemStack(Material.TRAPPED_CHEST));
				stack.setDisplayName(LocaleHandler.get().get("menu_nav_stock"));
				stack.setLore(Arrays.asList(new String[] {
						LocaleHandler.get().get("menu_nav_stock_info")
				}));
			}
			else
			{
				stack = new WrappedStack(new ItemStack(Material.CHEST));
				stack.setDisplayName(LocaleHandler.get().get("menu_nav_listings"));
				stack.setLore(Arrays.asList(new String[] {
						LocaleHandler.get().get("menu_nav_listings_info")
				}));
			}
			return stack;
		}

		@Override
		public boolean showButton(MarketViewer viewer)
		{
			return true;
		}

		@Override
		public WrappedStack onClick(final Player player, MarketViewer viewer) 
		{
			if (viewer.menu == MENU_LISTINGS)
			{
				Core.instance.handler().removeViewer(viewer);
				new BukkitRunnable()
				{
					@Override
					public void run()
					{
						Core.instance.handler().initViewer(player, MENU_STOCK);
					}
				}.runTaskLater(Core.instance, 1);
			}
			else
			{
				Core.instance.handler().removeViewer(viewer);
				new BukkitRunnable()
				{
					@Override
					public void run()
					{
						Core.instance.handler().initViewer(player, MENU_LISTINGS);
					}
				}.runTaskLater(Core.instance, 1);
			}
			return null;
		}
		
	};
	
	public static FunctionButton FUNC_NOSTOCK_CREATE_LISTING = new FunctionButton()
	{

		@Override
		public WrappedStack build(MarketViewer viewer)
		{
			WrappedStack stack = new WrappedStack(new ItemStack(Material.HOPPER));
			stack.setDisplayName(LocaleHandler.get().get("menu_listings_create_title"));
			stack.addLoreLast(Arrays.asList(LocaleHandler.get().get("menu_listings_create_info").split("\n")));
			return stack;
		}

		@Override
		public boolean showButton(MarketViewer viewer)
		{
			return true;
		}

		@Override
		public WrappedStack onClick(final Player player, MarketViewer viewer)
		{
			if (viewer.lastStackOnCursor != null)
			{
				if (BlacklistHandler.check(viewer.lastStackOnCursor))
				{
					WrappedStack stack = viewer.lastStackClicked;
					List<String> lore = stack.getLore();
					lore.set(0, LocaleHandler.get().get("general_item_blacklisted"));
					stack.setLore(lore);
					viewer.reset();
					return stack;
				}
				else
				{
					Core.instance.handler().removeViewer(viewer);
					Menus.MENU_CREATION_LISTING.sessions.put(viewer.uuid, new CreationSession(viewer.lastStackOnCursor));
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
			return null;
		}
	};
	
	static
	{
		MENU_LISTINGS.addFunction(45, FUNC_PREVPAGE);
		MENU_LISTINGS.addFunction(53, Menus.FUNC_NEXTPAGE);
		
		MENU_STOCK.addFunction(45, FUNC_PREVPAGE);
		MENU_STOCK.addFunction(53, Menus.FUNC_NEXTPAGE);
		
		if (Core.instance.config().get(Defaults.DISABLE_STOCK))
		{
			MENU_LISTINGS.addFunction(46, FUNC_NOSTOCK_CREATE_LISTING);
		}
		else
		{
			MENU_STOCK.addFunction(46, FUNC_NAVIGATION);
			MENU_LISTINGS.addFunction(46, FUNC_NAVIGATION);
		}
	}
}
