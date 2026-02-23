// Add this file to kubejs/startup_scripts/src/

const $IngotProperty = Java.loadClass('com.gregtechceu.gtceu.api.data.chemical.material.properties.IngotProperty');
const $DustProperty = Java.loadClass('com.gregtechceu.gtceu.api.data.chemical.material.properties.DustProperty');
const $BlastProperty = Java.loadClass('com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty');
const $FluidProperty = Java.loadClass('com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty');
const $FluidStorageKeys = Java.loadClass('com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys');


GTCEuStartupEvents.registry('gtceu:material', event => {
    // 补充碳化硅合金冶炼配方
    addFluid(GTMaterials.get('silicon_carbide'), $FluidStorageKeys.LIQUID);
    GTMaterials.get('silicon_carbide').setProperty(
        PropertyKey.BLAST, new $BlastProperty(
            3058, 'mid', GTValues.VA[GTValues.EV], 1500, GTValues.VA[GTValues.HV], -1
        )
    );
    // 修复铋铅合金 140 合金冶炼配方，使输出从液态变为熔融
    GTMaterials.get('cerrobase_140').removeProperty(PropertyKey.BLAST);
    GTMaterials.get('cerrobase_140').setProperty(
        PropertyKey.BLAST, new $BlastProperty(1800)
    )
});

let addFluid = (mat, key) => {
    let prop = new $FluidProperty();
    prop.getStorage().enqueueRegistration(key, new $FluidBuilder());
    mat.setProperty(PropertyKey.FLUID, prop);
}
