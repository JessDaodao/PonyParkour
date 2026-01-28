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
    private final String GUI_TITLE = ChatColor.DARK_GRAY + "跑酷地图";
    private final String SUB_MENU_TITLE_PREFIX = ChatColor.DARK_GRAY + "地图详情: ";
    private final String LEADERBOARD_TITLE_PREFIX = ChatColor.DARK_GRAY + "排行榜: ";

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
                lore.add(ChatColor.GRAY + "制作人员: " + ChatColor.YELLOW + arena.getAuthor());
                lore.add("");
                lore.add(ChatColor.GRAY + "点击查看详情");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            gui.setItem(index, item);
            index++;
        }

        player.openInventory(gui);
    }

    public void openSubMenu(Player player, ParkourArena arena) {
        Inventory gui = Bukkit.createInventory(null, 27, SUB_MENU_TITLE_PREFIX + arena.getName());

        ItemStack borderItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = borderItem.getItemMeta();
        if (borderMeta != null) {
            borderMeta.setDisplayName(" ");
            borderItem.setItemMeta(borderMeta);
        }

        for (int i = 0; i < 27; i++) {
            if (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) {
                gui.setItem(i, borderItem);
            }
        }

        ItemStack playItem = new ItemStack(Material.DIAMOND_BOOTS);
        ItemMeta playMeta = playItem.getItemMeta();
        playMeta.setDisplayName(ChatColor.GREEN + "开始游玩");
        playItem.setItemMeta(playMeta);

        ItemStack leaderboardItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta leaderboardMeta = leaderboardItem.getItemMeta();
        leaderboardMeta.setDisplayName(ChatColor.GOLD + "查看排行榜");
        leaderboardItem.setItemMeta(leaderboardMeta);

        ItemStack backItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "返回上一级");
        backItem.setItemMeta(backMeta);

        gui.setItem(11, playItem);
        gui.setItem(15, leaderboardItem);
        gui.setItem(18, backItem);

        player.openInventory(gui);
    }

    public void openLeaderboard(Player player, ParkourArena arena) {
        Inventory gui = Bukkit.createInventory(null, 54, LEADERBOARD_TITLE_PREFIX + arena.getName());

        ItemStack borderItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = borderItem.getItemMeta();
        if (borderMeta != null) {
            borderMeta.setDisplayName(" ");
            borderItem.setItemMeta(borderMeta);
        }

        for (int i = 0; i < 9; i++) {
            gui.setItem(i, borderItem);
            gui.setItem(54 - 9 + i, borderItem);
        }

        for (int i = 1; i < 5; i++) {
            gui.setItem(i * 9, borderItem);
            gui.setItem(i * 9 + 8, borderItem);
        }
        
        java.util.Map<java.util.UUID, Double> records = plugin.getDataManager().getArenaRecords(arena.getName());
        List<java.util.Map.Entry<java.util.UUID, Double>> sortedRecords = new ArrayList<>(records.entrySet());
        sortedRecords.sort(java.util.Map.Entry.comparingByValue());

        int index = 10;
        int rank = 1;
        for (java.util.Map.Entry<java.util.UUID, Double> entry : sortedRecords) {
            while (index < 54 && (index % 9 == 0 || index % 9 == 8)) {
                index++;
            }
            if (index >= 54 - 9) break;

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            org.bukkit.inventory.meta.SkullMeta meta = (org.bukkit.inventory.meta.SkullMeta) head.getItemMeta();
            
            org.bukkit.OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(entry.getKey());
            meta.setOwningPlayer(offlinePlayer);
            meta.setDisplayName(ChatColor.GOLD + "#" + rank + " " + ChatColor.YELLOW + (offlinePlayer.getName() != null ? offlinePlayer.getName() : "未知玩家"));
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "用时: " + ChatColor.GREEN + entry.getValue() + "秒");
            meta.setLore(lore);
            
            head.setItemMeta(meta);
            gui.setItem(index, head);
            index++;
            rank++;
        }

        ItemStack backItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta backMeta = backItem.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "返回上一级");
        backItem.setItemMeta(backMeta);
        gui.setItem(45, backItem);

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (title.equals(GUI_TITLE)) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();
            ItemMeta meta = clickedItem.getItemMeta();

            if (meta != null && meta.hasDisplayName()) {
                String arenaName = ChatColor.stripColor(meta.getDisplayName());
                ParkourArena arena = parkourManager.getArena(arenaName);

                if (arena != null) {
                    openSubMenu(player, arena);
                }
            }
        } else if (title.startsWith(SUB_MENU_TITLE_PREFIX)) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

            Player player = (Player) event.getWhoClicked();
            String arenaName = title.substring(SUB_MENU_TITLE_PREFIX.length());
            ParkourArena arena = parkourManager.getArena(arenaName);

            if (arena == null) {
                player.closeInventory();
                return;
            }

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem.getType() == Material.DIAMOND_BOOTS) {
                if (arena.getStartLocation() != null) {
                    player.closeInventory();
                    parkourManager.startSession(player, arena);
                } else {
                    player.sendMessage(plugin.getConfigManager().getPrefix() + "§c该跑酷地图尚未准备好");
                }
            } else if (clickedItem.getType() == Material.GOLD_INGOT) {
                openLeaderboard(player, arena);
            } else if (clickedItem.getType() == Material.RED_STAINED_GLASS_PANE && clickedItem.getItemMeta().getDisplayName().contains("返回")) {
                openMapSelector(player);
            }
        } else if (title.startsWith(LEADERBOARD_TITLE_PREFIX)) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem.getType() == Material.RED_STAINED_GLASS_PANE && clickedItem.getItemMeta().getDisplayName().contains("返回")) {
                Player player = (Player) event.getWhoClicked();
                String arenaName = title.substring(LEADERBOARD_TITLE_PREFIX.length());
                ParkourArena arena = parkourManager.getArena(arenaName);
                if (arena != null) {
                    openSubMenu(player, arena);
                } else {
                    openMapSelector(player);
                }
            }
        }
    }
}
