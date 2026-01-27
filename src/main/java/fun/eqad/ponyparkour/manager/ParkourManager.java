package fun.eqad.ponyparkour.manager;

import fun.eqad.ponyparkour.arena.ParkourArena;
import fun.eqad.ponyparkour.arena.ParkourSession;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Sound;

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
        
        ArmorStand stand = (ArmorStand) startLoc.getWorld().spawnEntity(startLoc, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(true);
        stand.setBasePlate(false);
        stand.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 200, 1, false, false));
        stand.addPassenger(player);
        session.setRideEntityUuid(stand.getUniqueId());
        
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20);
        
        player.sendTitle("§a" + arena.getName(), "§7作者: " + arena.getAuthor(), 10, 70, 20);

        // Start checking for landing
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!sessions.containsKey(player.getUniqueId())) {
                    this.cancel();
                    return;
                }
                
                ParkourSession currentSession = sessions.get(player.getUniqueId());
                if (!currentSession.isFalling()) {
                    this.cancel();
                    return;
                }

                Entity vehicle = player.getVehicle();
                if (vehicle == null) {
                    // Player dismounted early, force stop falling state
                    dismountAndStart(player, currentSession);
                    this.cancel();
                    return;
                }

                Location loc = vehicle.getLocation();
                if (loc.getBlockY() <= arena.getStartLocation().getBlockY()) {
                    dismountAndStart(player, currentSession);
                    this.cancel();
                }
            }
        }.runTaskTimer(fun.eqad.ponyparkour.PonyParkour.getInstance(), 5L, 1L);
    }

    private void dismountAndStart(Player player, ParkourSession session) {
        session.setFalling(false);
        session.resetStartTime();
        
        if (session.getRideEntityUuid() != null) {
            Entity entity = Bukkit.getEntity(session.getRideEntityUuid());
            if (entity != null) {
                entity.remove();
            }
            session.setRideEntityUuid(null);
        }
        
        player.leaveVehicle();
        player.removePotionEffect(PotionEffectType.SLOW_FALLING);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§a开始计时"));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
    }

    public void endSession(Player player) {
        if (sessions.containsKey(player.getUniqueId())) {
            ParkourSession session = sessions.get(player.getUniqueId());
            if (session.getRideEntityUuid() != null) {
                Entity entity = Bukkit.getEntity(session.getRideEntityUuid());
                if (entity != null) {
                    entity.remove();
                }
            }
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
