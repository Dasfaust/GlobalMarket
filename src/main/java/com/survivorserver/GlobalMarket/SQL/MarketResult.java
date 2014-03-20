package com.survivorserver.GlobalMarket.SQL;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import com.survivorserver.GlobalMarket.Listing;
import com.survivorserver.GlobalMarket.Mail;
import com.survivorserver.GlobalMarket.MarketStorage;

public class MarketResult {

	private ResultSet set;
	
	public MarketResult(ResultSet set) {
		this.set = set;
	}
	
	public boolean isEmpty() {
		try {
			return set == null ? true : !set.isBeforeFirst();
		} catch (SQLException e) {
			e.printStackTrace();
			return true;
		}
	}
	
	public boolean next() {
		try {
			return set.next();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public int getRow() {
		try {
			return set.getRow();
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public int getNumColumns() {
		try {
			return set.getMetaData().getColumnCount();
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public String getString(String label) {
		try {
			if (set.isBeforeFirst()) {
				set.next();
			}
			return set.getString(label);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getString(int index) {
		try {
			if (set.isBeforeFirst()) {
				set.next();
			}
			return set.getString(index);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public int getInt(String label) {
		try {
			if (set.isBeforeFirst()) {
				set.next();
			}
			return set.getInt(label);
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public int getInt(int index) {
		try {
			if (set.isBeforeFirst()) {
				set.next();
			}
			return set.getInt(index);
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public long getLong(String label) {
		try {
			if (set.isBeforeFirst()) {
				set.next();
			}
			return set.getLong(label);
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public long getLong(int index) {
		try {
			if (set.isBeforeFirst()) {
				set.next();
			}
			return set.getLong(index);
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public Double getDouble(String label) {
		try {
			if (set.isBeforeFirst()) {
				set.next();
			}
			return set.getDouble(label);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Double getDouble(int index) {
		try {
			if (set.isBeforeFirst()) {
				set.next();
			}
			return set.getDouble(index);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public ItemStack getItemStack(String label) {
		try {
			if (set.isBeforeFirst()) {
				set.next();
			}
			YamlConfiguration conf = new YamlConfiguration();
			conf.loadFromString(set.getString(label));
			return conf.getItemStack("item");
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Listing constructListing(MarketStorage storage) {
		try {
			if (set.isBeforeFirst()) {
				set.next();
			}
			return new Listing(set.getInt("id"), set.getString("seller"), set.getInt("item"), set.getInt("amount"), set.getDouble("price"), set.getString("world"), set.getLong("time"));
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Mail constructMail(MarketStorage storage) {
		try {
			if (set.isBeforeFirst()) {
				set.next();
			}
			return new Mail(set.getString("owner"), set.getInt("id"), set.getInt("item"), set.getInt("amount"), set.getDouble("pickup"), set.getString("sender"), set.getString("world"));
		} catch(SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

    public byte[] getBytes(String label) {
        try {
            if (set.isBeforeFirst()) {
                set.next();
            }
            return set.getBytes(label);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

	public void close() {
		try {
			set.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
