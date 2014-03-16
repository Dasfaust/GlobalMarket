package com.survivorserver.GlobalMarket.SQL;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.bukkit.inventory.ItemStack;

import com.survivorserver.GlobalMarket.MarketStorage;

public class MarketStatement {

	private Logger log;
	private PreparedStatement statement;
	private int counter = 1;
    private String failureDebug;
	
	public MarketStatement(Logger log, PreparedStatement statement) {
		this.statement = statement;
		this.log = log;
	}
	
	public MarketStatement setString(String string) {
		try {
			statement.setString(counter, string);
			counter++;
			return this;
		} catch (SQLException e) {
			log.info("An exception occurred while preparing an SQL statement:");
			e.printStackTrace();
			return this;
		}
	}
	
	public MarketStatement setInt(int num) {
		try {
			statement.setInt(counter, num);
			counter++;
			return this;
		} catch (SQLException e) {
			log.info("An exception occurred while preparing an SQL statement:");
			e.printStackTrace();
			return this;
		}
	}
	
	public MarketStatement setDouble(double dub) {
		try {
			statement.setDouble(counter, dub);
			counter++;
			return this;
		} catch (SQLException e) {
			log.info("An exception occurred while preparing an SQL statement:");
			e.printStackTrace();
			return this;
		}
	}
	
	public MarketStatement setLong(long lon) {
		try {
			statement.setLong(counter, lon);
			counter++;
			return this;
		} catch (SQLException e) {
			log.info("An exception occurred while preparing an SQL statement:");
			e.printStackTrace();
			return this;
		}
	}
	
	public MarketStatement setItemStack(ItemStack item) {
		try {
			statement.setString(counter, MarketStorage.itemStackToString(item));
			counter++;
			return this;
		} catch (SQLException e) {
			log.info("An exception occurred while preparing an SQL statement:");
			e.printStackTrace();
			return this;
		}
	}
	
	public MarketStatement setObject(Object ob) {
		try {
			statement.setObject(counter, ob);
			counter++;
			return this;
		} catch (SQLException e) {
			log.info("An exception occurred while preparing an SQL statement:");
			e.printStackTrace();
			return this;
		}
	}

	public void execute() {
		try {
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			log.info(failureDebug == null ? "An exception occurred while executing an SQL statement:" : failureDebug);
			e.printStackTrace();
		}
	}

	public MarketResult executeAndGetKeys() {
		try {
			statement.executeUpdate();
			MarketResult result = new MarketResult(statement.getGeneratedKeys());
			//statement.close();
			return result;
		} catch (SQLException e) {
			log.info("An exception occurred while executing an SQL statement:");
			e.printStackTrace();
			return null;
		}
	}
	
	public MarketResult query() {
		try {
			MarketResult result = new MarketResult(statement.executeQuery());
			//statement.close();
			return result;
		} catch (SQLException e) {
            log.info(failureDebug == null ? "An exception occurred while executing an SQL statement:" : failureDebug);
			e.printStackTrace();
			return null;
		}
	}

    public MarketStatement setFailureNotice(String string) {
        failureDebug = string;
        return this;
    }
}
