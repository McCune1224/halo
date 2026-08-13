package io.mckuna.halo;

import io.mckuna.halo.config.PluginConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

/**
 * /beaconreload — re-reads config and re-applies to all loaded beacons.
 * /beaconreload [scalar] — multiplies config radii by the scalar.
 * The only argument is a numeric scalar, so tab completion offers nothing
 * (the server default would suggest player names, which makes no sense here).
 */
public class BeaconRadiusCommand implements TabExecutor {

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
        if (args.length == 0) {
            config.load(plugin);
            int count = listener.applyToAllWorlds(plugin.getServer().getWorlds());
            sender.sendMessage(Component.text("[Halo] Beacon radius config reloaded; updated " + count + " beacons.").color(NamedTextColor.GREEN));
            return true;
        }
        if (args.length > 1) {
            sender.sendMessage(Component.text("Usage: /beaconreload [scalar]").color(NamedTextColor.RED));
            return true;
        }
        double scalar;
        try {
            scalar = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Usage: /beaconreload [scalar]").color(NamedTextColor.RED));
            return true;
        }
        if (!Double.isFinite(scalar)) { // NaN/Infinity would poison the active radii
            sender.sendMessage(Component.text("Usage: /beaconreload [scalar]").color(NamedTextColor.RED));
            return true;
        }
        config.load(plugin);            // reset to config values
        config.applyScalar(scalar);      // multiply
        int count = listener.applyToAllWorlds(plugin.getServer().getWorlds());
        sender.sendMessage(Component.text("[Halo] Beacon radii ×" + scalar + "; updated " + count + " beacons.").color(NamedTextColor.GREEN));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // The sole argument is a numeric scalar; no meaningful completions.
        return Collections.emptyList();
    }
}
