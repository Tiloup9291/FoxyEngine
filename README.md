# FoxyEngine - Java 3D Engine v1.0

A 100% Java software 3D engine. No GPU, no native libraries, no third-party
runtime dependencies. Everything (math, rasterizer, physics, audio mixing,
animation, particles) is written in pure Java on top of AWT/Swing for display
and input.

* 86 main sources across 12 packages, 43 test files (~197 tests)
* Fixed-tick simulation (60 Hz) + frame-capped rendering (60 FPS)
* Parallel software rasterizer (configurable raster bands)
* Playable demo: animated robot, 2 levels, orbit / free cameras, procedural
  audio, particles

## Features

* Software renderer: solid + wireframe, Phong shading, directional + ambient
  light, near-plane clipping, frustum culling, depth buffering, bilinear /
  nearest texture filtering, vignette + flash + fade post-process.
* Cameras: orbit-follow rig (tracks the robot) and free-fly rig. Mouse look,
  wheel zoom, arrow-key look/turn.
* Physics: gravity, ground plane, static AABB colliders, dynamic bodies
  (player + crate), broadphase grid, raycast queries.
* Animation: rigid-body humanoid rig (11 parts), keyframed clips (idle / walk /
  run / jump), cross-fade, state machine driven by body speed and ground
  contact.
* Levels: background loading thread with request queue and ready cache.
* Assets: reference-counted model / texture / sound caches behind one
  `AssetManager` facade.
* Audio: dedicated audio thread, software stereo mixer (gain / pan / pitch
  voices), distance attenuation + stereo pan spatializer, procedural synth
  sounds (jump / step / land / explosion), WAV decoding.
* FX: fixed-size pooled particle system (explosion, dust, sparks, rain) +
  full-screen post-process. Zero allocation after construction on the hot path.
* UI: pause menu, HUD (status, physics, assets, particles, LOD counters,
  profiler line), message queue, startup splash overlay.
* Profiling: per-section timers (input, physics, animation, pipeline, FX).

## Architecture

```text
                    +-------------------+
                    |   EngineKernel    |  owns everything, runs the loop
                    +--------+----------+
                             |
   +---------+----------+----+--------+-----------+----------+
   |         |          |             |           |          |
 Input    Physics   Animation      Render      Audio       FX / UI
```

* `engine.Main` loads `config/engine.properties` (falls back to defaults),
  builds `EngineKernel`, starts the loop, registers a shutdown hook.
* `engine.core.EngineKernel` creates the window, canvas, framebuffer, renderer,
  pipeline, input, physics, avatar, audio, menus, HUD, levels, particles, LOD
  and profiler. Each frame: fixed-step logic ticks, then one render + present
  via `BufferStrategy`, then AWT HUD.
* `engine.input` (AWT listeners) collects keys / mouse / wheel into
  `InputState`, snapshots one immutable `InputFrame` per tick. `ActionMapper`
  maps key codes to logical `InputAction`s (QWERTY + AZERTY defaults).
  `driveCameraPlayer()` filters movement keys so they drive the robot, not the
  orbit camera.
* `engine.physics` steps the world on a fixed accumulator. Single writer, no
  internal locks.
* `engine.pipeline` renders from an immutable `SceneSnapshot` in three stages:
  `TransformStage` (model -> world -> view), `ProjectionStage` (clip ->
  screen), `RenderStage` (dispatch), with parallel raster bands.
* `engine.render` is the software rasterizer: `Framebuffer` pixel array shared
  with a `BufferedImage` for zero-copy display + float depth buffer, triangle
  raster, Phong shading, LOD substitution for decor only.
* `engine.level.LevelThread`, `engine.audio.AudioThread`,
  `engine.fx.FXThread`, `engine.ui.MenuThread` run off the main loop and talk
  to the kernel through queues / snapshots.

### Package map

