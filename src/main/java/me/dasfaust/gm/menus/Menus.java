package me.dasfaust.gm.menus;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import me.dasfaust.gm.config.Config;
import me.dasfaust.gm.trade.*;
import org.bukkit.ChatColor;
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

	public static MenuBase<ServerListing> MENU_SERVER_LISTINGS = new MenuBase<ServerListing>()
	{
		@Override
		public String getTitle()
		{
			return LocaleHandler.get().get("menu_serverlistings_title");
		}

		@Override
		public boolean isStatic()
		{
			return false;
		}

		@Override
		public Map<Long, ServerListing> getObjects(MarketViewer viewer)
		{
			return Core.instance.storage().getAll(ServerListing.class);
		}

		@Override
		public ServerListing getObject(long id)
		{
			return Core.instance.storage().get(ServerListing.class, id);
		}

		@Override
		public Class<?> getObjectType()
		{
			return ServerListing.class;
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
				return stack.clone().tag();
			}
			
			@Override
			public WrappedStack onClick(MarketViewer viewer, WrappedStack stack)
			{
				GMLogger.debug("StockSlot onClick");
				GMLogger.debug("Clicks: " + viewer.timesClicked);
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
							player.setItemOnCursor(viewer.lastStackOnCursor.bukkit());
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

	public static MenuBase<StoredItem> MENU_STORAGE = new MenuBase<StoredItem>()
	{
		class StorageSlot extends StoredItem
		{
			public StorageSlot(long id)
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
				double fee = Core.instance.config().get(Defaults.STORAGE_STORE_AMOUNT);
				stack.setDisplayName(ChatColor.AQUA + "Open Storage Slot");
				stack.addLoreLast(Arrays.asList(new String[] {
						ChatColor.YELLOW + "<swap an item to store it>",
						ChatColor.YELLOW + "Price: " + Core.instance.econ().format(fee)
				}));
				return stack.clone().tag();
			}

			@Override
			public WrappedStack onClick(MarketViewer viewer, WrappedStack stack)
			{
				GMLogger.debug("StorageSlot onClick");
				GMLogger.debug("Clicks: " + viewer.timesClicked);
				if (viewer.lastStackOnCursor != null)
				{
					if (BlacklistHandler.check(viewer.lastStackOnCursor))
					{
						List<String> lore = stack.getLore();
						lore.set(lore.size() - 2, LocaleHandler.get().get("general_item_blacklisted"));
						stack.setLore(lore);
						viewer.reset();
						return stack;
					}
					Player player = viewer.player();
					double fee = Core.instance.config().get(Defaults.STORAGE_STORE_AMOUNT);
					if (Core.instance.econ().has(viewer.player(), fee))
					{
						Core.instance.econ().withdrawPlayer(viewer.player(), fee);
						long itemId = Core.instance.storage().store(viewer.lastStackOnCursor);
						StoredItem stored = new StoredItem();
						stored.amount = viewer.lastStackOnCursor.getAmount();
						stored.itemId = itemId;
						stored.owner = viewer.uuid;
						stored.creationTime = System.currentTimeMillis();
						stored.world = viewer.player().getWorld().getUID();
						Core.instance.storage().store(stored);
						viewer.player().setItemOnCursor(new ItemStack(Material.AIR));
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
				return stack;
			}
		}

		@Override
		public String getTitle()
		{
			return "Long Term Storage";
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
		public Map<Long, StoredItem> getObjects(MarketViewer viewer)
		{
			int maxStock = Core.instance.config().get(Defaults.STOCK_SLOTS);
			Map<Long, StoredItem> map = new LinkedHashMap<Long, StoredItem>();
			map.putAll(Core.instance.storage().getAll(StoredItem.class, StorageHelper.allStorageFor(viewer.uuid)));
			map.put(-1L, new StorageSlot(-1));
			return map;
		}

		@Override
		public StoredItem getObject(long id)
		{
			if (id < 0)
			{
				return new StorageSlot(id);
			}
			return Core.instance.storage().get(StoredItem.class, id);
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
		public String getItemId()
		{
			String configured = Core.instance.config().get(new Config.ConfigDefault<String>("menu_function_items.FUNC_PREVPAGE", null, null));
			return configured != null ? configured : Core.isCauldron ? "minecraft:map:0" : Material.EMPTY_MAP.toString() + ":0";
		}

		@Override
		public WrappedStack build(MarketViewer viewer)
		{
			WrappedStack stack = Config.functionItems.get("FUNC_PREVPAGE").clone();
			stack.setDisplayName(LocaleHandler.get().get("menu_nav_prev_page"));
			stack.setLore(Arrays.asList(new String[] {
					LocaleHandler.get().get("menu_nav_prev_page_info", viewer.currentPage - 1)
			}));
			return stack.clone().tag();
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
		public String getItemId()
		{
			String configured = Core.instance.config().get(new Config.ConfigDefault<String>("menu_function_items.FUNC_NEXTPAGE", null, null));
			return configured != null ? configured : Core.isCauldron ? "minecraft:map:0" : Material.EMPTY_MAP.toString() + ":0";
		}

		@Override
		public WrappedStack build(MarketViewer viewer)
		{
			WrappedStack stack = Config.functionItems.get("FUNC_NEXTPAGE").clone();
			stack.setDisplayName(LocaleHandler.get().get("menu_nav_next_page"));
			stack.setLore(Arrays.asList(new String[] {
					LocaleHandler.get().get("menu_nav_next_page_info", viewer.currentPage + 1)
			}));
			return stack.clone().tag();
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
		public String getItemId()
		{
			String configured = Core.instance.config().get(new Config.ConfigDefault<String>("menu_function_items.FUNC_NAVIGATION", null, null));
			return configured != null ? configured : Core.isCauldron ? "minecraft:chest:0" : Material.CHEST.toString() + ":0";
		}

		@Override
		public WrappedStack build(MarketViewer viewer)
		{
			WrappedStack stack = Config.functionItems.get("FUNC_NAVIGATION").clone();
			if (viewer.menu == MENU_LISTINGS || viewer.menu == MENU_SERVER_LISTINGS)
			{
				stack.setDisplayName(LocaleHandler.get().get("menu_nav_stock"));
				stack.setLore(Arrays.asList(new String[] {
						LocaleHandler.get().get("menu_nav_stock_info")
				}));
			}
			else
			{
				stack.setDisplayName(LocaleHandler.get().get("menu_nav_listings"));
				stack.setLore(Arrays.asList(new String[] {
						LocaleHandler.get().get("menu_nav_listings_info")
				}));
			}
			return stack.clone().tag();
		}

		@Override
		public boolean showButton(MarketViewer viewer)
		{
			return true;
		}

		@Override
		public WrappedStack onClick(final Player player, final MarketViewer viewer)
		{
			if (viewer.menu == MENU_LISTINGS || viewer.menu == MENU_SERVER_LISTINGS)
			{
				Core.instance.handler().removeViewer(viewer);
				new BukkitRunnable()
				{
					@Override
					public void run()
					{
						if (viewer.player != null)
						{
							Core.instance.handler().initViewer(player, viewer.player, MENU_STOCK);
						}
						else
						{
							Core.instance.handler().initViewer(player, MENU_STOCK);
						}
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
						if (viewer.player != null)
						{
							Core.instance.handler().initViewer(player, viewer.player, MENU_LISTINGS);
						}
						else
						{
							Core.instance.handler().initViewer(player, MENU_LISTINGS);
						}
					}
				}.runTaskLater(Core.instance, 1);
			}
			return null;
		}
	};

	public static FunctionButton FUNC_SERVER_LISTINGS_NAVIGATION = new FunctionButton()
	{
		@Override
		public String getItemId()
		{
			String configured = Core.instance.config().get(new Config.ConfigDefault<String>("menu_function_items.FUNC_SERVER_LISTINGS_NAVIGATION", null, null));
			return configured != null ? configured : Core.isCauldron ? "minecraft:chest:0" : Material.CHEST.toString() + ":0";
		}

		@Override
		public WrappedStack build(MarketViewer viewer)
		{
			WrappedStack stack = Config.functionItems.get("FUNC_SERVER_LISTINGS_NAVIGATION").clone();
			if (viewer.menu == MENU_LISTINGS || viewer.menu == MENU_STOCK)
			{
				stack.makeGlow();
				stack.setDisplayName(LocaleHandler.get().get("menu_nav_serverlistings"));
				stack.setLore(Arrays.asList(new String[] {
						LocaleHandler.get().get("menu_nav_serverlistings_info")
				}));
			}
			else
			{
				stack.setDisplayName(LocaleHandler.get().get("menu_nav_listings"));
				stack.setLore(Arrays.asList(new String[] {
						LocaleHandler.get().get("menu_nav_listings_info")
				}));
			}
			return stack.clone().tag();
		}

		@Override
		public boolean showButton(MarketViewer viewer)
		{
			return true;
		}

		@Override
		public WrappedStack onClick(final Player player, final MarketViewer viewer)
		{
			if (viewer.menu == MENU_LISTINGS || viewer.menu == MENU_STOCK)
			{
				Core.instance.handler().removeViewer(viewer);
				new BukkitRunnable()
				{
					@Override
					public void run()
					{
						if (viewer.player != null)
						{
							Core.instance.handler().initViewer(player, viewer.player, MENU_SERVER_LISTINGS);
						}
						else
						{
							Core.instance.handler().initViewer(player, MENU_SERVER_LISTINGS);
						}
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
						if (viewer.player != null)
						{
							Core.instance.handler().initViewer(player, viewer.player, MENU_LISTINGS);
						}
						else
						{
							Core.instance.handler().initViewer(player, MENU_LISTINGS);
						}
					}
				}.runTaskLater(Core.instance, 1);
			}
			return null;
		}
	};

	public static FunctionButton FUNC_STORAGE_NAVIGATION = new FunctionButton()
	{
		@Override
		public String getItemId()
		{
			String configured = Core.instance.config().get(new Config.ConfigDefault<String>("menu_function_items.FUNC_STORAGE_NAVIGATION", null, null));
			return configured != null ? configured : Core.isCauldron ? "minecraft:chest:0" : Material.CHEST.toString() + ":0";
		}

		@Override
		public WrappedStack build(MarketViewer viewer)
		{
			WrappedStack stack = Config.functionItems.get("FUNC_STORAGE_NAVIGATION").clone();
			if (viewer.menu == MENU_LISTINGS || viewer.menu == MENU_SERVER_LISTINGS)
			{
				stack.setDisplayName(ChatColor.AQUA + "Long Term Storage");
				stack.setLore(Arrays.asList(new String[] {
						ChatColor.GRAY + "Store items for a fee"
				}));
			}
			else
			{
				stack.setDisplayName(LocaleHandler.get().get("menu_nav_listings"));
				stack.setLore(Arrays.asList(new String[] {
						LocaleHandler.get().get("menu_nav_listings_info")
				}));
			}
			return stack.clone().tag();
		}

		@Override
		public boolean showButton(MarketViewer viewer)
		{
			return true;
		}

		@Override
		public WrappedStack onClick(final Player player, final MarketViewer viewer)
		{
			if (viewer.menu == MENU_LISTINGS || viewer.menu == MENU_SERVER_LISTINGS)
			{
				Core.instance.handler().removeViewer(viewer);
				new BukkitRunnable()
				{
					@Override
					public void run()
					{
						if (viewer.player != null)
						{
							Core.instance.handler().initViewer(player, viewer.player, MENU_STORAGE);
						}
						else
						{
							Core.instance.handler().initViewer(player, MENU_STORAGE);
						}
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
						if (viewer.player != null)
						{
							Core.instance.handler().initViewer(player, viewer.player, MENU_LISTINGS);
						}
						else
						{
							Core.instance.handler().initViewer(player, MENU_LISTINGS);
						}
					}
				}.runTaskLater(Core.instance, 1);
			}
			return null;
		}

	};

	public static FunctionButton FUNC_NOSTOCK_CREATE_LISTING = new FunctionButton()
	{
		@Override
		public String getItemId()
		{
			String configured = Core.instance.config().get(new Config.ConfigDefault<String>("menu_function_items.FUNC_NOSTOCK_CREATE_LISTING", null, null));
			return configured != null ? configured : Core.isCauldron ? "minecraft:hopper:0" : Material.HOPPER.toString() + ":0";
		}

		@Override
		public WrappedStack build(MarketViewer viewer)
		{
			WrappedStack stack = Config.functionItems.get("FUNC_NOSTOCK_CREATE_LISTING").clone();
			stack.setDisplayName(LocaleHandler.get().get("menu_listings_create_title"));
			stack.addLoreLast(Arrays.asList(LocaleHandler.get().get("menu_listings_create_info").split("\n")));
			return stack.clone().tag();
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
		if (Core.instance.config().get(Defaults.ENABLE_STORAGE))
		{
			MENU_LISTINGS.addFunction(52, Menus.FUNC_STORAGE_NAVIGATION);
		}

		MENU_SERVER_LISTINGS.addFunction(45, FUNC_PREVPAGE);
		MENU_SERVER_LISTINGS.addFunction(53, Menus.FUNC_NEXTPAGE);

		MENU_STOCK.addFunction(45, FUNC_PREVPAGE);
		MENU_STOCK.addFunction(53, Menus.FUNC_NEXTPAGE);

		MENU_STORAGE.addFunction(45, FUNC_PREVPAGE);
		MENU_STORAGE.addFunction(53, Menus.FUNC_NEXTPAGE);
		MENU_STORAGE.addFunction(52, Menus.FUNC_STORAGE_NAVIGATION);

		if (Core.instance.config().get(Defaults.DISABLE_STOCK))
		{
			MENU_LISTINGS.addFunction(46, FUNC_NOSTOCK_CREATE_LISTING);
		}
		else
		{
			MENU_STOCK.addFunction(46, FUNC_NAVIGATION);
			MENU_LISTINGS.addFunction(46, FUNC_NAVIGATION);

			MENU_SERVER_LISTINGS.addFunction(47, FUNC_SERVER_LISTINGS_NAVIGATION);
			MENU_SERVER_LISTINGS.addFunction(46, FUNC_NAVIGATION);

			if (Core.instance.config().get(Defaults.ENABLE_INFINITE_LISTINGS))
			{
				MENU_STOCK.addFunction(47, FUNC_SERVER_LISTINGS_NAVIGATION);
				MENU_LISTINGS.addFunction(47, FUNC_SERVER_LISTINGS_NAVIGATION);
			}
		}
	}

	public static void addButton(MenuBase<?> menu, int slot, FunctionButton button)
	{
		menu.addFunction(slot, button);
	}
}
