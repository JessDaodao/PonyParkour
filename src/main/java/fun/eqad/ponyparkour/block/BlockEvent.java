package fun.eqad.ponyparkour.block;

import fun.eqad.ponyparkour.PonyParkour;
import fun.eqad.ponyparkour.arena.ParkourSession;
import fun.eqad.ponyparkour.manager.ParkourManager;
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
import org.bukkit.util.Vector;

public class BlockEvent implements Listener {

    private final PonyParkour plugin;
    private final ParkourManager parkourManager;

    public BlockEvent(PonyParkour plugin) {
        this.plugin = plugin;
        this.parkourManager = plugin.getParkourManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!parkourManager.isPlaying(player)) return;

        Location to = event.getTo();
        if (to == null) return;

        Block blockUnder = to.getBlock().getRelative(BlockFace.DOWN);
        Material type = blockUnder.getType();

        if (type == Material.BLUE_WOOL) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 2, false, false));
        } else if (type == Material.GREEN_WOOL) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 40, 2, false, false));
        } else if (type == Material.RED_WOOL) {
            ParkourSession session = parkourManager.getSession(player);
            player.teleport(session.getLastCheckpointLocation());
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§c不好, 你死翘翘了!");
        } else if (type == Material.STICKY_PISTON) {
            if (blockUnder.getBlockData() instanceof Directional) {
                Directional directional = (Directional) blockUnder.getBlockData();
                BlockFace facing = directional.getFacing();
                
                Vector velocity = new Vector(0, 0, 0);
                double strength = 1.5;
                double verticalBoost = 0.5;

                switch (facing) {
                    case UP:
                        velocity.setY(strength);
                        break;
                    case DOWN:
                        velocity.setY(-strength);
                        break;
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
    }
}
