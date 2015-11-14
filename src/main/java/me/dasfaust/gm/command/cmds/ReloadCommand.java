package me.dasfaust.gm.command.cmds;

import me.dasfaust.gm.Core;
import me.dasfaust.gm.command.CommandContext;
import me.dasfaust.gm.tools.GMLogger;
import me.dasfaust.gm.tools.LocaleHandler;
import org.bukkit.command.CommandSender;

import java.io.IOException;

public class ReloadCommand extends CommandContext
{
    public ReloadCommand()
    {
        super(
                new String[] {
                        "reload"
                },
                "globalmarket.command.reload",
                0,
                "command_helptext_reload",
                false,
                false
        );
    }

    @Override
    public void process(CommandSender sender, String[] arguments)
    {
        try
        {
            Core.instance.config().load();
            sender.sendMessage(LocaleHandler.get().get("command_reload_complete"));
        }
        catch(Exception e)
        {
            GMLogger.severe(e, "Caught exception while reloading config:");
            sender.sendMessage(LocaleHandler.get().get("command_reload_failed"));
        }
    }
}
