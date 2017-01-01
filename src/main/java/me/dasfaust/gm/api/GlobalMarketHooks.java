package me.dasfaust.gm.api;

import me.dasfaust.gm.Core;
import me.dasfaust.gm.menus.MenuBase;
import me.dasfaust.gm.menus.Menus;
import me.dasfaust.gm.storage.abs.DummyObject;
import me.dasfaust.gm.storage.abs.MarketObject;
import me.dasfaust.gm.storage.abs.StorageHandler;
import me.dasfaust.gm.tools.GMLogger;
import me.dasfaust.gm.tools.LocaleHandler;

import javax.annotation.Nullable;

public class GlobalMarketHooks
{
    /**
     * Registers a Menu to the StorageHandler and its FunctionButtons to GM's config
     * @param menu the menu object
     * @param container if using a static container class, see 'me.dasfaust.gm.menus.Menus'
     */
    public static void registerMenu(MenuBase<? extends MarketObject> menu, @Nullable Class container)
    {
        try
        {
            if (menu.getObjectType() != null
                    && !menu.getObjectType().isAssignableFrom(DummyObject.class)
                    && !menu.getObjectType().equals(MarketObject.class))
            {
                StorageHandler.registerClass(menu.getObjectType());
            }
            Menus.menus.add(menu);
            Core.instance.config().addFunctionConfig(menu.getClass(), container);
        }
        catch(Exception e)
        {
            GMLogger.severe(e, String.format("API: can't register menu with class %s:", menu.getClass().getName()));
        }
    }

    /**
     * Registers a locale string to the current loaded locale. Useful for commands.
     * @param path
     * @param string
     */
    public static void registerLocale(String path, String string)
    {
        LocaleHandler.get().registerString(path, string);
    }
}
