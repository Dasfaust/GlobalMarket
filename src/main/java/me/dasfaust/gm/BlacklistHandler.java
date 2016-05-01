package me.dasfaust.gm;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import me.dasfaust.gm.tools.GMLogger;
import me.dasfaust.gm.trade.WrappedStack;

import org.bukkit.Material;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

public class BlacklistHandler
{
	public static boolean cauldron = false;
	public static Gson gson;
	public static File blacklistFile;
	public static List<String> blacklist;
	
	public static void init()
	{
		gson = new Gson();
		
		if (Core.isCauldron)
		{
			GMLogger.debug("We're running on Cauldron!");
			cauldron = true;
		}
		
		blacklist = new ArrayList<String>();
		blacklist.add(Material.APPLE.toString().toLowerCase() + ":-1");
		blacklist.add("minecraft:cobblestone:-1");
		blacklist.add("oredict:ingotSteel");
		
		reload();
	}

	public static void reload()
	{
		blacklistFile = new File(Core.instance.getDataFolder().getAbsolutePath() + File.separator + "blacklist.json");
		if (!blacklistFile.exists())
		{
			try
			{
				blacklistFile.createNewFile();
			}
			catch (IOException e)
			{
				GMLogger.severe(e, "Can't create blacklist.json:");
				return;
			}
			try
			{
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(blacklistFile), "UTF-8"));
				out.write(gson.toJson(blacklist));
				out.close();
			}
			catch (IOException e)
			{
				GMLogger.severe(e, "Can't write to blacklist.json:");
				return;
			}
		}
		try
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(blacklistFile), "UTF-8"));
			String all = "";
			String cur;
			while((cur = in.readLine()) != null)
			{
				all += cur;
			}
			in.close();
			blacklist = Lists.newArrayList(gson.fromJson(all.toLowerCase(), String[].class));
			GMLogger.debug(String.format("Blacklist has %s entries", blacklist.size()));
		}
		catch (Exception e)
		{
			GMLogger.severe(e, "Can't load blacklist.json:");
			return;
		}
	}
	
	public static boolean check(WrappedStack stack)
	{
		GMLogger.debug(String.format("Checking item [%s:%s] against blacklist...", stack.getMaterial().toString(), stack.getDamage()));
		if (cauldron)
		{
			Object nms = MinecraftReflection.getMinecraftItemStack(stack.bukkit());
			net.minecraft.item.ItemStack is = (net.minecraft.item.ItemStack) nms;
			cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier uid =
			cpw.mods.fml.common.registry.GameRegistry.findUniqueIdentifierFor(is.getItem());
			String id = String.format("%s:%s:%s", uid.modId, uid.name, stack.getDamage());
			String idAny = String.format("%s:%s:%s", uid.modId, uid.name, -1);
			GMLogger.debug(id);
			if (blacklist.contains(id) || blacklist.contains(idAny))
			{
				GMLogger.debug("Blacklist entry found via Forge identifier");
				return true;
			}
			int[] ids = net.minecraftforge.oredict.OreDictionary.getOreIDs(is);
			for (int o : ids)
			{
				String idOreDict = String.format("oredict:%s", net.minecraftforge.oredict.OreDictionary.getOreName(o));
				if (blacklist.contains(idOreDict))
				{
					return true;
				}
			}
		}
		String id = String.format("%s:%s", stack.getMaterial().toString().toLowerCase(), stack.getDamage());
		String idAny = String.format("%s:%s", stack.getMaterial().toString().toLowerCase(), -1);
		GMLogger.debug(id);
		return blacklist.contains(id) || blacklist.contains(idAny);
	}
}
