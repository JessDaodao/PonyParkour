package fun.eqad.ponyparkour;

import fun.eqad.ponyparkour.command.CommandManager;
import fun.eqad.ponyparkour.manager.ParkourManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class PonyParkour extends JavaPlugin {

    private static PonyParkour instance;
    private ParkourManager parkourManager;
    private CommandManager commandManager;
    private fun.eqad.ponyparkour.gui.GUIManager guiManager;
    private fun.eqad.ponyparkour.listener.ParkourListener parkourListener;
    private fun.eqad.ponyparkour.config.ConfigManager configManager;
    private fun.eqad.ponyparkour.data.DataManager dataManager;

    @Override
    public void onEnable() {
        instance = this;
        this.parkourManager = new ParkourManager();
        this.configManager = new fun.eqad.ponyparkour.config.ConfigManager(this);
        this.dataManager = new fun.eqad.ponyparkour.data.DataManager(this);
        this.dataManager.loadArenas();
        this.guiManager = new fun.eqad.ponyparkour.gui.GUIManager(this);
        this.parkourListener = new fun.eqad.ponyparkour.listener.ParkourListener(this);
        this.commandManager = new CommandManager(this);
        
        getCommand("parkour").setExecutor(commandManager);
        getCommand("parkour").setTabCompleter(commandManager);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new fun.eqad.ponyparkour.papi.ExpansionManager(this).register();
            getLogger().info("PlaceholderAPI hook registered!");
        } else {
            getLogger().warning("PlaceholderAPI not found, placeholders will not work.");
        }
    }

    public static PonyParkour getInstance() {
        return instance;
    }

    public ParkourManager getParkourManager() {
        return parkourManager;
    }

    public fun.eqad.ponyparkour.gui.GUIManager getGuiManager() {
        return guiManager;
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveArenas();
        }
    }

    public fun.eqad.ponyparkour.config.ConfigManager getConfigManager() {
        return configManager;
    }

    public fun.eqad.ponyparkour.data.DataManager getDataManager() {
        return dataManager;
    }
}
