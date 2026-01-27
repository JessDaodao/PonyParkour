package fun.eqad.ponyparkour.manager;

import fun.eqad.ponyparkour.arena.ParkourArena;
import fun.eqad.ponyparkour.arena.ParkourSession;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ParkourManager {
    private final Map<String, ParkourArena> arenas;
    private final Map<UUID, ParkourSession> sessions;

    public ParkourManager() {
        this.arenas = new HashMap<>();
        this.sessions = new HashMap<>();
    }

    public void createArena(String name) {
        if (!arenas.containsKey(name)) {
            arenas.put(name, new ParkourArena(name));
        }
    }

    public void deleteArena(String name) {
        arenas.remove(name);
    }

    public ParkourArena getArena(String name) {
        return arenas.get(name);
    }

    public Map<String, ParkourArena> getArenas() {
        return arenas;
    }

    public void startSession(Player player, ParkourArena arena) {
        if (sessions.containsKey(player.getUniqueId())) {
            endSession(player);
        }
        ParkourSession session = new ParkourSession(player, arena);
        sessions.put(player.getUniqueId(), session);
        player.teleport(arena.getStartLocation());
        player.sendMessage("§a跑酷开始: " + arena.getName());
    }

    public void endSession(Player player) {
        if (sessions.containsKey(player.getUniqueId())) {
            sessions.remove(player.getUniqueId());
            
            Location lobby = fun.eqad.ponyparkour.PonyParkour.getInstance().getConfigManager().getLobbyLocation();
            if (lobby != null) {
                player.teleport(lobby);
            }
        }
    }

    public ParkourSession getSession(Player player) {
        return sessions.get(player.getUniqueId());
    }

    public boolean isPlaying(Player player) {
        return sessions.containsKey(player.getUniqueId());
    }
}
