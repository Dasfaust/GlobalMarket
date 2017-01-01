package me.dasfaust.gm.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FilenameUtils;
import org.bukkit.ChatColor;

import com.google.gson.Gson;

import me.dasfaust.gm.Core;
import me.dasfaust.gm.config.Config.Defaults;

public class LocaleHandler
{
	private static Gson gson = new Gson();
	private static Map<String, Locale> locales = new HashMap<String, Locale>();
	
	public static void init()
	{
		JarFile jar = null;
        try
        {
            jar = new JarFile(new File(Core.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()));
        }
        catch (Exception e)
        {
            GMLogger.severe(e, "Could not load locale files from the plugin JAR. Are they present?");
            return;
        }
        Enumeration<JarEntry> entries = jar.entries();
        while(entries.hasMoreElements())
        {
            JarEntry entry = entries.nextElement();
            if (entry.getName().contains("locale") && entry.getName().contains(".json"))
            {
                try
                {
                    String json = "";
                    BufferedReader reader = new BufferedReader(new InputStreamReader(jar.getInputStream(entry), "UTF-8"));
                    String s;
                    while((s = reader.readLine()) != null)
                    {
                        json += s;
                    }
                    reader.close();
                    String id = FilenameUtils.getBaseName(entry.getName());
                    locales.put(id, gson.fromJson(json, Locale.class));
                    GMLogger.debug(String.format("Locale file loaded as '%s': %s", id, entry.getName()));
                }
                catch(Exception e)
                {
                    GMLogger.severe(e, String.format("Could not load locale file %s. Check your syntax", entry.getName()));
                }
            }
        }
        try
        {
            jar.close();
        }
        catch (IOException ignored) {}
	}
	
	public static Locale get()
    {
        return locales.get(Core.instance.config().get(Defaults.LOCALE_FILENAME));
    }
	
	public static class Locale
    {
        private Map<String, String> locale = new HashMap<String, String>();
        
        public String get(String path)
        {
            return locale.containsKey(path) ? ChatColor.translateAlternateColorCodes('&', locale.get(path)) : path;
        }
        
        public String get(String path, Object ... args)
        {
            return locale.containsKey(path) ? ChatColor.translateAlternateColorCodes('&', String.format(locale.get(path), args)) : path;
        }

        public void registerString(String path, String string)
        {
            if (!locale.containsKey(path))
            {
                locale.put(path, string);
            }
        }
    }
}
