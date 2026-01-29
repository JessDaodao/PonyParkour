package fun.eqad.ponyparkour.block;

import fun.eqad.ponyparkour.PonyParkour;
import fun.eqad.ponyparkour.arena.ParkourSession;
import fun.eqad.ponyparkour.manager.ParkourManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlockEvent implements Listener {

    private final PonyParkour plugin;
    private final ParkourManager parkourManager;
    private final Map<UUID, Long> pistonCooldowns = new HashMap<>();

    public BlockEvent(PonyParkour plugin) {
        this.plugin = plugin;
        this.parkourManager = plugin.getParkourManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        startBlockCheckTask();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!parkourManager.isPlaying(player)) return;
        if (!plugin.getConfigManager().shouldSpecialBlock()) return;

        Location to = event.getTo();
        if (to == null) return;

        if (event.getFrom().getBlockX() != to.getBlockX() ||
                event.getFrom().getBlockY() != to.getBlockY() ||
                event.getFrom().getBlockZ() != to.getBlockZ()) {
            plugin.getGhostBlockManager().scanNearbyBlocks(player);
        }

        Block blockUnder = to.clone().subtract(0, 0.1, 0).getBlock();
        if (blockUnder.getType() == Material.STICKY_PISTON) {
            processPiston(player, blockUnder);
            return;
        }

        Block centerBlock = to.getBlock();
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST};
        for (BlockFace face : faces) {
            Block relative = centerBlock.getRelative(face);
            if (relative.getType() == Material.STICKY_PISTON) {
                if (relative.getBlockData() instanceof Directional) {
                    Directional dir = (Directional) relative.getBlockData();
                    if (dir.getFacing() == face.getOppositeFace()) {
                        processPiston(player, relative);
                        return;
                    }
                }
            }
        }
    }

    public void handleBlockEffect(Player player, Block block) {
        if (!plugin.getConfigManager().shouldSpecialBlock()) return;

        Material type = block.getType();

        if (type == Material.BLUE_WOOL) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 2, false, false));
        } else if (type == Material.GREEN_WOOL) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 40, 2, false, false));
        } else if (type == Material.RED_WOOL) {
            ParkourSession session = parkourManager.getSession(player);
            if (session != null) {
                player.teleport(session.getLastCheckpointLocation());
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.sendMessage(plugin.getConfigManager().getPrefix() + "§c不好, 你死翘翘了!");
            }
        }
    }

    private void processPiston(Player player, Block pistonBlock) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        if (pistonCooldowns.containsKey(playerId)) {
            long lastTime = pistonCooldowns.get(playerId);
            if (currentTime - lastTime < 500) {
                return;
            }
        }
        pistonCooldowns.put(playerId, currentTime);

        if (pistonBlock.getBlockData() instanceof Directional) {
            Directional directional = (Directional) pistonBlock.getBlockData();
            BlockFace facing = directional.getFacing();
            
            Vector velocity = new Vector(0, 0, 0);
            double strength = 3.0;
            double upStrength = 1.8;
            double verticalBoost = 0.7;

            switch (facing) {
                case UP:
                    velocity.setY(upStrength);
                    break;
                case DOWN:
                    return;
                case NORTH:
                    velocity.setZ(-strength).setY(verticalBoost);
                    break;
                case SOUTH:
                    velocity.setZ(strength).setY(verticalBoost);
                    break;
                case WEST:
                    velocity.setX(-strength).setY(verticalBoost);
                    break;
                case EAST:
                    velocity.setX(strength).setY(verticalBoost);
                    break;
                default:
                    velocity.setY(strength);
                    break;
            }
            
            player.setVelocity(velocity);
            player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1.0f, 1.0f);
        }
    }

    private void startBlockCheckTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (parkourManager.isPlaying(player)) {
                        Location loc = player.getLocation().subtract(0, 0.1, 0);
                        Block block = loc.getBlock();
                        if (block.getType() != Material.AIR && block.getType() != Material.PISTON && block.getType() != Material.STICKY_PISTON) {
                            handleBlockEffect(player, block);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }
}
