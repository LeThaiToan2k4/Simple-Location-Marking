# Changelog

All notable changes to **Simple Location Marking** are documented in this file.

## [1.0.0] - Fabric 1.21.11

### Added
- **Location Management GUI (`P` Keybind):** Added `LocationListScreen` bound to the `P` key by default under the `Simple Location Marking` keybind category, styled with a cohesive **Translucent Dark Glass** theme (`0x7008090C` full-screen backdrop, `0xD012141C` main glass panel with `0xFF2E3446` `1px` border, and `0xE01A1D28` top header bar).
- **Accent-Colored Header Pill Badges:** Added header pill boxes in `LocationListScreen` showing the active dimension with distinct accent colors (emerald green for `Overworld`, crimson for `Nether`, purple for `The End`) and the player's current standing block coordinates (`X, Y, Z`).
- **Card-Styled Scrollable Waypoint List:** Styled the list viewport with a recessed dark translucent background (`0x880B0C10`), slim `4px` semi-transparent scrollbar, and clean row cards (`0x661E222F` normal, `0x99282E40` hover, `0xBB2D364D` + `1px` bright border when selected) featuring a glowing colored square swatch, waypoint name, muted gray `(X, Y, Z)` coordinates, right-aligned distance badge (`m`/`km`), and a distinct amber/red `[Hidden]` (`[Đã ẩn]`) pill tag when hidden.
- **MS Paint-Style Hexagon Color Palette & Live Marker Preview:** Implemented a custom pointy-top hexagonal honeycomb color picker widget (`HexColorPalette`) covering the full HSV/RGB spectrum plus a grayscale row with a multi-layered glowing outline around the selected cell, paired with a real-time **Live Color & Marker Preview Box** on `AddWaypointScreen` and `EditWaypointScreen`.
- **Add, Edit, Hide/Show, and Delete Controls:**
  - `Add`: Opens `AddWaypointScreen` with recessed translucent sub-panels for the name input, read-only standing coordinates (`X, Y, Z`), hexagon color palette (with random initial color selection), and live marker preview.
  - `Edit`: Opens `EditWaypointScreen` with editable name, editable integer `X, Y, Z` coordinates, hexagon color picker pre-selected to the waypoint's current color, and live marker preview.
  - `Hide/Show`: Toggles waypoint visibility in-game immediately and displays an amber/red `[Hidden]` (`[Đã ẩn]` in Vietnamese) badge in the management list when hidden.
  - `Delete`: Removes the selected waypoint from the active list, JSON storage file, and in-game HUD.
- **Translucent Dark Glass In-Game HUD Waypoint Markers:**
  - **Compact Default Badge:** Renders a centered `16x16` dark glass badge (`0xA6101218`) with a `1px` outer border + `2px` top accent bar in the waypoint's RGB color and a brightly tinted uppercase initial with drop shadow.
  - **Crosshair Hover Expansion:** Smoothly expands into a translucent dark glass pill (`0xBF101218`) displaying the full capitalized waypoint name when the player aims the crosshair near the marker.
  - **Scaled Distance Label:** Renders the distance (`<number>m` below `10000m`, `<number>km` at `>= 10000m`) directly below the badge inside a `0.75x`-scaled translucent dark pill (`0x80000000`) with `0xFFE0E0E0` text.
- **Constant Screen-Space Waypoint Scaling & Far-Distance Visibility:** Normalizes camera-relative waypoint direction vectors onto a unit sphere before applying view-projection matrices and locks HUD marker rendering to a fixed screen-space size so waypoints at `10m`, `500m`, or `50km+` never clip against Vanilla's far plane and never distort in scale.
- **Dimension Isolation & Per-World/Server JSON Storage:** Automatically isolates waypoints per dimension (`minecraft:overworld`, `minecraft:the_nether`, `minecraft:the_end`, or custom dimensions) and saves separate JSON files per singleplayer world folder or multiplayer server IP inside `.minecraft/config/simplelocationmarking/`.
- **Zero-Allocation HUD Rendering:** Pre-allocates transformation vectors, sort buffers, and cached text strings/widths to maintain high FPS with zero per-frame garbage collection overhead.
- **Bilingual Localization:** Added full English (`en_us.json`) and Vietnamese (`vi_vn.json`) language translations.
