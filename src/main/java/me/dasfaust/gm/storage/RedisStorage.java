package me.dasfaust.gm.storage;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Protocol;
import redis.clients.johm.JOhm;
import me.dasfaust.gm.Core;
import me.dasfaust.gm.MenuHandler;
import me.dasfaust.gm.menus.MarketViewer;
import me.dasfaust.gm.storage.abs.MarketObject;
import me.dasfaust.gm.storage.abs.StorageHandler;
import me.dasfaust.gm.tools.GMLogger;
import me.dasfaust.gm.trade.WrappedStack;

public class RedisStorage extends StorageHandler
{
	private String address;
	private int port;
	private int poolSize;
	private String password;
	private JedisPool pool;
	private Thread listener;
	
	private enum StorageAction
	{
		STORE("store"), REMOVE("remove");
		
		private final String value;
		
		private StorageAction(final String value)
		{
			this.value = value;
		}
		
		@Override
		public String toString()
		{
			return value;
		}
	}
	
	public RedisStorage(String address, String password, int port, int poolSize)
	{
		super();
		this.address = address;
		this.port = port;
		this.poolSize = poolSize;
		this.password = password;
	}
	
	@Override
	public boolean init()
	{
		JedisPoolConfig conf = new JedisPoolConfig();
		conf.setBlockWhenExhausted(false);
		conf.setMaxTotal(poolSize);
		pool = new JedisPool(conf, address, port, Protocol.DEFAULT_TIMEOUT, password.length() == 0 ? null : password);
		Jedis jedis = pool.getResource();
		try
		{
			if (!jedis.isConnected())
	        {
	            return false;
	        }
		}
		catch(Exception e)
		{
			GMLogger.severe(e, "Can't connect to Redis:");
			return false;
		}
		pool.returnResource(jedis);
        
		GMLogger.debug(String.format("Redis connection successful: %s:%s", address, port));
		
		listener = listener();
		listener.start();
		
        JOhm.setPool(pool);
        
		return true;
	}
	
	@Override
	public void load()
	{
		for (Class<?> type : classes)
		{
			for (Object ob : JOhm.getAll(type))
			{
				if (ob instanceof SerializedStack)
				{
					SerializedStack stack = (SerializedStack) ob;
					try
					{
						GMLogger.debug("Loading ItemStack: " + stack.id);
						items.put(stack.id, stack.buildStack());
					}
					catch (IOException e)
					{
						GMLogger.severe(e, "Couldn't load an ItemStack:");
						items.put(stack.id, new WrappedStack(
								new ItemStack(Material.STONE))
								.setDisplayName("Material Not Found")
								.setLore(Arrays.asList(new String[]{
														"This ItemStack couldn't be loaded!",
														String.format("Material %s is missing from the game.", stack.mat)
												}
										)
								)
						);
					}
				}
				else
				{
					MarketObject mob = (MarketObject) ob;
					if (!cache.containsKey(mob.getClass().getName()))
					{
						cache.put(mob.getClass().getName(), mob.createMap());
					}
					cache.get(mob.getClass().getName()).put(mob.id, mob);
				}
			}
		}
	}

	private void loadObject(Class<?> type, long id)
	{
		Object ob = JOhm.get(type, id);
		if (ob instanceof SerializedStack)
		{
			SerializedStack stack = (SerializedStack) ob;
			try
			{
				items.put(stack.id, stack.buildStack());
			}
			catch (IOException e)
			{
				GMLogger.severe(e, "Couldn't load an ItemStack:");
				items.put(id, new WrappedStack(
						new ItemStack(Material.STONE))
						.setDisplayName("Material Not Found")
						.setLore(Arrays.asList(new String[]{
												"This ItemStack couldn't be loaded!",
												String.format("Material %s is missing from the game.", stack.mat)
										}
								)
						)
				);
			}
		}
		else
		{
			MarketObject mob = (MarketObject) ob;
			if (!cache.containsKey(mob.getClass().getName()))
			{
				cache.put(mob.getClass().getName(), mob.createMap());
			}
			cache.get(mob.getClass().getName()).put(mob.id, mob);
		}
		changed();
	}
	
	private void remove(Class<?> type, long id)
	{
		if (type.equals(SerializedStack.class))
		{
			removeItem(id);
		}
		else
		{
			removeObject(type, id);
		}
	}
	
