package me.dasfaust.gm.command;

import org.bukkit.command.CommandSender;

public abstract class CommandContext
{
	public String[] command;
	public String permission;
	public int arguments;
	public String help;
	public boolean requirePlayerInstance;
	public boolean forceArgumentCount = true;
	
	public interface SubCommand {}
	
	public CommandContext(String[] command, String permission, int arguments, String help, boolean requirePlayerInstance)
	{
		this.command = command;
		this.permission = permission;
		this.arguments = arguments;
		this.help = help;
		this.requirePlayerInstance = requirePlayerInstance;
	}
	
	public CommandContext(String[] command, String permission, int arguments, String help, boolean requirePlayerInstance, boolean forceArgumentCount)
	{
		this.command = command;
		this.permission = permission;
		this.arguments = arguments;
		this.help = help;
		this.requirePlayerInstance = requirePlayerInstance;
		this.forceArgumentCount = forceArgumentCount;
	}
	
	public abstract void process(CommandSender sender, String[] arguments);
}
