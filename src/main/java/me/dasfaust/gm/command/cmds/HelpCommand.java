package me.dasfaust.gm.command.cmds;

import org.bukkit.command.CommandSender;

import me.dasfaust.gm.command.CommandContext;
import me.dasfaust.gm.command.CommandHandler;
import me.dasfaust.gm.tools.LocaleHandler;

public class HelpCommand extends CommandContext
{

	public HelpCommand()
	{
		super(
			new String[] {
				"help",
				"?"
			},
			null,
			0,
			"command_helptext_help",
			false,
			false
		);
	}

	@Override
	public void process(CommandSender sender, String[] arguments)
	{
		int size = CommandHandler.commands.size();
		String[] helpText = new String[size];
		for (int i = 0; i < size; i++)
		{
			helpText[i] = LocaleHandler.get().get(CommandHandler.commands.get(i).help);
		}
		sender.sendMessage(helpText);
	}
}
