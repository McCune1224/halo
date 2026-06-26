package io.mckuna.halo;

import io.mckuna.halo.config.PluginConfig;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;

/**
 * Applies the configured beacon radius whenever a beacon block is loaded
 * into the world (server start, chunk load, placement).
 */
public class BeaconRadiusListener implements Listener {

    private final PluginConfig config;

    public BeaconRadiusListener(PluginConfig config) {
        this.config = config;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        applyToWorld(event.getWorld());
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        applyToChunk(event.getChunk());
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType() == Material.BEACON) {
            applyBlock(event.getBlock());
        }
    }

    /**
     * Apply the configured radius to a single beacon block.
     */
    void applyBlock(Block block) {
        if (block.getType() != Material.BEACON) return;
        Beacon beacon = (Beacon) block.getState();
        int tier = beacon.getTier();
        if (tier > 0) {
            beacon.setEffectRange(config.getRadiusForTier(tier));
            beacon.update(true, false);
        }
    }

    /**
     * Scan a chunk for beacon blocks and apply ranges.
     */
    void applyToChunk(Chunk chunk) {
        for (int cx = 0; cx < 16; cx++) {
            for (int cz = 0; cz < 16; cz++) {
                for (int cy = chunk.getWorld().getMinHeight(); cy < chunk.getWorld().getMaxHeight(); cy++) {
                    Block block = chunk.getBlock(cx, cy, cz);
                    if (block.getType() == Material.BEACON) {
                        applyBlock(block);
                    }
                }
            }
        }
    }

    /**
     * Scan all loaded chunks in a world for beacon blocks and apply ranges.
     */
    public void applyToWorld(org.bukkit.World world) {
        // ponytail: O(chunks * 16^3 * height) scan — fine for a one-time startup op.
        for (Chunk chunk : world.getLoadedChunks()) {
            applyToChunk(chunk);
        }
    }

    /**
     * Apply to all loaded worlds (used by /beaconreload after config change).
     */
    public void applyToAllWorlds(java.util.List<org.bukkit.World> worlds) {
        for (org.bukkit.World world : worlds) {
            applyToWorld(world);
        }
    }
}
