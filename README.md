# Halo

A PaperMC plugin that overrides beacon effect ranges with configurable per-tier radii and a runtime scalar multiplier.

## Requirements

- **Paper** 26.2+ (Minecraft 26.2)
- **Java** 25+ (server) / 26+ (build)

## Usage

1. Drop `halo-1.0.0.jar` into your server's `plugins/` folder
2. Restart or `/reload`
3. Edit `plugins/Halo/config.yml`:
   ```yaml
   beacon-radius:
     tier-1: 10
     tier-2: 20
     tier-3: 30
     tier-4: 40
   ```

### Commands

| Command | Permission | Description |
|---------|-----------|-------------|
| `/beaconreload` | `halo.beaconreload` (default: op) | Re-reads `config.yml` and re-applies radii to all loaded beacons |
| `/beaconreload <scalar>` | `halo.beaconreload` | Multiplies all config radii by the given factor |

**Examples:**
- `/beaconreload` — loads radii from config
- `/beaconreload 5` — multiplies all tier radii by 5
- `/beaconreload 0.5` — halves all tier radii

## Build

```bash
./gradlew build
```

Output: `build/libs/halo-1.0.0.jar`

## License

MIT
