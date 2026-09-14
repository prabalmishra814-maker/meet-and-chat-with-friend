/* ==========================================================================
   Room Chat App - Web Admin Panel Application Logic (Firebase Realtime Database)
   ========================================================================== */

// Default Firebase Configuration (Extracted from app/google-services.json)
const defaultConfig = {
  apiKey: "AIzaSyDO8H1cGyL757e2I8bwyOjrwFN2YSfqq40",
  authDomain: "meet-and-chat-3abdb.firebaseapp.com",
  databaseURL: "https://meet-and-chat-3abdb-default-rtdb.firebaseio.com",
  projectId: "meet-and-chat-3abdb",
  storageBucket: "meet-and-chat-3abdb.firebasestorage.app",
  messagingSenderId: "686945289016",
  appId: "1:686945289016:android:604d09ab3ef828451ef7cd"
};

let firebaseConfig = JSON.parse(localStorage.getItem('roomchat_admin_config')) || defaultConfig;
let db = null;

// Cache Data
let usersData = {};
let roomsData = {};
let postsData = {};
let storeData = {};
let transactionsData = [];
let rechargesData = {};

// Initialize Admin App
document.addEventListener("DOMContentLoaded", () => {
  initFirebase();
  setupNavigation();
  setupSearchFilters();
  setupFormHandlers();
});

// Initialize Firebase SDK
function initFirebase() {
  try {
    if (!firebase.apps.length) {
      firebase.initializeApp(firebaseConfig);
    }
    db = firebase.database();

    // Test Database Connection
    db.ref(".info/connected").on("value", (snap) => {
      const dot = document.getElementById("statusDot");
      const text = document.getElementById("statusText");
      if (snap.val() === true) {
        dot.className = "status-dot online";
        text.innerText = "Firebase Live Connected";
        showToast("Connected to Firebase Realtime Database", "success");
      } else {
        dot.className = "status-dot";
        text.innerText = "Disconnected";
      }
    });

    // Attach Database Realtime Listeners
    listenToUsers();
    listenToRooms();
    listenToPosts();
    listenToStore();
    listenToTransactions();
    listenToRecharges();

  } catch (error) {
    console.error("Firebase Init Error:", error);
    showToast("Firebase Config Error: " + error.message, "danger");
  }
}

// Navigation Tab Switcher
function setupNavigation() {
  document.querySelectorAll(".nav-item").forEach(item => {
    item.addEventListener("click", () => {
      const target = item.getAttribute("data-target");
      switchTab(target);
    });
  });
}

function switchTab(tabId) {
  document.querySelectorAll(".nav-item").forEach(i => i.classList.remove("active"));
  document.querySelectorAll(".tab-pane").forEach(p => p.classList.remove("active"));

  const navItem = document.querySelector(`.nav-item[data-target="${tabId}"]`);
  const tabPane = document.getElementById(tabId);

  if (navItem) navItem.classList.add("active");
  if (tabPane) tabPane.classList.add("active");

  const titles = {
    dashboard: "Dashboard Overview",
    recharges: "User Payment Verification & Recharge Requests",
    users: "User Accounts Management",
    rooms: "Audio Chat Rooms Control",
    posts: "Community Feed & Posts",
    store: "Store Catalog & Items",
    notifications: "System Notification Broadcast",
    transactions: "Global Transactions Log"
  };

  document.getElementById("activePageTitle").innerText = titles[tabId] || "Admin Control";
}

// Realtime Listener: Users
function listenToUsers() {
  db.ref("users").on("value", (snapshot) => {
    usersData = snapshot.val() || {};
    renderUsers();
    updateDashboardStats();
  });
}

// Realtime Listener: Rooms
function listenToRooms() {
  db.ref("rooms").on("value", (snapshot) => {
    roomsData = snapshot.val() || {};
    renderRooms();
    updateDashboardStats();
  });
}

// Realtime Listener: Posts
function listenToPosts() {
  db.ref("Posts").on("value", (snapshot) => {
    postsData = snapshot.val() || {};
    renderPosts();
    updateDashboardStats();
  });
}

