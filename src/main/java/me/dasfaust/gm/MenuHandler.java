package me.dasfaust.gm;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.dasfaust.gm.menus.MarketViewer;
import me.dasfaust.gm.menus.MenuBase;
import me.dasfaust.gm.storage.abs.MarketObject;
import me.dasfaust.gm.tools.GMLogger;
import me.dasfaust.gm.trade.WrappedStack;

public class MenuHandler implements Listener
{
	public static Map<UUID, MarketViewer> viewers;
	
	public MenuHandler()
	{
		viewers = new HashMap<UUID, MarketViewer>();
	}
	
	public MarketViewer addViewer(MarketViewer viewer)
	{
		GMLogger.debug(String.format("Viewer added: %s (%s)", Core.instance.storage().findPlayer(viewer.uuid), viewer.uuid));
		return viewers.put(viewer.player == null ? viewer.uuid : viewer.player, viewer);
	}
	
	/**
	 * Create a MarketViewer and open the provided menu
	 * @param player
	 * @param menu
	 * @return
	 */
	public MarketViewer initViewer(Player player, MenuBase<?> menu)
	{
		UUID uuid = player.getUniqueId();
		removeViewer(uuid);
		MarketViewer viewer = new MarketViewer(menu, uuid);
		addViewer(viewer);
		viewer.open();
		return viewer;
	}

	public MarketViewer initViewer(Player player, UUID uuid, MenuBase<?> menu)
	{
		removeViewer(uuid);
		MarketViewer viewer = new MarketViewer(menu, uuid, player.getUniqueId());
		addViewer(viewer);
		viewer.open();
		return viewer;
	}

	public MarketViewer getViewer(UUID uuid)
	{
		return viewers.containsKey(uuid) ? viewers.get(uuid) : null;
	}
	
	public void removeViewer(MarketViewer viewer)
	{
		if (viewers.containsKey(viewer.player == null ? viewer.uuid : viewer.player)) viewers.remove(viewer.player == null ? viewer.uuid : viewer.player);
		Player player = Core.instance.getServer().getPlayer(viewer.player == null ? viewer.uuid : viewer.player);
		if (player != null)
		{
			player.closeInventory();
		}
	}
	
	public void removeViewer(UUID uuid)
	{
		if (viewers.containsKey(uuid)) viewers.remove(uuid);
	}
	
	/**
	 * Call when removing an object from storage that affects all players
	 */
	public void rebuildAllMenus(MenuBase<?> menu)
	{
		GMLogger.debug(String.format("Rebuilding all menus for menu %s", menu.getClass()));
		for (MarketViewer viewer : viewers.values())
		{
			if (viewer.menu == menu)
			{
				viewer.buildMenu();
			}
		}
	}

