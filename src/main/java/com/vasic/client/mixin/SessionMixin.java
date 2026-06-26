package com.vasic.client.mixin;

import com.vasic.client.VasicClient;
import net.minecraft.client.session.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Session.class)
public class SessionMixin {

    @Inject(method = "getUsername", at = @At("HEAD"), cancellable = true)
    private void onGetUsername(CallbackInfoReturnable<String> cir) {
        String custom = VasicClient.getCustomUsername();
        if (custom != null && !custom.isEmpty()) {
            cir.setReturnValue(custom);
        }
    }
}