// Realtime Listener: Store Items
function listenToStore() {
  db.ref("store_items").on("value", (snapshot) => {
    storeData = snapshot.val() || {};
    renderStore();
  });
}

// Realtime Listener: Wallet Transactions
function listenToTransactions() {
  db.ref("wallet_transactions").on("value", (snapshot) => {
    const raw = snapshot.val() || {};
    transactionsData = [];

    Object.keys(raw).forEach(uid => {
      const userTxs = raw[uid] || {};
      Object.keys(userTxs).forEach(txId => {
        transactionsData.push({
          uid: uid,
          txId: txId,
          ...userTxs[txId]
        });
      });
    });

    // Sort by timestamp descending
    transactionsData.sort((a, b) => (b.timestamp || 0) - (a.timestamp || 0));
    renderTransactions();
  });
}

// Realtime Listener: Recharge Requests
function listenToRecharges() {
  db.ref("recharge_requests").on("value", (snapshot) => {
    rechargesData = snapshot.val() || {};
    renderRecharges();
    updateDashboardStats();
  });
}

// Update Dashboard Counters
function updateDashboardStats() {
  const userKeys = Object.keys(usersData);
  const roomKeys = Object.keys(roomsData);
  const postKeys = Object.keys(postsData);

  let totalCoins = 0;
  userKeys.forEach(uid => {
    const coins = parseInt(usersData[uid].coins) || 0;
    totalCoins += coins;
  });

  let pendingRecharges = 0;
  Object.keys(rechargesData).forEach(reqId => {
    if (rechargesData[reqId].status === "PENDING") {
      pendingRecharges++;
    }
  });

  document.getElementById("statTotalUsers").innerText = userKeys.length;
  document.getElementById("statTotalRooms").innerText = roomKeys.length;
  document.getElementById("statTotalPosts").innerText = postKeys.length;
  document.getElementById("statTotalCoins").innerText = totalCoins.toLocaleString();
  document.getElementById("statPendingRecharges").innerText = pendingRecharges;
}

