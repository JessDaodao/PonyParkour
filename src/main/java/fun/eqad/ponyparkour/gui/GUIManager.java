package fun.eqad.ponyparkour.gui;

import fun.eqad.ponyparkour.PonyParkour;
import fun.eqad.ponyparkour.arena.ParkourArena;
import fun.eqad.ponyparkour.manager.ParkourManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GUIManager implements Listener {

    private final PonyParkour plugin;
    private final ParkourManager parkourManager;
    private final String GUI_TITLE = ChatColor.DARK_PURPLE + "跑酷地图";

    public GUIManager(PonyParkour plugin) {
        this.plugin = plugin;
        this.parkourManager = plugin.getParkourManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openMapSelector(Player player) {
        List<ParkourArena> arenas = new ArrayList<>(parkourManager.getArenas().values());
        int size = (int) Math.ceil(arenas.size() / 9.0) * 9;
        if (size == 0) size = 9;
        if (size > 54) size = 54;

        Inventory gui = Bukkit.createInventory(null, size, GUI_TITLE);

        for (ParkourArena arena : arenas) {
            ItemStack item = new ItemStack(arena.getIcon());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + arena.getName());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "点击加入!");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            gui.addItem(item);
        }

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        ItemMeta meta = clickedItem.getItemMeta();

        if (meta != null && meta.hasDisplayName()) {
            String arenaName = ChatColor.stripColor(meta.getDisplayName());
            ParkourArena arena = parkourManager.getArena(arenaName);

            if (arena != null) {
                if (arena.getStartLocation() != null) {
                    player.closeInventory();
                    parkourManager.startSession(player, arena);
                } else {
                    player.sendMessage(plugin.getConfigManager().getPrefix() + "§c该跑酷地图尚未准备好");
                }
            }
        }
    }
}
