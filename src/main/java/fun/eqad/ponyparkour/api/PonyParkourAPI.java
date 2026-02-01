package fun.eqad.ponyparkour.api;

import fun.eqad.ponyparkour.arena.ParkourArena;
import fun.eqad.ponyparkour.arena.ParkourSession;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import java.util.Map;

public interface PonyParkourAPI {
    /**
     * 获取插件实例
     * @return PonyParkour插件实例
     */
    Plugin getPlugin();

    /**
     * 检查玩家是否正在进行跑酷
     * @param player 玩家
     * @return 如果玩家正在跑酷则返回true
     */
    boolean isPlaying(Player player);

    /**
     * 获取玩家的跑酷会话
     * @param player 玩家
     * @return 跑酷会话，如果玩家未在跑酷则返回null
     */
    ParkourSession getSession(Player player);

    /**
     * 获取指定名称的跑酷场地
     * @param name 场地名称
     * @return 跑酷场地，如果不存在则返回null
     */
    ParkourArena getArena(String name);

    /**
     * 获取所有跑酷场地
     * @return 场地名称到场地对象的映射
     */
    Map<String, ParkourArena> getArenas();

    /**
     * 让玩家加入跑酷场地
     * @param player 玩家
     * @param arenaName 场地名称
     * @return 如果加入成功则返回true
     */
    boolean joinArena(Player player, String arenaName);

    /**
     * 让玩家离开当前跑酷
     * @param player 玩家
     * @return 如果离开成功则返回true
     */
    boolean leaveArena(Player player);
}
