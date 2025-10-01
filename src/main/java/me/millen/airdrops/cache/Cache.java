package me.millen.airdrops.cache;
/*
 *  created by millen on 23/05/2020
 */

import me.millen.airdrops.Base;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Cache {

	private Map<String, String> messages = new ConcurrentHashMap<>();

	public void setup() {
		for (String key : Base.get().getConfig().getKeys(false))
			if (Base.get().getConfig().get(key) instanceof String)
				messages.put(key, Base.get().getConfig().getString(key));
	}

	public void reload() {
		messages.clear();
		setup();
	}

	public String PERMISSION_DENIED(){
		return getString("permission-denied");
	}

	public String DEPLOYING(){ return getString("deploying"); }

	public String DEPLOYED(){
		return getString("deployed");
	}

	public String HEIGHT_AIR(){ return getString("height-air"); }

	public String AIRDROP_NAME(){ return getString("airdrop-name"); }

	public Integer SLOTS(){ return Base.get().getConfig().getInt("slots-amount"); }

//	public String PERMISSION_DENIED() {
//		return color("&cPermission denied.");
//	}
//
//	public String DEPLOYING() {
//		return color("&a&lDarkend &f>> &aDeploying &f&lAir Drop &ain {count}&r..");
//	}
//
//	public String DEPLOYED() {
//		return color("&a&lDarkend &7>> Airdrop deployed!");
//	}
//
//	public String HEIGHT_AIR() {
//		return color("&cThere must be 30 air blocks above to deploy.");
//	}
//
//	public String AIRDROP_NAME() {
//		return color("&f&l** &a&LAirdrop&f&l **");
//	}
//
	public String color(String string) {
		return ChatColor.translateAlternateColorCodes('&', string);
	}

	public List<String> AIRDROP_LORE() {
		return Base.get().getConfig().getStringList("airdrop-lore");
	}

	public String getString(String key) {
		return ChatColor.translateAlternateColorCodes('&', messages.get(key)).replace("{arrow}", "»");
	}
}