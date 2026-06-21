# Map authoring guide

Maps live under `Workspace/Maps/<MapName>` in Studio. The match service expects
this shape per map:

```
Workspace/
  Maps/
    Dust/                  Folder
      Geometry/            Folder (BasePart children — meshes, terrain)
      Spawns/
        TeamA/             Folder of SpawnLocation parts
        TeamB/             Folder of SpawnLocation parts
      BombSites/
        A/                 Model with at least one Part named "Plant"
        B/
      Lighting/            Folder of Light instances (PointLight, SpotLight)
      Sounds/              Folder of Sound objects (ambient bed, footsteps)
```

The first map listed alphabetically is the default. The match service rotates
maps round-robin per match.

## Layout checklist for competitive balance

- Two main paths from each team's spawn to each site (one "fast", one "safe").
- One mid connector with mutual sightline that rewards AWP play.
- No 1-way sightlines past 80 studs (mobile aim accuracy drops sharply).
- Every angle held by a defender should have at least one counter-angle a
  pusher can hold from cover.
- Cover spacing of ~12-20 studs along corridors gives bhop strafing room.

## Suggested starter maps

| Name | Style | Notes |
| --- | --- | --- |
| Dust   | Three-lane, mid-distance | Easiest to learn, AWP-friendly mid |
| Iceworks | Vertical, two-site | Slide-jump shortcuts reward movement |
| Subway | Tight CQB, two-site | Knife / SMG friendly |
| Skybase | Open arena | Rotation testing, casual / FFA |

Build geometry with Roblox parts or import meshes — `default.project.json`
already sets Future lighting and shadows. Keep collision parts simple
(`Block` mesh, `Plastic` material) and use cosmetic-only meshes as children
to keep server collision queries cheap.
