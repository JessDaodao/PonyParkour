package fun.eqad.ponyparkour.manager;

import fun.eqad.ponyparkour.arena.ParkourArena;
import fun.eqad.ponyparkour.arena.ParkourSession;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
        ParkourArena arena = arenas.get(name);
        if (arena != null) {
            for (UUID uuid : arena.getHologramUuids()) {
                Entity entity = Bukkit.getEntity(uuid);
                if (entity != null) {
                    entity.remove();
                }
            }
        }
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
        
        Location startLoc = arena.getStartLocation().clone().add(0, 20, 0);
        player.teleport(startLoc);
        
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 200, 1, false, false));
        
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20);
        
        player.sendTitle("§a" + arena.getName(), "§7作者: " + arena.getAuthor(), 10, 70, 20);
    }

    public void endSession(Player player) {
        if (sessions.containsKey(player.getUniqueId())) {
            sessions.remove(player.getUniqueId());
            
            Location lobby = fun.eqad.ponyparkour.PonyParkour.getInstance().getDataManager().getLobbyLocation();
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
