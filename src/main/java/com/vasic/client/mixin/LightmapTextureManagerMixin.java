package com.vasic.client.mixin;

import com.vasic.client.module.modules.render.Fullbright;
import com.vasic.client.VasicClient;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {

    @Inject(method = "getBrightness", at = @At("HEAD"), cancellable = true)
    private static void onGetBrightness(float value, CallbackInfoReturnable<Float> cir) {
        if (VasicClient.getInstance() != null) {
            var module = VasicClient.getInstance().getModuleManager().getModule("Fullbright");
            if (module != null && module.isEnabled()) {
                cir.setReturnValue(1.0f);
            }
        }
    }
}
