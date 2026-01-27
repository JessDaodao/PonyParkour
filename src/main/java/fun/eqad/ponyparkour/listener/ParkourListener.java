package fun.eqad.ponyparkour.listener;

import fun.eqad.ponyparkour.PonyParkour;
import fun.eqad.ponyparkour.arena.ParkourArena;
import fun.eqad.ponyparkour.arena.ParkourSession;
import fun.eqad.ponyparkour.manager.ParkourManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class ParkourListener implements Listener {

    private final PonyParkour plugin;
    private final ParkourManager parkourManager;

    public ParkourListener(PonyParkour plugin) {
        this.plugin = plugin;
        this.parkourManager = plugin.getParkourManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!parkourManager.isPlaying(player)) return;

        ParkourSession session = parkourManager.getSession(player);
        ParkourArena arena = session.getArena();
        Location to = event.getTo();

        if (to == null) return;

        String prefix = plugin.getConfigManager().getPrefix();

        if (isSameBlock(to, arena.getEndLocation())) {
            long timeTaken = System.currentTimeMillis() - session.getStartTime();
            player.sendMessage(prefix + "§6跑酷完成！用时: " + (timeTaken / 1000.0) + "秒!");
            parkourManager.endSession(player);
            return;
        }

        List<Location> checkpoints = arena.getCheckpoints();
        int nextCheckpointIndex = session.getCurrentCheckpointIndex() + 1;

        if (nextCheckpointIndex < checkpoints.size()) {
            Location nextCheckpoint = checkpoints.get(nextCheckpointIndex);
            if (isSameBlock(to, nextCheckpoint)) {
                session.setCheckpoint(nextCheckpointIndex);
                player.sendMessage(prefix + "§b到达检查点 " + (nextCheckpointIndex + 1) + " !");
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        parkourManager.endSession(event.getPlayer());
    }

    private boolean isSameBlock(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) return false;
        if (loc1.getWorld() != loc2.getWorld()) return false;
        return loc1.getBlockX() == loc2.getBlockX() &&
               loc1.getBlockY() == loc2.getBlockY() &&
               loc1.getBlockZ() == loc2.getBlockZ();
    }
}
