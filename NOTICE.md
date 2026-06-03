# Licensing

Critter Colonies uses a split license: the code is open source, the artwork is not.

## Code — GNU GPL 3.0

All files under `src/main/java/` and the project build configuration
(`build.gradle`, `gradle.properties`, `settings.gradle`, `.gitignore`, etc.)
are licensed under the **GNU General Public License v3.0**.

See [`LICENSE`](LICENSE) for the full text, or
<https://www.gnu.org/licenses/gpl-3.0.txt>.

This satisfies the GPL-3.0 requirement of MineColonies, which Critter
Colonies references as a runtime dependency.

You are free to:

- Fork the repository and study, modify, and redistribute the **code**
  under GPL-3.0 terms
- Use the code as a basis for your own derivative mods, provided your
  derivative is also GPL-3.0 and source-available

## Assets — All Rights Reserved

All files under `src/main/resources/assets/crittercolonies/` — textures,
GeckoLib geometry (`.geo.json`), GeckoLib animations (`.animation.json`),
and any other creative output bundled with the mod — are **All Rights
Reserved** by the author.

See [`LICENSE-ASSETS`](LICENSE-ASSETS) for the full terms.

In short: you may use the official mod and include it in modpacks, but
you may **not** redistribute, modify, or reuse the artwork outside of the
official Critter Colonies mod without explicit written permission.

A derivative mod based on this code must therefore ship its own original
artwork.

## Mod Metadata

The `mod_license` field in `gradle.properties` and the `license` field
in `mods.toml` / `neoforge.mods.toml` are display-only metadata. They
summarise the split as `"GPL-3.0 (code) / ARR (assets)"`. The legally
binding terms are the two `LICENSE` files in this repository.
