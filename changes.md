# EyeMine Changelog

## Version 7.0.0 - MC 26.1.1 (NeoForge + Fabric)

### Major Changes

- **Migrated from Architectury to MultiLoader Template** - the mod now uses Jared's MultiLoader Template with Java ServiceLoader for platform abstraction
- **Upgraded to MC 26.1.1** from MC 1.21.1
- **Upgraded to Java 25** from Java 21
- **Dropped Forge support** - Only NeoForge and Fabric are supported

### Build System

- Replaced Architectury Loom with buildSrc convention plugins (multiloader-common, multiloader-loader)
- NeoForge uses ModDevGradle, Fabric uses Fabric Loom 1.16.1
- Gradle upgraded to 9.4.1

### Platform Abstraction

- Replaced `@ExpectPlatform` with Java ServiceLoader pattern
- Created service interfaces: `IEyeMineConfigService`, `IInventoryConfigService`, `IPlatformHelper`, `NetworkService`
- Static facade classes delegate to services transparently - no changes needed in calling code

### Event System

- Created custom common event bus (`EyeMineEvents`) replacing Architectury events
- Platform entry points forward native events into the common bus
- Fabric: Custom mixins for key input (`FabricKeyboardHandlerMixin`) and screen events (`FabricSetScreenMixin`)

### Rendering

- Overlay rendering uses `RenderPipelines.GUI_TEXTURED` with ARGB color int for alpha support
- In-world dwell visualization now uses `RenderTypes.debugQuads()`
- NeoForge block outline is now accomplished via `ExtractBlockOutlineRenderStateEvent` + `CustomBlockOutlineRenderer`
- Fabric block outline is handled via `LevelRenderEvents.BEFORE_BLOCK_OUTLINE`
- Fabric HUD rendering done with `HudElementRegistry`

### Mixin Updates

- `LivingEntityMixin` rewritten to use `AttributeModifier` on `STEP_HEIGHT`
- `KeyboardInputMixin` rewritten for immutable `Input` record system; fractional walk speed preserved via `moveVector`
  - this was an internal change from mojang that just didnt work well for what we needed
- `TitleScreenMixin` updated for `extractRenderState()` and `Matrix3x2fStack`
- Added Fabric-specific mixins for key and screen event forwarding

### Removed

- Forge subproject
- ModMenu integration (commented out pending 26.1.1-compatible release)
