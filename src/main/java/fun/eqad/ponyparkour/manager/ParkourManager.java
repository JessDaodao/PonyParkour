package fun.eqad.ponyparkour.manager;

import fun.eqad.ponyparkour.arena.ParkourArena;
import fun.eqad.ponyparkour.arena.ParkourSession;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
        session.saveInventory();
        session.saveGameMode();
        
        boolean hidden = fun.eqad.ponyparkour.PonyParkour.getInstance().getDataManager().getPlayerVisibility(player.getUniqueId());
        session.setPlayersHidden(hidden);
        
        sessions.put(player.getUniqueId(), session);
        
        fun.eqad.ponyparkour.PonyParkour.getInstance().getDataManager().savePlayerSession(player.getUniqueId(), arena.getName());
        fun.eqad.ponyparkour.PonyParkour.getInstance().getDataManager().savePlayerInventory(player.getUniqueId(), session.getSavedInventory(), session.getSavedArmor());
        
        player.getInventory().clear();
        giveParkourItems(player);
        
        if (hidden) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.getUniqueId().equals(player.getUniqueId())) {
                    player.hidePlayer(fun.eqad.ponyparkour.PonyParkour.getInstance(), p);
                }
            }
            player.sendMessage(fun.eqad.ponyparkour.PonyParkour.getInstance().getConfigManager().getPrefix() + "§a已隐藏其他玩家");
        }
        
        Location startLoc = arena.getStartLocation().clone().add(0, 20, 0);
        player.teleport(startLoc);
        
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 600, 1, false, false));
        player.setWalkSpeed(0);
        
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setCollidable(false);
        player.setGameMode(GameMode.ADVENTURE);
        
        player.sendTitle("§a" + arena.getName(), "§7制作人员: " + arena.getAuthor(), 10, 70, 20);
    }

    public void resumeSession(Player player, ParkourArena arena, int checkpointIndex) {
        if (sessions.containsKey(player.getUniqueId())) {
            endSession(player);
        }
        ParkourSession session = new ParkourSession(player, arena);
        session.saveInventory();
        session.saveGameMode();
        session.setCheckpoint(checkpointIndex);
        
        boolean hidden = fun.eqad.ponyparkour.PonyParkour.getInstance().getDataManager().getPlayerVisibility(player.getUniqueId());
        session.setPlayersHidden(hidden);
        
        sessions.put(player.getUniqueId(), session);
        
        long savedElapsed = fun.eqad.ponyparkour.PonyParkour.getInstance().getDataManager().getSavedElapsedTime(player.getUniqueId());
        long current = System.currentTimeMillis();
        session.setStartTime(current - (savedElapsed * 1000));
        
        fun.eqad.ponyparkour.PonyParkour.getInstance().getDataManager().savePlayerSession(player.getUniqueId(), arena.getName());
        fun.eqad.ponyparkour.PonyParkour.getInstance().getDataManager().savePlayerCheckpoint(player.getUniqueId(), checkpointIndex);
        fun.eqad.ponyparkour.PonyParkour.getInstance().getDataManager().savePlayerElapsedTime(player.getUniqueId(), savedElapsed);
        fun.eqad.ponyparkour.PonyParkour.getInstance().getDataManager().savePlayerInventory(player.getUniqueId(), session.getSavedInventory(), session.getSavedArmor());
        
        player.getInventory().clear();
        giveParkourItems(player);
        
        if (hidden) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.getUniqueId().equals(player.getUniqueId())) {
                    player.hidePlayer(fun.eqad.ponyparkour.PonyParkour.getInstance(), p);
                }
            }
            player.sendMessage(fun.eqad.ponyparkour.PonyParkour.getInstance().getConfigManager().getPrefix() + "§a已隐藏其他玩家");
        }
        
        Location loc;
        if (checkpointIndex >= 0 && checkpointIndex < arena.getCheckpoints().size()) {
            loc = arena.getCheckpoints().get(checkpointIndex);
        } else {
            loc = arena.getStartLocation().clone().add(0, 1, 0);
        }
        
        Location tpLoc = loc.clone();
        tpLoc.setY(tpLoc.getY() + 0.5);
        tpLoc.setYaw(player.getLocation().getYaw());
        tpLoc.setPitch(player.getLocation().getPitch());
        player.teleport(tpLoc);
        
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setCollidable(false);
        player.setGameMode(GameMode.ADVENTURE);

        session.setFalling(false);
        session.setStartTime(current - (savedElapsed * 1000));
    }

    public void pauseSession(Player player) {
        if (sessions.containsKey(player.getUniqueId())) {
            ParkourSession session = sessions.get(player.getUniqueId());
            
            long elapsed = (System.currentTimeMillis() - session.getStartTime()) / 1000;
            fun.eqad.ponyparkour.PonyParkour.getInstance().getDataManager().savePlayerElapsedTime(player.getUniqueId(), elapsed);
            fun.eqad.ponyparkour.PonyParkour.getInstance().getDataManager().savePlayerCheckpoint(player.getUniqueId(), session.getCurrentCheckpointIndex());
            
            session.restoreInventory();
            session.restoreGameMode();
            sessions.remove(player.getUniqueId());

            if (player.isOnline()) {
                player.removePotionEffect(PotionEffectType.SLOW);
                player.removePotionEffect(PotionEffectType.JUMP);
                player.removePotionEffect(PotionEffectType.SLOW_FALLING);
                player.removePotionEffect(PotionEffectType.LEVITATION);
                player.setWalkSpeed(0.2f);
                player.setCollidable(true);
                
                for (Player p : Bukkit.getOnlinePlayers()) {
                    player.showPlayer(fun.eqad.ponyparkour.PonyParkour.getInstance(), p);
                }
            }
        }
    }

    public void endSession(Player player) {
        if (sessions.containsKey(player.getUniqueId())) {
            ParkourSession session = sessions.get(player.getUniqueId());
            session.restoreInventory();
            session.restoreGameMode();
            sessions.remove(player.getUniqueId());

            if (player.isOnline()) {
                Location lobby = fun.eqad.ponyparkour.PonyParkour.getInstance().getDataManager().getLobbyLocation();
                if (lobby != null) {
                    player.teleport(lobby);
                }
                player.removePotionEffect(PotionEffectType.SLOW);
                player.removePotionEffect(PotionEffectType.JUMP);
                player.removePotionEffect(PotionEffectType.SLOW_FALLING);
                player.removePotionEffect(PotionEffectType.LEVITATION);
                player.setWalkSpeed(0.2f);
                player.setCollidable(true);
                
                for (Player p : Bukkit.getOnlinePlayers()) {
                    player.showPlayer(fun.eqad.ponyparkour.PonyParkour.getInstance(), p);
                }
            }
            
            fun.eqad.ponyparkour.PonyParkour.getInstance().getDataManager().removeSavedSession(player.getUniqueId());
        }
    }

    private void giveParkourItems(Player player) {
        ItemStack leaveItem = new ItemStack(Material.BARRIER);
        ItemMeta leaveMeta = leaveItem.getItemMeta();
        leaveMeta.setDisplayName("§c退出跑酷");
        leaveItem.setItemMeta(leaveMeta);
        
        ItemStack hideItem = new ItemStack(Material.ENDER_EYE);
        ItemMeta hideMeta = hideItem.getItemMeta();
        
        ParkourSession session = getSession(player);
        if (session != null && session.arePlayersHidden()) {
            hideMeta.setDisplayName("§a显示/隐藏其他玩家 §7(当前: 隐藏)");
        } else {
            hideMeta.setDisplayName("§a显示/隐藏其他玩家 §7(当前: 显示)");
        }
        
        hideItem.setItemMeta(hideMeta);

        ItemStack checkpointItem = new ItemStack(Material.RED_BED);
        ItemMeta checkpointMeta = checkpointItem.getItemMeta();
        checkpointMeta.setDisplayName("§a回到上一个检查点");
        checkpointItem.setItemMeta(checkpointMeta);
        
        player.getInventory().setItem(3, checkpointItem);
        player.getInventory().setItem(4, hideItem);
        player.getInventory().setItem(5, leaveItem);
    }

    public ParkourSession getSession(Player player) {
        return sessions.get(player.getUniqueId());
    }

    public java.util.Collection<ParkourSession> getActiveSessions() {
        return sessions.values();
    }

    public boolean isPlaying(Player player) {
        return sessions.containsKey(player.getUniqueId());
    }
}
