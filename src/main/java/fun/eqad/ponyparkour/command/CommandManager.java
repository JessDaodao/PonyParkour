package fun.eqad.ponyparkour.command;

import fun.eqad.ponyparkour.PonyParkour;
import fun.eqad.ponyparkour.arena.ParkourArena;
import fun.eqad.ponyparkour.manager.ParkourManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandManager implements CommandExecutor, TabCompleter {

    private final PonyParkour plugin;
    private final ParkourManager parkourManager;

    public CommandManager(PonyParkour plugin) {
        this.plugin = plugin;
        this.parkourManager = plugin.getParkourManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = plugin.getConfigManager().getPrefix();

        if (!(sender instanceof Player)) {
            sender.sendMessage(prefix + "§c只有玩家可以使用此命令");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            help(player, label);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                if (args.length < 2) {
                    player.sendMessage(prefix + "§c用法: /parkour create <名称>");
                    return true;
                }
                parkourManager.createArena(args[1]);
                player.sendMessage(prefix + "§a跑酷地图 " + args[1] + " 已创建");
                break;
            case "delete":
                if (args.length < 2) {
                    player.sendMessage(prefix + "§c用法: /parkour delete <名称>");
                    return true;
                }
                parkourManager.deleteArena(args[1]);
                player.sendMessage(prefix + "§a跑酷地图 " + args[1] + " 已删除");
                break;
            case "set":
                if (args.length < 2) {
                    player.sendMessage(prefix + "§c用法: /parkour set <lobby|start|end|checkpoint|icon> [名称]");
                    return true;
                }
                String setType = args[1].toLowerCase();

                if (setType.equals("lobby")) {
                    plugin.getConfigManager().setLobbyLocation(player.getLocation());
                    player.sendMessage(prefix + "§a大厅位置已设置");
                    return true;
                }

                if (args.length < 3) {
                    player.sendMessage(prefix + "§c用法: /parkour set " + setType + " <名称>");
                    return true;
                }

                String arenaName = args[2];
                ParkourArena arena = parkourManager.getArena(arenaName);
                if (arena == null) {
                    player.sendMessage(prefix + "§c未找到该跑酷地图");
                    return true;
                }

                switch (setType) {
                    case "start":
                        arena.setStartLocation(player.getLocation());
                        player.sendMessage(prefix + "§a已设置 " + arenaName + " 的起点");
                        break;
                    case "end":
                        arena.setEndLocation(player.getLocation());
                        player.sendMessage(prefix + "§a已设置 " + arenaName + " 的终点");
                        break;
                    case "checkpoint":
                        arena.addCheckpoint(player.getLocation());
                        player.sendMessage(prefix + "§a已为 " + arenaName + " 添加检查点");
                        break;
                    case "icon":
                        Material itemInHand = player.getInventory().getItemInMainHand().getType();
                        if (itemInHand == Material.AIR) {
                            player.sendMessage(prefix + "§c你必须手持一个物品来设置图标");
                            return true;
                        }
                        arena.setIcon(itemInHand);
                        player.sendMessage(prefix + "§a已设置 " + arenaName + " 的图标");
                        break;
                    default:
                        player.sendMessage(prefix + "§c用法: /parkour set <lobby|start|end|checkpoint|icon> [名称]");
                        break;
                }
                break;
            case "join":
                if (args.length < 2) {
                    player.sendMessage(prefix + "§c用法: /parkour join <名称>");
                    return true;
                }
                ParkourArena arenaJoin = parkourManager.getArena(args[1]);
                if (arenaJoin == null) {
                    player.sendMessage(prefix + "§c未找到该跑酷地图");
                    return true;
                }
                if (arenaJoin.getStartLocation() == null) {
                    player.sendMessage(prefix + "§c该跑酷地图未设置起点");
                    return true;
                }
                parkourManager.startSession(player, arenaJoin);
                break;
            case "leave":
                parkourManager.endSession(player);
                break;
            case "gui":
                plugin.getGuiManager().openMapSelector(player);
                break;
            case "reload":
                if (!player.hasPermission("ponyparkour.admin")) {
                    player.sendMessage(prefix + "§c你没有执行该命令的权限");
                    return true;
                }
                plugin.getConfigManager().loadConfig();
                plugin.getDataManager().loadArenas();
                player.sendMessage(prefix + "§a配置重载成功");
                break;
            case "about":
                about(player);
                break;
            default:
                help(player, label);
                break;
        }

        return true;
    }

    private void help(CommandSender sender, String label) {
        boolean aliases = label.equalsIgnoreCase("pk");
        String prefix = plugin.getConfigManager().getPrefix();

        sender.sendMessage(prefix + "§7PonyParkour帮助:");
        if (aliases) {
            sender.sendMessage(" §7/pk create <名称> §8- §7创建跑酷地图");
            sender.sendMessage(" §7/pk delete <名称> §8- §7删除跑酷地图");
            sender.sendMessage(" §7/pk set <start|end|checkpoint|icon|lobby> <名称> §8- §7设置地图属性");
            sender.sendMessage(" §7/pk join <名称> §8- §7加入跑酷");
            sender.sendMessage(" §7/pk leave §8- §7离开跑酷");
            sender.sendMessage(" §7/pk gui §8- §7打开地图列表");
            sender.sendMessage(" §7/pk reload §8- §7重载配置");
            sender.sendMessage(" §7/pk about §8- §7关于插件");
        } else {
            sender.sendMessage(" §7/parkour create <名称> §8- §7创建跑酷地图");
            sender.sendMessage(" §7/parkour delete <名称> §8- §7删除跑酷地图");
            sender.sendMessage(" §7/parkour set <start|end|checkpoint|icon|lobby> <名称> §8- §7设置地图属性");
            sender.sendMessage(" §7/parkour join <名称> §8- §7加入跑酷");
            sender.sendMessage(" §7/parkour leave §8- §7离开跑酷");
            sender.sendMessage(" §7/parkour gui §8- §7打开地图列表");
            sender.sendMessage(" §7/parkour reload §8- §7重载配置");
            sender.sendMessage(" §7/parkour about §8- §7关于插件");
        }
    }

    private void about(CommandSender sender) {
        String prefix = plugin.getConfigManager().getPrefix();
        sender.sendMessage(prefix + "§7关于PonyParkour:");
        sender.sendMessage(" §7现代化跑酷插件");
        sender.sendMessage(" §7版本 §8- §7" + plugin.getDescription().getVersion());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> commands = new ArrayList<>();
            if ("create".startsWith(args[0].toLowerCase())) commands.add("create");
            if ("delete".startsWith(args[0].toLowerCase())) commands.add("delete");
            if ("set".startsWith(args[0].toLowerCase())) commands.add("set");
            if ("join".startsWith(args[0].toLowerCase())) commands.add("join");
            if ("leave".startsWith(args[0].toLowerCase())) commands.add("leave");
            if ("gui".startsWith(args[0].toLowerCase())) commands.add("gui");
            if ("reload".startsWith(args[0].toLowerCase())) commands.add("reload");
            if ("about".startsWith(args[0].toLowerCase())) commands.add("about");
            return commands;
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (Arrays.asList("delete", "join").contains(subCommand)) {
                return StringUtil.copyPartialMatches(args[1], new ArrayList<>(parkourManager.getArenas().keySet()), new ArrayList<>());
            } else if (subCommand.equals("set")) {
                return StringUtil.copyPartialMatches(args[1], Arrays.asList("checkpoint", "start", "end", "lobby", "icon"), new ArrayList<>());
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("set")) {
                return StringUtil.copyPartialMatches(args[2], new ArrayList<>(parkourManager.getArenas().keySet()), new ArrayList<>());
            }
        }
        
        Collections.sort(completions);
        return completions;
    }
}
