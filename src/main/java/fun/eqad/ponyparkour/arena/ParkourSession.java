package fun.eqad.ponyparkour.arena;

import org.bukkit.Location;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import fun.eqad.ponyparkour.PonyParkour;

public class ParkourSession {
    private final Player player;
    private final ParkourArena arena;
    private int currentCheckpointIndex;
    private long startTime;
    private boolean isFalling;
    private ItemStack[] savedInventory;
    private ItemStack[] savedArmor;
    private GameMode savedGameMode;
    private boolean playersHidden;
    private boolean finished;
    private long finishTime;
    private long lastCheckpointTime;
    private BukkitTask buffTask;

    public ParkourSession(Player player, ParkourArena arena) {
        this.player = player;
        this.arena = arena;
        this.currentCheckpointIndex = -1;
        this.startTime = System.currentTimeMillis();
        this.isFalling = true;
        this.playersHidden = false;
        this.finished = false;
        this.lastCheckpointTime = System.currentTimeMillis();
        startBuffTask();
    }

    private void startBuffTask() {
        this.buffTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 200, 0, false, false, false));
                }
            }
        }.runTaskTimer(PonyParkour.getInstance(), 0L, 100L);
    }

    public void stopBuffTask() {
        if (buffTask != null && !buffTask.isCancelled()) {
            buffTask.cancel();
        }
    }

    public void saveInventory() {
        this.savedInventory = player.getInventory().getContents();
        this.savedArmor = player.getInventory().getArmorContents();
    }

    public void saveGameMode() {
        this.savedGameMode = player.getGameMode();
    }

    public void restoreInventory() {
        if (savedInventory != null) {
            player.getInventory().setContents(savedInventory);
        }
        if (savedArmor != null) {
            player.getInventory().setArmorContents(savedArmor);
        }
    }

    public void restoreGameMode() {
        if (savedGameMode != null) {
            player.setGameMode(savedGameMode);
        }
    }

    public void setSavedInventory(ItemStack[] inventory) {
        this.savedInventory = inventory;
    }

    public void setSavedArmor(ItemStack[] armor) {
        this.savedArmor = armor;
    }

    public ItemStack[] getSavedInventory() {
        return savedInventory;
    }

    public ItemStack[] getSavedArmor() {
        return savedArmor;
    }

    public boolean arePlayersHidden() {
        return playersHidden;
    }

    public void setPlayersHidden(boolean hidden) {
        this.playersHidden = hidden;
    }

    public boolean isFalling() {
        return isFalling;
    }

    public void setFalling(boolean falling) {
        isFalling = falling;
        if (!falling) {
            this.startTime = System.currentTimeMillis();
        }
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void resetStartTime() {
        this.startTime = System.currentTimeMillis();
    }

    public Player getPlayer() {
        return player;
    }

    public ParkourArena getArena() {
        return arena;
    }

    public int getCurrentCheckpointIndex() {
        return currentCheckpointIndex;
    }

    public void setCheckpoint(int index) {
        this.currentCheckpointIndex = index;
        this.lastCheckpointTime = System.currentTimeMillis();
    }

    public long getLastCheckpointTime() {
        return lastCheckpointTime;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public long getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;
    }

    public long getStartTime() {
        return startTime;
    }
    
    public Location getLastCheckpointLocation() {
        if (currentCheckpointIndex == -1) {
            return arena.getStartLocation();
        }
        if (currentCheckpointIndex >= 0 && currentCheckpointIndex < arena.getCheckpoints().size()) {
            return arena.getCheckpoints().get(currentCheckpointIndex);
        }
        return arena.getStartLocation();
    }
}
