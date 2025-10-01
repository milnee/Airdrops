package me.millen.airdrops.listeners;
/*
 *  created by millen on 23/05/2020
 */

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class SaveLoot implements Listener{

	@EventHandler
	public void onClose(InventoryClickEvent event){
		if(event.getView().getTitle().equalsIgnoreCase(ChatColor.GOLD +"Airdrops Loot") && !event.getWhoClicked().hasPermission("airdrops.editloot"))
			event.setCancelled(true);
	}
}
