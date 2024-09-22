package net.dumbcode.projectnublar.mixin;

import net.dumbcode.projectnublar.Constants;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyboardHandler.class)
public abstract class DinoDebugRendererMixin {
    @Shadow protected abstract void debugFeedbackTranslated(String pMessage, Object... pArgs);

    @Unique
    private static final int L = 74;

    @Inject(method = "handleDebugKeys", at = @At("HEAD"), cancellable = true)
    private void onHandleDebugKeys(int keyCode, CallbackInfoReturnable<Boolean> cir) {
        if (keyCode == L) {
            Constants.shouldRenderDebugLegs = !Constants.shouldRenderDebugLegs;
            this.debugFeedbackTranslated("debug.toggled_joint_debug.message");
            cir.setReturnValue(true);
        }
    }
}