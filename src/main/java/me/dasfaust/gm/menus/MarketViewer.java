package me.dasfaust.gm.menus;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.dasfaust.gm.Core;
import me.dasfaust.gm.storage.abs.MarketObject;
import me.dasfaust.gm.tools.GMLogger;
import me.dasfaust.gm.trade.WrappedStack;

public class MarketViewer
{
	public UUID uuid;
	public MenuBase<? extends MarketObject> menu;
	public Map<Integer, Long> objectMap;
	public TreeMap<Long, MarketObject> objects;
	public Map<Long, WrappedStack> items;
	public Inventory inventory;
	public int currentPage = 1;
	public String[] search;
	public int timesClicked = 0;
	public int lastSlotClicked = -1;
	public int lastHotbarSlot = -1;
	public long lastObjectClicked = -1;
	public WrappedStack lastStackClicked;
	public WrappedStack lastStackOnCursor;
	public ClickType lastClickType = ClickType.UNKNOWN;
	public InventoryAction lastInventoryAction = InventoryAction.UNKNOWN;
	public boolean nextPage = false;
	public boolean prevPage = false;
	public UUID storage = UUID.randomUUID();
	
	public MarketViewer(MenuBase<? extends MarketObject> menu, UUID uuid)
	{
		this.uuid = uuid;
		this.menu = menu;
		
		objectMap = new HashMap<Integer, Long>();
	}
	
	public Player player()
	{
		return Core.instance.getServer().getPlayer(uuid);
	}
	
	public MarketViewer prepareInventory()
	{
		inventory = Core.instance.getServer().createInventory(null, menu.getSize());
		return this;
	}
	
	public MarketViewer reset()
	{
		timesClicked = 0;
		lastSlotClicked = -1;
		lastHotbarSlot = -1;
		lastObjectClicked = -1;
		lastStackClicked = null;
		lastStackOnCursor = null;
		storage = UUID.randomUUID();
		lastClickType = ClickType.UNKNOWN;
		lastInventoryAction = InventoryAction.UNKNOWN;
		return this;
	}
	
	public MarketViewer open()
	{
		reset();
		player().openInventory(inventory = Core.instance.getServer().createInventory(null, menu.getSize(), menu.getTitle()));
		buildMenu();
		menu.onOpen(this);
		return this;
	}
	
	public MarketViewer close()
	{
		reset();
		player().closeInventory();
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public MarketViewer buildMenu()
	{
		inventory.clear();
		objectMap.clear();
		if (!menu.isStatic())
		{
			UUID storageCurrent = Core.instance.storage().getChanged();
			if (!storage.equals(storageCurrent))
			{
				GMLogger.debug("Building menu from scratch!");
				objects = new TreeMap<Long, MarketObject>((Map<Long, MarketObject>) menu.getObjects(this));
				items = new HashMap<Long, WrappedStack>();
				for (MarketObject ob : objects.values())
				{
					items.put(ob.id, ob.getItemStack(this, Core.instance.storage()));
				}
				storage = storageCurrent;
			}
			
			int pageSize = menu.getSize() - menu.getFunctionSlots().length;
			int index = 0;
			if (objects.size() > pageSize)
			{
				index = (pageSize * currentPage) - pageSize;
			}
			ItemStack[] contents = inventory.getContents();
			int slot = 0;
			GMLogger.debug(String.format("Begin build. Slot: %s, Index: %s, Page: %s, PageSize: %s", slot, index, currentPage, pageSize));
			MarketObject[] obs = objects.descendingMap().values().toArray(new MarketObject[0]);
			while(objects.size() > index && slot < pageSize)
			{
				if (!ArrayUtils.contains(menu.getFunctionSlots(), slot))
				{	
					MarketObject ob = obs[index];
					WrappedStack stack = items.get(ob.id).clone();
					ob.onItemCreated(this, stack);
					objectMap.put(slot, ob.id);
					contents[slot] = stack.bukkit();
					index++;
				}
				slot++;
			}
			GMLogger.debug(String.format("End build. Slot: %s, Index: %s", slot, index));
			nextPage = index < objects.size();
			prevPage = currentPage > 1;
			inventory.setContents(contents);
		}
		menu.buildFunctions(this);
		return this;
	}
	
	public MarketViewer buildSlot(int slot)
	{
		if (objectMap.containsKey(slot))
		{
			MarketObject ob = objects.get(objectMap.get(slot));
			WrappedStack stack = items.get(ob.id).clone();
			ob.onItemCreated(this, stack);
			inventory.setItem(slot, stack.bukkit());
		}
		return this;
	}
}
