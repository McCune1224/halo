package io.mckuna.halo.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Loads per-tier beacon radius overrides from config.yml
 * and supports runtime scalar multiplication.
 */
public class PluginConfig {

    private static final int[] DEFAULT_RANGES = {0, 10, 20, 30, 40}; // index = tier, 0 unused
    private final int[] base = new int[5];    // values loaded from config
    private final int[] current = new int[5]; // active values (base * optional scalar)

    /**
     * Load or reload config from disk.
     */
    public void load(JavaPlugin plugin) {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        for (int tier = 1; tier <= 4; tier++) {
            int value = config.getInt("beacon-radius.tier-" + tier, DEFAULT_RANGES[tier]);
            base[tier] = value >= 0 ? value : DEFAULT_RANGES[tier];
            current[tier] = base[tier];
        }
    }

    /**
     * Multiply all tier radii by a scalar (rounds to nearest block).
     */
    public void applyScalar(double scalar) {
        for (int tier = 1; tier <= 4; tier++) {
            current[tier] = (int) Math.round(base[tier] * scalar);
        }
    }

    /**
     * Returns the active effect radius for the given beacon tier (1-4).
     */
    public int getRadiusForTier(int tier) {
        if (tier < 1 || tier > 4) return DEFAULT_RANGES[Math.min(Math.max(tier, 1), 4)];
        return current[tier];
    }
}
