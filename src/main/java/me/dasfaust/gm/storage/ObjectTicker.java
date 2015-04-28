package me.dasfaust.gm.storage;

import java.util.Iterator;
import java.util.List;

import me.dasfaust.gm.Core;
import me.dasfaust.gm.storage.abs.MarketObject;

import org.bukkit.scheduler.BukkitRunnable;

public class ObjectTicker extends BukkitRunnable
{
	private int objectsToTick = 10;
	
	@Override
	public void run()
	{
		final List<MarketObject> obs = Core.instance.storage().getAll();
		final Iterator<MarketObject> iter = obs.iterator();
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				int ticked = 0;
				while(iter.hasNext() && ticked < objectsToTick)
				{
					iter.next().onTick(Core.instance.storage());
					ticked++;
				}
			}
			
		}.runTaskTimer(Core.instance, 0, 20);
	}
}
