package com.survivorserver.GlobalMarket;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import com.survivorserver.GlobalMarket.SQL.Database;
import com.survivorserver.GlobalMarket.SQL.AsyncDatabase;
import com.survivorserver.GlobalMarket.SQL.MarketResult;
import com.survivorserver.GlobalMarket.SQL.QueuedStatement;
import com.survivorserver.GlobalMarket.SQL.StorageMethod;

public class HistoryHandler {

    private Market market;
    private LocaleHandler locale;
    private MarketStorage storage;
    private AsyncDatabase asyncDb;
    private ConfigHandler config;

    public HistoryHandler(Market market, AsyncDatabase asyncDb, ConfigHandler config) {
        this.market = market;
        this.asyncDb = asyncDb;
        locale = market.getLocale();
        storage = market.getStorage();
        this.config = config;
    }

    public enum MarketAction {
        LISTING_CREATED("listing_created"),
        LISTING_BOUGHT("listing_bought"),
        LISTING_REMOVED("listing_removed"),
        LISTING_EXPIRED("listing_expired"),
        LISTING_SOLD("listing_sold"),
        EARNINGS_RETRIEVED("earnings_retrieved");

        private String action;

        private MarketAction(String action) {
            this.action = action;
        }

        @Override
        public String toString() {
            return action;
        }
    }

    public void storeHistory(String player, String who, MarketAction action, ItemStack item, double price) {
        int itemId = storage.storeItem(item);
        asyncDb.addStatement(new QueuedStatement("INSERT INTO `history`(`player`, `action`, `who`, `item`, `amount`, `price`, `time`) VALUES (?,?,?,?,?,?,?)")
        .setValue(player)
        .setValue(action.toString())
        .setValue(who)
        .setValue(itemId)
        .setValue(item.getAmount())
        .setValue(price)
        .setValue(System.currentTimeMillis()));
    }

    public void storeHistory(String player, String who, MarketAction action, List<ItemStack> items, double pricePerItem) {
        int itemId = storage.storeItem(items.get(0));
        for (ItemStack item : items) {
            asyncDb.addStatement(new QueuedStatement("INSERT INTO `history`(`player`, `action`, `who`, `item`, `amount`, `price`, `time`) VALUES (?,?,?,?,?,?,?)")
            .setValue(player)
            .setValue(action.toString())
            .setValue(who)
            .setValue(itemId)
            .setValue(item.getAmount())
            .setValue(pricePerItem * item.getAmount())
            .setValue(System.currentTimeMillis()));
        }
    }

    public void storeHistory(String player, String who, MarketAction action, int itemId, int amount, double price) {
        asyncDb.addStatement(new QueuedStatement("INSERT INTO `history`(`player`, `action`, `who`, `item`, `amount`, `price`, `time`) VALUES (?,?,?,?,?,?,?)")
        .setValue(player)
        .setValue(action.toString())
        .setValue(who)
        .setValue(itemId)
        .setValue(amount)
        .setValue(price)
        .setValue(System.currentTimeMillis()));
    }

    public void incrementSpent(String player, double amount) {
        if (config.getStorageMethod() == StorageMethod.SQLITE) {
            asyncDb.addStatement(new QueuedStatement("INSERT OR REPLACE INTO users (name, earned, spent) VALUES (?, COALESCE((SELECT earned FROM users WHERE name=?), 0) + ?, COALESCE((SELECT spent FROM users WHERE name=?), 0))")
            .setValue(player)
            .setValue(player)
            .setValue(amount)
            .setValue(player));
        } else {
            asyncDb.addStatement(new QueuedStatement("INSERT INTO users (name,earned,spent) VALUES (?,?,?) ON DUPLICATE KEY UPDATE earned=earned+?")
            .setValue(player)
            .setValue(0)
            .setValue(amount)
            .setValue(amount));
        }
    }

    public void incrementEarned(String player, double amount) {
        if (config.getStorageMethod() == StorageMethod.SQLITE) {
            asyncDb.addStatement(new QueuedStatement("INSERT OR REPLACE INTO users (name, earned, spent) VALUES (?, COALESCE((SELECT earned FROM users WHERE name=?), 0) + ?, COALESCE((SELECT spent FROM users WHERE name=?), 0))")
            .setValue(player)
            .setValue(player)
            .setValue(amount)
            .setValue(player));
        } else {
            asyncDb.addStatement(new QueuedStatement("INSERT INTO users (name,earned,spent) VALUES (?,?,?) ON DUPLICATE KEY UPDATE earned=earned+?")
            .setValue(player)
            .setValue(amount)
            .setValue(0)
            .setValue(amount));
        }
    }

