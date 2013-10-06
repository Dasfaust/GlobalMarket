package com.survivorserver.GlobalMarket.SQL;

import java.util.ArrayList;
import org.bukkit.inventory.ItemStack;

public class QueuedStatement {

	private String query;
	private ArrayList<Object> values;
	
	public QueuedStatement(String query) {
		this.query = query;
		values = new ArrayList<Object>();
	}
	
	public QueuedStatement setValue(Object ob) {
		values.add(ob);
		return this;
	}
	
	public MarketStatement buildStatement(Database db) {
		MarketStatement statement = db.createStatement(query);
		for (Object ob : values) {
			if (ob instanceof ItemStack) {
				statement.setItemStack((ItemStack) ob);
			} else {
				statement.setObject(ob);
			}
		}
		return statement;
	}
}
