package io.github.tonycrane.tcpatch.mixin;

import io.github.tonycrane.tcpatch.compat.ctnhcore.SlaughterHouseMachineHooks;
import io.github.tonycrane.tcpatch.compat.ctnhcore.SlaughterHouseItemKey;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import com.lowdragmc.lowdraglib.syncdata.ISubscription;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;

@Pseudo
@Mixin(targets = "io.github.cpearl0.ctnhcore.common.machine.multiblock.SlaughterHouseMachine", remap = false)
public abstract class MixinSlaughterHouseMachine_CacheAndPerf implements SlaughterHouseMachineHooks {

    @Shadow
    public List<String> mobList;

    @Shadow
    public ItemStack hostWeapon;

    @Shadow
    public double damagePerSecond;

    @Shadow
    public static int ticksPerSecond;

    @Shadow
    private FakePlayer fakePlayer;

    @Shadow
    public abstract FakePlayer getFakePlayer(ServerLevel level);

    @Shadow
    public abstract void resetMobList();

    @Unique
    private MetaMachine tcpatch$self() {
        return (MetaMachine) (Object) this;
    }

    @Unique
    private WorkableElectricMultiblockMachine tcpatch$selfElectric() {
        return (WorkableElectricMultiblockMachine) (Object) this;
    }

    @Unique
    private WorkableMultiblockMachine tcpatch$selfWorkableMulti() {
        return (WorkableMultiblockMachine) (Object) this;
    }

    @Unique
    private List<IMultiPart> tcpatch$getParts() {
        return tcpatch$selfWorkableMulti().getParts();
    }

    @Unique
    private com.gregtechceu.gtceu.api.pattern.MultiblockState tcpatch$getMultiblockState() {
        return tcpatch$selfWorkableMulti().getMultiblockState();
    }

    @Unique
    private List<ISubscription> tcpatch$getTraitSubscriptions() {
        return ((MixinWorkableMultiblockMachine_TraitSubscriptionsAccessor) (Object) this).tcpatch$getTraitSubscriptions();
    }

    @Unique
    private static final int TCPATCH$LOOT_SAMPLES_PER_REFRESH = 4;

    @Unique
    private static final int TCPATCH$LOOT_CACHE_TTL_TICKS = 20;

    @Unique
    private static final int TCPATCH$ENTITY_CACHE_LIMIT = 64;

    @Unique
    private static final int TCPATCH$LOOT_TABLE_CACHE_LIMIT = 128;

    @Unique
    private boolean tcpatch$mobListDirty = true;

    @Unique
    private boolean tcpatch$lootCacheDirty = true;

    @Unique
    private long tcpatch$lootCacheComputedAtTick = -1;

    @Unique
    private long tcpatch$lootCacheValidUntilTick = -1;

    @Unique
    private List<ItemStack> tcpatch$lootCacheStacks = List.of();

    @Unique
    private int tcpatch$lootCacheTotalExperience = 0;

    @Unique
    private int tcpatch$lootCacheDuration = 0;

    @Unique
    private int tcpatch$lootCacheRepeatTimes = 1;

