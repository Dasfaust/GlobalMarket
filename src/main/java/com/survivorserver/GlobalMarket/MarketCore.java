package com.survivorserver.GlobalMarket;

import java.util.Arrays;

import com.survivorserver.GlobalMarket.Lib.Cauldron.CauldronHelper;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.survivorserver.GlobalMarket.HistoryHandler.MarketAction;

public class MarketCore {

    Market market;
    InterfaceHandler handler;
    MarketStorage storage;

    public MarketCore(Market market, InterfaceHandler handler, MarketStorage storage) {
        this.market = market;
        this.handler = handler;
        this.storage = storage;
    }

    public boolean buyListing(Listing listing, Player player, InterfaceViewer viewer, boolean removeListing, boolean mailItem, boolean refreshInterface) {
        double originalPrice = listing.getPrice();
        double cutPrice = originalPrice;
        Economy econ = market.getEcon();
        String seller = listing.getSeller();
        String infAccount = market.getInfiniteAccount();
        boolean isInfinite = listing.getSeller().equalsIgnoreCase(market.getInfiniteSeller());
        String buyer = player.getName();
        double cut = market.getCut(listing.getPrice(), listing.getSeller(), listing.getWorld());
        if (cut > 0) {
            cutPrice = originalPrice - cut;
        }
        ItemStack item = viewer.getInterface().getItemStack(viewer, listing);
        // Make the transaction between buyer and seller
        EconomyResponse response = econ.withdrawPlayer(buyer, originalPrice);
        if (!response.transactionSuccess()) {
            if (response.type == ResponseType.NOT_IMPLEMENTED) {
                market.log.severe(econ.getName() + " may not be compatible with GlobalMarket. It does not support the withdrawPlayer() function.");
            } else {
                market.log.severe("Recieved failed economy response from " + econ.getName() + ": " + response.errorMessage);
            }
            return false;
        }
        if (isInfinite && infAccount.length() >= 1) {
            // Put the money earned in the infinite seller's account
            response = econ.depositPlayer(infAccount, cutPrice);
            if (!response.transactionSuccess()) {
                if (response.type == ResponseType.NOT_IMPLEMENTED) {
                    market.log.severe(econ.getName() + " may not be compatible with GlobalMarket. It does not support the depositPlayer() function.");
                } else {
                    market.log.severe("Recieved failed economy response from " + econ.getName() + ": " + response.errorMessage);
                }
                return false;
            }
        } else {
            // Direct deposit?
            if (market.autoPayment()) {
                response = econ.depositPlayer(seller, cutPrice);
                if (!response.transactionSuccess()) {
                    if (response.type == ResponseType.NOT_IMPLEMENTED) {
                        market.log.severe(econ.getName() + " may not be compatible with GlobalMarket. It does not support the depositPlayer() function.");
                    } else {
                        market.log.severe("Recieved failed economy response from " + econ.getName() + ": " + response.errorMessage);
                    }
                    return false;
                }
            } else {
                // Send a Transaction Log
                storage.storePayment(item, seller, buyer, originalPrice, cutPrice, cut, listing.getWorld());
            }
            // Seller's stats
            if (market.enableHistory()) {
                market.getHistory().storeHistory(seller, buyer, MarketAction.LISTING_SOLD, listing.getItemId(), listing.getAmount(), originalPrice);
                market.getHistory().incrementEarned(seller, cutPrice);
                market.getHistory().incrementSpent(buyer, originalPrice);
                market.getHistory().storeHistory(buyer, seller, MarketAction.LISTING_BOUGHT, listing.getItemId(), listing.getAmount(), originalPrice);
            }
        }
        // Transfer the item to where it belongs
        if (mailItem) {
            int mailTime = market.getMailTime(player);
            if (mailTime > 0 && market.queueOnBuy() && !player.hasPermission("globalmarket.noqueue")) {
                storage.queueMail(buyer, null, listing.getItemId(), listing.getAmount(), listing.getWorld());
                player.sendMessage(ChatColor.GREEN + market.getLocale().get("item_will_send", mailTime));
            } else {
                storage.createMail(buyer, null, listing.getItemId(), listing.getAmount(), listing.getWorld());
            }
        }
        if (!isInfinite && removeListing) {
            storage.removeListing(listing.getId());
        }
        String itemName = market.getItemName(item);
        market.notifyPlayer(seller, market.autoPayment() ? market.getLocale().get("you_sold_your_listing", itemName) :
            market.getLocale().get("listing_purchased_mailbox", itemName));
        market.notifyPlayer(buyer, market.getLocale().get("you_have_new_mail"));
        // Update viewers
        if (refreshInterface) {
            handler.updateAllViewers();
        }
        return true;
    }

