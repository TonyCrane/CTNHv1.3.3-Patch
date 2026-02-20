// Add this file to kubejs/server_scripts/src/

ServerEvents.recipes(event => {
    // 坠星操纵者 - 外星矿物
    event.recipes.gtceu.meteor_capturer('desh_ore')
        .itemInputs('ctnhcore:heavy_plate_t3')
        .inputFluids(Fluid.of('bloodmagic:life_essence_fluid', 1024000))
        .itemOutputs(
            '256x gtceu:moon_stone_desh_ore',
            '128x gtceu:mars_stone_ostrum_ore',
            '64x gtceu:venus_stone_calorite_ore',
            '64x gtceu:moon_stone_arcane_crystal_ore',
        )
        .addData('radius', 6)
        .addData('rock', 'ad_astra:mars_stone')
        .EUt(12222)
        .duration(400)

    // 量子盘相关配方，修改自 1.4.0 版本配方
    event.recipes.gtceu.assembler('quantum_omni_cell_housing')
        .itemInputs('4x ae2omnicells:charged_ender_ingot')
        .itemInputs('2x gtceu:laminated_glass')
        .itemInputs('2x gtceu:quantum_eye')
        .itemInputs('1x ae2:singularity')
        .inputFluids(Fluid.of('gtceu:plusating_alloy', 288))
        .itemOutputs('ae2omnicells:quantum_omni_cell_housing')
        .cleanroom(CleanroomType.CLEANROOM)
        .EUt(GTValues.VA[GTValues.HV])
        .duration(100)
    
    event.recipes.gtceu.circuit_assembler('quantum_omni_cell_component_1k')
        .itemInputs('16x gtceu:carbon_fiber_mesh')
        .itemInputs('4x gtceu:dense_tungsten_steel_plate')
        .itemInputs('3x #gtceu:circuits/iv')
        .itemInputs('2x #gtceu:circuits/ev')
        .itemInputs('ae2omnicells:multidimensional_expansion_processor')
        .inputFluids(Fluid.of('gtceu:plusating_alloy', 144))
        .itemOutputs('ae2omnicells:quantum_omni_cell_component_1k')
        .cleanroom(CleanroomType.CLEANROOM)
        .EUt(GTValues.VA[GTValues.IV])
        .duration(200)
    
    event.recipes.gtceu.extruder('multidimensional_expansion_circuit_print_ctnh')
        .itemInputs('ae2:singularity')
        .notConsumable('ae2omnicells:multidimensional_expansion_print_press')
        .itemOutputs('ae2omnicells:multidimensional_expansion_circuit_print')
        .cleanroom(CleanroomType.CLEANROOM)
        .EUt(GTValues.VA[GTValues.LuV])
        .duration(600)
    
    event.recipes.gtceu.assembler('multidimensional_expansion_processor_ctnh')
        .itemInputs('ae2omnicells:multidimensional_expansion_circuit_print')
        .itemInputs('4x gtceu:solar_flare_black_diamond_plate')
        .itemInputs('16x gtceu:highly_advanced_soc')
        .itemOutputs('ae2omnicells:multidimensional_expansion_processor')
        .cleanroom(CleanroomType.CLEANROOM)
        .EUt(GTValues.VA[GTValues.IV])
        .duration(200)
    
    // 类星体符文配方，修改自 1.4.0 版本配方，ZPM EU 存储单元改为 ZPM 能量单元
    event.custom({
        "type": "mythicbotany:rune_ritual",
        "center": {
            "item": "mythicbotany:mjoellnir"
        },
        "group": "rune_rituals",
        "inputs": [{
            "item": "mythicbotany:midgard_rune"
        }, {
            "item": "mythicbotany:niflheim_rune"
        }, {
            "item": "gtceu:zenith_essence_bucket"
        }, {
            "item": "mythicbotany:alfheim_rune"
        }, {
            "item": "mythicbotany:helheim_rune"
        }, {
            "item": "mythicbotany:vanaheim_rune"
        }, {
            "item": "mythicbotany:joetunheim_rune"
        }, {
            "item": "mythicbotany:muspelheim_rune"
        }, {
            "item": "mythicbotany:nidavellir_rune"
        }, {
            "item": "mythicbotany:asgard_rune"
        }],
        "mana": 5000000,
        "outputs": [{
            "count": 1,
            "item": "ctnhcore:quasar_rune"
        }],
        "runes": [{
            "consume": true,
            "rune": { "item": "ctnhcore:horizen_rune" },
            "x": 2,
            "z": -2
        }, {
            "consume": true,
            "rune": { "item": "ctnhcore:horizen_rune" },
            "x": -2,
            "z": 2
        }, {
            "consume": true,
            "rune": { "item": "ctnhcore:starlight_rune" },
            "x": -3,
            "z": 3
        }, {
            "consume": true,
            "rune": { "item": "ctnhcore:starlight_rune" },
            "x": 3,
            "z": -3
        }, {
            "consume": true,
            "rune": { "item": "ctnhcore:twist_rune" },
            "x": -4,
            "z": 4
        }, {
            "consume": true,
            "rune": { "item": "ctnhcore:twist_rune" },
            "x": 4,
            "z": -4
        }, {
            "consume": true,
            "rune": { "item": "ctnhcore:proliferation_rune" },
            "x": -5,
            "z": 5
        }, {
            "consume": true,
            "rune": { "item": "ctnhcore:proliferation_rune" },
            "x": 5,
            "z": -5
        }, {
            "consume": false,
            "rune": { "item": "ae2omnicells:omni_cell_component_1m" },
            "x": 0,
            "z": 2
        }, {
            "consume": false,
            "rune": { "item": "ae2omnicells:omni_cell_component_1m" },
            "x": 1,
            "z": 2
        }, {
            "consume": false,
            "rune": { "item": "ae2omnicells:omni_cell_component_1m" },
            "x": -1,
            "z": 2
        }, {
            "consume": false,
            "rune": { "item": "ae2omnicells:complex_omni_cell_component_1m" },
            "x": 0,
            "z": -2
        }, {
            "consume": false,
            "rune": { "item": "ae2omnicells:complex_omni_cell_component_1m" },
            "x": 1,
            "z": -2
        }, {
            "consume": false,
            "rune": { "item": "ae2omnicells:complex_omni_cell_component_1m" },
            "x": -1,
            "z": -2
        }, {
            "consume": false,
            "rune": { "item": "ae2omnicells:quantum_omni_cell_component_1k" },
            "x": 2,
            "z": 0
        }, {
            "consume": false,
            "rune": { "item": "ae2omnicells:quantum_omni_cell_component_1k" },
            "x": 2,
            "z": 1
        }, {
            "consume": false,
            "rune": { "item": "ae2omnicells:quantum_omni_cell_component_1k" },
            "x": 2,
            "z": -1
        }, {
            "consume": false,
            "rune": { "item": "gtceu:energy_module" },
            "x": -2,
            "z": 0
        }, {
            "consume": false,
            "rune": { "item": "gtceu:energy_module" },
            "x": -2,
            "z": 1
        }, {
            "consume": false,
            "rune": { "item": "gtceu:energy_module" },
            "x": -2,
            "z": -1
        }],
        "ticks": 200
    }).id('tcpatch:quasar_rune')
})
