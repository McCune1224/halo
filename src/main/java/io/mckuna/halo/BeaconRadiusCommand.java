package io.mckuna.halo;

import io.mckuna.halo.config.PluginConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
                int count = listener.applyToAllWorlds(plugin.getServer().getWorlds());
                sender.sendMessage(Component.text("[Halo] Beacon radii ×" + scalar + "; updated " + count + " beacons.").color(NamedTextColor.GREEN));
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("Usage: /beaconreload [scalar]").color(NamedTextColor.RED));
            }
        } else {
            config.load(plugin);
            int count = listener.applyToAllWorlds(plugin.getServer().getWorlds());
            sender.sendMessage(Component.text("[Halo] Beacon radius config reloaded; updated " + count + " beacons.").color(NamedTextColor.GREEN));
        }
        return true;
    }
}