    public synchronized boolean buyListing(Listing listing, String buyer, boolean removeListing, boolean refreshInterface) {
        double originalPrice = listing.getPrice();
        double cutPrice = originalPrice;
        Economy econ = market.getEcon();
        String seller = listing.getSeller();
        String infAccount = market.getInfiniteAccount();
        boolean isInfinite = listing.getSeller().equalsIgnoreCase(market.getInfiniteSeller());
        double cut = market.getCut(listing.getPrice(), listing.getSeller(), listing.getWorld());
        if (cut > 0) {
            cutPrice = originalPrice - cut;
        }
        ItemStack item = storage.getItem(listing.getItemId(), listing.getAmount());
        if (!econ.has(buyer, listing.getPrice())) {
            return false;
        }
        // Make the transaction between buyer and seller
        EconomyResponse response = econ.withdrawPlayer(buyer, originalPrice);
        if (!response.transactionSuccess()) {
            if (response.type == ResponseType.NOT_IMPLEMENTED) {
                market.log.severe(econ.getName() + " may not be compatible with GlobalMarket. It does not support the withdrawPlayer() function.");
            }
            return false;
        }
        if (isInfinite && infAccount.length() >= 1) {
            // Put the money earned in the infinite seller's account
            response = econ.depositPlayer(infAccount, cutPrice);
            if (!response.transactionSuccess()) {
                if (response.type == ResponseType.NOT_IMPLEMENTED) {
                    market.log.severe(econ.getName() + " may not be compatible with GlobalMarket. It does not support the depositPlayer() function.");
                }
                return false;
            }
        } else {
            // Direct deposit?
            if (market.autoPayment()) {
                response = econ.depositPlayer(seller, cutPrice);
                if (!response.transactionSuccess()) {
                    if (response.type == ResponseType.NOT_IMPLEMENTED) {
                        market.log.severe(econ.getName() + " may not be compatible with GlobalMarket. It does not support the depositPlayer() function.");
                    }
                    return false;
                }
            } else {
                // Send a Transaction Log
                storage.storePayment(item, seller, buyer, originalPrice, cutPrice, cut, listing.getWorld());
            }
            // Seller's stats
            if (market.enableHistory()) {
                market.getHistory().storeHistory(seller, buyer, MarketAction.LISTING_SOLD, listing.getItemId(), listing.getAmount(), originalPrice);
                market.getHistory().incrementEarned(seller, cutPrice);
                market.getHistory().incrementSpent(buyer, originalPrice);
                market.getHistory().storeHistory(buyer, seller, MarketAction.LISTING_BOUGHT, listing.getItemId(), listing.getAmount(), originalPrice);
            }
        }
        // Transfer the item to where it belongs
        storage.createMail(buyer, null, listing.getItemId(), listing.getAmount(), listing.getWorld());
        if (!isInfinite && removeListing) {
            storage.removeListing(listing.getId());
        }
        String itemName = market.getItemName(item);
        market.notifyPlayer(seller, market.autoPayment() ? market.getLocale().get("you_sold_your_listing_of", itemName) :
            market.getLocale().get("listing_purchased_mailbox", itemName));
        market.notifyPlayer(buyer, market.getLocale().get("you_have_new_mail"));
        // Update viewers
        if (refreshInterface) {
            handler.updateAllViewers();
        }
        return true;
    }

