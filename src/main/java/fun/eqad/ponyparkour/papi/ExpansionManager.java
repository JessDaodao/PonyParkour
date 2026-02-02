package fun.eqad.ponyparkour.papi;

import fun.eqad.ponyparkour.PonyParkour;
import fun.eqad.ponyparkour.arena.ParkourSession;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ExpansionManager extends PlaceholderExpansion {

    private final PonyParkour plugin;

    public ExpansionManager(PonyParkour plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "ponyparkour";
    }

    @Override
    public String getAuthor() {
        return "EQAD Network";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) return "";
        if (params == null || params.isEmpty()) return null;

        if (params.equalsIgnoreCase("arena")) {
            if (plugin.getParkourManager().isPlaying(player)) {
                ParkourSession session = plugin.getParkourManager().getSession(player);
                return session.getArena().getName();
            }
            return "None";
        }

        if (params.startsWith("time")) {
            if (plugin.getParkourManager().isPlaying(player)) {
                ParkourSession session = plugin.getParkourManager().getSession(player);
                if (session.isFalling()) {
                    return "00:00.000";
                }
                long timeTaken;
                if (session.isFinished()) {
                    timeTaken = session.getFinishTime() - session.getStartTime();
                } else {
                    timeTaken = System.currentTimeMillis() - session.getStartTime();
                }
                long minutes = (timeTaken / 1000) / 60;
                long seconds = (timeTaken / 1000) % 60;
                long millis = timeTaken % 1000;
                String text = String.format("%02d:%02d.%03d", minutes, seconds, millis);

                String type = "def";
                if (params.length() > "time".length()) {
                    String[] parts = params.split("_");
                    if (parts.length >= 2) {
                        type = parts[1];
                    }
                }

                if (type.equalsIgnoreCase("ani")) {
                    long millisSinceLastMinute = timeTaken % 60000;
                    long duration = 2000;
                    if (millisSinceLastMinute < duration) {
                        double progress = (double) millisSinceLastMinute / duration;
                        int val = (int) (progress * 255);
                        val = Math.max(0, Math.min(255, val));
                        String hex = String.format("#FF%02X%02X", val, val);
                        return net.md_5.bungee.api.ChatColor.of(hex) + text;
                    } else {
                        return net.md_5.bungee.api.ChatColor.WHITE + text;
                    }
                }

                return text;
            }
            return "00:00.000";
        }

        if (params.startsWith("checkpoint")) {
            if (plugin.getParkourManager().isPlaying(player)) {
                ParkourSession session = plugin.getParkourManager().getSession(player);
                int current = session.getCurrentCheckpointIndex() + 1;
                int total = session.getArena().getCheckpoints().size();
                String text = current + "/" + total;

                String type = "def";
                if (params.length() > "checkpoint".length()) {
                    String[] parts = params.split("_");
                    if (parts.length >= 2) {
                        type = parts[1];
                    }
                }

                if (type.equalsIgnoreCase("ani")) {
                    if (session.getCurrentCheckpointIndex() == -1) {
                        return net.md_5.bungee.api.ChatColor.WHITE + text;
                    }
                    long elapsed = System.currentTimeMillis() - session.getLastCheckpointTime();
                    long duration = 2000;
                    if (elapsed < duration) {
                        double progress = (double) elapsed / duration;
                        int val = (int) (progress * 255);
                        val = Math.max(0, Math.min(255, val));
                        String hex = String.format("#FF%02X%02X", val, val);
                        return net.md_5.bungee.api.ChatColor.of(hex) + text;
                    } else {
                        return net.md_5.bungee.api.ChatColor.WHITE + text;
                    }
                }

                return text;
            }
            return "0/0";
        }

        if (params.startsWith("rank_")) {
            String[] parts = params.split("_");
            if (parts.length >= 3) {
                String arenaName = parts[1];
                try {
                    int rank = Integer.parseInt(parts[2]);
                    String type = "full";
                    if (parts.length >= 4) {
                        type = parts[3];
                    }

                    if (rank < 1) return "非法参数";

                    Map<UUID, Double> records = plugin.getDataManager().getArenaRecords(arenaName);
                    if (records.isEmpty()) return "暂无数据";

                    List<Map.Entry<UUID, Double>> sortedRecords = new ArrayList<>(records.entrySet());
                    sortedRecords.sort(Map.Entry.comparingByValue());

                    if (rank > sortedRecords.size()) return "暂无数据";

                    Map.Entry<UUID, Double> entry = sortedRecords.get(rank - 1);
                    String playerName = Bukkit.getOfflinePlayer(entry.getKey()).getName();
                    if (playerName == null) playerName = "未知玩家";
                    
                    if (type.equalsIgnoreCase("name")) {
                        return playerName;
                    } else if (type.equalsIgnoreCase("time")) {
                        return entry.getValue() + "秒";
                    } else {
                        return playerName + " - " + entry.getValue() + "秒";
                    }
                } catch (NumberFormatException e) {
                    return "非法参数";
                }
            }
        }

        return null;
    }
}
