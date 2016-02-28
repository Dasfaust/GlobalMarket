package me.dasfaust.gm.command;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.dasfaust.gm.Core;
import me.dasfaust.gm.command.cmds.CreateSeverListingCommand;
import me.dasfaust.gm.command.cmds.HelpCommand;
import me.dasfaust.gm.command.cmds.ReloadCommand;
import me.dasfaust.gm.command.cmds.SendCommand;
import me.dasfaust.gm.config.Config.Defaults;
import me.dasfaust.gm.menus.Menus;
import me.dasfaust.gm.tools.GMLogger;
import me.dasfaust.gm.tools.LocaleHandler;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;


public class CommandHandler implements Listener
{
	public static List<CommandContext> commands = new ArrayList<CommandContext>();
	
	public CommandHandler()
	{
		Core.instance.getServer().getPluginManager().registerEvents(this, Core.instance);
	}
	
	public void init()
	{
		String rootValue = Core.instance.config().get(Defaults.COMMAND_ROOT_NAME);
		BukkitCommand root = new BukkitCommand(Core.instance.config().get(Defaults.COMMAND_ROOT_NAME))
		{
			
			@Override
			public boolean execute(CommandSender sender, String commandLabel, String[] args)
			{
				if (sender instanceof Player)
				{
					if (args.length == 1 && sender.hasPermission("globalmarket.viewother"))
					{
						String player = args[0];
						UUID uuid = Core.instance.storage().findPlayer(player);
						if (uuid == null)
						{
							sender.sendMessage(ChatColor.RED + String.format("No player by the name of %s found.", player));
							return true;
						}
						Core.instance.handler().initViewer((Player) sender, uuid, Menus.MENU_LISTINGS);
					}
					else
					{
						Player player = (Player) sender;
						Core.instance.handler().initViewer(player, Menus.MENU_LISTINGS);
					}
				}
				else
				{
					// Help should be first
					commands.get(0).process(sender, null);
				}
				return true;
			}
			
		};
		root.setDescription("GlobalMarket commands");
		root.setUsage(String.format("/%s help", rootValue));
		root.setPermission("globalmarket.use");
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
		commands.add(new ReloadCommand());
		commands.add(new SendCommand());
		commands.add(new CreateSeverListingCommand());
	}
	
	@EventHandler
	public void onServer(ServerCommandEvent event)
	{
		String message = event.getCommand();
		String wholeCommand = message.replace("/", "");
		for (CommandContext context : commands)
		{
			for (String cmd : context.command)
			{
				String prefix = Core.instance.config().get(Defaults.COMMAND_ROOT_NAME) + " " + cmd;
				if (wholeCommand.startsWith(prefix))
				{
					String _cmd = wholeCommand.replace(prefix, "").replace(cmd, "").trim();
					GMLogger.debug("Command pre-process server: " + _cmd + " Length: " + _cmd.length());
					String[] arguments = _cmd.length() == 0 ? new String[0] : _cmd.split(" ");
					handleCommand(event.getSender(), context, arguments);
					break;
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayer(PlayerCommandPreprocessEvent event)
	{
		String message = event.getMessage();
		if (message.startsWith("/"))
		{
			String wholeCommand = message.replace("/", "");
			for (CommandContext context : commands)
			{
				for (String cmd : context.command)
				{
					String prefix = Core.instance.config().get(Defaults.COMMAND_ROOT_NAME) + " " + cmd;
					if (wholeCommand.startsWith(prefix))
					{
						event.setCancelled(true);
						String _cmd = wholeCommand.replace(prefix, "").replace(cmd, "").trim();
						GMLogger.debug("Command pre-process: " + _cmd + " Length: " + _cmd.length());
						String[] arguments = _cmd.length() == 0 ? new String[0] : _cmd.split(" ");
						handleCommand(event.getPlayer(), context, arguments);
						break;
					}
				}
			}
		}
	}
	
	public void handleCommand(CommandSender sender, CommandContext context, String[] arguments)
	{
		if (context.requirePlayerInstance && !(sender instanceof Player))
		{
			sender.sendMessage(LocaleHandler.get().get("command_player_only"));
			return;
		}
		if (context.permission != null && !sender.hasPermission(context.permission))
		{
			sender.sendMessage(LocaleHandler.get().get("command_no_permission"));
			return;
		}
		GMLogger.debug(String.format("Context for %s called. Forcing arguments? %s Argument length: %s", context.command[0], context.forceArgumentCount, arguments.length));
		if ((arguments == null && context.forceArgumentCount) || (context.forceArgumentCount && arguments.length < context.arguments))
		{
			sender.sendMessage(LocaleHandler.get().get(context.help, Defaults.COMMAND_ROOT_NAME.value));
			return;
		}
		context.process(sender, arguments);
	}
}

