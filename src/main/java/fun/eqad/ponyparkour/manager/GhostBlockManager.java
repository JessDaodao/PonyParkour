package fun.eqad.ponyparkour.manager;

import fun.eqad.ponyparkour.PonyParkour;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class GhostBlockManager implements Listener {

    private final PonyParkour plugin;
    private final Set<Location> ghostBlocks;
    private final int cycleTicks = 60;
    private boolean isVisibleState = true;

    public GhostBlockManager(PonyParkour plugin) {
        this.plugin = plugin;
        this.ghostBlocks = new HashSet<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        startTask();
        scanLoadedChunks();
    }

    private void scanLoadedChunks() {
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                scanChunk(chunk);
            }
        }
    }

    private void scanChunk(Chunk chunk) {
        
        int minY = chunk.getWorld().getMinHeight();
        int maxY = chunk.getWorld().getMaxHeight();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y < maxY; y++) {
                    Block block = chunk.getBlock(x, y, z);
                    if (block.getType() == Material.GRAY_WOOL) {
                        addGhostBlock(block.getLocation());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        scanChunk(event.getChunk());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.GRAY_WOOL) {
            if (ghostBlocks.contains(event.getBlock().getLocation())) {
                ghostBlocks.remove(event.getBlock().getLocation());
            }
        }
    }

    public void addGhostBlock(Location loc) {
        ghostBlocks.add(loc);
    }

    private void startTask() {
        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                tick++;
                
                if (tick >= cycleTicks) {
                    isVisibleState = !isVisibleState;
                    tick = 0;
                    updateBlocks();
                }

                if (!isVisibleState) {
                    showParticles();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void updateBlocks() {
        for (Location loc : ghostBlocks) {
            if (!loc.getChunk().isLoaded()) continue;

            Block block = loc.getBlock();
            
            if (isVisibleState) {
                block.setType(Material.GRAY_WOOL);
            } else {
                block.setType(Material.AIR);
            }
        }
    }

    private void showParticles() {
        for (Location loc : ghostBlocks) {
            if (!loc.getChunk().isLoaded()) continue;
            if (loc.getWorld().getNearbyEntities(loc, 20, 20, 20).stream().noneMatch(e -> e instanceof Player)) continue;

            loc.getWorld().spawnParticle(Particle.CLOUD, loc.clone().add(0.5, 0.5, 0.5), 5, 0.3, 0.3, 0.3, 0);
        }
    }
    
    public void scanNearbyBlocks(Player player) {
        Location pLoc = player.getLocation();
        int radius = 10;
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = pLoc.getBlock().getRelative(x, y, z);
                    if (block.getType() == Material.GRAY_WOOL) {
                        addGhostBlock(block.getLocation());
                    }
                }
            }
        }
    }
    
    public void restoreAllBlocks() {
        for (Location loc : ghostBlocks) {
            loc.getBlock().setType(Material.GRAY_WOOL);
        }
    }
}
