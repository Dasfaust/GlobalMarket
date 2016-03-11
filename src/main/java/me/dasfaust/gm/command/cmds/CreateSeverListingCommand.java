package me.dasfaust.gm.command.cmds;

import me.dasfaust.gm.BlacklistHandler;
import me.dasfaust.gm.Core;
import me.dasfaust.gm.command.CommandContext;
import me.dasfaust.gm.menus.Menus;
import me.dasfaust.gm.tools.LocaleHandler;
import me.dasfaust.gm.trade.ServerListing;
import me.dasfaust.gm.trade.WrappedStack;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CreateSeverListingCommand extends CommandContext
{
    public CreateSeverListingCommand()
    {
        super(
                new String[] {
                        "createinf",
                        "listinf"
                },
                "globalmarket.command.createserverlisting",
                1,
                "command_helptext_createserverlisting",
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
        ServerListing listing = new ServerListing();
        listing.amount = amount;
        listing.price = price;
        listing.itemId = storageId;
        listing.creationTime = System.currentTimeMillis();
        listing.world = player.getWorld().getUID();
        Core.instance.storage().store(listing);
        Core.instance.handler().rebuildAllMenus(Menus.MENU_SERVER_LISTINGS);
        sender.sendMessage(LocaleHandler.get().get("command_createserverlisting_listing_created"));
    }
}
