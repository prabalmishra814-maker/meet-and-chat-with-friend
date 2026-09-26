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

// Previous state keys for live diff notifications
let prevUsersKeys = null;
let prevRoomsKeys = null;
let prevPostsKeys = null;
let prevRechargesKeys = null;

// Initialize Admin App
document.addEventListener("DOMContentLoaded", () => {
  checkAuthSession();
  setupAuthHandlers();
  initFirebase();
  setupNavigation();
  setupSearchFilters();
  setupFormHandlers();
});

// Check & Manage Admin Auth Session
function checkAuthSession() {
  const isLoggedIn = localStorage.getItem("roomchat_admin_logged_in") === "true" ||
                     sessionStorage.getItem("roomchat_admin_logged_in") === "true";

  const loginOverlay = document.getElementById("loginOverlay");
  if (isLoggedIn) {
    if (loginOverlay) loginOverlay.classList.remove("active");
  } else {
    if (loginOverlay) loginOverlay.classList.add("active");
  }
}

// Setup Login & Logout Handlers
function setupAuthHandlers() {
  const formLogin = document.getElementById("formAdminLogin");
  if (formLogin) {
    formLogin.addEventListener("submit", (e) => {
      e.preventDefault();
      const email = document.getElementById("loginEmail").value.trim();
      const password = document.getElementById("loginPassword").value;
      const remember = document.getElementById("loginRemember").checked;

      showToast("Authenticating with Firebase Auth...", "info");

      if (firebase.auth) {
        // Attempt Real Firebase Email/Password Authentication First
        firebase.auth().signInWithEmailAndPassword(email, password).then((userCred) => {
          performLoginSuccess(remember);
          showToast(`Welcome ${userCred.user.email || 'Admin'}! Authenticated successfully. 🎉`, "success");
        }).catch((authErr) => {
          console.warn("Firebase Email/Password Auth failed:", authErr.message);

          // Check if default fallback credentials match
          const DEFAULT_EMAIL = "admin@roomchat.com";
          const DEFAULT_PASS = "admin123456";

          if ((email.toLowerCase() === DEFAULT_EMAIL.toLowerCase() && password === DEFAULT_PASS) ||
              (email.toLowerCase() === "admin" && password === DEFAULT_PASS)) {

            // If default credentials matched, attempt anonymous sign in for rules compliance
            if (!firebase.auth().currentUser) {
              firebase.auth().signInAnonymously().then(() => {
                performLoginSuccess(remember);
                showToast("Welcome Admin! Logged in successfully. 🎉", "success");
              }).catch(() => {
                performLoginSuccess(remember);
                showToast("Welcome Admin! (Notice: Create admin@roomchat.com in Firebase Console -> Auth -> Users)", "warning");
              });
            } else {
              performLoginSuccess(remember);
              showToast("Welcome Admin! Logged in successfully. 🎉", "success");
            }

          } else {
            showToast("Login Failed: " + authErr.message, "danger");
          }
        });
      } else {
        // Fallback without Firebase Auth SDK
        performLoginSuccess(remember);
        showToast("Welcome Admin!", "success");
      }
    });
  }

  // Toggle Password Eye Icon
  const btnTogglePass = document.getElementById("btnTogglePassword");
  if (btnTogglePass) {
    btnTogglePass.addEventListener("click", () => {
      const passInput = document.getElementById("loginPassword");
      const iconEye = document.getElementById("iconEye");
      if (passInput) {
        if (passInput.type === "password") {
          passInput.type = "text";
          if (iconEye) iconEye.className = "fa-solid fa-eye-slash";
        } else {
          passInput.type = "password";
          if (iconEye) iconEye.className = "fa-solid fa-eye";
        }
      }
    });
  }

  // Logout Handlers
  const btnLogout = document.getElementById("btnLogout");
  if (btnLogout) btnLogout.addEventListener("click", performLogout);

  const btnSidebarLogout = document.getElementById("btnSidebarLogout");
  if (btnSidebarLogout) btnSidebarLogout.addEventListener("click", performLogout);
}

function performLoginSuccess(remember) {
  if (remember) {
    localStorage.setItem("roomchat_admin_logged_in", "true");
  } else {
    sessionStorage.setItem("roomchat_admin_logged_in", "true");
  }
  const loginOverlay = document.getElementById("loginOverlay");
  if (loginOverlay) loginOverlay.classList.remove("active");
}

function performLogout() {
  if (confirm("Are you sure you want to log out of Admin Control Panel?")) {
    localStorage.removeItem("roomchat_admin_logged_in");
    sessionStorage.removeItem("roomchat_admin_logged_in");
    if (firebase.auth) {
      firebase.auth().signOut().catch(() => {});
    }
    const loginOverlay = document.getElementById("loginOverlay");
    if (loginOverlay) loginOverlay.classList.add("active");
    showToast("Logged out of Admin Session", "info");
  }
}

