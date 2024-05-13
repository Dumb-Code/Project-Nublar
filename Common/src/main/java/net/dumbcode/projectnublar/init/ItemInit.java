package net.dumbcode.projectnublar.init;

import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.item.AmberItem;
import net.dumbcode.projectnublar.item.FossilItem;
import net.dumbcode.projectnublar.item.TankItem;
import net.dumbcode.projectnublar.item.TestTubeItem;
import net.dumbcode.projectnublar.item.ComputerChipItem;
import net.dumbcode.projectnublar.item.DiskStorageItem;
import net.dumbcode.projectnublar.item.FilterItem;
import net.dumbcode.projectnublar.item.SyringeItem;
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

    public static final RegistryObject<Item> IRON_TANK_UPGRADE = ITEMS.register("iron_tank_upgrade", () -> new TankItem(getItemProperties(), 3000));
    public static final RegistryObject<Item> GOLD_TANK_UPGRADE = ITEMS.register("gold_tank_upgrade", () -> new TankItem(getItemProperties(), 4000));
    public static final RegistryObject<Item> DIAMOND_TANK_UPGRADE = ITEMS.register("diamond_tank_upgrade", () -> new TankItem(getItemProperties(),8000));

    public static final RegistryObject<Item> IRON_COMPUTER_CHIP = ITEMS.register("iron_computer_chip", () -> new ComputerChipItem(getItemProperties(), 3*20*60));
    public static final RegistryObject<Item> GOLD_COMPUTER_CHIP = ITEMS.register("gold_computer_chip", () -> new ComputerChipItem(getItemProperties(),2*20*60));
    public static final RegistryObject<Item> DIAMOND_COMPUTER_CHIP = ITEMS.register("diamond_computer_chip", () -> new ComputerChipItem(getItemProperties(),20*60));
    public static final RegistryObject<Item> DEV_COMPUTER_CHIP = ITEMS.register("dev_computer_chip", () -> new ComputerChipItem(getItemProperties(),10));

    public static final RegistryObject<Item> HARD_DRIVE = ITEMS.register("hard_drive", () -> new DiskStorageItem(getItemProperties(), 10*20));
    public static final RegistryObject<Item> SSD = ITEMS.register("ssd", () -> new DiskStorageItem(getItemProperties(), 5*20));
    public static final RegistryObject<Item> DEV_SSD = ITEMS.register("dev_ssd", () -> new DiskStorageItem(getItemProperties(), 10));

    public static final RegistryObject<Item> SYRINGE = ITEMS.register("syringe", () -> new SyringeItem(getItemProperties()));

    public static Item.Properties getItemProperties() {
        return new Item.Properties();
    }

    public static void loadClass() {
    }
}
