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
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

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
    private final Map<String, Map<String, Object>> brokenArenas = new HashMap<>();
    private final Map<UUID, Boolean> playerVisibilityPrefs = new HashMap<>();
    private final Map<UUID, String> savedSessions = new HashMap<>();
    private final Map<UUID, String> savedInventories = new HashMap<>();
    private final Map<UUID, String> savedArmors = new HashMap<>();
    private final Map<UUID, Boolean> pendingLobbyTeleport = new HashMap<>();
    private final Map<String, Map<UUID, Double>> arenaRecords = new HashMap<>();

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
        brokenArenas.clear();

        try (Reader reader = new FileReader(arenasFile)) {
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> rootData = gson.fromJson(reader, type);

            if (rootData == null) return;

            if (rootData.containsKey("lobby")) {
                this.lobbyLocation = deserializeLocation((Map<String, Object>) rootData.get("lobby"));
            }

            if (rootData.containsKey("playerVisibility")) {
                Map<String, Boolean> visibilityData = (Map<String, Boolean>) rootData.get("playerVisibility");
                for (Map.Entry<String, Boolean> entry : visibilityData.entrySet()) {
                    playerVisibilityPrefs.put(UUID.fromString(entry.getKey()), entry.getValue());
                }
            }

            if (rootData.containsKey("savedSessions")) {
                Map<String, String> sessionsData = (Map<String, String>) rootData.get("savedSessions");
                for (Map.Entry<String, String> entry : sessionsData.entrySet()) {
                    savedSessions.put(UUID.fromString(entry.getKey()), entry.getValue());
                }
            }

            if (rootData.containsKey("savedInventories")) {
                Map<String, String> invData = (Map<String, String>) rootData.get("savedInventories");
                for (Map.Entry<String, String> entry : invData.entrySet()) {
                    savedInventories.put(UUID.fromString(entry.getKey()), entry.getValue());
                }
            }

            if (rootData.containsKey("savedArmors")) {
                Map<String, String> armorData = (Map<String, String>) rootData.get("savedArmors");
                for (Map.Entry<String, String> entry : armorData.entrySet()) {
                    savedArmors.put(UUID.fromString(entry.getKey()), entry.getValue());
                }
            }

            if (rootData.containsKey("pendingLobbyTeleport")) {
                List<String> pending = (List<String>) rootData.get("pendingLobbyTeleport");
                for (String uuidStr : pending) {
                    pendingLobbyTeleport.put(UUID.fromString(uuidStr), true);
                }
            }

            if (rootData.containsKey("arenaRecords")) {
                Map<String, Map<String, Double>> recordsData = (Map<String, Map<String, Double>>) rootData.get("arenaRecords");
                for (Map.Entry<String, Map<String, Double>> entry : recordsData.entrySet()) {
                    Map<UUID, Double> records = new HashMap<>();
                    for (Map.Entry<String, Double> recordEntry : entry.getValue().entrySet()) {
                        records.put(UUID.fromString(recordEntry.getKey()), recordEntry.getValue());
                    }
                    arenaRecords.put(entry.getKey(), records);
                }
            }

            if (rootData.containsKey("arenas")) {
                Map<String, Map<String, Object>> arenasData = (Map<String, Map<String, Object>>) rootData.get("arenas");
                for (Map.Entry<String, Map<String, Object>> entry : arenasData.entrySet()) {
                    String name = entry.getKey();
                    Map<String, Object> arenaData = entry.getValue();
                    
                    try {
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
                                Location loc = deserializeLocation(locData);
                                if (loc != null) {
                                    arena.addCheckpoint(loc);
                                }
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
                        if (arenaData.containsKey("author")) {
                            arena.setAuthor((String) arenaData.get("author"));
                        }

                        parkourManager.getArenas().put(name, arena);
                    } catch (IllegalArgumentException e) {
                        brokenArenas.put(name, arenaData);
                    }
                }
            }
            plugin.getLogger().info("已加载 " + parkourManager.getArenas().size() + " 个跑酷地图");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveArenas() {
        Map<String, Object> rootData = new HashMap<>();
        
        if (lobbyLocation != null) {
            rootData.put("lobby", serializeLocation(lobbyLocation));
        }

        Map<String, Boolean> visibilityData = new HashMap<>();
        for (Map.Entry<UUID, Boolean> entry : playerVisibilityPrefs.entrySet()) {
            visibilityData.put(entry.getKey().toString(), entry.getValue());
        }
        rootData.put("playerVisibility", visibilityData);

        Map<String, String> sessionsData = new HashMap<>();
        for (Map.Entry<UUID, String> entry : savedSessions.entrySet()) {
            sessionsData.put(entry.getKey().toString(), entry.getValue());
        }
        rootData.put("savedSessions", sessionsData);

        Map<String, String> invData = new HashMap<>();
        for (Map.Entry<UUID, String> entry : savedInventories.entrySet()) {
            invData.put(entry.getKey().toString(), entry.getValue());
        }
        rootData.put("savedInventories", invData);

        Map<String, String> armorData = new HashMap<>();
        for (Map.Entry<UUID, String> entry : savedArmors.entrySet()) {
            armorData.put(entry.getKey().toString(), entry.getValue());
        }
        rootData.put("savedArmors", armorData);

        List<String> pendingTeleport = new ArrayList<>();
        for (UUID uuid : pendingLobbyTeleport.keySet()) {
            pendingTeleport.add(uuid.toString());
        }
        rootData.put("pendingLobbyTeleport", pendingTeleport);

        Map<String, Map<String, Double>> recordsData = new HashMap<>();
        for (Map.Entry<String, Map<UUID, Double>> entry : arenaRecords.entrySet()) {
            Map<String, Double> records = new HashMap<>();
            for (Map.Entry<UUID, Double> recordEntry : entry.getValue().entrySet()) {
                records.put(recordEntry.getKey().toString(), recordEntry.getValue());
            }
            recordsData.put(entry.getKey(), records);
        }
        rootData.put("arenaRecords", recordsData);

        Map<String, Map<String, Object>> arenasData = new HashMap<>(brokenArenas);
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
                if (loc != null) {
                    checkpoints.add(serializeLocation(loc));
                }
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
            if (arena.getAuthor() != null) {
                arenaData.put("author", arena.getAuthor());
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
        if (world == null) {
            throw new IllegalArgumentException("世界不存在: " + worldName);
        }
        
        double x = ((Number) map.get("x")).doubleValue();
        double y = ((Number) map.get("y")).doubleValue();
        double z = ((Number) map.get("z")).doubleValue();
        float yaw = ((Number) map.get("yaw")).floatValue();
        float pitch = ((Number) map.get("pitch")).floatValue();
        return new Location(world, x, y, z, yaw, pitch);
    }

    public boolean getPlayerVisibility(UUID uuid) {
        return playerVisibilityPrefs.getOrDefault(uuid, false);
    }

    public void setPlayerVisibility(UUID uuid, boolean hidden) {
        playerVisibilityPrefs.put(uuid, hidden);
        saveArenas();
    }

    public void savePlayerSession(UUID uuid, String arenaName) {
        savedSessions.put(uuid, arenaName);
        saveArenas();
    }

    public String getSavedSession(UUID uuid) {
        return savedSessions.get(uuid);
    }

    public void removeSavedSession(UUID uuid) {
        savedSessions.remove(uuid);
        savedInventories.remove(uuid);
        savedArmors.remove(uuid);
        saveArenas();
    }

    public void savePlayerInventory(UUID uuid, ItemStack[] inventory, ItemStack[] armor) {
        try {
            savedInventories.put(uuid, itemStackArrayToBase64(inventory));
            savedArmors.put(uuid, itemStackArrayToBase64(armor));
            saveArenas();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ItemStack[] getSavedInventory(UUID uuid) {
        if (!savedInventories.containsKey(uuid)) return null;
        try {
            return itemStackArrayFromBase64(savedInventories.get(uuid));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ItemStack[] getSavedArmor(UUID uuid) {
        if (!savedArmors.containsKey(uuid)) return null;
        try {
            return itemStackArrayFromBase64(savedArmors.get(uuid));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setPendingLobbyTeleport(UUID uuid, boolean pending) {
        if (pending) {
            pendingLobbyTeleport.put(uuid, true);
        } else {
            pendingLobbyTeleport.remove(uuid);
        }
        saveArenas();
    }

    public boolean isPendingLobbyTeleport(UUID uuid) {
        return pendingLobbyTeleport.containsKey(uuid);
    }

    public void saveRecord(String arenaName, UUID uuid, double time) {
        arenaRecords.computeIfAbsent(arenaName, k -> new HashMap<>());
        Map<UUID, Double> records = arenaRecords.get(arenaName);
        if (!records.containsKey(uuid) || records.get(uuid) > time) {
            records.put(uuid, time);
            saveArenas();
        }
    }

    public Map<UUID, Double> getArenaRecords(String arenaName) {
        return arenaRecords.getOrDefault(arenaName, new HashMap<>());
    }

    private String itemStackArrayToBase64(ItemStack[] items) throws IllegalStateException, IOException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            
            dataOutput.writeInt(items.length);
            
            for (int i = 0; i < items.length; i++) {
                dataOutput.writeObject(items[i]);
            }
            
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IOException("Unable to save item stacks.", e);
        }
    }

    private ItemStack[] itemStackArrayFromBase64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];
    
            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }
    
            dataInput.close();
            return items;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }
}
