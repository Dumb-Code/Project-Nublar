package net.dumbcode.projectnublar.api.dinosaur;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.StringRepresentable;

public record DinoBehaviourData(
        String speciesID,
        String dietID,

        double maxHealth,
        double maxStamina,
        double attackDamage,
        double speedModifier,
        double sizeMultiplier,
        double intelligence,
        double immunity,
        double resistance,
        double healthRegen,
        double growthRate,
        double fertility,
        double gestationTime,
        double eggClutchSize,
        double visionQuality,

        double domesticity,
        double aggressionLevel,
        double trustThreshold,
        double trustMultiplier,
        double socialNeed,
        double socialDrain,
        int groupSize,

        double eatRate,
        double drinkRate,
        int starvationLimit,
        int dehydrationLimit,
        double staminaDrain,

        double happyThreshold,
        double uncomfortableThreshold,
        double rageThreshold,

        boolean canFormGroup,
        ActivityWindow activityWindow,
        List<SleepWindow> sleepWindows
)
{
    /** Save key of {@link #activityWindow}; also the species config key. */
    public static final String ACTIVITY_WINDOW_KEY = "activity_window";
    /** Save key of {@link #sleepWindows}; also the species config key. */
    public static final String SLEEP_WINDOWS_KEY = "sleep_windows";
    /** The flag that {@link #activityWindow} replaced. Read only, from animals saved before it. */
    private static final String OLD_NOCTURNAL_KEY = "nocturnal";

    /**
     * When in the day a species sleeps (engineering backlog F2.4 {@code activity_window}, F2.0 "Sleep windows"). It
     * replaces the old {@code nocturnal} flag.
     */
    public enum ActivityWindow implements StringRepresentable {
        /** Sleeps 16,000 to 24,000 (22:00 to 06:00). */
        DIURNAL("diurnal"),
        /** Sleeps 4,000 to 12,000 (10:00 to 18:00). */
        NOCTURNAL("nocturnal"),
        /** Sleeps in the species' own {@code sleep_windows}, several short sleeps a day. */
        CATHEMERAL("cathemeral");

        /** The config and save form: the lower-case name. */
        public static final Codec<ActivityWindow> CODEC = StringRepresentable.fromEnum(ActivityWindow::values);

        private final String name;

        ActivityWindow(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    /**
     * One daily sleep window in day ticks. A window never crosses 24,000; none of the backlog's windows needs it.
     *
     * @param start the first day tick asleep
     * @param end   the first day tick awake again
     */
    public record SleepWindow(int start, int end) {
        /** Ticks in one Minecraft day. */
        public static final int DAY_TICKS = 24000;
        /** Backlog F2.0: a diurnal species sleeps 16,000 to 24,000. */
        public static final SleepWindow DIURNAL = new SleepWindow(16000, DAY_TICKS);
        /** Backlog F2.0: a nocturnal species sleeps 4,000 to 12,000 (10:00 to 18:00, 8 hours). */
        public static final SleepWindow NOCTURNAL = new SleepWindow(4000, 12000);

        /** The config and save form, {@code {"start": 16000, "end": 21000}}; a window must not be empty. */
        public static final Codec<SleepWindow> CODEC = RecordCodecBuilder.<SleepWindow>create(instance -> instance.group(
                Codec.intRange(0, DAY_TICKS).fieldOf("start").forGetter(SleepWindow::start),
                Codec.intRange(0, DAY_TICKS).fieldOf("end").forGetter(SleepWindow::end)
        ).apply(instance, SleepWindow::new)).validate(window -> window.start < window.end
                ? DataResult.success(window)
                : DataResult.error(() -> "A sleep window must start before it ends: " + window));

        boolean contains(int dayTick) {
            return this.start <= dayTick && dayTick < this.end;
        }
    }

    /**
     * {@code true} while the species sleeps at this day time (engineering backlog F2.0 "Sleep windows"): diurnal
     * 16,000 to 24,000, nocturnal 4,000 to 12,000, cathemeral its own {@link #sleepWindows}. {@code Rest} lets an
     * animal sleep on at full stamina only inside it, and a herd lies down for it.
     *
     * @param dayTime the overworld clock; any day, only the time of day counts
     */
    public boolean inSleepWindow(long dayTime) {
        int dayTick = Math.floorMod(dayTime, SleepWindow.DAY_TICKS);
        return switch (this.activityWindow) {
            case DIURNAL -> SleepWindow.DIURNAL.contains(dayTick);
            case NOCTURNAL -> SleepWindow.NOCTURNAL.contains(dayTick);
            case CATHEMERAL -> inAny(this.sleepWindows, dayTick);
        };
    }

    /** A plain loop: the herd code asks every tick, for every leader and resting animal. */
    private static boolean inAny(List<SleepWindow> windows, int dayTick) {
        for (SleepWindow window : windows) {
            if (window.contains(dayTick)) {
                return true;
            }
        }
        return false;
    }

    public static DinoBehaviourData fromNBT(CompoundTag tag) {
        String pSpeciesID = tag.getStringOr("species_id", "");
        String pDietID = tag.getStringOr("diet_id", "");


        double pMaxHealth = tag.getDoubleOr("default_health", 0);
        double pEnergyCapacity= tag.getDoubleOr("default_energy_capacity", 0);
        double pAttack = tag.getDoubleOr("default_attack_damage", 0);
        double pSpeed= tag.getDoubleOr("default_speed", 0);
        double pSize= tag.getDoubleOr("default_size", 0);
        double pIntelligence= tag.getDoubleOr("default_intelligence", 0);
        double pImmunity= tag.getDoubleOr("default_immunity", 0);
        double pResistance= tag.getDoubleOr("default_resistance", 0);
        double pHealthRegen= tag.getDoubleOr("default_health_regen", 0);
        double pGrowthRate= tag.getDoubleOr("default_growth_rate", 0);
        double pFertility= tag.getDoubleOr("default_fertility", 0);
        double pGestationTime= tag.getDoubleOr("default_gestation_time", 0);
        double pClutchSize= tag.getDoubleOr("default_egg_clutch", 0);
        double pVisionQuality= tag.getDoubleOr("default_vision", 0);

        double pDomesticity= tag.getDoubleOr("default_domesticity", 0);
        double pAggressionScore= tag.getDoubleOr("default_aggression", 0);
        double pTamingScore= tag.getDoubleOr("default_tame_score", 0);
        double pTrustIncrease = tag.getDoubleOr("default_trust_increase", 0);
        double pSocial= tag.getDoubleOr("default_social", 0);
        double pSocialDrain= tag.getDoubleOr("default_social_drain", 0);
        int pGroupSize= tag.getIntOr("default_group_size", 0);


        double pEatRate= tag.getDoubleOr("default_eat_rate", 0);
        double pDehydrationRate = tag.getDoubleOr("default_dehydration_rate", 0);
        int pStarvationLimit = tag.getIntOr("default_starvation_limit", 0);
        int pDehydrationLimit = tag.getIntOr("default_dehydration_limit", 0);
        double pBaseExhaustionRate = tag.getDoubleOr("default_exhaustion_rate", 0);

        double pLowRisk = tag.getDoubleOr("low_risk_threshold", 0);
        double pMediumRisk = tag.getDoubleOr("medium_risk_threshold", 0);
        double pHighRisk = tag.getDoubleOr("high_risk_threshold", 0);

        boolean pPack = tag.getBooleanOr("can_form_group", false);
        // An animal saved before the activity window has only the old flag: nocturnal true is the nocturnal window.
        ActivityWindow pActivityWindow = tag.read(ACTIVITY_WINDOW_KEY, ActivityWindow.CODEC)
                .orElseGet(() -> tag.getBooleanOr(OLD_NOCTURNAL_KEY, false)
                        ? ActivityWindow.NOCTURNAL : ActivityWindow.DIURNAL);
        List<SleepWindow> pSleepWindows = tag.read(SLEEP_WINDOWS_KEY, SleepWindow.CODEC.listOf()).orElse(List.of());

        return new DinoBehaviourData(pSpeciesID,pDietID,pMaxHealth,pEnergyCapacity,pAttack,pSpeed,pSize,
                pIntelligence,pImmunity,pResistance,pHealthRegen,pGrowthRate,pFertility,pGestationTime,pClutchSize,pVisionQuality,pDomesticity,
                pAggressionScore,pTamingScore,pTrustIncrease,pSocial,pSocialDrain,pGroupSize,pEatRate,pDehydrationRate,pStarvationLimit,pDehydrationLimit,
                pBaseExhaustionRate,pLowRisk,pMediumRisk,pHighRisk,pPack,pActivityWindow,pSleepWindows);
    }

    public CompoundTag toNBT(DinoBehaviourData behaviourData) {
        CompoundTag tag = new CompoundTag();
        tag.putString("species_id", behaviourData.speciesID);
        tag.putString("diet_id", behaviourData.dietID);

        tag.putDouble("default_health",behaviourData.maxHealth);
        tag.putDouble("default_stamina",behaviourData.maxStamina);
        tag.putDouble("default_attack_damage",behaviourData.attackDamage);
        tag.putDouble("default_speed",behaviourData.speedModifier);
        tag.putDouble("default_size",behaviourData.sizeMultiplier);
        tag.putDouble("default_intelligence",behaviourData.intelligence);
        tag.putDouble("default_immunity",behaviourData.immunity);
        tag.putDouble("default_resistance",behaviourData.resistance);
        tag.putDouble("default_health_regen",behaviourData.healthRegen);
        tag.putDouble("default_growth_rate",behaviourData.growthRate);
        tag.putDouble("default_fertility",behaviourData.fertility);
        tag.putDouble("default_gestation_time",behaviourData.gestationTime);
        tag.putDouble("default_egg_clutch",behaviourData.eggClutchSize);
        tag.putDouble("default_vision",behaviourData.visionQuality);


        tag.putDouble("default_domesticity",behaviourData.domesticity);
        tag.putDouble("default_aggression",behaviourData.aggressionLevel);
        tag.putDouble("default_tame_score",behaviourData.trustThreshold);
        tag.putDouble("default_trust_increase",behaviourData.trustMultiplier);
        tag.putDouble("default_social",behaviourData.socialNeed);
        tag.putDouble("default_social_drain",behaviourData.socialDrain);
        tag.putInt("default_group_size",behaviourData.groupSize);


        tag.putDouble("default_eat_rate", behaviourData.eatRate);
        tag.putDouble("default_dehydration_rate", behaviourData.drinkRate);
        tag.putInt("default_starvation_limit",behaviourData.starvationLimit);
        tag.putInt("default_dehydration_limit",behaviourData.dehydrationLimit);
        tag.putDouble("default_exhaustion_rate", behaviourData.staminaDrain);

        tag.putDouble("low_risk_threshold",behaviourData.happyThreshold);
        tag.putDouble("medium_risk_threshold",behaviourData.uncomfortableThreshold);
        tag.putDouble("high_risk_threshold",behaviourData.rageThreshold);

        tag.putBoolean("can_form_group", behaviourData.canFormGroup);
        tag.store(ACTIVITY_WINDOW_KEY, ActivityWindow.CODEC, behaviourData.activityWindow);
        tag.store(SLEEP_WINDOWS_KEY, SleepWindow.CODEC.listOf(), behaviourData.sleepWindows);

        return tag;
    }
}


