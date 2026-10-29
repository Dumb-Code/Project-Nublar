package net.dumbcode.projectnublar.client.screen;

import com.mojang.datafixers.util.Pair;
import net.dumbcode.projectnublar.ProjectNublar;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.dinosaur.DNAData;
import net.dumbcode.projectnublar.api.dinosaur.DinoData;
import net.dumbcode.projectnublar.api.dinosaur.DiskStorage;
import net.dumbcode.projectnublar.api.gene.Genes;
import net.dumbcode.projectnublar.api.util.NublarMath;
import net.dumbcode.projectnublar.block.entity.SequencerBlockEntity;
import net.dumbcode.projectnublar.client.widget.*;
import net.dumbcode.projectnublar.container.ToggleSlot;
import net.dumbcode.projectnublar.entity.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.gui.widget.*;
import net.dumbcode.projectnublar.registry.DataComponentInit;
import net.dumbcode.projectnublar.registry.GeneInit;
import net.dumbcode.projectnublar.item.DiskStorageItem;
import net.dumbcode.projectnublar.menutypes.SequencerMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Screen for the sequencer. Behaves as a three-tab state machine ({@code currentTab}):
 * 0 = sequence, 1 = edit (basic/advanced sub-modes), 2 = synthesis. Tab switches are mirrored to
 * the server through {@code clickMenuButton} with the frozen ids in
 * {@link SequencerMenu.MenuButton}. Every coordinate and color below is a frozen layout value.
 *
 * <p>Note: the constructor reads the block entity from the client level (pre-existing
 * client/server mixing).
 */
public class SequencerScreen extends AbstractContainerScreen<SequencerMenu> {
    private static Identifier TEXTURE = Constants.modLoc("textures/gui/sequencer.png");
    private static Identifier TEXTURE_2 = Constants.modLoc("textures/gui/sequencer_page.png");
    private static Identifier INVENTORY = Constants.modLoc("textures/gui/inventory_overlay.png");
    private static Identifier CENTER = Constants.modLoc("textures/gui/center_pieces.png");
    private static Identifier SPIRAL = Constants.modLoc("textures/gui/dna_spiral.png");
    private static Identifier SYNTH = Constants.modLoc("textures/gui/synth_page.png");
    private static Identifier EDIT = Constants.modLoc("textures/gui/edit_page.png");
    ProgressWidget progressWidget;
    TextScrollBox textScrollBox;
    TextScrollBox isolatedTextScrollBox;
    private final float[] ringModifiers = new float[5];
    private ScrollingButtonListWidget<SequencerScreen> listWidget;
    private ScrollingButtonListWidget<SequencerScreen> isolatedWidget;
    private LivingEntity selectedDino;
    private LivingEntity sequencingDino;
    private static final int RING_SIZE = 175;
    private static final int RING_COUNT = 5;
    /** Number of secondary (non-base) DNA sliders; their construction is identical. */
    private static final int SUB_SLIDER_COUNT = 8;
    private static final int TAB_SEQUENCE = 0;
    private static final int TAB_EDIT = 1;
    private static final int TAB_SYNTH = 2;
    private int x;
    private int y;
    private int currentTab = TAB_SEQUENCE;
    private List<DNASlider> dNASliders = new ArrayList<>();
    private int activeSliders = 0;
    private int totalPercentage = 0;
    private ScrollingButtonListWidget<SequencerScreen> entityList;
    public DinoData dinoData = new DinoData();
    public BorderedButton beginButton;
    private boolean isAdvanced;
    public List<GeneButton> geneButtons = new ArrayList<>();
    public Genes.Gene selectedGene = null;
    public GeneHolder slider;

    public SequencerScreen(SequencerMenu processorMenu, Inventory inventory, Component component) {
        super(processorMenu, inventory, component, 351, 199);
        inventoryLabelY = -8200;
        titleLabelY = -8200;
        // Pre-existing client/server mixing: reads the block entity from the client level.
        dinoData = ((SequencerBlockEntity) Minecraft.getInstance().level
                .getBlockEntity(processorMenu.pos())).getDinoData();
        RandomSource random = RandomSource.create();
        for (int i = 0; i < this.ringModifiers.length; i++) {
            this.ringModifiers[i] = random.nextFloat() * 0.5F + 0.25F;
        }
        processorMenu.storageSlot.setActive(false);
        processorMenu.dnaInputSlot.setActive(false);
        processorMenu.emptyVialOutputSlot.setActive(false);
        processorMenu.waterInputSlot.setActive(false);
        processorMenu.waterInputDisplaySlot.setActive(false);
        processorMenu.boneMatterInputSlot.setActive(false);
        processorMenu.boneMatterInputDisplaySlot.setActive(false);
        processorMenu.sugarInputSlot.setActive(false);
        processorMenu.sugarInputDisplaySlot.setActive(false);
        processorMenu.plantMatterInputSlot.setActive(false);
        processorMenu.plantMatterInputDisplaySlot.setActive(false);
        processorMenu.emptyVialInputSlot.setActive(false);
        processorMenu.emptyVialInputDisplaySlot.setActive(false);
        processorMenu.dnaTestTubeOutputSlot.setActive(false);
        processorMenu.dnaTestTubeOutputDisplaySlot.setActive(false);
        processorMenu.inventorySlots.forEach(slot -> slot.setActive(false));
    }


