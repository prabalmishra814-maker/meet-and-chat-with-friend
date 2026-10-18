import os
import urllib.request

assets_dir = 'lucky_fruit/assets'
os.makedirs(assets_dir, exist_ok=True)

# 1. Download core Fluent 3D Emoji assets
download_list = {
    'tomato.webp': 'https://cdn.jsdelivr.net/gh/microsoft/fluentui-emoji@main/assets/Tomato/3D/tomato_3d.png',
    'lettuce.webp': 'https://cdn.jsdelivr.net/gh/microsoft/fluentui-emoji@main/assets/Leafy%20green/3D/leafy_green_3d.png',
    'corn.webp': 'https://cdn.jsdelivr.net/gh/microsoft/fluentui-emoji@main/assets/Ear%20of%20corn/3D/ear_of_corn_3d.png',
    'carrot.webp': 'https://cdn.jsdelivr.net/gh/microsoft/fluentui-emoji@main/assets/Carrot/3D/carrot_3d.png',
    'bread.webp': 'https://cdn.jsdelivr.net/gh/microsoft/fluentui-emoji@main/assets/Hot%20dog/3D/hot_dog_3d.png',
    'skewers.webp': 'https://cdn.jsdelivr.net/gh/microsoft/fluentui-emoji@main/assets/Dango/3D/dango_3d.png',
    'meat_leg.webp': 'https://cdn.jsdelivr.net/gh/microsoft/fluentui-emoji@main/assets/Poultry%20leg/3D/poultry_leg_3d.png',
    'meat.webp': 'https://cdn.jsdelivr.net/gh/microsoft/fluentui-emoji@main/assets/Cut%20of%20meat/3D/cut_of_meat_3d.png',
    'pizza.webp': 'https://cdn.jsdelivr.net/gh/microsoft/fluentui-emoji@main/assets/Pizza/3D/pizza_3d.png',
    'salad.webp': 'https://cdn.jsdelivr.net/gh/microsoft/fluentui-emoji@main/assets/Green%20salad/3D/green_salad_3d.png',
    'gold-coin.png': 'https://cdn.jsdelivr.net/gh/microsoft/fluentui-emoji@main/assets/Coin/3D/coin_3d.png',
    'greedy-character.png': 'https://cdn.jsdelivr.net/gh/microsoft/fluentui-emoji@main/assets/Man%20cook/Default/3D/man_cook_3d_default.png'
}

for filename, url in download_list.items():
    path = os.path.join(assets_dir, filename)
    try:
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
        with urllib.request.urlopen(req) as resp, open(path, 'wb') as f:
            f.write(resp.read())
        print(f"[OK] Downloaded {filename}")
    except Exception as e:
        print(f"[ERR] {filename}: {e}")

# 2. Helper to write SVGs
def write_svg(filename, content):
    with open(os.path.join(assets_dir, filename), 'w', encoding='utf-8') as f:
        f.write(content.strip())
    print(f"[OK] Created SVG {filename}")

# Casino Chips
def make_chip(filename, outer, inner, text, accent, ring):
    content = f'''<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100" width="100" height="100">
  <defs>
    <radialGradient id="bg_{text}" cx="35%" cy="35%" r="65%">
      <stop offset="0%" stop-color="{accent}" />
      <stop offset="60%" stop-color="{inner}" />
      <stop offset="100%" stop-color="{outer}" />
    </radialGradient>
    <filter id="shadow_{text}" x="-20%" y="-20%" width="140%" height="140%">
      <feDropShadow dx="0" dy="4" stdDeviation="3" flood-color="#000" flood-opacity="0.6"/>
    </filter>
  </defs>
  <!-- Outer Glow & Ring -->
  <circle cx="50" cy="50" r="47" fill="{ring}" filter="url(#shadow_{text})" stroke="#FFE66D" stroke-width="2"/>
  <circle cx="50" cy="50" r="44" fill="{outer}"/>
  <!-- Casino Dashed Outer Rim -->
  <circle cx="50" cy="50" r="41" fill="none" stroke="#FFFFFF" stroke-width="4" stroke-dasharray="10 9" opacity="0.9"/>
  <!-- Inner Ring & Surface -->
  <circle cx="50" cy="50" r="33" fill="url(#bg_{text})" stroke="#FFD700" stroke-width="2"/>
  <circle cx="50" cy="50" r="25" fill="{inner}" opacity="0.95"/>
  <!-- Center Text -->
  <text x="50" y="55" font-family="'Impact', 'Arial Black', sans-serif" font-weight="900" font-size="{20 if len(text)<=3 else 16}" fill="#FFFFFF" text-anchor="middle" dominant-baseline="central" filter="drop-shadow(0px 2px 2px rgba(0,0,0,0.9))">{text}</text>
</svg>'''
    write_svg(filename, content)

make_chip('chip-1k.svg', '#4a0e4e', '#c2185b', '1K', '#ff80ab', '#f48fb1')
make_chip('chip-10k.svg', '#5d0000', '#d32f2f', '10K', '#ff6666', '#ef9a9a')
make_chip('chip-100k.svg', '#004d40', '#00897b', '100K', '#80cbc4', '#80cbc4')
make_chip('chip-500k.svg', '#0d47a1', '#1976d2', '500K', '#64b5f6', '#90caf9')
make_chip('chip-1m.svg', '#1b5e20', '#388e3c', '1M', '#81c784', '#a5d6a7')

