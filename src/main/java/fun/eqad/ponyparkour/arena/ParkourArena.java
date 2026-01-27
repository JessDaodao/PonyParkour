package fun.eqad.ponyparkour.arena;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class ParkourArena {
    private String name;
    private Location startLocation;
    private Location endLocation;
    private List<Location> checkpoints;
    private Material icon;

    public ParkourArena(String name) {
        this.name = name;
        this.checkpoints = new ArrayList<>();
        this.icon = Material.GRASS_BLOCK;
    }

    public String getName() {
        return name;
    }

    public Location getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(Location startLocation) {
        this.startLocation = startLocation;
    }

    public Location getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(Location endLocation) {
        this.endLocation = endLocation;
    }

    public List<Location> getCheckpoints() {
        return checkpoints;
    }

    public void addCheckpoint(Location checkpoint) {
        this.checkpoints.add(checkpoint);
    }

    public Material getIcon() {
        return icon;
    }

    public void setIcon(Material icon) {
        this.icon = icon;
    }
}
