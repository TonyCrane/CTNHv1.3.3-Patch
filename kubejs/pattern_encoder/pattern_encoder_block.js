// 放到 kubejs/startup_scripts/ 下，服务器需要所有客户端都加这个文件，不然会报错

StartupEvents.registry('block', event => {
  event
    .create('blueprint_pattern_encoder')
    .displayName('蓝图样板生成器')
    .soundType('metal')
    .mapColor('metal')
    .tagBlock('mineable/pickaxe')
    .requiresTool(true)
    .textureAll('ae2:block/cell_workbench')
})