| Package | Contents |
|---|---|
| `engine` | `Main` entry point |
| `engine.core` | `EngineKernel`, `EngineConfig`, `FrameLimiter`, `Profiler`, `Splash` |
| `engine.math` | `Vec2/3/4`, `Mat4`, `Quaternion`, `AABB` |
| `engine.render` | `Mesh`, `Material`, `Texture`, `TextureCache`, `Camera`, `CameraRig`, `Transform`, `Frustum`, `Clipper`, `Framebuffer`, `Rasterizer`, `PhongRaster`, `Lighting`, `Light`, `Renderer`, `Lod`, `LodMesh` |
| `engine.pipeline` | `SceneSnapshot`, `TransformStage`, `ProjectionStage`, `RenderStage`, `RenderPipeline` |
| `engine.physics` | `Body`, `Collider`, `Contact`, `Broadphase`, `PhysicsWorld`, `PlayerController`, `Ray`, `RayHit`, `Raycast` |
| `engine.anim` | `Node`, `Rig`, `Humanoid`, `Channel`, `Keyframe`, `Track`, `Clip`, `Clips`, `Animator`, `AnimStateMachine` |
| `engine.assets` | `ObjParser`, `MtlParser`, `ObjMeshBuilder`, `ObjParseException`, `ModelRepository`, `AssetManager` |
| `engine.level` | `LevelDef`, `LevelParser`, `LevelParseException`, `LevelThread` |
| `engine.input` | `InputAction`, `ActionMapper`, `InputFrame`, `InputState`, `InputThread` |
| `engine.audio` | `AudioClip`, `WavDecoder`, `SynthSounds`, `SoundBank`, `AudioMixer`, `Spatializer`, `AudioThread` |
| `engine.fx` | `Particle`, `ParticlePool`, `Emitters`, `ParticleRenderer`, `PostProcess`, `FXThread` |
| `engine.ui` | `GameState`, `Menu`, `MenuItem`, `MenuThread`, `Hud` |

## Controls

| Key | Action |
|---|---|
| W / Z, S, A / Q, D | Move robot (forward / back / left / right) |
| SPACE | Jump |
| Mouse move | Look (when captured) |
| Mouse wheel | Zoom (orbit distance) |
| Arrow Left / Right | Turn camera |
| Arrow Up / Down | Look up / down |
| E / X (or Ctrl) | Camera up / down |
| G | Explosion FX |
| N | Next level (`demo` <-> `arena`) |
| M | Next model (showcase cube / pyramid / plane) |
| C | Toggle orbit / free camera |
| R | Respawn player at current level spawn |
| V | Toggle solid / wireframe |
| T | Toggle profiler HUD line |
| F | Toggle mouse capture |
| ESC / P, ENTER | Pause / resume, confirm menu item |

In robot mode, W/A/S/D drive the body; arrows, mouse and wheel still drive the
orbit camera. `driveCameraPlayer()` enforces this split.


## Implementation examples

Render one frame (simplified):

```java
SceneSnapshot snap = buildSnapshot(instances, camera, lights);
pipeline.render(snap, framebuffer);   // transform -> project -> raster bands
postProcess.apply(framebuffer);       // vignette + flash + fade
particles.draw(framebuffer, camera);  // billboards with depth test
present(framebuffer);                 // BufferStrategy blit + HUD
```

Physics fixed step:

```java
world.accumulate(dt);
while (world.consumeStep(fixedDt)) {
    controller.apply(input, player);  // accel + jump impulse
    world.step(fixedDt);              // gravity, ground, AABB contacts
}
```

Animation selection:

```java
AnimState state = stateMachine.select(speed, onGround, jumping);
animator.play(state.clip(), crossFadeTime);
animator.update(dt, rig);             // pose -> mesh part transforms
```

Level background load:

```java
levels.request("arena");              // queue, parsed off-thread
if (levels.isReady("arena")) {
    loadLevel("arena");               // apply colliders, spawn, decor
}
```

## Build and run

Requirements: JDK 17, Gradle wrapper included. No runtime dependencies.

```bat
gradlew.bat run
```

```sh
./gradlew run
```

Manual compile without Gradle (from the project root):

```sh
javac -encoding UTF-8 -d out $(find src/main/java -name "*.java")
java -cp out engine.Main
```

The working directory must be the project root: the engine loads relative
paths `config/engine.properties` and `assets/...`. All tuning lives in
`config/engine.properties` (window, FPS cap, tick rate, render mode, FOV,
near/far, lights, camera speeds, mouse, gravity, player speed/jump, audio,
LOD, profiler).

## Frame rate and tick ranges

* `engine.fps.target`: 0-120. `0` = unlimited (no pacing).
* `engine.tick.rate`: 0-240. `0` = one variable tick per frame (physics uses
  the real frame delta). Any positive value uses a fixed step
  (`1/tick` seconds); physics runs up to 16 substeps per frame, then drops
  excess time (shown as `DROP xms` in the HUD info line) instead of freezing.
* Out-of-range values are clamped (`fps` to 120, `tick` to 240, negatives to
  0), so every value in range behaves sanely.

Performance notes (quality conserved: scale 1.0, bilinear, post-process on):

* Multi-band rendering tiles by scanline overlap: each band rasterizes only
  the triangles touching its rows and reuses a thread-local scratch buffer
  instead of allocating a full framebuffer per band per frame.
* The Phong inner loop is allocation-free per pixel (no `Vec3` garbage) and
  skips the specular `pow` when the material has no specular.
* Near-plane clipping fast-paths fully visible triangles (no polygon lists).
* HUD status strings refresh at 4 Hz instead of every frame (no per-frame
  `String.format`).