// Render Recharge Requests Table (Payment Verification)
function renderRecharges() {
  const tbody = document.getElementById("rechargeTableBody");
  const query = (document.getElementById("rechargeSearch").value || "").toLowerCase();

  tbody.innerHTML = "";
  const reqIds = Object.keys(rechargesData);

  if (reqIds.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color: var(--text-muted);">No payment recharge requests submitted yet.</td></tr>`;
    return;
  }

  // Sort: PENDING requests first, then by timestamp descending
  reqIds.sort((a, b) => {
    const reqA = rechargesData[a];
    const reqB = rechargesData[b];
    if (reqA.status === "PENDING" && reqB.status !== "PENDING") return -1;
    if (reqA.status !== "PENDING" && reqB.status === "PENDING") return 1;
    return (reqB.timestamp || 0) - (reqA.timestamp || 0);
  });

  let count = 0;
  reqIds.forEach(reqId => {
    const r = rechargesData[reqId];
    const userName = r.userName || "User";
    const profileId = r.userProfileId || r.uid.substring(0, 6);
    const coins = r.coinAmount || 0;
    const price = r.priceAmount || "₹0";
    const utr = r.utrNumber || "N/A";
    const status = r.status || "PENDING";
    const dateStr = r.timestamp ? new Date(r.timestamp).toLocaleString() : "N/A";

    if (query && !userName.toLowerCase().includes(query) && !utr.toLowerCase().includes(query) && !r.uid.toLowerCase().includes(query)) {
      return;
    }

    count++;
    let statusBadge = `<span class="badge badge-gold">⏳ PENDING</span>`;
    if (status === "APPROVED") statusBadge = `<span class="badge badge-green">✅ APPROVED</span>`;
    if (status === "REJECTED") statusBadge = `<span class="badge badge-red">❌ REJECTED</span>`;

    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>
        <strong style="display:block;">${escapeHtml(userName)}</strong>
        <small style="color: var(--text-muted);">ID: ${profileId} | UID: ${r.uid.substring(0, 8)}...</small>
      </td>
      <td><strong style="color: var(--accent-gold);">🪙 ${coins} Coins</strong><br><small style="color: var(--text-muted);">${r.packageName || ''}</small></td>
      <td><strong style="color: var(--primary);">${price}</strong></td>
      <td><span class="badge badge-purple" style="font-family: monospace; font-size:12px;">${escapeHtml(utr)}</span></td>
      <td>${statusBadge}</td>
      <td><small style="color: var(--text-secondary);">${dateStr}</small></td>
      <td>
        ${status === "PENDING" ? `
          <div style="display:flex; gap:6px;">
            <button class="btn-primary" style="padding:4px 10px; font-size:11px; background: var(--accent-success);" onclick="approveRecharge('${reqId}', '${r.uid}', ${coins}, '${price}', '${escapeHtml(utr)}')">
              ✅ Approve
            </button>
            <button class="btn-danger" style="padding:4px 10px; font-size:11px;" onclick="rejectRecharge('${reqId}', '${r.uid}', '${price}', '${escapeHtml(utr)}')">
              ❌ Reject
            </button>
          </div>
        ` : `<small style="color: var(--text-muted);">Processed</small>`}
      </td>
    `;
    tbody.appendChild(tr);
  });

  if (count === 0) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color: var(--text-muted);">No matching recharge requests found.</td></tr>`;
  }
}

// Approve Recharge & Add Coins
function approveRecharge(reqId, uid, coinAmount, priceAmount, utrNumber) {
  if (!confirm(`Are you sure you want to APPROVE payment of ${priceAmount} for ${coinAmount} Coins?\nUTR: ${utrNumber}`)) {
    return;
  }

  // 1. Update user coins balance
  const userRef = db.ref("users").child(uid);
  userRef.child("coins").once("value", (snap) => {
    let currentCoins = parseInt(snap.val()) || 0;
    let newCoins = currentCoins + coinAmount;

    userRef.child("coins").setValue(newCoins).then(() => {

      // 2. Add Wallet Transaction
      const txRef = db.ref("wallet_transactions").child(uid).push();
      txRef.set({
        txId: txRef.key,
        title: "Coin Recharge Approved 🪙",
        description: `Approved UTR: ${utrNumber} (${priceAmount})`,
        coinAmount: coinAmount,
        diamondAmount: 0,
        type: "TOPUP",
        timestamp: Date.now()
      });

      // 3. Update Recharge Request Status
      db.ref("recharge_requests").child(reqId).update({
        status: "APPROVED",
        approvedTimestamp: Date.now()
      });

      // 4. Send Notification to User
      const notifRef = db.ref("notifications").child(uid).push();
      notifRef.set({
        title: "Recharge Approved! 🎉",
        message: `Your payment of ${priceAmount} for ${coinAmount} Coins (UTR: ${utrNumber}) was APPROVED! Coins have been added to your wallet.`,
        type: "RECHARGE_APPROVED",
        timestamp: Date.now()
      });

      showToast(`🎉 Approved! ${coinAmount} Coins added to user wallet.`, "success");
    });
  });
}

// Reject Recharge Request
function rejectRecharge(reqId, uid, priceAmount, utrNumber) {
  if (!confirm(`Are you sure you want to REJECT payment request for UTR: ${utrNumber}?`)) {
    return;
  }

  // 1. Update Recharge Request Status
  db.ref("recharge_requests").child(reqId).update({
    status: "REJECTED",
    rejectedTimestamp: Date.now()
  });

  // 2. Send Notification to User
  const notifRef = db.ref("notifications").child(uid).push();
  notifRef.set({
    title: "Recharge Rejected ❌",
    message: `Your recharge request for ${priceAmount} (UTR: ${utrNumber}) could not be verified. Please check UTR number or contact support.`,
    type: "RECHARGE_REJECTED",
    timestamp: Date.now()
  });

  showToast(`Recharge Request Rejected for UTR: ${utrNumber}`, "info");
}

// Render Users Table
function renderUsers() {
  const tbody = document.getElementById("userTableBody");
  const query = (document.getElementById("userSearch").value || "").toLowerCase();

  tbody.innerHTML = "";
  const uids = Object.keys(usersData);

  if (uids.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color: var(--text-muted);">No users found in database.</td></tr>`;
    return;
  }

  let count = 0;
  uids.forEach(uid => {
    const u = usersData[uid];
    const name = u.name || "User";
    const profileId = u.profileId || uid.substring(0, 6);
    const coins = u.coins || 0;
    const diamonds = u.diamonds || 0;
    const level = u.level || "1";
    const gender = u.gender || "N/A";
    const avatar = u.avtar || "https://via.placeholder.com/150";

    if (query && !name.toLowerCase().includes(query) && !profileId.toLowerCase().includes(query) && !uid.toLowerCase().includes(query)) {
      return;
    }

    count++;
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>
        <div class="avatar-cell">
          <img src="${avatar}" class="user-avatar" onerror="this.src='https://via.placeholder.com/150'">
          <div>
            <strong style="display:block;">${escapeHtml(name)}</strong>
            <small style="color: var(--text-muted);">UID: ${uid}</small>
          </div>
        </div>
      </td>
      <td><span class="badge badge-purple">ID: ${profileId}</span></td>
      <td><strong style="color: var(--accent-gold);">🪙 ${coins}</strong></td>
      <td><strong style="color: var(--primary);">💎 ${diamonds}</strong></td>
      <td><span class="badge badge-cyan">Lv.${level}</span></td>
      <td>${gender}</td>
      <td>
        <div style="display:flex; gap:6px;">
          <button class="btn-primary" style="padding:4px 8px; font-size:11px;" onclick="openTopupModal('${uid}', '${escapeHtml(name)}')">
            🪙 Coins
          </button>
          <button class="btn-primary" style="padding:4px 8px; font-size:11px; background: var(--secondary);" onclick="openEditUserModal('${uid}')">
            ✏️ Edit
          </button>
          <button class="btn-danger" onclick="deleteUser('${uid}', '${escapeHtml(name)}')">
            🗑️
          </button>
        </div>
      </td>
    `;
    tbody.appendChild(tr);
  });

  if (count === 0) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color: var(--text-muted);">No matching users found.</td></tr>`;
  }
}

// Render Audio Chat Rooms
function renderRooms() {
  const container = document.getElementById("roomsGrid");
  const query = (document.getElementById("roomSearch").value || "").toLowerCase();

  container.innerHTML = "";
  const roomIds = Object.keys(roomsData);

  if (roomIds.length === 0) {
    container.innerHTML = `<div style="grid-column: 1/-1; text-align:center; color: var(--text-muted);">No active audio chat rooms.</div>`;
    return;
  }

  let count = 0;
  roomIds.forEach(roomId => {
    const r = roomsData[roomId];
    const name = r.roomName || "Audio Room";
    const owner = r.ownerName || "Host";
    const cover = r.coverImage || "https://via.placeholder.com/300";
    const isLocked = r.isLocked === true;
    const category = r.category || "General";

    if (query && !name.toLowerCase().includes(query) && !owner.toLowerCase().includes(query) && !roomId.toLowerCase().includes(query)) {
      return;
    }

    count++;
    const card = document.createElement("div");
    card.className = "grid-card";
    card.innerHTML = `
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:10px;">
        <span class="badge badge-cyan">${category}</span>
        <span class="badge ${isLocked ? 'badge-red' : 'badge-green'}">${isLocked ? '🔒 Locked' : '🌐 Public'}</span>
      </div>
      <img src="${cover}" class="post-media" onerror="this.src='https://via.placeholder.com/300'">
      <h3 style="font-size:16px; font-weight:700; margin-bottom:4px;">${escapeHtml(name)}</h3>
      <p style="font-size:12px; color: var(--text-secondary); margin-bottom:12px;">Host: ${escapeHtml(owner)} | ID: ${roomId}</p>

      <div style="display:flex; gap:8px; margin-top:12px;">
        <button class="btn-primary" style="flex:1; padding:6px; font-size:12px;" onclick="toggleRoomLock('${roomId}', ${!isLocked})">
          ${isLocked ? '🔓 Unlock' : '🔒 Lock'}
        </button>
        <button class="btn-danger" style="padding:6px 12px; font-size:12px;" onclick="deleteRoom('${roomId}')">
          🗑️ Close Room
        </button>
      </div>
    `;
    container.appendChild(card);
  });

  if (count === 0) {
    container.innerHTML = `<div style="grid-column: 1/-1; text-align:center; color: var(--text-muted);">No matching rooms found.</div>`;
  }
}

// Render Community Posts
function renderPosts() {
  const container = document.getElementById("postsGrid");
  const query = (document.getElementById("postSearch").value || "").toLowerCase();

  container.innerHTML = "";
  const postIds = Object.keys(postsData);

  if (postIds.length === 0) {
    container.innerHTML = `<div style="grid-column: 1/-1; text-align:center; color: var(--text-muted);">No community posts found.</div>`;
    return;
  }

  let count = 0;
  postIds.forEach(postId => {
    const p = postsData[postId];
    const author = p.username || p.publisher || "Author";
    const content = p.postContent || "No content";
    const image = p.postImage || "";
    const likes = p.likeCount || 0;
    const comments = p.commentCount || 0;

    if (query && !content.toLowerCase().includes(query) && !author.toLowerCase().includes(query)) {
      return;
    }

    count++;
    const card = document.createElement("div");
    card.className = "grid-card";
    card.innerHTML = `
      <div style="display:flex; align-items:center; gap:10px; margin-bottom:10px;">
        <div class="avatar-cell">
          <strong>${escapeHtml(author)}</strong>
        </div>
      </div>
      <p style="font-size:14px; line-height:1.4; color: var(--text-primary);">${escapeHtml(content)}</p>
      ${image ? `<img src="${image}" class="post-media" onerror="this.style.display='none'">` : ''}
      <div style="display:flex; justify-content:space-between; align-items:center; margin-top:12px; font-size:12px; color: var(--text-secondary);">
        <span>❤️ ${likes} Likes | 💬 ${comments} Comments</span>
        <button class="btn-danger" onclick="deletePost('${postId}')">🗑️ Delete Post</button>
      </div>
    `;
    container.appendChild(card);
  });

  if (count === 0) {
    container.innerHTML = `<div style="grid-column: 1/-1; text-align:center; color: var(--text-muted);">No matching posts found.</div>`;
  }
}

// Render Store Catalog
function renderStore() {
  const container = document.getElementById("storeGrid");
  container.innerHTML = "";
  const itemIds = Object.keys(storeData);

  if (itemIds.length === 0) {
    container.innerHTML = `<div style="grid-column: 1/-1; text-align:center; color: var(--text-muted);">No store items in database. Click 'Add Store Item' to create one.</div>`;
    return;
  }

  itemIds.forEach(itemId => {
    const item = storeData[itemId];
    const name = item.name || "Store Item";
    const type = item.type || "FRAME";
    const priceCoins = item.priceCoins || item.price || 0;

    const card = document.createElement("div");
    card.className = "grid-card";
    card.innerHTML = `
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:10px;">
        <span class="badge badge-purple">${type}</span>
        <span class="badge badge-gold">🪙 ${priceCoins} Coins</span>
      </div>
      <h3 style="font-size:16px; font-weight:700; margin-bottom:6px;">${escapeHtml(name)}</h3>
      <p style="font-size:12px; color: var(--text-muted); margin-bottom:12px;">Resource: ${item.iconResName || 'N/A'}</p>
      <button class="btn-danger" style="width:100%;" onclick="deleteStoreItem('${itemId}')">🗑️ Remove Item</button>
    `;
    container.appendChild(card);
  });
}

// Render Wallet Transactions Log
function renderTransactions() {
  const tbody = document.getElementById("txTableBody");
  tbody.innerHTML = "";

  if (transactionsData.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; color: var(--text-muted);">No transactions logged yet.</td></tr>`;
    return;
  }

  transactionsData.slice(0, 50).forEach(tx => {
    const user = usersData[tx.uid] ? usersData[tx.uid].name : tx.uid.substring(0, 8);
    const dateStr = tx.timestamp ? new Date(tx.timestamp).toLocaleString() : 'N/A';

    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td><small style="color: var(--text-muted);">${tx.txId || 'N/A'}</small></td>
      <td><strong>${escapeHtml(user)}</strong></td>
      <td>
        <strong style="display:block;">${escapeHtml(tx.title || 'Transaction')}</strong>
        <span class="badge badge-cyan">${tx.type || 'TOPUP'}</span>
      </td>
      <td><strong style="color: var(--accent-gold);">${tx.coinAmount || 0} 🪙</strong></td>
      <td><strong style="color: var(--primary);">${tx.diamondAmount || 0} 💎</strong></td>
      <td><small style="color: var(--text-secondary);">${dateStr}</small></td>
    `;
    tbody.appendChild(tr);
  });
}

// Search Inputs Handlers
function setupSearchFilters() {
  document.getElementById("userSearch").addEventListener("input", renderUsers);
  document.getElementById("roomSearch").addEventListener("input", renderRooms);
  document.getElementById("postSearch").addEventListener("input", renderPosts);
  document.getElementById("rechargeSearch").addEventListener("input", renderRecharges);

  document.getElementById("broadcastTarget").addEventListener("change", (e) => {
    const group = document.getElementById("targetUidGroup");
    group.style.display = (e.target.value === "SPECIFIC") ? "block" : "none";
  });
}

// Setup Form Handlers
function setupFormHandlers() {
  // Topup Form
  document.getElementById("formTopup").addEventListener("submit", (e) => {
    e.preventDefault();
    const uid = document.getElementById("topupUid").value;
    const amount = parseInt(document.getElementById("topupCoins").value) || 0;
    const op = document.getElementById("topupOpType").value;

    if (!uid) return;

    const userRef = db.ref("users").child(uid);
    userRef.child("coins").once("value", (snap) => {
      let current = parseInt(snap.val()) || 0;
      let newCoins = op === "ADD" ? (current + amount) : amount;

      userRef.child("coins").setValue(newCoins).then(() => {
        // Record Wallet Transaction
        const txRef = db.ref("wallet_transactions").child(uid).push();
        txRef.set({
          txId: txRef.key,
          title: "Admin Coin Top-Up 🪙",
          description: `Admin updated coins balance by ${amount}`,
          coinAmount: amount,
          diamondAmount: 0,
          type: "TOPUP",
          timestamp: Date.now()
        });

        showToast(`Successfully updated coins to ${newCoins}`, "success");
        closeModal("modalTopup");
      });
    });
  });

  // Edit User Form
  document.getElementById("formEditUser").addEventListener("submit", (e) => {
    e.preventDefault();
    const uid = document.getElementById("editUserUid").value;
    const name = document.getElementById("editUserName").value;
    const level = document.getElementById("editUserLevel").value;
    const gender = document.getElementById("editUserGender").value;
    const bio = document.getElementById("editUserBio").value;

    if (!uid) return;

    db.ref("users").child(uid).update({
      name: name,
      level: level,
      gender: gender,
      bio: bio
    }).then(() => {
      showToast("User details updated successfully", "success");
      closeModal("modalEditUser");
    });
  });

  // Add Store Item Form
  document.getElementById("btnAddStoreItem").addEventListener("click", () => openModal("modalAddStoreItem"));

  document.getElementById("formAddStoreItem").addEventListener("submit", (e) => {
    e.preventDefault();
    const name = document.getElementById("storeItemName").value;
    const type = document.getElementById("storeItemType").value;
    const price = parseInt(document.getElementById("storeItemPrice").value) || 0;
    const icon = document.getElementById("storeItemIcon").value;

    const newRef = db.ref("store_items").push();
    newRef.set({
      id: newRef.key,
      name: name,
      type: type,
      priceCoins: price,
      iconResName: icon
    }).then(() => {
      showToast("Store item added successfully!", "success");
      closeModal("modalAddStoreItem");
      document.getElementById("formAddStoreItem").reset();
    });
  });

  // Broadcast Notification Form
  document.getElementById("formBroadcast").addEventListener("submit", (e) => {
    e.preventDefault();
    const target = document.getElementById("broadcastTarget").value;
    const specificUid = document.getElementById("broadcastUid").value;
    const title = document.getElementById("broadcastTitle").value;
    const message = document.getElementById("broadcastMessage").value;

    const notifObj = {
      title: title,
      message: message,
      type: "SYSTEM_BROADCAST",
      timestamp: Date.now()
    };

    if (target === "SPECIFIC" && specificUid) {
      db.ref("notifications").child(specificUid).push().set(notifObj).then(() => {
        showToast("Notification sent to " + specificUid, "success");
        document.getElementById("formBroadcast").reset();
      });
    } else {
      // Broadcast to all active users
      const uids = Object.keys(usersData);
      let count = 0;
      uids.forEach(uid => {
        db.ref("notifications").child(uid).push().set(notifObj);
        count++;
      });
      showToast(`Broadcast sent to ${count} users!`, "success");
      document.getElementById("formBroadcast").reset();
    }
  });

  // Config Form Modal
  document.getElementById("btnOpenConfig").addEventListener("click", () => {
    document.getElementById("cfgDatabaseURL").value = firebaseConfig.databaseURL;
    document.getElementById("cfgApiKey").value = firebaseConfig.apiKey;
    document.getElementById("cfgProjectId").value = firebaseConfig.projectId;
    openModal("modalConfig");
  });

  document.getElementById("formConfig").addEventListener("submit", (e) => {
    e.preventDefault();
    firebaseConfig.databaseURL = document.getElementById("cfgDatabaseURL").value;
    firebaseConfig.apiKey = document.getElementById("cfgApiKey").value;
    firebaseConfig.projectId = document.getElementById("cfgProjectId").value;

    localStorage.setItem('roomchat_admin_config', JSON.stringify(firebaseConfig));
    showToast("Firebase Config saved! Reloading...", "success");
    setTimeout(() => location.reload(), 1000);
  });

  // Refresh Button
  document.getElementById("btnRefresh").addEventListener("click", () => {
    showToast("Refreshing Realtime Data...", "info");
    initFirebase();
  });
}

// User Actions
function openTopupModal(uid, name) {
  document.getElementById("topupUid").value = uid;
  document.getElementById("topupUserName").value = name;
  document.getElementById("topupCoins").value = "";
  openModal("modalTopup");
}

function openEditUserModal(uid) {
  const u = usersData[uid];
  if (!u) return;

  document.getElementById("editUserUid").value = uid;
  document.getElementById("editUserName").value = u.name || "";
  document.getElementById("editUserLevel").value = u.level || "1";
  document.getElementById("editUserGender").value = u.gender || "Male";
  document.getElementById("editUserBio").value = u.bio || "";

  openModal("modalEditUser");
}

function deleteUser(uid, name) {
  if (confirm(`Are you sure you want to delete user '${name}' (${uid}) from Firebase?`)) {
    db.ref("users").child(uid).remove().then(() => {
      showToast(`User '${name}' deleted!`, "success");
    });
  }
}

// Room Actions
function toggleRoomLock(roomId, shouldLock) {
  db.ref("rooms").child(roomId).child("isLocked").setValue(shouldLock).then(() => {
    showToast(`Room ${roomId} ${shouldLock ? 'locked' : 'unlocked'}`, "success");
  });
}

function deleteRoom(roomId) {
  if (confirm(`Are you sure you want to close/delete chat room '${roomId}'?`)) {
    db.ref("rooms").child(roomId).remove().then(() => {
      showToast(`Room '${roomId}' closed!`, "success");
    });
  }
}

// Post Actions
function deletePost(postId) {
  if (confirm(`Are you sure you want to delete this community post?`)) {
    db.ref("Posts").child(postId).remove().then(() => {
      showToast("Post deleted successfully!", "success");
    });
  }
}

// Store Actions
function deleteStoreItem(itemId) {
  if (confirm(`Are you sure you want to remove this store item?`)) {
    db.ref("store_items").child(itemId).remove().then(() => {
      showToast("Store item removed!", "success");
    });
  }
}

// Modal Helpers
function openModal(id) {
  document.getElementById(id).classList.add("active");
}

function closeModal(id) {
  document.getElementById(id).classList.remove("active");
}

// Toast Helper
function showToast(message, type = "info") {
  const container = document.getElementById("toastContainer");
  const toast = document.createElement("div");
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `<i class="fa-solid fa-circle-info"></i> <span>${escapeHtml(message)}</span>`;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(100%)';
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}

// Utility: HTML Escaper
function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}
