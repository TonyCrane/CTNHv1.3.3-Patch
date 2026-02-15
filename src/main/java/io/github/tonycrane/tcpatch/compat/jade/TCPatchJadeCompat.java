package io.github.tonycrane.tcpatch.compat.jade;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import tech.vixhentx.mcmod.ctnhlib.jade.JadePriorityManager;

public final class TCPatchJadeCompat {

    private TCPatchJadeCompat() {}

    public static void init() {
        JadePriorityManager.registerBlockData(new MultiThreadRecipeOutputProvider(), BlockEntity.class, 1650,
                "tcpatch_multithread_recipe_output_data");
        JadePriorityManager.registerBlockComponent(new MultiThreadRecipeOutputProvider(), Block.class, 1650,
                "tcpatch_multithread_recipe_output_component");
    }
}
