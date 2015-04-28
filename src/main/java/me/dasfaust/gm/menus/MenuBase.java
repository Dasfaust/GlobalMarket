package me.dasfaust.gm.menus;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import me.dasfaust.gm.storage.abs.MarketObject;
import me.dasfaust.gm.trade.WrappedStack;

public abstract class MenuBase<T extends MarketObject>
{
	public Map<Integer, FunctionButton> functions = new HashMap<Integer, FunctionButton>();
	
	public abstract String getTitle();
	
	public abstract boolean isStatic();
	
	public int getSize()
	{
		return 54;
	}
	
	public int[] getFunctionSlots()
	{
		return new int[] {45, 46, 47, 48, 49, 50, 51, 52, 53};
	}
	
	public ClickType getResetClick()
	{
		return ClickType.RIGHT;
	}
	
	public abstract Map<Long, T> getObjects(MarketViewer viewer);
	
	public abstract T getObject(long id);
	
	public abstract Class<?> getObjectType();
	
	public void onOpen(MarketViewer viewer)
	{
		
	}
	
	public void onUnboundClick(MarketViewer viewer, InventoryClickEvent event)
	{
		
	}
	
	public void onClose(MarketViewer viewer)
	{
		
	}
	
	public MenuBase<T> addFunction(int slot, FunctionButton button)
	{
		functions.put(slot, button);
		return this;
	}
	
	public void buildFunctions(MarketViewer viewer)
	{
		ItemStack[] contents = viewer.inventory.getContents();
		for (int slot : getFunctionSlots())
		{
			if (functions.containsKey(slot))
			{
				FunctionButton button = functions.get(slot);
				if (button.showButton(viewer))
				{
					contents[slot] = button.build(viewer).bukkit();
				}
			}
		}
		viewer.inventory.setContents(contents);
	}
	
	public static abstract class FunctionButton
	{
		public abstract WrappedStack build(MarketViewer viewer);
		
		public abstract boolean showButton(MarketViewer viewer);
		
		public abstract WrappedStack onClick(Player player, MarketViewer viewer);
	}
}
