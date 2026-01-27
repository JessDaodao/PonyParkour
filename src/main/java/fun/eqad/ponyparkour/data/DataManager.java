package fun.eqad.ponyparkour.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fun.eqad.ponyparkour.PonyParkour;
import fun.eqad.ponyparkour.arena.ParkourArena;
import fun.eqad.ponyparkour.manager.ParkourManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DataManager {

    private final PonyParkour plugin;
    private final ParkourManager parkourManager;
    private final File arenasFile;
    private final Gson gson;
    private Location lobbyLocation;

    public DataManager(PonyParkour plugin) {
        this.plugin = plugin;
        this.parkourManager = plugin.getParkourManager();
        this.arenasFile = new File(plugin.getDataFolder(), "data.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public Location getLobbyLocation() {
        return lobbyLocation;
    }

    public void setLobbyLocation(Location lobbyLocation) {
        this.lobbyLocation = lobbyLocation;
        saveArenas();
    }

    public void loadArenas() {
        if (!arenasFile.exists()) return;

        try (Reader reader = new FileReader(arenasFile)) {
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> rootData = gson.fromJson(reader, type);

            if (rootData == null) return;

            if (rootData.containsKey("lobby")) {
                this.lobbyLocation = deserializeLocation((Map<String, Object>) rootData.get("lobby"));
            }

            if (rootData.containsKey("arenas")) {
                Map<String, Map<String, Object>> arenasData = (Map<String, Map<String, Object>>) rootData.get("arenas");
                for (Map.Entry<String, Map<String, Object>> entry : arenasData.entrySet()) {
                    String name = entry.getKey();
                    Map<String, Object> arenaData = entry.getValue();
                ParkourArena arena = new ParkourArena(name);

                if (arenaData.containsKey("start")) {
                    arena.setStartLocation(deserializeLocation((Map<String, Object>) arenaData.get("start")));
                }
                if (arenaData.containsKey("end")) {
                    arena.setEndLocation(deserializeLocation((Map<String, Object>) arenaData.get("end")));
                }
                if (arenaData.containsKey("checkpoints")) {
                    List<Map<String, Object>> checkpointsData = (List<Map<String, Object>>) arenaData.get("checkpoints");
                    for (Map<String, Object> locData : checkpointsData) {
                        arena.addCheckpoint(deserializeLocation(locData));
                    }
                }
                if (arenaData.containsKey("icon")) {
                    arena.setIcon(Material.valueOf((String) arenaData.get("icon")));
                }
                if (arenaData.containsKey("holograms")) {
                    List<String> hologramUuids = (List<String>) arenaData.get("holograms");
                    for (String uuidStr : hologramUuids) {
                        arena.addHologramUuid(UUID.fromString(uuidStr));
                    }
                }
                if (arenaData.containsKey("pointHolograms")) {
                    Map<String, List<String>> pointHologramsData = (Map<String, List<String>>) arenaData.get("pointHolograms");
                    for (Map.Entry<String, List<String>> phEntry : pointHologramsData.entrySet()) {
                        String key = phEntry.getKey();
                        for (String uuidStr : phEntry.getValue()) {
                            arena.addPointHologram(key, UUID.fromString(uuidStr));
                        }
                    }
                }
                if (arenaData.containsKey("fallY")) {
                    arena.setFallY(((Number) arenaData.get("fallY")).intValue());
                }

                parkourManager.getArenas().put(name, arena);
                }
            }
            plugin.getLogger().info("Loaded " + parkourManager.getArenas().size() + " arenas from JSON.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveArenas() {
        Map<String, Object> rootData = new HashMap<>();
        
        if (lobbyLocation != null) {
            rootData.put("lobby", serializeLocation(lobbyLocation));
        }

        Map<String, Map<String, Object>> arenasData = new HashMap<>();
        for (ParkourArena arena : parkourManager.getArenas().values()) {
            Map<String, Object> arenaData = new HashMap<>();
            if (arena.getStartLocation() != null) {
                arenaData.put("start", serializeLocation(arena.getStartLocation()));
            }
            if (arena.getEndLocation() != null) {
                arenaData.put("end", serializeLocation(arena.getEndLocation()));
            }
            List<Map<String, Object>> checkpoints = new ArrayList<>();
            for (Location loc : arena.getCheckpoints()) {
                checkpoints.add(serializeLocation(loc));
            }
            arenaData.put("checkpoints", checkpoints);
            arenaData.put("icon", arena.getIcon().name());
            
            List<String> hologramUuids = new ArrayList<>();
            for (UUID uuid : arena.getHologramUuids()) {
                hologramUuids.add(uuid.toString());
            }
            arenaData.put("holograms", hologramUuids);

            Map<String, List<String>> pointHologramsData = new HashMap<>();
            for (Map.Entry<String, List<UUID>> entry : arena.getPointHolograms().entrySet()) {
                List<String> uuids = new ArrayList<>();
                for (UUID uuid : entry.getValue()) {
                    uuids.add(uuid.toString());
                }
                pointHologramsData.put(entry.getKey(), uuids);
            }
            arenaData.put("pointHolograms", pointHologramsData);
            
            if (arena.getFallY() != null) {
                arenaData.put("fallY", arena.getFallY());
            }

            arenasData.put(arena.getName(), arenaData);
        }
        rootData.put("arenas", arenasData);

        try (Writer writer = new FileWriter(arenasFile)) {
            gson.toJson(rootData, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Object> serializeLocation(Location loc) {
        Map<String, Object> map = new HashMap<>();
        if (loc == null || loc.getWorld() == null) return map;
        map.put("world", loc.getWorld().getName());
        map.put("x", loc.getX());
        map.put("y", loc.getY());
        map.put("z", loc.getZ());
        map.put("yaw", loc.getYaw());
        map.put("pitch", loc.getPitch());
        return map;
    }

    private Location deserializeLocation(Map<String, Object> map) {
        if (map == null || !map.containsKey("world")) return null;
        String worldName = (String) map.get("world");
        if (worldName == null) return null;
        
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        
        double x = ((Number) map.get("x")).doubleValue();
        double y = ((Number) map.get("y")).doubleValue();
        double z = ((Number) map.get("z")).doubleValue();
        float yaw = ((Number) map.get("yaw")).floatValue();
        float pitch = ((Number) map.get("pitch")).floatValue();
        return new Location(world, x, y, z, yaw, pitch);
    }
}