	private void publish(Class<?> type, long id, StorageAction action)
	{
		Jedis jedis = pool.getResource();
		jedis.publish("GlobalMarket", String.format("%s,%s,%s,%s", type.getName(), action.toString(), id, Core.instance.getServer().getPort()));
		pool.returnResource(jedis);
	}

	private long incrementIndex(Class<?> type)
	{
		Jedis jedis = pool.getResource();
		long index = jedis.incr(String.format("globalmarket_index_%s", type.getName()));
		pool.returnResource(jedis);
		return index;
	}
	
	@Override
	public long store(MarketObject ob)
	{
		if (!cache.containsKey(ob.getClass().getName()))
		{
			cache.put(ob.getClass().getName(), ob.createMap());
		}
		ob.id = incrementIndex(ob.getClass());
		JOhm.save(ob, true);
		GMLogger.debug("Object id: " + ob.id);
		cache.get(ob.getClass().getName()).put(ob.id, ob);
		publish(ob.getClass(), ob.id, StorageAction.STORE);
		changed();
		return ob.id;
	}

	@Override
	public long store(WrappedStack stack)
	{
		WrappedStack s = stack.clone().setAmount(1);
		for (Entry<Long, WrappedStack> ent : items.entrySet())
		{
			if (ent.getValue().equals(s))
			{
				GMLogger.debug("Item already stored: " + ent.getKey());
				return ent.getKey();
			}
		}
		changed();
		try
		{
			SerializedStack ss = new SerializedStack(s);
			ss.id = incrementIndex(ss.getClass());
			JOhm.save(ss);
			GMLogger.debug("Item id is " + ss.id);
			GMLogger.debug("Hashcode: " + ss.hashCode());
			items.put(ss.id, stack);
			publish(ss.getClass(), ss.id, StorageAction.STORE);
			return ss.id;
		}
		catch (IOException e)
		{
			GMLogger.severe(e, "Couldn't serialize ItemStack:");
		}
		return 0;
	}

