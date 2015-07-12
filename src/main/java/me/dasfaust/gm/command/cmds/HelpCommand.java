package me.dasfaust.gm.command.cmds;

import org.bukkit.command.CommandSender;

import me.dasfaust.gm.Core;
import me.dasfaust.gm.command.CommandContext;
import me.dasfaust.gm.command.CommandHandler;
import me.dasfaust.gm.config.Config.Defaults;
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
		int size = CommandHandler.commands.size() + 1;
		String[] helpText = new String[size];
		helpText[0] = LocaleHandler.get().get("command_helptext_header");
		for (int i = 1; i < size; i++)
		{
			helpText[i] = String.format("/%s %s", Core.instance.config().get(Defaults.COMMAND_ROOT_NAME), LocaleHandler.get().get(CommandHandler.commands.get(i - 1).help));
		}
		sender.sendMessage(helpText);
	}
}
