package com.shardclay.shardplexhubmanager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ShardplexHubManager extends JavaPlugin implements Listener {

    private Location hubCenter;
    private int voidFallYLevel;

    @Override
    public void onEnable() {
        // This ensures the config file has all defaults. It will not overwrite user settings.
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        if (!loadConfigValues()) {
            getLogger().severe("Failed to load configuration! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("ShardplexHubManager has been enabled!");
    }

    private boolean loadConfigValues() {
        reloadConfig();
        voidFallYLevel = getConfig().getInt("void_fall_y_level", 70);

        String worldName = getConfig().getString("world_name", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            getLogger().severe("World '" + worldName + "' not found!");
            return false;
        }

        String locString = getConfig().getString("world_center", "0.5 160.0 0.5 90.0 -25.0");
        String[] parts = locString.split(" ");
        if (parts.length != 5) {
            getLogger().severe("Invalid format for world_center in config.yml!");
            return false;
        }

        try {
            double x = Double.parseDouble(parts[0]);
            double y = Double.parseDouble(parts[1]);
            double z = Double.parseDouble(parts[2]);
            float yaw = Float.parseFloat(parts[3]);
            float pitch = Float.parseFloat(parts[4]);
            hubCenter = new Location(world, x, y, z, yaw, pitch);
            return true;
        } catch (NumberFormatException e) {
            getLogger().severe("Could not parse coordinates in world_center!");
            return false;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (hubCenter != null) {
            event.getPlayer().teleport(hubCenter);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (hubCenter != null && event.getTo().getY() < voidFallYLevel) {
            if (hubCenter.getY() >= voidFallYLevel) {
                event.getPlayer().teleport(hubCenter);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("shm")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("shardplex.shm.reload")) {
                    sender.sendMessage(ChatColor.RED + "У вас нет прав на использование данной команды.");
                    return true;
                }
                if (loadConfigValues()) {
                    sender.sendMessage(ChatColor.GREEN + "ShardplexHubManager reloaded.");
                } else {
                    sender.sendMessage(ChatColor.RED + "Не удалось перезагрузить конфигурацию. Проверьте консоль на наличие ошибок.");
                }
                return true;
            }
        }
        return false;
    }
}