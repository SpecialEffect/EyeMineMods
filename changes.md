# EyeMine Changelog

## Version 6.1.0-alpha.1

### New Features

#### AutoAim (Experimental)

- **AutoAim submod**: Automatically rotates the camera to face the nearest mob or attacker. Toggle with the `O` key.
- **Turn speed adjustment**: Increase/decrease aim speed with `=` and `-` keys at runtime.
- **Attacker tracking**: When hit, AutoAim attempts to identify and target the source of damage using multiple heuristics (projectile traceback, approaching threats, nearest hostile).

---


## Version 6.0.2

### Bug Fixes

#### Gaze-Based Walking Refinements

- **Walking now fully stops when gaze is at the on-screen keyboard or outside the window**: In 6.0.1, walking was reduced to 15% speed in these cases. After further testing, full stop is the correct behavior — the original "don't stop completely" feedback from Kirsty was about in-game camera pitch (looking up/down hills), not the on-screen keyboard area.

- **Extreme pitch angles no longer fully stop walking**: Looking straight up or down (beyond 80°) now slows movement to 5% instead of stopping entirely. This preserves the ability to navigate hills and look around without losing all momentum.

#### Dwell Action Improvements

- **Dwell actions now suppressed when gaze is at keyboard area or outside window**: Prevents unintended dwell-triggered interactions (e.g., block placement, item use) while the user is interacting with the on-screen keyboard.

---

## Version 6.0.1

### Bug Fixes

#### Gaze Detection Improvements

- **Fixed auto-walking not stopping when looking at keyboard area**: The GLFW cursor reset events at position (0,0) were incorrectly clearing the gaze-below-threshold state. The fix now properly detects and ignores these cursor reset events to preserve the gaze state.

- **Added outside-window gaze detection**: Walking now also pauses/slows when gaze is detected outside the game window, matching the original intended behavior.

#### Walking Behavior Changes

- **Walking now slows down instead of stopping completely when looking at keyboard**: Per feedback from Kirsty (original developer), stopping completely wasn't intuitive when looking up/down hills. Walking now reduces to 15% speed when gaze is at the keyboard area or outside the window.

#### Mining and Attacking Fixes

- **Toggle mining now pauses when looking at keyboard**: Per EyeGazeGirl's feedback ("toggle mining meant to stop too"), continuous mining now pauses when gaze is below the hotbar or outside the window.

- **Toggle attacking now pauses when looking at keyboard**: Continuous attacking now pauses when gaze is at the keyboard area or outside the window, consistent with mining behavior.

#### Keybinding Persistence Fix

- **Fixed keybindings resetting on every game launch**: The AutoJump module was calling `options.save()` and `options.load()` during startup config sync, which could interfere with Minecraft's keybinding loading process. The fix now only persists settings when the user manually toggles them, not during startup.
