# Vasic CC — Competitive Roblox FPS

A scaffolded competitive FPS for Roblox inspired by Rivals, with Source-engine-style
movement (bhop, air strafe, slide), a cosmetic skin system, ranked matchmaking with
SBMM, leaderboards, and progression. Built to run on PC and mobile.

## Project layout

This repo uses [Rojo](https://rojo.space) to sync Luau source into Roblox Studio.

```
default.project.json        Rojo project
src/
  ReplicatedStorage/
    Modules/                Shared game logic (movement math, weapon configs,
                            rank tiers, skin registry, etc.)
    Remotes/                RemoteEvent / RemoteFunction definitions
  ServerScriptService/
    Services/               Authoritative services: matchmaking, rank, data,
                            leaderboard, anti-cheat
    Main.server.lua         Boot entry point
  StarterPlayer/
    StarterPlayerScripts/   Client controllers (movement, weapons, input, HUD)
  StarterGui/               UI scaffolding
```

## What is implemented

| System | State |
| --- | --- |
| Source-style movement (accel, friction, air strafe, bhop, slide, crouch) | Working |
| Weapon framework (recoil pattern, spread, fire modes, reload, hit reg) | Working |
| Knife framework (Karambit / Butterfly / Bayonet config + inspect hooks) | Working |
| Cosmetic-only skin registry + inventory | Working |
| Rank tiers, MMR (Glicko-2 lite), SBMM queue, season reset | Working |
| Leaderboards (global + seasonal) via OrderedDataStore | Working |
| Progression (XP curve, achievements, unlocks) | Working |
| PC + mobile input adapter (touch buttons + look stick) | Working |
| Anti-cheat hooks (speed / fire-rate / teleport sanity) | Working |
| Maps, art assets, audio, finished UI polish | **Not included** — author in Studio |

Gameplay logic is authoritative on the server. Client prediction for movement
keeps input feeling responsive; the server validates kinematics each tick.

## Getting started

1. Install Rojo (`aftman add rojo-rbx/rojo` or `cargo install rojo`).
2. `rojo serve` from the repo root.
3. Open a fresh Baseplate in Roblox Studio and connect the Rojo plugin.
4. Place map geometry under `Workspace/Maps/<MapName>` with a folder of
   `SpawnPoint` parts per team and a `BombSites` folder for objective modes.
5. Configure DataStore access in the game's place settings before publishing.

## Cosmetic-only guarantee

Skins live in `ReplicatedStorage/Modules/SkinRegistry.lua` and only carry
visual fields (`viewmodel`, `worldmodel`, `tracer`, `muzzle`, `inspectAnim`).
Weapon stats come exclusively from `WeaponConfigs.lua` keyed by weapon id,
never by skin id — there is no place in the code path where a skin can
modify damage, recoil, or movement.

## Mobile support

`InputAdapter` exposes a unified action set. On `UserInputService.TouchEnabled`
devices it spawns a virtual left stick, right look stick, jump/crouch/slide
buttons, fire / aim / reload / swap, and an auto-fire toggle. Sensitivity and
dead-zones are persisted in player data.
