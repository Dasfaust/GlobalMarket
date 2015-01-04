package com.survivorserver.GlobalMarket.Command;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.survivorserver.GlobalMarket.LocaleHandler;
import com.survivorserver.GlobalMarket.Market;
import com.survivorserver.GlobalMarket.Lib.Cauldron.CauldronHelper;
import com.survivorserver.GlobalMarket.SQL.Database;

public class PriceCheckCommand extends SubCommand {

    public PriceCheckCommand(Market market, LocaleHandler locale) {
        super(market, locale);
    }

    @Override
    public String getCommand() {
        return "pricecheck";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"price", "pc"};
    }

    @Override
    public String getPermissionNode() {
        return "globalmarket.pricecheck";
    }

    @Override
    public String getHelp() {
        return locale.get("cmd.prefix") + locale.get("cmd.pc_syntax") + " " + locale.get("cmd.pc_descr");
    }

    @Override
    public boolean allowConsoleSender() {
        return false;
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (!market.enableHistory()) {
            sender.sendMessage(ChatColor.RED + locale.get("history_not_enabled"));
            return true;
        }
        final Player player = (Player) sender;
        if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR) {
            final ItemStack item = player.getItemInHand();
            new BukkitRunnable() {
                public void run() {
                    Database db = market.getStorage().getAsyncDb().getDb();
                    synchronized(db) {
                    	if (market.mcpcpSupportEnabled()) {
                            player.sendMessage(market.getHistory().getPricesInformation(CauldronHelper.wrapItemStack(item), db));
                        } else {
                            player.sendMessage(market.getHistory().getPricesInformation(item, db));
                        }
                    }
                }
            }.runTaskAsynchronously(market);
        }
        return true;
    }
}
