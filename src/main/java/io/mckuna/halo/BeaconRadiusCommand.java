package io.mckuna.halo;

import io.mckuna.halo.config.PluginConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * /beaconreload — re-reads config and re-applies to all loaded beacons.
 * /beaconreload [scalar] — multiplies config radii by the scalar.
 */
public class BeaconRadiusCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final PluginConfig config;
    private final BeaconRadiusListener listener;

    public BeaconRadiusCommand(JavaPlugin plugin, PluginConfig config, BeaconRadiusListener listener) {
        this.plugin = plugin;
        this.config = config;
        this.listener = listener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            try {
                double scalar = Double.parseDouble(args[0]);
                config.load(plugin);            // reset to config values
                config.applyScalar(scalar);      // multiply
                listener.applyToAllWorlds(plugin.getServer().getWorlds());
                sender.sendMessage("§a[Halo] Beacon radii ×" + scalar);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cUsage: /beaconreload [scalar]");
            }
        } else {
            config.load(plugin);
            listener.applyToAllWorlds(plugin.getServer().getWorlds());
            sender.sendMessage("§a[Halo] Beacon radius config reloaded.");
        }
        return true;
    }
}