	@SuppressWarnings("unchecked")
	@EventHandler(priority = EventPriority.HIGH)
	public void onClick(InventoryClickEvent event)
	{
		HumanEntity ent = event.getWhoClicked();
		if (ent instanceof Player)
		{
			Player player = (Player) ent;
			GMLogger.debug(String.format("Inventory click: %s (slot: %s)", player.getName(), event.getRawSlot()));
			UUID uuid = player.getUniqueId();
			MarketViewer viewerOb;
			if ((viewerOb = getViewer(uuid)) != null)
			{
				GMLogger.debug(String.format("%s has MarketViewer object", player.getName()));

				if (event.getAction() == InventoryAction.UNKNOWN
					|| event.getAction() == InventoryAction.NOTHING)
				{
					event.setCancelled(true);
					event.setResult(Result.DENY);
					return;
				}
				
				GMLogger.debug("Hotbar button: " + event.getHotbarButton());
				
				if (event.getRawSlot() <= viewerOb.menu.getSize() - 1
						|| event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)
				{
					event.setCancelled(true);
					event.setResult(Result.DENY);
				}
				
				if (!player.getOpenInventory().getTitle().equals(viewerOb.menu.getTitle()))
				{
					GMLogger.debug("Viewer doesn't have the correct inventory");
					removeViewer(uuid);
					return;
				}
				
				if (event.getClick().equals(viewerOb.menu.getResetClick()))
				{
					if (viewerOb.lastSlotClicked > -1)
					{
						viewerOb.buildSlot(viewerOb.lastSlotClicked);
					}
					viewerOb.reset();
					return;
				}
				
				if (!viewerOb.menu.isStatic() && viewerOb.objectMap.containsKey(event.getRawSlot()))
				{
					MarketObject ob = viewerOb.objects.get(viewerOb.objectMap.get(event.getRawSlot()));
					if (viewerOb.lastSlotClicked == event.getRawSlot()
							&& viewerOb.lastClickType == event.getClick()
							&& viewerOb.lastObjectClicked == ob.id)
					{
						viewerOb.timesClicked++;
					}
					else
					{
						viewerOb.timesClicked = 0;
						viewerOb.buildSlot(viewerOb.lastSlotClicked);
					}
					viewerOb.lastObjectClicked = ob.id;
					updateViewer(viewerOb, event);
					WrappedStack st = ob.onClick(viewerOb, viewerOb.lastStackClicked);
					if (st != null)
					{
						event.getInventory().setItem(event.getRawSlot(), st.clone().bukkit());
					}
				}
				else if(ArrayUtils.contains(viewerOb.menu.getFunctionSlots(), event.getRawSlot()))
				{
					// Hidden button
					if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR)
					{
						if (viewerOb.lastSlotClicked == event.getRawSlot()
								&& viewerOb.lastClickType == event.getClick())
						{
							viewerOb.timesClicked++;
						}
						else
						{
							viewerOb.timesClicked = 0;
						}
						
						if (event.getCursor() != null && event.getCursor().getType() != Material.AIR)
						{
							// Bug in CraftBukkit/Spigot. Messing with a swapped item on the same tick disconnects the player
							
							/*HashMap<Integer, ItemStack> map = player.getInventory().addItem(player.getItemOnCursor());
							ItemStack cursorClone = event.getCursor().clone();
							player.setItemOnCursor(new ItemStack(Material.AIR));
							// Place item from cursor into their inventory. If it doesn't fit, don't let the click happen
							if (!map.isEmpty())
							{
								for (ItemStack s : map.values())
								{
									player.getWorld().dropItem(player.getLocation(), s);
								}
								return;
							}
							
							updateViewer(viewerOb, event);
							
							map = (HashMap<Integer, ItemStack>) player.getInventory().all(cursorClone);
							if (map.isEmpty()) return;
							for (ItemStack s : map.values())
							{
								viewerOb.lastStackOnCursor = new WrappedStack(s);
								break;
							}*/

							updateViewer(viewerOb, event);

							final MarketViewer _viewer = viewerOb;
							final int _raw = event.getRawSlot();
							new BukkitRunnable()
							{
								@Override
								public void run()
								{
									Player _player = _viewer.player();
									if (_player != null)
									{
										_viewer.menu.functions.get(_raw).onClick(_player, _viewer);
									}
								}
							}.runTaskLater(Core.instance, 1);
						}
						else
						{
							updateViewer(viewerOb, event);
							WrappedStack click = viewerOb.menu.functions.get(event.getRawSlot()).onClick(player, viewerOb);
							if (click != null)
							{
								event.getInventory().setItem(event.getRawSlot(), click.clone().bukkit());
							}
						}
					}
				}
				else
				{
					viewerOb.menu.onUnboundClick(viewerOb, event);

					// Clean up after certain Forge mods who ignore Bukkit events (*cough* InvTweaks *cough*)
					ItemStack item = event.getCurrentItem();
					if (item != null && item.getType() != Material.AIR && new WrappedStack(item).hasTag())
					{
						event.setCancelled(true);
						event.setResult(Result.DENY);
						event.setCurrentItem(new ItemStack(Material.AIR));
					}
				}
			}
			else
			{
				// Clean up after certain Forge mods who ignore Bukkit events (*cough* InvTweaks *cough*)
				ItemStack item = event.getCurrentItem();
				if (item != null && item.getType() != Material.AIR && new WrappedStack(item).hasTag())
				{
					event.setCancelled(true);
					event.setResult(Result.DENY);
					event.setCurrentItem(new ItemStack(Material.AIR));
				}
			}
		}
	}

	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event)
	{
		// Clean up after certain Forge mods who ignore Bukkit events (*cough* InvTweaks *cough*)
		ItemStack item = event.getItemDrop().getItemStack();
		if (item != null && item.getType() != Material.AIR && new WrappedStack(item).hasTag())
		{
			event.setCancelled(true);
			event.getItemDrop().setItemStack(new ItemStack(Material.AIR));
		}
	}

	private void updateViewer(MarketViewer viewer, InventoryClickEvent event)
	{
		viewer.lastClickType = event.getClick();
		viewer.lastInventoryAction = event.getAction();
		viewer.lastSlotClicked = event.getRawSlot();
		viewer.lastHotbarSlot = event.getHotbarButton();
		if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR)
		{
			viewer.lastStackClicked = new WrappedStack(event.getCurrentItem());
		}
		if (event.getCursor() != null && event.getCursor().getType() != Material.AIR)
		{
			viewer.lastStackOnCursor = new WrappedStack(event.getCursor());
		}
	}
	
	@EventHandler
	private void onInventoryClose(InventoryCloseEvent event)
	{
		GMLogger.debug("InventoryClose");
		MarketViewer viewer;
		if ((viewer = getViewer(event.getPlayer().getUniqueId())) != null)
		{
			viewer.menu.onClose(viewer);
		}
		removeViewer(event.getPlayer().getUniqueId());

		// Clean up after certain Forge mods who ignore Bukkit events (*cough* InvTweaks *cough*)
		ItemStack[] contents = event.getPlayer().getInventory().getContents();
		for (int i = 0; i < contents.length; i++)
		{
			ItemStack stack = contents[i];
			if (stack != null && stack.getType() != Material.AIR && new WrappedStack(stack).hasTag())
			{
				GMLogger.debug("ItemStack is tagged");
				event.getPlayer().getInventory().setItem(i, null);
			}
		}
	}
}
