package fun.eqad.ponyparkour.config;

import fun.eqad.ponyparkour.PonyParkour;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final PonyParkour plugin;
    private FileConfiguration config;
    private String prefix;
    public ConfigManager(PonyParkour plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        loadConfig();
    }

    public void loadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        this.prefix = ChatColor.translateAlternateColorCodes('&', config.getString("messages.prefix", "&8[&bPonyParkour&8]&r "));
    }

    public String getPrefix() {
        return prefix;
    }
}
