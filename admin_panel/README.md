# Room Chat App - Web Admin Panel 🌐👑

A complete, feature-packed Web HTML/CSS/JS Single-Page Control Panel for managing the **Room Chat** Android application in real time using Firebase Realtime Database.

---

## 🚀 How to Run the Admin Panel

1. **Directly in Web Browser:**
   - Double-click or open `admin_panel/index.html` in Chrome, Edge, Firefox, or Safari.
   - Or open using a local live server extension / Python `python -m http.server` in the `admin_panel/` directory.

2. **Pre-configured Firebase Credentials:**
   - The panel is already pre-configured with the credentials from `app/google-services.json` (`meet-and-chat-3abdb-default-rtdb.firebaseio.com`).
   - As soon as you open `index.html`, you will see **🟢 Firebase Live Connected** at the bottom-left sidebar.

---

## ⚡ Admin Panel Features & Controls

### 1. 📊 Dashboard Analytics
- Live counters for **Total Users**, **Active Audio Rooms**, **Community Feed Posts**, and **Total Circulating Coins**.
- Quick action buttons to jump straight to management tasks.

### 2. 👥 Users Management (`users` node)
- **Live Search**: Search users instantly by Name, Profile ID, or UID.
- **🪙 Top-Up Coins**: Add or set coins for any user with automatic transaction logging.
- **✏️ Edit Profile**: Modify User Name, Level, Gender, and Bio.
- **🗑️ Delete User**: Remove bad/banned user accounts from database.

### 3. 🎙️ Audio Chat Rooms (`rooms` node)
- View active live chat rooms with cover image, room name, category, host name.
- **🔒 Lock / Unlock Room**: Instantly change room privacy.
- **🗑️ Close Room**: Terminate room from Realtime Database.

### 4. 📝 Posts & Community Feed (`Posts` node)
- View community posts, images, likes, and comment counts.
- **🗑️ Delete Post**: Instantly delete inappropriate posts.

### 5. 🛒 Store Catalog (`store_items` node)
- View avatar frames, room themes, and VIP items.
- **➕ Add Store Item**: Create new store items with name, price in coins, and icon resource name.
- **🗑️ Remove Item**: Delete store items.

### 6. 📢 Push Notification Broadcast (`notifications` node)
- Send system announcements / rewards to **ALL users** or **Specific User UID**.
- Realtime push into user's notification list in the Android app.

### 7. 💸 Transactions Log (`wallet_transactions` node)
- Audit log of global coin recharges, gift sent/received, and rewards.

---

## 📁 File Structure

```
admin_panel/
├── index.html     # HTML Layout with Glassmorphism Dashboard
├── style.css      # Dark Theme Responsive Stylesheet
├── admin.js       # Realtime Firebase Data Listeners & Control Handlers
└── README.md      # Documentation & Guide
```
