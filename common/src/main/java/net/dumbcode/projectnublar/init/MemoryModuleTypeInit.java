package net.dumbcode.projectnublar.init;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.DeferredSupplier;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.entity.dinosaur.AbstractDinosaur;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class MemoryModuleTypeInit {
    public static final DeferredRegister<MemoryModuleType<?>> MEMORIES = DeferredRegister.create(Constants.MODID, Registries.MEMORY_MODULE_TYPE);


    public static final DeferredSupplier<MemoryModuleType<Integer>> LAST_FEED_TIME =
            MEMORIES.register("time_last_fed", ()-> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredSupplier<MemoryModuleType<Integer>> DAYS_SINCE_LAST_FED  =
            MEMORIES.register("days_since_last_meal", ()-> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredSupplier<MemoryModuleType<Integer>> LAST_DRINK_TIME =
            MEMORIES.register("time_last_drank", ()-> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredSupplier<MemoryModuleType<Integer>> DAYS_SINCE_LAST_DRANK  =
            MEMORIES.register("days_since_last_drink", ()-> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredSupplier<MemoryModuleType<Integer>> MEAL_COUNTER  =
            MEMORIES.register("meal_counter", ()-> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredSupplier<MemoryModuleType<Integer>> DRINK_COUNTER  =
            MEMORIES.register("drink_counter", ()-> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredSupplier<MemoryModuleType<Boolean>> DRANK_TODAY =
            MEMORIES.register("has_drank_today", ()-> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredSupplier<MemoryModuleType<Boolean>> EATEN_TODAY =
            MEMORIES.register("has_eaten_today", ()-> new MemoryModuleType<>(Optional.empty()));



    public static final DeferredSupplier<MemoryModuleType<Float>> MOOD =
            MEMORIES.register("dinosaur_mood", () -> new MemoryModuleType<>(Optional.empty()));

    public static final DeferredSupplier<MemoryModuleType<Byte>> INITIATED_TURF_WAR =
            MEMORIES.register("initiate_turf_war", ()-> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredSupplier<MemoryModuleType<Integer>> TURF_WAR_MEMBER =
            MEMORIES.register("turf_war_entity_id",()->new MemoryModuleType<>(Optional.empty()));
 public static final DeferredSupplier<MemoryModuleType<Integer>> TURF_WAR_OUTCOME =
            MEMORIES.register("turf_war_outcome_id",()->new MemoryModuleType<>(Optional.empty()));
    public static final DeferredSupplier<MemoryModuleType<AbstractDinosaur>> SOCIAL_TARGET =
            MEMORIES.register("social_dino_entity",()->new MemoryModuleType<>(Optional.empty()));

    public static final DeferredSupplier<MemoryModuleType<Boolean>> IS_HUNGRY =
            MEMORIES.register("is_dino_hungry", ()-> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredSupplier<MemoryModuleType<Boolean>> IS_DRINKING =
            MEMORIES.register("is_dino_drinking", ()-> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredSupplier<MemoryModuleType<Boolean>> IS_STARVING =
            MEMORIES.register("is_dino_starving", ()-> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredSupplier<MemoryModuleType<Boolean>> IS_DEHYDRATED =
            MEMORIES.register("is_dino_dehydrated", ()-> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredSupplier<MemoryModuleType<Boolean>> IS_EXHAUSTED =
            MEMORIES.register("is_dino_exhausted", ()-> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredSupplier<MemoryModuleType<Boolean>> IS_THIRSTY =
            MEMORIES.register("is_dino_thirsty", ()-> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredSupplier<MemoryModuleType<Boolean>> IS_TIRED =
            MEMORIES.register("is_dino_tired", ()-> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredSupplier<MemoryModuleType<Boolean>> IS_RESTING =
            MEMORIES.register("is_dino_resting", ()-> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredSupplier<MemoryModuleType<Boolean>> IS_SITTING =
            MEMORIES.register("is_dino_sitting", ()-> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredSupplier<MemoryModuleType<Boolean>> GETTING_UP =
            MEMORIES.register("is_dino_rising", ()-> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredSupplier<MemoryModuleType<Boolean>> IS_SLEEPING =
            MEMORIES.register("is_dino_sleeping", ()-> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredSupplier<MemoryModuleType<Boolean>> IS_EATING =
            MEMORIES.register("is_dino_eating", ()-> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredSupplier<MemoryModuleType<Boolean>> IS_ROARING =
            MEMORIES.register("is_dino_roaring", ()-> new MemoryModuleType<>(Optional.empty()));

    public static final DeferredSupplier<MemoryModuleType<BlockPos>> HAS_FOUND_WATER =
            MEMORIES.register( "found_water_source", ()-> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredSupplier<MemoryModuleType<ItemEntity>> FOUND_FOOD_ITEM =
            MEMORIES.register( "found_food_item", ()-> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredSupplier<MemoryModuleType<Boolean>> HUNTING =
            MEMORIES.register("is_dino_hunting", ()-> new MemoryModuleType<>(Optional.empty()));

    public static final DeferredSupplier<MemoryModuleType<Boolean>> WANTS_TO_BREAK_FENCE =
            MEMORIES.register("wants_to_break_fence", ()-> new MemoryModuleType<>(Optional.empty()));

    public static final DeferredSupplier<MemoryModuleType<UUID>> GROUP_UUID =
            MEMORIES.register("dino_pack_id", () -> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredSupplier<MemoryModuleType<UUID>> GROUP_LEADER_UUID =
            MEMORIES.register("dino_pack_leader", () -> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredSupplier<MemoryModuleType<Boolean>> IS_GROUP_LEADER =
            MEMORIES.register("is_pack_leader", ()-> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredSupplier<MemoryModuleType<Boolean>> HAS_GROUP =
            MEMORIES.register("is_pack_leader", ()-> new MemoryModuleType<>(Optional.empty()));

    public static final DeferredSupplier<MemoryModuleType<Float>> HUNTING_RANGE =
            MEMORIES.register("hunting_range", () -> new MemoryModuleType<>(Optional.empty()));

    /// TO-DO REMOVE THIS Memory
    public static final DeferredSupplier<MemoryModuleType<Boolean>> BRAIN_OVERRIDE =
            MEMORIES.register("is_brain_overridden", ()-> new MemoryModuleType<>(Optional.empty()));

    public static final DeferredSupplier<MemoryModuleType<UUID>> MATE_UUID  =
            MEMORIES.register("dino_mate_id", ()-> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredSupplier<MemoryModuleType<AbstractDinosaur>> MATE  =
            MEMORIES.register("dino_mate", ()-> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredSupplier<MemoryModuleType<Boolean>> PREGNANT =
            MEMORIES.register("dino_pregnant", ()-> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredSupplier<MemoryModuleType<Integer>> LAY_DATE =
            MEMORIES.register("dino_lay_eggs_date", ()-> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredSupplier<MemoryModuleType<BlockPos>> NEST_LOCATION =
            MEMORIES.register("nest_location", () -> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredSupplier<MemoryModuleType<List<AbstractDinosaur>>> OFFSPRING_LIST  =
            MEMORIES.register("dino_offspring_list", ()-> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredSupplier<MemoryModuleType<List<AbstractDinosaur>>> NEAREST_DINOSAURS  =
            MEMORIES.register("dino_nearby_peers", ()-> new MemoryModuleType<>(Optional.empty()));

    public static final DeferredSupplier<MemoryModuleType<Map<Player,Integer>>> PLAYER_REPUTATION =
            MEMORIES.register("player_reputation", () -> new MemoryModuleType<>(Optional.empty()));


    public static void loadClass(){MEMORIES.register();}


}
