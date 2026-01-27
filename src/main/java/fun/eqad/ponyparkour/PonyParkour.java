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
    private fun.eqad.ponyparkour.block.BlockEvent blockEvent;
    private fun.eqad.ponyparkour.manager.GhostBlockManager ghostBlockManager;
    private fun.eqad.ponyparkour.config.ConfigManager configManager;
    private fun.eqad.ponyparkour.data.DataManager dataManager;

    @Override
    public void onEnable() {
        getLogger().info("   ___                    ___           _");
        getLogger().info("  / _ \\___  _ __  _   _  / _ \\__ _ _ __| | _____  _   _ _ __");
        getLogger().info(" / /_)/ _ \\| '_ \\| | | |/ /_)/ _` | '__| |/ / _ \\| | | | '__|");
        getLogger().info("/ ___/ (_) | | | | |_| / ___/ (_| | |  |   < (_) | |_| | |");
        getLogger().info("\\/    \\___/|_| |_|\\__, \\/    \\__,_|_|  |_|\\_\\___/ \\__,_|_|");
        getLogger().info("                  |___/");
        getLogger().info("草, 走, 忽略");
        getLogger().info("Author: EQAD Network");

        instance = this;
        this.parkourManager = new ParkourManager();
        this.configManager = new fun.eqad.ponyparkour.config.ConfigManager(this);
        this.dataManager = new fun.eqad.ponyparkour.data.DataManager(this);
        this.dataManager.loadArenas();
        this.guiManager = new fun.eqad.ponyparkour.gui.GUIManager(this);
        this.parkourListener = new fun.eqad.ponyparkour.listener.ParkourListener(this);
        this.blockEvent = new fun.eqad.ponyparkour.block.BlockEvent(this);
        this.ghostBlockManager = new fun.eqad.ponyparkour.manager.GhostBlockManager(this);
        this.commandManager = new CommandManager(this);
        
        getCommand("parkour").setExecutor(commandManager);
        getCommand("parkour").setTabCompleter(commandManager);

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new fun.eqad.ponyparkour.papi.ExpansionManager(this).register();
            getLogger().info("检测到PlaceholderAPI, 已启用相关支持");
        }

        getLogger().info("PonyParkour已成功加载");
    }

    @Override
    public void onDisable() {
        if (ghostBlockManager != null) {
            ghostBlockManager.restoreAllBlocks();
        }
        if (dataManager != null) {
            dataManager.saveArenas();
        }

        getLogger().info("PonyParkour已成功卸载");
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
    public fun.eqad.ponyparkour.config.ConfigManager getConfigManager() {
        return configManager;
    }
    public fun.eqad.ponyparkour.data.DataManager getDataManager() {
        return dataManager;
    }
    public fun.eqad.ponyparkour.manager.GhostBlockManager getGhostBlockManager() {
        return ghostBlockManager;
    }
}
