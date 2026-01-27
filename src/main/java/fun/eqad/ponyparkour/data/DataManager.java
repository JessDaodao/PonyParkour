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

public class DataManager {

    private final PonyParkour plugin;
    private final ParkourManager parkourManager;
    private final File arenasFile;
    private final Gson gson;

    public DataManager(PonyParkour plugin) {
        this.plugin = plugin;
        this.parkourManager = plugin.getParkourManager();
        this.arenasFile = new File(plugin.getDataFolder(), "arenas.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void loadArenas() {
        if (!arenasFile.exists()) return;

        try (Reader reader = new FileReader(arenasFile)) {
            Type type = new TypeToken<Map<String, Map<String, Object>>>(){}.getType();
            Map<String, Map<String, Object>> data = gson.fromJson(reader, type);

            if (data == null) return;

            for (Map.Entry<String, Map<String, Object>> entry : data.entrySet()) {
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

                parkourManager.getArenas().put(name, arena);
            }
            plugin.getLogger().info("Loaded " + parkourManager.getArenas().size() + " arenas from JSON.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveArenas() {
        Map<String, Map<String, Object>> data = new HashMap<>();

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

            data.put(arena.getName(), arenaData);
        }

        try (Writer writer = new FileWriter(arenasFile)) {
            gson.toJson(data, writer);
            plugin.getLogger().info("Saved " + parkourManager.getArenas().size() + " arenas to JSON.");
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
        World world = Bukkit.getWorld((String) map.get("world"));
        if (world == null) return null;
        double x = ((Number) map.get("x")).doubleValue();
        double y = ((Number) map.get("y")).doubleValue();
        double z = ((Number) map.get("z")).doubleValue();
        float yaw = ((Number) map.get("yaw")).floatValue();
        float pitch = ((Number) map.get("pitch")).floatValue();
        return new Location(world, x, y, z, yaw, pitch);
    }
}