// Initialize Firebase SDK
function initFirebase() {
  try {
    if (!firebase.apps.length) {
      firebase.initializeApp(firebaseConfig);
    } else {
      firebase.app();
    }
    db = firebase.database();

    // Enable Local Session Persistence & Auth Listener
    if (firebase.auth) {
      firebase.auth().setPersistence(firebase.auth.Auth.Persistence.LOCAL).catch(() => {});

      firebase.auth().onAuthStateChanged((user) => {
        if (user) {
          console.log("Firebase Auth: Logged in as", user.email || user.uid);
        } else {
          console.log("Firebase Auth: Signed out, attempting anonymous fallback...");
          firebase.auth().signInAnonymously().catch(() => {});
        }
      });
    }

    // Test Database Connection
    db.ref(".info/connected").on("value", (snap) => {
      const dot = document.getElementById("statusDot");
      const text = document.getElementById("statusText");
      if (snap.val() === true) {
        if (dot) dot.className = "status-dot online";
        if (text) text.innerText = "Firebase Live Connected";
        showToast("Connected to Firebase Realtime Database", "success");
      } else {
        if (dot) dot.className = "status-dot";
        if (text) text.innerText = "Connecting / Disconnected";
      }
    });

    // Attach Database Realtime Listeners with error handlers
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

  const titleEl = document.getElementById("activePageTitle");
  if (titleEl) titleEl.innerText = titles[tabId] || "Admin Control";
}

// Realtime Listener: Users (Detects new user registrations live)
function listenToUsers() {
  db.ref("users").on("value", (snapshot) => {
    const newData = snapshot.val() || {};
    const newKeys = Object.keys(newData);

    if (prevUsersKeys !== null) {
      const addedKeys = newKeys.filter(k => !prevUsersKeys.includes(k));
      addedKeys.forEach(k => {
        const u = newData[k];
        if (u) {
          showToast(`👥 New User Registered: ${escapeHtml(u.name || u.userName || 'User')}!`, "info");
        }
      });
    }

    prevUsersKeys = newKeys;
    usersData = newData;

    renderUsers();
    renderRooms();
    renderPosts();
    updateDashboardStats();
  }, (err) => {
    console.error("Error loading users node:", err);
    showToast("Firebase Read Error (users): " + err.message, "danger");
  });
}

// Realtime Listener: Rooms (Detects new rooms created by users live)
function listenToRooms() {
  db.ref("rooms").on("value", (snapshot) => {
    const newData = snapshot.val() || {};
    const newKeys = Object.keys(newData);

    if (prevRoomsKeys !== null) {
      const addedKeys = newKeys.filter(k => !prevRoomsKeys.includes(k));
      addedKeys.forEach(k => {
        const r = newData[k];
        if (r) {
          showToast(`🎙️ New Audio Room Created: ${escapeHtml(r.room_name || r.roomName || 'Room')}!`, "info");
        }
      });
    }

    prevRoomsKeys = newKeys;
    roomsData = newData;

    renderRooms();
    updateDashboardStats();
  }, (err) => {
    console.error("Error loading rooms node:", err);
    showToast("Firebase Read Error (rooms): " + err.message, "danger");
  });
}

// Realtime Listener: Posts (Detects new posts live)
function listenToPosts() {
  db.ref("Posts").on("value", (snapshot) => {
    const newData = snapshot.val() || {};
    const newKeys = Object.keys(newData);

    if (prevPostsKeys !== null) {
      const addedKeys = newKeys.filter(k => !prevPostsKeys.includes(k));
      addedKeys.forEach(k => {
        const p = newData[k];
        if (p) {
          showToast(`📝 New Post Shared in Feed!`, "info");
        }
      });
    }

    prevPostsKeys = newKeys;
    postsData = newData;

    renderPosts();
    updateDashboardStats();
  }, (err) => {
    console.error("Error loading Posts node:", err);
    showToast("Firebase Read Error (Posts): " + err.message, "danger");
  });
}

// Realtime Listener: Store Items
function listenToStore() {
  db.ref("store_items").on("value", (snapshot) => {
    storeData = snapshot.val() || {};
    renderStore();
  }, (err) => {
    console.error("Error loading store_items node:", err);
    showToast("Firebase Read Error (store_items): " + err.message, "danger");
  });
}

// Realtime Listener: Wallet Transactions
function listenToTransactions() {
  db.ref("wallet_transactions").on("value", (snapshot) => {
    const raw = snapshot.val() || {};
    transactionsData = [];

    Object.keys(raw).forEach(uid => {
      const userTxs = raw[uid] || {};
      if (typeof userTxs === 'object') {
        Object.keys(userTxs).forEach(txId => {
          transactionsData.push({
            uid: uid,
            txId: txId,
            ...(typeof userTxs[txId] === 'object' ? userTxs[txId] : {})
          });
        });
      }
    });

    // Sort by timestamp descending
    transactionsData.sort((a, b) => (b.timestamp || 0) - (a.timestamp || 0));
    renderTransactions();
  }, (err) => {
    console.error("Error loading wallet_transactions node:", err);
    showToast("Firebase Read Error (wallet_transactions): " + err.message, "danger");
  });
}

// Realtime Listener: Recharge Requests (Detects new payment requests from app live)
function listenToRecharges() {
  db.ref("recharge_requests").on("value", (snapshot) => {
    const newData = snapshot.val() || {};
    const newKeys = Object.keys(newData);

    if (prevRechargesKeys !== null) {
      const addedKeys = newKeys.filter(k => !prevRechargesKeys.includes(k));
      addedKeys.forEach(k => {
        const r = newData[k];
        if (r && r.status === "PENDING") {
          showToast(`💳 NEW RECHARGE REQUEST: ${escapeHtml(r.userName || 'User')} submitted ${escapeHtml(r.priceAmount || 'payment')} (UTR: ${escapeHtml(r.utrNumber || 'N/A')})!`, "warning");
        }
      });
    }

    prevRechargesKeys = newKeys;
    rechargesData = newData;

    renderRecharges();
    updateDashboardStats();
  }, (err) => {
    console.error("Error loading recharge_requests node:", err);
    showToast("Firebase Read Error (recharge_requests): " + err.message, "danger");
  });
}

// Update Dashboard Counters
function updateDashboardStats() {
  const userKeys = Object.keys(usersData || {});
  const roomKeys = Object.keys(roomsData || {});
  const postKeys = Object.keys(postsData || {});

  let totalCoins = 0;
  userKeys.forEach(uid => {
    const u = usersData[uid];
    if (u && u.coins) {
      totalCoins += parseInt(u.coins, 10) || 0;
    }
  });

  let pendingRecharges = 0;
  Object.keys(rechargesData || {}).forEach(reqId => {
    const r = rechargesData[reqId];
    if (r && r.status === "PENDING") {
      pendingRecharges++;
    }
  });

  const elUsers = document.getElementById("statTotalUsers");
  const elRooms = document.getElementById("statTotalRooms");
  const elPosts = document.getElementById("statTotalPosts");
  const elCoins = document.getElementById("statTotalCoins");
  const elPending = document.getElementById("statPendingRecharges");

  if (elUsers) elUsers.innerText = userKeys.length;
  if (elRooms) elRooms.innerText = roomKeys.length;
  if (elPosts) elPosts.innerText = postKeys.length;
  if (elCoins) elCoins.innerText = totalCoins.toLocaleString();
  if (elPending) elPending.innerText = pendingRecharges;
}

// Render Recharge Requests Table (Payment Verification)
function renderRecharges() {
  const tbody = document.getElementById("rechargeTableBody");
  if (!tbody) return;

  const query = (document.getElementById("rechargeSearch")?.value || "").toLowerCase().trim();
  tbody.innerHTML = "";

  const reqIds = Object.keys(rechargesData || {});

  if (reqIds.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color: var(--text-muted);">No payment recharge requests submitted yet.</td></tr>`;
    return;
  }

  // Sort: PENDING requests first, then by timestamp descending
  reqIds.sort((a, b) => {
    const reqA = rechargesData[a] || {};
    const reqB = rechargesData[b] || {};
    if (reqA.status === "PENDING" && reqB.status !== "PENDING") return -1;
    if (reqA.status !== "PENDING" && reqB.status === "PENDING") return 1;
    return (reqB.timestamp || 0) - (reqA.timestamp || 0);
  });

  let count = 0;
  reqIds.forEach(reqId => {
    const r = rechargesData[reqId];
    if (!r) return;

    const uid = r.uid || "";
    const userName = r.userName || (usersData[uid] ? usersData[uid].name : "User");
    const profileId = r.userProfileId || (usersData[uid] ? usersData[uid].profileId : (uid ? uid.substring(0, 6) : "N/A"));
    const coins = parseInt(r.coinAmount, 10) || 0;
    const price = r.priceAmount || "₹0";
    const utr = r.utrNumber || "N/A";
    const status = r.status || "PENDING";
    const dateStr = r.timestamp ? new Date(r.timestamp).toLocaleString() : "N/A";

    if (query && !userName.toLowerCase().includes(query) && !utr.toLowerCase().includes(query) && !uid.toLowerCase().includes(query)) {
      return;
    }

    count++;
    let statusBadge = `<span class="badge badge-gold">⏳ PENDING</span>`;
    if (status === "APPROVED") statusBadge = `<span class="badge badge-green">✅ APPROVED</span>`;
    if (status === "REJECTED") statusBadge = `<span class="badge badge-red">❌ REJECTED</span>`;

    const tr = document.createElement("tr");
    tr.className = status === "PENDING" ? "highlight-row" : "";
    tr.innerHTML = `
      <td>
        <strong style="display:block;">${escapeHtml(userName)}</strong>
        <small style="color: var(--text-muted);">ID: ${escapeHtml(String(profileId))} | UID: ${escapeHtml(uid.substring(0, 8))}...</small>
      </td>
      <td><strong style="color: var(--accent-gold);">🪙 ${coins} Coins</strong><br><small style="color: var(--text-muted);">${escapeHtml(r.packageName || '')}</small></td>
      <td><strong style="color: var(--primary);">${escapeHtml(price)}</strong></td>
      <td><span class="badge badge-purple" style="font-family: monospace; font-size:12px;">${escapeHtml(utr)}</span></td>
      <td>${statusBadge}</td>
      <td><small style="color: var(--text-secondary);">${escapeHtml(dateStr)}</small></td>
      <td>
        ${status === "PENDING" ? `
          <div style="display:flex; gap:6px;">
            <button class="btn-primary btn-approve-recharge" style="padding:4px 10px; font-size:11px; background: var(--accent-success);"
              data-reqid="${escapeHtml(reqId)}"
              data-uid="${escapeHtml(uid)}"
              data-coins="${coins}"
              data-price="${escapeHtml(price)}"
              data-utr="${escapeHtml(utr)}">
              ✅ Approve
            </button>
            <button class="btn-danger btn-reject-recharge" style="padding:4px 10px; font-size:11px;"
              data-reqid="${escapeHtml(reqId)}"
              data-uid="${escapeHtml(uid)}"
              data-price="${escapeHtml(price)}"
              data-utr="${escapeHtml(utr)}">
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
    return;
  }

  // Event Listeners via Data Attributes
  tbody.querySelectorAll(".btn-approve-recharge").forEach(btn => {
    btn.addEventListener("click", () => {
      const reqId = btn.getAttribute("data-reqid");
      const uid = btn.getAttribute("data-uid");
      const coins = parseInt(btn.getAttribute("data-coins"), 10) || 0;
      const price = btn.getAttribute("data-price") || "₹0";
      const utr = btn.getAttribute("data-utr") || "N/A";
      approveRecharge(reqId, uid, coins, price, utr);
    });
  });

  tbody.querySelectorAll(".btn-reject-recharge").forEach(btn => {
    btn.addEventListener("click", () => {
      const reqId = btn.getAttribute("data-reqid");
      const uid = btn.getAttribute("data-uid");
      const price = btn.getAttribute("data-price") || "₹0";
      const utr = btn.getAttribute("data-utr") || "N/A";
      rejectRecharge(reqId, uid, price, utr);
    });
  });
}

// Approve Recharge & Add Coins
function approveRecharge(reqId, uid, coinAmount, priceAmount, utrNumber) {
  if (!db) {
    showToast("Firebase database connection not ready", "danger");
    return;
  }

  if (!reqId || !uid) {
    showToast("Invalid request or user ID", "danger");
    return;
  }

  const numCoins = parseInt(coinAmount, 10) || 0;

  if (!confirm(`Are you sure you want to APPROVE payment of ${priceAmount} for ${numCoins} Coins?\nUTR: ${utrNumber}`)) {
    return;
  }

  // ⚡ Optimistic Instant UI Update (0ms delay)
  if (rechargesData[reqId]) {
    rechargesData[reqId].status = "APPROVED";
  }
  if (usersData[uid]) {
    usersData[uid].coins = (parseInt(usersData[uid].coins, 10) || 0) + numCoins;
  }
  renderRecharges();
  renderUsers();
  updateDashboardStats();

  showToast("Processing approval...", "info");

  // 1. Fetch current user coins balance from Firebase
  const userRef = db.ref("users").child(uid);
  userRef.child("coins").once("value").then((snap) => {
    let currentCoins = parseInt(snap.val(), 10) || 0;
    let newCoins = currentCoins + numCoins;

    // 2. Set new user coins using .set()
    return userRef.child("coins").set(newCoins);
  }).then(() => {
    // 3. Add Wallet Transaction
    const txRef = db.ref("wallet_transactions").child(uid).push();
    return txRef.set({
      txId: txRef.key,
      title: "Coin Recharge Approved 🪙",
      description: `Approved UTR: ${utrNumber} (${priceAmount})`,
      coinAmount: numCoins,
      diamondAmount: 0,
      type: "TOPUP",
      timestamp: Date.now()
    });
  }).then(() => {
    // 4. Update Recharge Request Status & User Claimed Offers
    const reqInfo = rechargesData[reqId] || {};
    if (reqInfo.packageKey || reqInfo.packageName) {
      const pKey = reqInfo.packageKey || (reqInfo.packageName || '').replace(/[^a-zA-Z0-9_]/g, '_');
      if (pKey) {
        db.ref("users").child(uid).child("claimed_offers").child(pKey).set(true);
      }
    }
    return db.ref("recharge_requests").child(reqId).update({
      status: "APPROVED",
      approvedTimestamp: Date.now()
    });
  }).then(() => {
    // 5. Send Notification to User
    const notifRef = db.ref("notifications").child(uid).push();
    return notifRef.set({
      title: "Recharge Approved! 🎉",
      message: `Your payment of ${priceAmount} for ${numCoins} Coins (UTR: ${utrNumber}) was APPROVED! Coins have been added to your wallet.`,
      type: "RECHARGE_APPROVED",
      timestamp: Date.now()
    });
  }).then(() => {
    showToast(`🎉 Approved! ${numCoins} Coins added to user wallet.`, "success");
  }).catch((err) => {
    console.error("Approve recharge error:", err);
    showToast("Failed to approve recharge: " + err.message, "danger");
  });
}

// Reject Recharge Request
function rejectRecharge(reqId, uid, priceAmount, utrNumber) {
  if (!db) {
    showToast("Firebase database connection not ready", "danger");
    return;
  }

  if (!reqId) {
    showToast("Invalid request ID", "danger");
    return;
  }

  if (!confirm(`Are you sure you want to REJECT payment request for UTR: ${utrNumber}?`)) {
    return;
  }

  // ⚡ Optimistic Instant UI Update
  if (rechargesData[reqId]) {
    rechargesData[reqId].status = "REJECTED";
  }
  renderRecharges();
  updateDashboardStats();

  showToast("Processing rejection...", "info");

  // 1. Update Recharge Request Status
  db.ref("recharge_requests").child(reqId).update({
    status: "REJECTED",
    rejectedTimestamp: Date.now()
  }).then(() => {
    // 2. Send Notification to User if UID present
    if (uid) {
      const notifRef = db.ref("notifications").child(uid).push();
      return notifRef.set({
        title: "Recharge Rejected ❌",
        message: `Your recharge request for ${priceAmount} (UTR: ${utrNumber}) could not be verified. Please check UTR number or contact support.`,
        type: "RECHARGE_REJECTED",
        timestamp: Date.now()
      });
    }
  }).then(() => {
    showToast(`Recharge Request Rejected for UTR: ${utrNumber}`, "info");
  }).catch((err) => {
    console.error("Reject recharge error:", err);
    showToast("Failed to reject recharge: " + err.message, "danger");
  });
}

// Render Users Table
function renderUsers() {
  const tbody = document.getElementById("userTableBody");
  if (!tbody) return;

  const query = (document.getElementById("userSearch")?.value || "").toLowerCase().trim();
  tbody.innerHTML = "";

  const uids = Object.keys(usersData || {});

  if (uids.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color: var(--text-muted);">No users found in database.</td></tr>`;
    return;
  }

  let count = 0;
  uids.forEach(uid => {
    const u = usersData[uid];
    if (!u) return;

    const name = u.name || u.userName || "User";
    const profileId = u.profileId || uid.substring(0, 6);
    const coins = u.coins || 0;
    const diamonds = u.diamonds || 0;
    const level = u.level || "1";
    const gender = u.gender || "N/A";
    const avatar = u.avtar || u.avatar || u.userIcon || "https://via.placeholder.com/150";

    if (query && !name.toLowerCase().includes(query) && !String(profileId).toLowerCase().includes(query) && !uid.toLowerCase().includes(query)) {
      return;
    }

    count++;
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>
        <div class="avatar-cell">
          <img src="${escapeHtml(avatar)}" class="user-avatar" onerror="this.src='https://via.placeholder.com/150'">
          <div>
            <strong style="display:block;">${escapeHtml(name)}</strong>
            <small style="color: var(--text-muted);">UID: ${escapeHtml(uid)}</small>
          </div>
        </div>
      </td>
      <td><span class="badge badge-purple">ID: ${escapeHtml(String(profileId))}</span></td>
      <td><strong style="color: var(--accent-gold);">🪙 ${coins}</strong></td>
      <td><strong style="color: var(--primary);">💎 ${diamonds}</strong></td>
      <td><span class="badge badge-cyan">Lv.${escapeHtml(String(level))}</span></td>
      <td>${escapeHtml(gender)}</td>
      <td>
        <div style="display:flex; gap:6px;">
          <button class="btn-primary btn-user-coins" style="padding:4px 8px; font-size:11px;" data-uid="${escapeHtml(uid)}" data-name="${escapeHtml(name)}">
            🪙 Coins
          </button>
          <button class="btn-primary btn-user-edit" style="padding:4px 8px; font-size:11px; background: var(--secondary);" data-uid="${escapeHtml(uid)}">
            ✏️ Edit
          </button>
          <button class="btn-danger btn-user-delete" data-uid="${escapeHtml(uid)}" data-name="${escapeHtml(name)}">
            🗑️
          </button>
        </div>
      </td>
    `;
    tbody.appendChild(tr);
  });

  if (count === 0) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color: var(--text-muted);">No matching users found.</td></tr>`;
    return;
  }

  // Event Listeners for User actions
  tbody.querySelectorAll(".btn-user-coins").forEach(btn => {
    btn.addEventListener("click", () => openTopupModal(btn.getAttribute("data-uid"), btn.getAttribute("data-name")));
  });

  tbody.querySelectorAll(".btn-user-edit").forEach(btn => {
    btn.addEventListener("click", () => openEditUserModal(btn.getAttribute("data-uid")));
  });

  tbody.querySelectorAll(".btn-user-delete").forEach(btn => {
    btn.addEventListener("click", () => deleteUser(btn.getAttribute("data-uid"), btn.getAttribute("data-name")));
  });
}

// Render Audio Chat Rooms
function renderRooms() {
  const container = document.getElementById("roomsGrid");
  if (!container) return;

  const query = (document.getElementById("roomSearch")?.value || "").toLowerCase().trim();
  container.innerHTML = "";

  const roomIds = Object.keys(roomsData || {});

  if (roomIds.length === 0) {
    container.innerHTML = `<div style="grid-column: 1/-1; text-align:center; color: var(--text-muted);">No active audio chat rooms.</div>`;
    return;
  }

  let count = 0;
  roomIds.forEach(roomId => {
    const r = roomsData[roomId];
    if (!r) return;

    const name = r.room_name || r.roomName || "Audio Room";
    const hostUid = r.uid || "";
    const ownerName = (usersData[hostUid] ? usersData[hostUid].name : null) || r.ownerName || r.username || (hostUid ? hostUid.substring(0, 8) : "Host");
    const cover = r.img || r.coverImage || "https://via.placeholder.com/300";
    const isLocked = r.isLocked === true;
    const category = r.category || "General";

    if (query && !name.toLowerCase().includes(query) && !ownerName.toLowerCase().includes(query) && !roomId.toLowerCase().includes(query)) {
      return;
    }

    count++;
    const card = document.createElement("div");
    card.className = "grid-card";
    card.innerHTML = `
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:10px;">
        <span class="badge badge-cyan">${escapeHtml(category)}</span>
        <span class="badge ${isLocked ? 'badge-red' : 'badge-green'}">${isLocked ? '🔒 Locked' : '🌐 Public'}</span>
      </div>
      <img src="${escapeHtml(cover)}" class="post-media" onerror="this.src='https://via.placeholder.com/300'">
      <h3 style="font-size:16px; font-weight:700; margin-bottom:4px;">${escapeHtml(name)}</h3>
      <p style="font-size:12px; color: var(--text-secondary); margin-bottom:12px;">Host: ${escapeHtml(ownerName)} | ID: ${escapeHtml(roomId)}</p>

      <div style="display:flex; gap:8px; margin-top:12px;">
        <button class="btn-primary btn-room-lock" style="flex:1; padding:6px; font-size:12px;" data-roomid="${escapeHtml(roomId)}" data-locked="${!isLocked}">
          ${isLocked ? '🔓 Unlock' : '🔒 Lock'}
        </button>
        <button class="btn-danger btn-room-delete" style="padding:6px 12px; font-size:12px;" data-roomid="${escapeHtml(roomId)}">
          🗑️ Close Room
        </button>
      </div>
    `;
    container.appendChild(card);
  });

  if (count === 0) {
    container.innerHTML = `<div style="grid-column: 1/-1; text-align:center; color: var(--text-muted);">No matching rooms found.</div>`;
    return;
  }

  container.querySelectorAll(".btn-room-lock").forEach(btn => {
    btn.addEventListener("click", () => toggleRoomLock(btn.getAttribute("data-roomid"), btn.getAttribute("data-locked") === "true"));
  });

  container.querySelectorAll(".btn-room-delete").forEach(btn => {
    btn.addEventListener("click", () => deleteRoom(btn.getAttribute("data-roomid")));
  });
}

// Render Community Posts
function renderPosts() {
  const container = document.getElementById("postsGrid");
  if (!container) return;

  const query = (document.getElementById("postSearch")?.value || "").toLowerCase().trim();
  container.innerHTML = "";

  const postIds = Object.keys(postsData || {});

  if (postIds.length === 0) {
    container.innerHTML = `<div style="grid-column: 1/-1; text-align:center; color: var(--text-muted);">No community posts found.</div>`;
    return;
  }

  let count = 0;
  postIds.forEach(postId => {
    const p = postsData[postId];
    if (!p) return;

    const authorUid = p.uid || "";
    const author = (usersData[authorUid] ? usersData[authorUid].name : null) || p.username || p.publisher || (authorUid ? authorUid.substring(0, 8) : "Author");
    const content = p.title || p.postContent || p.content || "No content";
    const image = p.poster || p.postImage || p.image || "";
    const likes = p.likeCount || 0;
    const comments = p.commentCount || 0;
    const dateStr = p.datetime || "";

    if (query && !content.toLowerCase().includes(query) && !author.toLowerCase().includes(query)) {
      return;
    }

    count++;
    const card = document.createElement("div");
    card.className = "grid-card";
    card.innerHTML = `
      <div style="display:flex; align-items:center; justify-content:space-between; margin-bottom:10px;">
        <div class="avatar-cell">
          <strong>${escapeHtml(author)}</strong>
        </div>
        ${dateStr ? `<small style="color: var(--text-muted);">${escapeHtml(dateStr)}</small>` : ''}
      </div>
      <p style="font-size:14px; line-height:1.4; color: var(--text-primary); margin-bottom: 8px;">${escapeHtml(content)}</p>
      ${image ? `<img src="${escapeHtml(image)}" class="post-media" onerror="this.style.display='none'">` : ''}
      <div style="display:flex; justify-content:space-between; align-items:center; margin-top:12px; font-size:12px; color: var(--text-secondary);">
        <span>❤️ ${likes} Likes | 💬 ${comments} Comments</span>
        <button class="btn-danger btn-post-delete" data-postid="${escapeHtml(postId)}">🗑️ Delete Post</button>
      </div>
    `;
    container.appendChild(card);
  });

  if (count === 0) {
    container.innerHTML = `<div style="grid-column: 1/-1; text-align:center; color: var(--text-muted);">No matching posts found.</div>`;
    return;
  }

  container.querySelectorAll(".btn-post-delete").forEach(btn => {
    btn.addEventListener("click", () => deletePost(btn.getAttribute("data-postid")));
  });
}

// Render Store Catalog
function renderStore() {
  const container = document.getElementById("storeGrid");
  if (!container) return;

  container.innerHTML = "";
  const itemIds = Object.keys(storeData || {});

  if (itemIds.length === 0) {
    container.innerHTML = `<div style="grid-column: 1/-1; text-align:center; color: var(--text-muted);">No store items in database. Click 'Add Store Item' to create one.</div>`;
    return;
  }

  itemIds.forEach(itemId => {
    const item = storeData[itemId];
    if (!item) return;

    const name = item.name || "Store Item";
    const type = item.type || "FRAME";
    const priceCoins = item.priceCoins || item.price || 0;

    const card = document.createElement("div");
    card.className = "grid-card";
    card.innerHTML = `
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:10px;">
        <span class="badge badge-purple">${escapeHtml(type)}</span>
        <span class="badge badge-gold">🪙 ${priceCoins} Coins</span>
      </div>
      <h3 style="font-size:16px; font-weight:700; margin-bottom:6px;">${escapeHtml(name)}</h3>
      <p style="font-size:12px; color: var(--text-muted); margin-bottom:12px;">Resource: ${escapeHtml(item.iconResName || 'N/A')}</p>
      <button class="btn-danger btn-store-delete" style="width:100%;" data-itemid="${escapeHtml(itemId)}">🗑️ Remove Item</button>
    `;
    container.appendChild(card);
  });

  container.querySelectorAll(".btn-store-delete").forEach(btn => {
    btn.addEventListener("click", () => deleteStoreItem(btn.getAttribute("data-itemid")));
  });
}

// Render Wallet Transactions Log
function renderTransactions() {
  const tbody = document.getElementById("txTableBody");
  if (!tbody) return;

  tbody.innerHTML = "";

  if (transactionsData.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; color: var(--text-muted);">No transactions logged yet.</td></tr>`;
    return;
  }

  transactionsData.slice(0, 50).forEach(tx => {
    if (!tx) return;

    const user = usersData[tx.uid] ? usersData[tx.uid].name : (tx.uid ? tx.uid.substring(0, 8) : "N/A");
    const dateStr = tx.timestamp ? new Date(tx.timestamp).toLocaleString() : 'N/A';

    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td><small style="color: var(--text-muted);">${escapeHtml(tx.txId || 'N/A')}</small></td>
      <td><strong>${escapeHtml(user)}</strong></td>
      <td>
        <strong style="display:block;">${escapeHtml(tx.title || 'Transaction')}</strong>
        <span class="badge badge-cyan">${escapeHtml(tx.type || 'TOPUP')}</span>
      </td>
      <td><strong style="color: var(--accent-gold);">${tx.coinAmount || 0} 🪙</strong></td>
      <td><strong style="color: var(--primary);">${tx.diamondAmount || 0} 💎</strong></td>
      <td><small style="color: var(--text-secondary);">${escapeHtml(dateStr)}</small></td>
    `;
    tbody.appendChild(tr);
  });
}

// Search Inputs Handlers
function setupSearchFilters() {
  const uSearch = document.getElementById("userSearch");
  if (uSearch) uSearch.addEventListener("input", renderUsers);

  const rSearch = document.getElementById("roomSearch");
  if (rSearch) rSearch.addEventListener("input", renderRooms);

  const pSearch = document.getElementById("postSearch");
  if (pSearch) pSearch.addEventListener("input", renderPosts);

  const rcSearch = document.getElementById("rechargeSearch");
  if (rcSearch) rcSearch.addEventListener("input", renderRecharges);

  const bTarget = document.getElementById("broadcastTarget");
  if (bTarget) {
    bTarget.addEventListener("change", (e) => {
      const group = document.getElementById("targetUidGroup");
      if (group) group.style.display = (e.target.value === "SPECIFIC") ? "block" : "none";
    });
  }
}

// Setup Form Handlers
function setupFormHandlers() {
  // Topup Form
  const fTopup = document.getElementById("formTopup");
  if (fTopup) {
    fTopup.addEventListener("submit", (e) => {
      e.preventDefault();
      const uid = document.getElementById("topupUid").value;
      const amount = parseInt(document.getElementById("topupCoins").value, 10) || 0;
      const op = document.getElementById("topupOpType").value;

      if (!uid || !db) return;

      const userRef = db.ref("users").child(uid);
      userRef.child("coins").once("value").then((snap) => {
        let current = parseInt(snap.val(), 10) || 0;
        let newCoins = op === "ADD" ? (current + amount) : amount;

        // ⚡ Optimistic UI Update
        if (usersData[uid]) {
          usersData[uid].coins = newCoins;
          renderUsers();
          updateDashboardStats();
        }

        return userRef.child("coins").set(newCoins).then(() => newCoins);
      }).then((newCoins) => {
        const txRef = db.ref("wallet_transactions").child(uid).push();
        return txRef.set({
          txId: txRef.key,
          title: "Admin Coin Top-Up 🪙",
          description: `Admin updated coins balance by ${amount}`,
          coinAmount: amount,
          diamondAmount: 0,
          type: "TOPUP",
          timestamp: Date.now()
        }).then(() => newCoins);
      }).then((newCoins) => {
        showToast(`Successfully updated coins to ${newCoins}`, "success");
        closeModal("modalTopup");
      }).catch((err) => {
        console.error("Topup error:", err);
        showToast("Topup failed: " + err.message, "danger");
      });
    });
  }

  // Edit User Form
  const fEditUser = document.getElementById("formEditUser");
  if (fEditUser) {
    fEditUser.addEventListener("submit", (e) => {
      e.preventDefault();
      const uid = document.getElementById("editUserUid").value;
      const name = document.getElementById("editUserName").value;
      const level = document.getElementById("editUserLevel").value;
      const gender = document.getElementById("editUserGender").value;
      const bio = document.getElementById("editUserBio").value;

      if (!uid || !db) return;

      // ⚡ Optimistic UI Update
      if (usersData[uid]) {
        usersData[uid].name = name;
        usersData[uid].level = level;
        usersData[uid].gender = gender;
        usersData[uid].bio = bio;
        renderUsers();
      }

      db.ref("users").child(uid).update({
        name: name,
        level: level,
        gender: gender,
        bio: bio
      }).then(() => {
        showToast("User details updated successfully", "success");
        closeModal("modalEditUser");
      }).catch((err) => {
        console.error("Edit user error:", err);
        showToast("Failed to update user: " + err.message, "danger");
      });
    });
  }

  // Add Store Item Form
  const btnAddStore = document.getElementById("btnAddStoreItem");
  if (btnAddStore) btnAddStore.addEventListener("click", () => openModal("modalAddStoreItem"));

  const fAddStore = document.getElementById("formAddStoreItem");
  if (fAddStore) {
    fAddStore.addEventListener("submit", (e) => {
      e.preventDefault();
      const name = document.getElementById("storeItemName").value;
      const type = document.getElementById("storeItemType").value;
      const price = parseInt(document.getElementById("storeItemPrice").value, 10) || 0;
      const icon = document.getElementById("storeItemIcon").value;

      if (!db) return;

      const newRef = db.ref("store_items").push();

      // ⚡ Optimistic UI Update
      storeData[newRef.key] = {
        id: newRef.key,
        name: name,
        type: type,
        priceCoins: price,
        iconResName: icon
      };
      renderStore();

      newRef.set({
        id: newRef.key,
        name: name,
        type: type,
        priceCoins: price,
        iconResName: icon
      }).then(() => {
        showToast("Store item added successfully!", "success");
        closeModal("modalAddStoreItem");
        fAddStore.reset();
      }).catch((err) => {
        console.error("Add store item error:", err);
        showToast("Failed to add store item: " + err.message, "danger");
      });
    });
  }

  // Broadcast Notification Form
  const fBroadcast = document.getElementById("formBroadcast");
  if (fBroadcast) {
    fBroadcast.addEventListener("submit", (e) => {
      e.preventDefault();
      const target = document.getElementById("broadcastTarget").value;
      const specificUid = document.getElementById("broadcastUid").value;
      const title = document.getElementById("broadcastTitle").value;
      const message = document.getElementById("broadcastMessage").value;

      if (!db) return;

      const notifObj = {
        title: title,
        message: message,
        type: "SYSTEM_BROADCAST",
        timestamp: Date.now()
      };

      if (target === "SPECIFIC" && specificUid) {
        db.ref("notifications").child(specificUid).push().set(notifObj).then(() => {
          showToast("Notification sent to " + specificUid, "success");
          fBroadcast.reset();
        }).catch((err) => {
          showToast("Broadcast error: " + err.message, "danger");
        });
      } else {
        const uids = Object.keys(usersData || {});
        let count = 0;
        uids.forEach(uid => {
          db.ref("notifications").child(uid).push().set(notifObj);
          count++;
        });
        showToast(`Broadcast sent to ${count} users!`, "success");
        fBroadcast.reset();
      }
    });
  }

  // Config Form Modal
  const btnCfg = document.getElementById("btnOpenConfig");
  if (btnCfg) {
    btnCfg.addEventListener("click", () => {
      document.getElementById("cfgDatabaseURL").value = firebaseConfig.databaseURL;
      document.getElementById("cfgApiKey").value = firebaseConfig.apiKey;
      document.getElementById("cfgProjectId").value = firebaseConfig.projectId;
      openModal("modalConfig");
    });
  }

  const fCfg = document.getElementById("formConfig");
  if (fCfg) {
    fCfg.addEventListener("submit", (e) => {
      e.preventDefault();
      firebaseConfig.databaseURL = document.getElementById("cfgDatabaseURL").value.trim();
      firebaseConfig.apiKey = document.getElementById("cfgApiKey").value.trim();
      firebaseConfig.projectId = document.getElementById("cfgProjectId").value.trim();

      localStorage.setItem('roomchat_admin_config', JSON.stringify(firebaseConfig));
      showToast("Firebase Config saved! Live Reconnecting...", "success");
      closeModal("modalConfig");
      initFirebase();
    });
  }

  // Reset Config Button
  const btnResetCfg = document.getElementById("btnResetConfig");
  if (btnResetCfg) {
    btnResetCfg.addEventListener("click", () => {
      if (confirm("Reset Firebase configuration to default settings?")) {
        localStorage.removeItem('roomchat_admin_config');
        firebaseConfig = defaultConfig;
        showToast("Resetting to default config...", "info");
        closeModal("modalConfig");
        initFirebase();
      }
    });
  }

  // Refresh Button
  const btnRef = document.getElementById("btnRefresh");
  if (btnRef) {
    btnRef.addEventListener("click", () => {
      showToast("Refreshing Realtime Data...", "info");
      initFirebase();
    });
  }
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
  document.getElementById("editUserName").value = u.name || u.userName || "";
  document.getElementById("editUserLevel").value = u.level || "1";
  document.getElementById("editUserGender").value = u.gender || "Male";
  document.getElementById("editUserBio").value = u.bio || "";

  openModal("modalEditUser");
}

function deleteUser(uid, name) {
  if (!db) return;
  if (confirm(`Are you sure you want to delete user '${name}' (${uid}) from Firebase?`)) {

    // ⚡ Optimistic Instant UI Update
    delete usersData[uid];
    renderUsers();
    updateDashboardStats();

    db.ref("users").child(uid).remove().then(() => {
      showToast(`User '${name}' deleted!`, "success");
    }).catch((err) => {
      showToast("Delete user failed: " + err.message, "danger");
    });
  }
}

// Room Actions
function toggleRoomLock(roomId, shouldLock) {
  if (!db) return;

  // ⚡ Optimistic Instant UI Update
  if (roomsData[roomId]) {
    roomsData[roomId].isLocked = shouldLock;
    renderRooms();
  }

  db.ref("rooms").child(roomId).child("isLocked").set(shouldLock).then(() => {
    showToast(`Room ${roomId} ${shouldLock ? 'locked' : 'unlocked'}`, "success");
  }).catch((err) => {
    showToast("Lock toggle failed: " + err.message, "danger");
  });
}

function deleteRoom(roomId) {
  if (!db) return;
  if (confirm(`Are you sure you want to close/delete chat room '${roomId}'?`)) {

    // ⚡ Optimistic Instant UI Update
    delete roomsData[roomId];
    renderRooms();
    updateDashboardStats();

    db.ref("rooms").child(roomId).remove().then(() => {
      showToast(`Room '${roomId}' closed!`, "success");
    }).catch((err) => {
      showToast("Delete room failed: " + err.message, "danger");
    });
  }
}

// Post Actions
function deletePost(postId) {
  if (!db) return;
  if (confirm(`Are you sure you want to delete this community post?`)) {

    // ⚡ Optimistic Instant UI Update
    delete postsData[postId];
    renderPosts();
    updateDashboardStats();

    db.ref("Posts").child(postId).remove().then(() => {
      showToast("Post deleted successfully!", "success");
    }).catch((err) => {
      showToast("Delete post failed: " + err.message, "danger");
    });
  }
}

// Store Actions
function deleteStoreItem(itemId) {
  if (!db) return;
  if (confirm(`Are you sure you want to remove this store item?`)) {

    // ⚡ Optimistic Instant UI Update
    delete storeData[itemId];
    renderStore();

    db.ref("store_items").child(itemId).remove().then(() => {
      showToast("Store item removed!", "success");
    }).catch((err) => {
      showToast("Delete store item failed: " + err.message, "danger");
    });
  }
}

// Modal Helpers
function openModal(id) {
  const m = document.getElementById(id);
  if (m) m.classList.add("active");
}

function closeModal(id) {
  const m = document.getElementById(id);
  if (m) m.classList.remove("active");
}

// Toast Helper
function showToast(message, type = "info") {
  const container = document.getElementById("toastContainer");
  if (!container) return;

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
  if (str === null || str === undefined) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}
