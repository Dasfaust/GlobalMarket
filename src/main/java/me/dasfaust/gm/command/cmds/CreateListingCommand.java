package me.dasfaust.gm.command.cmds;

import me.dasfaust.gm.BlacklistHandler;
import me.dasfaust.gm.Core;
import me.dasfaust.gm.StorageHelper;
import me.dasfaust.gm.command.CommandContext;
import me.dasfaust.gm.config.Config;
import me.dasfaust.gm.menus.Menus;
import me.dasfaust.gm.tools.LocaleHandler;
import me.dasfaust.gm.trade.MarketListing;
import me.dasfaust.gm.trade.StockedItem;
import me.dasfaust.gm.trade.WrappedStack;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CreateListingCommand extends CommandContext
{
    public CreateListingCommand()
    {
        super(
                new String[] {
                        "create",
                        "list"
                },
                "globalmarket.command.createlisting",
                1,
                "command_helptext_createlisting",
                true,
                true
        );
    }

    @Override
    public void process(CommandSender sender, String[] arguments)
    {
        WrappedStack stack;
        Player player = (Player) sender;
        ItemStack inHand = player.getItemInHand();
        if (inHand == null || inHand.getType() == Material.AIR || BlacklistHandler.check(stack = new WrappedStack(inHand)))
        {
            sender.sendMessage(LocaleHandler.get().get("command_createserverlisting_no_item"));
            return;
        }
        double price;
        try
        {
            price = Double.parseDouble(arguments[0]);
            if (price <= 0)
            {
                throw new NumberFormatException("price is too low!");
            }
        }
        catch(Exception e)
        {
            sender.sendMessage(LocaleHandler.get().get("command_createserverlisting_invalid_amount", e.getMessage()));
            return;
        }
        int amount = stack.getAmount();
        if (arguments.length == 2)
        {
            try
            {
                int am = Integer.parseInt(arguments[1]);
                if (am <= 0 || am > amount)
                {
                    throw new NumberFormatException("amount is too low or too high!");
                }
                amount = am;
            }
            catch(Exception e)
            {
                sender.sendMessage(LocaleHandler.get().get("command_createserverlisting_invalid_amount", e.getMessage()));
                return;
            }
        }
        long storageId = Core.instance.storage().store(stack);
        if (!Core.instance.config().get(Config.Defaults.DISABLE_STOCK))
        {
            if (Core.instance.storage().getAll(MarketListing.class, StorageHelper.allListingsFor(player.getUniqueId(), storageId)).size() > 0)
            {
                sender.sendMessage(LocaleHandler.get().get("command_create_failed_already_listed"));
                return;
            }
            if (StorageHelper.isStockFull(player.getUniqueId()))
            {
                sender.sendMessage(LocaleHandler.get().get("command_create_failed_stock_full"));
                return;
            }
            StockedItem stock = StorageHelper.stockFor(player.getUniqueId(), storageId);
            if (stock != null)
            {
                if (stock.amount + amount > Core.instance.config().get(Config.Defaults.STOCK_SLOTS_SIZE))
                {
                    sender.sendMessage(LocaleHandler.get().get("command_create_failed_stock_full"));
                    return;
                }
                StorageHelper.updateStockAmount(stock, stock.amount + amount);
            }
            else
            {
                stock = new StockedItem();
                stock.amount = amount;
                stock.creationTime = System.currentTimeMillis();
                stock.itemId = storageId;
                stock.owner = player.getUniqueId();
                stock.world = player.getWorld().getUID();
                Core.instance.storage().store(stock);
            }
        }
        if (stack.getAmount() == amount)
        {
            player.setItemInHand(new ItemStack(Material.AIR));
        }
        else
        {
            player.setItemInHand(stack.setAmount(stack.getAmount() - amount).bukkit());
        }
        MarketListing listing = new MarketListing();
        listing.seller = player.getUniqueId();
        listing.world = player.getWorld().getUID();
        listing.itemId = Core.instance.storage().store(stack);
        listing.amount = amount;
        listing.price = price;
        listing.creationTime = System.currentTimeMillis();
        Core.instance.storage().store(listing);
        Core.instance.handler().rebuildAllMenus(Menus.MENU_LISTINGS);
        sender.sendMessage(LocaleHandler.get().get("command_create_success"));
    }
}
