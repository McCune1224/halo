package io.mckuna.halo.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PluginConfigTest {

    private static PluginConfig config(String yaml) {
        PluginConfig config = new PluginConfig();
        config.load(YamlConfiguration.loadConfiguration(new StringReader(yaml)));
        return config;
    }

    @Test
    void missingKeysFallBackToVanillaDefaults() {
        PluginConfig config = config("beacon-radius: {}");
        assertEquals(10.0, config.getRadiusForTier(1));
        assertEquals(20.0, config.getRadiusForTier(2));
        assertEquals(30.0, config.getRadiusForTier(3));
        assertEquals(40.0, config.getRadiusForTier(4));
    }

    @Test
    void customValuesAreRead() {
        PluginConfig config = config("""
            beacon-radius:
              tier-1: 15
              tier-2: 25
              tier-3: 35
              tier-4: 45
            """);
        assertEquals(15.0, config.getRadiusForTier(1));
        assertEquals(45.0, config.getRadiusForTier(4));
    }

    @Test
    void negativeValueMeansVanillaDefault() {
        PluginConfig config = config("""
            beacon-radius:
              tier-1: -1
              tier-2: -7
            """);
        assertEquals(-1.0, config.getRadiusForTier(1));
        assertEquals(-1.0, config.getRadiusForTier(2));
        assertEquals(30.0, config.getRadiusForTier(3));
    }

    @Test
    void scalarMultipliesWithoutRounding() {
        PluginConfig config = config("beacon-radius: {tier-1: 10, tier-2: 20}");
        config.applyScalar(0.5);
        assertEquals(5.0, config.getRadiusForTier(1));
        config.applyScalar(3);
        assertEquals(30.0, config.getRadiusForTier(1));
    }

    @Test
    void outOfRangeTierReturnsVanillaSentinel() {
        PluginConfig config = config("beacon-radius: {}");
        assertEquals(-1.0, config.getRadiusForTier(0));
        assertEquals(-1.0, config.getRadiusForTier(5));
    }
}
