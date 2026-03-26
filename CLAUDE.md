# EyeMine Mods

Minecraft accessibility mod for eye-gaze and alternative input control. Developed for SpecialEffect (specialeffect.org.uk).

## Build & Run

Different branches require different Java versions. **Do not change `~/.gradle/gradle.properties` globally** — use the explicit `JAVA_HOME` prefix instead so it works regardless of each developer's machine setup.

| Branch | MC Version | Java | Linux path | Windows path |
|--------|-----------|------|------------|--------------|
| `EyeMine`, `eyemineV2_mc1_16_5` | 1.16.5 | 17 | `/usr/lib/jvm/java-17-openjdk-amd64` | `C:\Program Files\Eclipse Adoptium\jdk-17...` |
| `architectury_1.21` | 1.21.1 | 21 | `/usr/lib/jvm/java-21-openjdk-amd64` | `C:\Program Files\Eclipse Adoptium\jdk-21...` |

```bash
# MC 1.16.5 branches (Java 17)
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew build
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./gradlew runClient

# MC 1.21.1 branch (Java 21)
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew build
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew runClient
```

On Windows use `set JAVA_HOME=C:\path\to\jdk && gradlew build` or set it in the session beforehand.

## Project Structure

Uses **Architectury** for cross-loader support (Forge + Fabric).

```
common/     # Shared code (most development happens here)
fabric/     # Fabric-specific code
forge/      # Forge-specific code
```

- **Minecraft**: 1.16.5
- **Architectury**: 1.30.55
- **Mod version**: 3.1.0

## SubMod System

All features are implemented as SubMods extending `SubMod` base class.
Located in: `common/src/main/java/com/specialeffect/eyemine/submod/`

### SubMod Categories

| Category | Location | Purpose |
|----------|----------|---------|
| building | `/submod/building/` | AutoPillar, CountBlocks, DwellBuild, PickBlock, UseItem |
| mining | `/submod/mining/` | ContinuouslyMine, DwellMine, GatherDrops, MineOne |
| movement | `/submod/movement/` | AutoFly, AutoJump, MoveWithGaze, Sneak, Swim, etc. |
| survival | `/submod/survival/` | **AutoAim** (WIP) |
| misc | `/submod/misc/` | AutoOpenDoors, IronSights, QuickCommands, etc. |
| mouse | `/submod/mouse/` | MouseHandlerMod, InputSource |

### Creating a SubMod

1. Extend `SubMod` class
2. Override `onInitializeClient()` to register keybindings and events
3. Add keybindings to `Keybindings.keybindings` list
4. Register with `EyeMineClient.setupSubMod()` in `instantiateSubMods()`

Example pattern (see `AutoAim.java`):
```java
public class MySubMod extends SubMod {
    public static KeyMapping myKeyBinding;

    public void onInitializeClient() {
        Keybindings.keybindings.add(myKeyBinding = new KeyMapping(...));
        ClientRawInputEvent.KEY_PRESSED.register(this::onKeyInput);
        ClientTickEvent.CLIENT_PRE.register(this::onClientTick);
    }
}
```

## Current Work: AutoAim

Located at: `common/src/main/java/com/specialeffect/eyemine/submod/survival/AutoAim.java`

**Status**: Work in progress (basic implementation complete)

**Current functionality**:
- Keybind: `O` key to trigger
- Finds nearest LivingEntity within 10 blocks
- Rotates player to face target on each tick
- Detects when player is attacked and finds attacker

**Key methods**:
- `findClosestEntity()` - uses `TargetingConditions` to find nearest mob
- `onClientTick()` - calculates yaw and calls `player.turn()`
- `onLivingAttack()` - event handler for when player takes damage

## Publishing to CurseForge

### Version bump process

Version is set in **one place only**: `mod_version` in `gradle.properties`.
Both `fabric.mod.json` and `neoforge.mods.toml` are populated automatically at build time.

**To release:**
1. Update `mod_version` in `gradle.properties`
   - Stable: `6.1.0`
   - Alpha (experimental/WIP): `6.1.0-alpha.1`
   - Beta: `6.1.0-beta.1`
2. Add a changelog entry to `changes.md`
3. Commit, tag, and push:
   ```bash
   git tag v6.1.0-alpha.1
   git push origin v6.1.0-alpha.1
   ```

GitHub Actions automatically builds and uploads both Fabric + NeoForge JARs to CurseForge.
Alpha/beta versions are hidden from the default download button on the mod page.

### Required secret
`CURSEFORGE_TOKEN` must be set in GitHub → Settings → Secrets and variables → Actions.
Get the key from: https://www.curseforge.com/account/api-keys

## Branches

- `EyeMine` - most recent development branch (current)
- `becky` - same as EyeMine minus javadoc cleanup
- `eyemineV2_mc1_14_4` - official release branch (1.14.4)
- `eyemineV2_mc1_16_5` - 1.16.5 port
- `architectury_1.18.2` - 1.18.2 experiments
