package net.dumbcode.projectnublar.init;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.item.AmberItem;
import net.dumbcode.projectnublar.item.BulbItem;
import net.dumbcode.projectnublar.item.FossilItem;
import net.dumbcode.projectnublar.item.IncubatedEggItem;
import net.dumbcode.projectnublar.item.PlantTankItem;
import net.dumbcode.projectnublar.item.TankItem;
import net.dumbcode.projectnublar.item.TestTubeItem;
import net.dumbcode.projectnublar.item.ComputerChipItem;
import net.dumbcode.projectnublar.item.DiskStorageItem;
import net.dumbcode.projectnublar.item.FilterItem;
import net.dumbcode.projectnublar.item.SyringeItem;
import net.dumbcode.projectnublar.item.UnincubatedEggItem;
import net.dumbcode.projectnublar.registration.RegistrationProvider;
import net.dumbcode.projectnublar.registration.RegistryObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;

public class ItemInit {
    public static final RegistrationProvider<Item> ITEMS = RegistrationProvider.get(Registries.ITEM, Constants.MODID);


    public static final RegistryObject<Item> FOSSIL_ITEM = ITEMS.register("fossil", () -> new FossilItem(getItemProperties()));
    public static final RegistryObject<Item> AMBER_ITEM = ITEMS.register("amber", () -> new AmberItem(getItemProperties()));

    public static final RegistryObject<Item> TEST_TUBE_ITEM = ITEMS.register("test_tube", () -> new TestTubeItem(getItemProperties()));

    public static final RegistryObject<Item> IRON_FILTER = ITEMS.register("iron_filter", () -> new FilterItem(getItemProperties().durability(100), 0.25));
    public static final RegistryObject<Item> GOLD_FILTER = ITEMS.register("gold_filter", () -> new FilterItem(getItemProperties().durability(100),0.5));
    public static final RegistryObject<Item> DIAMOND_FILTER = ITEMS.register("diamond_filter", () -> new FilterItem(getItemProperties().durability(100),1));
    public static final RegistryObject<Item> DEV_FILTER = ITEMS.register("dev_filter", () -> new FilterItem(getItemProperties(),1));

    public static final RegistryObject<Item> IRON_TANK_UPGRADE = ITEMS.register("iron_tank_upgrade", () -> new TankItem(getItemProperties(), 3000, 2000, 24, 128));
    public static final RegistryObject<Item> GOLD_TANK_UPGRADE = ITEMS.register("gold_tank_upgrade", () -> new TankItem(getItemProperties(), 4000, 3000, 32, 192));
    public static final RegistryObject<Item> DIAMOND_TANK_UPGRADE = ITEMS.register("diamond_tank_upgrade", () -> new TankItem(getItemProperties(),8000, 4000, 40, -1));

    public static final RegistryObject<Item> IRON_COMPUTER_CHIP = ITEMS.register("iron_computer_chip", () -> new ComputerChipItem(getItemProperties(), 3*20*60, 7 * 20 * 60, 8 * 20 * 60));
    public static final RegistryObject<Item> GOLD_COMPUTER_CHIP = ITEMS.register("gold_computer_chip", () -> new ComputerChipItem(getItemProperties(),2*20*60, 4 * 60 * 20, 6 * 20 * 60));
    public static final RegistryObject<Item> DIAMOND_COMPUTER_CHIP = ITEMS.register("diamond_computer_chip", () -> new ComputerChipItem(getItemProperties(),20*60, -1, 4 * 20 * 60));
    public static final RegistryObject<Item> DEV_COMPUTER_CHIP = ITEMS.register("dev_computer_chip", () -> new ComputerChipItem(getItemProperties(),10, 10, 10));

    public static final RegistryObject<Item> HARD_DRIVE = ITEMS.register("hard_drive", () -> new DiskStorageItem(getItemProperties(), 10*20));
    public static final RegistryObject<Item> SSD = ITEMS.register("ssd", () -> new DiskStorageItem(getItemProperties(), 5*20));
    public static final RegistryObject<Item> DEV_SSD = ITEMS.register("dev_ssd", () -> new DiskStorageItem(getItemProperties(), 10));

    public static final RegistryObject<Item> SYRINGE = ITEMS.register("syringe", () -> new SyringeItem(getItemProperties()));
    public static final RegistryObject<Item> SEQUENCER_DOOR = registerSingleItem("sequencer_door");
    public static final RegistryObject<Item> SEQUENCER_SCREEN = registerSingleItem("sequencer_monitor");
    public static final RegistryObject<Item> SEQUENCER_COMPUTER = registerSingleItem("sequencer_computer");

    public static final RegistryObject<Item> CRACKED_ARTIFICIAL_EGG = ITEMS.register("cracked_artificial_egg", () -> new Item(getItemProperties().stacksTo(1)));
    public static final RegistryObject<Item> ARTIFICIAL_EGG = ITEMS.register("artificial_egg", () -> new Item(getItemProperties().stacksTo(1)));
    public static final RegistryObject<Item> UNINCUBATED_EGG = ITEMS.register("unincubated_egg", () -> new UnincubatedEggItem(getItemProperties()));
    public static final RegistryObject<Item> SMALL_CONTAINER_UPGRADE = ITEMS.register("small_container_upgrade", () -> new Item(getItemProperties()));
    public static final RegistryObject<Item> LARGE_CONTAINER_UPGRADE = ITEMS.register("large_container_upgrade", () -> new Item(getItemProperties()));
    public static final RegistryObject<Item> WARM_BULB = ITEMS.register("warm_bulb", () -> new BulbItem(getItemProperties(), 15*20));
    public static final RegistryObject<Item> WARMER_BULB = ITEMS.register("warmer_bulb", () -> new BulbItem(getItemProperties(), 12*20));
    public static final RegistryObject<Item> HOT_BULB = ITEMS.register("hot_bulb", () -> new BulbItem(getItemProperties(),9*20));
    public static final RegistryObject<Item> DEV_BULB = ITEMS.register("dev_bulb", () -> new BulbItem(getItemProperties(),2));
    public static final RegistryObject<Item> IRON_PLANT_TANK = ITEMS.register("iron_plant_tank_ugprade", () -> new PlantTankItem(getItemProperties(),128));
    public static final RegistryObject<Item> GOLD_PLANT_TANK = ITEMS.register("gold_plant_tank_upgrade", () -> new PlantTankItem(getItemProperties(), 192));
    public static final RegistryObject<Item> INCUBATOR_NEST = ITEMS.register("incubator_nest", () -> new Item(getItemProperties()));
    public static final RegistryObject<Item> INCUBATOR_LID = ITEMS.register("incubator_lid", () -> new Item(getItemProperties()));
    public static final RegistryObject<Item> INCUBATOR_ARM_BASE = ITEMS.register("incubator_arm_base", () -> new Item(getItemProperties()));
    public static final RegistryObject<Item> INCUBATOR_ARM = ITEMS.register("incubator_arm", () -> new Item(getItemProperties()));
    public static final RegistryObject<Item> INCUBATED_EGG = ITEMS.register("incubated_egg", () -> new IncubatedEggItem(getItemProperties()));
    public static final RegistryObject<Item> LEVELING_SENSOR = ITEMS.register("leveling_sensor", () -> new Item(getItemProperties()));
    public static RegistryObject<Item> registerSingleItem(String name) {
        return ITEMS.register(name, () -> new Item(getItemProperties().stacksTo(1)));
    }
    public static Item.Properties getItemProperties() {
        return new Item.Properties();
    }

    public static void loadClass() {
    }
}
