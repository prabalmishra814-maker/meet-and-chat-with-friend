/* ==========================================================================
   GreedyPro Fruit & Food Game Script
   100% Standalone, Isolated Scope
   ========================================================================== */

(function () {
  'use strict';

  // --- Food Definitions & Multipliers ---
  const FOOD_DATA = {
    tomato:  { name: 'Tomato',  mult: 5,  img: 'assets/food_tomato.svg' },
    cabbage: { name: 'Cabbage', mult: 5,  img: 'assets/food_cabbage.svg' },
    corn:    { name: 'Corn',    mult: 5,  img: 'assets/food_corn.svg' },
    carrot:  { name: 'Carrot',  mult: 5,  img: 'assets/food_carrot.svg' },
    hotdog:  { name: 'Hotdog',  mult: 10, img: 'assets/food_hotdog.svg' },
    skewer:  { name: 'Skewer',  mult: 15, img: 'assets/food_skewer.svg' },
    ham:     { name: 'Ham',     mult: 25, img: 'assets/food_ham.svg' },
    steak:   { name: 'Steak',   mult: 45, img: 'assets/food_steak.svg' }
  };

  // Outer Wheel Order (Clockwise starting from Top)
  const WHEEL_ITEMS = ['tomato', 'cabbage', 'corn', 'carrot', 'hotdog', 'skewer', 'ham', 'steak'];

  // --- Initial Game State ---
  let state = {
    balance: 36871,
    todayWinnings: 0,
    jackpot: 21417758,
    selectedChip: 1000,
    bets: {},
    timerSec: 30,
    gameState: 'BETTING', // 'BETTING' | 'SPINNING'
    soundEnabled: true,
    resultHistory: ['corn', 'corn', 'cabbage', 'tomato', 'hotdog', 'tomato']
  };

  // Clear bets helper
  function resetBets() {
    state.bets = {
      tomato: 0, cabbage: 0, corn: 0, carrot: 0, hotdog: 0,
      skewer: 0, ham: 0, steak: 0
    };
  }
  resetBets();

  // --- Web Audio API Synthesizer ---
  let audioCtx = null;

  function initAudio() {
    if (!audioCtx) {
      const AudioContext = window.AudioContext || window.webkitAudioContext;
      if (AudioContext) {
        audioCtx = new AudioContext();
      }
    }
    if (audioCtx && audioCtx.state === 'suspended') {
      audioCtx.resume();
    }
  }

  function playSound(type) {
    if (!state.soundEnabled) return;
    initAudio();
    if (!audioCtx) return;

    try {
      const osc = audioCtx.createOscillator();
      const gain = audioCtx.createGain();
      osc.connect(gain);
      gain.connect(audioCtx.destination);

      const now = audioCtx.currentTime;

      if (type === 'chip') {
        osc.type = 'sine';
        osc.frequency.setValueAtTime(600, now);
        osc.frequency.exponentialRampToValueAtTime(1200, now + 0.08);
        gain.gain.setValueAtTime(0.3, now);
        gain.gain.linearRampToValueAtTime(0.01, now + 0.08);
        osc.start(now);
        osc.stop(now + 0.08);
      } else if (type === 'tick') {
        osc.type = 'triangle';
        osc.frequency.setValueAtTime(800, now);
        gain.gain.setValueAtTime(0.15, now);
        gain.gain.linearRampToValueAtTime(0.01, now + 0.04);
        osc.start(now);
        osc.stop(now + 0.04);
      } else if (type === 'spinTick') {
        osc.type = 'sine';
        osc.frequency.setValueAtTime(900, now);
        gain.gain.setValueAtTime(0.2, now);
        gain.gain.linearRampToValueAtTime(0.01, now + 0.05);
        osc.start(now);
        osc.stop(now + 0.05);
      } else if (type === 'win') {
        osc.type = 'triangle';
        osc.frequency.setValueAtTime(523.25, now); // C5
        osc.frequency.setValueAtTime(659.25, now + 0.1); // E5
        osc.frequency.setValueAtTime(783.99, now + 0.2); // G5
        osc.frequency.setValueAtTime(1046.50, now + 0.3); // C6
        gain.gain.setValueAtTime(0.4, now);
        gain.gain.linearRampToValueAtTime(0.01, now + 0.6);
        osc.start(now);
        osc.stop(now + 0.6);
      }
    } catch (e) {
      console.warn('Audio play error:', e);
    }
  }

  // --- DOM Elements ---
  const el = {
    balanceNum: document.getElementById('balance-num'),
    winningsNum: document.getElementById('winnings-num'),
    jackpotNum: document.getElementById('jackpot-num'),
    timerDisplay: document.getElementById('timer-display'),
    selectTimeLabel: document.querySelector('.select-time-label'),
    chipBtns: document.querySelectorAll('.chip-btn'),
    foodBubbles: document.querySelectorAll('.food-bubble'),
    cornerBtns: document.querySelectorAll('.corner-btn'),
    resultHistory: document.getElementById('result-history'),
    toastMsg: document.getElementById('toast-msg'),
    musicBtn: document.getElementById('music-btn'),
    menuBtn: document.getElementById('menu-btn'),
    menuModal: document.getElementById('menu-modal'),
    closeMenuBtn: document.getElementById('close-menu-btn'),
    soundToggleBtn: document.getElementById('sound-toggle-btn'),
    modalAddCoins: document.getElementById('modal-add-coins'),
    resetGameBtn: document.getElementById('reset-game-btn'),
    addCoinsBtn: document.getElementById('add-coins-btn'),
    coinsModal: document.getElementById('coins-modal'),
    claimCoinsBtn: document.getElementById('claim-coins-btn'),
    closeCoinsBtn: document.getElementById('close-coins-btn'),
    leaderboardBtn: document.getElementById('leaderboard-btn'),
    leaderboardModal: document.getElementById('leaderboard-modal'),
    closeLeaderboardBtn: document.getElementById('close-leaderboard-btn'),
    lbTabCurrent: document.getElementById('lb-tab-current'),
    lbTabPrev: document.getElementById('lb-tab-prev'),
    leaderboardList: document.getElementById('leaderboard-list')
  };

  // --- Formatting Helpers ---
  function formatNum(n) {
    return Number(n).toLocaleString('en-US');
  }

  function formatShort(n) {
    if (n >= 1000000) return (n / 1000000).toFixed(0) + 'M';
    if (n >= 1000) return (n / 1000).toFixed(0) + 'K';
    return n.toString();
  }

  // --- UI Update Functions ---
  function updateUI() {
    if (el.balanceNum) el.balanceNum.textContent = formatNum(state.balance);
    if (el.winningsNum) el.winningsNum.textContent = formatNum(state.todayWinnings);
    if (el.jackpotNum) el.jackpotNum.textContent = formatNum(state.jackpot);

    el.foodBubbles.forEach(bubble => {
      const food = bubble.getAttribute('data-food');
      const betAmt = state.bets[food] || 0;
      const badge = bubble.querySelector('.bet-badge');
      if (badge) badge.textContent = formatShort(betAmt);

      if (betAmt > 0) {
        bubble.classList.add('has-bet');
      } else {
        bubble.classList.remove('has-bet');
      }
    });

    renderResultHistory();
  }

  function renderResultHistory() {
    if (!el.resultHistory) return;
    el.resultHistory.innerHTML = '';
    state.resultHistory.forEach((foodKey, idx) => {
      const food = FOOD_DATA[foodKey] || FOOD_DATA.tomato;
      const itemEl = document.createElement('div');
      itemEl.className = 'result-item';

      const img = document.createElement('img');
      img.src = food.img;
      img.alt = food.name;
      itemEl.appendChild(img);

      if (idx === 0) {
        const badge = document.createElement('div');
        badge.className = 'new-badge';
        badge.textContent = 'new';
        itemEl.appendChild(badge);
      }

      el.resultHistory.appendChild(itemEl);
    });
  }

  function showToast(msg, duration = 2500) {
    if (!el.toastMsg) return;
    el.toastMsg.textContent = msg;
    el.toastMsg.classList.remove('hidden');
    setTimeout(() => {
      el.toastMsg.classList.add('hidden');
    }, duration);
  }

  // --- Betting Interaction ---
  function placeBet(foodKey) {
    initAudio();
    if (state.gameState !== 'BETTING') {
      showToast('Wait for the next round!');
      return;
    }

    const betAmount = state.selectedChip;
    if (state.balance < betAmount) {
      playSound('tick');
      showToast('Insufficient Coins! Click + to add coins!');
      if (el.coinsModal) el.coinsModal.classList.remove('hidden');
      return;
    }

    state.balance -= betAmount;
    state.bets[foodKey] = (state.bets[foodKey] || 0) + betAmount;
    playSound('chip');
    updateUI();
  }

  el.chipBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      initAudio();
      playSound('chip');
      el.chipBtns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      state.selectedChip = parseInt(btn.getAttribute('data-amount'), 10);
    });
  });

  el.foodBubbles.forEach(bubble => {
    bubble.addEventListener('click', () => {
      const food = bubble.getAttribute('data-food');
      placeBet(food);
    });
  });

  // --- Wall-Clock Synchronized Game Loop Timer & Outcome PRNG ---
  const CYCLE_DURATION = 40; // 30s betting + 10s spin
  const BETTING_DURATION = 30;

  let timerInterval = null;
  let currentRoundIdx = -1;
  let spinTriggeredRound = -1;

  function mulberry32(a) {
    return function() {
      let t = a += 0x6D2B79F5;
      t = Math.imul(t ^ (t >>> 15), t | 1);
      t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
      return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
    };
  }

  function getGreedyOutcome(roundIndex) {
    const seed = roundIndex * 7919 + 20011;
    const prng1 = mulberry32(seed);
    const prng2 = mulberry32(seed + 100);
    const rand = prng1();
    if (rand < 0.65) {
      const x5s = ['tomato', 'cabbage', 'corn', 'carrot'];
      return x5s[Math.floor(prng2() * x5s.length)];
    } else if (rand < 0.88) {
      const x10s = ['hotdog', 'skewer'];
      return x10s[Math.floor(prng2() * x10s.length)];
    } else {
      const x25s = ['ham', 'steak'];
      return x25s[Math.floor(prng2() * x25s.length)];
    }
  }

  function syncTimer() {
    const nowSec = Math.floor(Date.now() / 1000);
    const cyclePos = nowSec % CYCLE_DURATION; // 0 to 39
    const roundIdx = Math.floor(nowSec / CYCLE_DURATION);

    if (cyclePos < BETTING_DURATION) {
      const remainingBet = BETTING_DURATION - cyclePos; // 30 down to 1
      state.timerSec = remainingBet;

      if (state.gameState !== 'BETTING' || roundIdx !== currentRoundIdx) {
        currentRoundIdx = roundIdx;
        resetBets();
        state.gameState = 'BETTING';
        if (el.selectTimeLabel) el.selectTimeLabel.textContent = 'Select Time';
        updateUI();
      }

      if (el.timerDisplay) el.timerDisplay.textContent = state.timerSec + 's';

      if (state.timerSec <= 5 && state.timerSec > 0) {
        playSound('tick');
      }
    } else {
      const remainingSpin = CYCLE_DURATION - cyclePos; // 10 down to 1
      state.timerSec = remainingSpin;

      if (el.timerDisplay) el.timerDisplay.textContent = state.timerSec + 's';

      if (spinTriggeredRound !== roundIdx) {
        currentRoundIdx = roundIdx;
        startSpinRound(roundIdx, remainingSpin);
      }
    }
  }

  syncTimer();
  timerInterval = setInterval(syncTimer, 1000);

  function startSpinRound(roundIdx, remainingSpinSec) {
    spinTriggeredRound = roundIdx;

    let totalBet = 0;
    for (let k in state.bets) {
      totalBet += state.bets[k] || 0;
    }

    if (totalBet > 0 && window.AndroidGreedy) {
      const callbackName = '_cb_greedy_bet_' + roundIdx + '_' + Date.now();
      window[callbackName] = function(success, newBalance, errorMsg) {
        delete window[callbackName];
        if (success) {
          if (typeof newBalance === 'number' && !isNaN(newBalance)) {
            state.balance = newBalance;
          }
          state.gameState = 'SPINNING';
          if (el.selectTimeLabel) el.selectTimeLabel.textContent = 'Predicting...';
          updateUI();
          executeSpinAnimation(roundIdx, remainingSpinSec);
        } else {
          showToast(errorMsg || "Bet deduction failed!");
          resetBets();
          state.gameState = 'BETTING';
          updateUI();
        }
      };
      window.AndroidGreedy.deductBetCoins(totalBet, roundIdx, callbackName);
      return;
    }

    state.gameState = 'SPINNING';
    if (el.selectTimeLabel) el.selectTimeLabel.textContent = 'Predicting...';
    executeSpinAnimation(roundIdx, remainingSpinSec);
  }

  function executeSpinAnimation(roundIdx, remainingSpinSec) {
    const winnerFood = getGreedyOutcome(roundIdx);
    let winnerIndex = WHEEL_ITEMS.indexOf(winnerFood);

    const decelDelays = [100, 130, 170, 220, 290, 380, 500, 700];
    const decelSteps = decelDelays.length;

    const targetDurationMs = Math.max(1500, Math.min(7500, (remainingSpinSec - 2) * 1000));
    const fastDuration = Math.max(500, targetDurationMs - 2490);
    const fastDelay = Math.max(35, Math.floor(fastDuration / 60));

    let reqRemainder = (winnerIndex + 1 - decelSteps) % WHEEL_ITEMS.length;
    if (reqRemainder < 0) reqRemainder += WHEEL_ITEMS.length;

    let fastSteps = Math.floor(fastDuration / fastDelay);
    fastSteps = fastSteps - (fastSteps % WHEEL_ITEMS.length) + reqRemainder;
    if (fastSteps < WHEEL_ITEMS.length) fastSteps += WHEEL_ITEMS.length;

    let totalSteps = fastSteps + decelSteps;
    let currentIdx = 0;
    let stepCount = 0;

    function spinStep() {
      el.foodBubbles.forEach(b => b.classList.remove('active-spin'));
      if (el.cornerBtns) el.cornerBtns.forEach(b => b.classList.remove('active-spin'));

      playSound('spinTick');

      const bubbleEl = document.querySelector(`.food-bubble[data-food="${WHEEL_ITEMS[currentIdx]}"]`);
      if (bubbleEl) bubbleEl.classList.add('active-spin');

      stepCount++;
      if (stepCount < totalSteps) {
        currentIdx = (currentIdx + 1) % WHEEL_ITEMS.length;
        let nextDelay = fastDelay;
        if (stepCount >= fastSteps) {
          let decelIndex = stepCount - fastSteps;
          nextDelay = decelDelays[decelIndex] || 500;
        }
        setTimeout(spinStep, nextDelay);
      } else {
        finishSpinRound(winnerFood, roundIdx);
      }
    }

    spinStep();
  }

  function finishSpinRound(winnerFood, roundIdx) {
    el.foodBubbles.forEach(b => b.classList.remove('active-spin'));
    if (el.cornerBtns) el.cornerBtns.forEach(b => b.classList.remove('active-spin'));

    let winningEl = document.querySelector(`.food-bubble[data-food="${winnerFood}"]`) ||
                    document.querySelector(`.corner-btn[data-food="${winnerFood}"]`);

    if (winningEl) {
      winningEl.classList.add('winner');
      setTimeout(() => winningEl.classList.remove('winner'), 3000);
    }

    const foodInfo = FOOD_DATA[winnerFood] || FOOD_DATA.tomato;
    const userBet = state.bets[winnerFood] || 0;

    if (userBet > 0) {
      const winAmount = userBet * foodInfo.mult;
      if (window.AndroidGreedy) {
        const callbackName = '_cb_greedy_win_' + (roundIdx || Date.now()) + '_' + Date.now();
        window[callbackName] = function(success, newBalance, errorMsg) {
          delete window[callbackName];
          if (success && typeof newBalance === 'number' && !isNaN(newBalance)) {
            state.balance = newBalance;
          } else {
            state.balance += winAmount;
          }
          state.todayWinnings += winAmount;
          playSound('win');
          showToast(`🎉 WINNER! ${foodInfo.name} (x${foodInfo.mult}) +${formatNum(winAmount)} Coins!`, 3500);
          resetBets();
          updateUI();
        };
        window.AndroidGreedy.addWinPayout(winAmount, roundIdx, callbackName);
      } else {
        state.balance += winAmount;
        state.todayWinnings += winAmount;
        playSound('win');
        showToast(`🎉 WINNER! ${foodInfo.name} (x${foodInfo.mult}) +${formatNum(winAmount)} Coins!`, 3500);
        resetBets();
        updateUI();
      }
    } else {
      showToast(`Round Winner: ${foodInfo.name} (x${foodInfo.mult})`);
      resetBets();
      updateUI();
    }

    state.resultHistory.unshift(winnerFood);
    if (state.resultHistory.length > 8) state.resultHistory.pop();

    state.jackpot += Math.floor(Math.random() * 400) + 100;
  }

  // --- Modals & Settings Handlers ---
  if (el.musicBtn) {
    el.musicBtn.addEventListener('click', () => {
      state.soundEnabled = !state.soundEnabled;
      if (state.soundEnabled) {
        initAudio();
        playSound('chip');
        el.musicBtn.classList.remove('muted');
        showToast('🔊 Sound Enabled');
      } else {
        el.musicBtn.classList.add('muted');
        showToast('🔇 Sound Muted');
      }
      if (el.soundToggleBtn) {
        el.soundToggleBtn.textContent = state.soundEnabled ? '🔊 ON' : '🔇 OFF';
      }
    });
  }

  if (el.menuBtn) {
    el.menuBtn.addEventListener('click', () => {
      initAudio();
      if (el.menuModal) el.menuModal.classList.remove('hidden');
    });
  }

  if (el.closeMenuBtn) {
    el.closeMenuBtn.addEventListener('click', () => {
      if (el.menuModal) el.menuModal.classList.add('hidden');
    });
  }

  if (el.soundToggleBtn) {
    el.soundToggleBtn.addEventListener('click', () => {
      state.soundEnabled = !state.soundEnabled;
      el.soundToggleBtn.textContent = state.soundEnabled ? '🔊 ON' : '🔇 OFF';
    });
  }

  if (el.modalAddCoins) {
    el.modalAddCoins.addEventListener('click', () => {
      state.balance += 50000;
      playSound('win');
      updateUI();
      showToast('+50,000 Coins Added!');
    });
  }

  if (el.resetGameBtn) {
    el.resetGameBtn.addEventListener('click', () => {
      state.balance = 36871;
      state.todayWinnings = 0;
      resetBets();
      updateUI();
      showToast('Game Reset!');
      if (el.menuModal) el.menuModal.classList.add('hidden');
    });
  }

  if (el.addCoinsBtn) {
    el.addCoinsBtn.addEventListener('click', () => {
      initAudio();
      if (el.coinsModal) el.coinsModal.classList.remove('hidden');
    });
  }

  if (el.claimCoinsBtn) {
    el.claimCoinsBtn.addEventListener('click', () => {
      state.balance += 100000;
      playSound('win');
      updateUI();
      showToast('🎉 100,000 Free Coins Claimed!');
      if (el.coinsModal) el.coinsModal.classList.add('hidden');
    });
  }

  if (el.closeCoinsBtn) {
    el.closeCoinsBtn.addEventListener('click', () => {
      if (el.coinsModal) el.coinsModal.classList.add('hidden');
    });
  }

  // --- Leaderboard Dialog Handlers ---
  if (el.leaderboardBtn) {
    el.leaderboardBtn.addEventListener('click', openLeaderboard);
  }
  if (el.closeLeaderboardBtn) {
    el.closeLeaderboardBtn.addEventListener('click', () => {
      if (el.leaderboardModal) el.leaderboardModal.classList.add('hidden');
    });
  }
  if (el.lbTabCurrent) {
    el.lbTabCurrent.addEventListener('click', () => {
      el.lbTabCurrent.classList.add('active');
      if (el.lbTabPrev) el.lbTabPrev.classList.remove('active');
      loadLeaderboardForRound(currentRoundIdx);
    });
  }
  if (el.lbTabPrev) {
    el.lbTabPrev.addEventListener('click', () => {
      el.lbTabPrev.classList.add('active');
      if (el.lbTabCurrent) el.lbTabCurrent.classList.remove('active');
      loadLeaderboardForRound(currentRoundIdx - 1);
    });
  }

  function openLeaderboard() {
    initAudio();
    if (!el.leaderboardModal) return;
    el.leaderboardModal.classList.remove('hidden');
    if (el.lbTabCurrent) el.lbTabCurrent.classList.add('active');
    if (el.lbTabPrev) el.lbTabPrev.classList.remove('active');
    loadLeaderboardForRound(currentRoundIdx);
  }

  function loadLeaderboardForRound(roundIdx) {
    if (!el.leaderboardList) return;
    el.leaderboardList.innerHTML = '<div class="lb-empty">Loading leaderboard...</div>';

    if (window.AndroidGreedy && typeof window.AndroidGreedy.getRoundLeaderboard === 'function') {
      const callbackName = '_cb_greedy_lb_' + Date.now();
      window[callbackName] = function(jsonStr) {
        delete window[callbackName];
        renderLeaderboardItems(jsonStr);
      };
      window.AndroidGreedy.getRoundLeaderboard(roundIdx, callbackName);
    } else {
      renderLeaderboardItems('[]');
    }
  }

  function renderLeaderboardItems(jsonStr) {
    if (!el.leaderboardList) return;
    let list = [];
    try {
      list = JSON.parse(jsonStr || '[]');
    } catch(e) {
      list = [];
    }

    if (!list || list.length === 0) {
      el.leaderboardList.innerHTML = '<div class="lb-empty">No participants in this round!</div>';
      return;
    }

    el.leaderboardList.innerHTML = '';
    list.forEach(item => {
      const row = document.createElement('div');
      row.className = 'lb-item';

      let rankDisplay = `#${item.rank}`;
      if (item.rank === 1) rankDisplay = '🥇';
      else if (item.rank === 2) rankDisplay = '🥈';
      else if (item.rank === 3) rankDisplay = '🥉';

      const avatarUrl = (item.userAvatar && item.userAvatar.trim() !== '')
        ? item.userAvatar
        : 'assets/chip_1k.svg';

      const betFormatted = (item.betAmount || 0).toLocaleString();
      const winVal = item.winAmount || 0;
      const winFormatted = winVal > 0 ? `+${winVal.toLocaleString()}` : '0';
      const winClass = winVal > 0 ? 'lb-win' : 'lb-win zero';

      row.innerHTML = `
        <div class="lb-left">
          <div class="lb-rank">${rankDisplay}</div>
          <img class="lb-avatar" src="${avatarUrl}" onerror="this.src='assets/chip_1k.svg'" alt="Avatar">
          <div class="lb-name">${escapeHtml(item.userName || 'Player')}</div>
        </div>
        <div class="lb-right">
          <div class="lb-win ${winClass}">Win: ${winFormatted}</div>
          <div class="lb-bet">Bet: ${betFormatted}</div>
        </div>
      `;
      el.leaderboardList.appendChild(row);
    });
  }

  function escapeHtml(str) {
    return String(str || '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
  }

  // Initial Render
  updateUI();

  // Connect with Android Bridge if present
  if (window.AndroidGreedy) {
    window.updateGreedyBalance = function(newBalance) {
      if (typeof newBalance === 'number' && !isNaN(newBalance)) {
        state.balance = newBalance;
        updateUI();
      }
    };
    try {
      window.AndroidGreedy.requestUserCoins();
    } catch(e) {
      console.error("Error requesting user coins:", e);
    }
  }

})();
