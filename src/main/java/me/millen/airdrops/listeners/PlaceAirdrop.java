package me.millen.airdrops.listeners;
/*
 *  created by millen on 23/05/2020
 */

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import me.millen.airdrops.Base;
import me.millen.airdrops.builder.Airdrop;
import me.millen.airdrops.injection.Injector;
import me.millen.airdrops.utils.RandomCollection;

public class PlaceAirdrop implements Listener {

    private List<Airdrop> airdrops = Lists.newArrayList();
    private Map<UUID, ItemStack> last = Maps.newConcurrentMap();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void interact(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            last.put(event.getPlayer().getUniqueId(), event.getPlayer().getItemInHand());
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof Dispenser) {
            Dispenser dispenser = (Dispenser) event.getInventory().getHolder();

            Airdrop airdrop = matchDrop(dispenser.getLocation());
            if (airdrop != null) {
                boolean empty = true;
                for (ItemStack s : dispenser.getInventory().getContents()) {
                    if (s != null) {
                        empty = false;
                        break;
                    }
                }

                if (empty) {
                    dispenser.getBlock().setType(Material.AIR);
                    airdrop.destroy();
                    airdrops.remove(airdrop);
                }
            }
        }
    }

    @EventHandler
    public void change(EntityChangeBlockEvent event) {
        Block block = event.getBlock();

        if (event.getEntity() instanceof FallingBlock) {
            Airdrop airdrop = matchDrop(block.getLocation());
            if (airdrop != null) {
                event.getEntity().remove();
                event.setCancelled(true);
                block.setType(Material.DISPENSER);
                Dispenser dispenser = (Dispenser) block.getState();
                dispenser.setData(new org.bukkit.material.Dispenser(BlockFace.UP));
                Player who = Bukkit.getPlayer(airdrop.getCaller());
                Bukkit.getScheduler().runTaskLater(Base.get(), () -> dispenser.getInventory().setContents(getLoot(who)), 1);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (matchDrop(block.getLocation().clone().add(0, 1, 0)) != null)
            event.setCancelled(true);

        Airdrop airdrop = matchDrop(event.getBlock().getLocation());
        if (airdrop != null) {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            airdrops.remove(airdrop);
            airdrop.destroy();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void place(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        Location check = event.getBlock().getLocation().clone().add(0, -15, 0);
        for (int index = 0; index < 30; index++) {
            Airdrop drop = matchDrop(check.add(0, 1, 0));
            if (drop != null) {
                event.setCancelled(true);
                return;
            }
        }

        if (!player.hasPermission("airdrops.use") || !validate(player))
            return;

        event.setCancelled(true);
        Block block = event.getBlock();
        Location loc = block.getLocation();

        if (emptyLoot() || (matchDrop(loc) != null))
            return;

        World world = block.getWorld();
        Location temp = loc.clone();

        for (int index = 0; index < 30; index++) {
            if (!world.getBlockAt(temp.add(0, 1, 0)).getType().equals(Material.AIR)) {
                String height = ChatColor.translateAlternateColorCodes('&', Base.get().getCache().HEIGHT_AIR());
                player.sendMessage(height);
                return;
            }
        }

        Block below = loc.getWorld().getBlockAt(loc.clone().add(0, -1, 0));
        if (below.getType().equals(Material.AIR) || below.isLiquid() || below.getType().isTransparent() || !isFull(below))
            return;

        try {
            if (player.getItemInHand().getAmount() <= 1)
                player.getInventory().remove(player.getItemInHand());
            else
                player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);

            AtomicReference<Hologram> hologram = new AtomicReference<>();
            hologram.set(HologramsAPI.createHologram(Base.get(), loc.clone().add(0.5, 1.5, 0.5)));
            hologram.get().appendTextLine("Airdrop falling in 3..");

            Airdrop airdrop = new Airdrop(player.getUniqueId(), loc, hologram.get());
            this.airdrops.add(airdrop);

            Firework f = block.getWorld().spawn(loc.clone().add(0.5, 0, 0.5), Firework.class);
            FireworkMeta fm = f.getFireworkMeta();
            fm.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BURST).withColor(Color.WHITE).withColor(Color.BLUE).trail(true).build());
            f.setFireworkMeta(fm);

            AtomicReference<Integer> countdown = new AtomicReference<>();
            countdown.set(3);

            AtomicReference<Integer> stay = new AtomicReference<>();
            stay.set(60);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (countdown.get() > 0) {
                        update(hologram.get(), Base.get().getCache().DEPLOYING().replace("{count}", String.valueOf(countdown.get())));

                        countdown.set(countdown.get() - 1);
                    } else if (countdown.get() == 0) {
                        update(hologram.get(), Base.get().getCache().DEPLOYED());

                        countdown.set(-1);
                    } else {
                        if (stay.get() > 0) {
                            stay.set(stay.get() - 1);
                        } else {
                            airdrop.destroy();

                            cancel();
                        }
                    }
                }
            }.runTaskTimer(Base.get(), 0, 13);
        } catch (Exception ignored) {
        }
    }

    public boolean validate(Player player) {
        ItemStack stack = last.get(player.getUniqueId());
        if (stack == null || stack.getType().equals(Material.AIR) || !stack.getType().equals(Material.DISPENSER))
            return false;

        ItemMeta meta = stack.getItemMeta();
        if (!meta.hasLore())
            return false;

        boolean date = Base.get().getCache().AIRDROP_LORE().stream().anyMatch(lore -> lore.contains("{date}"));
        for (int index = 0; index < meta.getLore().size(); index++) {
            if (!meta.getLore().get(index).equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', Base.get().getCache().AIRDROP_LORE().get(index))))
                return date && containsChars(meta.getLore().get(index), "//::");
        }

        return meta.getDisplayName().equalsIgnoreCase(Base.get().getCache().AIRDROP_NAME());
    }

    public boolean containsChars(String string, String chars) {
        for (Character character : chars.toCharArray()) {
            if (!string.contains(String.valueOf(character)))
                return false;
        }

        return true;
    }

    public boolean emptyLoot() {
        Inventory loot = Base.get().getLootManager().getLoot();
        for (int index = 0; index < 53; index++) {
            if (loot.getItem(index) == null)
                continue;

            if (Injector.hasKey("chance", loot.getItem(index)))
                return false;
        }

        Bukkit.getLogger().log(Level.WARNING, "[AIRDROPS] loot doesn't contain any item with chance.");
        Bukkit.getLogger().log(Level.WARNING, "Please modify items with /airdrops item chance (args..) to validate them.");
        return true;
    }

    public ItemStack[] getLoot(Player player) {
        Inventory loot = Base.get().getLootManager().getLoot();
        List<ItemStack> selected = Lists.newArrayList();

        RandomCollection<ItemStack> chances = new RandomCollection<>();
        for (int index = 0; index < 54; index++) {
            if (loot.getItem(index) == null)
                continue;

            if (Injector.hasKey("chance", loot.getItem(index)))
                chances.add(Double.parseDouble(Injector.getKey("chance", loot.getItem(index))), loot.getItem(index));
        }

        int slots = 9;
        for (int index = 0; index < slots; index++) {
            ItemStack select = chances.next().clone();

            ItemMeta meta = select.getItemMeta();
            if (meta.hasLore()) {
                List<String> lore = meta.getLore();
                lore.removeIf(lore2 -> lore2.startsWith(ChatColor.GOLD + "Chance: "));
                meta.setLore(lore);
                select.setItemMeta(meta);
            }

            if (!Injector.hasKey("destroy", select))
                selected.add(select);
            if (Injector.hasKey("command", select)) {
                String cmd = Injector.getKey("command", select).replace("{player}", player.getName()).replace("{amount}", String.valueOf(select.getAmount()));

                for (int i = 0; i < Integer.parseInt(Injector.getKey("times", select)); i++)
                    Base.get().getServer().dispatchCommand(Base.get().getServer().getConsoleSender(), cmd);
            }
        }

        return selected.toArray(new ItemStack[0]);
    }

    public Airdrop matchDrop(Location location) {
        for (Airdrop airdrop : airdrops) {
            if (airdrop.getLocation().equals(location))
                return airdrop;
        }

        return null;
    }

    public boolean isFull(Block below) {
        boolean full;
        switch (below.getType()) {
            case BED_BLOCK:
            case STEP:
            case WOOD_STEP:
            case GOLD_PLATE:
            case WOOD_PLATE:
            case STONE_PLATE:
            case IRON_PLATE:
            case STONE_BUTTON:
            case WOOD_BUTTON:
            case TRAP_DOOR:
            case ENCHANTMENT_TABLE:
            case SOUL_SAND:
            case BREWING_STAND:
            case ENDER_PORTAL_FRAME:
            case DRAGON_EGG:
            case TRIPWIRE_HOOK:
            case COCOA:
            case CHEST:
            case ENDER_CHEST:
            case TRAPPED_CHEST:
            case FLOWER_POT:
            case ANVIL:
            case REDSTONE_COMPARATOR:
            case DAYLIGHT_DETECTOR:
            case HOPPER:
                full = false;
                break;
            default:
                full = true;
        }

        return full;
    }

    public void update(Hologram hologram, String text) {
        hologram.clearLines();
        hologram.appendTextLine(text);
    }
}