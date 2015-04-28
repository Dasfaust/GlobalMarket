package me.dasfaust.gm;

import java.util.Iterator;

import me.dasfaust.gm.menus.MarketViewer;
import me.dasfaust.gm.tools.GMLogger;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

public class MiscListener implements Listener
{
	@EventHandler
	public void onPrePlayerLogin(AsyncPlayerPreLoginEvent event)
	{
		Core.instance.storage().cachePlayer(event.getUniqueId(), event.getName());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onServiceRegister(ServiceRegisterEvent event)
	{
		RegisteredServiceProvider<Economy> econ
			= Core.instance.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (econ != null && Core.instance.economy == null)
		{
			Core.instance.economy = econ.getProvider();
			GMLogger.info(String.format("Economy service registered (%s). Resuming function", Core.instance.economy.getName()));
			if (!Core.instance.postEnable)
			{
				Core.instance.postEnable();
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onServiceUnregister(ServiceUnregisterEvent event)
	{
		RegisteredServiceProvider<Economy> econ
			= Core.instance.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (econ == null)
		{
			Core.instance.economy = null;
			GMLogger.warning(String.format("Lost economy service"));
			Core.instance.handler();
			Iterator<MarketViewer> it = MenuHandler.viewers.values().iterator();
			while(it.hasNext())
			{
				it.next().close();
			}
		}
	}
}
