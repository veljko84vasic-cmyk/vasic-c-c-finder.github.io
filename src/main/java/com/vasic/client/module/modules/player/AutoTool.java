package com.vasic.client.module.modules.player;

import com.vasic.client.module.Category;
import com.vasic.client.module.Module;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

public class AutoTool extends Module {

    public AutoTool() {
        super("AutoTool", "Auto-switch to the best tool for the block", Category.PLAYER, GLFW.GLFW_KEY_T);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!mc.options.attackKey.isPressed()) return;

        if (mc.crosshairTarget instanceof BlockHitResult blockHit && blockHit.getType() == HitResult.Type.BLOCK) {
            BlockState state = mc.world.getBlockState(blockHit.getBlockPos());
            int bestSlot = -1;
            float bestSpeed = 1.0f;

            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.player.getInventory().getStack(i);
                float speed = stack.getMiningSpeedMultiplier(state);
                if (speed > bestSpeed) {
                    bestSpeed = speed;
                    bestSlot = i;
                }
            }

            if (bestSlot != -1) {
                mc.player.getInventory().selectedSlot = bestSlot;
            }
        }
    }
}
