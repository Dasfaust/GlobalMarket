package me.dasfaust.gm.storage.abs;

import java.util.Map;
import java.util.UUID;

import me.dasfaust.gm.Core;
import me.dasfaust.gm.menus.MarketViewer;
import me.dasfaust.gm.trade.WrappedStack;

import com.google.gson.annotations.Expose;

import redis.clients.johm.Attribute;
import redis.clients.johm.Id;
import redis.clients.johm.Model;

@Model
public abstract class MarketObject
{
	@Expose
	@Id
	public long id;
	
	@Expose
	@Attribute
	public int amount = 0;
	
	@Expose
	@Attribute
	public long itemId = 0;
	
	@Expose
	@Attribute
	public long creationTime = 0;
	
	@Expose
	@Attribute
	public UUID world;
	
	public abstract Map<Long, MarketObject> createMap();
	
	@Override
	public int hashCode()
	{
		return (int) (id + amount + itemId + creationTime);
	}
	
	public void onTick(StorageHandler storage)
	{
		
	}
	
	public WrappedStack onClick(MarketViewer viewer, WrappedStack stack)
	{
		return stack;
	}
	
	public WrappedStack getItemStack(MarketViewer viewer, StorageHandler storage)
	{
		return Core.instance.storage().get(itemId);
	}
	
	public abstract WrappedStack onItemCreated(MarketViewer viewer, WrappedStack stack);
}
