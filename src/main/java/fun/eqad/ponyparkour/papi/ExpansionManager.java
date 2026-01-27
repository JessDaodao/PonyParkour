package fun.eqad.ponyparkour.papi;

import fun.eqad.ponyparkour.PonyParkour;
import fun.eqad.ponyparkour.arena.ParkourSession;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

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

        // %ponyparkour_time%
        if (params.equalsIgnoreCase("time")) {
            if (plugin.getParkourManager().isPlaying(player)) {
                ParkourSession session = plugin.getParkourManager().getSession(player);
                if (session.isFalling()) {
                    return "00:00.000";
                }
                long timeTaken = System.currentTimeMillis() - session.getStartTime();
                long minutes = (timeTaken / 1000) / 60;
                long seconds = (timeTaken / 1000) % 60;
                long millis = timeTaken % 1000;
                return String.format("%02d:%02d.%03d", minutes, seconds, millis);
            }
            return "00:00.000";
        }

        // %ponyparkour_arena_name%
        if (params.equalsIgnoreCase("arena_name")) {
            if (plugin.getParkourManager().isPlaying(player)) {
                ParkourSession session = plugin.getParkourManager().getSession(player);
                return session.getArena().getName();
            }
            return "None";
        }

        return null;
    }
}
