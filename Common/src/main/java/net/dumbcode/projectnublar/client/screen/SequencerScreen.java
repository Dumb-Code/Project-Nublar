package net.dumbcode.projectnublar.client.screen;

import com.mojang.math.Axis;
import com.nyfaria.nyfsguilib.client.widgets.NGLSlider;
import com.nyfaria.nyfsguilib.client.widgets.ScrollingButtonListWidget;
import net.dumbcode.projectnublar.CommonClass;
import net.dumbcode.projectnublar.Constants;
import net.dumbcode.projectnublar.api.DNAData;
import net.dumbcode.projectnublar.api.DinoData;
import net.dumbcode.projectnublar.api.Genes;
import net.dumbcode.projectnublar.api.NublarMath;
import net.dumbcode.projectnublar.block.entity.SequencerBlockEntity;
import net.dumbcode.projectnublar.client.widget.BorderedButton;
import net.dumbcode.projectnublar.client.widget.DNASlider;
import net.dumbcode.projectnublar.client.widget.EntityWidget;
import net.dumbcode.projectnublar.client.widget.GeneButton;
import net.dumbcode.projectnublar.client.widget.IsolatedDataDisplayWidget;
import net.dumbcode.projectnublar.client.widget.ProgressWidget;
import net.dumbcode.projectnublar.client.widget.SequenceDataDisplayWidget;
import net.dumbcode.projectnublar.client.widget.TextScrollBox;
import net.dumbcode.projectnublar.container.ToggleSlot;
import net.dumbcode.projectnublar.entity.Dinosaur;
import net.dumbcode.projectnublar.item.DiskStorageItem;
import net.dumbcode.projectnublar.menutypes.SequencerMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SequencerScreen extends AbstractContainerScreen<SequencerMenu> {
    private static ResourceLocation TEXTURE = Constants.modLoc("textures/gui/sequencer.png");
    private static ResourceLocation TEXTURE_2 = Constants.modLoc("textures/gui/sequencer_page.png");
    private static ResourceLocation INVENTORY = Constants.modLoc("textures/gui/inventory_overlay.png");
    private static ResourceLocation CENTER = Constants.modLoc("textures/gui/center_pieces.png");
    private static ResourceLocation SPIRAL = Constants.modLoc("textures/gui/dna_spiral.png");
    private static ResourceLocation SYNTH = Constants.modLoc("textures/gui/synth_page.png");
    private static ResourceLocation EDIT = Constants.modLoc("textures/gui/edit_page.png");
    ProgressWidget progressWidget;
    TextScrollBox textScrollBox;
    TextScrollBox isolatedTextScrollBox;
    private final float[] ringModifiers = new float[5];
    private ScrollingButtonListWidget<SequencerScreen> listWidget;
    private ScrollingButtonListWidget<SequencerScreen> isolatedWidget;
    private LivingEntity selectedDino;
    private LivingEntity sequencingDino;
    private static final int RING_SIZE = 175;
    private int x;
    private int y;
    private int currentTab = 0;
    private List<DNASlider> dNASliders = new ArrayList<>();
    private int activeSliders = 0;
    private int totalPercentage = 0;
    private ScrollingButtonListWidget<SequencerScreen> entityList;
    public DinoData dinoData = new DinoData();
    public BorderedButton beginButton;
    private boolean isAdvanced;
    public List<GeneButton> geneButtons = new ArrayList<>();
    public Genes.Gene selectedGene = null;
    public NGLSlider slider;

    public SequencerScreen(SequencerMenu processorMenu, Inventory inventory, Component component) {
        super(processorMenu, inventory, component);
        inventoryLabelY = -8200;
        titleLabelY = -8200;
        imageHeight = 199;
        imageWidth = 351;
        dinoData = ((SequencerBlockEntity) Minecraft.getInstance().level.getBlockEntity(processorMenu.getPos())).getDinoData();
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
            public boolean mouseClicked(double $$0, double $$1, int $$2) {
                return false;
            }
        };
        this.addWidget(progressWidget);
        listWidget = new ScrollingButtonListWidget<>(this, leftPos + 36, topPos + 42, 150, 102, Component.empty()) {
            @Override
            protected void renderBackground(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
                SequencerScreen.drawBorder(pGuiGraphics, getX(), getY(), this.width, this.height, Constants.BORDER_COLOR, 1);
                pGuiGraphics.fill(getX() + 1, getY() + 1, getX() + this.width - 1, getY() + this.height - 1, 0xCF0F2234);
            }
        };
        isolatedWidget = new ScrollingButtonListWidget<>(this, leftPos + 36, topPos + 42, 150, 102, Component.empty()) {
            @Override
            protected void renderBackground(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
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
                if (menu.storageSlot.getItem().hasTag()) {
                    menu.storageSlot.getItem().getTag().getAllKeys().forEach(
                            key -> {
                                DNAData dnaData = DNAData.loadFromNBT(menu.storageSlot.getItem().getTag().getCompound(key));
                                if (BuiltInRegistries.ENTITY_TYPE.getKey(dnaData.getEntityType()).getNamespace().equals(Constants.MODID)) {
                                    EntityWidget entityWidget = new EntityWidget(leftPos + 233, topPos + 25, 107, 20, dnaData, (button, selected2) -> {
                                        slider.setEntityType(dnaData.getEntityType());
                                        slider.setDNAData(dnaData);
                                        dinoData.setBaseDino(dnaData.getEntityType());
                                        sequencingDino = (LivingEntity) dnaData.getEntityType().create(Minecraft.getInstance().level);
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
                if (menu.storageSlot.getItem().hasTag()) {
                    menu.storageSlot.getItem().getTag().getAllKeys().forEach(
                            key -> {
                                DNAData dnaData = DNAData.loadFromNBT(menu.storageSlot.getItem().getTag().getCompound(key));
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
        DNASlider dnaSliderSub1 = new DNASlider(leftPos + 10, topPos, 85, 16, Component.empty(), Component.literal("%"), 0, 50, 0, 1, 0, true, (slider, value) -> {
            calculatePercentageTotals(slider);
        });
        dNASliders.add(dnaSliderSub1);
        dnaSliderSub1.setConsumer(onClick);
        dnaSliderSub1.setValidator(validator);
        DNASlider dnaSliderSub2 = new DNASlider(leftPos + 10, topPos, 85, 16, Component.empty(), Component.literal("%"), 0, 50, 0, 1, 0, true, (slider, value) -> {
            calculatePercentageTotals(slider);
        });
        dNASliders.add(dnaSliderSub2);
        dnaSliderSub2.setConsumer(onClick);
        dnaSliderSub2.setValidator(validator);
        DNASlider dnaSliderSub3 = new DNASlider(leftPos + 10, topPos, 85, 16, Component.empty(), Component.literal("%"), 0, 50, 0, 1, 0, true, (slider, value) -> {
            calculatePercentageTotals(slider);
        });
        dNASliders.add(dnaSliderSub3);
        dnaSliderSub3.setConsumer(onClick);
        dnaSliderSub3.setValidator(validator);
        DNASlider dnaSliderSub4 = new DNASlider(leftPos + 10, topPos, 85, 16, Component.empty(), Component.literal("%"), 0, 50, 0, 1, 0, true, (slider, value) -> {
            calculatePercentageTotals(slider);
        });
        dNASliders.add(dnaSliderSub4);
        dnaSliderSub4.setConsumer(onClick);
        dnaSliderSub4.setValidator(validator);
        DNASlider dnaSliderSub5 = new DNASlider(leftPos + 10, topPos, 85, 16, Component.empty(), Component.literal("%"), 0, 50, 0, 1, 0, true, (slider, value) -> {
            calculatePercentageTotals(slider);
        });
        dNASliders.add(dnaSliderSub5);
        dnaSliderSub5.setConsumer(onClick);
        dnaSliderSub5.setValidator(validator);
        DNASlider dnaSliderSub6 = new DNASlider(leftPos + 10, topPos, 85, 16, Component.empty(), Component.literal("%"), 0, 50, 0, 1, 0, true, (slider, value) -> {
            calculatePercentageTotals(slider);
        });
        dNASliders.add(dnaSliderSub6);
        dnaSliderSub6.setConsumer(onClick);
        dnaSliderSub6.setValidator(validator);
        DNASlider dnaSliderSub7 = new DNASlider(leftPos + 10, topPos, 85, 16, Component.empty(), Component.literal("%"), 0, 50, 0, 1, 0, true, (slider, value) -> {
            calculatePercentageTotals(slider);
        });
        dNASliders.add(dnaSliderSub7);
        dnaSliderSub7.setConsumer(onClick);
        dnaSliderSub7.setValidator(validator);
        DNASlider dnaSliderSub8 = new DNASlider(leftPos + 10, topPos, 85, 16, Component.empty(), Component.literal("%"), 0, 50, 0, 1, 0, true, (slider, value) -> {
            calculatePercentageTotals(slider);
        });
        dNASliders.add(dnaSliderSub8);
        dnaSliderSub8.setConsumer(onClick);
        dnaSliderSub8.setValidator(validator);
        dNASliders.forEach(this::addWidget);
        entityList = new ScrollingButtonListWidget<>(this, leftPos + 233, topPos + 25, 107, 98, Component.empty());
        this.addWidget(beginButton = BorderedButton.builder(Component.literal("Begin"), Component.literal("Cancel"), button -> {
                    Minecraft.getInstance().gameMode.handleInventoryButtonClick(getMenu().containerId, 99);
                })
                .width(100)
                .pos(leftPos + (imageWidth / 2) - 50, topPos + 20)
                .build());
        beginButton.setMessageConsumer(button -> {
            return ((SequencerBlockEntity) Minecraft.getInstance().level.getBlockEntity(menu.getPos())).isSynthesizing();
        });
        int index = 0;
        for (int ya = 0; ya < 20; ya++) {
            for (int xa = 0; xa < 4; xa++) {
                index = xa + (ya * 4);
                Genes.Gene gene = index < Genes.GENE_STORAGE.size() ? Genes.GENE_STORAGE.get(index) : null;
                GeneButton geneButton = new GeneButton(this, leftPos + 20 + (xa * 18), topPos + 30 + (ya * 7), index < Genes.GENE_STORAGE.size() ? Genes.GENE_STORAGE.get(index) : null);
                geneButton.active = (gene != null);
                geneButtons.add(geneButton);
                this.addWidget(geneButton);
            }
        }
        slider = new NGLSlider(leftPos + 235, topPos + 50, 100, 20, Component.empty(), Component.literal("%"), -100, 100, 0, true, (slider, value) -> {
            dinoData.setGeneValue(selectedGene, value);
        });
        this.addWidget(slider);
        this.addWidget(entityList);
        this.addWidget(textScrollBox);
        this.addWidget(isolatedTextScrollBox);
        buildGeneIsolationMap();
        if (currentTab == 0) {
            enableSequenceScreen();
            disableEditScreen();
            disableSynthScreen();
        }
        if (currentTab == 1) {
            enableEditScreen();
            disableSequenceScreen();
            disableSynthScreen();
        }
        if (currentTab == 2) {
            enableSynthScreen();
            disableSequenceScreen();
            disableEditScreen();
        }
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (this.getFocused() != null && this.isDragging() && pButton == 0 && this.getFocused().mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)) {
            return true;
        }
        return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int buttonCode) {
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
                if (currentTab == 0) {
                    enableSequenceScreen();

                }
                if (currentTab == 1) {
                    enableEditScreen();
                }
                if (currentTab == 2) {
                    enableSynthScreen();
                }
                return super.mouseClicked(mouseX, mouseY, buttonCode);
            }
        }
        if (currentTab == 0) {
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
        if (currentTab == 1) {
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
        if (mouseX < leftPos + 171 && mouseY < topPos + 15 && mouseY > topPos + 5 && mouseX > leftPos + 93) {
            currentTab = 0;
            enableSequenceScreen();
            disableSynthScreen();
            disableEditScreen();
            Minecraft.getInstance().gameMode.handleInventoryButtonClick(getMenu().containerId, 100);
        }
        if (mouseX < leftPos + 255 && mouseY < topPos + 15 && mouseY > topPos + 5 && mouseX > leftPos + 177) {
            currentTab = 1;
            disableSequenceScreen();
            disableSynthScreen();
            enableEditScreen();
            Minecraft.getInstance().gameMode.handleInventoryButtonClick(getMenu().containerId, 101);
        }
        if (mouseX < leftPos + 344 && mouseY < topPos + 15 && mouseY > topPos + 5 && mouseX > leftPos + 266) {
            currentTab = 2;
            disableSequenceScreen();
            disableEditScreen();
            enableSynthScreen();
            Minecraft.getInstance().gameMode.handleInventoryButtonClick(getMenu().containerId, 102);
        }

//        menu.boneMatterInputSlot.setActive(false);
//        menu.emptyVialOutputSlot.setActive(false);
//        menu.dnaTestTubeOutputSlot.setActive(false);
//        menu.waterInputSlot.setActive(false);
//        menu.sugarInputSlot.setActive(false);
//        menu.plantMatterInputSlot.setActive(false);
        return super.mouseClicked(mouseX, mouseY, buttonCode);
    }

    private void enableAdvanced() {
        geneButtons.forEach(button -> {
            button.active = button.type != null;
            if (button.type != null)
                button.active = DiskStorageItem.getGeneCompletion(button.type, menu.storageSlot.getItem()) >= 1;
        });
        isAdvanced = true;
        dNASliders.forEach(slider -> {
            slider.active = false;
        });

    }

    private void disableAdvanced() {
        geneButtons.forEach(button -> button.active = false);

    }

    @Override
    protected void slotClicked(Slot pSlot, int pSlotId, int pMouseButton, ClickType pType) {
        if (pSlot == getMenu().storageDisplaySlot) {
            getMenu().storageSlot.toggleActive();
            getMenu().dnaInputSlot.setActive(false);
            getMenu().emptyVialOutputSlot.setActive(false);
            getMenu().inventorySlots.forEach(slot -> slot.setActive(getMenu().storageSlot.isActive()));
            this.children().forEach(child -> ((AbstractWidget) child).active = !getMenu().storageSlot.isActive());
        } else if (pSlot == getMenu().dnaInputDisplaySlot) {
            getMenu().dnaInputSlot.toggleActive();
            getMenu().storageSlot.setActive(false);
            getMenu().emptyVialOutputSlot.setActive(false);
            getMenu().inventorySlots.forEach(slot -> slot.setActive(getMenu().dnaInputSlot.isActive()));
            this.children().forEach(child -> ((AbstractWidget) child).active = !getMenu().dnaInputSlot.isActive());
        } else if (pSlot == getMenu().emptyVialOutputDisplaySlot) {
            getMenu().emptyVialOutputSlot.toggleActive();
            getMenu().dnaInputSlot.setActive(false);
            getMenu().storageSlot.setActive(false);
            getMenu().inventorySlots.forEach(slot -> slot.setActive(getMenu().emptyVialOutputSlot.isActive()));
            this.children().forEach(child -> ((AbstractWidget) child).active = !getMenu().emptyVialOutputSlot.isActive());
        } else if (pSlot == getMenu().waterInputDisplaySlot) {
            getMenu().waterInputSlot.toggleActive();
            getMenu().boneMatterInputSlot.setActive(false);
            getMenu().sugarInputSlot.setActive(false);
            getMenu().plantMatterInputSlot.setActive(false);
            getMenu().emptyVialInputSlot.setActive(false);
            getMenu().dnaTestTubeOutputSlot.setActive(false);
            getMenu().inventorySlots.forEach(slot -> slot.setActive(getMenu().waterInputSlot.isActive()));
            this.children().forEach(child -> ((AbstractWidget) child).active = !getMenu().waterInputSlot.isActive());
        } else if (pSlot == getMenu().boneMatterInputDisplaySlot) {
            getMenu().boneMatterInputSlot.toggleActive();
            getMenu().waterInputSlot.setActive(false);
            getMenu().sugarInputSlot.setActive(false);
            getMenu().plantMatterInputSlot.setActive(false);
            getMenu().emptyVialInputSlot.setActive(false);
            getMenu().dnaTestTubeOutputSlot.setActive(false);
            getMenu().inventorySlots.forEach(slot -> slot.setActive(getMenu().boneMatterInputSlot.isActive()));
            this.children().forEach(child -> ((AbstractWidget) child).active = !getMenu().boneMatterInputSlot.isActive());
        } else if (pSlot == getMenu().sugarInputDisplaySlot) {
            getMenu().sugarInputSlot.toggleActive();
            getMenu().boneMatterInputSlot.setActive(false);
            getMenu().waterInputSlot.setActive(false);
            getMenu().plantMatterInputSlot.setActive(false);
            getMenu().emptyVialInputSlot.setActive(false);
            getMenu().dnaTestTubeOutputSlot.setActive(false);
            getMenu().inventorySlots.forEach(slot -> slot.setActive(getMenu().sugarInputSlot.isActive()));
            this.children().forEach(child -> ((AbstractWidget) child).active = !getMenu().sugarInputSlot.isActive());
        } else if (pSlot == getMenu().plantMatterInputDisplaySlot) {
            getMenu().plantMatterInputSlot.toggleActive();
            getMenu().boneMatterInputSlot.setActive(false);
            getMenu().sugarInputSlot.setActive(false);
            getMenu().waterInputSlot.setActive(false);
            getMenu().emptyVialInputSlot.setActive(false);
            getMenu().dnaTestTubeOutputSlot.setActive(false);
            getMenu().inventorySlots.forEach(slot -> slot.setActive(getMenu().plantMatterInputSlot.isActive()));
            this.children().forEach(child -> ((AbstractWidget) child).active = !getMenu().plantMatterInputSlot.isActive());
        } else if (pSlot == getMenu().emptyVialInputDisplaySlot) {
            getMenu().emptyVialInputSlot.toggleActive();
            getMenu().boneMatterInputSlot.setActive(false);
            getMenu().sugarInputSlot.setActive(false);
            getMenu().plantMatterInputSlot.setActive(false);
            getMenu().waterInputSlot.setActive(false);
            getMenu().dnaTestTubeOutputSlot.setActive(false);
            getMenu().inventorySlots.forEach(slot -> slot.setActive(getMenu().emptyVialInputSlot.isActive()));
            this.children().forEach(child -> ((AbstractWidget) child).active = !getMenu().emptyVialInputSlot.isActive());
        }
        if (getMenu().dnaTestTubeOutputDisplaySlot == pSlot) {
            getMenu().dnaTestTubeOutputSlot.toggleActive();
            getMenu().boneMatterInputSlot.setActive(false);
            getMenu().sugarInputSlot.setActive(false);
            getMenu().plantMatterInputSlot.setActive(false);
            getMenu().waterInputSlot.setActive(false);
            getMenu().emptyVialInputSlot.setActive(false);
            getMenu().inventorySlots.forEach(slot -> slot.setActive(getMenu().dnaTestTubeOutputSlot.isActive()));
            this.children().forEach(child -> ((AbstractWidget) child).active = !getMenu().dnaTestTubeOutputSlot.isActive());
        }

        super.slotClicked(pSlot, pSlotId, pMouseButton, pType);
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
        if(isAdvanced){
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

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        x = this.leftPos;
        y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 351, 398);
        int ringStartX = (this.imageWidth - RING_SIZE) / 2;
        int ringStartY = (this.imageHeight - RING_SIZE) / 2;
        for (int ring = 0; ring < 5; ring++) {
            int u = (ring % 3) * RING_SIZE;
            int v = (ring / 3) * RING_SIZE;

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(this.leftPos, this.topPos, 0);
            guiGraphics.pose().translate(this.imageWidth / 2F, this.imageHeight / 2F, 0);
            guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees((minecraft.player.tickCount + minecraft.getFrameTime()) * (ring % 2 == 0 ? 1 : -1) * this.ringModifiers[ring] + 0.5F));
            guiGraphics.pose().translate(-this.imageWidth / 2F, -this.imageHeight / 2F, 0);
            guiGraphics.blit(CENTER, ringStartX, ringStartY, u, v, RING_SIZE, RING_SIZE, 525, 350);
            guiGraphics.pose().popPose();
        }
        guiGraphics.blit(CENTER, this.leftPos + (this.imageWidth - 63) / 2, this.topPos + (this.imageHeight - 63) / 2, RING_SIZE * 2, RING_SIZE, 63, 63, 525, 350);
        guiGraphics.blit(TEXTURE, x, y, 0, imageHeight, imageWidth, imageHeight, 351, 398);
        switch (currentTab) {
            case 0:
                renderSequenceScreen(guiGraphics, partialTicks, mouseX, mouseY);
                break;
            case 1:
                renderEditScreen(guiGraphics, partialTicks, mouseX, mouseY);
                if (isAdvanced) {
                    renderAdvancedEditScreen(guiGraphics, partialTicks, mouseX, mouseY);
                } else {
                    renderBasicEditScreen(guiGraphics, partialTicks, mouseX, mouseY);
                }
                break;
            case 2:
                renderSynthScreen(guiGraphics, partialTicks, mouseX, mouseY);
                break;
        }

    }

    public void renderSequenceScreen(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        Component text = Component.translatable("button." + Constants.MODID + ".regular");
        int textWidth = this.font.width(text);
        guiGraphics.drawString(this.font, Component.translatable("button." + Constants.MODID + ".regular"), this.leftPos + 174 - textWidth, this.topPos + 22, 0xFFFFFFFF);
        guiGraphics.drawString(this.font, Component.translatable("button." + Constants.MODID + ".isolated"), this.leftPos + 180, this.topPos + 22, 0xFFFFFFFF);

        guiGraphics.blit(TEXTURE_2, x, y, 0, 0, imageWidth, imageHeight, 472, 199);
        progressWidget.render(guiGraphics, mouseX, mouseY, partialTicks);
        if (showInventory()) {
            guiGraphics.enableScissor(this.leftPos, this.topPos, this.leftPos + 87, this.topPos + height);
        }
        listWidget.render(guiGraphics, mouseX, mouseY, partialTicks);
        isolatedWidget.render(guiGraphics, mouseX, mouseY, partialTicks);
        if (showInventory()) {
            guiGraphics.disableScissor();
            guiGraphics.enableScissor(this.leftPos + 261, this.topPos, this.leftPos + width, this.topPos + height);
        }
        textScrollBox.render(guiGraphics, mouseX, mouseY, partialTicks);
        isolatedTextScrollBox.render(guiGraphics, mouseX, mouseY, partialTicks);
        if (selectedDino != null) {
            InventoryScreen.renderEntityInInventory(guiGraphics, this.leftPos + 270, this.topPos + 68, 10, new Quaternionf().rotateZ((float) Math.PI).rotateY(minecraft.level.getGameTime() * 5f * (float) (Math.PI / 180f)), null, selectedDino);
        }
        if (showInventory()) {
            guiGraphics.disableScissor();
            guiGraphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
            guiGraphics.blit(INVENTORY, x, y, 0, 0, imageWidth, imageHeight, 351, 199);
        }
    }

    public void renderEditScreen(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        drawBorder(guiGraphics, leftPos + 105, topPos + 24, 222 - 106 + 2, 101, Constants.BORDER_COLOR, 1);
        guiGraphics.fill(leftPos + 106, topPos + 25, leftPos + 222, topPos + 123, 0xCF193B59);
        if (sequencingDino != null) {
            guiGraphics.enableScissor(leftPos + 105, topPos + 25, leftPos + 222, topPos + 123);
            ((Dinosaur)sequencingDino).setDinoData(dinoData);
            InventoryScreen.renderEntityInInventory(guiGraphics, leftPos + 105 + 55, topPos + 25 + 90, 18, new Quaternionf().rotateZ((float) Math.PI).rotateY(90), null, sequencingDino);
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 10);
            InventoryScreen.renderEntityInInventory(guiGraphics, leftPos + 105 + 70 + 5, topPos + 25 + 90 + 2, 18, new Quaternionf().rotateZ((float) Math.PI).rotateY(135), null, Minecraft.getInstance().player);
            guiGraphics.pose().popPose();
            guiGraphics.disableScissor();
        }
        guiGraphics.fill(leftPos + 106, topPos + 136, leftPos + 339, topPos + 176, 0xCF0F2234);
        drawBorder(guiGraphics, leftPos + 105, topPos + 135, 339 - 106 + 2, 42, Constants.BORDER_COLOR, 1);
        guiGraphics.blit(EDIT, leftPos, topPos, 0, 0, 351, 199, 351, 199);
    }

    public void renderBasicEditScreen(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        drawBorder(guiGraphics, leftPos + 232, topPos + 24, 109, 100, Constants.BORDER_COLOR, 1);
        guiGraphics.fill(leftPos + 233, topPos + 25, leftPos + 340, topPos + 123, 0xCF0F2234);
        dNASliders.forEach(dnaSlider -> dnaSlider.render(guiGraphics, mouseX, mouseY, partialTicks));
        entityList.render(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.drawCenteredString(this.font, Component.literal("Advanced"), leftPos + 49, topPos + 10, 0xFFFFFFFF);
    }

    public void renderSynthScreen(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        guiGraphics.blit(SYNTH, x, y, 0, 0, imageWidth, imageHeight, 479, 199);
        guiGraphics.drawCenteredString(this.font, Component.literal("Water"), leftPos + 58, topPos + 58, 16777215);
        guiGraphics.drawCenteredString(this.font, Component.literal("Bone Matter"), leftPos + 58 + 236, topPos + 58, 16777215);
        guiGraphics.drawCenteredString(this.font, Component.literal("Plant Matter"), leftPos + 58 + 236, topPos + 58 + 70, 16777215);
        guiGraphics.drawCenteredString(this.font, Component.literal("Sugar"), leftPos + 58, topPos + 58 + 70, 16777215);
        float percent = getMenu().getDataSlot(6) / (float) getMenu().getDataSlot(9);
        int yOffset = Mth.floor(128 - (128 * percent));
        guiGraphics.blit(SYNTH, x + (imageWidth / 2) - 64, y + (imageHeight / 2) - 63 + yOffset, 351, yOffset, 128, Mth.floor(128 * percent), 479, 199);
        guiGraphics.blit(SYNTH, x + 159, y + 171, 351, 136, Mth.floor(33 * percent), 11, 479, 199);
        beginButton.render(guiGraphics, mouseX, mouseY, partialTicks);
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
            guiGraphics.blit(INVENTORY, x, y, 0, 0, imageWidth, imageHeight, imageWidth, 199);
        }
    }

    public void renderAdvancedEditScreen(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        geneButtons.forEach(geneButton -> geneButton.render(guiGraphics, mouseX, mouseY, partialTicks));
        guiGraphics.drawCenteredString(this.font, Component.literal("Basic"), leftPos + 49, topPos + 10, 0xFFFFFFFF);
        if(selectedGene != null) {
            guiGraphics.drawCenteredString(this.font, selectedGene.getTooltip(), leftPos + 285, topPos + 25, 0xFFFFFFFF);
            slider.render(guiGraphics, mouseX, mouseY, partialTicks);
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

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui_tab." + Constants.MODID + ".sequence"), leftPos + 131, topPos + 5, 16777215);
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui_tab." + Constants.MODID + ".edit"), leftPos + 215, topPos + 5, 16777215);
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui_tab." + Constants.MODID + ".synthesis"), leftPos + 304, topPos + 5, 16777215);
        progressWidget.setTooltip(Tooltip.create((Component.literal(StringUtil.formatTickDuration(Mth.floor(getMenu().getDataSlot(1) - (progressWidget.progress.get() * getMenu().getDataSlot(1))))))));
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    public static void drawBorder(GuiGraphics guiGraphics, int x, int y, int width, int height, int color, int thickness) {
        guiGraphics.fill(x, y, x + width, y + thickness, color);
        guiGraphics.fill(x, y, x + thickness, y + height, color);
        guiGraphics.fill(x + width - thickness, y, x + width, y + height, color);
        guiGraphics.fill(x, y + height - thickness, x + width, y + height, color);
    }

    public static void fill(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        guiGraphics.fill(x, y, x + width, y + height, color);
    }

    @Override
    protected void containerTick() {
        if (getMenu().storageSlot.getItem().hasTag()) {
            if (getMenu().storageSlot.getItem().getTag().getAllKeys().size() != listWidget.size()) {
                listWidget.clearButtons();
                getMenu().storageSlot.getItem().getTag().getAllKeys().forEach(key -> {
                    listWidget.addButton(new SequenceDataDisplayWidget(0, 0, 120, 14, () -> getMenu().storageSlot.getItem(), key, (button, selected) -> {
                        if (selected) {
                            DNAData data = DNAData.loadFromNBT(getMenu().storageSlot.getItem().getTag().getCompound(button.getValue()));
                            textScrollBox.setText(List.of(
                                            data.getFormattedType().withStyle(ChatFormatting.GOLD),
                                            Component.literal("Time Period").withStyle(ChatFormatting.UNDERLINE),
                                            Component.literal("Cretaceous").withStyle(ChatFormatting.AQUA),
                                            Component.literal("Diet").withStyle(ChatFormatting.UNDERLINE),
                                            Component.literal("Apple")
                                    )
                            );
                            selectedDino = (LivingEntity) DNAData.loadFromNBT(getMenu().storageSlot.getItem().getTag().getCompound(button.getValue())).getEntityType().create(Minecraft.getInstance().level);
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
        if (!getMenu().storageSlot.getItem().hasTag()) {
            return;
        }
        for (Genes.Gene gene : Genes.GENE_STORAGE) {
            double totalPercent = 0;
            String geneInfo = CommonClass.checkReplace(gene.name()) + " ";
            for (String key : getMenu().storageSlot.getItem().getTag().getAllKeys()) {
                DNAData data = DNAData.loadFromNBT(getMenu().storageSlot.getItem().getTag().getCompound(key));
                if (gene.entities().containsKey(data.getEntityType())) {
                    totalPercent += data.getDnaPercentage();
                }
            }
            double genePercent = totalPercent / gene.entities().size();
            if (totalPercent > 0) {
                isolatedWidget.addButton(new IsolatedDataDisplayWidget(0, 0, 120, 14, geneInfo + NublarMath.round(genePercent * 100, 2) + "%", (button, selected) -> {
                    if (selected) {
                        List<Component> dinoDnas = new ArrayList<>();
                        List<EntityType<?>> entities = new ArrayList<>();
                        entities.addAll(gene.entities().keySet());
                        for (String key : getMenu().storageSlot.getItem().getTag().getAllKeys()) {
                            DNAData data = DNAData.loadFromNBT(getMenu().storageSlot.getItem().getTag().getCompound(key));
                            if (gene.entities().containsKey(data.getEntityType())) {
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
        }
    }

    public void renderSynthIngredientBar(GuiGraphics guiGraphics, int x, int y, float percent) {
        guiGraphics.blit(SYNTH, x, y, 351, 128, Mth.floor(98 * percent), 8, 479, 199);
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (currentTab == 1) {
            this.menu.sendUpdate(dinoData);
        }
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    public void calculateSliders() {
        activeSliders = 3;
        if (dinoData.getBaseDino() != EntityType.PIG) {
            DNASlider slider = dNASliders.get(0);
            ItemStack stack = menu.storageSlot.getItem();
            if (stack.hasTag()) {
                DNAData data = DNAData.loadFromNBT(stack.getTag().getCompound(DNAData.createStorageKey(dinoData.getBaseDino(), null)));
                slider.setDNAData(data);
                double value = dinoData.getEntityPercentage(new DinoData.EntityInfo(dinoData.getBaseDino(), null));
                slider.setValue(value, true);
                slider.setEntityType(dinoData.getBaseDino());
                slider.active = true;
                sequencingDino = (LivingEntity) data.getEntityType().create(Minecraft.getInstance().level);
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
                    if(slider.getEntityType() == entry.getKey().type()) {
                        shouldAdd = false;
                    }
                    if (slider.getEntityType() == null && shouldAdd) {
                        slider.setEntityType(entry.getKey().type());
                        slider.setValue(entry.getValue(), true);
                        DNAData data = DNAData.loadFromNBT(menu.storageSlot.getItem().getTag().getCompound(DNAData.createStorageKey(entry.getKey().type(), entry.getKey().variant())));
                        slider.setDNAData(data);
                        slider.active = true;
                        shouldAdd = false;
                    }
                }
            }
        }
    }
}
