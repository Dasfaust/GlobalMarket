package me.dasfaust.gm.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import me.dasfaust.gm.trade.WrappedStack;

import org.apache.commons.codec.binary.Base64;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.io.NbtBinarySerializer;
import com.google.gson.annotations.Expose;

import redis.clients.johm.Attribute;
import redis.clients.johm.Id;
import redis.clients.johm.Model;

@Model
public class SerializedStack
{
	public SerializedStack() {}
	
	public SerializedStack(WrappedStack stack) throws IOException
	{
		this.mat = stack.getMaterial().toString();
		this.damage = stack.getDamage();
		if (stack.nbt != null && stack.nbt.iterator().hasNext())
		{
			DataOutputStream output = null;
	        ByteArrayOutputStream stream = new ByteArrayOutputStream();
	        NbtBinarySerializer.DEFAULT.serialize(stack.nbt, output = new DataOutputStream(new GZIPOutputStream(stream)));
	        if (output != null)
	        {
	            output.close();
	        }
	        this.nbt = Base64.encodeBase64String(stream.toByteArray());
	        stream.close();
		}
	}
	
	@Expose
	@Id
	public long id;
	
	@Expose
	@Attribute
	public String mat;
	
	@Expose
	@Attribute
	public int damage = 0;
	
	@Expose
	@Attribute
	public String nbt;
	
	public long getId()
	{
		return id;
	}
	
	public WrappedStack buildStack() throws IOException
	{
		if (mat == null || Material.getMaterial(mat) == null)
		{
			throw new IOException("Material not found!");
		}
		ItemStack base = new ItemStack(Material.getMaterial(mat), 1, (short) damage);
		WrappedStack stack = new WrappedStack(base);
		if (nbt != null)
		{
			ByteArrayInputStream stream = new ByteArrayInputStream(Base64.decodeBase64(nbt));
			DataInputStream input = null;
			NbtCompound comp = NbtBinarySerializer.DEFAULT.deserializeCompound(input = new DataInputStream(new GZIPInputStream(stream)));
            input.close();
            stream.close();
            stack.setNbt(comp);
		}
		return stack;
	}
	
	@Override
    public int hashCode()
	{
		return mat.hashCode() + (nbt == null ? 0 : nbt.hashCode()) + damage;
	}
	
	@Override
	public boolean equals(Object ob)
	{
		if (ob == this)
		{
			return true;
		}
		if (ob instanceof SerializedStack)
		{
			SerializedStack st = (SerializedStack) ob;
			if (st.mat.equals(mat) && st.damage == damage && st.nbt.equals(nbt))
			{
				return true;
			}
		}
		return false;
	}
}
