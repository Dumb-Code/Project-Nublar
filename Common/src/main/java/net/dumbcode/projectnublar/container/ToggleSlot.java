package net.dumbcode.projectnublar.container;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

public class ToggleSlot extends Slot {
    boolean active;
    Function<ItemStack, Boolean> validator;

    public ToggleSlot(Container pContainer, int pSlot, int pX, int pY) {
        this(pContainer, pSlot, pX, pY, (stack) -> true);
    }
    public ToggleSlot(Container pContainer, int pSlot, int pX, int pY, boolean active) {
        this(pContainer, pSlot, pX, pY, active, (stack) -> true);
    }
    public ToggleSlot(Container pContainer, int pSlot, int pX, int pY, Function<ItemStack, Boolean> validator) {
        this(pContainer, pSlot, pX, pY, true, validator);
    }

    public ToggleSlot(Container pContainer, int pSlot, int pX, int pY, boolean active, Function<ItemStack, Boolean> validator) {
        super(pContainer, pSlot, pX, pY);
        this.active = active;
        this.validator = validator;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return active && validator.apply(stack);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void toggleActive() {
        active = !active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
