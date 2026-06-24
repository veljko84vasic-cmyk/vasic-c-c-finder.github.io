# Vasic Client — Minecraft 1.21.1 Utility Mod

A Fabric mod for Minecraft 1.21.1 with a ClickGUI, HUD, FPS optimizations, and 12 utility modules.

## How to use

- Press **Right Shift** to open the mod menu
- Click modules to toggle them ON/OFF
- Drag category headers to rearrange panels

## Modules

| Module | Key | What it does |
| --- | --- | --- |
| Fullbright | B | See in the dark |
| NoFog | G | Remove fog |
| Zoom | C | Zoom in like a spyglass |
| Sprint | V | Auto-sprint |
| ToggleSneak | Z | Hold sneak without holding the key |
| NoSlowdown | N | No speed reduction when using items |
| AutoTool | T | Auto-switch to best tool |
| FastPlace | F | Remove block placement delay |
| NoFall | J | Prevent fall damage |
| FPSBoost | P | Kill particles + clouds for more FPS |
| ArmorHUD | H | Show armor durability on screen |
| Timer | U | Session timer |

## HUD

Always shows: FPS counter, XYZ coordinates, facing direction, active module list.

## Building

1. Open in IntelliJ IDEA
2. Import as Gradle project
3. Run `genSources` from the Gradle panel
4. Click Play to run Minecraft Client
5. To build a .jar: run `build` from Gradle panel, find it in `build/libs/`

## Installing the .jar

1. Install Fabric Loader for 1.21.1
2. Drop `vasic-client-1.0.0.jar` into `.minecraft/mods/`
3. Launch Minecraft with the Fabric profile
