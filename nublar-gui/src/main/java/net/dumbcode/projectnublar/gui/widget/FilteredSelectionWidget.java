package net.dumbcode.projectnublar.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class FilteredSelectionWidget<T, E extends FilteredSelectionWidget.SelectionEntry<T>> extends AbstractWidget implements GuiEventListener {
    private static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/resource_packs.png");
    private static final int ENTRY_HEIGHT = 18;

    private final Component title;
    private final Consumer<T> selectCallback;
    private final List<E> entries = new ArrayList<>();
    private final Font font;
    private List<E> entriesFiltered = new ArrayList<>();
    private E selected = null;
    private boolean extended = false;
    private int scrollOffset = 0;
    /**
     * Has the current text being edited on the textbox.
     */
    private String value = "";
    private int maxLength = 32;
    private int frame;
    private boolean bordered = true;
    /**
     * if true the textbox can lose focus by clicking elsewhere on the screen
     */
    private boolean canLoseFocus = true;
    /**
     * If this value is true along with isFocused, keyTyped will process the keys.
     */
    private boolean isEditable = true;
    private boolean shiftPressed;
    /**
     * The current character index that should be used as start of the rendered text.
     */
    private int displayPos;
    private int cursorPos;
    /**
     * other selection position, maybe the same as the cursor
     */
    private int highlightPos;
    private int textColor = 14737632;
    private int textColorUneditable = 7368816;
    @Nullable
    private String suggestion;
    @Nullable
    private Consumer<String> responder;
    /**
     * Called to check if the text is valid
     */
    private BiPredicate<String, E> filter;
    private BiFunction<String, Integer, FormattedCharSequence> formatter = (p_94147_, p_94148_) -> FormattedCharSequence.forward(p_94147_, Style.EMPTY);

    public FilteredSelectionWidget(int x, int y, int width, Component title, Consumer<T> selectCallback) {
        super(x, y, width, ENTRY_HEIGHT, Component.literal(""));
        this.title = title;
        this.selectCallback = selectCallback;
        this.font = Minecraft.getInstance().font;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderFilter(guiGraphics, mouseX, mouseY, partialTick);
        entriesFiltered = entries.stream().filter(this::testFilter).toList();
        int x = this.getX();
        int y = this.getY();

        // render(guiGraphics, x, y, this.width, this.height);

        if (extended) {
            int boxHeight = Math.max(1, ENTRY_HEIGHT * Math.min(entriesFiltered.size(), 4)) + 2;

            guiGraphics.pose().pushPose();

            guiGraphics.pose().translate(0, 0, 100);

            guiGraphics.fill(x, y + ENTRY_HEIGHT - 1, x + width, y + ENTRY_HEIGHT + boxHeight - 1, 0xFFFFFFFF);
            guiGraphics.fill(x + 1, y + ENTRY_HEIGHT, x + width - 1, y + ENTRY_HEIGHT + boxHeight - 2, 0xFF000000);

            guiGraphics.blit(ICON_OVERLAY_LOCATION, x + width - 14, y + 1, 114, 5, 11, 7);

            E hoverEntry = getEntryAtPosition(mouseX, mouseY);

            for (int i = 0; i < 4; i++) {
                int idx = i + scrollOffset;
                if (idx < entriesFiltered.size()) {
                    int entryY = y + ((i + 1) * ENTRY_HEIGHT);

                    E entry = entriesFiltered.get(idx);
                    entry.render(guiGraphics, null, x + 1, entryY, width - 2, entry == hoverEntry, this.active ? 16777215 : 10526880, alpha);
                }
            }

            if (entriesFiltered.size() > 4) {
                int scrollY = Math.round(y + ENTRY_HEIGHT + (scrollOffset * ENTRY_HEIGHT * 4F / entriesFiltered.size()));
                int barHeight = Math.round(4F * ENTRY_HEIGHT * 4F / entriesFiltered.size());

                guiGraphics.fill(x + width - 5, scrollY, x + width - 1, scrollY + barHeight, 0xFF666666);
                guiGraphics.fill(x + width - 4, scrollY + 1, x + width - 2, scrollY + barHeight - 1, 0xFFAAAAAA);
            }

            guiGraphics.pose().popPose();
        } else {
            guiGraphics.blit(ICON_OVERLAY_LOCATION, x + width - 14, y + 1, 82, 20, 11, 7);
        }
    }

    protected void render(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        if (selected != null) {
            selected.render(guiGraphics, title, x, y, width, false, this.active ? 16777215 : 10526880, alpha);
        } else {
            guiGraphics.drawString(font, title, x + 6, y + (height - 8) / 2, this.active ? 16777215 : 10526880 | Mth.ceil(alpha * 255.0F) << 24, false);
        }
    }

    @Override
    public int getHeight() {
        if (extended) {
            return ENTRY_HEIGHT * (Math.min(entries.size(), 4) + 1) + 1;
        }
        return ENTRY_HEIGHT;
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        int x = this.getX();
        int y = this.getY();
        if (visible && active && pMouseX >= (x + width - 17) && pMouseX <= x + width && pMouseY >= y && pMouseY <= y + getHeight()) {
            int maxX = x + width - (entries.size() > 4 ? 5 : 0);
            int maxY = y + ENTRY_HEIGHT * Math.min(entries.size() + 1, 5);
            //            if (extended && pMouseX < maxX && pMouseY > (y + ENTRY_HEIGHT) && pMouseY < maxY) {
            //                setSelected(getEntryAtPosition(pMouseX, pMouseY), true);
            //            }

            if (pMouseX < maxX) {
                extended = !extended;
                scrollOffset = 0;
            }

            playDownSound(Minecraft.getInstance().getSoundManager());

            return true;
        }
        if (!this.isVisible()) {
            return false;
        } else {
            boolean flag = pMouseX >= (double) x && pMouseX < (double) (x + this.width - 17) && pMouseY >= (double) y && pMouseY < (double) (y + this.height);
            if (this.canLoseFocus) {
                this.setFocused(flag);
            }

            if (this.isFocused() && flag && pButton == 0) {
                int i = Mth.floor(pMouseX) - x;
                if (this.bordered) {
                    i -= 4;
                }

                String s = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
                this.moveCursorTo(this.font.plainSubstrByWidth(s, i).length() + this.displayPos);
                return true;
            } else if (visible && active && pMouseX >= x && pMouseX <= x + width && pMouseY >= y && pMouseY <= y + getHeight()) {
                int maxX = x + width - (entries.size() > 4 ? 1 : 0);
                int maxY = y + ENTRY_HEIGHT * Math.min(entries.size() + 1, 5);
                if (extended && pMouseX < maxX && pMouseY > (y + ENTRY_HEIGHT) && pMouseY < maxY) {
                    setSelected(getEntryAtPosition(pMouseX, pMouseY), true);


                    if (pMouseX < maxX) {
                        extended = !extended;
                        scrollOffset = 0;
                    }

                    playDownSound(Minecraft.getInstance().getSoundManager());
                }
                return true;
            }
        }
        //        extended = false;
        //        scrollOffset = 0;

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int x = this.getX();
        int y = this.getY();
        int maxY = y + ENTRY_HEIGHT * Math.min(entriesFiltered.size() + 1, 5);
        if (extended && mouseX >= x && mouseX <= x + width && mouseY > y + ENTRY_HEIGHT && mouseY < maxY) {
            if (delta < 0 && scrollOffset < entriesFiltered.size() - 4) {
                scrollOffset++;
            } else if (delta > 0 && scrollOffset > 0) {
                scrollOffset--;
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean isMouseOver(double pMouseX, double pMouseY) {
        if (!active || !visible) {
            return false;
        }
        int x = this.getX();
        int y = this.getY();
        return pMouseX >= x && pMouseY >= y && pMouseX < (x + width) && pMouseY < (y + getHeight());
    }
    //    public boolean isMouseOver(double pMouseX, double pMouseY) {
    //        return this.visible && pMouseX >= (double)this.x && pMouseX < (double)(this.x + this.width) && pMouseY >= (double)this.y && pMouseY < (double)(this.y + this.height);
    //    }

    @Nullable
    private E getEntryAtPosition(double mouseX, double mouseY) {
        int x = this.getX();
        int y = this.getY();
        if (mouseX < x || mouseX > x + width || mouseY < (y + ENTRY_HEIGHT) || mouseY > (y + (ENTRY_HEIGHT * 5))) {
            return null;
        }

        double posY = mouseY - (y + ENTRY_HEIGHT);
        int idx = (int) (posY / ENTRY_HEIGHT) + scrollOffset;

        return idx < entriesFiltered.size() ? entriesFiltered.get(idx) : null;
    }

    public void addEntry(E entry) {
        entries.add(entry);
    }

    public void removeEntry(E entry) {
        entries.remove(entry);
    }

    public void clearEntries() {
        entries.clear();
    }

    public boolean isExtended() {
        return extended;
    }

    public void setExtended(boolean extended) {
        this.extended = extended;
    }

    public void setSelected(@Nullable E selected, boolean notify) {
        this.selected = selected;
        if (selected != null)
            this.value = selected.message().getString();
        else
            this.value = "";
        if (notify && selectCallback != null && selected != null) {
            selectCallback.accept(selected.object());
        }
    }

    public E getSelected() {
        return selected;
    }

    public Stream<E> stream() {
        return entries.stream();
    }

    public void setResponder(Consumer<String> pResponder) {
        this.responder = pResponder;
    }

    public void setFormatter(BiFunction<String, Integer, FormattedCharSequence> pTextFormatter) {
        this.formatter = pTextFormatter;
    }

    /**
     * Increments the cursor counter
     */
    public void tick() {
        ++this.frame;
    }

    protected MutableComponent createNarrationMessage() {
        Component component = this.getMessage();
        return Component.translatable("gui.narrate.editBox", component, this.value);
    }

    /**
     * Returns the contents of the textbox
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Sets the text of the textbox, and moves the cursor to the end.
     */
    public void setValue(String pText) {
        if (pText.length() > this.maxLength) {
            this.value = pText.substring(0, this.maxLength);
        } else {
            this.value = pText;
        }

        this.moveCursorToEnd();
        this.setHighlightPos(this.cursorPos);
        this.onValueChange(pText);
    }

    /**
     * Returns the text between the cursor and selectionEnd.
     */
    public String getHighlighted() {
        int i = Math.min(this.cursorPos, this.highlightPos);
        int j = Math.max(this.cursorPos, this.highlightPos);
        return this.value.substring(i, j);
    }

    public void setFilter(BiPredicate<String, E> pValidator) {
        this.filter = pValidator;
    }

    /**
     * Adds the given text after the cursor, or replaces the currently selected text if there is a selection.
     */
    public void insertText(String pTextToWrite) {
        int i = Math.min(this.cursorPos, this.highlightPos);
        int j = Math.max(this.cursorPos, this.highlightPos);
        int k = this.maxLength - this.value.length() - (i - j);
        String s = SharedConstants.filterText(pTextToWrite);
        int l = s.length();
        if (k < l) {
            s = s.substring(0, k);
            l = k;
        }

        String s1 = (new StringBuilder(this.value)).replace(i, j, s).toString();
        this.value = s1;
        scrollOffset = 0;
        this.setCursorPosition(i + l);
        this.setHighlightPos(this.cursorPos);
        this.onValueChange(this.value);

    }

    private void onValueChange(String pNewText) {
        if (this.responder != null) {
            this.responder.accept(pNewText);
        }

    }

    private void deleteText(int pCount) {
        if (Screen.hasControlDown()) {
            this.deleteWords(pCount);
        } else {
            this.deleteChars(pCount);
        }

    }

    /**
     * Deletes the given number of words from the current cursor's position, unless there is currently a selection, in
     * which case the selection is deleted instead.
     */
    public void deleteWords(int pNum) {
        if (!this.value.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.insertText("");
            } else {
                this.deleteChars(this.getWordPosition(pNum) - this.cursorPos);
            }
        }
    }

    /**
     * Deletes the given number of characters from the current cursor's position, unless there is currently a selection,
     * in which case the selection is deleted instead.
     */
    public void deleteChars(int pNum) {
        if (!this.value.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.insertText("");
            } else {
                int i = this.getCursorPos(pNum);
                int j = Math.min(i, this.cursorPos);
                int k = Math.max(i, this.cursorPos);
                if (j != k) {
                    String s = (new StringBuilder(this.value)).delete(j, k).toString();
                    this.value = s;
                    this.moveCursorTo(j);
                }
            }
        }
    }

    /**
     * Gets the starting index of the word at the specified number of words away from the cursor position.
     */
    public int getWordPosition(int pNumWords) {
        return this.getWordPosition(pNumWords, this.getCursorPosition());
    }

    /**
     * Gets the starting index of the word at a distance of the specified number of words away from the given position.
     */
    private int getWordPosition(int pN, int pPos) {
        return this.getWordPosition(pN, pPos, true);
    }

    /**
     * Like getNthWordFromPos (which wraps this), but adds option for skipping consecutive spaces
     */
    private int getWordPosition(int pN, int pPos, boolean pSkipWs) {
        int i = pPos;
        boolean flag = pN < 0;
        int j = Math.abs(pN);

        for (int k = 0; k < j; ++k) {
            if (!flag) {
                int l = this.value.length();
                i = this.value.indexOf(32, i);
                if (i == -1) {
                    i = l;
                } else {
                    while (pSkipWs && i < l && this.value.charAt(i) == ' ') {
                        ++i;
                    }
                }
            } else {
                while (pSkipWs && i > 0 && this.value.charAt(i - 1) == ' ') {
                    --i;
                }

                while (i > 0 && this.value.charAt(i - 1) != ' ') {
                    --i;
                }
            }
        }

        return i;
    }

    /**
     * Moves the text cursor by a specified number of characters and clears the selection
     */
    public void moveCursor(int pDelta) {
        this.moveCursorTo(this.getCursorPos(pDelta));
    }

    private int getCursorPos(int pDelta) {
        return Util.offsetByCodepoints(this.value, this.cursorPos, pDelta);
    }

    /**
     * Sets the current position of the cursor.
     */
    public void moveCursorTo(int pPos) {
        this.setCursorPosition(pPos);
        if (!this.shiftPressed) {
            this.setHighlightPos(this.cursorPos);
        }

        this.onValueChange(this.value);
    }

    /**
     * Moves the cursor to the very start of this text box.
     */
    public void moveCursorToStart() {
        this.moveCursorTo(0);
    }

    /**
     * Moves the cursor to the very end of this text box.
     */
    public void moveCursorToEnd() {
        this.moveCursorTo(this.value.length());
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (!this.canConsumeInput()) {
            return false;
        } else {
            this.shiftPressed = Screen.hasShiftDown();
            if (Screen.isSelectAll(pKeyCode)) {
                this.moveCursorToEnd();
                this.setHighlightPos(0);
                return true;
            } else if (Screen.isCopy(pKeyCode)) {
                Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
                return true;
            } else if (Screen.isPaste(pKeyCode)) {
                if (this.isEditable) {
                    this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
                }

                return true;
            } else if (Screen.isCut(pKeyCode)) {
                Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
                if (this.isEditable) {
                    this.insertText("");
                }

                return true;
            } else {
                switch (pKeyCode) {
                    case 259:
                        if (this.isEditable) {
                            this.shiftPressed = false;
                            this.deleteText(-1);
                            this.shiftPressed = Screen.hasShiftDown();
                        }

                        return true;
                    case 260:
                    case 264:
                    case 265:
                    case 266:
                    case 267:
                    default:
                        return false;
                    case 261:
                        if (this.isEditable) {
                            this.shiftPressed = false;
                            this.deleteText(1);
                            this.shiftPressed = Screen.hasShiftDown();
                        }

                        return true;
                    case 262:
                        if (Screen.hasControlDown()) {
                            this.moveCursorTo(this.getWordPosition(1));
                        } else {
                            this.moveCursor(1);
                        }

                        return true;
                    case 263:
                        if (Screen.hasControlDown()) {
                            this.moveCursorTo(this.getWordPosition(-1));
                        } else {
                            this.moveCursor(-1);
                        }

                        return true;
                    case 268:
                        this.moveCursorToStart();
                        return true;
                    case 269:
                        this.moveCursorToEnd();
                        return true;
                }
            }
        }
    }

    public boolean canConsumeInput() {
        return this.isVisible() && this.isFocused() && this.isEditable();
    }

    public boolean charTyped(char pCodePoint, int pModifiers) {
        if (!this.canConsumeInput()) {
            return false;
        } else if (SharedConstants.isAllowedChatCharacter(pCodePoint)) {
            if (this.isEditable) {
                this.insertText(Character.toString(pCodePoint));
            }

            return true;
        } else {
            return false;
        }
    }

    public void renderFilter(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        int x = this.getX();
        int y = this.getY();
        if (this.isVisible()) {
            if (this.isBordered()) {
                int i = this.isFocused() ? -1 : -6250336;
                guiGraphics.fill(x - 1, y - 1, x + this.width + 1, y + this.height + 1, i);
                guiGraphics.fill(x, y, x + this.width, y + this.height, -16777216);
            }

            int i2 = this.isEditable ? this.textColor : this.textColorUneditable;
            int j = this.cursorPos - this.displayPos;
            int k = this.highlightPos - this.displayPos;
            String s = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
            boolean flag = j >= 0 && j <= s.length();
            boolean flag1 = this.isFocused() && this.frame / 6 % 2 == 0 && flag;
            int l = this.bordered ? x + 4 : x;
            int i1 = this.bordered ? y + (this.height - 8) / 2 : y;
            int j1 = l;
            if (k > s.length()) {
                k = s.length();
            }

            if (!s.isEmpty()) {
                String s1 = flag ? s.substring(0, j) : s;
                j1 = guiGraphics.drawString(this.font, this.formatter.apply(s1, this.displayPos), l, i1, i2);
            }

            boolean flag2 = this.cursorPos < this.value.length() || this.value.length() >= this.getMaxLength();
            int k1 = j1;
            if (!flag) {
                k1 = j > 0 ? l + this.width : l;
            } else if (flag2) {
                k1 = j1 - 1;
                --j1;
            }

            if (!s.isEmpty() && flag && j < s.length()) {
                guiGraphics.drawString(this.font, this.formatter.apply(s.substring(j), this.cursorPos), j1, i1, i2);
            }

            if (!flag2 && this.suggestion != null) {
                guiGraphics.drawString(this.font, this.suggestion, (k1 - 1), i1, 0xFF808080);
            }

            if (flag1) {
                if (flag2) {
                    guiGraphics.fill(k1, i1 - 1, k1 + 1, i1 + 1 + 9, 0xFFD0D0D0);
                } else {
                    guiGraphics.drawString(this.font, "_", k1, i1, i2);
                }
            }

            if (k != j) {
                int l1 = l + this.font.width(s.substring(0, k));
                this.renderHighlight(k1, i1 - 1, l1 - 1, i1 + 1 + 9);
            }

        }
    }

    /**
     * Draws the blue selection box.
     */
    private void renderHighlight(int pStartX, int pStartY, int pEndX, int pEndY) {
        if (pStartX < pEndX) {
            int i = pStartX;
            pStartX = pEndX;
            pEndX = i;
        }

        if (pStartY < pEndY) {
            int j = pStartY;
            pStartY = pEndY;
            pEndY = j;
        }

        int x = this.getX();
        int y = this.getY();

        if (pEndX > x + this.width) {
            pEndX = x + this.width;
        }

        if (pStartX > x + this.width) {
            pStartX = x + this.width;
        }

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
        // RenderSystem.disableTexture();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex((double) pStartX, (double) pEndY, 0.0D).endVertex();
        bufferbuilder.vertex((double) pEndX, (double) pEndY, 0.0D).endVertex();
        bufferbuilder.vertex((double) pEndX, (double) pStartY, 0.0D).endVertex();
        bufferbuilder.vertex((double) pStartX, (double) pStartY, 0.0D).endVertex();
        tesselator.end();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableColorLogicOp();
        // RenderSystem.enableTexture();
    }

    /**
     * Returns the maximum number of character that can be contained in this textbox.
     */
    private int getMaxLength() {
        return this.maxLength;
    }

    /**
     * Sets the maximum length for the text in this text box. If the current text is longer than this length, the current
     * text will be trimmed.
     */
    public void setMaxLength(int pLength) {
        this.maxLength = pLength;
        if (this.value.length() > pLength) {
            this.value = this.value.substring(0, pLength);
            this.onValueChange(this.value);
        }

    }

    /**
     * Returns the current position of the cursor.
     */
    public int getCursorPosition() {
        return this.cursorPos;
    }

    public void setCursorPosition(int pPos) {
        this.cursorPos = Mth.clamp(pPos, 0, this.value.length());
    }

    /**
     * Gets whether the background and outline of this text box should be drawn (true if so).
     */
    private boolean isBordered() {
        return this.bordered;
    }

    /**
     * Sets whether the background and outline of this text box should be drawn.
     */
    public void setBordered(boolean pEnableBackgroundDrawing) {
        this.bordered = pEnableBackgroundDrawing;
    }

    /**
     * Sets the color to use when drawing this text box's text. A different color is used if this text box is disabled.
     */
    public void setTextColor(int pColor) {
        this.textColor = pColor;
    }

    /**
     * Sets the color to use for text in this text box when this text box is disabled.
     */
    public void setTextColorUneditable(int pColor) {
        this.textColorUneditable = pColor;
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent event) {
        return this.visible && this.isEditable ? super.nextFocusPath(event) : null;
    }

    public void setFocused(boolean focused) {
        if (this.canLoseFocus || focused) {
            super.setFocused(focused);
            if (focused) {
                this.frame = 0;
            }
        }
    }

    private boolean isEditable() {
        return this.isEditable;
    }

    /**
     * Sets whether this text box is enabled. Disabled text boxes cannot be typed in.
     */
    public void setEditable(boolean pEnabled) {
        this.isEditable = pEnabled;
    }

    /**
     * Returns the width of the textbox depending on if background drawing is enabled.
     */
    public int getInnerWidth() {
        return this.isBordered() ? this.width - 8 : this.width;
    }

    /**
     * Sets the position of the selection anchor (the selection anchor and the cursor position mark the edges of the
     * selection). If the anchor is set beyond the bounds of the current text, it will be put back inside.
     */
    public void setHighlightPos(int pPosition) {
        int i = this.value.length();
        this.highlightPos = Mth.clamp(pPosition, 0, i);
        if (this.font != null) {
            if (this.displayPos > i) {
                this.displayPos = i;
            }

            int j = this.getInnerWidth();
            String s = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), j);
            int k = s.length() + this.displayPos;
            if (this.highlightPos == this.displayPos) {
                this.displayPos -= this.font.plainSubstrByWidth(this.value, j, true).length();
            }

            if (this.highlightPos > k) {
                this.displayPos += this.highlightPos - k;
            } else if (this.highlightPos <= this.displayPos) {
                this.displayPos -= this.displayPos - this.highlightPos;
            }

            this.displayPos = Mth.clamp(this.displayPos, 0, i);
        }

    }

    /**
     * Sets whether this text box loses focus when something other than it is clicked.
     */
    public void setCanLoseFocus(boolean pCanLoseFocus) {
        this.canLoseFocus = pCanLoseFocus;
    }

    /**
     * Returns {@code true} if this textbox is visible.
     */
    public boolean isVisible() {
        return this.visible;
    }

    /**
     * Sets whether this textbox is visible.
     */
    public void setVisible(boolean pIsVisible) {
        this.visible = pIsVisible;
    }

    public void setSuggestion(@Nullable String pSuggestion) {
        this.suggestion = pSuggestion;
    }

    public int getScreenX(int pCharNum) {
        return pCharNum > this.value.length() ? this.getX() : this.getX() + this.font.width(this.value.substring(0, pCharNum));
    }

    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
        pNarrationElementOutput.add(NarratedElementType.TITLE, Component.translatable("narration.edit_box", this.getValue()));
    }

    public boolean testFilter(E object) {
        if (filter == null)
            return true;
        return filter.test(value, object);
    }

    public record SelectionEntry<T>(T object, Component message,
                                    @Nullable ResourceLocation icon) implements GuiEventListener {
        public SelectionEntry(T object, Component message) {
            this(object, message, null);
        }

        public void render(GuiGraphics guiGraphics, @Nullable Component title, int x, int y, int width, boolean hovered, int fgColor, float alpha) {
            if (hovered) {
                guiGraphics.fill(x, y, x + width, y + ENTRY_HEIGHT, 0xFFA0A0A0);
            }

            Font font = Minecraft.getInstance().font;
            int titleWidth = title == null ? 0 : font.width(title);

            FormattedText formattedMessage = font.substrByWidth(this.message, width - titleWidth - 3);
            FormattedText composite = title == null ? FormattedText.composite(formattedMessage) : FormattedText.composite(title, formattedMessage);
            FormattedCharSequence text = Language.getInstance().getVisualOrder(composite);
            if (icon != null) {
                guiGraphics.blit(icon, x + 1, y + 1, 0, 0, ENTRY_HEIGHT-2, ENTRY_HEIGHT-2, ENTRY_HEIGHT-2, ENTRY_HEIGHT-2);
            }
            guiGraphics.drawString(font, text, x + 1 + (icon == null ? 0 : ENTRY_HEIGHT-2), y + (ENTRY_HEIGHT - 8) / 2, fgColor | Mth.ceil(alpha * 255.0F) << 24);
        }

        @Override
        public void setFocused(boolean focused) {
        }

        @Override
        public boolean isFocused() {
            return false;
        }
    }
}
