# GreedyPro Fruit & Food Game - Assets Documentation

All visual graphics, illustrations, vectors, and audio synthesized assets in this standalone game were custom-designed and generated specifically for the GreedyPro game package inside `greedy-game/assets/`.

## License Information

- **License**: CC0 1.0 Universal / Public Domain Dedication.
- **Usage**: Free for personal, commercial, educational, and open-source distribution without restriction.
- **Dependencies**: 100% self-contained local SVG files and Web Audio API synthesized sound effects. No external network requests required at runtime.

---

## Asset Inventory

### Background & Environment
| File Path | Description | Format |
|---|---|---|
| `assets/bg_game.svg` | Vibrant fantasy casino night sky with ambient glowing orbs, stars & skyline | SVG |
| `assets/bg_space.svg` | Deep purple fantasy night sky with stars, nebulae, and city horizon | SVG |

### Header & Branding
| File Path | Description | Format |
|---|---|---|
| `assets/jackpot_gold.svg` | Ultra-detailed 3D metallic gold "JACKPOT" title banner with gold coin stacks | SVG |
| `assets/chef_greedypro.svg` | Cartoon chef character avatar with "Greedy Pro" gold banner | SVG |
| `assets/coin_gold.svg` | Shiny golden coin icon | SVG |

### Food & Fruit Multiplier Artwork
| File Path | Description | Multiplier | Format |
|---|---|---|---|
| `assets/food_tomato.svg` | 3D glossy red tomato with stem | x5 | SVG |
| `assets/food_cabbage.svg` | Green lettuce/cabbage head | x5 | SVG |
| `assets/food_corn.svg` | Golden corn cob with green husks | x5 | SVG |
| `assets/food_carrot.svg` | Vibrant 3D orange carrot | x5 | SVG |
| `assets/food_hotdog.svg` | Hotdog in bun with mustard | x10 | SVG |
| `assets/food_skewer.svg` | Grilled meat and vegetable skewers | x15 | SVG |
| `assets/food_ham.svg` | Juicy meat drumstick ham with bone | x25 | SVG |
| `assets/food_steak.svg` | T-Bone steak cut with marbling | x45 | SVG |
| `assets/food_pizza.svg` | Pepperoni pizza slice | x8 | SVG |
| `assets/food_salad.svg` | Fresh garden salad in bowl | x8 | SVG |

### Casino Chips & Interactive Elements
| File Path | Description | Value | Format |
|---|---|---|---|
| `assets/chip_1k.svg` | Magenta 3D Casino Chip | 1K (1,000) | SVG |
| `assets/chip_10k.svg` | Orange/Red 3D Casino Chip | 10K (10,000) | SVG |
| `assets/chip_100k.svg` | Cyan/Teal 3D Casino Chip | 100K (100,000) | SVG |
| `assets/chip_500k.svg` | Royal Blue 3D Casino Chip | 500K (500,000) | SVG |
| `assets/chip_1m.svg` | Emerald Green 3D Casino Chip | 1M (1,000,000) | SVG |
| `assets/icon_music.svg` | Golden circular background music/sound button | N/A | SVG |
| `assets/icon_menu.svg` | Golden circular menu button | N/A | SVG |
| `assets/icon_plus.svg` | Golden circular plus button | N/A | SVG |
| `assets/icon_info.svg` | Blue information icon button | N/A | SVG |

---

## Audio Synthesizer

Sound effects are generated dynamically via the HTML5 **Web Audio API** in `script.js` (sine/triangle oscillator frequencies with volume envelopes). No `.mp3` or `.wav` network downloads required!
