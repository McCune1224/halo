package io.mckuna.halo;

import io.papermc.paper.event.block.BeaconActivatedEvent;
import io.mckuna.halo.config.PluginConfig;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Applies the configured beacon radius whenever a beacon block is loaded
 * into the world (server start, chunk load, placement, activation) and
 * re-applies it when a nearby pyramid block is placed or broken.
 */
public class BeaconRadiusListener implements Listener {

    private static final int PYRAMID_HALF_WIDTH = 4; // largest pyramid layer is 9x9
    private static final int PYRAMID_HEIGHT = 4;     // up to 4 layers under the beacon
    private static final Material[] FALLBACK_PYRAMID_MATERIALS = {
        Material.NETHERITE_BLOCK, Material.EMERALD_BLOCK, Material.DIAMOND_BLOCK,
        Material.GOLD_BLOCK, Material.IRON_BLOCK
    }; // = contents of the minecraft:beacon_base_blocks tag (verified in 26.2), used if tag lookup fails

    private final JavaPlugin plugin;
    private final PluginConfig config;
    private final Tag<Material> pyramidTag;

    public BeaconRadiusListener(JavaPlugin plugin, PluginConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.pyramidTag = Bukkit.getTag(Tag.REGISTRY_BLOCKS,
            NamespacedKey.minecraft("beacon_base_blocks"), Material.class);
    }

    /** True for blocks that can form beacon pyramid layers. */
    private boolean isPyramidBlock(Block block) {
        Material type = block.getType();
        if (pyramidTag != null) return pyramidTag.isTagged(type);
        for (Material m : FALLBACK_PYRAMID_MATERIALS) {
            if (m == type) return true;
        }
        return false;
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
        if (event.isCancelled()) return;
        Block block = event.getBlock();
        if (block.getType() == Material.BEACON) {
            scheduleApply(block); // tier is 0 at event time; apply next tick once levels exist
        } else if (isPyramidBlock(block)) {
            scheduleNearbyBeaconScan(block);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        if (isPyramidBlock(event.getBlock())) {
            scheduleNearbyBeaconScan(event.getBlock());
        }
    }

    @EventHandler
    public void onBeaconActivated(BeaconActivatedEvent event) {
        // Fires from the beacon tick once levels went 0 -> n; covers
        // "beacon placed, pyramid built afterwards".
        applyBlock(event.getBlock());
    }

    /** Re-apply next tick (block events fire before the structure change settles). */
    private void scheduleApply(Block beaconBlock) {
        plugin.getServer().getRegionScheduler().run(plugin, beaconBlock.getLocation(),
            task -> applyBlock(beaconBlock));
    }

    /**
     * A changed block can be any pyramid layer; the beacon sits directly on top,
     * up to 4 blocks above and 4 blocks away on x/z. Schedule a re-apply for
     * every beacon found in that box.
     */
    private void scheduleNearbyBeaconScan(Block changed) {
        World world = changed.getWorld();
        int cx = changed.getX(), cy = changed.getY(), cz = changed.getZ();
        for (int dy = 1; dy <= PYRAMID_HEIGHT; dy++) {
            for (int dx = -PYRAMID_HALF_WIDTH; dx <= PYRAMID_HALF_WIDTH; dx++) {
                for (int dz = -PYRAMID_HALF_WIDTH; dz <= PYRAMID_HALF_WIDTH; dz++) {
                    Block candidate = world.getBlockAt(cx + dx, cy + dy, cz + dz);
                    if (candidate.getType() == Material.BEACON) {
                        scheduleApply(candidate);
                    }
                }
            }
        }
    }

    /** Apply the configured radius to a single beacon block. */
    void applyBlock(Block block) {
        if (block.getType() != Material.BEACON) return;
        applyRadius((Beacon) block.getState());
    }

    /**
     * Set the configured radius on a beacon state; returns true when the block
     * entity changed and was updated. Skips the network update when the
     * effective range already matches (no wasted block-entity packets).
     */
    private boolean applyRadius(Beacon beacon) {
        int tier = beacon.getTier();
        if (tier <= 0) return false;
        double target = config.getRadiusForTier(tier); // < 0 means vanilla tier-based range
        double current = beacon.getEffectRange();
        if (target < 0) {
            double vanilla = tier * 10.0 + 10.0; // mirrors Paper's computeBeaconRange
            if (Math.abs(current - vanilla) > 1e-9) {
                beacon.resetEffectRange();
                beacon.update(true, false);
                return true;
            }
        } else if (Math.abs(current - target) > 1e-9) {
            beacon.setEffectRange(target);
            beacon.update(true, false);
            return true;
        }
        return false;
    }

    /** Scan a chunk's tile entities for beacons; returns number of ranges changed. */
    int applyToChunk(Chunk chunk) {
        int changed = 0;
        for (BlockState state : chunk.getTileEntities()) {
            if (state instanceof Beacon beacon && applyRadius(beacon)) {
                changed++;
            }
        }
        return changed;
    }

    /** Scan all loaded chunks in a world; returns number of ranges changed. */
    public int applyToWorld(World world) {
        int changed = 0;
        for (Chunk chunk : world.getLoadedChunks()) {
            changed += applyToChunk(chunk);
        }
        return changed;
    }

    /** Apply to all loaded worlds; returns number of ranges changed. */
    public int applyToAllWorlds(java.util.List<World> worlds) {
        int changed = 0;
        for (World world : worlds) {
            changed += applyToWorld(world);
        }
        return changed;
    }
}
