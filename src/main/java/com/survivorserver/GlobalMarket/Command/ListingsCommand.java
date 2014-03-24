package com.survivorserver.GlobalMarket.Command;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.survivorserver.GlobalMarket.LocaleHandler;
import com.survivorserver.GlobalMarket.Market;

public class ListingsCommand extends SubCommand {

    public ListingsCommand(Market market, LocaleHandler locale) {
        super(market, locale);
    }

    @Override
    public String getCommand() {
        return "listings";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"listing", "buy", "auctions"};
    }

    @Override
    public String getPermissionNode() {
        return "globalmarket.quicklist";
    }

    @Override
    public String getHelp() {
        return locale.get("cmd.prefix") + locale.get("cmd.listings_syntax") + " " + locale.get("cmd.listings_descr");
    }

    @Override
    public boolean allowConsoleSender() {
        return false;
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String search = null;
        if (args.length >= 2) {
            search = args[1];
            if (args.length > 2) {
                for (int i = 2 ; i < args.length ; i++) {
                    search = search + " " + args[i];
                }
            }
        }
        if (player.getGameMode() == GameMode.CREATIVE && !market.allowCreative(player)) {
            player.sendMessage(ChatColor.RED + locale.get("not_allowed_while_in_creative"));
            return true;
        }
        market.getInterfaceHandler().openInterface(player, search, "Listings");
        return true;
    }
}
