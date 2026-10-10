# Pinyon Shift on Android

**Status: alpha, for developers.** The game builds for Android, installs and
plays through the Vulkan renderer. On the reference tablet (nubia NP05J,
Snapdragon 8 Elite) the race runs at 60 fps at 1x until the device heats up,
and settles near 30 once its skin passes about 44 C
([60 fps backlog](ANDROID_60FPS_BACKLOG.md)). The target is a high-end Android handheld or phone with
a Snapdragon 8 Gen 2 or newer (Adreno 740+), 12 GB of memory and Android 13
or later. The plan and its progress are in the
[Android port backlog](ANDROID_PORT_BACKLOG.md).

## What stays private

The package holds the game translated from your own disc, exactly as
`pinyon_shift.exe` does on Windows, so it is built on your PC and installed
on your own device, and it is never published or shared. The game files go
from your PC to your device over USB. Nothing is uploaded. The repository
refuses Android packages, libraries and signing keys (see
[legal](LEGAL.md)).

## Requirements and compatibility

- A Windows PC where the game is already built (the launcher, or
  `tools/build-preview.ps1`): the Android build reuses the code translated
  there.
- The Android SDK command-line tools (Android Studio installs them) and JDK
  17. `pinyon.py android doctor --install` adds the pinned NDK, build tools
  and platform from `config/android-toolchain.json`, and shows the Android
  SDK license for you to accept (`--accept-licenses` answers yes).
- The declared install floor is now **Android 10 (API 29), arm64 and Vulkan
  1.1**, rather than refusing everything below Android 13/Vulkan 1.3. This is
  a broader *install floor*, not a guarantee that every Vulkan 1.1 phone can
  render FH1: the native executor still needs dynamic rendering, supplied by
  Vulkan 1.3 core or `VK_KHR_dynamic_rendering`. On Vulkan 1.1, that extension
  also has dependencies such as `VK_KHR_depth_stencil_resolve`. The current
  native executor rejects devices without dynamic rendering; it has no
  classic-render-pass fallback yet.
- **Mali G90T is not verified yet.** MediaTek identifies the G90-series GPU as
  Mali-G76 MC4, but the exact phone driver must still expose the renderer's
  required extensions, formats and features. In addition, the Android backlog
  still has BC texture decoding/transcoding for Mali/PowerVR open; an
  uncompressed fallback can substantially increase texture memory and churn.
  G90T must pass the startup capability report plus title-screen, free-roam
  and race tests on actual hardware before being called supported. Changing
  the manifest alone cannot supply missing renderer functionality.
- Start testing with **6 GB RAM or more** and enough free storage for the game
  data. A 4 GB phone may install but is not a supported performance target
  and may run into shared-memory pressure; no RAM amount guarantees
  compatibility or stable FPS.
- USB debugging turned on for installation/testing, and around 8 GB free
  storage for the Android package, game files, shader caches and saves.

## From the PC to the device

In the launcher, once the game is built, **Build Android APK** does the
first two steps below. It asks you to accept the Android SDK license, and on
a PC with no Android SDK or JDK it fetches the pinned command-line tools and
Eclipse Temurin JDK 17 into the install folder (`.local/toolchain`), checked
against their SHA-256. A failure is described in
`.local/logs/android-error.json`. Then install the package and copy the
game with the last two commands.

From a terminal:

```bash
python tools/pinyon.py android doctor --install
```

```bash
python tools/pinyon.py android build
```

```bash
python tools/pinyon.py android install
```

```bash
python tools/pinyon.py android push-data
```

`build` cross-compiles the game (20 to 60 minutes the first time) and
packages `.local/android/pinyon-shift.apk`, signed with a key made on your
PC. `push-data` copies the extracted game (7.2 GB, 2,400 files) into the
app's folder on the device, `Android/data/com.pinyonshift.fh1/files/game/base`;
it resumes where it stopped if interrupted. Saves, settings, logs and mods
live beside it in `files/state`, with the same layout as on the PC, so a save
copies between the two as a folder.

Start the game from the launcher icon, or:

```bash
python tools/pinyon.py android run
```

`run --null-gpu` starts it without a renderer, and `run --route FILE` runs a
render-test route (`config/render-tests/`). `pull-logs` copies the logs,
crash reports and route output to `.local/android/device-logs/`.

## Launcher settings

The Android launcher has an output-surface resolution selector for 800×600,
720×480, 1280×720 and 1280×1024. The selected fixed-size surface is scaled to
the fullscreen view by Android; this changes the presentation buffer size,
not FH1's internal render scale. That resolution applies when the game starts.

The internal render scale intentionally remains at the engine's supported
native 1× default for now. Fractional 0.25×–0.75× and 1.25×–1.75× scales are
not implemented by the current texture cache/native executor, which uses
integer scale factors. Exposing them as selectable settings would pretend a
feature exists when it does not. The optional 2× path can allocate a dedicated
resolve buffer of about 2 GB; it is not enabled by this launcher for memory
safety.

## Controls

A controller (built in, Bluetooth or USB) works as on the PC. On a touch
screen, on-screen controls appear at the first touch: a steering stick
wherever the left thumb lands, throttle (RT) and brake (LT) under the right
thumb, A, B, X, Y, the bumpers, Back and Start. They hide 20 seconds after the
last touch, so a controller player never sees them
(`pinyon_shift_touch_controls` turns them off).

Android's Back button or gesture opens SETTINGS, and inside the menus it steps
back; tap a row to open or change it. SETTINGS also holds the trainer (with
cheats on), SAVE PHOTO and the achievements, which have keyboard keys on the PC.

## Troubleshooting

| Symptom | Cause |
| --- | --- |
| The app closes at once | The game files are not on the device: run `push-data`. If logcat says `Cannot create the state folder`, files were copied into the app's folder by hand: run `push-data` again, which lets the app use them. |
| `VULKAN_CAPABILITY_REPORT` in the log | The device's features, formats and memory, logged at every start; attach it to reports. |
| RESOLUTION SCALE offers only 1X | Higher scales need resolve buffers larger than a phone's shared memory holds; `android_allow_resolution_scale` lifts the limit for testing. |
| `skipped a resolve` in the log | A guest copy the renderer cannot pack yet (one is known, in the title screen's attract sequence); the frame continues without it. |
| No sound | No output device could be opened; the game runs silently instead of stopping. |
