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
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
            if (isSameBlock(to, arena.getStartLocation())) {
                session.setFalling(false);
                session.resetStartTime();
                player.removePotionEffect(org.bukkit.potion.PotionEffectType.SLOW_FALLING);
                player.removePotionEffect(org.bukkit.potion.PotionEffectType.SLOW);
                player.removePotionEffect(org.bukkit.potion.PotionEffectType.JUMP);
                player.setWalkSpeed(0.2f);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§a开始计时"));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
                return;
            }

            if (from.getX() != to.getX() || from.getZ() != to.getZ()) {
                Location newTo = from.clone();
                newTo.setY(to.getY());
                newTo.setYaw(to.getYaw());
                newTo.setPitch(to.getPitch());
                event.setTo(newTo);
            }
            return;
        }

        if (arena.getFallY() != null && to.getY() < arena.getFallY()) {
            player.teleport(session.getLastCheckpointLocation());
            player.sendMessage(prefix + "§c你掉下去了TAT");
            return;
        }

        if (isSameBlock(to, arena.getEndLocation())) {
            if (session.isFinished()) return;
            session.setFinished(true);

            long timeTaken = System.currentTimeMillis() - session.getStartTime();
            double timeSeconds = timeTaken / 1000.0;
            player.sendMessage(prefix + "§6跑酷完成！用时: " + timeSeconds + "秒!");

            plugin.getDataManager().saveRecord(arena.getName(), player.getUniqueId(), timeSeconds);

            player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 60, 0, false, false));
            player.sendTitle("§a已完成", "§e用时: " + timeSeconds + "秒", 10, 40, 10);

            new BukkitRunnable() {
                @Override
                public void run() {
                    parkourManager.endSession(player);
                }
            }.runTaskLater(plugin, 60L);
            return;
        }

        List<Location> checkpoints = arena.getCheckpoints();
        
        for (int i = 0; i < checkpoints.size(); i++) {
            if (isSameBlock(to, checkpoints.get(i))) {
                if (i > session.getCurrentCheckpointIndex()) {
                    session.setCheckpoint(i);
                    
                    new BukkitRunnable() {
                        int count = 0;
                        @Override
                        public void run() {
                            if (count >= 3) {
                                this.cancel();
                                return;
                            }
                            float pitch = 1f + (count * 0.5f);
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, pitch);
                            count++;
                        }
                    }.runTaskTimer(plugin, 0L, 4L);
                    
                    final int checkpointNum = i + 1;
                    final int totalCheckpoints = checkpoints.size();
                    final String fullMessage = "检查点 (" + checkpointNum + "/" + totalCheckpoints + ")";
                    
                    new BukkitRunnable() {
                        int tick = 0;
                        final int duration = 20;
                        final int typeDuration = 10;
                        
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
                            
                            int charCount = fullMessage.length();
                            if (tick < typeDuration) {
                                charCount = (int) ((float) tick / typeDuration * fullMessage.length()) + 1;
                                if (charCount > fullMessage.length()) charCount = fullMessage.length();
                            }
                            
                            String currentMessage = fullMessage.substring(0, charCount);
                            String message = color + currentMessage;
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

    @EventHandler
    public void onEntityDismount(EntityDismountEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (parkourManager.isPlaying(player)) {
                ParkourSession session = parkourManager.getSession(player);
                if (session.isFalling()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!parkourManager.isPlaying(player)) return;

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getItem() == null) return;

            if (event.getItem().getType() == Material.BARRIER) {
                parkourManager.endSession(player);
                player.sendMessage(plugin.getConfigManager().getPrefix() + "§c已退出跑酷");
                event.setCancelled(true);
            } else if (event.getItem().getType() == Material.ENDER_EYE) {
                ParkourSession session = parkourManager.getSession(player);
                boolean hide = !session.arePlayersHidden();
                session.setPlayersHidden(hide);
                
                plugin.getDataManager().setPlayerVisibility(player.getUniqueId(), hide);
                
                ItemStack item = event.getItem();
                org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
                
                if (hide) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (!p.getUniqueId().equals(player.getUniqueId())) {
                            player.hidePlayer(plugin, p);
                        }
                    }
                    meta.setDisplayName("§a显示/隐藏其他玩家 §7(当前: 隐藏)");
                    player.sendMessage(plugin.getConfigManager().getPrefix() + "§a已隐藏其他玩家");
                } else {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        player.showPlayer(plugin, p);
                    }
                    meta.setDisplayName("§a显示/隐藏其他玩家 §7(当前: 显示)");
                    player.sendMessage(plugin.getConfigManager().getPrefix() + "§a已显示其他玩家");
                }
                item.setItemMeta(meta);
                event.setCancelled(true);
            } else if (event.getItem().getType() == Material.RED_BED) {
                ParkourSession session = parkourManager.getSession(player);
                Location lastCheckpoint = session.getLastCheckpointLocation();
                
                if (lastCheckpoint == null) {
                    lastCheckpoint = session.getArena().getStartLocation();
                }

                if (lastCheckpoint != null) {
                    player.teleport(lastCheckpoint);
                    if (session.getCurrentCheckpointIndex() == -1) {
                        player.sendMessage(plugin.getConfigManager().getPrefix() + "§a已回到上一个检查点");
                    } else {
                        player.sendMessage(plugin.getConfigManager().getPrefix() + "§a已回到上一个检查点");
                    }
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                } else {
                    player.sendMessage(plugin.getConfigManager().getPrefix() + "§c无法找到传送点");
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (parkourManager.isPlaying(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (parkourManager.isPlaying(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerSwapHandItems(org.bukkit.event.player.PlayerSwapHandItemsEvent event) {
        if (parkourManager.isPlaying(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        parkourManager.pauseSession(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        Player joinedPlayer = event.getPlayer();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!joinedPlayer.isOnline()) return;

                String savedArena = plugin.getDataManager().getSavedSession(joinedPlayer.getUniqueId());
                boolean pendingTeleport = plugin.getDataManager().isPendingLobbyTeleport(joinedPlayer.getUniqueId());
                
                if (savedArena != null || pendingTeleport) {
                    ItemStack[] savedInv = plugin.getDataManager().getSavedInventory(joinedPlayer.getUniqueId());
                    ItemStack[] savedArmor = plugin.getDataManager().getSavedArmor(joinedPlayer.getUniqueId());
                    
                    if (savedInv != null) {
                        joinedPlayer.getInventory().setContents(savedInv);
                    }
                    if (savedArmor != null) {
                        joinedPlayer.getInventory().setArmorContents(savedArmor);
                    }
                    
                    Location lobby = plugin.getDataManager().getLobbyLocation();
                    if (lobby != null) {
                        joinedPlayer.teleport(lobby);
                    }
                    
                    if (savedArena != null) {
                        plugin.getDataManager().removeSavedSession(joinedPlayer.getUniqueId());
                    }
                    
                    if (pendingTeleport) {
                        plugin.getDataManager().setPendingLobbyTeleport(joinedPlayer.getUniqueId(), false);
                    }
                }

                for (ParkourSession session : parkourManager.getActiveSessions()) {
                    if (session.arePlayersHidden()) {
                        session.getPlayer().hidePlayer(plugin, joinedPlayer);
                    }
                }
            }
        }.runTaskLater(plugin, 20L);
    }

    private boolean isSameBlock(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) return false;
        if (loc1.getWorld() != loc2.getWorld()) return false;
        return loc1.getBlockX() == loc2.getBlockX() &&
               loc1.getBlockY() == loc2.getBlockY() &&
               loc1.getBlockZ() == loc2.getBlockZ();
    }
}
