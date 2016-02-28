package me.dasfaust.gm.storage.abs;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.gson.annotations.Expose;

import me.dasfaust.gm.storage.SerializedStack;
import me.dasfaust.gm.tools.GMLogger;
import me.dasfaust.gm.trade.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public abstract class StorageHandler
{
	public static List<Class<?>> classes = new ArrayList<Class<?>>();

	public static WrappedStack BACKUP_STACK = new WrappedStack(new ItemStack(Material.STONE))
			.setDisplayName("Internal Error")
			.setLore(Arrays.asList(new String[]{
					"Internal Market error.",
					"Please report this!",
					"Missing item ID."
			}));

	static
	{
		classes.add(SerializedStack.class);
		classes.add(MarketListing.class);
		classes.add(StockedItem.class);
		classes.add(ServerListing.class);
		classes.add(StoredItem.class);
	}
	
	public Map<Long, WrappedStack> items;
	@Expose
	public Map<String, Map<Long, MarketObject>> cache;
	@Expose
	public Map<UUID, String> playerCache;
	
	protected UUID changed = UUID.randomUUID();
	
	public StorageHandler()
	{
		items = new HashMap<Long, WrappedStack>();
		cache = new HashMap<String, Map<Long, MarketObject>>();
		playerCache = new ConcurrentHashMap<UUID, String>();
	}
	
	public static void registerClass(Class<?> type)
	{
		classes.add(type);
	}
	
	public abstract boolean init();
	
	public abstract void load();
	
	public abstract long store(MarketObject ob);
	
	public abstract long store(WrappedStack stack);
	
	public abstract boolean removeObject(Class<?> type, long id);
	
	public abstract boolean removeItem(long id);
	
	/**
	 * Call before modifying an object to verify it still exists
	 * @param type
	 * @param id
	 */
	public abstract boolean verify(Class<?> type, long id);
	
	@SuppressWarnings("unchecked")
	public <T extends MarketObject> T get(Class<T> type, long id)
	{
		if (cache.containsKey(type.getName()))
		{
			return (T) cache.get(type.getName()).get(id);
		}
		throw new IllegalArgumentException(String.format("%s is not a stored type", type.getName()));
	}
	
	@SuppressWarnings("unchecked")
	public <T extends MarketObject> Map<Long, T> getAll(Class<T> type)
	{
		if (cache.containsKey(type.getName()))
		{
			return (Map<Long, T>) cache.get(type.getName());
		}
		return new HashMap<Long, T>();
	}
	
	@SuppressWarnings("unchecked")
	public <T extends MarketObject> Map<Long, T> getAll(Class<T> type, Predicate<T> pred)
	{
		if (cache.containsKey(type.getName()))
		{
			Map<Long, T> map = new HashMap<Long, T>();
			Iterable<MarketObject> it = Iterables.filter(cache.get(type.getName()).values(), (Predicate<MarketObject>) pred);
			Iterator<MarketObject> i = it.iterator();
			while(i.hasNext())
			{
				MarketObject ob = i.next();
				map.put(ob.id, (T) ob);
			}
			return map;
		}
		return new HashMap<Long, T>();
	}
	
	public WrappedStack get(long id)
	{
		if (items.containsKey(id))
		{
			return items.get(id).clone().setAmount(1);
		}
		GMLogger.severe(new IllegalArgumentException(String.format("Item %s doesn't exist", id)), "Storage is not synced with memory:");
		return BACKUP_STACK.clone();
	}
	
	public List<WrappedStack> getStacks()
	{
		return new ArrayList<WrappedStack>(items.values());
	}
	
	public int getHash(Class<?> type)
	{
		if (cache.containsKey(type.getName()))
		{
			return cache.get(type.getName()).hashCode();
		}
		return -1;
	}
	
	/**
	 * Returns all MarketObjects
	 * @return
	 */
	public List<MarketObject> getAll()
	{
		List<MarketObject> obs = new ArrayList<MarketObject>();
		for (Entry<String, Map<Long, MarketObject>> ent : cache.entrySet())
		{
			obs.addAll(ent.getValue().values());
		}
		return obs;
	}
	
	/**
	 * Thread-safe UUID lookup from username
	 * @param username
	 * @return
	 */
	public abstract UUID findPlayer(String username);
	
	/**
	 * Thread-safe username lookup from UUID
	 * @param uuid
	 * @return
	 */
	public abstract String findPlayer(UUID uuid);
	
	/**
	 * Cache player UUID and username. Thread-safe
	 * @param uuid
	 * @param username
	 */
	public abstract void cachePlayer(UUID uuid, String username);
	
	public abstract void save();
	
	public abstract void saveAsync();
	
	public abstract void close();
	
	protected void changed()
	{
		changed = UUID.randomUUID();
	}
	
	public UUID getChanged()
	{
		return changed;
	}
}
