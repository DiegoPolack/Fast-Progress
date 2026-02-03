# Fast Progress

[![Modrinth Downloads](https://img.shields.io/modrinth/dt/fast-progress?logo=modrinth&logoColor=white&label=Modrinth%20downloads)](https://modrinth.com/mod/fast-progress)
[![Modrinth Version](https://img.shields.io/modrinth/v/fast-progress?logo=modrinth&logoColor=white&label=Modrinth%20version)](https://modrinth.com/mod/fast-progress)
[![CurseForge](https://img.shields.io/badge/CurseForge-fast--progress-F16436?logo=curseforge&logoColor=white)](https://legacy.curseforge.com/minecraft/mc-mods/fast-progress)
[![Fabric](https://img.shields.io/badge/Loader-Fabric-DBD0B4)](#)
[![Forge](https://img.shields.io/badge/Loader-Forge-orange)](#)
[![NeoForge](https://img.shields.io/badge/Loader-NeoForge-5BC2E7)](#)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A lightweight progression/QoL mod that **multiplies block drops** when players break blocks — fully configurable (multiplier, structure blocks, placed blocks, Fortune/Silk Touch, and per-block filters).

---

## Links

- **Modrinth:** https://modrinth.com/mod/fast-progress  
- **CurseForge:** https://legacy.curseforge.com/minecraft/mc-mods/fast-progress  

---

## Supported Versions / Loaders

- **Minecraft 1.20.1**
  - Fabric
  - Forge
- **Minecraft 1.21.1**
  - Fabric
  - NeoForge
- **Minecraft 1.21.10**
  - Fabric
  - NeoForge
- **Minecraft 1.21.11**
  - Fabric
  - NeoForge

> Download the correct file for your **Minecraft version** and **loader**.

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

- **Server-side Friendly**  
  The server is authoritative: the mod works when installed on a server, and **vanilla clients can still join**.

- **In-Game Configuration (Optional Client UI)**  
  If you install the mod on the client (plus Cloth Config), you get an in-game config screen that can edit **server config** (OP-only) via sync.

---

## How It Works (high level)

When a player breaks a block, Fast Progress:
1. Checks config (multiplier, filters, and toggles).
2. Optionally excludes blocks inside generated structures.
3. Optionally excludes blocks that were placed by a player/entity (tracked on placement).
4. Computes vanilla drops (respecting enchantments) and spawns **additional copies** to reach the multiplier.

---

## Installation

### Server (recommended)
Install **Fast Progress** on the server. Edit `config/fastprogress.cfg` on the server, or use the admin commands below.

#### Fabric servers
Put these in the server `mods/` folder:
- **Fast Progress (Fabric)**
- **Fabric API**
- **Architectury API**

#### Forge servers (1.20.1)
Put these in the server `mods/` folder:
- **Fast Progress (Forge)**
- **Architectury API**

#### NeoForge servers (1.21.x)
Put these in the server `mods/` folder:
- **Fast Progress (NeoForge)**
- **Architectury API**

### Client (optional, for in-game config UI)
If you want the in-game config screen:
- install the matching **Fast Progress** jar on the client
- plus:
  - **Cloth Config** (matching loader)
  - *(Fabric only, optional but recommended)* **Mod Menu**

> In multiplayer, the config screen can edit the **server config** only if you are **OP/admin**. Otherwise it will fall back to local-only behavior.

---

## Admin Commands (Server-side)

These commands work server-side (useful even for vanilla clients). OP/admin required.

- `/fastprogress show`
- `/fastprogress reload`
- `/fastprogress multiplier <1..64>`
- `/fastprogress affectStructureBlocks <true|false>`
- `/fastprogress affectPlacedBlocks <true|false>`
- `/fastprogress affectFortuneDrops <true|false>`
- `/fastprogress affectSilkTouchDrops <true|false>`
- `/fastprogress filter <blacklist|whitelist>`
- `/fastprogress block add <minecraft:block_id>`
- `/fastprogress block remove <minecraft:block_id>`
- `/fastprogress block list`

Alias: `/fp`

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

---

## Building from Source

### Requirements
- **Java 17** for the 1.20.1 project
- **Java 21** for the 1.21.x projects
- Gradle (wrapper included)

### Build

See [`Build-Guide.txt`](Build Guide).

## License

This project is licensed under the **MIT License**. See [`LICENSE`](LICENSE).
