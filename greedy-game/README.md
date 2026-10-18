# GreedyPro Fruit & Food Standalone Game

Welcome to the **GreedyPro Fruit & Food Game**, a fully independent, standalone HTML5 game recreated precisely from the GreedyPro reference design.

---

## 🚀 How to Run the Game

1. Navigate to the `greedy-game/` folder.
2. Open `index.html` directly in any web browser (Chrome, Edge, Firefox, Safari, Mobile Browsers).

```bash
# Example double-click or open in browser:
double-click greedy-game/index.html
```

No build tools, npm packages, web servers, or dependencies are required!

---

## 📁 Directory Structure

```text
greedy-game/
├── index.html        # Main HTML5 game page
├── style.css         # Game styling scoped strictly to .greedy-game
├── script.js        # Game state, betting logic, timer, wheel spin, audio synthesizer
├── assets/           # Local vector SVG graphics & artwork
│   ├── bg_space.svg
│   ├── jackpot_gold.svg
│   ├── chef_greedypro.svg
│   ├── food_tomato.svg
│   ├── food_cabbage.svg
│   ├── food_corn.svg
│   ├── food_carrot.svg
│   ├── food_hotdog.svg
│   ├── food_skewer.svg
│   ├── food_ham.svg
│   ├── food_steak.svg
│   ├── food_pizza.svg
│   ├── food_salad.svg
│   ├── chip_1k.svg
│   ├── chip_10k.svg
│   ├── chip_100k.svg
│   ├── chip_500k.svg
│   ├── chip_1m.svg
│   ├── icon_menu.svg
│   ├── icon_plus.svg
│   └── icon_info.svg
├── ASSETS.md         # Image inventory and license details
└── README.md         # Instructions and documentation
```

---

## 🎮 Game Features & Controls

- **Coin Chips Selection**: Choose between `1K`, `10K`, `100K`, `500K`, and `1M` chip denominations.
- **Placing Bets**: Click on any of the 8 food bubbles (`Tomato x5`, `Cabbage x5`, `Corn x5`, `Carrot x5`, `Hotdog x10`, `Skewer x15`, `Ham x25`, `Steak x45`) or the corner buttons (`Pizza x8`, `Salad x8`).
- **28-Second Round Countdown**: Players can place bets during the countdown phase.
- **Animated Wheel Spin**: When the timer reaches 0s, the selection phase locks and the wheel spins sequentially with realistic deceleration before landing on the winning outcome.
- **Real-Time Balance & Winnings**: Correct bets pay out `Bet Amount × Multiplier`. Today's winnings and remaining coins update automatically with victory notifications.
- **Result History**: Bottom strip shows recent game results with a `new` badge on the latest outcome.
- **Audio Synthesizer**: Web Audio API sound effects for chip placement, countdown ticks, spin highlights, and win chimes.
- **Free Coins Recharge**: Click the golden `+` button or visit settings to claim 50,000 or 100,000 free virtual coins.
- **Menu & Settings Modal**: Toggle sound effects, review payout multiplier tables, or reset game data.

---

## 🔒 Isolation & Safety Guarantees

- **100% Standalone**: Located strictly inside `greedy-game/`. No existing project files, code, dependencies, or routes were modified.
- **Scoped Styling**: All CSS classes are scoped under `.greedy-game`.
- **Private JS Scope**: All JavaScript code is encapsulated in an IIFE (`(function() { ... })();`), preventing global variable leakage.
