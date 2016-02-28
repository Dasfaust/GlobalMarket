package me.dasfaust.gm.storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.Expose;

import me.dasfaust.gm.Core;
import me.dasfaust.gm.storage.abs.MarketObject;
import me.dasfaust.gm.storage.abs.StorageHandler;
import me.dasfaust.gm.tools.GMLogger;
import me.dasfaust.gm.trade.WrappedStack;

public class JsonStorage extends StorageHandler implements JsonDeserializer<StorageHandler>
{
	@Expose
	public Map<String, Long> indexes = new HashMap<String, Long>();
	@Expose
	public Map<Long, SerializedStack> itemStorage = new HashMap<Long, SerializedStack>();
	private Gson gson;
	private File data;
	
	public JsonStorage()
	{
		gson = new GsonBuilder().registerTypeAdapter(JsonStorage.class, this).excludeFieldsWithoutExposeAnnotation().create();
		data = new File(Core.instance.getDataFolder().getAbsolutePath() + File.separator + "data.json");
	}
	
	@Override
	public boolean init() 
	{
		File folder = Core.instance.getDataFolder();
		if (!folder.exists())
		{
			folder.mkdir();
		}
		try
		{
			return data.exists() ? true : data.createNewFile();
		}
		catch (IOException e)
		{
			GMLogger.severe(e, "Can't create data.json:");
		}
		return false;
	}

	private long incrementIndex(Class<?> type)
	{
		long ind = 1;
		if (indexes.containsKey(type.getName()))
		{
			ind = indexes.get(type.getName());
			indexes.remove(type.getName());
			ind++;
		}
		indexes.put(type.getName(), ind);
		return ind;
	}
	