    public void removeListing(Listing listing, Player player) {
        if (!listing.getSeller().equalsIgnoreCase(market.getInfiniteSeller())) {
            int mailTime = market.getMailTime(player);
            if (mailTime > 0 && market.queueOnBuy() && !player.hasPermission("globalmarket.noqueue")) {
                storage.queueMail(listing.getSeller(), null, listing.getItemId(), listing.getAmount(), listing.getWorld());
                player.sendMessage(ChatColor.GREEN + market.getLocale().get("item_will_send", mailTime));
            } else {
                storage.createMail(listing.getSeller(), null, listing.getItemId(), listing.getAmount(), listing.getWorld());
            }
        }
        storage.removeListing(listing.getId());
        handler.updateAllViewers();
        if (market.enableHistory()) {
            if (!listing.getSeller().equalsIgnoreCase(market.getInfiniteSeller())) {
                if (listing.getSeller().equalsIgnoreCase(player.getName())) {;
                    market.getHistory().storeHistory(listing.getSeller(), "You", MarketAction.LISTING_REMOVED, listing.getItemId(), listing.getAmount(), 0);
                } else {
                    market.getHistory().storeHistory(listing.getSeller(), player.getName(), MarketAction.LISTING_REMOVED, listing.getItemId(), listing.getAmount(), 0);
                }
            }
        }
        market.notifyPlayer(listing.getSeller(), market.getLocale().get("you_have_new_mail"));
    }

    public synchronized void removeListing(Listing listing, String player) {
        if (!listing.getSeller().equalsIgnoreCase(market.getInfiniteSeller())) {
            storage.createMail(listing.getSeller(), null, listing.getItemId(), listing.getAmount(), listing.getWorld());
        }
        storage.removeListing(listing.getId());
        handler.updateAllViewers();
        if (market.enableHistory()) {
            if (!listing.getSeller().equalsIgnoreCase(market.getInfiniteSeller())) {
                if (listing.getSeller().equalsIgnoreCase(player)) {;
                    market.getHistory().storeHistory(listing.getSeller(), "You", MarketAction.LISTING_REMOVED, listing.getItemId(), listing.getAmount(), 0);
                } else {
                    market.getHistory().storeHistory(listing.getSeller(), player, MarketAction.LISTING_REMOVED, listing.getItemId(), listing.getAmount(), 0);
                }
            }
        }
        market.notifyPlayer(listing.getSeller(), market.getLocale().get("you_have_new_mail"));
    }

    public synchronized void expireListing(Listing listing) {
        if (!listing.getSeller().equalsIgnoreCase(market.getInfiniteSeller())) {
            storage.createMail(listing.getSeller(), "Expired", listing.getItemId(), listing.getAmount(), listing.getWorld());
        }
        storage.removeListing(listing.getId());
        handler.updateAllViewers();
        if (!listing.getSeller().equalsIgnoreCase(market.getInfiniteSeller())) {
            if (market.enableHistory()) {
                market.getHistory().storeHistory(listing.getSeller(), null, MarketAction.LISTING_EXPIRED, listing.getItemId(), listing.getAmount(), 0);
            }
        }
        market.notifyPlayer(listing.getSeller(), market.getLocale().get("you_have_new_mail"));
    }

    public void retrieveMail(Mail mail, InterfaceViewer viewer, Player player, boolean transactionLog) {
        Inventory playerInv = player.getInventory();
        ItemStack item = storage.getItem(mail.getItemId(), mail.getAmount());
        if (transactionLog) {
            ItemMeta meta = item.getItemMeta();
            meta.setLore(Arrays.asList(new String[] {ChatColor.GRAY + market.getLocale().get("transaction_log.unsignable")}));
            item.setItemMeta(meta);
        }
        if (market.mcpcpSupportEnabled()) {
            CauldronHelper.addItemToInventory(player.getName(), item);
        } else {
            playerInv.addItem(item);
        }
        storage.removeMail(mail.getId());
    }
}
