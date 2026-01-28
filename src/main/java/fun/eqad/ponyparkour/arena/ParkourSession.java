package fun.eqad.ponyparkour.arena;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ParkourSession {
    private final Player player;
    private final ParkourArena arena;
    private int currentCheckpointIndex;
    private long startTime;
    private boolean isFalling;
    private ItemStack[] savedInventory;
    private ItemStack[] savedArmor;
    private boolean playersHidden;

    public ParkourSession(Player player, ParkourArena arena) {
        this.player = player;
        this.arena = arena;
        this.currentCheckpointIndex = -1;
        this.startTime = System.currentTimeMillis();
        this.isFalling = true;
        this.playersHidden = false;
    }

    public void saveInventory() {
        this.savedInventory = player.getInventory().getContents();
        this.savedArmor = player.getInventory().getArmorContents();
    }

    public void restoreInventory() {
        if (savedInventory != null) {
            player.getInventory().setContents(savedInventory);
        }
        if (savedArmor != null) {
            player.getInventory().setArmorContents(savedArmor);
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