	@Override
	public void load()
	{
		try
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(data), "UTF-8"));
			String all = "";
			String cur;
			while((cur = in.readLine()) != null)
			{
				all += cur;
			}
			in.close();
			gson.fromJson(all, JsonStorage.class);
		}
		catch (Exception e)
		{
			GMLogger.severe(e, "Can't read from data.json:");
		}
	}

	@Override
	public long store(MarketObject ob)
	{
		changed();
		if (!cache.containsKey(ob.getClass().getName()))
		{
			cache.put(ob.getClass().getName(), ob.createMap());
		}
		ob.id = incrementIndex(ob.getClass());
		GMLogger.debug("Object id: " + ob.id);
		cache.get(ob.getClass().getName()).put(ob.id, ob);
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
			itemStorage.put(ss.id, ss);
			GMLogger.debug("Item id is " + ss.id);
			GMLogger.debug("Hashcode: " + ss.hashCode());
			items.put(ss.id, stack);
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
				changed();
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean verify(Class<?> type, long id)
	{
		return true;
	}

	@Override
	public boolean removeItem(long id)
	{
		if (items.containsKey(id))
		{
			items.remove(id);
			changed();
			return itemStorage.remove(id) != null;
		}
		return false;
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
		OfflinePlayer player = Core.instance.getServer().getOfflinePlayer(uuid);
		if (player != null)
		{
			playerCache.put(player.getUniqueId(), player.getName());
			return player.getName();
		}
		return "<unresolved player>";
	}

	@Override
	public void cachePlayer(UUID uuid, String username)
	{
		if (playerCache.containsKey(uuid))
		{
			playerCache.remove(uuid);
		}
		playerCache.put(uuid, username);
		changed();
	}
	
	@Override
	public void save()
	{
		try
		{
			GMLogger.debug("JsonStorage: save() called...");
			GMLogger.debug(String.format("Saving to %s. File exists: %s, isFile: %s", data.getAbsolutePath(), data.exists(), data.isFile()));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(data), "UTF-8"));
			String output = gson.toJson(this);
			out.write(output);
			out.close();
			GMLogger.debug("Output:");
			GMLogger.debug(output);
			GMLogger.debug("Save finished.");
		}
		catch (Exception e)
		{
			GMLogger.severe(e, "Can't save to data.json:");
		}
	}

	@Override
	public void close()
	{
		save();
	}
	
	@Override
	public void saveAsync()
	{
		final JsonStorage dummy = new JsonStorage();
		dummy.cache.putAll(cache);
		dummy.indexes.putAll(indexes);
		dummy.itemStorage.putAll(itemStorage);
		dummy.playerCache.putAll(playerCache);
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				dummy.save();
			}
		}.runTaskAsynchronously(Core.instance);
	}
	
	@Override
	public JsonStorage deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException
	{
		try
		{
			JsonObject parent = json.getAsJsonObject();
			for (Entry<String, JsonElement> set : parent.entrySet())
			{
				if (set.getKey().equals("indexes"))
				{
					for (Entry<String, JsonElement> child : set.getValue().getAsJsonObject().entrySet())
					{
						indexes.put(child.getKey(), child.getValue().getAsLong());
					}
				}
				else if (set.getKey().equals("itemStorage"))
				{
					for (Entry<String, JsonElement> child : set.getValue().getAsJsonObject().entrySet())
					{
						SerializedStack stack = new SerializedStack();
						for (Entry<String, JsonElement> e : child.getValue().getAsJsonObject().entrySet())
						{
							Field f = stack.getClass().getField(e.getKey());
							if (f != null)
							{
								f.setAccessible(true);
								f.set(stack, parseValue(f, e.getValue()));
							}
						}
						GMLogger.debug(String.format("Item loaded. Id: %s, type: %s", stack.id, stack.mat));
						long id = Long.parseLong(child.getKey());
						try
						{
							items.put(id, stack.buildStack());
							itemStorage.put(id, stack);
						}
						catch(Exception e)
						{
							GMLogger.severe(e, "ItemStack can't be loaded:");
							items.put(id, new WrappedStack(
									new ItemStack(Material.STONE))
									.setDisplayName("Material Not Found")
									.setLore(Arrays.asList(new String[]{
															"This ItemStack couldn't be loaded!",
															String.format("Material %s is missing from the game.", stack.mat)
													}
											)
							));
							itemStorage.put(id, stack);
						}
					}
				}
				else if (set.getKey().equals("cache"))
				{
					for (Entry<String, JsonElement> child : set.getValue().getAsJsonObject().entrySet())
					{
						Class<?> ob = null;
						try
						{
							ob = Class.forName(child.getKey());
						}
						catch(Exception e)
						{
							GMLogger.warning(String.format("Class is stored but not found: %s", child.getKey()));
							continue;
						}
						for (Entry<String, JsonElement> el : child.getValue().getAsJsonObject().entrySet())
						{
							long id = Long.parseLong(el.getKey());
							Object instance = ob.newInstance();
							for (Entry<String, JsonElement> e : el.getValue().getAsJsonObject().entrySet())
							{
								Field f = ob.getField(e.getKey());
								if (f != null)
								{
									f.setAccessible(true);
									f.set(instance, parseValue(f, e.getValue()));
								}
							}
							if (!cache.containsKey(ob.getName()))
							{
								cache.put(ob.getName(), ((MarketObject) instance).createMap());
							}
							cache.get(ob.getName()).put(id, (MarketObject) instance);
						}
					}
				}
				else if (set.getKey().equals("playerCache"))
				{
					for (Entry<String, JsonElement> child : set.getValue().getAsJsonObject().entrySet())
					{
						playerCache.put(UUID.fromString(child.getKey()), child.getValue().getAsString());
					}
				}
			}
		}
		catch(NoSuchFieldException | IllegalAccessException | InstantiationException e)
		{
			GMLogger.severe(e, "Reflection error while parsing data.json:");
		}
		return null;
	}
	
	private Object parseValue(Field f, JsonElement e)
	{
		GMLogger.debug("Parsing: " + f.getName() + ", type: " + f.getType());
		if (f.getType().equals(Integer.class) || f.getType().equals(int.class))
		{
			return e.getAsInt();
		}
		if (f.getType().equals(Long.class) || f.getType().equals(long.class))
		{
			return e.getAsLong();
		}
		if (f.getType().equals(String.class))
		{
			return e.getAsString();
		}
		if (f.getType().equals(Double.class) || f.getType().equals(double.class))
		{
			return e.getAsDouble();
		}
		if (f.getType().equals(UUID.class))
		{
			return UUID.fromString(e.getAsString());
		}
		throw new IllegalArgumentException("Couldn't find value for JsonElement!");
	}
}
