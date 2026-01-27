package fun.eqad.ponyparkour.listener;

import fun.eqad.ponyparkour.PonyParkour;
import fun.eqad.ponyparkour.arena.ParkourArena;
import fun.eqad.ponyparkour.arena.ParkourSession;
import fun.eqad.ponyparkour.manager.ParkourManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

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
        Location from = event.getFrom();

        if (to == null) return;

        String prefix = plugin.getConfigManager().getPrefix();

        if (session.isFalling()) {
            if (from.getX() != to.getX() || from.getZ() != to.getZ()) {
                Location newLoc = from.clone();
                newLoc.setY(to.getY());
                newLoc.setYaw(to.getYaw());
                newLoc.setPitch(to.getPitch());
                event.setTo(newLoc);
            }

            if (isSameBlock(to, arena.getStartLocation())) {
                session.setFalling(false);
                session.resetStartTime();
                player.removePotionEffect(org.bukkit.potion.PotionEffectType.SLOW_FALLING);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§a开始计时"));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
            }
            return;
        }

        if (arena.getFallY() != null && to.getY() < arena.getFallY()) {
            player.teleport(session.getLastCheckpointLocation());
            player.sendMessage(prefix + "§c你掉下去了！已传送回最近的检查点。");
            return;
        }

        if (isSameBlock(to, arena.getEndLocation())) {
            long timeTaken = System.currentTimeMillis() - session.getStartTime();
            player.sendMessage(prefix + "§6跑酷完成！用时: " + (timeTaken / 1000.0) + "秒!");
            parkourManager.endSession(player);
            return;
        }

        List<Location> checkpoints = arena.getCheckpoints();
        
        for (int i = 0; i < checkpoints.size(); i++) {
            if (isSameBlock(to, checkpoints.get(i))) {
                if (i > session.getCurrentCheckpointIndex()) {
                    session.setCheckpoint(i);
                    
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
                    
                    final int checkpointNum = i + 1;
                    final int totalCheckpoints = checkpoints.size();
                    new BukkitRunnable() {
                        int tick = 0;
                        final int duration = 20;
                        
                        final int startR = 0x25;
                        final int startG = 0x89;
                        final int startB = 0xff;
                        
                        final int endR = 0x00;
                        final int endG = 0x67;
                        final int endB = 0xe0;
                        
                        @Override
                        public void run() {
                            if (tick >= duration) {
                                this.cancel();
                                return;
                            }
                            
                            float ratio = (float) tick / (float) duration;
                            int r = (int) (startR + (endR - startR) * ratio);
                            int g = (int) (startG + (endG - startG) * ratio);
                            int b = (int) (startB + (endB - startB) * ratio);
                            
                            ChatColor color = ChatColor.of(new java.awt.Color(r, g, b));
                            String message = color + "检查点 (" + checkpointNum + "/" + totalCheckpoints + ")";
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
                            
                            tick++;
                        }
                    }.runTaskTimer(plugin, 0L, 1L);
                }
                break;
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        parkourManager.endSession(event.getPlayer());
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (parkourManager.isPlaying(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (parkourManager.isPlaying(player)) {
                event.setCancelled(true);
                player.setFoodLevel(20);
            }
        }
    }

    private boolean isSameBlock(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) return false;
        if (loc1.getWorld() != loc2.getWorld()) return false;
        return loc1.getBlockX() == loc2.getBlockX() &&
               loc1.getBlockY() == loc2.getBlockY() &&
               loc1.getBlockZ() == loc2.getBlockZ();
    }
}
