# 🎲 Ludo Online Multiplayer Server

This directory contains the real-time server code for 4-Player Ludo Online Matchmaking & Game State Synchronization.

---

## ⚡ Architecture Overview

1. **Built-In Firebase Backend (Default in App)**:
   - The Android app connects directly to Firebase Realtime Database via `LudoBridge.java`.
   - Players across all chat rooms looking for a game are automatically matched under `ludo_online/rooms`.
   - Requires **ZERO** external hosting or setup!

2. **Standalone Node.js + WebSocket Server (Optional)**:
   - Located in this directory (`ludo-server/server.js`).
   - Use this if you want to host your own dedicated Node.js Socket.IO server.

---

## 🚀 How to Run the Node.js Server

```bash
cd ludo-server
npm install
npm start
```

The server will start on `http://localhost:3000`. You can deploy this server to any hosting provider (Heroku, Render, AWS, VPS, Glitch, Replit).
