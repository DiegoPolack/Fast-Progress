
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/fast-progress?logo=modrinth&logoColor=white&label=Modrinth%20downloads)](https://modrinth.com/mod/fast-progress)
[![Modrinth Version](https://img.shields.io/modrinth/v/fast-progress?logo=modrinth&logoColor=white&label=Modrinth%20version)](https://modrinth.com/mod/fast-progress)
[![CurseForge](https://img.shields.io/badge/CurseForge-fast--progress-F16436?logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/fast-progress)
[![NeoForge](https://img.shields.io/badge/Loader-NeoForge-5BC2E7)](#)
[![Fabric](https://img.shields.io/badge/Loader-Fabric-DBD0B4)](#)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A lightweight progression/QoL mod that **multiplies block drops** when players break blocks — fully configurable (multiplier, structure blocks, placed blocks, Fortune/Silk Touch, and per-block filters).

---

## Links

- **Modrinth:** https://modrinth.com/mod/fast-progress
- **CurseForge:** https://www.curseforge.com/minecraft/mc-mods/fast-progress

---

## Supported Versions / Loaders

- **Minecraft 1.20.1**
  - Fabric
  - Forge
  - NeoForge (same forge .jar)
- **Minecraft 1.21.1**
  - Fabric
  - NeoForge
- **Minecraft 1.21.10**
  - Fabric
  - NeoForge
- **Minecraft 1.21.11**
  - Fabric
  - NeoForge

> Make sure you download the correct jar for your Minecraft version and loader.

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

### Fabric (1.20.1 / 1.21.1 / 1.21.10 / 1.21.11)
Place the following in your `mods/` folder:
- `Fast Progress (Fabric).jar`
- **Fabric API**
- **Architectury API**
- **Cloth Config (Fabric)**
- *(Optional but recommended)* **Mod Menu** (enables the config button in the Mods list)

### Forge (1.20.1)
Place the following in your `mods/` folder:
- `Fast Progress (Forge).jar`
- **Architectury API**
- **Cloth Config (Forge)**

### NeoForge (1.20.1 / 1.21.1 / 1.21.10 / 1.21.11)
Place the following in your `mods/` folder:
- `Fast Progress (NeoForge).jar`
- **Architectury API**
- **Cloth Config (NeoForge)**

> Make sure you’re using the correct jar for your loader (Fabric vs Forge vs NeoForge).

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
