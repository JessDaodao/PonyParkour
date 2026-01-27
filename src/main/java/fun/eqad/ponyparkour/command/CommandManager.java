package fun.eqad.ponyparkour.command;

import fun.eqad.ponyparkour.PonyParkour;
import fun.eqad.ponyparkour.arena.ParkourArena;
import fun.eqad.ponyparkour.manager.ParkourManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
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
                    player.sendMessage(prefix + "§c用法: /parkour delete <arena|checkpoint|start|end> [名称]");
                    return true;
                }
                String deleteType = args[1].toLowerCase();
                
                if (deleteType.equals("arena")) {
                    if (args.length < 3) {
                        player.sendMessage(prefix + "§c用法: /parkour delete arena <名称>");
                        return true;
                    }
                    parkourManager.deleteArena(args[2]);
                    player.sendMessage(prefix + "§a跑酷地图 " + args[2] + " 已删除");
                    return true;
                }

                if (args.length < 3) {
                    player.sendMessage(prefix + "§c用法: /parkour delete " + deleteType + " <名称>");
                    return true;
                }

                String arenaNameDel = args[2];
                ParkourArena arenaDel = parkourManager.getArena(arenaNameDel);
                if (arenaDel == null) {
                    player.sendMessage(prefix + "§c未找到该跑酷地图");
                    return true;
                }

                switch (deleteType) {
                    case "start":
                        if (arenaDel.getStartLocation() != null) {
                            arenaDel.getStartLocation().getBlock().setType(Material.AIR);
                            arenaDel.setStartLocation(null);
                            removeHolograms(arenaDel, "start");
                            player.sendMessage(prefix + "§a已删除 " + arenaNameDel + " 的起点");
                        } else {
                            player.sendMessage(prefix + "§c该地图未设置起点");
                        }
                        break;
                    case "end":
                        if (arenaDel.getEndLocation() != null) {
                            arenaDel.getEndLocation().getBlock().setType(Material.AIR);
                            arenaDel.setEndLocation(null);
                            removeHolograms(arenaDel, "end");
                            player.sendMessage(prefix + "§a已删除 " + arenaNameDel + " 的终点");
                        } else {
                            player.sendMessage(prefix + "§c该地图未设置终点");
                        }
                        break;
                    case "checkpoint":
                        if (args.length < 4) {
                            player.sendMessage(prefix + "§c用法: /parkour delete checkpoint <名称> <编号>");
                            return true;
                        }
                        try {
                            int index = Integer.parseInt(args[3]) - 1;
                            if (index >= 0 && index < arenaDel.getCheckpoints().size()) {
                                Location cpLoc = arenaDel.getCheckpoints().get(index);
                                cpLoc.getBlock().setType(Material.AIR);
                                arenaDel.getCheckpoints().remove(index);
                                removeHolograms(arenaDel, "checkpoint_" + index);
                                
                                for (int i = index + 1; i <= arenaDel.getCheckpoints().size(); i++) {
                                    List<java.util.UUID> uuids = arenaDel.removePointHolograms("checkpoint_" + i);
                                    if (uuids != null) {
                                        for (java.util.UUID uuid : uuids) {
                                            arenaDel.addPointHologram("checkpoint_" + (i - 1), uuid);
                                            
                                            org.bukkit.entity.Entity entity = org.bukkit.Bukkit.getEntity(uuid);
                                            if (entity != null && entity.getCustomName() != null && entity.getCustomName().equals(String.valueOf(i + 1))) {
                                                entity.setCustomName(String.valueOf(i));
                                            }
                                        }
                                    }
                                }
                                
                                player.sendMessage(prefix + "§a已删除 " + arenaNameDel + " 的检查点 " + (index + 1));
                            } else {
                                player.sendMessage(prefix + "§c无效的检查点编号");
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage(prefix + "§c检查点编号必须是数字");
                        }
                        break;
                    default:
                        player.sendMessage(prefix + "§c用法: /parkour delete <arena|checkpoint|start|end> [名称]");
                        break;
                }
                break;
            case "set":
                if (args.length < 2) {
                    player.sendMessage(prefix + "§c用法: /parkour set <lobby|start|end|checkpoint|icon|fall|author> [名称]");
                    return true;
                }
                String setType = args[1].toLowerCase();

                if (setType.equals("lobby")) {
                    plugin.getDataManager().setLobbyLocation(player.getLocation());
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
                        Location startLoc = player.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);
                        startLoc.setYaw(player.getLocation().getYaw());
                        startLoc.setPitch(player.getLocation().getPitch());
                        arena.setStartLocation(startLoc);
                        startLoc.getBlock().setType(Material.OAK_PRESSURE_PLATE);
                        spawnHologram(startLoc, "§a[起点]", arenaName, arena, "start");
                        player.sendMessage(prefix + "§a已设置 " + arenaName + " 的起点");
                        break;
                    case "end":
                        Location endLoc = player.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);
                        endLoc.setYaw(player.getLocation().getYaw());
                        endLoc.setPitch(player.getLocation().getPitch());
                        arena.setEndLocation(endLoc);
                        endLoc.getBlock().setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
                        spawnHologram(endLoc, "§c[终点]", arenaName, arena, "end");
                        player.sendMessage(prefix + "§a已设置 " + arenaName + " 的终点");
                        break;
                    case "checkpoint":
                        int index = -1;
                        if (args.length > 3) {
                            try {
                                index = Integer.parseInt(args[3]) - 1;
                            } catch (NumberFormatException e) {
                                player.sendMessage(prefix + "§c检查点编号必须是数字");
                                return true;
                            }
                        }

                        Location cpLoc = player.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);
                        cpLoc.setYaw(player.getLocation().getYaw());
                        cpLoc.setPitch(player.getLocation().getPitch());

                        if (index >= 0) {
                            if (index < arena.getCheckpoints().size()) {
                                arena.getCheckpoints().set(index, cpLoc);
                                cpLoc.getBlock().setType(Material.STONE_PRESSURE_PLATE);
                                spawnHologram(cpLoc, "§b[检查点]", String.valueOf(index + 1), arena, "checkpoint_" + index);
                                player.sendMessage(prefix + "§a已更新 " + arenaName + " 的检查点 " + (index + 1));
                            } else if (index == arena.getCheckpoints().size()) {
                                arena.addCheckpoint(cpLoc);
                                cpLoc.getBlock().setType(Material.STONE_PRESSURE_PLATE);
                                spawnHologram(cpLoc, "§b[检查点]", String.valueOf(index + 1), arena, "checkpoint_" + index);
                                player.sendMessage(prefix + "§a已为 " + arenaName + " 添加检查点 " + (index + 1));
                            } else {
                                player.sendMessage(prefix + "§c检查点编号不连续，当前最大编号: " + arena.getCheckpoints().size());
                            }
                        } else {
                            int newIndex = arena.getCheckpoints().size();
                            arena.addCheckpoint(cpLoc);
                            cpLoc.getBlock().setType(Material.STONE_PRESSURE_PLATE);
                            spawnHologram(cpLoc, "§b[检查点]", String.valueOf(newIndex + 1), arena, "checkpoint_" + newIndex);
                            player.sendMessage(prefix + "§a已为 " + arenaName + " 添加检查点");
                        }
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
                    case "fall":
                        int fallY;
                        if (args.length >= 4) {
                            try {
                                fallY = Integer.parseInt(args[3]);
                            } catch (NumberFormatException e) {
                                player.sendMessage(prefix + "§cY坐标必须是整数");
                                return true;
                            }
                        } else {
                            fallY = player.getLocation().getBlockY();
                        }
                        arena.setFallY(fallY);
                        player.sendMessage(prefix + "§a已设置 " + arenaName + " 的掉落高度为 " + fallY);
                        break;
                    case "author":
                        if (args.length < 4) {
                            player.sendMessage(prefix + "§c用法: /parkour set author <名称> <作者>");
                            return true;
                        }
                        String author = args[3];
                        if (args.length > 4) {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 3; i < args.length; i++) {
                                sb.append(args[i]).append(" ");
                            }
                            author = sb.toString().trim();
                        }
                        arena.setAuthor(author);
                        player.sendMessage(prefix + "§a已设置 " + arenaName + " 的作者为 " + author);
                        break;
                    default:
                        player.sendMessage(prefix + "§c用法: /parkour set <lobby|start|end|checkpoint|icon|fall|author> [名称]");
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
            sender.sendMessage(" §7/pk delete <参数> <名称> §8- §7删除地图或地图点位");
            sender.sendMessage(" §7/pk set <参数> <名称> §8- §7设置地图点位");
            sender.sendMessage(" §7/pk join <名称> §8- §7加入跑酷");
            sender.sendMessage(" §7/pk leave §8- §7离开跑酷");
            sender.sendMessage(" §7/pk gui §8- §7打开地图列表");
            sender.sendMessage(" §7/pk reload §8- §7重载配置");
            sender.sendMessage(" §7/pk about §8- §7关于插件");
        } else {
            sender.sendMessage(" §7/parkour create <名称> §8- §7创建跑酷地图");
            sender.sendMessage(" §7/parkour delete <参数> <名称> §8- §7删除地图或地图点位");
            sender.sendMessage(" §7/parkour set <参数> <名称> §8- §7设置地图点位");
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
            if (subCommand.equals("join")) {
                return StringUtil.copyPartialMatches(args[1], new ArrayList<>(parkourManager.getArenas().keySet()), new ArrayList<>());
            } else if (subCommand.equals("delete")) {
                return StringUtil.copyPartialMatches(args[1], Arrays.asList("arena", "checkpoint", "start", "end"), new ArrayList<>());
            } else if (subCommand.equals("set")) {
                return StringUtil.copyPartialMatches(args[1], Arrays.asList("checkpoint", "start", "end", "lobby", "icon", "fall", "author"), new ArrayList<>());
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("delete")) {
                if (args[1].equalsIgnoreCase("lobby")) return completions;
                return StringUtil.copyPartialMatches(args[2], new ArrayList<>(parkourManager.getArenas().keySet()), new ArrayList<>());
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("set") && args[1].equalsIgnoreCase("fall")) {
                if (sender instanceof Player) {
                    completions.add(String.valueOf(((Player) sender).getLocation().getBlockY()));
                }
                return completions;
            }
            if ((args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("delete")) && args[1].equalsIgnoreCase("checkpoint")) {
                String arenaName = args[2];
                ParkourArena arena = parkourManager.getArena(arenaName);
                if (arena != null) {
                    for (int i = 1; i <= arena.getCheckpoints().size(); i++) {
                        completions.add(String.valueOf(i));
                    }
                    if (args[0].equalsIgnoreCase("set")) {
                        completions.add(String.valueOf(arena.getCheckpoints().size() + 1));
                    }
                    return StringUtil.copyPartialMatches(args[3], completions, new ArrayList<>());
                }
            }
        }
        
        Collections.sort(completions);
        return completions;
    }

    private void spawnHologram(Location location, String line1, String line2, ParkourArena arena, String key) {
        removeHolograms(arena, key);

        double x = location.getBlockX() + 0.5;
        double y = location.getBlockY() + 1.5;
        double z = location.getBlockZ() + 0.5;
        Location holoLoc = new Location(location.getWorld(), x, y, z);
        
        ArmorStand as1 = (ArmorStand) location.getWorld().spawnEntity(holoLoc.clone().add(0, 0.3, 0), EntityType.ARMOR_STAND);
        as1.setCustomName(line1);
        as1.setCustomNameVisible(true);
        as1.setGravity(false);
        as1.setVisible(false);
        as1.setMarker(true);
        as1.setSmall(true);
        arena.addPointHologram(key, as1.getUniqueId());

        ArmorStand as2 = (ArmorStand) location.getWorld().spawnEntity(holoLoc, EntityType.ARMOR_STAND);
        as2.setCustomName(line2);
        as2.setCustomNameVisible(true);
        as2.setGravity(false);
        as2.setVisible(false);
        as2.setMarker(true);
        as2.setSmall(true);
        arena.addPointHologram(key, as2.getUniqueId());
    }

    private void removeHolograms(ParkourArena arena, String key) {
        List<java.util.UUID> uuids = arena.removePointHolograms(key);
        if (uuids != null) {
            for (java.util.UUID uuid : uuids) {
                org.bukkit.entity.Entity entity = org.bukkit.Bukkit.getEntity(uuid);
                if (entity != null) {
                    entity.remove();
                }
            }
        }
    }
}
