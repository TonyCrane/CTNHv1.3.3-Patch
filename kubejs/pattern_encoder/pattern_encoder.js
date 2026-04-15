// 放到 kubejs/server_scripts/ 下

var BLOCK_ID = 'kubejs:blueprint_pattern_encoder'
var CLIPBOARD_ID = 'create:clipboard'
var BLANK_PATTERN_ID = 'ae2:blank_pattern'
var OUT_PATTERN_ID = 'ae2:processing_pattern'

function forEachList(list, fn) {
  if (!list) return
  if (typeof list.forEach === 'function') {
    list.forEach(fn)
    return
  }
  if (typeof list.length === 'number') {
    for (var i = 0; i < list.length; i++) fn(list[i], i)
    return
  }
  if (typeof list.size === 'function' && typeof list.get === 'function') {
    for (var j = 0; j < list.size(); j++) fn(list.get(j), j)
    return
  }
  if (typeof list.iterator === 'function') {
    var it = list.iterator()
    var k = 0
    while (it.hasNext()) fn(it.next(), k++)
  }
}

/**
 * 从 Create 蓝图加农炮的材料清单剪贴板里提取 (id, count)。
 * 重点：已勾选的条目 ItemAmount 可能为 0，但 Text 里仍包含总需求（例如 "\\n x2"）。
 */
function readChecklistInputs(clipboardItem) {
  var nbt = clipboardItem && clipboardItem.nbt
  var pages = nbt && nbt.Pages
  if (!pages) return []

  /** @type {{id: string, count: number}[]} */
  var inputs = []

  var extractCountFromText = function (text) {
    if (!text) return null
    var s = String(text)
    var m = /\\n x(\d+)/.exec(s) || /\n x(\d+)/.exec(s)
    return m ? Number(m[1]) : null
  }

  // Rhino 环境下 for...of/const 容易出作用域问题，这里统一走兼容遍历
  forEachList(pages, function (page) {
    var entries = page && page.Entries
    if (!entries) return

    forEachList(entries, function (entry) {
      var icon = entry && entry.Icon
      if (!icon) return
      var id = String(icon.id)
      if (!id || id === 'minecraft:air') return

      var fromText = extractCountFromText(entry && entry.Text)
      var fromAmount = entry && entry.ItemAmount != null ? Number(entry.ItemAmount) : null
      var count = fromText != null ? fromText : (fromAmount != null ? fromAmount : 1)

      if (!Number.isFinite(count) || count <= 0) return
      inputs.push({ id: id, count: Math.floor(count) })
    })
  })

  // 合并同类项（同 id）
  /** @type {Record<string, number>} */
  var merged = {}
  forEachList(inputs, function (it) {
    merged[it.id] = (merged[it.id] || 0) + it.count
  })

  /** @type {{id: string, count: number}[]} */
  var out = []
  for (var k in merged) {
    out.push({ id: String(k), count: Number(merged[k]) })
  }
  return out
}

function escapeForTextComponent(s) {
  return String(s)
    .replace(/\\/g, '\\\\')
    .replace(/\"/g, '\\"')
    // 这里的文本会被塞进 Name:'{"text":"..."}' 的单引号字符串里，单引号会直接把 SNBT 截断
    .replace(/'/g, '')
    .replace(/\n/g, '')
    .replace(/\r/g, '')
    .replace(/\t/g, ' ')
}

function buildProcessingPatternSnbt(inputs, paperName) {
  var inParts = []
  forEachList(inputs, function (it) {
    // AE2 processing pattern 使用 "#"(Long) + "#c"("ae2:i") 来表示数量
    inParts.push('{"#":' + it.count + 'L,"#c":"ae2:i",id:"' + it.id + '"}')
  })
  while (inParts.length < 81) inParts.push('{}')
  if (inParts.length > 81) inParts.length = 81

  // 输出保持为普通纸张（不附加 tag / Name），避免客户端名称渲染问题
  var out0 = '{"#":1L,"#c":"ae2:i",id:"minecraft:paper"}'

  var outParts = [out0]
  while (outParts.length < 27) outParts.push('{}')

  return '{in:[' + inParts.join(',') + '],out:[' + outParts.join(',') + ']}'
}

function findClipboardInHands(player) {
  var main = player.mainHandItem
  var off = player.offHandItem
  if (main && main.id === CLIPBOARD_ID) return { item: main, hand: 'MAIN_HAND' }
  if (off && off.id === CLIPBOARD_ID) return { item: off, hand: 'OFF_HAND' }
  return null
}

function findBlankPatternInOtherHand(player, clipboardHand) {
  var main = player.mainHandItem
  var off = player.offHandItem

  if (clipboardHand === 'MAIN_HAND') {
    if (off && off.id === BLANK_PATTERN_ID) return { item: off, hand: 'OFF_HAND' }
  } else {
    if (main && main.id === BLANK_PATTERN_ID) return { item: main, hand: 'MAIN_HAND' }
  }

  return null
}

BlockEvents.rightClicked(event => {
  if (event.level.isClientSide()) return
  // 右键会对双手各触发一次；只处理主手，避免同一次点击执行两次逻辑
  if (event.hand == 'OFF_HAND') return
  if (event.block.id !== BLOCK_ID) return

  var player = event.player
  if (!player) return

  var clipboard = findClipboardInHands(player)
  if (!clipboard) {
    player.tell('需要手持材料清单剪贴板（Create 蓝图加农炮的材料清单）')
    return
  }

  var blank = findBlankPatternInOtherHand(player, clipboard.hand)
  if (!blank) {
    player.tell('另一只手需要拿着 AE2 空白样板')
    return
  }

  var inputs = readChecklistInputs(clipboard.item)
  if (!inputs.length) {
    player.tell('没有在剪贴板里找到可用的材料条目')
    return
  }

  var paperName = clipboard.item && clipboard.item.displayName ? String(clipboard.item.displayName) : '材料清单'
  var snbt = buildProcessingPatternSnbt(inputs, paperName)
  var outItem = Item.of(OUT_PATTERN_ID, snbt)

  // 如果 SNBT 解析失败，Item.of 会退化成无 NBT 的物品；这里直接提示便于继续排查
  if (!outItem || !outItem.nbt) {
    player.tell('生成样板失败：NBT 解析失败（已回退为无 NBT 样板）')
    player.tell('SNBT(前200): ' + String(snbt).substring(0, 200))
    return
  }

  // 消耗 1 个空白样板，不消耗剪贴板
  if (blank.hand === 'MAIN_HAND') {
    player.mainHandItem.count--
  } else {
    player.offHandItem.count--
  }

  player.give(outItem)
  player.tell('已生成 AE2 处理样板（请手动修改输出物品）')
})

ServerEvents.recipes(event => {
  // 生成器方块：空白样板 + 剪贴板（无序合成）
  const r = event.shapeless('kubejs:blueprint_pattern_encoder', [BLANK_PATTERN_ID, CLIPBOARD_ID])
  // 尽量保留剪贴板（如果该方法不存在，KubeJS 会在加载时报错；到时我再按实际 API 改写）
  if (r.keepIngredient) r.keepIngredient(CLIPBOARD_ID)
})
