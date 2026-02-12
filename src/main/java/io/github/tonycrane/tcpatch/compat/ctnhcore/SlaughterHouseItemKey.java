package io.github.tonycrane.tcpatch.compat.ctnhcore;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;

/**
 * Helper key for merging loot drops in SlaughterHouseMachine patch.
 *
 * <p>NOTE: Must NOT live under the mixin package, otherwise Mixin may throw
 * IllegalClassLoadError when the transformed target class references it.</p>
 */
public record SlaughterHouseItemKey(Item item, CompoundTag tag) {
    public SlaughterHouseItemKey(Item item, CompoundTag tag) {
        this.item = item;
        this.tag = tag == null ? null : tag.copy();
    }
}
