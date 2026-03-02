package net.dumbcode.projectnublar.init;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.DeferredSupplier;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.FossilCollection;
import net.dumbcode.projectnublar.api.fossil.Quality;
import net.dumbcode.projectnublar.item.*;
import net.dumbcode.projectnublar.item.fossil.FossilBlockItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ItemInit {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Constants.MODID, Registries.ITEM);

    public static final DeferredSupplier<Item> FOSSIL_ITEM = ITEMS.register("fossil", () -> new FossilItem(getItemProperties()));

    public static final DeferredSupplier<Item> AMBER_ITEM = ITEMS.register("amber", () -> new AmberItem(getItemProperties()));

    public static final DeferredSupplier<Item> DEV_STICK = ITEMS.register("dev_stick", () -> new DebugStick(getItemProperties()));

    public static final DeferredSupplier<Item> TEST_TUBE_ITEM = ITEMS.register("test_tube", () -> new TestTubeItem(getItemProperties()));
    public static final List<DeferredSupplier<Item>> TYRANNOSAURUS_FOSSIL_ORE = registerFossilBlockItems();

    public static final DeferredSupplier<Item> IRON_FILTER = ITEMS.register("iron_filter", () -> new FilterItem(getItemProperties().durability(100), 0.25));
    public static final DeferredSupplier<Item> GOLD_FILTER = ITEMS.register("gold_filter", () -> new FilterItem(getItemProperties().durability(100),0.5));
    public static final DeferredSupplier<Item> DIAMOND_FILTER = ITEMS.register("diamond_filter", () -> new FilterItem(getItemProperties().durability(100),1));
    public static final DeferredSupplier<Item> DEV_FILTER = ITEMS.register("dev_filter", () -> new FilterItem(getItemProperties(),1));

    public static final DeferredSupplier<Item> IRON_TANK_UPGRADE = ITEMS.register("iron_tank_upgrade", () -> new TankItem(getItemProperties(), 3000, 2000, 24, 128));
    public static final DeferredSupplier<Item> GOLD_TANK_UPGRADE = ITEMS.register("gold_tank_upgrade", () -> new TankItem(getItemProperties(), 4000, 3000, 32, 192));
    public static final DeferredSupplier<Item> DIAMOND_TANK_UPGRADE = ITEMS.register("diamond_tank_upgrade", () -> new TankItem(getItemProperties(),8000, 4000, 40, -1));

    public static final DeferredSupplier<Item> IRON_COMPUTER_CHIP = ITEMS.register("iron_computer_chip", () -> new ComputerChipItem(getItemProperties(), 3*20*60, 7 * 20 * 60, 8 * 20 * 60));
    public static final DeferredSupplier<Item> GOLD_COMPUTER_CHIP = ITEMS.register("gold_computer_chip", () -> new ComputerChipItem(getItemProperties(),2*20*60, 4 * 60 * 20, 6 * 20 * 60));
    public static final DeferredSupplier<Item> DIAMOND_COMPUTER_CHIP = ITEMS.register("diamond_computer_chip", () -> new ComputerChipItem(getItemProperties(),20*60, -1, 4 * 20 * 60));
    public static final DeferredSupplier<Item> DEV_COMPUTER_CHIP = ITEMS.register("dev_computer_chip", () -> new ComputerChipItem(getItemProperties(),10, 10, 10));

    public static final DeferredSupplier<Item> HARD_DRIVE = ITEMS.register("hard_drive", () -> new DiskStorageItem(getItemProperties(), 10*20));
    public static final DeferredSupplier<Item> SSD = ITEMS.register("ssd", () -> new DiskStorageItem(getItemProperties(), 5*20));
    public static final DeferredSupplier<Item> DEV_SSD = ITEMS.register("dev_ssd", () -> new DiskStorageItem(getItemProperties(), 10));

    public static final DeferredSupplier<Item> SYRINGE = ITEMS.register("syringe", () -> new SyringeItem(getItemProperties()));
    public static final DeferredSupplier<Item> SEQUENCER_DOOR = registerSingleItem("sequencer_door");
    public static final DeferredSupplier<Item> SEQUENCER_SCREEN = registerSingleItem("sequencer_monitor");
    public static final DeferredSupplier<Item> SEQUENCER_COMPUTER = registerSingleItem("sequencer_computer");

    public static final DeferredSupplier<Item> CRACKED_ARTIFICIAL_EGG = ITEMS.register("cracked_artificial_egg", () -> new Item(getItemProperties().stacksTo(1)));
    public static final DeferredSupplier<Item> ARTIFICIAL_EGG = ITEMS.register("artificial_egg", () -> new Item(getItemProperties().stacksTo(1)));
    public static final DeferredSupplier<Item> UNINCUBATED_EGG = ITEMS.register("unincubated_egg", () -> new UnincubatedEggItem(getItemProperties()));
    public static final DeferredSupplier<Item> SMALL_CONTAINER_UPGRADE = ITEMS.register("small_container_upgrade", () -> new Item(getItemProperties()));
    public static final DeferredSupplier<Item> LARGE_CONTAINER_UPGRADE = ITEMS.register("large_container_upgrade", () -> new Item(getItemProperties()));
    public static final DeferredSupplier<Item> WARM_BULB = ITEMS.register("warm_bulb", () -> new BulbItem(getItemProperties(), 15*20));
    public static final DeferredSupplier<Item> WARMER_BULB = ITEMS.register("warmer_bulb", () -> new BulbItem(getItemProperties(), 12*20));
    public static final DeferredSupplier<Item> HOT_BULB = ITEMS.register("hot_bulb", () -> new BulbItem(getItemProperties(),9*20));
    public static final DeferredSupplier<Item> DEV_BULB = ITEMS.register("dev_bulb", () -> new BulbItem(getItemProperties(),2));
    public static final DeferredSupplier<Item> IRON_PLANT_TANK = ITEMS.register("iron_plant_tank_ugprade", () -> new PlantTankItem(getItemProperties(),128));
    public static final DeferredSupplier<Item> GOLD_PLANT_TANK = ITEMS.register("gold_plant_tank_upgrade", () -> new PlantTankItem(getItemProperties(), 192));
    public static final DeferredSupplier<Item> INCUBATOR_NEST = ITEMS.register("incubator_nest", () -> new Item(getItemProperties()));
    public static final DeferredSupplier<Item> INCUBATOR_LID = ITEMS.register("incubator_lid", () -> new Item(getItemProperties()));
    public static final DeferredSupplier<Item> INCUBATOR_ARM_BASE = ITEMS.register("incubator_arm_base", () -> new Item(getItemProperties()));
    public static final DeferredSupplier<Item> INCUBATOR_ARM = ITEMS.register("incubator_arm", () -> new Item(getItemProperties()));
    public static final DeferredSupplier<Item> INCUBATED_TYRANNOSAURUS_REX_EGG = ITEMS.register("incubated_egg", () -> new IncubatedEggItem(getItemProperties(),DinosaurInit.TYRANNOSAURUS_REX));
    public static final DeferredSupplier<Item> LEVELING_SENSOR = ITEMS.register("leveling_sensor", () -> new Item(getItemProperties()));
    public static final DeferredSupplier<Item> WIRE_SPOOL = ITEMS.register("wire_spool", () -> new Item(getItemProperties()));
    public static DeferredSupplier<Item> registerSingleItem(String name) {
        return ITEMS.register(name, () -> new Item(getItemProperties().stacksTo(1)));
    }
    public static Item.Properties getItemProperties() {
        return new Item.Properties();
    }

    public static List<DeferredSupplier<Item>> registerFossilBlockItems(){
       List<DeferredSupplier<Item>> items = new ArrayList<>();
        FossilCollection.getFossilOresForDinosaur(DinosaurInit.TYRANNOSAURUS_REX).forEach(blockDeferredSupplier -> {
           for(Quality quality: Quality.values()){
               items.add(ITEMS.register(quality.getName().toLowerCase() + "_" + blockDeferredSupplier.getId().getPath(), () -> new FossilBlockItem(blockDeferredSupplier.get(), getItemProperties(),quality)));
           }
        });
        return items;
    }

    public static void loadClass() {ITEMS.register();
    }
}
