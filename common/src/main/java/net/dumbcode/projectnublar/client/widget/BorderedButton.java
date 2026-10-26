package net.dumbcode.projectnublar.client.widget;

import net.dumbcode.projectnublar.client.screen.SequencerScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


public class BorderedButton extends AbstractButton {
    public static final int SMALL_WIDTH = 120;
    public static final int DEFAULT_WIDTH = 150;
    public static final int DEFAULT_HEIGHT = 20;
    protected static final BorderedButton.CreateNarration DEFAULT_NARRATION = (component) -> {
        return component.get();
    };
    protected final BorderedButton.OnPress onPress;
    protected final BorderedButton.CreateNarration createNarration;
    private final Component message1;
    private final Component message2;
    private Function<BorderedButton,Boolean> messageConsumer = (p_169084_) -> true;

    public static BorderedButton.Builder builder(Component pMessage, Component message2, BorderedButton.OnPress pOnPress) {
        return new BorderedButton.Builder(pMessage, message2, pOnPress);
    }

    protected BorderedButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, Component pMessage2, BorderedButton.OnPress pOnPress, BorderedButton.CreateNarration pCreateNarration) {
        super(pX, pY, pWidth, pHeight, pMessage);
        this.onPress = pOnPress;
        this.createNarration = pCreateNarration;
        this.message1 = pMessage;
        this.message2 = pMessage2;
    }

    @Override
    public Component getMessage() {
        return !messageConsumer.apply(this) ? message1 : message2;
    }

    protected BorderedButton(Builder builder) {
        this(builder.x, builder.y, builder.width, builder.height, builder.message, builder.message2, builder.onPress, builder.createNarration);
        setTooltip(builder.tooltip); // Forge: Make use of the Builder tooltip
    }

    @Override
    public void onPress(InputWithModifiers input) {
        this.onPress.onPress(this);
    }

    protected MutableComponent createNarrationMessage() {
        return this.createNarration.createNarrationMessage(() -> {
            return super.createNarrationMessage();
        });
    }

    public void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
        this.defaultButtonNarrationText(pNarrationElementOutput);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        SequencerScreen.drawBorder(pGuiGraphics, this.getX(), this.getY(), this.width, this.height, 1, 1);
        pGuiGraphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1, 0xFF000000);
        // White scrolling label with a 2 px margin; getMessage() above bypasses the grey inactive style.
        this.extractDefaultLabel(pGuiGraphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
    }

    /** Keeps the arrow cursor on hover, as on 1.20.1; 26.2's AbstractButton would show a hand (P38). */
    @Override
    protected void handleCursor(GuiGraphicsExtractor graphics) {
    }
    public void setMessageConsumer(Function<BorderedButton,Boolean> messageConsumer) {
        this.messageConsumer = messageConsumer;
    }

    public static class Builder {
        private final Component message;
        private final Component message2;
        private final BorderedButton.OnPress onPress;
        @Nullable
        private Tooltip tooltip;
        private int x;
        private int y;
        private int width = 150;
        private int height = 20;
        private BorderedButton.CreateNarration createNarration = BorderedButton.DEFAULT_NARRATION;

        public Builder(Component pMessage, Component message2, BorderedButton.OnPress pOnPress) {
            this.message = pMessage;
            this.message2 = message2;
            this.onPress = pOnPress;
        }

        public BorderedButton.Builder pos(int pX, int pY) {
            this.x = pX;
            this.y = pY;
            return this;
        }

        public BorderedButton.Builder width(int pWidth) {
            this.width = pWidth;
            return this;
        }

        public BorderedButton.Builder size(int pWidth, int pHeight) {
            this.width = pWidth;
            this.height = pHeight;
            return this;
        }

        public BorderedButton.Builder bounds(int pX, int pY, int pWidth, int pHeight) {
            return this.pos(pX, pY).size(pWidth, pHeight);
        }

        public BorderedButton.Builder tooltip(@Nullable Tooltip pTooltip) {
            this.tooltip = pTooltip;
            return this;
        }

        public BorderedButton.Builder createNarration(BorderedButton.CreateNarration pCreateNarration) {
            this.createNarration = pCreateNarration;
            return this;
        }



        public BorderedButton build() {
            return build(BorderedButton::new);
        }

        public BorderedButton build(java.util.function.Function<Builder, BorderedButton> builder) {
            return builder.apply(this);
        }
    }

    public interface CreateNarration {
        MutableComponent createNarrationMessage(Supplier<MutableComponent> pMessageSupplier);
    }

    public interface OnPress {
        void onPress(BorderedButton pBorderedButton);
    }
}