    @Override
    protected void init() {
        super.init();
        dNASliders.clear();
        progressWidget = new ProgressWidget(leftPos + 56, topPos + 152, 241, 19, null, SPIRAL, -1, () -> {
            if (getMenu().getDataSlot(1) == 0)
                return 0F;
            return (getMenu().getDataSlot(0)) / (float) getMenu().getDataSlot(1);
        }, true, false) {
            @Override
            public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
                return false;
            }
        };
        this.addWidget(progressWidget);
        listWidget = new ScrollingButtonListWidget<>(this, leftPos + 36, topPos + 42, 150, 102, Component.empty()) {
            @Override
            protected void renderBackground(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
                SequencerScreen.drawBorder(pGuiGraphics, getX(), getY(), this.width, this.height, Constants.BORDER_COLOR, 1);
                pGuiGraphics.fill(getX() + 1, getY() + 1, getX() + this.width - 1, getY() + this.height - 1, 0xCF0F2234);
            }
        };
        isolatedWidget = new ScrollingButtonListWidget<>(this, leftPos + 36, topPos + 42, 150, 102, Component.empty()) {
            @Override
            protected void renderBackground(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
                SequencerScreen.drawBorder(pGuiGraphics, getX(), getY(), this.width, this.height, Constants.BORDER_COLOR, 1);
                pGuiGraphics.fill(getX() + 1, getY() + 1, getX() + this.width - 1, getY() + this.height - 1, 0xCF0F2234);
            }
        };
        this.addWidget(listWidget);
        this.addWidget(isolatedWidget);
        isolatedWidget.visible = false;
        textScrollBox = new TextScrollBox(leftPos + 210, topPos + 70, 120, 70, List.of());
        textScrollBox.setBorder(true, Constants.BORDER_COLOR, 1);
        textScrollBox.setBackgroundColor(0xCF193B59);
        isolatedTextScrollBox = new TextScrollBox(leftPos + 36 + 150 + 9, topPos + 42, 150, 98, List.of());
        isolatedTextScrollBox.setBorder(true, Constants.BORDER_COLOR, 1);
        isolatedTextScrollBox.setBackgroundColor(0xCF193B59);
        isolatedTextScrollBox.visible = false;
        DNASlider dnaSliderMain = new DNASlider(leftPos + 10, topPos, 85, 16, Component.empty(), Component.literal("%"), 0, 100, 0, 1, 0, true, (slider, value) -> {
            calculatePercentageTotals(slider);
        });

        dnaSliderMain.setConsumer((slider, selected) -> {
            if (selected) {
                entityList.clearButtons();
                if (menu.storageSlot.getItem().has(DataComponentInit.DISK_STORAGE.get())) {
                    // read only: component values are shared between stack copies
                    DiskStorage storage = DiskStorage.fromStack(menu.storageSlot.getItem());
                    storage.keys().forEach(
                            key -> {
                                DNAData dnaData = storage.get(key);
                                if (BuiltInRegistries.ENTITY_TYPE.getKey(dnaData.getEntityType()).getNamespace().equals(Constants.MODID)) {
                                    EntityWidget entityWidget = new EntityWidget(leftPos + 233, topPos + 25, 107, 20, dnaData, (button, selected2) -> {
                                        slider.setEntityType(dnaData.getEntityType());
                                        slider.setDNAData(dnaData);
                                        dinoData.setBaseDino(dnaData.getEntityType());
                                        sequencingDino = (LivingEntity) createPreview(dnaData.getEntityType());
                                        entityList.children().forEach(child -> {
                                            EntityWidget entityWidget1 = (EntityWidget) child;
                                            if (button != entityWidget1) {
                                                entityWidget1.setSelected(false);
                                            }
                                        });
                                    });
                                    if (dnaData.getEntityType() == slider.getEntityType()) {
                                        entityWidget.setSelected(true);
                                    }
                                    entityList.addButton(entityWidget, true, 0);
                                }
                            }
                    );
                }
            }
            dNASliders.forEach(slider1 -> {
                if (slider1 != slider) {
                    slider1.setSelected(false);
                }
            });
        });
        dnaSliderMain.setValidator((slider, oldValue, newValue) -> {
            double current = getCurrentDNAPercent() / 100d;
            double max = ((DNASlider) slider).maxDNA();
            double valueChange = newValue - oldValue;
            if (current + valueChange > max) {
                return false;
            }
            return true;
        });
        DNASlider.OnClick onClick = (slider, selected) -> {
            if (selected) {
                entityList.clearButtons();
                if (menu.storageSlot.getItem().has(DataComponentInit.DISK_STORAGE.get())) {
                    // read only: component values are shared between stack copies
                    DiskStorage storage = DiskStorage.fromStack(menu.storageSlot.getItem());
                    storage.keys().forEach(
                            key -> {
                                DNAData dnaData = storage.get(key);
                                if (!BuiltInRegistries.ENTITY_TYPE.getKey(dnaData.getEntityType()).getNamespace().equals(Constants.MODID)) {
                                    EntityWidget entityWidget = new EntityWidget(leftPos + 233, topPos + 25, 107, 20, dnaData, (button, selected2) -> {
                                        slider.setEntityType(dnaData.getEntityType());
                                        slider.setDNAData(dnaData);
                                        entityList.children().forEach(child -> {
                                            EntityWidget entityWidget1 = (EntityWidget) child;
                                            if (button != entityWidget1) {
                                                entityWidget1.setSelected(false);
                                            }
                                        });
                                    });
                                    if (dnaData.getEntityType() == slider.getEntityType()) {
                                        entityWidget.setSelected(true);
                                    }
                                    entityList.addButton(entityWidget, true, 0);
                                }
                            }
                    );
                }
            }
            dNASliders.forEach(slider1 -> {
                if (slider1 != slider) {
                    slider1.setSelected(false);
                }
            });
        };

