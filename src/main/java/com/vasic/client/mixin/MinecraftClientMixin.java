package com.vasic.client.mixin;

import com.vasic.client.VasicClient;
import com.vasic.client.module.modules.player.FastPlace;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    @Shadow
    private int itemUseCooldown;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (FastPlace.isActive()) {
            itemUseCooldown = 0;
        }
    }

    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    private void onHandleInput(CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        if (client.currentScreen != null) return;

        long window = client.getWindow().getHandle();
        for (int key = GLFW.GLFW_KEY_A; key <= GLFW.GLFW_KEY_Z; key++) {
            if (GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS) {
                // Key detection is handled per-frame; module manager checks bindings
            }
        }
    }
}
