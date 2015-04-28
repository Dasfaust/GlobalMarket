package me.dasfaust.gm;

import java.util.Map;
import java.util.UUID;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import me.dasfaust.gm.config.Config.Defaults;
import me.dasfaust.gm.trade.MarketListing;
import me.dasfaust.gm.trade.StockedItem;

public class StorageHelper
{
	public static void updateStockAmount(StockedItem stock, int newAmount)
	{
		Core.instance.storage().removeObject(StockedItem.class, stock.id);
		stock.amount = newAmount;
		Core.instance.storage().store(stock);
	}
	
	public static Predicate<StockedItem> allStockFor(final UUID owner)
	{
		return new Predicate<StockedItem>()
		{
			@Override
			public boolean apply(StockedItem input)
			{
				return (input.owner.equals(owner));
			}
		};
	}
	
	public static Predicate<StockedItem> allStockFor(final UUID owner, final long itemId)
	{
		return new Predicate<StockedItem>()
		{
			@Override
			public boolean apply(StockedItem input)
			{
				return (input.itemId == itemId && input.owner.equals(owner));
			}
		};
	}
	
	public static Predicate<MarketListing> allListingsFor(final UUID owner, final long itemId)
	{
		return new Predicate<MarketListing>()
		{
			@Override
			public boolean apply(MarketListing input)
			{
				return (input.itemId == itemId && input.seller.equals(owner));
			}
		};
	}
	
	public static boolean isStockFull(final UUID owner)
	{
		return Core.instance.storage().getAll(StockedItem.class, new Predicate<StockedItem>()
		{
			@Override
			public boolean apply(StockedItem input)
			{
				return input.owner.equals(owner);
			}
		}).size() >= Core.instance.config().get(Defaults.STOCK_SLOTS);
	}
	
	public static StockedItem stockFor(final UUID owner, final long itemId)
	{
		Map<Long, StockedItem> map = Core.instance.storage().getAll(StockedItem.class, allStockFor(owner, itemId));
		return Iterables.getFirst(map.values(), null);
	}
}
