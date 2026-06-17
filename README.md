# Tractor Field Trip

Top-down 2D maze game. Drive a tractor through a construction-site grid, collect every crate / sack / brick, dodge cranes / pits / barrels, refuel before the tank runs dry. Vertical orientation only.

## How to build

1. Open Android Studio.
2. File → Open → pick this folder (`~/Desktop/Projects/TractorFieldTrip`).
3. Wait for Gradle sync. Android Studio will download the wrapper jar, plugins, and AndroidX dependencies the first time.
4. Hit **Run** on an emulator or device (API 26+).

You should see the gameplay HUD: "Level 3" title top-left, three hearts and a pause button top-right, the red→yellow→green fuel bar with the gas-can marker, an empty 1:1 area in the middle (where the maze board will live), and three counter markers at the bottom.

If sync fails on JDK: Android Studio → Settings → Build, Execution, Deployment → Build Tools → Gradle → set Gradle JDK to **JDK 17**.

## Project layout

```
TractorFieldTrip/
├── settings.gradle.kts
├── build.gradle.kts                          (root — plugin versions)
├── gradle.properties
├── .gitignore
└── app/
    ├── build.gradle.kts                      (app module — deps, ViewBinding on)
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/farmyard/tractortrip/lab/
        │   ├── MainActivity.kt               (single-activity host)
        │   └── GameFragment.kt               (ViewBinding, renders HUD)
        └── res/
            ├── layout/
            │   ├── activity_main.xml         (FragmentContainerView)
            │   └── fragment_game.xml         (gameplay screen — Guidelines + ConstraintLayout)
            ├── drawable/
            │   ├── bg_sand.xml               (radial sand gradient)
            │   ├── bg_caution_panel.xml      (yellow/black framed plate, hearts/pause)
            │   ├── bg_counter_marker.xml       (yellow/black framed plate, bottom counters)
            │   ├── bg_fuel_bar.xml           (red→yellow→green gradient)
            │   ├── ic_heart.xml, ic_pause.xml, ic_fuel_marker.xml
            │   ├── ic_counter_crate.xml, ic_counter_sack.xml, ic_counter_brick.xml
            │   └── ic_launcher_background.xml, ic_launcher_foreground.xml
            ├── mipmap-anydpi-v26/
            │   ├── ic_launcher.xml           (adaptive icon)
            │   └── ic_launcher_round.xml
            ├── values/
            │   ├── colors.xml, strings.xml, dimens.xml, themes.xml
            └── xml/
                └── data_extraction_rules.xml
```

## Stack

- Kotlin, single Activity + Fragment
- AndroidX, ConstraintLayout, Material Components
- ViewBinding (no DataBinding, no Compose, no DI framework)
- minSdk 26, targetSdk 34, AGP 8.2.2, Kotlin 1.9.22

## What's NOT in here yet

- **The maze board itself** — `maze_container` in `fragment_game.xml` is a 1:1 placeholder. The grid (stone tiles + tractor + collectibles + obstacles) belongs in a custom `View` drawing to `Canvas`, not in XML. That comes next.
- **Game logic** — input handling (swipe-at-junction), tractor movement, collision, fuel countdown, lives, level progression, two-tractor switching. All Kotlin, all to be added.
- **Other screens** — splash, main menu, level select, pause overlay, success/fail popups, settings, onboarding.
- **Real art** — game pieces are still vector placeholders; PNG exports from the Figma should go in `drawable-xxhdpi/` and replace the `src=` references in `fragment_game.xml`.

## Asset checklist (export from Figma)

| Element | Filename | Used as |
|---|---|---|
| Stone tile (maze wall) | `tile_stone.png` | Wall cells in maze board |
| Blue tractor (4 rotations) | `tractor_blue_up.png`, `..._down.png`, `..._left.png`, `..._right.png` | Player tractor |
| Yellow tractor (4 rotations) | `tractor_yellow_*.png` | 2nd tractor on multi-tractor levels |
| Wooden crate | `item_crate.png` | Pickup + counter icon |
| Sack | `item_sack.png` | Pickup + counter icon |
| Brick stack | `item_brick.png` | Pickup + counter icon |
| Red gas can | `item_fuel.png` | Pickup + fuel-bar marker |
| Crane | `obstacle_crane.png` | Stop obstacle |
| Red barrel | `obstacle_barrel.png` | -1 life obstacle |
| Pit | `obstacle_pit.png` | -1 life obstacle |
| Grass tuft (corner decor) | `decor_grass_*.png` | Background ornament |
| Onboarding movement scene | `onboarding_bg_movement.png` | Onboarding page 1 |
| Onboarding collect scene | `onboarding_bg_collect.png` | Onboarding page 2 |
| Onboarding fuel scene | `onboarding_bg_fuel.png` | Onboarding page 3 |

After dropping the 3 onboarding backgrounds in `drawable-xxhdpi/`, swap the drawable refs in `OnboardingFragment.kt`:
```kotlin
private val pages = listOf(
    OnboardingPage(R.drawable.onboarding_bg_movement, R.string.onboarding_movement),
    OnboardingPage(R.drawable.onboarding_bg_collect,  R.string.onboarding_collect),
    OnboardingPage(R.drawable.onboarding_bg_fuel,     R.string.onboarding_fuel),
)
```

After dropping the real PNGs, swap the `src=` in `fragment_game.xml`:
- `ic_counter_crate` → `item_crate`
- `ic_counter_sack` → `item_sack`
- `ic_counter_brick` → `item_brick`
- `ic_fuel_marker` → `item_fuel`

## Level title font

The Figma uses a chunky outlined display font (looks like Bungee / Lilita One). The XML currently falls back to `sans-serif-black` so it builds without a font file.

To match the design:

1. Drop `level_title.ttf` into `app/src/main/res/font/`.
2. In `fragment_game.xml`, replace `android:fontFamily="sans-serif-black"` with `android:fontFamily="@font/level_title"` on `tv_level_title` and the three counter TextViews.

The brown outline is currently approximated with `shadowColor` / `shadowDx` / `shadowDy` / `shadowRadius`. For a real stroke, write a `StrokedTextView` (overrides `onDraw` to paint the stroke layer behind the fill), or pre-bake the title text with stroke into a PNG.

## How HUD values are driven

`GameFragment` exposes a few private fields you can hook up to game state later:

```kotlin
private var level: Int = 3
private var fuelPct: Float = 0.75f
private var cratesCollected = 0
// ...
```

Mutate them and call `renderHud()` to refresh the views. The fuel-bar marker uses `ConstraintLayout.LayoutParams.horizontalBias` — set it to `0f..1f`:

```kotlin
setFuelMarker(0.4f)  // marker at 40% along the bar
```
