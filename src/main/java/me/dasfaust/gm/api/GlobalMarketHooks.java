package me.dasfaust.gm.api;

import me.dasfaust.gm.config.Config;
import me.dasfaust.gm.menus.MenuBase;
import me.dasfaust.gm.storage.abs.DummyObject;
import me.dasfaust.gm.storage.abs.MarketObject;
import me.dasfaust.gm.storage.abs.StorageHandler;
import me.dasfaust.gm.tools.GMLogger;

public class GlobalMarketHooks
{
    public static void registerMenu(MenuBase<? extends MarketObject> menu)
    {
        try
        {
            if (menu.getObjectType() != null
                    && !menu.getObjectType().isAssignableFrom(DummyObject.class)
                    && !menu.getObjectType().equals(MarketObject.class))
            {
                StorageHandler.registerClass(menu.getObjectType());
            }
            Config.addFunctionConfig(menu.getClass());
        }
        catch(Exception e)
        {
            GMLogger.severe(e, String.format("API: can't register menu with class %s:", menu.getClass().getName()));
        }
    }
}
