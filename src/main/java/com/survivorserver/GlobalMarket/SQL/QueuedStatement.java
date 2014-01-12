package com.survivorserver.GlobalMarket.SQL;

import java.util.ArrayList;

import org.bukkit.inventory.ItemStack;

import com.survivorserver.GlobalMarket.Market;

public class QueuedStatement {

	private String query;
	private ArrayList<Object> values;
	
	public QueuedStatement(String query) {
		this.query = query;
		values = new ArrayList<Object>();
	}
	
	public String getUnbuiltQuery() {
		return query;
	}
	
	public QueuedStatement setValue(Object ob) {
		values.add(ob);
		return this;
	}
	
	public ArrayList<Object> getValues() {
		return values;
	}
	
	public MarketStatement buildStatement(Database db) {
		try {
			MarketStatement statement = db.createStatement(query);
			for (Object ob : values) {
				if (ob instanceof ItemStack) {
					statement.setItemStack((ItemStack) ob);
				} else {
					statement.setObject(ob);
				}
			}
			return statement;
		} catch(Exception e) {
			Market.getMarket().log.info("Error while building queued statement:");
			e.printStackTrace();
			return null;
		}
	}
}
