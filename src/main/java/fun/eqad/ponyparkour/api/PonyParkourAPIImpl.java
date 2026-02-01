package fun.eqad.ponyparkour.api;

import fun.eqad.ponyparkour.PonyParkour;
import fun.eqad.ponyparkour.arena.ParkourArena;
import fun.eqad.ponyparkour.arena.ParkourSession;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import java.util.Map;

public class PonyParkourAPIImpl implements PonyParkourAPI {
    private final PonyParkour plugin;

    public PonyParkourAPIImpl(PonyParkour plugin) {
        this.plugin = plugin;
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public boolean isPlaying(Player player) {
        return plugin.getParkourManager().isPlaying(player);
    }

    @Override
    public ParkourSession getSession(Player player) {
        return plugin.getParkourManager().getSession(player);
    }

    @Override
    public ParkourArena getArena(String name) {
        return plugin.getParkourManager().getArena(name);
    }

    @Override
    public Map<String, ParkourArena> getArenas() {
        return plugin.getParkourManager().getArenas();
    }

    @Override
    public boolean joinArena(Player player, String arenaName) {
        ParkourArena arena = plugin.getParkourManager().getArena(arenaName);
        if (arena == null) return false;
        
        plugin.getParkourManager().startSession(player, arena);
        return true;
    }

    @Override
    public boolean leaveArena(Player player) {
        if (!isPlaying(player)) return false;
        
        plugin.getParkourManager().endSession(player);
        return true;
    }
}
