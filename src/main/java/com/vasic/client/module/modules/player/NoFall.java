package com.vasic.client.module.modules.player;

import com.vasic.client.module.Category;
import com.vasic.client.module.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.lwjgl.glfw.GLFW;

public class NoFall extends Module {

    public NoFall() {
        super("NoFall", "Prevent fall damage", Category.PLAYER, GLFW.GLFW_KEY_J);
    }

    @Override
    public void onTick() {
        if (mc.player != null && mc.player.fallDistance > 2.5f) {
            mc.player.networkHandler.sendPacket(
                    new PlayerMoveC2SPacket.OnGroundOnly(true)
            );
        }
    }
}
