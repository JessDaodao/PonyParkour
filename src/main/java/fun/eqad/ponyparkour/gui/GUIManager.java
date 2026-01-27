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
        int size = 54;

        Inventory gui = Bukkit.createInventory(null, size, GUI_TITLE);

        ItemStack borderItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = borderItem.getItemMeta();
        if (borderMeta != null) {
            borderMeta.setDisplayName(" ");
            borderItem.setItemMeta(borderMeta);
        }

        for (int i = 0; i < 9; i++) {
            gui.setItem(i, borderItem);
            gui.setItem(size - 9 + i, borderItem);
        }

        for (int i = 1; i < 5; i++) {
            gui.setItem(i * 9, borderItem);
            gui.setItem(i * 9 + 8, borderItem);
        }

        int index = 10;
        for (ParkourArena arena : arenas) {
            while (index < size && (index % 9 == 0 || index % 9 == 8)) {
                index++;
            }
            if (index >= size - 9) break;

            ItemStack item = new ItemStack(arena.getIcon());
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + arena.getName());
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "作者: " + ChatColor.YELLOW + arena.getAuthor());
                lore.add("");
                lore.add(ChatColor.GRAY + "点击游玩!");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            gui.setItem(index, item);
            index++;
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
