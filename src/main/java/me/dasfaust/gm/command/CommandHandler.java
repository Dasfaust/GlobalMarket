package me.dasfaust.gm.command;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import me.dasfaust.gm.Core;
import me.dasfaust.gm.command.cmds.HelpCommand;
import me.dasfaust.gm.config.Config.Defaults;
import me.dasfaust.gm.menus.Menus;
import me.dasfaust.gm.tools.GMLogger;
import me.dasfaust.gm.tools.LocaleHandler;

import org.bukkit.Bukkit;
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
					Player player = (Player) sender;
					Core.instance.handler().initViewer(player, Menus.MENU_LISTINGS);
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
					String[] arguments = wholeCommand.replace(prefix, "").trim().split(" ");
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
						String[] arguments = wholeCommand.replace(prefix, "").trim().split(" ");
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
		if (arguments != null && context.forceArgumentCount && arguments.length < context.arguments)
		{
			sender.sendMessage(LocaleHandler.get().get(context.help));
			return;
		}
		context.process(sender, arguments);
	}
}

