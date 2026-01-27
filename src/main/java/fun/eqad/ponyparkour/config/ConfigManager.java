package fun.eqad.ponyparkour.config;

import fun.eqad.ponyparkour.PonyParkour;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final PonyParkour plugin;
    private FileConfiguration config;
    private String prefix;
    private Location lobbyLocation;

    public ConfigManager(PonyParkour plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        loadConfig();
    }

    public void loadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        this.prefix = ChatColor.translateAlternateColorCodes('&', config.getString("messages.prefix", "&8[&bPonyParkour&8]&r "));
        
        if (plugin.getConfig().contains("lobby")) {
            this.lobbyLocation = (Location) plugin.getConfig().get("lobby");
        }
    }

    public void setLobbyLocation(Location location) {
        this.lobbyLocation = location;
        plugin.getConfig().set("lobby", location);
        plugin.saveConfig();
    }

    public Location getLobbyLocation() {
        return lobbyLocation;
    }

    public String getPrefix() {
        return prefix;
    }
}
