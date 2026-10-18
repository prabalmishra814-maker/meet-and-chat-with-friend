# 4v4 Ludo King Game Assets & Licensing Documentation - 3D Edition

All visual graphics, 3D pawns, dice, board icons, and sound synthesis assets utilized in this standalone HTML5 4v4 Ludo game are original high-resolution vector assets and open-source audio elements.

## Asset Catalog & Licensing

| Asset File Path | Description | Format | License | Source / Notes |
| :--- | :--- | :--- | :--- | :--- |
| `assets/token_red.svg` | Glossy 3D Red Pawn Token with Metallic Base & Gold Crown Collar | SVG | MIT / Public Domain | Custom Created 3D Vector |
| `assets/token_green.svg` | Glossy 3D Green Pawn Token with Metallic Base & Gold Crown Collar | SVG | MIT / Public Domain | Custom Created 3D Vector |
| `assets/token_yellow.svg` | Glossy 3D Yellow Pawn Token with Metallic Base & Silver Collar | SVG | MIT / Public Domain | Custom Created 3D Vector |
| `assets/token_blue.svg` | Glossy 3D Blue Pawn Token with Metallic Base & Gold Crown Collar | SVG | MIT / Public Domain | Custom Created 3D Vector |
| `assets/dice_1.svg` - `dice_6.svg` | 3D Rounded White Dice Faces with Red/Black/Green Pips & Inset Shadow | SVG | MIT / Public Domain | Custom Created 3D Vector |
| `assets/star_safe.svg` | 3D Metallic Golden Star for Safe Squares | SVG | MIT / Public Domain | Custom Created 3D Vector |
| `assets/trophy_center.svg` | Center Finishing Crown & Trophy Graphic with Metallic Gold Highlights | SVG | MIT / Public Domain | Custom Created 3D Vector |
| `assets/icon_sound_on.svg` | Audio On Vector Icon | SVG | Material / CC0 | Material Design Icons |
| `assets/icon_sound_off.svg` | Audio Muted Vector Icon | SVG | Material / CC0 | Material Design Icons |
| `assets/icon_settings.svg` | Game Settings Gear Icon | SVG | Material / CC0 | Material Design Icons |
| `assets/icon_info.svg` | Rules / Info Vector Icon | SVG | Material / CC0 | Material Design Icons |

## Audio Synthesis

Audio effects (dice rolling, token movement, capturing, victory fanfare, and six-rolled chime) are dynamically generated using the **Web Audio API** oscillator and gain nodes. This ensures 100% offline reliability without requiring external MP3/WAV network downloads or file dependency issues inside Android WebView.
