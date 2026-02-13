package io.github.tonycrane.tcpatch.mixin.gtmthings;

import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;

@Pseudo
@Mixin(targets = "com.hepdd.gtmthings.common.block.machine.trait.miner.DigitalMinerLogic", remap = false)
public abstract class MixinDigitalMinerLogic_RawOreFilter {

    @Unique
    private static final TagKey<Item> TCPATCH$RAW_MATERIALS_TAG =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("forge", "raw_materials"));

    @Unique
    private final Map<Block, Boolean> tcpatch$rawOreFilterCache = new IdentityHashMap<>();

    @Unique
    private static volatile Method tcpatch$getMachineMethod;

    @Shadow
    protected int x;

    @Shadow
    protected int y;

    @Shadow
    protected int z;

    @Shadow
    protected int startX;

    @Shadow
    protected int startZ;

    @Shadow
    private int minBuildHeight;

    @Shadow
    private int minHeight;

    @Shadow
    private int currentRadius;

    @Shadow
    private ItemFilter itemFilter;

    @Shadow
    protected abstract boolean isSilkTouchMode();

    @Shadow
    public abstract ItemStack getPickaxeTool();

    @Shadow
    protected abstract void getRegularBlockDrops(NonNullList<ItemStack> blockDrops, BlockState blockState, LootParams.Builder builder);

    @Shadow
    protected abstract boolean hasPostProcessing();

    @Shadow
    protected abstract boolean doPostProcessing(NonNullList<ItemStack> blockDrops, BlockState blockState, LootParams.Builder builder);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void tcpatch$clearRawOreCacheOnInit(
            @NotNull IRecipeLogicMachine machine,
            int maximumRadius,
            int minHeight,
            int maxHeight,
            int silk,
            ItemFilter itemFilter,
            int speed,
            CallbackInfo ci) {
        tcpatch$rawOreFilterCache.clear();
    }

    @Inject(method = "resetRecipeLogic(IIIILcom/gregtechceu/gtceu/api/cover/filter/ItemFilter;)V", at = @At("TAIL"))
    private void tcpatch$clearRawOreCacheOnReset(
            int maximumRadius,
            int minHeight,
            int maxHeight,
            int silk,
            ItemFilter itemFilter,
            CallbackInfo ci) {
        tcpatch$rawOreFilterCache.clear();
    }

    @Inject(method = "getBlocksToMine", at = @At("HEAD"), cancellable = true)
    private void tcpatch$patchGetBlocksToMine(CallbackInfoReturnable<LinkedList<BlockPos>> cir) {
        LinkedList<BlockPos> blocks = new LinkedList<>();

        Level level = tcpatch$getLevel();
        ServerLevel serverLevel = level instanceof ServerLevel sl ? sl : null;

        // Keep logic consistent with GTMThings: calculate then clamp to Short.MAX_VALUE
        int calcAmount = Short.MAX_VALUE;
        int calculated = 0;

        if (this.minBuildHeight == Integer.MAX_VALUE) {
            this.minBuildHeight = level.getMinBuildHeight();
        }

        while (calculated < calcAmount) {
            if (y > this.minHeight) {
                if (z <= startZ + currentRadius * 2) {
                    if (x <= startX + currentRadius * 2) {
                        BlockPos blockPos = new BlockPos(x, y, z);
                        BlockState state = level.getBlockState(blockPos);
                        if (state.getBlock().defaultDestroyTime() >= 0 &&
                                level.getBlockEntity(blockPos) == null &&
                                state.is(Tags.Blocks.ORES)) {
                            if (itemFilter == null) {
                                blocks.addLast(blockPos);
                            } else if (tcpatch$matchesItemFilter(serverLevel, blockPos, state)) {
                                blocks.addLast(blockPos);
                            }
                        }
                        ++x;
                    } else {
                        x = startX;
                        ++z;
                    }
                } else {
                    z = startZ;
                    --y;
                }
            } else {
                cir.setReturnValue(blocks);
                return;
            }

            if (!blocks.isEmpty()) {
                calculated = blocks.size();
            }
        }

        cir.setReturnValue(blocks);
    }

    @Unique
    private Level tcpatch$getLevel() {
        try {
            Object machine = tcpatch$getMachine();
            return (Level) machine.getClass().getMethod("getLevel").invoke(machine);
        } catch (Throwable t) {
            throw new RuntimeException("tcpatch: failed to call getMachine().getLevel() reflectively", t);
        }
    }

    @Unique
    private Object tcpatch$getMachine() {
        try {
            Method m = tcpatch$getMachineMethod;
            if (m == null) {
                m = this.getClass().getMethod("getMachine");
                m.setAccessible(true);
                tcpatch$getMachineMethod = m;
            }
            return m.invoke(this);
        } catch (Throwable t) {
            throw new RuntimeException("tcpatch: failed to call getMachine() reflectively", t);
        }
    }

    @Unique
    private boolean tcpatch$matchesItemFilter(@Nullable ServerLevel serverLevel, @NotNull BlockPos pos, @NotNull BlockState state) {
        if (itemFilter == null) return true;
        if (itemFilter.test(state.getBlock().asItem().getDefaultInstance())) return true;
        if (isSilkTouchMode()) return false;
        if (serverLevel == null) return false;
        return tcpatch$matchesRawOreDrops(serverLevel, pos, state);
    }

    @Unique
    private boolean tcpatch$matchesRawOreDrops(@NotNull ServerLevel serverLevel, @NotNull BlockPos pos, @NotNull BlockState state) {
        Block block = state.getBlock();
        Boolean cached = tcpatch$rawOreFilterCache.get(block);
        if (cached != null) return cached;

        NonNullList<ItemStack> predictedDrops = NonNullList.create();
        LootParams.Builder builder = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.BLOCK_STATE, state)
                .withParameter(LootContextParams.ORIGIN, Vec3.atLowerCornerOf(pos))
                .withParameter(LootContextParams.TOOL, getPickaxeTool());

        getRegularBlockDrops(predictedDrops, state, builder);
        if (!predictedDrops.isEmpty() && hasPostProcessing()) {
            doPostProcessing(predictedDrops, state, builder);
        }

        boolean matches = false;
        for (ItemStack drop : predictedDrops) {
            if (!drop.isEmpty() && drop.is(TCPATCH$RAW_MATERIALS_TAG) && itemFilter.test(drop)) {
                matches = true;
                break;
            }
        }

        tcpatch$rawOreFilterCache.put(block, matches);
        return matches;
    }
}