    public double[] getMonetaryUsage(String player, Database db) throws SQLException {
        double[] eS = new double[] {0, 0, 0};
        MarketResult result = db.createStatement("SELECT * FROM users WHERE name=?").setString(player).query();
        if (result.next()) {
            eS[0] = result.getDouble(2);
            eS[1] = result.getDouble(3);
            eS[2] = eS[0] - eS[1];
        }
        result.close();
        return eS;
    }

    public String[] buildHistory(String player, int limit, Database db) {
        List<String> history = new ArrayList<String>();
        try {
            double[] eS = getMonetaryUsage(player, db);
            history.add(ChatColor.GREEN + locale.get("history.title", player));
            history.add(ChatColor.GRAY + "| " + ChatColor.GREEN + locale.get("history.total_earned", market.getEcon().format(eS[0])));
            history.add(ChatColor.GRAY + "| " + ChatColor.GREEN + locale.get("history.total_spent", market.getEcon().format(eS[1])));
            history.add(ChatColor.GRAY + "| " + ChatColor.GREEN + locale.get("history.actual_amount_made", market.getEcon().format(eS[2])));
            MarketResult result = db.createStatement("SELECT history.player, history.action, history.who, history.amount, history.price, history.time, items.item FROM history, items WHERE history.player = ? AND history.item = items.id")
                    .setString(player)
                    .query();
            while(result.next()) {
                String hist = "";
                String date = new Date(result.getLong("time")).toString();
                MarketAction action = MarketAction.valueOf(result.getString("action").toUpperCase());
                String who = result.getString("who");
                ItemStack item = result.getItemStack("item");
                double price = result.getDouble("price");
                String itemName = market.getItemName(item);
                String prefix = locale.get("history.prefix", result.getRow(), date);
                switch(action) {
                case LISTING_CREATED:
                    hist = prefix + ChatColor.RESET + locale.get("history.item_listed", itemName, market.getEcon().format(price) + ChatColor.RESET);
                    break;
                case LISTING_BOUGHT:
                    hist = prefix + ChatColor.RESET + locale.get("history.item_bought", itemName, market.getEcon().format(price) + ChatColor.RESET);
                    break;
                case LISTING_REMOVED:
                    hist = prefix + ChatColor.RESET + locale.get("history.listing_removed", who, itemName);
                    break;
                case LISTING_EXPIRED:
                    hist = prefix + ChatColor.RESET + locale.get("history.listing_expired", itemName);
                    break;
                case LISTING_SOLD:
                    hist = prefix + ChatColor.RESET + locale.get("history.item_sold", itemName, market.getEcon().format(price) + ChatColor.RESET);
                    break;
                case EARNINGS_RETRIEVED:
                    hist = prefix + ChatColor.RESET + locale.get("history.earnings_retrieved", who, market.getEcon().format(price) + ChatColor.RESET);
                    break;
                default:
                    break;
                }
                history.add(ChatColor.GRAY + hist);
            }
            result.close();
            if (history.size() == 4) {
                history.add(ChatColor.GRAY + "" + ChatColor.ITALIC + locale.get("history.none_yet"));
            }
        } catch(Exception e) {
            market.log.severe("Error while retrieving history for " + player + ":");
            e.printStackTrace();
        }
        return history.toArray(new String[0]);
    }

    public String getPricesInformation(ItemStack it, Database db) {
        ItemStack item = it.clone();
        item.setAmount(1);
        String itemName = market.getItemName(item);
        int itemId = 0;
        try {
            MarketResult result = db.createStatement("SELECT id FROM items WHERE item=?")
                    .setString(MarketStorage.itemStackToString(item))
                    .query();
            if (result.next()) {
                itemId = result.getInt(1);
            }
            if (itemId != 0) {
                int amount = 0;
                double price = 0;
                MarketResult res = db.createStatement("SELECT * FROM history WHERE item=? AND action=?")
                        .setInt(itemId)
                        .setString(MarketAction.LISTING_SOLD.toString())
                        .query();
                while (res.next()) {
                    amount += res.getInt("amount");
                    price += res.getDouble("price");
                }
                if (amount == 0 || price == 0) {
                    return ChatColor.YELLOW + market.getLocale().get("prices.no_data", itemName);
                }
                double average = price / amount;
                return ChatColor.GREEN + market.getLocale().get("prices.header", itemName) + "\n"
                + market.getLocale().get("prices.price_per_item", market.getEcon().format(average)) + "\n"
                + market.getLocale().get("prices.price_per_stack", market.getEcon().format(average * item.getMaxStackSize()));
            }
        } catch(Exception e) {
            market.log.severe("Error while building pricing information:");
            e.printStackTrace();
        }
        return ChatColor.YELLOW + market.getLocale().get("prices.no_data", itemName);
    }
}
