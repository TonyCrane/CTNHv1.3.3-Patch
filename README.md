# CTNH v1.3.3 Patch

[![Build tcpatch mod JAR](https://github.com/TonyCrane/CTNHv1.3.3-Patch/actions/workflows/build-jar.yml/badge.svg?branch=master)](https://github.com/TonyCrane/CTNHv1.3.3-Patch/actions/workflows/build-jar.yml)

针对 CTNH 整合包 v1.3.3 版本的补丁，无需修改其他 mod 即可修复已知问题，只需服务端安装（客户端可选），可通过 [Action](https://github.com/TonyCrane/CTNHv1.3.3-Patch/actions/workflows/build-jar.yml) 里最新 Runs 的 Artifacts 下载模组 .jar 文件。

本补丁修复的内容如下：

- 修复 GTMThings 巨型输入总成不适配可编程电路卡、在样板管理终端不显示电路编号的问题
- 修复 AE2OmniCell 存储元件在使用了溢出销毁卡后无法按预期销毁的问题
- 改进 GTMThings 数字型采矿机，允许通过矿石掉落的粗矿来进行筛选
- 修复 CTNH-Core 集成沉积工厂的多方块结构（如需 JEI 显示更新后的多方块结构，则需要客户端也安装本 mod）
- 修复 CTNH-Bio 生物机器的超频逻辑与实际耗电
    - 生物机器实际耗电和配方耗电同步（原来为始终耗导线上电压等级的 1A 电）
    - e.g. 给 MV 机器通 HV 电跑 MV 级别的配方可以超频一级到 HV 配方级别
- 延长 CTNH-Bio 缸中之脑机器自我怀疑随机判定周期至每秒一次，而非每个游戏刻一次
- 允许 CTNH-Core 太空光伏基站使用变电动力仓
- 修复 CTNH-Core 小硅岩发电机和火箭燃料发电机的发电逻辑，类似原版内燃发电机
