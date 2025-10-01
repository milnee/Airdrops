package me.millen.airdrops.builder;
/*
 *  created by millen on 23/05/2020
 */

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AirdropBuilder{

	private ItemStack airdrop;

	public AirdropBuilder(Material material){
		airdrop = new ItemStack(material);
	}

	public AirdropBuilder name(String name){
		ItemMeta meta = airdrop.getItemMeta();
		meta.setDisplayName(name);
		airdrop.setItemMeta(meta);
		return this;
	}

	public AirdropBuilder amount(int amount){
		airdrop.setAmount(amount);
		return this;
	}

	public AirdropBuilder lore(List<String> lores){
		ItemMeta meta = airdrop.getItemMeta();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		List<String> formatted = new ArrayList<>();
		for(String string : lores)
			formatted.add(ChatColor.translateAlternateColorCodes('&', string.replace("{date}", dtf.format(now))));
		meta.setLore(formatted);
		airdrop.setItemMeta(meta);

		return this;
	}

	public ItemStack build(){
		return airdrop;
	}
}
