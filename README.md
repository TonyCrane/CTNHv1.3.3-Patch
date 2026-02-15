# CTNH v1.3.3 Patch

[![Build tcpatch mod JAR](https://github.com/TonyCrane/CTNHv1.3.3-Patch/actions/workflows/build-jar.yml/badge.svg?branch=master)](https://github.com/TonyCrane/CTNHv1.3.3-Patch/actions/workflows/build-jar.yml)

针对 CTNH 整合包 v1.3.3 版本的补丁，无需修改其他 mod 即可修复已知问题，只需服务端安装（客户端可选），可通过 [Action](https://github.com/TonyCrane/CTNHv1.3.3-Patch/actions/workflows/build-jar.yml) 里最新 Runs 的 Artifacts 下载模组 .jar 文件。

本补丁修复的内容如下：

- 修复 GTMThings 巨型输入总成不适配可编程电路卡、在样板管理终端不显示电路编号的问题
- 修复 AE2OmniCell 存储元件在使用了溢出销毁卡后无法按预期销毁的问题 ([AE2OmniCells#4](https://github.com/Frostbite-time/AE2OmniCells/pull/4))
- 改进 GTMThings 数字型采矿机，允许通过矿石掉落的粗矿来进行筛选 ([CTNH-Core#`c65ca6d`](https://github.com/CTNH-Team/CTNH-Core/commit/c65ca6ddee2763252ca3b07057a82ee5e4a7fc3a))
- 修复 CTNH-Core 集成沉积工厂的多方块结构（如需 JEI 显示更新后的多方块结构，则需要客户端也安装本 mod）([CTNH-Core#`56ee79d`](https://github.com/CTNH-Team/CTNH-Core/commit/56ee79d223f1deb13acf20fc6a677e08944cbc87))
- 修复 CTNH-Bio 生物机器的超频逻辑与实际耗电 ([CTNH-Bio#`160a4cc`](https://github.com/CTNH-Team/CTNH-Bio/commit/160a4cceeff47441bc9524cbcb5f828c73cce673)/[`6573e1f`](https://github.com/CTNH-Team/CTNH-Bio/commit/6573e1f12a9169c3363de15d9b739a564705a534))
    - 生物机器实际耗电和配方耗电同步（原来为始终耗导线上电压等级的 1A 电）
    - e.g. 给 MV 机器通 HV 电跑 MV 级别的配方可以超频一级到 HV 配方级别
- 延长 CTNH-Bio 缸中之脑机器自我怀疑随机判定周期至每秒一次，而非每个游戏刻一次
- 允许 CTNH-Core 太空光伏基站使用变电动力仓
- 修复 CTNH-Core 小硅岩发电机和火箭燃料发电机的发电逻辑，类似原版内燃发电机 ([CTNH-Core#`b0bad72`](https://github.com/CTNH-Team/CTNH-Core/commit/b0bad72084585710799d4d4175344bcdba2c17e8))
- 修复 CTNH-Core Jade 机器配方电压显示问题（根据最大仓室选择显示的电压）([CTNH-Core#`042fc4c`](https://github.com/CTNH-Team/CTNH-Core/commit/042fc4c4d485a978b9686fc9b24f726b664af8e4))
    - Credit. [GregTech-Modern#4002](https://github.com/GregTechCEu/GregTech-Modern/pull/4002)（在此基础上支持更通用的能源仓类型）
- 允许 CTNH-Core 恶魔意志发电机使用激光仓，不再检查普通动力仓数量 ([CTNH-Mana#`6184e09`](https://github.com/CTNH-Team/CTNH-Mana/commit/6184e0981902b93024ac87c5176eae88035a8801)/[`b7386aa`](https://github.com/CTNH-Team/CTNH-Mana/commit/b7386aacf87ff7181f1782386ab1edc8f6f63657))
- 优化 CTNH-Core 屠宰场机器运行逻辑，降低卡顿，优化输出显示，修复电压等级不影响物品输出的问题 ([CTNH-Core#65](https://github.com/CTNH-Team/CTNH-Core/pull/65))
- 修复了 ProgrammedCircuitCard 中无编程电路的样板默认电路为 0 的问题 ([CTNH-Energy#`6e10617`](https://github.com/CTNH-Team/CTNH-Energy/commit/6e106176d24629743dad70ccba0f0f3cef6efb02))
- 修复了 CTNH-Energy 高级样板总成自身电路无效的问题 ([CTNH-Energy#`6e10617`](https://github.com/CTNH-Team/CTNH-Energy/commit/6e106176d24629743dad70ccba0f0f3cef6efb02))
- 修复了 CTNH-Core 中子加速器性能占用过高的问题 ([CTNH-Core#`e42f2b1`](https://github.com/CTNH-Team/CTNH-Core/commit/e42f2b1d8e7e01a7c59145a110e48eb3fecdecc5))

## Kubejs Patches

一些 kubejs 脚本中的问题需要手动修改 kubejs 文件，根据 `kubejs/` 中的代码和开头的注释说明进行修改：

- [`lasersorder_fix.js`](kubejs/lasersorder_fix.js)：修复激光分配仪装配线配方中缺失输入的问题
- [`neutronium_fix.js`](kubejs/neutronium_fix.js)：将中子素的原版锭和块替换为 Avaritia 的版本，以修复中子素相关的配方问题
- [`additional_recipes.js`](kubejs/additional_recipes.js)：添加额外配方
    - 坠星操纵者消耗 T3 重型合金板产出三种外星矿物
