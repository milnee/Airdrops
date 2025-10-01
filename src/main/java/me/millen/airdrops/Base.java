package me.millen.airdrops;
/*
 *  created by millen on 23/05/2020
 */

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.bukkit.plugin.java.JavaPlugin;

import me.millen.airdrops.cache.Cache;
import me.millen.airdrops.commands.Airdrops;
import me.millen.airdrops.listeners.PlaceAirdrop;
import me.millen.airdrops.listeners.SaveLoot;
import me.millen.airdrops.manager.AirdropManager;
import me.millen.airdrops.manager.LootManager;
import me.millen.airdrops.updater.Updater;

public class Base extends JavaPlugin {

    private AirdropManager airdropManager;
    private LootManager lootManager;
    private Cache cache;

    public static Base get() {
        return getPlugin(Base.class);
    }

    public void onEnable() {
        verifyConfiguration();
        setup();
    }

    public void onDisable() {
        lootManager.save();
        saveConfig();
    }

    public void setup() {
        cache = new Cache();
        cache.setup();

        airdropManager = new AirdropManager();
        airdropManager.setup();

        lootManager = new LootManager();
        lootManager.setup();

        getCommand("airdrops").setExecutor(new Airdrops());
        getServer().getPluginManager().registerEvents(new PlaceAirdrop(), this);
        getServer().getPluginManager().registerEvents(new SaveLoot(), this);
    }

    public AirdropManager getAirdropManager() {
        return airdropManager;
    }

    public LootManager getLootManager() {
        return lootManager;
    }

    public Cache getCache() {
        return cache;
    }

    public void verifyConfiguration() {
        File file = new File(getDataFolder(), "config.yml");

        Updater updater = new Updater();
        try {
            if (!file.exists()) {
                saveDefaultConfig();
                reloadConfig();
                return;
            }
            updater.update(this, "config.yml", file, Collections.emptyList());
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        reloadConfig();
    }

    public void reloadCfg() {
        reloadConfig();
        cache.reload();
    }
}