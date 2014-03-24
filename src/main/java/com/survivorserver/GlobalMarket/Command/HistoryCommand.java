package com.survivorserver.GlobalMarket.Command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.survivorserver.GlobalMarket.LocaleHandler;
import com.survivorserver.GlobalMarket.Market;
import com.survivorserver.GlobalMarket.SQL.Database;

public class HistoryCommand extends SubCommand {

    public HistoryCommand(Market market, LocaleHandler locale) {
        super(market, locale);
    }

    @Override
    public String getCommand() {
        return "history";
    }

    @Override
    public String[] getAliases() {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "globalmarket.history";
    }

    @Override
    public String getHelp() {
        return locale.get("cmd.prefix") + locale.get("cmd.history_syntax") + " " + locale.get("cmd.history_descr");
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
        new BukkitRunnable() {
            public void run() {
                Database db = market.getConfigHandler().createConnection();
                db.connect();
                player.sendMessage(market.getHistory().buildHistory(player.getName(), 15, db));
                db.close();
            }
        }.runTaskAsynchronously(market);
        return true;
    }
}
