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
    private List<java.util.UUID> hologramUuids;
    private java.util.Map<String, List<java.util.UUID>> pointHolograms;
    private Integer fallY;
    private String author;

    public ParkourArena(String name) {
        this.name = name;
        this.checkpoints = new ArrayList<>();
        this.icon = Material.GRASS_BLOCK;
        this.hologramUuids = new ArrayList<>();
        this.pointHolograms = new java.util.HashMap<>();
        this.fallY = null;
        this.author = "Unknown";
    }

    public List<java.util.UUID> getHologramUuids() {
        return hologramUuids;
    }

    public void addHologramUuid(java.util.UUID uuid) {
        this.hologramUuids.add(uuid);
    }

    public java.util.Map<String, List<java.util.UUID>> getPointHolograms() {
        return pointHolograms;
    }

    public void addPointHologram(String key, java.util.UUID uuid) {
        pointHolograms.computeIfAbsent(key, k -> new ArrayList<>()).add(uuid);
        addHologramUuid(uuid);
    }

    public List<java.util.UUID> removePointHolograms(String key) {
        List<java.util.UUID> uuids = pointHolograms.remove(key);
        if (uuids != null) {
            hologramUuids.removeAll(uuids);
        }
        return uuids;
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

    public Integer getFallY() {
        return fallY;
    }

    public void setFallY(Integer fallY) {
        this.fallY = fallY;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
