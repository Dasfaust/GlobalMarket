package me.dasfaust.gm.command.cmds;

import me.dasfaust.gm.Core;
import me.dasfaust.gm.StorageHelper;
import me.dasfaust.gm.command.CommandContext;
import me.dasfaust.gm.config.Config;
import me.dasfaust.gm.storage.JsonStorage;
import me.dasfaust.gm.storage.RedisStorage;
import me.dasfaust.gm.storage.abs.StorageHandler;
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
            String oldPersistence = Core.instance.config().get(Config.Defaults.PERSISTENCE_METHOD);
            Core.instance.config().load();
            if (!oldPersistence.equals(Core.instance.config().get(Config.Defaults.PERSISTENCE_METHOD)))
            {
                StorageHandler storage;
                if (Core.instance.config().get(Config.Defaults.PERSISTENCE_METHOD).equalsIgnoreCase("redis"))
                {
                    storage = new RedisStorage(
                            Core.instance.config().get(Config.Defaults.PERSISTENCE_METHOD_REDIS_ADDRESS),
                            Core.instance.config().get(Config.Defaults.PERSISTENCE_METHOD_REDIS_PASSWORD),
                            Core.instance.config().get(Config.Defaults.PERSISTENCE_METHOD_REDIS_PORT),
                            Core.instance.config().get(Config.Defaults.PERSISTENCE_METHOD_REDIS_POOLSIZE)
                    );
                }
                else
                {
                    storage = new JsonStorage();
                }
                Core.instance.setStorage(storage);
            }
            GMLogger.setDebug(Core.instance.config().get(Config.Defaults.ENABLE_DEBUG));
            sender.sendMessage(LocaleHandler.get().get("command_reload_complete"));
        }
        catch(Exception e)
        {
            GMLogger.severe(e, "Caught exception while reloading config:");
            sender.sendMessage(LocaleHandler.get().get("command_reload_failed"));
        }
    }
}