* If the demo still drops on your machine, raise `engine.pipeline.bands` to
  the CPU core count and check the `T` profiler line to find the hot section
  (`render` vs `pipeline` vs `post`).

## Tests

43 test files (~197 tests) cover math, clipping, raster, textures, LOD,
OBJ/MTL parsing, asset caches, audio mixer/spatializer/synth/WAV, physics,
broadphase, raycast, pipeline, input mapping and camera behavior, menus, HUD,
particles, post-process, config, profiler and splash.

What they are for: regression safety for the hot loop (transform, projection,
raster, physics step), format parsers (OBJ/MTL/level/WAV), cache accounting,
and input/camera rules (e.g. movement keys must not zoom the orbit camera).

Run:

```bat
gradlew.bat test
```

```sh
./gradlew test
```

Reports land under `build/reports/tests/test` and `build/test-results/test`.

## Java version and dependencies

* Language level: Java 17 (`options.release = 17`, toolchain 17).
* Runtime: JDK only (AWT/Swing for window, input, audio line). Zero runtime
  deps.
* Build: Gradle `java` + `application` plugins, main class `engine.Main`.
* Test only: JUnit Jupiter `5.10.3`, JUnit Platform launcher `1.10.3`
  (Maven Central). Not shipped with the game.

## Supported files and syntax

### OBJ models (`assets/models/*.obj`)

Subset of Wavefront OBJ: `v` positions, `vt` UVs, `vn` normals (optional),
`usemtl`, `mtllib`, quad or triangle faces `f v/vt/vn`. Quads are split into
two triangles. Strict validation with `ObjParseException` on bad data.

```obj
mtllib cube.mtl
v -1 -1 -1
v  1 -1 -1
vt 0 0
vt 1 0
usemtl blue
f 5/1 6/2 7/3 8/4
```

### MTL materials (`assets/models/*.mtl`)

```mtl
newmtl blue
Ka 0.10 0.10 0.12
Kd 0.30 0.64 1.00
Ks 0.60 0.60 0.60
Ns 48
map_Kd checker.png
```

`Ka` ambient, `Kd` diffuse, `Ks` specular, `Ns` shininess, `map_Kd` color
texture (resolved inside `assets/models`, PNG/JPG via `ImageIO` or grayscale
fallback).

### Levels (`assets/levels/*.lvl`)

Line format, `#` comments, `key=value` per line:

```lvl
# Demo level: decor + platform + wall (all collidable).
name=demo
spawn=0,2,2
ground=0
# model,x,y,z,yawDeg,scale[,collide]
entity=cube,3,0.5,-2,15,1.0,collide
entity=pyramid,-3,0.6,1,0,1.2,collide
entity=plane,0,-0.01,0,0,10.0
# Explicit static boxes: cx,cy,cz,sx,sy,sz
static=2.5,0.25,-2.5,2,0.5,2
static=-3,1.0,-4,1,2,0.5
```

Keys: `name`, `spawn=x,y,z`, `ground=y`,
`entity=model,x,y,z,yawDeg,scale[,collide]`,
`static=cx,cy,cz,sx,sy,sz`. Malformed lines throw `LevelParseException`
naming the file and line number. `engine.model.id` picks the showcase model;
level entities reference the same model ids by file name without extension.

### Textures (`assets/textures/*.png`)

PNG/JPG loaded through `ImageIO` into `TextureCache` (reference-counted,
purgeable). Sampling: `nearest` or `bilinear` via `engine.texture.filter`.
UVs come from the OBJ `vt` entries; missing UVs sample texel `(0,0)`.

### Engine config (`config/engine.properties`)

Flat `engine.*` keys, all with built-in defaults (see
`EngineConfig.defaults()`):

```properties
engine.window.width=800
engine.window.height=600
engine.window.title=Java 3D Engine v1.0
engine.fps.target=60
engine.tick.rate=60
engine.render.mode=solid
engine.render.fov=70
engine.render.near=0.1
engine.render.far=100
engine.camera.mode=orbit
engine.model.id=cube
engine.model.scale=0.8
```

Full key list: window size/title, fps target (0 = unlimited), tick rate,
render mode (`solid`/`wireframe`), FOV, near/far, clear color, cube/wire
colors, model id/scale, camera mode (`orbit`/`fps`) + move/rot speeds, light
direction vector (toward the light) + intensity/color/ambient, texture filter,
pipeline band count (1 = single-thread), mouse sensitivity / wheel zoom /
capture flag, gravity Y, ground Y, player speed/jump, audio enabled/master/max
distance, LOD and profiler flags.

### Audio (`SynthSounds` + `*.wav`)

No audio assets ship with the repo: jump / step / land / explosion are
synthesized at startup. `WavDecoder` accepts PCM WAV files (8/16-bit,
mono/stereo) for `SoundBank` if you add your own.

