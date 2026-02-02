<div align="center">

# PonyParkour - 现代化跑酷插件

</div>

### 简介

一个轻量化的跑酷插件

注：史山，请捏住鼻子查看源代码

### 命令

- `/pk create <名称>` 创建跑酷地图
- `/pk delete <参数> <名称>` 删除地图或地图点位
- `/pk set <参数> <名称>` 设置地图点位
- `/pk join <名称>` 加入跑酷
- `/pk leave` 离开跑酷
- `/pk gui` 打开地图列表
- `/pk reload` 重载配置文件

### 权限节点

- `ponyparkour.admin` 管理员命令

### PAPI变量

- `%ponyparkour_arena%` 跑酷场地
- `%ponyparkour_time_[ani]%` 跑酷时间
- `%ponyparkour_checkpoint_[ani]%` 跑酷检查点
- `%ponyparkour_rank_<arena>_<number>_[full|time|name]%` 跑酷排行

### API接入

查看[示例插件](https://github.com/JessDaodao/PonyParkourAPI_Example)

### 配置文件

```yaml
# PonyParkour配置文件

messages:
  # 插件消息前缀
  prefix: "&8[&bPonyParkour&8]&r "

settings:
  block:
    # 是否启用跑酷特殊方块
    enable: true
```