package me.dasfaust.gm.trade;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class PriceIndex
{
	public static ConcurrentHashMap<Long, HashSet<Double>> index = new ConcurrentHashMap<Long, HashSet<Double>>();
}
