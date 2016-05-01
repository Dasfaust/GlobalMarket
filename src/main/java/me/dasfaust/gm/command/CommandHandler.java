package me.dasfaust.gm.command;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import me.dasfaust.gm.Core;
import me.dasfaust.gm.command.cmds.*;
import me.dasfaust.gm.config.Config;
import me.dasfaust.gm.config.Config.Defaults;
import me.dasfaust.gm.menus.Menus;
import me.dasfaust.gm.tools.GMLogger;
import me.dasfaust.gm.tools.LocaleHandler;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class CommandHandler implements Listener
{
	public static List<CommandContext> commands = new ArrayList<CommandContext>();
	
	public CommandHandler() { }
	
	public void init()
	{
		String rootValue = Core.instance.config().get(Defaults.COMMAND_ROOT_NAME);
		BukkitCommand root = new BukkitCommand(Core.instance.config().get(Defaults.COMMAND_ROOT_NAME))
		{
			
			@Override
			public boolean execute(CommandSender sender, String commandLabel, String[] args)
			{
                // Checking up on Bukkit
                if (!sender.hasPermission("globalmarket.use"))
                {
					GMLogger.debug(String.format("Permissions check on %s for globalmarket.use has returned false", sender.getName()));
                    sender.sendMessage(LocaleHandler.get().get("command_no_permission"));
                    return true;
                }
				GMLogger.debug(String.format("Permissions check on %s for globalmarket.use has returned true", sender.getName()));
                if (args.length > 0)
                {
                    String allArgs = StringUtils.join(args, " ") + " ";
					GMLogger.debug(String.format("Args: [%s]", allArgs));
                    contextLoop: for (CommandContext context : commands)
                    {
                        for (String cmd : context.command)
                        {
                            if (allArgs.toLowerCase().startsWith(cmd + " "))
                            {
                                String _args = allArgs.replace(cmd, "").trim();
                                String[] argArray = _args.length() == 0 ? new String[0] : _args.trim().split(" ");
                                handleCommand(sender, context, argArray);
                                break contextLoop;
                            }
                        }
                    }
                }
                else
                {
                    if (sender instanceof Player && Core.instance.config().get(Defaults.MARKET_COMMAND_OPENS_GUI))
                    {
                        Player player = (Player) sender;
                        Core.instance.handler().initViewer(player, Menus.MENU_LISTINGS);
                    }
                    else
                    {
                        // Help should be first
                        commands.get(0).process(sender, null);
                    }
                }
				return true;
			}
			
		};
		root.setDescription("GlobalMarket commands");
		root.setUsage(String.format("/%s help", rootValue));
		root.setAliases(new ArrayList<String>());
		try
		{
			Field comMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			comMap.setAccessible(true);
			CommandMap map = (CommandMap) comMap.get(Bukkit.getServer());
			map.register(rootValue, root);
		}
		catch(Exception e)
		{
			GMLogger.severe(e, "Couldn't register GlobalMarket command:");
		}
		
		commands.clear();
		commands.add(new HelpCommand());
        commands.add(new BrowseCommand());
		commands.add(new ReloadCommand());
		commands.add(new SendCommand());
		commands.add(new CreateListingCommand());
		commands.add(new CreateSeverListingCommand());
	}
	
	public void handleCommand(CommandSender sender, CommandContext context, String[] arguments)
	{
        for (String arg : arguments)
        {
            GMLogger.debug("HandleCommand: argument passed: " + arg);
        }
		if (context.requirePlayerInstance && !(sender instanceof Player))
		{
			sender.sendMessage(LocaleHandler.get().get("command_player_only"));
			return;
		}
		if (context.permission != null && !sender.hasPermission(context.permission))
		{
			GMLogger.debug(String.format("Permissions check on %s for %s has returned false", sender.getName(), context.permission));
			sender.sendMessage(LocaleHandler.get().get("command_no_permission"));
			return;
		}
		GMLogger.debug(String.format("Permissions check on %s for %s has returned true", sender.getName(), context.permission));
		GMLogger.debug(String.format("Context for %s called. Forcing arguments? %s Argument length: %s", context.command[0], context.forceArgumentCount, arguments.length));
		if ((arguments == null && context.forceArgumentCount) || (context.forceArgumentCount && arguments.length < context.arguments))
		{
			sender.sendMessage(LocaleHandler.get().get(context.help, Defaults.COMMAND_ROOT_NAME.value));
			return;
		}
		context.process(sender, arguments);
	}
}

