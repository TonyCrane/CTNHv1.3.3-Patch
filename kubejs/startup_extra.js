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
            3000, 'mid', GTValues.VA[GTValues.EV], -1, GTValues.VA[GTValues.HV], -1
        )
    );
});

let addFluid = (mat, key) => {
    let prop = new $FluidProperty();
    prop.getStorage().enqueueRegistration(key, new $FluidBuilder());
    mat.setProperty(PropertyKey.FLUID, prop);
}
