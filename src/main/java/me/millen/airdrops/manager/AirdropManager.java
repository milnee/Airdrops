package me.millen.airdrops.manager;
/*
 *  created by millen on 23/05/2020
 */

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.material.MaterialData;

import me.millen.airdrops.interfaces.Manager;

public class AirdropManager implements Manager{

	public void setup(){}

	public void dropAt(Location location){
		FallingBlock fallingBlock = location.getWorld().spawnFallingBlock(location.clone().add(0.5, 30, 0.5), Material.DISPENSER, new MaterialData(Material.DISPENSER).getData());
	}
}
