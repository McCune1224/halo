package io.mckuna.halo;

import io.mckuna.halo.config.PluginConfig;
import org.bukkit.plugin.java.JavaPlugin;

public class HaloPlugin extends JavaPlugin {

    private PluginConfig config;
    private BeaconRadiusListener listener;

    @Override
    public void onEnable() {
        saveDefaultConfig(); // ensure config.yml is on disk

        config = new PluginConfig();
        config.load(this);

        listener = new BeaconRadiusListener(this, config);
        getServer().getPluginManager().registerEvents(listener, this);

        BeaconRadiusCommand cmd = new BeaconRadiusCommand(this, config, listener);
        getCommand("beaconreload").setExecutor(cmd);

        // Apply to all already-loaded worlds (server startup)
        for (org.bukkit.World world : getServer().getWorlds()) {
            listener.applyToWorld(world);
        }

        getLogger().info("Halo enabled — beacon radius overrides active.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Halo disabled.");
    }
}
