package me.millen.airdrops.commands;

/*
 *  created by millen on 23/05/2020
 */

import java.util.Map;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.millen.airdrops.Base;
import me.millen.airdrops.builder.AirdropBuilder;
import me.millen.airdrops.injection.Injector;

public class Airdrops implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (player.hasPermission("airdrops.use")) {
                if (args.length < 1) {
                    sendUsage(player);
                } else if (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("giveall")) {
                    if (!player.hasPermission("airdrops.give")) {
                        player.sendMessage(Base.get().getCache().PERMISSION_DENIED());
                        return false;
                    }
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.GRAY + "Usage: /airdrops give/giveall " + (args[0].equalsIgnoreCase("give") ? "(player) (amount)" : "(amount)"));
                        return false;
                    } else {
                        Player target = Bukkit.getPlayer(args[1]);
                        if (args[0].equalsIgnoreCase("give") && target == null) {
                            player.sendMessage(ChatColor.RED + "Unknown player '" + args[1] + "'.");
                            return false;
                        }
                        if (!isDouble(args[0].equalsIgnoreCase("give") ? args[2] : args[1])) {
                            player.sendMessage(ChatColor.RED + "Please specify the amount in numbers.");
                            return false;
                        }

                        ItemStack drop = new AirdropBuilder(Material.DISPENSER)
                                .name(ChatColor.translateAlternateColorCodes('&', Base.get().getCache().AIRDROP_NAME()))
                                .lore(Base.get().getCache().AIRDROP_LORE())
                                .amount(Integer.parseInt(args[0].equalsIgnoreCase("give") ? args[2] : args[1]))
                                .build();
                        if (args[0].equalsIgnoreCase("give"))
                            target.getInventory().addItem(drop);
                        else
                            Bukkit.getOnlinePlayers().forEach(player1 -> player1.getInventory().addItem(drop));
                    }
                } else if (args[0].equalsIgnoreCase("loot")) {
                    if (player.hasPermission("airdrops.loot"))
                        player.openInventory(Base.get().getLootManager().getLoot());
                } else if (args[0].equalsIgnoreCase("reload")) {
                    if (player.hasPermission("airdrops.reload")) {
                        Base.get().reloadCfg();
                        player.sendMessage(ChatColor.GREEN + "Reloaded config file.");
                    }
                } else if (args[0].equalsIgnoreCase("item")) {
                    if (player.hasPermission("airdrops.item")) {
                        if (player.getItemInHand() == null || player.getItemInHand().getType().equals(Material.AIR))
                            return false;

                        if (args.length >= 2) {
                            if (args[1].equalsIgnoreCase("chance")) {
                                if (args.length < 3) {
                                    player.sendMessage(ChatColor.GRAY + "Usage: /airdrops item chance (percentage)");
                                    return false;
                                }

                                if (!isDouble(args[2]) || Double.parseDouble(args[2]) < 0 || Double.parseDouble(args[2]) > 100) {
                                    player.sendMessage(ChatColor.RED + "Percentage must be a number from 0 to 100");
                                    return false;
                                } else {
                                    Injector injector = new Injector(player.getItemInHand());
                                    injector.setChances(Double.parseDouble(args[2]));

                                    player.sendMessage(ChatColor.YELLOW + "Modified chance to " + ChatColor.GOLD + args[2] + "%" + ChatColor.YELLOW + ".");
                                    player.setItemInHand(injector.getStack());
                                }
                            } else if (args[1].equalsIgnoreCase("flag")) {
                                if (args.length < 3) {
                                    player.sendMessage(ChatColor.GRAY + "Usage: /airdrops item flag (flag)");
                                } else {
                                    if (args[2].equalsIgnoreCase("destroy")) {
                                        Injector injector = new Injector(player.getItemInHand());

                                        injector.toggleDestroy();
                                        player.sendMessage(ChatColor.YELLOW + (Injector.hasKey("destroy", player.getItemInHand()) ? "Added" : "Removed") + " flag " + ChatColor.GOLD + "destroy" + ChatColor.YELLOW + ".");
                                        player.setItemInHand(injector.getStack());
                                    } else {
                                        player.sendMessage(ChatColor.GRAY + "Available flags: destroy");
                                        return false;
                                    }
                                }
                            } else if (args[1].equalsIgnoreCase("command")) {
                                if (args.length < 4 || !isDouble(args[2])) {
                                    player.sendMessage(ChatColor.GRAY + "Usage: /airdrops item command (times-executed) command..");
                                    return false;
                                }
                                StringBuilder cmd = new StringBuilder();
                                for (int index = 3; index < args.length; index++) {
                                    cmd.append(args[index]).append(" ");
                                }

                                Injector injector = new Injector(player.getItemInHand());

                                injector.setTimes(Integer.parseInt(args[2]));
                                injector.setCommand(cmd.toString());
                                player.sendMessage(ChatColor.YELLOW + "Set item command to: " + ChatColor.GOLD + cmd.toString());
                                player.setItemInHand(injector.getStack());
                            }
                        } else {
                            Map<String, String> keys = Injector.getKeys(player.getItemInHand());
                            if (Objects.nonNull(keys)) {
                                for (String key : keys.keySet())
                                    if (!key.contains("uuid"))
                                        player.sendMessage(ChatColor.GOLD + key + ": " + ChatColor.YELLOW + keys.get(key));
                            }
                        }
                    } else {
                        player.sendMessage(Base.get().getCache().PERMISSION_DENIED());
                    }
                } else {
                    sendUsage(player);
                }
            } else {
                player.sendMessage(Base.get().getCache().PERMISSION_DENIED());
            }
        } else {
            if (args.length < 2) {
                sender.sendMessage("/airdrops (give/giveall) (player) (amount)");
            } else {
                if (args[0].equalsIgnoreCase("give")) {
                    if (args.length >= 3) {
                        ItemStack drop = new AirdropBuilder(Material.DISPENSER)
                                .name(ChatColor.translateAlternateColorCodes('&', Base.get().getCache().AIRDROP_NAME()))
                                .lore(Base.get().getCache().AIRDROP_LORE())
                                .amount(Integer.parseInt(args[0].equalsIgnoreCase("give") ? args[2] : args[1]))
                                .build();
                        Player player = Bukkit.getPlayer(args[1]);
                        if (player == null) {
                            sender.sendMessage("Unknown player.");
                        } else {
                            player.getInventory().addItem(drop);
                        }
                    }
                } else if (args[0].equalsIgnoreCase("giveall")) {
                    ItemStack drop = new AirdropBuilder(Material.DISPENSER)
                            .name(ChatColor.translateAlternateColorCodes('&', Base.get().getCache().AIRDROP_NAME()))
                            .lore(Base.get().getCache().AIRDROP_LORE())
                            .amount(Integer.parseInt(args[0].equalsIgnoreCase("give") ? args[2] : args[1]))
                            .build();
                    Bukkit.getOnlinePlayers().forEach(players -> players.getInventory().addItem(drop));
                }
            }
        }

        return false;
    }

    public boolean isDouble(String string) {
        try {
            Double.parseDouble(string);
        } catch (NumberFormatException ex) {
            return false;
        }

        return true;
    }

    public void sendUsage(Player player) {
        player.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "----------------------------------------------");
        player.sendMessage(ChatColor.GRAY + "/airdrops loot - Show loot");
        if (player.hasPermission("airdrops.give")) {
            player.sendMessage(ChatColor.GRAY + "/airdrops give <player> <amount> - Give airdrops to a player");
            player.sendMessage(ChatColor.GRAY + "/airdrops giveall <amount> - Give airdrops to all players");
        }
        if (player.hasPermission("airdrops.item")) {
            player.sendMessage(ChatColor.GRAY + "/airdrops item - View item properties");
            player.sendMessage(ChatColor.GRAY + "/airdrops item chance <percentage> - Set drop chance");
            player.sendMessage(ChatColor.GRAY + "/airdrops item flag destroy - Toggle destroy flag");
            player.sendMessage(ChatColor.GRAY + "/airdrops item command <times> <command>");
        }
        if (player.hasPermission("airdrops.reload"))
            player.sendMessage(ChatColor.GRAY + "/airdrops reload - Reload config");
        player.sendMessage(ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "----------------------------------------------");
    }
}