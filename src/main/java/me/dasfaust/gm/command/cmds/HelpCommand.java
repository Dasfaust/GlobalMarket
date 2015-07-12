package me.dasfaust.gm.command.cmds;

import java.util.ArrayList;

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
		ArrayList<String> cmds = new ArrayList<String>();
		cmds.add(LocaleHandler.get().get("command_helptext_header"));
		for (CommandContext context : CommandHandler.commands)
		{
			if (context.permission != null && !sender.hasPermission(context.permission))
			{
				continue;
			}
			cmds.add(LocaleHandler.get().get(context.help, Core.instance.config().get(Defaults.COMMAND_ROOT_NAME)));
		}
		sender.sendMessage(cmds.toArray(new String[0]));
	}
}