        DNASlider.Validator validator = (slider, oldValue, newValue) -> {
            if ((getCurrentDNAPercent() / 100d) + NublarMath.round(newValue, 2) - NublarMath.round(oldValue, 2) > 1) {
                return false;
            }
            return !(newValue > ((DNASlider) slider).maxDNA());
        };
        dNASliders.add(dnaSliderMain);
        // The eight secondary sliders were byte-identical copy-paste; built in a loop instead.
        for (int i = 0; i < SUB_SLIDER_COUNT; i++) {
            DNASlider subSlider = new DNASlider(leftPos + 10, topPos, 85, 16, Component.empty(),
                    Component.literal("%"), 0, 50, 0, 1, 0, true,
                    (slider, value) -> calculatePercentageTotals(slider));
            dNASliders.add(subSlider);
            subSlider.setConsumer(onClick);
            subSlider.setValidator(validator);
        }
        dNASliders.forEach(this::addWidget);
        entityList = new ScrollingButtonListWidget<>(this, leftPos + 233, topPos + 25, 107, 98, Component.empty());
        this.addWidget(beginButton = BorderedButton.builder(Component.literal("Begin"), Component.literal("Cancel"), button -> {
                    Minecraft.getInstance().gameMode.handleInventoryButtonClick(
                            getMenu().containerId, SequencerMenu.MenuButton.TOGGLE_SYNTH.getId());
                })
                .width(100)
                .pos(leftPos + (imageWidth / 2) - 50, topPos + 20)
                .build());
        beginButton.setMessageConsumer(button -> {
            return ((SequencerBlockEntity) Minecraft.getInstance().level.getBlockEntity(menu.pos())).isSynthesizing();
        });
        int index = 0;
        int geneSize = GeneInit.GENES.getRegistrar().getIds().size();
        geneButtons.clear();
        List<Genes.Gene> geneList = GeneInit.getList();
        for (int ya = 0; ya < 20; ya++) {
            for (int xa = 0; xa < 4; xa++) {
                index = xa + (ya * 4);
                Genes.Gene gene = index < geneSize ? geneList.get(index) : null;
                GeneButton geneButton = new GeneButton(this, leftPos + 20 + (xa * 18), topPos + 30 + (ya * 7), gene);
                geneButton.active = (gene != null);
                geneButtons.add(geneButton);
                this.addWidget(geneButton);
            }
        }
        slider = new GeneSlider(leftPos + 235, topPos + 50, 100, 20, Component.empty(), Component.literal("%"), -100, 100, 0, true, (slider, value) -> {
            dinoData.setGeneValue(selectedGene, value);
        });
        this.addWidget((AbstractWidget) slider);
        this.addWidget(entityList);
        this.addWidget(textScrollBox);
        this.addWidget(isolatedTextScrollBox);
        buildGeneIsolationMap();
        if (currentTab == TAB_SEQUENCE) {
            enableSequenceScreen();
            disableEditScreen();
            disableSynthScreen();
        }
        if (currentTab == TAB_EDIT) {
            enableEditScreen();
            disableSequenceScreen();
            disableSynthScreen();
        }
        if (currentTab == TAB_SYNTH) {
            enableSynthScreen();
            disableSequenceScreen();
            disableEditScreen();
        }
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double pDragX, double pDragY) {
        if (this.getFocused() != null && this.isDragging() && event.button() == 0 && this.getFocused().mouseDragged(event, pDragX, pDragY)) {
            return true;
        }
        return super.mouseDragged(event, pDragX, pDragY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (showInventory()) {
            if (mouseX < leftPos + 87 || mouseX > leftPos + imageWidth - 87 || mouseY < topPos + 31 || mouseY > topPos + imageHeight - 31) {
                this.menu.inventorySlots.forEach(slot -> slot.setActive(false));
                this.menu.storageSlot.setActive(false);
                this.menu.dnaInputSlot.setActive(false);
                this.menu.emptyVialOutputSlot.setActive(false);
                this.menu.waterInputSlot.setActive(false);
                this.menu.boneMatterInputSlot.setActive(false);
                this.menu.sugarInputSlot.setActive(false);
                this.menu.plantMatterInputSlot.setActive(false);
                this.menu.emptyVialInputSlot.setActive(false);
                this.menu.dnaTestTubeOutputSlot.setActive(false);
                if (currentTab == TAB_SEQUENCE) {
                    enableSequenceScreen();
                }
                if (currentTab == TAB_EDIT) {
                    enableEditScreen();
                }
                if (currentTab == TAB_SYNTH) {
                    enableSynthScreen();
                }
                return super.mouseClicked(event, doubleClick);
            }
        }
        if (currentTab == TAB_SEQUENCE) {
            if (mouseX > leftPos + 113 && mouseX < leftPos + 176 && mouseY > topPos + 21 && mouseY < topPos + 33) {
                isolatedWidget.visible = false;
                listWidget.visible = true;
                textScrollBox.visible = true;
                isolatedTextScrollBox.visible = false;
                selectedDino = null;
                listWidget.getButtons().forEach(b -> ((SequenceDataDisplayWidget) b).selected = false);
                textScrollBox.setText(List.of());
            }
            if (mouseX > leftPos + 177 && mouseX < leftPos + 240 && mouseY > topPos + 21 && mouseY < topPos + 33) {
                isolatedWidget.visible = true;
                listWidget.visible = false;
                textScrollBox.visible = false;
                isolatedTextScrollBox.visible = true;
                selectedDino = null;
                isolatedWidget.getButtons().forEach(b -> ((IsolatedDataDisplayWidget) b).selected = false);
                isolatedTextScrollBox.setText(List.of());
                buildGeneIsolationMap();
            }
        }
        if (currentTab == TAB_EDIT) {
            if (mouseX >= leftPos + 15 && mouseX <= leftPos + 83) {
                if (mouseY >= topPos + 9 && mouseY <= topPos + 18) {
                    if (isAdvanced) {
                        disableAdvanced();
                        isAdvanced = false;
                        calculateSliders();
                    } else {
                        enableAdvanced();
                    }
                }
            }
        }
        // Tab header hitboxes; each switch is mirrored to the server with its frozen button id.
        if (mouseX < leftPos + 171 && mouseY < topPos + 15 && mouseY > topPos + 5 && mouseX > leftPos + 93) {
            currentTab = TAB_SEQUENCE;
            enableSequenceScreen();
            disableSynthScreen();
            disableEditScreen();
            Minecraft.getInstance().gameMode.handleInventoryButtonClick(
                    getMenu().containerId, SequencerMenu.MenuButton.SEQUENCE_TAB.getId());
        }
        if (mouseX < leftPos + 255 && mouseY < topPos + 15 && mouseY > topPos + 5 && mouseX > leftPos + 177) {
            currentTab = TAB_EDIT;
            disableSequenceScreen();
            disableSynthScreen();
            enableEditScreen();
            Minecraft.getInstance().gameMode.handleInventoryButtonClick(
                    getMenu().containerId, SequencerMenu.MenuButton.EDIT_TAB.getId());
        }
        if (mouseX < leftPos + 344 && mouseY < topPos + 15 && mouseY > topPos + 5 && mouseX > leftPos + 266) {
            currentTab = TAB_SYNTH;
            disableSequenceScreen();
            disableEditScreen();
            enableSynthScreen();
            Minecraft.getInstance().gameMode.handleInventoryButtonClick(
                    getMenu().containerId, SequencerMenu.MenuButton.SYNTH_TAB.getId());
        }
        return super.mouseClicked(event, doubleClick);
    }

    private void enableAdvanced() {
        geneButtons.forEach(button -> {
            button.active = button.type != null;
            if (button.type != null) {
                double completion = DiskStorageItem.getGeneCompletion(button.type, menu.storageSlot.getItem());
                button.active = completion >= button.type.requirement();
            }
        });
        isAdvanced = true;
        dNASliders.forEach(slider -> {
            slider.active = false;
        });
        listWidget.active = false;
        entityList.active = false;
        isolatedWidget.active = false;

    }

    private void disableAdvanced() {
        geneButtons.forEach(button -> {
            button.active = false;
            button.setSelected(false);
        });
        ((AbstractWidget)slider).active = false;
        ((AbstractWidget)slider).visible = false;
        selectedGene = null;
    }

    /**
     * Clicking a display slot toggles its real slot and deactivates the rest of its tab group;
     * the player inventory mirrors the toggled slot's state and every other widget is disabled
     * while it is open. The nine branches were byte-identical apart from the slot group.
     */
    @Override
    protected void slotClicked(Slot clickedSlot, int slotId, int mouseButton, ContainerInput clickType) {
        if (clickedSlot == getMenu().storageDisplaySlot) {
            toggleSlotExclusive(getMenu().storageSlot,
                    getMenu().dnaInputSlot, getMenu().emptyVialOutputSlot);
        } else if (clickedSlot == getMenu().dnaInputDisplaySlot) {
            toggleSlotExclusive(getMenu().dnaInputSlot,
                    getMenu().storageSlot, getMenu().emptyVialOutputSlot);
        } else if (clickedSlot == getMenu().emptyVialOutputDisplaySlot) {
            toggleSlotExclusive(getMenu().emptyVialOutputSlot,
                    getMenu().dnaInputSlot, getMenu().storageSlot);
        } else if (clickedSlot == getMenu().waterInputDisplaySlot) {
            toggleSlotExclusive(getMenu().waterInputSlot,
                    getMenu().boneMatterInputSlot, getMenu().sugarInputSlot,
                    getMenu().plantMatterInputSlot, getMenu().emptyVialInputSlot,
                    getMenu().dnaTestTubeOutputSlot);
        } else if (clickedSlot == getMenu().boneMatterInputDisplaySlot) {
            toggleSlotExclusive(getMenu().boneMatterInputSlot,
                    getMenu().waterInputSlot, getMenu().sugarInputSlot,
                    getMenu().plantMatterInputSlot, getMenu().emptyVialInputSlot,
                    getMenu().dnaTestTubeOutputSlot);
        } else if (clickedSlot == getMenu().sugarInputDisplaySlot) {
            toggleSlotExclusive(getMenu().sugarInputSlot,
                    getMenu().boneMatterInputSlot, getMenu().waterInputSlot,
                    getMenu().plantMatterInputSlot, getMenu().emptyVialInputSlot,
                    getMenu().dnaTestTubeOutputSlot);
        } else if (clickedSlot == getMenu().plantMatterInputDisplaySlot) {
            toggleSlotExclusive(getMenu().plantMatterInputSlot,
                    getMenu().boneMatterInputSlot, getMenu().sugarInputSlot,
                    getMenu().waterInputSlot, getMenu().emptyVialInputSlot,
                    getMenu().dnaTestTubeOutputSlot);
        } else if (clickedSlot == getMenu().emptyVialInputDisplaySlot) {
            toggleSlotExclusive(getMenu().emptyVialInputSlot,
                    getMenu().boneMatterInputSlot, getMenu().sugarInputSlot,
                    getMenu().plantMatterInputSlot, getMenu().waterInputSlot,
                    getMenu().dnaTestTubeOutputSlot);
        }

        if (getMenu().dnaTestTubeOutputDisplaySlot == clickedSlot) {
            toggleSlotExclusive(getMenu().dnaTestTubeOutputSlot,
                    getMenu().boneMatterInputSlot, getMenu().sugarInputSlot,
                    getMenu().plantMatterInputSlot, getMenu().waterInputSlot,
                    getMenu().emptyVialInputSlot);
        }

        super.slotClicked(clickedSlot, slotId, mouseButton, clickType);
    }

    private void toggleSlotExclusive(ToggleSlot toggled, ToggleSlot... others) {
        toggled.toggleActive();
        for (ToggleSlot other : others) {
            other.setActive(false);
        }
        getMenu().inventorySlots.forEach(slot -> slot.setActive(toggled.isActive()));
        this.children().forEach(child -> ((AbstractWidget) child).active = !toggled.isActive());
    }

    public void disableSequenceScreen() {
        isolatedWidget.visible = false;
        listWidget.visible = false;
        textScrollBox.visible = false;
        isolatedTextScrollBox.visible = false;
        menu.dnaInputDisplaySlot.setActive(false);
        isolatedWidget.getButtons().forEach(b -> ((IsolatedDataDisplayWidget) b).selected = false);
        isolatedTextScrollBox.setText(List.of());
        listWidget.getButtons().forEach(b -> ((SequenceDataDisplayWidget) b).selected = false);
        textScrollBox.setText(List.of());
        menu.slots.forEach(slot -> ((ToggleSlot) slot).setActive(false));
        selectedDino = null;
    }

    public void enableSequenceScreen() {
        isolatedWidget.visible = false;
        listWidget.visible = true;
        listWidget.active = true;
        textScrollBox.visible = true;
        textScrollBox.active = true;
        isolatedTextScrollBox.visible = false;
        isolatedWidget.getButtons().forEach(b -> {
            ((IsolatedDataDisplayWidget) b).selected = false;
            b.active = true;
        });
        isolatedTextScrollBox.setText(List.of());
        listWidget.getButtons().forEach(b -> {
            ((SequenceDataDisplayWidget) b).selected = false;
            b.active = true;
        });

        textScrollBox.setText(List.of());
        menu.dnaInputDisplaySlot.setActive(true);
        menu.emptyVialOutputDisplaySlot.setActive(true);
        menu.storageDisplaySlot.setActive(true);
    }

    public void enableEditScreen() {
        calculateSliders();
        if (isAdvanced) {
            enableAdvanced();
        } else {
            disableAdvanced();
        }
    }

    public void disableEditScreen() {
        dNASliders.forEach(slider -> slider.visible = false);
        disableAdvanced();
    }

    public void enableSynthScreen() {
        menu.boneMatterInputDisplaySlot.setActive(true);
        menu.emptyVialInputDisplaySlot.setActive(true);
        menu.dnaTestTubeOutputDisplaySlot.setActive(true);
        menu.waterInputDisplaySlot.setActive(true);
        menu.sugarInputDisplaySlot.setActive(true);
        menu.plantMatterInputDisplaySlot.setActive(true);
        this.beginButton.active = true;

    }

    public void disableSynthScreen() {
        menu.boneMatterInputDisplaySlot.setActive(false);
        menu.emptyVialInputDisplaySlot.setActive(false);
        menu.dnaTestTubeOutputDisplaySlot.setActive(false);
        menu.waterInputDisplaySlot.setActive(false);
        menu.sugarInputDisplaySlot.setActive(false);
        menu.plantMatterInputDisplaySlot.setActive(false);
        this.beginButton.active = false;

    }

    /**
     * The dimmed background (super), the frame, the turning rings and the open tab. The tab's
     * widgets are drawn here too, so they sit under the slots, as on 1.20.1.
     */
    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
        x = this.leftPos;
        y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 351, 398);
        int ringStartX = (this.imageWidth - RING_SIZE) / 2;
        int ringStartY = (this.imageHeight - RING_SIZE) / 2;
        // 1.20.1 Minecraft.getFrameTime(): the partial tick of the running game, not this method's frame delta.
        float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        for (int ring = 0; ring < 5; ring++) {
            int u = (ring % 3) * RING_SIZE;
            int v = (ring / 3) * RING_SIZE;

            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate(this.leftPos, this.topPos);
            guiGraphics.pose().translate(this.imageWidth / 2F, this.imageHeight / 2F);
            guiGraphics.pose().rotate(Mth.DEG_TO_RAD * ((minecraft.player.tickCount + partialTick) * (ring % 2 == 0 ? 1 : -1) * this.ringModifiers[ring] + 0.5F));
            guiGraphics.pose().translate(-this.imageWidth / 2F, -this.imageHeight / 2F);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CENTER, ringStartX, ringStartY, u, v, RING_SIZE, RING_SIZE, 525, 350);
            guiGraphics.pose().popMatrix();
        }
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CENTER, this.leftPos + (this.imageWidth - 63) / 2, this.topPos + (this.imageHeight - 63) / 2, RING_SIZE * 2, RING_SIZE, 63, 63, 525, 350);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, imageHeight, imageWidth, imageHeight, 351, 398);
        switch (currentTab) {
            case TAB_SEQUENCE:
                renderSequenceScreen(guiGraphics, partialTicks, mouseX, mouseY);
                break;
            case TAB_EDIT:
                renderEditScreen(guiGraphics, partialTicks, mouseX, mouseY);
                if (isAdvanced) {
                    renderAdvancedEditScreen(guiGraphics, partialTicks, mouseX, mouseY);
                } else {
                    renderBasicEditScreen(guiGraphics, partialTicks, mouseX, mouseY);
                }
                break;
            case TAB_SYNTH:
                renderSynthScreen(guiGraphics, partialTicks, mouseX, mouseY);
                break;
        }
    }

    public void renderSequenceScreen(GuiGraphicsExtractor guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE_2, x, y, 0, 0, imageWidth, imageHeight, 472, 199);
        progressWidget.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        if (showInventory()) {
            guiGraphics.enableScissor(this.leftPos, this.topPos, this.leftPos + 87, this.topPos + height);
        }
        listWidget.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        isolatedWidget.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        if (showInventory()) {
            guiGraphics.disableScissor();
            guiGraphics.enableScissor(this.leftPos + 261, this.topPos, this.leftPos + width, this.topPos + height);
        }
        textScrollBox.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        isolatedTextScrollBox.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        if (showInventory()) {
            guiGraphics.disableScissor();
            guiGraphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, INVENTORY, x, y, 0, 0, imageWidth, imageHeight, 351, 199);
        }
        // The labels and the turning dinosaur come after the inventory dim and overlay: on 1.20.1
        // depth kept them in front of both (P76). Nothing above overlaps the labels, and the dinosaur
        // still comes after the text boxes it overlaps.
        Component text = Component.translatable("button." + Constants.MODID + ".regular");
        int textWidth = this.font.width(text);
        guiGraphics.text(this.font, Component.translatable("button." + Constants.MODID + ".regular"), this.leftPos + 174 - textWidth, this.topPos + 22, 0xFFFFFFFF);
        guiGraphics.text(this.font, Component.translatable("button." + Constants.MODID + ".isolated"), this.leftPos + 180, this.topPos + 22, 0xFFFFFFFF);
        if (selectedDino != null) {
            if (showInventory()) {
                guiGraphics.enableScissor(this.leftPos + 261, this.topPos, this.leftPos + width, this.topPos + height);
            }
            extractEntity(guiGraphics, this.leftPos + 270, this.topPos + 68, 10, new Quaternionf().rotateZ((float) Math.PI).rotateY(minecraft.level.getGameTime() * 5f * (float) (Math.PI / 180f)), selectedDino);
            if (showInventory()) {
                guiGraphics.disableScissor();
            }
        }
    }

    public void renderEditScreen(GuiGraphicsExtractor guiGraphics, float partialTicks, int mouseX, int mouseY) {
        drawBorder(guiGraphics, leftPos + 105, topPos + 24, 222 - 106 + 2, 101, Constants.BORDER_COLOR, 1);
        guiGraphics.fill(leftPos + 106, topPos + 25, leftPos + 222, topPos + 123, 0xCF193B59);
        if (sequencingDino != null) {
            ((Dinosaur) sequencingDino).setDinoData(dinoData);
            // The dinosaur and the player share one texture and depth buffer, so they hide each other
            // by depth as on 1.20.1 (P75). The box is the 1.20.1 scissor window; the player is 10 units
            // nearer, as 1.20.1 moved it.
            extractEntityGroup(new GuiEntityGroupRenderState(List.of(
                    new GuiEntityGroupRenderState.Entry(previewRenderState(sequencingDino), leftPos + 105 + 55, topPos + 25 + 90, 0,
                            new Quaternionf().rotateZ((float) Math.PI).rotateY(90)),
                    new GuiEntityGroupRenderState.Entry(previewRenderState(Minecraft.getInstance().player), leftPos + 105 + 70 + 5, topPos + 25 + 90 + 2, 10,
                            new Quaternionf().rotateZ((float) Math.PI).rotateY(135))),
                    leftPos + 105, topPos + 25, leftPos + 222, topPos + 123, 18));
        }
        guiGraphics.fill(leftPos + 106, topPos + 136, leftPos + 339, topPos + 176, 0xCF0F2234);
        drawBorder(guiGraphics, leftPos + 105, topPos + 135, 339 - 106 + 2, 42, Constants.BORDER_COLOR, 1);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, EDIT, leftPos, topPos, 0, 0, 351, 199, 351, 199);
    }

    public void renderBasicEditScreen(GuiGraphicsExtractor guiGraphics, float partialTicks, int mouseX, int mouseY) {
        drawBorder(guiGraphics, leftPos + 232, topPos + 24, 109, 100, Constants.BORDER_COLOR, 1);
        guiGraphics.fill(leftPos + 233, topPos + 25, leftPos + 340, topPos + 123, 0xCF0F2234);
        dNASliders.forEach(dnaSlider -> dnaSlider.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks));
        entityList.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.centeredText(this.font, Component.literal("Advanced"), leftPos + 49, topPos + 10, 0xFFFFFFFF);
    }

    public void renderSynthScreen(GuiGraphicsExtractor guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, SYNTH, x, y, 0, 0, imageWidth, imageHeight, 479, 199);
        float percent = getMenu().getDataSlot(6) / (float) getMenu().getDataSlot(9);
        int yOffset = Mth.floor(128 - (128 * percent));
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, SYNTH, x + (imageWidth / 2) - 64, y + (imageHeight / 2) - 63 + yOffset, 351, yOffset, 128, Mth.floor(128 * percent), 479, 199);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, SYNTH, x + 159, y + 171, 351, 136, Mth.floor(33 * percent), 11, 479, 199);
        beginButton.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderSynthIngredientBar(guiGraphics, leftPos + 9, topPos + 68, menu.getDataSlot(2) / (float) menu.getDataSlot(8));
        this.renderSynthIngredientBar(guiGraphics, leftPos + 9, topPos + 68 + 70, menu.getDataSlot(4) / (float) menu.getDataSlot(7));
        this.renderSynthIngredientBar(guiGraphics, leftPos + 9 + 236, topPos + 68, menu.getDataSlot(3) / (float) menu.getDataSlot(7));
        this.renderSynthIngredientBar(guiGraphics, leftPos + 9 + 236, topPos + 68 + 70, menu.getDataSlot(5) / (float) menu.getDataSlot(7));
        if (showInventory()) {
            guiGraphics.enableScissor(this.leftPos + 87, this.topPos + 31, this.leftPos + 267, this.topPos + 167);
        }
        if (showInventory()) {
            guiGraphics.disableScissor();
            guiGraphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, INVENTORY, x, y, 0, 0, imageWidth, imageHeight, imageWidth, 199);
        }
        // The labels come after the inventory dim and overlay: on 1.20.1 depth kept them in front of
        // both (P76). Nothing drawn earlier in this tab overlaps them.
        guiGraphics.centeredText(this.font, Component.literal("Water"), leftPos + 58, topPos + 58, 0xFFFFFFFF);
        guiGraphics.centeredText(this.font, Component.literal("Bone Matter"), leftPos + 58 + 236, topPos + 58, 0xFFFFFFFF);
        guiGraphics.centeredText(this.font, Component.literal("Plant Matter"), leftPos + 58 + 236, topPos + 58 + 70, 0xFFFFFFFF);
        guiGraphics.centeredText(this.font, Component.literal("Sugar"), leftPos + 58, topPos + 58 + 70, 0xFFFFFFFF);
    }

    public void renderAdvancedEditScreen(GuiGraphicsExtractor guiGraphics, float partialTicks, int mouseX, int mouseY) {
        geneButtons.forEach(geneButton -> geneButton.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks));
        guiGraphics.centeredText(this.font, Component.literal("Basic"), leftPos + 49, topPos + 10, 0xFFFFFFFF);
        if (selectedGene != null) {
            guiGraphics.centeredText(this.font, selectedGene.getTooltip(), leftPos + 285, topPos + 25, 0xFFFFFFFF);
            ((AbstractWidget) slider).extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    public boolean showInventory() {
        return getMenu().storageSlot.isActive() ||
                getMenu().dnaInputSlot.isActive() ||
                getMenu().emptyVialOutputSlot.isActive() ||
                getMenu().waterInputSlot.isActive() ||
                getMenu().boneMatterInputSlot.isActive() ||
                getMenu().sugarInputSlot.isActive() ||
                getMenu().plantMatterInputSlot.isActive() ||
                getMenu().emptyVialInputSlot.isActive() ||
                getMenu().dnaTestTubeOutputSlot.isActive();
    }

    /** The tab titles go after the slots and under the carried item, as 1.20.1 drew them after its container pass. */
    @Override
    public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {

        super.extractContents(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.centeredText(this.font, Component.translatable("gui_tab." + Constants.MODID + ".sequence"), leftPos + 131, topPos + 5, 0xFFFFFFFF);
        guiGraphics.centeredText(this.font, Component.translatable("gui_tab." + Constants.MODID + ".edit"), leftPos + 215, topPos + 5, 0xFFFFFFFF);
        guiGraphics.centeredText(this.font, Component.translatable("gui_tab." + Constants.MODID + ".synthesis"), leftPos + 304, topPos + 5, 0xFFFFFFFF);
        progressWidget.setTooltip(Tooltip.create((Component.literal(StringUtil.formatTickDuration(Mth.floor(getMenu().getDataSlot(1) - (progressWidget.progress.get() * getMenu().getDataSlot(1))), SharedConstants.TICKS_PER_SECOND)))));
    }

    /** Draws a frame of {@code thickness} pixels just inside the given rectangle. */
    public static void drawBorder(GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height, int color, int thickness) {
        guiGraphics.fill(x, y, x + width, y + thickness, color);
        guiGraphics.fill(x, y, x + thickness, y + height, color);
        guiGraphics.fill(x + width - thickness, y, x + width, y + height, color);
        guiGraphics.fill(x, y + height - thickness, x + width, y + height, color);
    }

    /** Fills the rectangle at ({@code x}, {@code y}) with the given width and height. */
    public static void fill(GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height, int color) {
        guiGraphics.fill(x, y, x + width, y + height, color);
    }

    @Override
    protected void containerTick() {
        if (getMenu().storageSlot.getItem().has(DataComponentInit.DISK_STORAGE.get())) {
            // read only: component values are shared between stack copies
            DiskStorage storage = DiskStorage.fromStack(getMenu().storageSlot.getItem());
            if (storage.keys().size() != listWidget.size()) {
                listWidget.clearButtons();
                storage.keys().forEach(key -> {
                    listWidget.addButton(new SequenceDataDisplayWidget(0, 0, 120, 14, () -> getMenu().storageSlot.getItem(), key, (button, selected) -> {
                        if (selected) {
                            // read only: component values are shared between stack copies
                            DNAData data = DiskStorage.fromStack(getMenu().storageSlot.getItem()).getOrMissing(button.getValue());
                            textScrollBox.setText(List.of(
                                            data.getFormattedType().withStyle(ChatFormatting.GOLD),
                                            Component.literal("Time Period").withStyle(ChatFormatting.UNDERLINE),
                                            Component.literal("Cretaceous").withStyle(ChatFormatting.AQUA),
                                            Component.literal("Diet").withStyle(ChatFormatting.UNDERLINE),
                                            Component.literal("Apple")
                                    )
                            );
                            selectedDino = (LivingEntity) createPreview(data.getEntityType());
                            listWidget.getButtons().forEach(b -> {
                                if (b != button) {
                                    ((SequenceDataDisplayWidget) b).selected = false;
                                }
                            });
                        } else {
                            textScrollBox.setText(List.of());
                            selectedDino = null;
                        }
                    }), true, 0);
                });
            }
        } else if (listWidget.size() > 0) {
            listWidget.clearButtons();
        }
    }

    List<NGLSlider> alreadyChanged = new ArrayList<>();


    public void calculatePercentageTotals(NGLSlider slider) {
        if (dNASliders.get(0).getValue() == 100 && slider != dNASliders.get(0)) {
            slider.setValue(0, true);
            return;
        }
        int currentDNAPercent = getCurrentDNAPercent();
        boolean greaterThan100 = currentDNAPercent > 100;
        while (greaterThan100) {
            if (alreadyChanged.size() >= dNASliders.size() - 2) {
                alreadyChanged.clear();
            }
            for (int i = 1; i < dNASliders.size(); i++) {
                DNASlider dNASlider = dNASliders.get(i);
                if (alreadyChanged.contains(dNASlider)) {
                    continue;
                }
//                if (dNASlider == slider) {
//                    continue;
//                }
                currentDNAPercent = getCurrentDNAPercent();
                greaterThan100 = currentDNAPercent > 100;
                if (!greaterThan100) {
                    break;
                }
                dNASlider.setValue(dNASlider.getValue() - 1, true);
                alreadyChanged.add(dNASlider);
            }
            greaterThan100 = getCurrentDNAPercent() > 100;
        }
        dinoData.addEntity(((DNASlider) slider).getDnaData().getEntityInfo(), slider.getValue());
    }

    public int getCurrentDNAPercent() {
        return (int) dNASliders.stream().mapToDouble(DNASlider::getValue).sum();
    }

    public void buildGeneIsolationMap() {
        isolatedWidget.clearButtons();
        if (!getMenu().storageSlot.getItem().has(DataComponentInit.DISK_STORAGE.get())) {
            return;
        }
        // read only: component values are shared between stack copies
        DiskStorage storage = DiskStorage.fromStack(getMenu().storageSlot.getItem());
        GeneInit.GENES.getRegistrar().forEach(gene -> {
            double totalPercent = 0;
            String geneInfo = ProjectNublar.checkReplace(gene.name()) + " ";
            for (String key : storage.keys()) {
                DNAData data = storage.get(key);
                if (Genes.GENE_STORAGE.get(gene).stream().map(Pair::getFirst).toList().contains(data.getEntityType())) {
                    totalPercent += data.getDnaPercentage();
                }
            }
            double genePercent = totalPercent / Genes.GENE_STORAGE.get(gene).size();
            if (totalPercent > 0) {
                isolatedWidget.addButton(new IsolatedDataDisplayWidget(0, 0, 120, 14, geneInfo + NublarMath.round(genePercent * 100, 2) + "%", (button, selected) -> {
                    if (selected) {
                        List<Component> dinoDnas = new ArrayList<>();
                        List<EntityType<?>> entities = new ArrayList<>(Genes.GENE_STORAGE.get(gene).stream().map(Pair::getFirst).toList());
                        // read only: component values are shared between stack copies
                        DiskStorage current = DiskStorage.fromStack(getMenu().storageSlot.getItem());
                        for (String key : current.keys()) {
                            DNAData data = current.get(key);
                            if (Genes.GENE_STORAGE.get(gene).stream().map(Pair::getFirst).toList().contains(data.getEntityType())) {
                                entities.remove(data.getEntityType());
                                dinoDnas.add(data.getFormattedType().append(" ").append(data.getFormattedDNANoDescriptor()));
                            }
                        }
                        for (EntityType<?> entity : entities) {
                            dinoDnas.add(entity.getDescription().copy().append(" ").append("0%"));
                        }
                        isolatedTextScrollBox.setText(dinoDnas);
                        isolatedWidget.getButtons().forEach(b -> {

                            if (b != button) {
                                ((IsolatedDataDisplayWidget) b).selected = false;
                            }
                        });
                    } else {
                        isolatedTextScrollBox.setText(List.of());
                    }
                }), true, 0);
            }
        });
    }

    public void renderSynthIngredientBar(GuiGraphicsExtractor guiGraphics, int x, int y, float percent) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, SYNTH, x, y, 351, 128, Mth.floor(98 * percent), 8, 479, 199);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (currentTab == TAB_EDIT) {
            this.menu.sendUpdate(dinoData);
        }
        return super.mouseReleased(event);
    }

    public void calculateSliders() {
        activeSliders = 3;
        if (dinoData.getBaseDino() != EntityTypes.PIG) {
            DNASlider slider = dNASliders.get(0);
            ItemStack stack = menu.storageSlot.getItem();
            if (stack.has(DataComponentInit.DISK_STORAGE.get())) {
                // read only: component values are shared between stack copies
                DNAData data = DiskStorage.fromStack(stack).getOrMissing(DNAData.createStorageKey(dinoData.getBaseDino(), null));
                slider.setDNAData(data);
                double value = dinoData.getEntityPercentage(new DinoData.EntityInfo(dinoData.getBaseDino(), null));
                slider.setValue(value, true);
                slider.setEntityType(dinoData.getBaseDino());
                slider.active = true;
                sequencingDino = (LivingEntity) createPreview(data.getEntityType());
                activeSliders = Mth.floor((data.getDnaPercentage() - 0.5d) / (0.5 / 6)) + 3;
            }

        }
        int yOffset = (int) ((activeSliders / 2f) * 18f) - 3;
        int startY = topPos + (imageHeight / 2) - yOffset;
        entityList.active = true;
        for (int i = 0; i < dNASliders.size(); i++) {
            dNASliders.get(i).visible = i < activeSliders;
            dNASliders.get(i).setY(startY);
            startY += 18;
            dNASliders.get(i).active = true;
        }

        for (Map.Entry<DinoData.EntityInfo, Double> entry : dinoData.getEntityPercentages().entrySet()) {
            if (entry.getKey().type() != dinoData.getBaseDino()) {
                boolean shouldAdd = true;
                for (DNASlider slider : dNASliders) {
                    if (slider.getEntityType() == entry.getKey().type()) {
                        shouldAdd = false;
                    }
                    if (slider.getEntityType() == null && shouldAdd) {
                        slider.setEntityType(entry.getKey().type());
                        slider.setValue(entry.getValue(), true);
                        // read only: component values are shared between stack copies
                        DNAData data = DiskStorage.fromStack(menu.storageSlot.getItem()).getOrMissing(DNAData.createStorageKey(entry.getKey().type(), entry.getKey().variant()));
                        slider.setDNAData(data);
                        slider.active = true;
                        shouldAdd = false;
                    }
                }
            }
        }
    }

    /**
     * Creates a preview entity in the client level. Like 1.20.1 {@code EntityType.create(Level)},
     * only the type's feature flags are checked: 26.2's {@code create(Level, EntitySpawnReason)}
     * would also refuse a monster on Peaceful.
     *
     * <p>The preview gets the id -1, as vanilla's spawner display entity does
     * ({@code BaseSpawner.SET_DISPLAY_ENTITY_ID}). A 26.2 entity takes its id from
     * {@code Level.getNextEntityId()}, which is 0 on the client (only the server hands out ids),
     * and {@code getId()} throws on 0. The renderer reads the id of every living entity it draws.
     */
    private @Nullable Entity createPreview(EntityType<?> type) {
        Level level = this.minecraft.level;
        if (!type.isEnabled(level.enabledFeatures())) {
            return null;
        }
        Entity entity = type.create(level, new EntitySpawnRequest(EntitySpawnReason.LOAD, true));
        if (entity != null) {
            entity.setId(-1);
        }
        return entity;
    }

    /**
     * Draws a living entity as 1.20.1 {@code InventoryScreen.renderEntityInInventory} did: its feet
     * at ({@code x}, {@code y}), {@code scale} GUI pixels per block, turned by {@code pose}. 26.2
     * draws a GUI entity into a texture of its own; here that texture covers the whole screen, so
     * only the current scissor cuts the entity, as before. One such entity per frame: a second
     * {@code graphics.entity} call would reuse the same texture ({@link GuiEntityGroupRenderState}).
     */
    private void extractEntity(GuiGraphicsExtractor graphics, int x, int y, int scale, Quaternionf pose, LivingEntity entity) {
        Vector3f feet = new Vector3f((x - this.width / 2.0F) / scale, (y - this.height / 2.0F) / scale, 0.0F);
        graphics.entity(previewRenderState(entity), scale, feet, pose, null, 0, 0, this.width, this.height);
    }

    /**
     * Adds a group of entities to this frame. {@code GuiGraphicsExtractor} has no public method for a
     * mod's picture-in-picture state, but a screen extracts into the game's one GUI render state
     * ({@code Minecraft} gives {@code gameRenderState().guiRenderState} to {@code Gui}, which builds
     * the screen's extractor on it), and {@code graphics.entity} adds its state there the same way.
     */
    private void extractEntityGroup(GuiEntityGroupRenderState group) {
        this.minecraft.gameRenderer.gameRenderState().guiRenderState.addPicturesInPictureState(group);
    }

    /** The entity's render state as 1.20.1 drew screen entities: no shadow, no outline, full light. */
    private EntityRenderState previewRenderState(LivingEntity entity) {
        EntityRenderState renderState = this.minecraft.getEntityRenderDispatcher().getRenderer(entity).createRenderState(entity, 1.0F);
        renderState.shadowPieces.clear();
        renderState.outlineColor = 0;
        renderState.lightCoords = LightCoordsUtil.FULL_BRIGHT;
        return renderState;
    }

    public int leftPos() {
        return leftPos;
    }

    public int topPos() {
        return topPos;
    }

    // These two overrides only widen visibility to public so GeneButton can add/remove the gene
    // slider on this screen.
    @Override
    public <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget) {
        return super.addRenderableWidget(widget);
    }

    @Override
    public void removeWidget(GuiEventListener listener) {
        super.removeWidget(listener);
    }
}
