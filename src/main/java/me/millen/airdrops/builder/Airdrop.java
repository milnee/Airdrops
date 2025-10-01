package me.millen.airdrops.builder;
/*
 *  created by millen on 26/05/2020
 */

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;

import com.gmail.filoghost.holographicdisplays.api.Hologram;

import me.millen.airdrops.Base;

public class Airdrop {

    private UUID caller;
    private Location location;
    private Hologram hologram;

    public Airdrop(UUID caller, Location loc, Hologram hologram) {
        this.caller = caller;
        this.location = loc;
        this.hologram = hologram;

        Base.get().getAirdropManager().dropAt(loc);
    }

    public UUID getCaller() {
        return caller;
    }

    public void setCaller(UUID caller) {
        this.caller = caller;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public Hologram getHologram() {
        return hologram;
    }

    public void setHologram(Hologram hologram) {
        this.hologram = hologram;
    }

    public void destroy() {
        location.getBlock().setType(Material.AIR);
        if (!hologram.isDeleted()) {
            hologram.delete();
        }
    }
}