package io.mckuna.halo.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Loads per-tier beacon radius overrides from config.yml
 * and supports runtime scalar multiplication.
 * Negative config values (-1) mean "vanilla tier-based range" (no override).
 */
public class PluginConfig {

    private static final double[] DEFAULT_RANGES = {0, 10, 20, 30, 40}; // index = tier, 0 unused
    private final double[] base = new double[5];    // values loaded from config
    private final double[] current = new double[5]; // active values (base * optional scalar)

    /** Load or reload config from disk. */
    public void load(JavaPlugin plugin) {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        load(plugin.getConfig());
    }

    /** Load values from an already-obtained configuration. */
    public void load(FileConfiguration config) {
        for (int tier = 1; tier <= 4; tier++) {
            double value = config.getDouble("beacon-radius.tier-" + tier, DEFAULT_RANGES[tier]);
            base[tier] = value < 0 ? -1 : value;
            current[tier] = base[tier];
        }
    }

    /** Multiply all tier radii by a scalar (kept at double precision). */
    public void applyScalar(double scalar) {
        for (int tier = 1; tier <= 4; tier++) {
            current[tier] = base[tier] * scalar;
        }
    }

    /**
     * Active effect radius for the given tier (1-4); &lt; 0 means "use the
     * vanilla tier-based range". Returns -1 for out-of-range tiers.
     */
    public double getRadiusForTier(int tier) {
        if (tier < 1 || tier > 4) return -1;
        return current[tier];
    }
}