    @Unique
    private final LinkedHashMap<String, LivingEntity> tcpatch$entityCache = new LinkedHashMap<>(TCPATCH$ENTITY_CACHE_LIMIT,
            0.75f, true) {

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, LivingEntity> eldest) {
            return size() > TCPATCH$ENTITY_CACHE_LIMIT;
        }
    };

    @Unique
    private final LinkedHashMap<ResourceLocation, LootTable> tcpatch$lootTableCache = new LinkedHashMap<>(
            TCPATCH$LOOT_TABLE_CACHE_LIMIT, 0.75f, true) {

        @Override
        protected boolean removeEldestEntry(Map.Entry<ResourceLocation, LootTable> eldest) {
            return size() > TCPATCH$LOOT_TABLE_CACHE_LIMIT;
        }
    };

    @Unique
    private static final ResourceLocation TCPATCH$XP_JUICE_FLUID = ResourceLocation.fromNamespaceAndPath("enderio",
            "xp_juice");

    @Unique
    private static final ResourceLocation TCPATCH$XP_JUICE_SOURCE_FLUID = ResourceLocation.fromNamespaceAndPath("enderio",
            "xp_juice_source");

    @Inject(method = "onStructureFormed", at = @At("TAIL"))
    private void tcpatch$onStructureFormedTail(CallbackInfo ci) {
        tcpatch$markMobListDirty();
        tcpatch$attachInputChangeSubscriptions();
    }

    @Inject(method = "resetWeapon", at = @At("TAIL"))
    private void tcpatch$resetWeaponTail(CallbackInfo ci) {
        tcpatch$lootCacheDirty = true;
    }

    @Inject(method = "beforeWorking", at = @At("HEAD"))
    private void tcpatch$beforeWorkingHead(GTRecipe recipe, CallbackInfoReturnable<Boolean> cir) {
        tcpatch$ensureMobListUpToDate();
    }

    @Unique
    private void tcpatch$markMobListDirty() {
        tcpatch$mobListDirty = true;
        tcpatch$lootCacheDirty = true;
    }

    @Unique
    private void tcpatch$ensureMobListUpToDate() {
        if (!tcpatch$mobListDirty) return;
        resetMobList();

        var mobs = new LinkedHashSet<String>();
        for (var mobId : mobList) {
            if (mobId == null || mobId.isEmpty()) continue;
            mobs.add(mobId);
        }
        mobList.clear();
        mobList.addAll(mobs);

        tcpatch$mobListDirty = false;
    }

    @Unique
    private void tcpatch$clearLootCache() {
        tcpatch$lootCacheDirty = true;
        tcpatch$lootCacheComputedAtTick = -1;
        tcpatch$lootCacheValidUntilTick = -1;
        tcpatch$lootCacheStacks = List.of();
        tcpatch$lootCacheTotalExperience = 0;
        tcpatch$lootCacheDuration = 0;
        tcpatch$lootCacheRepeatTimes = 1;
    }

    @Unique
    private void tcpatch$attachInputChangeSubscriptions() {
        if (!(tcpatch$self().getLevel() instanceof ServerLevel)) return;

        Long2ObjectMap<IO> ioMap = tcpatch$getMultiblockState().getMatchContext().getOrCreate("ioMap",
                Long2ObjectMaps::emptyMap);
        for (var part : tcpatch$getParts()) {
            IO io = ioMap.getOrDefault(part.self().getPos().asLong(), IO.BOTH);
            if (io == IO.NONE || io == IO.OUT) continue;

            for (var handlerList : part.getRecipeHandlers()) {
                if (!handlerList.isValid(IO.IN)) continue;
                tcpatch$getTraitSubscriptions()
                        .add(handlerList.subscribe(this::tcpatch$markMobListDirty, ItemRecipeCapability.CAP));
            }
        }
    }

    @Unique
    private LivingEntity tcpatch$getOrCreateCachedEntity(ServerLevel level, String mobId) {
        var cached = tcpatch$entityCache.get(mobId);
        if (cached != null && cached.level() == level) {
            return cached;
        }

        var typeOpt = EntityType.byString(mobId);
        if (typeOpt.isEmpty()) return null;
        var created = typeOpt.get().create(level);
        if (!(created instanceof LivingEntity living)) return null;
        tcpatch$entityCache.put(mobId, living);
        return living;
    }

    @Unique
    private static double tcpatch$getEffectiveHealth(LivingEntity livingEntity) {
        if (livingEntity.getArmorValue() != 0) {
            var armor = livingEntity.getArmorValue();
            return livingEntity.getMaxHealth() / ((double) 20 / (armor + 20));
        }
        return livingEntity.getMaxHealth();
    }

    @Unique
    private LootTable tcpatch$getOrCacheLootTable(MinecraftServer server, ResourceLocation tableId) {
        return tcpatch$lootTableCache.computeIfAbsent(tableId, id -> server.getLootData().getLootTable(id));
    }

    @Unique
    private void tcpatch$rebuildLootCache(ServerLevel level) {
        tcpatch$ensureMobListUpToDate();

        if (mobList.isEmpty()) {
            tcpatch$clearLootCache();
            tcpatch$lootCacheDirty = false;
            tcpatch$lootCacheComputedAtTick = level.getGameTime();
            tcpatch$lootCacheValidUntilTick = level.getGameTime() + TCPATCH$LOOT_CACHE_TTL_TICKS;
            return;
        }

        int repeatTimes = Math.max(1, tcpatch$selfElectric().getTier() - 2);
        tcpatch$lootCacheRepeatTimes = repeatTimes;

        double totalTime = 0;
        int totalExperience = 0;
        Map<SlaughterHouseItemKey, Long> lootCounts = new HashMap<>();

        var server = Objects.requireNonNull(level.getServer());
        var fakePlayer = getFakePlayer(level);
        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, hostWeapon);
        var damageSource = fakePlayer.damageSources().playerAttack(fakePlayer);
        var origin = tcpatch$self().getPos().getCenter();
        var blockState = tcpatch$self().getBlockState();
        BlockEntity blockEntity = level.getBlockEntity(tcpatch$self().getPos());

        for (int i = 0; i < TCPATCH$LOOT_SAMPLES_PER_REFRESH; i++) {
            String mob = mobList.get(level.getRandom().nextInt(mobList.size()));

            if (mob.equals("minecraft:wither")) {
                var stack = Items.NETHER_STAR.getDefaultInstance();
                var key = new SlaughterHouseItemKey(stack.getItem(), null);
                lootCounts.merge(key, (long) stack.getCount(), Long::sum);
                continue;
            }

            var livingEntity = tcpatch$getOrCreateCachedEntity(level, mob);
            if (livingEntity == null) continue;

            var enchantInfluence = EnchantmentHelper.getDamageBonus(hostWeapon, livingEntity.getMobType());
            totalTime += tcpatch$getEffectiveHealth(livingEntity) / ((damagePerSecond + enchantInfluence) * repeatTimes) *
                    ticksPerSecond;
            totalExperience += livingEntity.getExperienceReward() * 20;

                var lootTableId = livingEntity.getLootTable();
                if (lootTableId == null) continue;
                var lootTable = tcpatch$getOrCacheLootTable(server, lootTableId);
            LootParams lootParams = new LootParams.Builder(level)
                    .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, fakePlayer)
                    .withParameter(LootContextParams.TOOL, hostWeapon)
                    .withParameter(LootContextParams.THIS_ENTITY, livingEntity)
                    .withParameter(LootContextParams.DAMAGE_SOURCE, damageSource)
                    .withParameter(LootContextParams.ORIGIN, origin)
                    .withParameter(LootContextParams.KILLER_ENTITY, fakePlayer)
                    .withParameter(LootContextParams.BLOCK_STATE, blockState)
                    .withParameter(LootContextParams.BLOCK_ENTITY, blockEntity)
                    .withParameter(LootContextParams.DIRECT_KILLER_ENTITY, fakePlayer)
                    .withParameter(LootContextParams.EXPLOSION_RADIUS, 0F)
                    .create(lootTable.getParamSet());

            for (var loot : lootTable.getRandomItems(lootParams)) {
                if (loot.isEmpty()) continue;
                var key = new SlaughterHouseItemKey(loot.getItem(), loot.getTag());
                lootCounts.merge(key, (long) loot.getCount(), Long::sum);
            }
        }

        if (repeatTimes > 1) {
            totalExperience = Math.multiplyExact(totalExperience, repeatTimes);
            for (var entry : lootCounts.entrySet()) {
                entry.setValue(Math.multiplyExact(entry.getValue(), (long) repeatTimes));
            }
        }

        var mergedStacks = new ArrayList<ItemStack>(lootCounts.size());
        for (var entry : lootCounts.entrySet()) {
            long totalCount = entry.getValue();
            var item = entry.getKey().item();
            var tag = entry.getKey().tag();

            int count = (int) Math.min(Integer.MAX_VALUE, totalCount);
            var stack = new ItemStack(item, count);
            if (tag != null) stack.setTag(tag.copy());
            mergedStacks.add(stack);
        }

        tcpatch$lootCacheStacks = mergedStacks;
        tcpatch$lootCacheTotalExperience = totalExperience;
        tcpatch$lootCacheDuration = Math.max(1, (int) totalTime * repeatTimes);
        tcpatch$lootCacheDirty = false;
        tcpatch$lootCacheComputedAtTick = level.getGameTime();
        tcpatch$lootCacheValidUntilTick = level.getGameTime() + TCPATCH$LOOT_CACHE_TTL_TICKS;
    }

    @Unique
    private void tcpatch$ensureLootCacheUpToDateInternal(ServerLevel level) {
        tcpatch$ensureMobListUpToDate();
        long now = level.getGameTime();
        if (!tcpatch$lootCacheDirty && tcpatch$lootCacheValidUntilTick >= now) return;
        tcpatch$rebuildLootCache(level);
    }

    @Unique
    private static Fluid tcpatch$resolveXpJuiceFluid() {
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(TCPATCH$XP_JUICE_FLUID);
        if (fluid != null && fluid != Fluids.EMPTY) return fluid;
        fluid = ForgeRegistries.FLUIDS.getValue(TCPATCH$XP_JUICE_SOURCE_FLUID);
        if (fluid != null && fluid != Fluids.EMPTY) return fluid;
        return Fluids.EMPTY;
    }

    /**
     * @author crane
     * @reason Backport CTNH-Core SlaughterHouseMachine caching & subscription improvements
     */
    @Overwrite
    public static ModifierFunction recipeModifier(MetaMachine machine, GTRecipe recipe) {
        var newrecipe = GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK)
                .applyModifier(machine, recipe.copy());
        if (newrecipe == null) {
            return recipe1 -> null;
        }

        if (machine instanceof SlaughterHouseMachineHooks hooks) {
            if (machine.getLevel() instanceof ServerLevel level) {
                hooks.tcpatch$ensureLootCacheUpToDate(level);
                if (!hooks.tcpatch$getMobList().isEmpty()) {
                    var cachedStacks = hooks.tcpatch$getLootCacheStacks();
                    var itemList = new ArrayList<Content>(cachedStacks.size());
                    for (var stack : cachedStacks) {
                        itemList.add(new Content(SizedIngredient.create(stack.copy()), 1, 1, 0));
                    }
                    newrecipe.outputs.put(ItemRecipeCapability.CAP, itemList);

                    Fluid xpJuice = tcpatch$resolveXpJuiceFluid();
                    if (xpJuice != Fluids.EMPTY) {
                        newrecipe.outputs.put(FluidRecipeCapability.CAP,
                                List.of(new Content(
                                        FluidIngredient.of(new FluidStack(xpJuice, hooks.tcpatch$getLootCacheTotalExperience())),
                                        1, 1, 0)));
                    }

                    newrecipe.duration = hooks.tcpatch$getLootCacheDuration();
                }
            }
        }

        return recipe1 -> newrecipe;
    }

    @Override
    public void tcpatch$ensureLootCacheUpToDate(ServerLevel level) {
        tcpatch$ensureLootCacheUpToDateInternal(level);
    }

    @Override
    public List<ItemStack> tcpatch$getLootCacheStacks() {
        return tcpatch$lootCacheStacks;
    }

    @Override
    public int tcpatch$getLootCacheTotalExperience() {
        return tcpatch$lootCacheTotalExperience;
    }

    @Override
    public int tcpatch$getLootCacheDuration() {
        return tcpatch$lootCacheDuration;
    }

    @Override
    public int tcpatch$getLootCacheRepeatTimes() {
        return tcpatch$lootCacheRepeatTimes;
    }

    @Override
    public List<String> tcpatch$getMobList() {
        return mobList;
    }

    @Override
    public void tcpatch$onStructureInvalidOrUnload() {
        mobList.clear();
        tcpatch$markMobListDirty();
        tcpatch$clearLootCache();
        tcpatch$entityCache.clear();
        tcpatch$lootTableCache.clear();
        fakePlayer = null;
    }
}
