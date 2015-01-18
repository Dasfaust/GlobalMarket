package com.survivorserver.GlobalMarket.Command;

import com.survivorserver.GlobalMarket.Interface.IMenu;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.survivorserver.GlobalMarket.InterfaceViewer;
import com.survivorserver.GlobalMarket.LocaleHandler;
import com.survivorserver.GlobalMarket.Market;

public class MailCommand extends SubCommand {

    public MailCommand(Market market, LocaleHandler locale) {
        super(market, locale);
    }

    @Override
    public String getCommand() {
        return "mail";
    }

    @Override
    public String[] getAliases() {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "globalmarket.quickmail";
    }

    @Override
    public String getHelp() {
        return locale.get("cmd.prefix") + locale.get("cmd.mail_syntax") + " " + locale.get("cmd.mail_descr");
    }

    @Override
    public boolean allowConsoleSender() {
        return false;
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (args.length == 2 && (sender.hasPermission("globalmarket.admin") || sender.hasPermission("globalmarket.quickmail.others"))) {
            String viewAs = args[1];
            OfflinePlayer p = market.getServer().getOfflinePlayer(viewAs);
            if (p == null || !p.hasPlayedBefore()) {
                sender.sendMessage(ChatColor.RED + locale.get("player_not_found", viewAs));
                return true;
            }
            IMenu mInterface = market.getInterfaceHandler().getInterface("Mail");
            Inventory inv = market.getServer().createInventory(player, mInterface.getSize(), mInterface.getTitle() + " (" + viewAs + ")");
            InterfaceViewer viewer = new InterfaceViewer(viewAs, player.getName(), inv, mInterface, player.getWorld().getName());
            market.getInterfaceHandler().addViewer(viewer);
            market.getInterfaceHandler().refreshViewer(viewer, "Mail");
            market.getInterfaceHandler().openGui(viewer);
        } else {
            if (player.getGameMode() == GameMode.CREATIVE && !market.allowCreative(player)) {
                player.sendMessage(ChatColor.RED + locale.get("not_allowed_while_in_creative"));
                return true;
            }
            market.getInterfaceHandler().openInterface(player, null, "Mail");
        }
        return true;
    }
}
