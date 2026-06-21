# Architecture

## Tick & authority

- The server is authoritative for damage, ammo, position validation, MMR,
  and inventory. Clients send intents (`FireShot`, `MeleeSwing`, etc.) and
  receive results (`HitConfirm`, `DamageTaken`, `RankUpdate`).
- The client runs the same `Movement` module the server validates against,
  using `RunService.RenderStepped` for input-to-velocity responsiveness.
  Roblox owns the physics body — we feed it velocity each tick.
- Movement snapshots flow client→server at 10 Hz so `AntiCheat` can sanity
  check positions without burdening the network like a full input replay.

## Data flow

```
PlayerAdded ─► DataService.load ─► profile in memory
                                   ├─► CombatService awards damage / kills
                                   ├─► MatchService settles rounds + matches
                                   │      └─► RankSystem.update  ─► LeaderboardService.recordMMR
                                   └─► InventoryService applies EquipSkin
PlayerRemoving / BindToClose ─► DataService.saveNow
```

## Cosmetic-only invariant

The only stat-affecting tables are `WeaponConfigs` and `KnifeConfigs`. They are
keyed by weapon/knife id. `SkinRegistry` is keyed by skin id and contains no
combat fields. Damage paths in `CombatService` read `WeaponConfigs.get(id)` —
they never read from a skin. Adding a stat field to a skin would require
modifying `CombatService`, which is the gate where cosmetic-only is enforced.

## SBMM

`Matchmaking.tryForm` runs every 2 seconds (`QueueService`). It picks the
oldest ticket as the anchor and gathers nearest-MMR players within an
expanding window (`80 + waitSeconds * 25` MMR). It snake-drafts into two
teams and rejects forms whose team-average skew exceeds
`80 + waitSeconds * 10`. This makes early matches tight and falls back to
loose matches if the queue is starved.

## Season rollover

`SeasonService.currentId` derives the season number from `os.time()` and a
fixed `EPOCH`. When a player loads in, `DataService.load` compares the season
on their stored profile to the current one — if it differs, it calls
`RankSystem.applySeasonReset`, which soft-resets MMR (compress toward 1000
by 30%, refresh RD, halve placements). No coordinator required; every server
makes the same decision.

## Anti-cheat

Lightweight per-tick checks in `AntiCheat`:
- Movement: speed between snapshots can't exceed `SPEED_CAP_HARD * 1.5`.
- Fire rate: `FireShot` interval per weapon must respect `60/RPM * 0.9`.
- Hit line: shot range can't exceed `MAX_SHOT_DISTANCE`.

Violations accumulate per user; a future enhancement would have MatchService
kick at a threshold.
