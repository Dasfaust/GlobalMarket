package me.dasfaust.gm.command.cmds;

import me.dasfaust.gm.Core;
import me.dasfaust.gm.command.CommandContext;
import me.dasfaust.gm.menus.MenuBase;
import me.dasfaust.gm.menus.Menus;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BrowseCommand extends CommandContext
{
    public BrowseCommand()
    {
        super(
                new String[] {
                        "browse",
                        "listings"
                },
                "globalmarket.command.browse",
                0,
                "command_helptext_browse",
                false,
                false
        );
    }

    @Override
    public void process(CommandSender sender, String[] arguments)
    {
        if (sender instanceof Player)
        {
            MenuBase<?> menu = Menus.MENU_LISTINGS;
            if (arguments.length > 0)
            {
                if (arguments[0].equalsIgnoreCase("stock"))
                {
                    menu = Menus.MENU_STOCK;
                }
                else if (arguments[0].equalsIgnoreCase("serverlistings"))
                {
                    menu = Menus.MENU_SERVER_LISTINGS;
                }
            }
            Player player = (Player) sender;
            Core.instance.handler().initViewer(player, menu);
        }
        else
        {
            if (arguments.length == 0)
            {
                sender.sendMessage(ChatColor.RED + "If calling this from a console, please specify the player.");
                return;
            }
            if (arguments.length == 1)
            {
                UUID uuid = Core.instance.storage().findPlayer(arguments[0]);
                if (uuid == null)
                {
                    sender.sendMessage(ChatColor.RED + String.format("No player by the name of '%s' found.", arguments[0]));
                    return;
                }
                Player player = Core.instance.getServer().getPlayer(uuid);
                if (player == null || !player.isOnline())
                {
                    sender.sendMessage(ChatColor.RED + String.format("Player '%s' is not online.", arguments[0]));
                    return;
                }
                Core.instance.handler().initViewer(player, Menus.MENU_LISTINGS);
            }
            else if (arguments.length == 2)
            {
                MenuBase<?> menu = Menus.MENU_LISTINGS;
                if (arguments[0].equalsIgnoreCase("stock"))
                {
                    menu = Menus.MENU_STOCK;
                }
                else if (arguments[0].equalsIgnoreCase("serverlistings"))
                {
                    menu = Menus.MENU_SERVER_LISTINGS;
                }
                UUID uuid = Core.instance.storage().findPlayer(arguments[1]);
                if (uuid == null)
                {
                    sender.sendMessage(ChatColor.RED + String.format("No player by the name of '%s' found.", arguments[1]));
                    return;
                }
                Player player = Core.instance.getServer().getPlayer(uuid);
                if (player == null || !player.isOnline())
                {
                    sender.sendMessage(ChatColor.RED + String.format("Player '%s' is not online.", arguments[1]));
                    return;
                }
                Core.instance.handler().initViewer(player, menu);
            }
        }
    }
}
