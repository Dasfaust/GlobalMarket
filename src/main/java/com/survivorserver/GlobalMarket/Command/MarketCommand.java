package com.survivorserver.GlobalMarket.Command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import com.survivorserver.GlobalMarket.LocaleHandler;
import com.survivorserver.GlobalMarket.Market;

public class MarketCommand implements CommandExecutor {

	Market market;
	List<SubCommand> executors;
	LocaleHandler locale;
	
	public MarketCommand(Market market) {
		this.market = market;
		locale = market.getLocale();
		executors = new ArrayList<SubCommand>();
		registerSubCommand(new ListingsCommand(market, locale));
		registerSubCommand(new MailCommand(market, locale));
		registerSubCommand(new CreateCommand(market, locale));
		registerSubCommand(new SendCommand(market, locale));
		registerSubCommand(new PriceCheckCommand(market, locale));
		registerSubCommand(new MailboxCommand(market, locale));
		registerSubCommand(new StallCommand(market, locale));
		registerSubCommand(new HistoryCommand(market, locale));
		registerSubCommand(new CancelSearchCommand(market, locale));
		registerSubCommand(new ReloadCommand(market, locale));
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (cmd.getLabel().equalsIgnoreCase("market")) {
			if (args.length > 0 && !args[0].equalsIgnoreCase("help") && !args[0].equalsIgnoreCase("?")) {
				SubCommand sub = findExecutor(args[0]);
				if (sub != null) {
					if (sub.getPermissionNode() == null || sender.hasPermission(sub.getPermissionNode())) {
						if (!sub.allowConsoleSender()) {
							if (sender instanceof ConsoleCommandSender) {
								sender.sendMessage(locale.get("cmd.prefix") + locale.get("player_context_required"));
								return true;
							}
						}
						if (sub.onCommand(sender, args)) {
							return true;
						}
						sender.sendMessage(sub.getHelp());
						return true;
					} else {
						sender.sendMessage(ChatColor.YELLOW + locale.get("no_permission_for_this_command"));
						return true;
					}
				}
			}
			sender.sendMessage(locale.get("cmd.prefix") + locale.get("cmd.help_legend"));
			for (SubCommand sub : executors) {
				if (sender.hasPermission(sub.getPermissionNode())) {
					sender.sendMessage(sub.getHelp());
				}
			}
			return true;
		}
		return false;
	}
	
	public synchronized void registerSubCommand(SubCommand sub) {
		executors.add(sub);
	}
	
	public synchronized void unregisterSubCommand(Class<?> unreg) {
		Iterator<SubCommand> it = executors.iterator();
		while(it.hasNext()) {
			SubCommand cmd = it.next();
			if (cmd.getClass().equals(unreg)) {
				it.remove();
			}
		}
	}
	
	public SubCommand findExecutor(String cmd) {
		for (SubCommand sub : executors) {
			if (sub.getCommand().equalsIgnoreCase(cmd)) {
				return sub;
			}
			String[] aliases = sub.getAliases();
			if (aliases != null) {
				for (int i = 0; i < aliases.length; i++) {
					if (aliases[i].equalsIgnoreCase(cmd)) {
						return sub;
					}
				}
			}
		}
		return null;
	}
}
