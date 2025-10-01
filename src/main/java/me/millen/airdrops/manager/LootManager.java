package me.millen.airdrops.manager;
/*
 *  created by millen on 23/05/2020
 */

import me.millen.airdrops.Base;
import me.millen.airdrops.injection.Injector;
import me.millen.airdrops.interfaces.Manager;
import me.millen.airdrops.utils.Serializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

public class LootManager implements Manager{

	private File file = new File(Base.get().getDataFolder(), "loot.yml");
	private YamlConfiguration setter = YamlConfiguration.loadConfiguration(file);
	private Inventory loot;

	public void setup(){
		loot = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Airdrops Loot");

		if(!file.exists()){
			saveOnly();
			return;
		}

		load();
	}

	public Inventory getLoot(){
		return loot;
	}

	public void saveOnly(){
		try{
			setter.save(file);
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public void save(){
		if(!file.exists())
			saveOnly();

		if(loot.getSize() <= 0)
			return;

		setter.set("loot", null);
		for(int index = 0; index < 54; index++){
			if(loot.getItem(index) == null || loot.getItem(index).getType().equals(Material.AIR))
				continue;

			setter.set("loot." +index +".item", serializeItemStack(loot.getItem(index)));
			Map<String, String> keys = Injector.getKeys(loot.getItem(index));
			if(Objects.nonNull(keys)){
				if(keys.isEmpty())
					continue;

				setter.set("loot." +index +".tags", Serializer.serialize(keys));
			}
		}

		saveOnly();
	}

	public void load(){
		if(setter.get("loot") == null)
			return;

		for(String slot : setter.getConfigurationSection("loot").getKeys(false)){
			ItemStack stack = deserializeItemStack(setter.getString("loot." +slot +".item"));

			if(setter.isSet("loot." +slot +".tags")){
				Map<String, String> keys = Serializer.deserialize(setter.getString("loot." +slot +".tags"));

				Injector injector = new Injector(stack);
				for(String string : keys.keySet()){
					if(!injector.hasKey(string)){
						injector.set(string, keys.get(string));
					}
				}

				stack = injector.getStack();
			}

			loot.setItem(Integer.parseInt(slot), stack);
		}
	}

	public FileConfiguration getSetter(){
		return setter;
	}

	public void set(ItemStack stack){
		setter.set("temp", stack);
		saveOnly();
	}

	/**
	 * ItemStack List to Base64
	 */
	public static ItemStack deserializeItemStack(String data){
		ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(data, 32).toByteArray());
		DataInputStream dataInputStream = new DataInputStream(inputStream);

		ItemStack itemStack = null;
		try {
			Class<?> nbtTagCompoundClass = getNMSClass("NBTTagCompound");
			Class<?> nmsItemStackClass = getNMSClass("ItemStack");
			Object nbtTagCompound = getNMSClass("NBTCompressedStreamTools").getMethod("a", DataInputStream.class).invoke(null, dataInputStream);
			//Object nbtTagCompound = getNMSClass("NBTCompressedStreamTools").getMethod("a", DataInputStream.class).invoke(null, inputStream);
			Object craftItemStack = nmsItemStackClass.getMethod("createStack", nbtTagCompoundClass).invoke(null, nbtTagCompound);
			itemStack = (ItemStack) getOBClass("inventory.CraftItemStack").getMethod("asBukkitCopy", nmsItemStackClass).invoke(null, craftItemStack);
		} catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}

		return itemStack;
	}

	public static String serializeItemStack(ItemStack item) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		DataOutputStream dataOutput = new DataOutputStream(outputStream);

		try {
			Class<?> nbtTagCompoundClass = getNMSClass("NBTTagCompound");
			Constructor<?> nbtTagCompoundConstructor = nbtTagCompoundClass.getConstructor();
			Object nbtTagCompound = nbtTagCompoundConstructor.newInstance();
			Object nmsItemStack = getOBClass("inventory.CraftItemStack").getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
			getNMSClass("ItemStack").getMethod("save", nbtTagCompoundClass).invoke(nmsItemStack, nbtTagCompound);
			getNMSClass("NBTCompressedStreamTools").getMethod("a", nbtTagCompoundClass, DataOutput.class).invoke(null, nbtTagCompound, (DataOutput)dataOutput);

		} catch (SecurityException | NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}

		//return BaseEncoding.base64().encode(outputStream.toByteArray());
		return new BigInteger(1, outputStream.toByteArray()).toString(32);
	}

	private static Class<?> getNMSClass(String name) {
		String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		try {
			return Class.forName("net.minecraft.server." + version + "." + name);
		} catch (ClassNotFoundException var3) {
			var3.printStackTrace();
			return null;
		}
	}

	private static Class<?> getOBClass(String name) {
		String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
		try {
			return Class.forName("org.bukkit.craftbukkit." + version + "." + name);
		} catch (ClassNotFoundException var3) {
			var3.printStackTrace();
			return null;
		}
	}
}