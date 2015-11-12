package me.dasfaust.gm.command.cmds;

import me.dasfaust.gm.BlacklistHandler;
import me.dasfaust.gm.Core;
import me.dasfaust.gm.StorageHelper;
import me.dasfaust.gm.command.CommandContext;
import me.dasfaust.gm.config.Config;
import me.dasfaust.gm.menus.Menus;
import me.dasfaust.gm.storage.abs.StorageHandler;
import me.dasfaust.gm.tools.LocaleHandler;
import me.dasfaust.gm.trade.StockedItem;
import me.dasfaust.gm.trade.WrappedStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class SendCommand extends CommandContext
{
    public SendCommand()
    {
        super(
                new String[] {
                        "send",
                        "gift",
                        "give",
                        "s"
                },
                "globalmarket.command.send",
                1,
                "command_helptext_send",
                true,
                true
        );
    }

    @Override
    public void process(CommandSender sender, String[] arguments)
    {
        if (Core.instance.config().get(Config.Defaults.DISABLE_STOCK))
        {
            sender.sendMessage(LocaleHandler.get().get("command_send_failed_no_stock"));
            return;
        }
        WrappedStack stack;
        Player player = (Player) sender;
        ItemStack inHand = player.getItemInHand();
        if (inHand == null || inHand.getType() == Material.AIR || BlacklistHandler.check(stack = new WrappedStack(inHand)))
        {
            sender.sendMessage(LocaleHandler.get().get("command_send_no_item"));
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
                sender.sendMessage(LocaleHandler.get().get("command_send_invalid_amount", e.getMessage()));
                return;
            }
        }
        String sendingTo = arguments[0];
        UUID uuidSendingTo = Core.instance.storage().findPlayer(sendingTo);
        if (uuidSendingTo == null)
        {
            sender.sendMessage(LocaleHandler.get().get("general_player_not_found", sendingTo));
            return;
        }
        if (StorageHelper.isStockFull(uuidSendingTo))
        {
            sender.sendMessage(LocaleHandler.get().get("command_send_failed_stock_full", sendingTo));
            return;
        }
        long storageId = Core.instance.storage().store(stack);
        StockedItem receiverStock = StorageHelper.stockFor(uuidSendingTo, storageId);
        if (receiverStock != null)
        {
            if (receiverStock.amount + amount > Core.instance.config().get(Config.Defaults.STOCK_SLOTS_SIZE))
            {
                sender.sendMessage(LocaleHandler.get().get("command_send_failed_stock_full", sendingTo));
                return;
            }
            StorageHelper.updateStockAmount(receiverStock, receiverStock.amount + amount);
        }
        else
        {
            StockedItem stock = new StockedItem();
            stock.amount = amount;
            stock.creationTime = System.currentTimeMillis();
            stock.itemId = storageId;
            stock.owner = uuidSendingTo;
            stock.world = player.getWorld().getUID();
            Core.instance.storage().store(stock);
        }
        if (stack.getAmount() == amount)
        {
            player.setItemInHand(new ItemStack(Material.AIR));
        }
        else
        {
            player.setItemInHand(stack.setAmount(stack.getAmount() - amount).bukkit());
        }
        Core.instance.handler().rebuildAllMenus(Menus.MENU_STOCK);
        Player receiving;
        if ((receiving = Bukkit.getPlayer(uuidSendingTo)) != null)
        {
            receiving.sendMessage(LocaleHandler.get().get("command_send_received", sender.getName()));
        }
        sender.sendMessage(LocaleHandler.get().get("command_send_item_sent"));
    }
}
