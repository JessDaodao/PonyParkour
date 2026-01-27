package fun.eqad.ponyparkour.arena;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ParkourSession {
    private final Player player;
    private final ParkourArena arena;
    private int currentCheckpointIndex;
    private long startTime;
    private boolean isFalling;

    public ParkourSession(Player player, ParkourArena arena) {
        this.player = player;
        this.arena = arena;
        this.currentCheckpointIndex = -1;
        this.startTime = System.currentTimeMillis();
        this.isFalling = true;
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