# Jackpot Header Logo & Coins
jackpot_svg = '''<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 500 120" width="500" height="120">
  <defs>
    <linearGradient id="goldText" x1="0%" y1="0%" x2="0%" y2="100%">
      <stop offset="0%" stop-color="#FFFDF0"/>
      <stop offset="25%" stop-color="#FFEE55"/>
      <stop offset="50%" stop-color="#FFB300"/>
      <stop offset="75%" stop-color="#FF8F00"/>
      <stop offset="100%" stop-color="#FFE082"/>
    </linearGradient>
    <filter id="jackpotGlow" x="-20%" y="-20%" width="140%" height="140%">
      <feDropShadow dx="0" dy="2" stdDeviation="4" flood-color="#FFD700" flood-opacity="0.8"/>
      <feDropShadow dx="0" dy="4" stdDeviation="8" flood-color="#FF6F00" flood-opacity="0.9"/>
    </filter>
  </defs>

  <!-- Side Gold Coin Piles -->
  <!-- Left Coins -->
  <g transform="translate(40, 50)">
    <circle cx="0" cy="10" r="22" fill="#FFB300" stroke="#FFF" stroke-width="2"/>
    <circle cx="-15" cy="18" r="20" fill="#FFA000" stroke="#FFF" stroke-width="2"/>
    <circle cx="15" cy="18" r="20" fill="#FFC107" stroke="#FFF" stroke-width="2"/>
    <circle cx="0" cy="25" r="18" fill="#FFD54F" stroke="#FFF" stroke-width="2"/>
  </g>

  <!-- Right Coins -->
  <g transform="translate(460, 50)">
    <circle cx="0" cy="10" r="22" fill="#FFB300" stroke="#FFF" stroke-width="2"/>
    <circle cx="15" cy="18" r="20" fill="#FFA000" stroke="#FFF" stroke-width="2"/>
    <circle cx="-15" cy="18" r="20" fill="#FFC107" stroke="#FFF" stroke-width="2"/>
    <circle cx="0" cy="25" r="18" fill="#FFD54F" stroke="#FFF" stroke-width="2"/>
  </g>

  <!-- JACKPOT Main Text -->
  <text x="250" y="65" font-family="'Impact', 'Arial Black', sans-serif" font-weight="900" font-size="64" fill="url(#goldText)" text-anchor="middle" filter="url(#jackpotGlow)" stroke="#5D4037" stroke-width="3" letter-spacing="4">JACKPOT</text>
</svg>'''
write_svg('jackpot-logo.svg', jackpot_svg)

# Deep Purple Night Sky Background with fireworks & starry sky
bg_svg = '''<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 1000" width="600" height="1000">
  <defs>
    <linearGradient id="bgGrad" x1="0%" y1="0%" x2="0%" y2="100%">
      <stop offset="0%" stop-color="#0d0428"/>
      <stop offset="30%" stop-color="#1a0b40"/>
      <stop offset="60%" stop-color="#2d0b5a"/>
      <stop offset="85%" stop-color="#4a1270"/>
      <stop offset="100%" stop-color="#1d0638"/>
    </linearGradient>
    <radialGradient id="centerGlow" cx="50%" cy="45%" r="45%">
      <stop offset="0%" stop-color="#8a2be2" stop-opacity="0.4"/>
      <stop offset="50%" stop-color="#4b0082" stop-opacity="0.2"/>
      <stop offset="100%" stop-color="#000000" stop-opacity="0"/>
    </radialGradient>
  </defs>

  <rect width="600" height="1000" fill="url(#bgGrad)"/>
  <rect width="600" height="1000" fill="url(#centerGlow)"/>

  <!-- Stars -->
  <g fill="#FFFFFF" opacity="0.8">
    <circle cx="80" cy="120" r="1.5"/>
    <circle cx="150" cy="80" r="2"/>
    <circle cx="220" cy="180" r="1.2"/>
    <circle cx="380" cy="90" r="1.8"/>
    <circle cx="480" cy="140" r="2.5"/>
    <circle cx="520" cy="220" r="1.5"/>
    <circle cx="60" cy="300" r="2"/>
    <circle cx="540" cy="380" r="1.8"/>
    <circle cx="100" cy="500" r="1.2"/>
    <circle cx="500" cy="600" r="2"/>
  </g>

  <!-- Fireworks graphic bursts -->
  <g stroke="#FF4081" stroke-width="1.5" opacity="0.6" stroke-dasharray="4,4">
    <path d="M 70 150 L 110 150 M 90 130 L 90 170 M 76 136 L 104 164 M 104 136 L 76 164" />
    <path d="M 490 160 L 530 160 M 510 140 L 510 180 M 496 146 L 524 174 M 524 146 L 496 174" stroke="#FFD700" />
  </g>

  <!-- Horizon City Silhouette -->
  <path d="M 0 880 L 40 880 L 40 850 L 70 850 L 70 880 L 120 880 L 120 830 L 150 830 L 150 880 L 220 880 L 220 860 L 260 860 L 260 880 L 320 880 L 320 840 L 370 840 L 370 880 L 440 880 L 440 820 L 480 820 L 480 880 L 550 880 L 550 850 L 600 850 L 600 1000 L 0 1000 Z" fill="#120224" opacity="0.75"/>
</svg>'''
write_svg('background.svg', bg_svg)

print("All assets successfully created!")
