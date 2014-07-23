package com.survivorserver.GlobalMarket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import com.survivorserver.GlobalMarket.Interface.IMarketItem;

public class Listing implements IMarketItem, Comparable<Listing> {

    public int id;
    public int itemId;
    public int amount;
    public String seller;
    public double price;
    public String world;
    public Long time;
    public List<Listing> stacked;
    // Legacy
    ItemStack item;

    public Listing() {
        this.stacked = new ArrayList<Listing>();
    }

    public Listing(int id, String seller, int itemId, int amount, double price, String world, Long time) {
        this.id = id;
        this.itemId = itemId;
        this.amount = amount;
        this.seller = seller;
        this.price = price;
        this.world = world;
        this.time = time;
        this.stacked = new ArrayList<Listing>();
    }

    /*
     * Legacy constructor
     */
    public Listing(int id, ItemStack item, String seller, double price, Long time) {
        this.id = id;
        this.seller = seller;
        this.price = price;
        this.time = time;
        this.item = item;
    }

    public int getId() {
        return id;
    }

    public int getItemId() {
        return itemId;
    }

    public int getAmount() {
        return amount;
    }

    public String getSeller() {
        return seller;
    }

    public double getPrice() {
        return price;
    }

    public String getWorld() {
        return world;
    }

    public Long getTime() {
        return time;
    }

    /**
     * Should only be used by the legacy importer
     * @deprecated
     * @return ItemStack associated with this item
     */
    public ItemStack getItem() {
        return item;
    }

    @Override
    public int compareTo(Listing l) {
        int s = seller.compareTo(l.getSeller());
        if (s == 0) {
            int it = new Integer(itemId).compareTo(l.getItemId());
            if (it == 0) {
                double ppa1 = l.getPrice() / l.getAmount();
                double ppa2 = this.price / this.amount;
                return Double.compare(ppa1, ppa2);
            }
            return it;
        }
        return s;
    }

    public boolean isStackable(Listing l) {
        return compareTo(l) == 0 ? true : false;
    }

    public void addStacked(Listing l) {
        if (!stacked.contains(l)) {
            stacked.add(l);
        }
    }

    public int countStacked() {
        stacked.removeAll(Collections.singleton(null));
        return stacked.size();
    }

    public List<Listing> getStacked() {
        return stacked;
    }

    public void setStacked(List<Listing> siblings) {
        stacked.clear();
        stacked.addAll(siblings);
    }

    public static class Comparators {

        public static Comparator<Listing> RECENT = new Comparator<Listing>() {
            @Override
            public int compare(Listing o1, Listing o2) {
                return new Long(o2.getTime()).compareTo(o1.getTime());
            }
        };

        public static Comparator<Listing> PRICE_LOWEST = new Comparator<Listing>() {
            @Override
            public int compare(Listing o1, Listing o2) {
                return new Double(o1.price).compareTo(o2.price);
            }
        };

        public static Comparator<Listing> PRICE_HIGHEST = new Comparator<Listing>() {
            @Override
            public int compare(Listing o1, Listing o2) {
                return new Double(o2.price).compareTo(o1.price);
            }
        };

        public static Comparator<Listing> AMOUNT_HIGHEST = new Comparator<Listing>() {
            @Override
            public int compare(Listing o1, Listing o2) {
                int o1c = o1.getAmount();
                if (o1.countStacked() > 0) {
                    for (Listing l : o1.getStacked()) {
                        o1c += l.getAmount();
                    }
                }
                int o2c = o2.getAmount();
                if (o2.countStacked() > 0) {
                    for (Listing l : o2.getStacked()) {
                        o2c += l.getAmount();
                    }
                }
                if (o1c > o2c) {
                    return -1;
                } else if (o1c < o2c) {
                    return 1;
                } else {
                    return 0;
                }
            }
        };
    }
}