	@Override
	public boolean removeObject(Class<?> type, long id)
	{
		if (cache.containsKey(type.getName()))
		{
			if (cache.get(type.getName()).containsKey(id))
			{
				cache.get(type.getName()).remove(id);
				JOhm.delete(type, id);
				publish(type, id, StorageAction.REMOVE);
				changed();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean verify(Class<?> type, long id)
	{
		return JOhm.exists(type, id);
	}
	
	@Override
	public boolean removeItem(long id)
	{
		if (items.containsKey(id))
		{
			JOhm.delete(SerializedStack.class, id);
			publish(SerializedStack.class, id, StorageAction.REMOVE);
			changed();
			return items.remove(id) != null;
		}
		return false;
	}

	@Override
	public void save()
	{
		Jedis jedis = pool.getResource();
		jedis.save();
		pool.returnResource(jedis);
	}
	
	@Override
	public void close()
	{
		save();
        pool.destroy();
	}
	
	@Override
	public void saveAsync()
	{
		save();
	}
	
	private Thread listener()
	{
		return new Thread()
        {
			@Override
            public void run()
            {
				Jedis jedis = pool.getResource();
				jedis.subscribe(new JedisPubSub() 
				{
					@Override
					public void onMessage(String channel, String message)
					{
						if (channel.equals("GlobalMarket"))
						{
							GMLogger.debug(message);
							String[] msg = message.split(",");
							final Class<?> type;
							final String action = msg[1];
							final long id = Long.parseLong(msg[2]);
							int port = Integer.parseInt(msg[3]);
							try
							{
								type = Class.forName(msg[0]);
							}
							catch(ClassNotFoundException e)
							{
								GMLogger.severe(e, String.format("Redis: can't find class %s for object %s", msg[0], id));
								return;
							}
							if (port != Bukkit.getServer().getPort())
							{
								new BukkitRunnable()
								{
									@Override
									public void run() {
										GMLogger.debug("Message was from another server");
										if (action.equals(StorageAction.STORE.value))
										{
											loadObject(type, id);
										}
										else if(action.equals(StorageAction.REMOVE.value))
										{
											remove(type, id);
										}
										for (Entry<UUID, MarketViewer> set : MenuHandler.viewers.entrySet())
										{
											if (set.getValue().menu.getObjectType().equals(type))
											{
												set.getValue().buildMenu();
											}
										}
									}
								}.runTask(Core.instance);
							}
						}
					}

					@Override
					public void onPMessage(String pattern, String channel, String message) {}

					@Override
					public void onSubscribe(String channel, int subscribedChannels) {}

					@Override
					public void onUnsubscribe(String channel, int subscribedChannels) {}

					@Override
					public void onPUnsubscribe(String pattern, int subscribedChannels) {}

					@Override
					public void onPSubscribe(String pattern, int subscribedChannels) {}
				}, "GlobalMarket");
				pool.returnResource(jedis);
            }
        };
	}

	@SuppressWarnings("deprecation")
	@Override
	public UUID findPlayer(String username)
	{
		for (Entry<UUID, String> entry : playerCache.entrySet())
		{
			if (entry.getValue().equalsIgnoreCase(username))
			{
				return entry.getKey();
			}
		}
		Jedis jedis = pool.getResource();
		String uuid = jedis.get(String.format("globalmarket_username_%s", username));
		pool.returnResource(jedis);
		if (uuid != null)
		{
			UUID uuid_ = UUID.fromString(uuid);
			playerCache.put(uuid_, username);
			return uuid_;
		}
		OfflinePlayer player = Core.instance.getServer().getOfflinePlayer(username);
		if (player != null)
		{
			playerCache.put(player.getUniqueId(), player.getName().toLowerCase());
			return player.getUniqueId();
		}
		return null;
	}

	@Override
	public String findPlayer(UUID uuid)
	{
		if (playerCache.containsKey(uuid))
		{
			return playerCache.get(uuid);
		}
		Jedis jedis = pool.getResource();
		String username = jedis.get(String.format("globalmarket_uuid_%s", uuid.toString()));
		pool.returnResource(jedis);
		if (username != null)
		{
			playerCache.put(uuid, username);
			return username;
		}
		OfflinePlayer player = Core.instance.getServer().getOfflinePlayer(uuid);
		if (player != null)
		{
			playerCache.put(player.getUniqueId(), player.getName());
			return player.getName();
		}
		return null;
	}

	@Override
	public void cachePlayer(UUID uuid, String username)
	{
		if (playerCache.containsKey(uuid))
		{
			playerCache.remove(uuid);
		}
		playerCache.put(uuid, username);
		Jedis jedis = pool.getResource();
		jedis.set(String.format("globalmarket_uuid_%s", uuid.toString()), username);
		jedis.set(String.format("globalmarket_username_%s", username), uuid.toString());
		pool.returnResource(jedis);
	}

	public void importFromJson(JsonStorage storage)
	{
		Jedis jedis = pool.getResource();
		jedis.del(String.format("globalmarket_index_%s", SerializedStack.class.getName()));
		for (Object ob : JOhm.getAll(SerializedStack.class))
		{
			JOhm.delete(SerializedStack.class, ((SerializedStack) ob).id);
		}
		for (SerializedStack stack : storage.itemStorage.values())
		{
			JOhm.save(stack);
			jedis.incr(String.format("globalmarket_index_%s", SerializedStack.class.getName()));
			GMLogger.debug("Item id is " + stack.id);
			GMLogger.debug("Hashcode: " + stack.hashCode());
		}
		for (Entry<String, Map<Long, MarketObject>> entry : storage.cache.entrySet())
		{
			try
			{
				Class<?> c = Class.forName(entry.getKey());
				for (Object ob : JOhm.getAll(c))
				{
					JOhm.delete(c, ((MarketObject) ob).id);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			jedis.del(String.format("globalmarket_index_%s", entry.getKey()));
			for (MarketObject ob : entry.getValue().values())
			{
				if (!cache.containsKey(ob.getClass().getName()))
				{
					cache.put(ob.getClass().getName(), ob.createMap());
				}
				JOhm.save(ob, true);
				jedis.incr(String.format("globalmarket_index_%s", entry.getKey()));
				GMLogger.debug("Object id: " + ob.id);
				cache.get(ob.getClass().getName()).put(ob.id, ob);
			}
		}
		pool.returnResource(jedis);
		this.items = storage.items;
	}
}
