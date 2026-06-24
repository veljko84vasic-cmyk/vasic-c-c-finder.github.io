package com.vasic.client.module;

import com.vasic.client.module.modules.misc.ArmorHud;
import com.vasic.client.module.modules.misc.FPSBoost;
import com.vasic.client.module.modules.misc.Timer;
import com.vasic.client.module.modules.movement.NoSlowdown;
import com.vasic.client.module.modules.movement.Sprint;
import com.vasic.client.module.modules.movement.ToggleSneak;
import com.vasic.client.module.modules.player.AutoTool;
import com.vasic.client.module.modules.player.FastPlace;
import com.vasic.client.module.modules.player.NoFall;
import com.vasic.client.module.modules.render.Fullbright;
import com.vasic.client.module.modules.render.NoFog;
import com.vasic.client.module.modules.render.Zoom;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    public void init() {
        // Render
        modules.add(new Fullbright());
        modules.add(new NoFog());
        modules.add(new Zoom());

        // Movement
        modules.add(new Sprint());
        modules.add(new ToggleSneak());
        modules.add(new NoSlowdown());

        // Player
        modules.add(new AutoTool());
        modules.add(new FastPlace());
        modules.add(new NoFall());

        // Misc
        modules.add(new FPSBoost());
        modules.add(new ArmorHud());
        modules.add(new Timer());
    }

    public void onTick() {
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.onTick();
            }
        }
    }

    public void onKey(int key) {
        if (key == GLFW.GLFW_KEY_UNKNOWN) return;
        for (Module module : modules) {
            if (module.getKeyBind() == key) {
                module.toggle();
            }
        }
    }

    public List<Module> getModules() {
        return modules;
    }

    public List<Module> getModulesByCategory(Category category) {
        List<Module> result = new ArrayList<>();
        for (Module module : modules) {
            if (module.getCategory() == category) {
                result.add(module);
            }
        }
        return result;
    }

    public Module getModule(String name) {
        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    public List<Module> getEnabledModules() {
        List<Module> result = new ArrayList<>();
        for (Module module : modules) {
            if (module.isEnabled()) {
                result.add(module);
            }
        }
        return result;
    }
}
