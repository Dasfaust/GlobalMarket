package me.dasfaust.gm.trade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import me.dasfaust.gm.Core;
import me.dasfaust.gm.config.Config;
import me.dasfaust.gm.tools.GMLogger;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;

public class WrappedStack
{
	public static UUID TAG = UUID.randomUUID();
	
	public ItemStack base;
	public NbtCompound nbt;

	public WrappedStack(ItemStack base)
	{
		if(base == null)
		{
			GMLogger.debug("WrappedStack was passed an empty ItemStack. Abort!");
			base = new ItemStack(Material.STONE);
		}
		if (Material.getMaterial("BLACK_SHULKER_BOX") != null)
        {
            this.base = MinecraftReflection.getBukkitItemStack(MinecraftReflection.getMinecraftItemStack(base));
        }
        else
        {
            this.base = MinecraftReflection.getBukkitItemStack(base);
        }
		try
		{
			nbt = (NbtCompound) NbtFactory.fromItemTag(this.base);
		}
		catch(Exception e)
		{
			if (!base.hasItemMeta())
			{
				//throw new IllegalArgumentException("Cannot wrap type " + base.getType().toString());
				GMLogger.debug(String.format("Material %s (%s) cannot have ItemMeta. NBT is null", base.getType().toString(), base.getType().getId()));
				nbt = null;
			}
			else
			{
				ItemMeta meta = base.getItemMeta();
				if (meta == null)
				{
					meta = Bukkit.getServer().getItemFactory().getItemMeta(base.getType());
				}
				base.setItemMeta(meta);
				this.base = MinecraftReflection.getBukkitItemStack(base);
				nbt = (NbtCompound) NbtFactory.fromItemTag(this.base);
			}
		}
		
	}
	
	/**
	 * Get the item's amount
	 * @return
	 */
	public int getAmount()
	{
		return base.getAmount();
	}
	
	/**
	 * Get the item's damage
	 * @return
	 */
	public int getDamage()
	{
		return base.getDurability();
	}
	
	/**
	 * Set the item's amount
	 * @param amt
	 * @return
	 */
	public WrappedStack setAmount(int amt)
	{
		base.setAmount(amt);
		return this;
	}
	
	/**
	 * Set the item's damage
	 * @param dmg
	 * @return
	 */
	public WrappedStack setDamage(int dmg)
	{
		base.setDurability((short) dmg);
		return this;
	}
	
	/**
	 * Get the item's material
	 * @return
	 */
	public Material getMaterial()
	{
		return base.getType();
	}
	
	/**
	 * Get the item's material id
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public int getMaterialId()
	{
		return base.getType().getId();
	}
	
	/**
	 * Safely clone this item
	 */
	public WrappedStack clone()
	{
		WrappedStack clone = new WrappedStack(new ItemStack(base.getType(), base.getAmount(), base.getDurability()));
		if (nbt != null)
		{
			clone.setNbt((NbtCompound) nbt.deepClone());
		}
		return clone;
	}
	
	/**
	 * Set the item's NBT
	 * @param comp
	 */
	public WrappedStack setNbt(NbtCompound comp)
	{
		NbtFactory.setItemTag(base, comp);
		nbt = comp;
		return this;
	}
	
	/**
	 * Get the item's NBT tag
	 * @return
	 */
	public NbtCompound getNbt()
	{
		return nbt;
	}
	
	/**
	 * Does the item have a display name?
	 * @return
	 */
	public boolean hasDisplayName()
	{
		return getDisplayTag().containsKey("Name");
	}
	
	/**
	 * Get the display name. Check hasDisplayName() first!
	 * @return
	 */
	public String getDisplayName()
	{
		return getDisplayTag().getString("Name");
	}
	
	/**
	 * Set the item's display name
	 * @param name
	 * @return
	 */
	public WrappedStack setDisplayName(String name)
	{
		NbtCompound disp = getDisplayTag();
		disp.remove("Name");
		disp.put("Name", name);
		return this;
	}
	
	/**
	 * Does the item have lore?
	 * @return
	 */
	public boolean hasLore()
	{
		return getDisplayTag().containsKey("Lore");
	}
	
	/**
	 * Get the item's lore. Check hasLore() first!
	 * @return
	 */
	public List<String> getLore()
	{
		List<String> lore = new ArrayList<String>();
        for (Object ob : nbt.getCompound("display").getList("Lore"))
        {
            lore.add(ob.toString());
        }
        return lore;
	}
	
	/**
	 * Set the item's lore
	 * @param lore
	 * @return
	 */
	public WrappedStack setLore(List<String> lore)
	{
		NbtCompound disp = getDisplayTag();
		disp.remove("Lore");
		disp.put(NbtFactory.ofList("Lore", lore));
		return this;
	}
	
	/**
	 * Append new strings to the item's lore
	 * @param lore
	 * @return
	 */
	public WrappedStack addLoreLast(List<String> lore)
	{
		if (hasLore())
		{
			List<String> old = getLore();
			old.addAll(lore);
			setLore(old);
		}
		else
		{
			setLore(lore);
		}
		return this;
	}
	
	/**
	 * Prepend new strings to the item's lore
	 * @param lore
	 * @return
	 */
	public WrappedStack addLoreBefore(List<String> lore)
	{
		if (hasLore())
		{
			lore.addAll(getLore());
			setLore(lore);
		}
		else
		{
			setLore(lore);
		}
		return this;
	}
	
	/**
	 * Add enchantment glow
	 * @return
	 */
	public WrappedStack makeGlow()
	{
		if (!nbt.containsKey("ench"))
		{
			nbt.put(NbtFactory.ofList("ench"));
		}
		return this;
	}
	
	/**
	 * Clear the enchantment glow. Removes all enchants!
	 * @return
	 */
	public WrappedStack clearGlow()
	{
		nbt.remove("ench");
		return this;
	}
	
	/**
	 * Add an empty compound for tracking purposes
	 */
	public WrappedStack tag()
	{
		nbt.put(NbtFactory.of("market", 1));
		if (Core.instance.config().get(Config.Defaults.ENABLE_DEBUG))
		{
			addLoreLast(Arrays.asList(new String[]{"Has tag"}));
		}
		return this;
	}
	
	/**
	 * Check for the compound added by tag()
	 * @return
	 */
	public boolean hasTag()
	{
		return nbt.containsKey("market");
	}
	
	/**
	 * Gets the item's display compound. Creates one if not found
	 * @return
	 */
	public NbtCompound getDisplayTag()
	{
		if (!nbt.containsKey("display"))
		{
			nbt.put(NbtFactory.ofCompound("display"));
		}
		return nbt.getCompound("display");
	}
	
	public ItemStack bukkit()
	{
		return base;
	}
	
	/**
	 * Checks for an empty NBT tag and removes it, if found
	 * @return
	 */
	public WrappedStack checkNbt()
	{
		if (nbt != null && !nbt.iterator().hasNext())
		{
			setNbt(null);
		}
		return this;
	}
	
	@Override
	public boolean equals(Object ob)
	{
		if (ob == this)
		{
			return true;
		}
		if (ob instanceof WrappedStack)
		{
			WrappedStack stack = (WrappedStack) ob;
			if (stack.getMaterial().equals(getMaterial())
					&& stack.getDamage() == getDamage())
			{
				if (stack.nbt != null)
				{
					return stack.nbt.equals(nbt);
				}
				return true;
			}
		}
		return false;
	}
}
