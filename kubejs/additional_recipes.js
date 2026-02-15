// Add this file to kubejs/server_scripts/src/

ServerEvents.recipes(event => {
    event.recipes.gtceu.meteor_capturer('desh_ore')
        .itemInputs('ctnhcore:heavy_plate_t3')
        .inputFluids(Fluid.of('bloodmagic:life_essence_fluid', 1024000))
        .itemOutputs(
            '256x gtceu:mars_stone_desh_ore',
            '128x gtceu:mars_stone_calorite_ore',
            '64x gtceu:mars_stone_ostrum_ore'
        )
        .addData('radius', 6)
        .addData('rock', 'ad_astra:mars_stone')
        .EUt(12222)
        .duration(400);
    }
)
