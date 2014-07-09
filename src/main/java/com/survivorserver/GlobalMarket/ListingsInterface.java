package com.survivorserver.GlobalMarket;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.survivorserver.GlobalMarket.HistoryHandler.MarketAction;
import com.survivorserver.GlobalMarket.Interface.MarketInterface;
import com.survivorserver.GlobalMarket.Interface.MarketItem;
import com.survivorserver.GlobalMarket.Lib.PacketManager;
import com.survivorserver.GlobalMarket.Lib.SearchResult;
import com.survivorserver.GlobalMarket.Lib.SortMethod;

public class ListingsInterface extends MarketInterface {

    protected Market market;
    private MarketStorage storage;

    public ListingsInterface(Market market) {
        this.market = market;
        storage = market.getStorage();
    }

    @Override
    public String getName() {
        return "Listings";
    }

    @Override
    public String getTitle() {
        return market.getLocale().get("interface.listings_title");
    }

    @Override
    public int getSize() {
        return 54;
    }

    @Override
    public boolean doSingleClickActions() {
        return false;
    }

    @Override
    public ItemStack prepareItem(MarketItem marketItem, InterfaceViewer viewer, int page, int slot, boolean leftClick, boolean shiftClick) {
        Listing listing = (Listing) marketItem;
        ItemStack item = storage.getItem(listing.getItemId(), listing.getAmount());
        ItemMeta meta = item.getItemMeta();

        boolean isSeller = viewer.getViewer().equalsIgnoreCase(listing.getSeller());
        boolean isAdmin = market.getInterfaceHandler().isAdmin(viewer.getViewer());

        List<String> lore = meta.getLore();
        if (!meta.hasLore()) {
            lore = new ArrayList<String>();
        }
        String price = ChatColor.WHITE + market.getLocale().get("price") + (listing.getPrice() > 0 ? market.getEcon().format(listing.getPrice()) : market.getLocale().get("free"));
        String seller = ChatColor.WHITE + market.getLocale().get("seller") + ChatColor.GRAY + ChatColor.ITALIC + listing.getSeller();
        lore.add(price);
        lore.add(seller);

        // Prevent things like hotbar swapping from triggering an action
        if (!shiftClick && !leftClick) {
            viewer.resetActions();
        }

        // Don't want people buying their own listings
        if (isSeller && leftClick) {
            viewer.resetActions();
        }

        // Or canceling listings they don't have permissions to
        if (!isSeller && shiftClick) {
            if (!isAdmin) {
                viewer.resetActions();
            }
        }

        if (!isSeller) {
            String buyMsg = ChatColor.YELLOW + market.getLocale().get("click_to_buy");
            if (leftClick) {
                if (market.getEcon().has(viewer.getViewer(), listing.getPrice())) {
                    if (viewer.getClicks() >= 2) {
                        if (!listing.getSeller().equalsIgnoreCase(market.getInfiniteSeller())) {
                            buyMsg = ChatColor.RED + market.getLocale().get("interface.transaction_error");
                        }
                    } else {
                        buyMsg = ChatColor.GREEN + market.getLocale().get("click_again_to_confirm");
                    }
                } else {
                    buyMsg = ChatColor.RED + market.getLocale().get("not_enough_money", market.getEcon().currencyNamePlural());
                    viewer.resetActions();
                }
            }
            lore.add(buyMsg);
        }

        if (isSeller || isAdmin) {
            String removeMsg = ChatColor.DARK_GRAY + market.getLocale().get("shift_click_to_remove");
            if (shiftClick) {
                removeMsg = ChatColor.GREEN + market.getLocale().get("shift_click_again_to_confirm");
            }
            lore.add(removeMsg);
        }

        if (listing.getSeller().equalsIgnoreCase(market.getInfiniteSeller())) {
            lore.add(ChatColor.LIGHT_PURPLE + market.getLocale().get("interface.infinite"));
        }

        int siblings = listing.countStacked();
        if (siblings > 0) {
            int count = 0;
            if (siblings <= 15) {
                for (Listing l : listing.getStacked()) {
                    count += l.getAmount();
                }
            }
            lore.add(ChatColor.AQUA + market.getLocale().get("interface.stacked", listing.getAmount(), count > 0 ? (count + listing.getAmount()) : market.getLocale().get("interface.stacked_many")));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public void handleLeftClickAction(InterfaceViewer viewer, MarketItem item, InventoryClickEvent event) {
        if (market.getCore().buyListing((Listing) item, (Player) event.getWhoClicked(), viewer, true, true, true)) {
            viewer.resetActions();
        }
    }

    @Override
    public void handleShiftClickAction(InterfaceViewer viewer, MarketItem item, InventoryClickEvent event) {
        viewer.resetActions();
        market.getCore().removeListing((Listing) item, (Player) event.getWhoClicked());
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<MarketItem> getContents(InterfaceViewer viewer) {
        return (List<MarketItem>)(List<?>) market.getStorage().getListings(viewer.getViewer(), viewer.getSort(), viewer.getWorld());
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<MarketItem> doSearch(InterfaceViewer viewer, String search) {
        SearchResult result = market.getStorage().getListings(viewer.getViewer(), viewer.getSort(), search, viewer.getWorld());
        viewer.setSearchSize(result.getTotalFound());
        return (List<MarketItem>)(List<?>) result.getPage();
    }

    @Override
    public MarketItem getItem(InterfaceViewer viewer, int id) {
        return market.getStorage().getListing(id);
    }

    @Override
    public boolean identifyItem(ItemMeta meta) {
        if (meta.hasDisplayName()) {
            String name = meta.getDisplayName();
            if (name.contains(market.getLocale().get("interface.page").replace(" %s", ""))) {
                return true;
            }
            if (name.contains(market.getLocale().get("interface.search"))) {
                return true;
            }
            if (name.contains(market.getLocale().get("interface.cancel_search"))) {
                return true;
            }
        }
        if (meta.hasLore()) {
            for (String lore : meta.getLore()) {
                if (lore.contains(market.getLocale().get("price")) || lore.contains(market.getLocale().get("click_to_retrieve"))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onInterfacePrepare(InterfaceViewer viewer, List<MarketItem> contents, ItemStack[] invContents, Inventory inv) {
    }

    @Override
    public void onInterfaceClose(InterfaceViewer viewer) {
        YamlConfiguration playerConf = market.getConfigHandler().getPlayerConfig(viewer.getViewer());
        if (!playerConf.getString("listings.sort_method").equalsIgnoreCase(viewer.getSort().toString())) {
            playerConf.set("listings.sort_method", viewer.getSort().toString());
            market.getConfigHandler().savePlayerConfig(viewer.getViewer());
        }
    }

    @Override
    public void onInterfaceOpen(InterfaceViewer viewer) {
        YamlConfiguration playerConf = market.getConfigHandler().getPlayerConfig(viewer.getViewer());
        if (!playerConf.isSet("listings.sort_method")) {
            playerConf.set("listings.sort_method", SortMethod.DEFAULT.toString());
            market.getConfigHandler().savePlayerConfig(viewer.getViewer());
        } else {
            viewer.setSort(SortMethod.valueOf(playerConf.getString("listings.sort_method").toUpperCase()));
        }
    }

    @Override
    public ItemStack getItemStack(InterfaceViewer viewer, MarketItem item) {
        return market.getStorage().getItem(item.getItemId(), item.getAmount());
    }

    @Override
    public void onUnboundClick(final Market market, final InterfaceHandler handler, final InterfaceViewer viewer, int slot, final InventoryClickEvent event) {
        super.onUnboundClick(market, handler, viewer, slot, event);
        int invSize = event.getInventory().getSize();

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        // Sort toggle
        if (slot == invSize - 5 && event.getAction() != InventoryAction.SWAP_WITH_CURSOR) {
            SortMethod sort = viewer.getSort();
            if (sort == SortMethod.DEFAULT) {
                viewer.setSort(SortMethod.PRICE_HIGHEST);
            } else if (sort == SortMethod.PRICE_HIGHEST) {
                viewer.setSort(SortMethod.PRICE_LOWEST);
            } else if(sort == SortMethod.PRICE_LOWEST) {
                viewer.setSort(SortMethod.AMOUNT_HIGHEST);
            } else {
                viewer.setSort(SortMethod.DEFAULT);
            }
            handler.refreshViewer(viewer, viewer.getInterface().getName());
            return;
        }

        if (market.useProtocolLib()) {
            // Create
            if (slot == invSize - 8) {
                if (event.getAction() == InventoryAction.SWAP_WITH_CURSOR) {
                    // Put the item back into the inv for safe keeping
                    int last = viewer.getLastLowerSlot();
                    Inventory inv = event.getWhoClicked().getInventory();
                    ItemStack cursor = event.getCursor().clone();
                    event.getWhoClicked().setItemOnCursor(new ItemStack(Material.AIR));
                    if (last >= 0) {
                        ItemStack lastSlot = inv.getItem(last);
                        if (lastSlot == null || lastSlot.getType() == Material.AIR) {
                            inv.setItem(last, cursor);
                        } else {
                            ItemStack lastItem = inv.getItem(last);
                            if (lastItem.getType() == cursor.getType()) {
                                lastItem.setAmount(lastItem.getAmount() + cursor.getAmount());
                            } else {
                                event.getWhoClicked().getWorld().dropItem(event.getWhoClicked().getLocation(), cursor);
                            }
                        }
                        create((Player) event.getWhoClicked(), inv.getItem(last), viewer);
                    }
                } else {
                    viewer.resetActions();
                    handler.refreshFunctionBar(viewer);
                }
            }
        }
    }

    @Override
    public void buildFunctionBar(Market market, InterfaceHandler handler, InterfaceViewer viewer, ItemStack[] contents, boolean pPage, boolean nPage) {
        super.buildFunctionBar(market, handler, viewer, contents, pPage, nPage);

        // Sort toggle
        ItemStack curPage = new ItemStack(Material.REDSTONE_COMPARATOR);
        ItemMeta curMeta = curPage.getItemMeta();
        if (curMeta == null) {
            curMeta = market.getServer().getItemFactory().getItemMeta(curPage.getType());
        }
        curMeta.setDisplayName(ChatColor.WHITE + market.getLocale().get("interface.sort_by"));
        List<String> curLore = new ArrayList<String>();
        curLore.add(ChatColor.YELLOW + market.getLocale().get("interface.sorting_by", market.getLocale().get("interface.sort_methods." + viewer.getSort().toString())));
        curMeta.setLore(curLore);
        curPage.setItemMeta(curMeta);
        contents[contents.length - 5] = curPage;

        if (market.useProtocolLib()) {
            // Create button
            ItemStack create = new ItemStack(Material.HOPPER);
            ItemMeta createMeta = create.getItemMeta();
            createMeta.setDisplayName(ChatColor.RESET + market.getLocale().get("interface.create"));
            List<String> createLore = new ArrayList<String>();
            if (viewer.getCreateMessage() != null) {
                createLore.add(ChatColor.RED + "<" + viewer.getCreateMessage() + ">");
                viewer.resetActions();
            } else {
                createLore.add(ChatColor.GREEN + market.getLocale().get("interface.swap_to_create"));
            }
            createMeta.setLore(createLore);
            create.setItemMeta(createMeta);
            contents[contents.length - 8] = create;
        }
    }

    private void create(final Player player, final ItemStack item, final InterfaceViewer viewer) {
        // Not sure that this can happen, but better safe than sorry!
        if (player == null) {
            return;
        }
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        final LocaleHandler locale = market.getLocale();
        final PacketManager packet = market.getPacket();
        if (player.getGameMode() == GameMode.CREATIVE && !market.allowCreative(player)) {
            viewer.setCreateMessage(locale.get("not_allowed_while_in_creative"));
            market.getInterfaceHandler().refreshFunctionBar(viewer);
            return;
        }
        if (market.itemBlacklisted(item)) {
            viewer.setCreateMessage(locale.get("item_is_blacklisted"));
            market.getInterfaceHandler().refreshFunctionBar(viewer);
            return;
        }
        int max = market.maxListings(player);
        if (max > 0 && storage.getNumListingsFor(player.getName(), player.getWorld().getName()) >= max) {
            viewer.setCreateMessage(locale.get("selling_too_many_items"));
            market.getInterfaceHandler().refreshFunctionBar(viewer);
            return;
        }
        int maxMail = market.getMaxMail(player);
        if (maxMail > 0) {
            if (market.getStorage().getNumMail(player.getName(), player.getWorld().getName(), true) >= maxMail) {
                viewer.setCreateMessage(locale.get("full_mailbox"));
                market.getInterfaceHandler().refreshFunctionBar(viewer);
                return;
            }
        }
        // Suspend the viewer so we can unsuspend later
        market.getInterfaceHandler().suspendViewer(viewer);
        // Will disconnect client if you close their inventory on the same tick as the event. I think.
        new BukkitRunnable() {
            @Override
            public void run() {
                player.closeInventory();
                // Let's get their input
                String[] placeholder = new String[] {ChatColor.AQUA + locale.get("interface.amount_sign") + ChatColor.RESET, Integer.toString(item.getAmount()), ChatColor.AQUA + locale.get("interface.price_sign") + ChatColor.RESET, ""};
                packet.getMessage().display(player, ChatColor.YELLOW + market.getLocale().get("interface.specify_amount_and_price"), 100);
                packet.getSignInput().create(player, placeholder, packet.getSignInput().new InputResult() {
                    @Override
                    public void finished(final Player player, final String[] input, final boolean cancelled) {
                        // This is async and we need to synchronize again...
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (cancelled) {
                                    // The player has disconnected
                                    market.getInterfaceHandler().purgeViewer(player);
                                    return;
                                }
                                market.getInterfaceHandler().unsuspendViewer(player, viewer);
                                int ticks = 80;
                                int amount = 0;
                                try {
                                    amount = Integer.parseInt(input[1]);
                                } catch(NumberFormatException e) {
                                    packet.getMessage().display(player, ChatColor.RED + locale.get("not_a_valid_number", input[1]), ticks);
                                    return;
                                }
                                if (amount <= 0) {
                                    packet.getMessage().display(player, ChatColor.RED + locale.get("not_a_valid_amount", input[1]), ticks);
                                    return;
                                }
                                double price = 0;
                                try {
                                    price = Double.parseDouble(input[3]);
                                } catch(NumberFormatException e) {
                                    packet.getMessage().display(player, ChatColor.RED + locale.get("not_a_valid_number", input[3]), ticks);
                                    return;
                                }
                                if (price < 0.01) {
                                    packet.getMessage().display(player, ChatColor.RED + locale.get("price_too_low"), ticks);
                                    return;
                                }
                                double maxPrice = market.getMaxPrice(player, player.getItemInHand());
                                if (maxPrice > 0 && price > maxPrice) {
                                    packet.getMessage().display(player, ChatColor.RED + locale.get("price_too_high"), ticks);
                                    return;
                                }
                                if (item.getAmount() < amount) {
                                    int carrying = 0;
                                    for (ItemStack i: player.getInventory().getContents()) {
                                        if (i != null && i.isSimilar(item)) {
                                            carrying += i.getAmount();
                                        }
                                    }
                                    if (amount > carrying) {
                                        packet.getMessage().display(player, ChatColor.RED + locale.get("you_dont_have_x_of_this_item", amount), ticks);
                                        return;
                                    }
                                }
                                double fee = market.getCreationFee(player, price);
                                if (fee > 0) {
                                    if (!market.getEcon().has(player.getName(), fee)) {
                                        packet.getMessage().display(player, ChatColor.RED + locale.get("you_cant_pay_this_fee"), ticks);
                                        return;
                                    }
                                    market.getEcon().withdrawPlayer(player.getName(), fee);
                                }
                                List<ItemStack> toList = new ArrayList<ItemStack>();
                                if (amount < item.getAmount()) {
                                    item.setAmount(item.getAmount() - amount);
                                    ItemStack toAdd = item.clone();
                                    toAdd.setAmount(amount);
                                    toList.add(toAdd);
                                } else if (amount == item.getAmount()) {
                                    toList.add(item.clone());
                                    player.getInventory().setItem(viewer.getLastLowerSlot(), new ItemStack(Material.AIR));
                                } else {
                                    int am = amount;
                                    ItemStack[] contents = player.getInventory().getContents();
                                    for (int i = 0; i < contents.length; i++) {
                                        if (am == 0) {
                                            break;
                                        }
                                        ItemStack c = contents[i];
                                        if (c != null && c.isSimilar(item)) {
                                            if (am >= c.getAmount()) {
                                                ItemStack a = c.clone();
                                                contents[i] = null;
                                                am -= a.getAmount();
                                                toList.add(a);
                                            } else {
                                                ItemStack a = c.clone();
                                                c.setAmount(c.getAmount() - am);
                                                a.setAmount(am);
                                                am = 0;
                                                toList.add(a);
                                                break;
                                            }
                                        }
                                    }
                                    player.getInventory().setContents(contents);
                                }
                                String world = player.getWorld().getName();
                                if (toList.size() > 1) {
                                    double pricePer = new BigDecimal(price / amount).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
                                    int tradeTime = market.getTradeTime(player);
                                    if (tradeTime > 0) {
                                        storage.queueListing(player.getName(), toList, pricePer, world);
                                        packet.getMessage().display(player, fee > 0 ? ChatColor.GREEN + locale.get("items_queued_with_fee", tradeTime, fee) : ChatColor.GREEN + locale.get("items_queued", tradeTime), ticks);
                                    } else {
                                        storage.createListing(player.getName(), toList, pricePer, world);
                                        packet.getMessage().display(player, fee > 0 ? ChatColor.GREEN + locale.get("items_listed_with_fee", fee) : ChatColor.GREEN + locale.get("items_listed"), ticks);
                                    }
                                    if (market.enableHistory()) {
                                        market.getHistory().storeHistory(player.getName(), "", MarketAction.LISTING_CREATED, toList, price);
                                    }
                                } else {
                                    int tradeTime = market.getTradeTime(player);
                                    if (tradeTime > 0) {
                                        storage.queueListing(player.getName(), toList.get(0), price, world);
                                        packet.getMessage().display(player, fee > 0 ? ChatColor.GREEN + locale.get("item_queued_with_fee", tradeTime, fee) : ChatColor.GREEN + locale.get("item_queued", tradeTime), ticks);
                                    } else {
                                        storage.createListing(player.getName(), toList.get(0), price, world);
                                        packet.getMessage().display(player, fee > 0 ? ChatColor.GREEN + locale.get("item_listed_with_fee", fee) : ChatColor.GREEN + locale.get("item_listed"), ticks);
                                    }
                                    if (market.enableHistory()) {
                                        market.getHistory().storeHistory(player.getName(), "", MarketAction.LISTING_CREATED, toList.get(0), price);
                                    }
                                }
                            }
                        }.runTaskLater(market, 1);
                    }
                });
            }
        }.runTaskLater(market, 1);
    }
}
