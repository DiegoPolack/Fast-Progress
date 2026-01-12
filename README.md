# Fast Progress

[![Modrinth Downloads](https://img.shields.io/modrinth/dt/fast-progress?logo=modrinth&logoColor=white&label=Modrinth%20downloads)](https://modrinth.com/mod/fast-progress)
[![Modrinth Version](https://img.shields.io/modrinth/v/fast-progress?logo=modrinth&logoColor=white&label=Modrinth%20version)](https://modrinth.com/mod/fast-progress)
[![NeoForge](https://img.shields.io/badge/Loader-NeoForge-5BC2E7)](#)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A lightweight progression/QoL mod that **multiplies block drops** when players break blocks — fully configurable (multiplier, structure blocks, placed blocks, Fortune/Silk Touch, and per-block filters).

> **Minecraft:** 1.20.1 (Fabric & Forge)  
> *(More versions/loaders may be added later.)*

---

## Links

- **Modrinth:** https://modrinth.com/mod/fast-progress  
- **CurseForge:** https://legacy.curseforge.com/minecraft/mc-mods/fast-progress  

---

## Features

- **Drop Multiplier**  
  Multiply block drops by a configurable factor (`BlockDropMultiplier`).

- **Structure Block Control**  
  Choose whether the multiplier applies to blocks that are part of **generated structures**.

- **Placed Block Control**  
  Choose whether the multiplier applies to blocks **placed by players/entities** (the mod tracks placed blocks).

- **Fortune / Silk Touch Toggles**  
  Decide whether the multiplier should apply when the tool has **Fortune** and/or **Silk Touch**.

- **Whitelist / Blacklist Filter**  
  Precisely control which blocks are affected (supports block IDs, and optionally tags if enabled in your build).

- **In-Game Config Menu**  
  Edit the config from the Mods menu (Cloth Config UI).

---

## How It Works (high level)

When a player breaks a block, Fast Progress:
1. Checks config (multiplier, filters, and toggles).
2. Optionally excludes blocks inside generated structures.
3. Optionally excludes blocks that were placed by a player/entity (tracked on placement).
4. Computes vanilla drops (respecting enchantments) and spawns **additional copies** to reach the multiplier.

---

## Installation

### Fabric (1.20.1)
Place the following in your `mods/` folder:
- `Fast Progress (Fabric).jar`
- **Fabric API**
- **Cloth Config (Fabric)**
- *(Optional but recommended)* **Mod Menu** (enables the config button in the Mods list)

### Forge (1.20.1)
Place the following in your `mods/` folder:
- `Fast Progress (Forge).jar`
- **Cloth Config (Forge)**

> Make sure you’re using the correct jar for your loader (Fabric vs Forge).

---

## Configuration

Fast Progress creates a config file at:

`config/fastprogress.cfg`

### Example
```cfg
BlockDropMultiplier: 2
# Determines the factor which the drop rate increase. Default: 2

AffectStructureBlocks: false
# Determines if the block drop multiplier affects blocks from generated structures. Default: false

AffectEntityAndPlayerPlacedBlocks: false
# Determines if the block drop multiplier affects blocks placed by Player or Entities. Default: false

AffectFortuneDrops: true
# If false, the drop multiplier will NOT apply when the tool has Fortune.

AffectSilkTouchDrops: true
# If false, the drop multiplier will NOT apply when the tool has Silk Touch.

Filter: 0
# 0 = Blacklist ; 1 = Whitelist. Default = 0
# Blacklist: blocks in the list are NOT affected.
# Whitelist: ONLY blocks in the list are affected. (If the list is empty, nothing is affected.)

Blocks:
# One block id per line
# minecraft:diamond_ore
# minecraft:ancient_debris
```

### Filter Rules
- **Blacklist (0):** blocks listed are **NOT affected**.
- **Whitelist (1):** **ONLY** blocks listed are affected. If the list is empty, nothing is affected.

### In-Game Config
You can edit these settings from the Mods menu:
- Fabric: via Mod Menu
- Forge: via the built-in Mods screen config button

---

## Building from Source

### Requirements
- **Java 17**
- Gradle (wrapper included)

### Build
```bash
./gradlew build
```

Artifacts will be generated in:
- `fabric/build/libs/`
- `forge/build/libs/`

### Clean Build
```bash
./gradlew clean build
```

---

## Project Structure (Architectury)

- `common/` — shared logic (drops, filters, tracking, structure checks)
- `fabric/` — Fabric platform hooks (events, Mod Menu integration)
- `forge/` — Forge platform hooks (events, config screen registration)

---

## License

This project is licensed under the **MIT License**. See [`LICENSE`](LICENSE).
