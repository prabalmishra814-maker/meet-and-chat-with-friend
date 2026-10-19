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
    closeCoinsBtn: document.getElementById('close-coins-btn')
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
    el.balanceNum.textContent = formatNum(state.balance);
    el.winningsNum.textContent = formatNum(state.todayWinnings);
    el.jackpotNum.textContent = formatNum(state.jackpot);

    // Update food bubble bet badges
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
      el.coinsModal.classList.remove('hidden');
      return;
    }

    state.balance -= betAmount;
    state.bets[foodKey] = (state.bets[foodKey] || 0) + betAmount;
    playSound('chip');
    updateUI();
  }

  // Attach click handlers to chips
  el.chipBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      initAudio();
      playSound('chip');
      el.chipBtns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      state.selectedChip = parseInt(btn.getAttribute('data-amount'), 10);
    });
  });

  // Attach click handlers to food bubbles
  el.foodBubbles.forEach(bubble => {
    bubble.addEventListener('click', () => {
      const food = bubble.getAttribute('data-food');
      placeBet(food);
    });
  });

  // --- Wall-Clock Synchronized Game Loop Timer ---
  const CYCLE_DURATION = 40; // 30s betting + 10s spin
  const BETTING_DURATION = 30;

  let timerInterval = setInterval(() => {
    const nowSec = Math.floor(Date.now() / 1000);
    const cyclePos = nowSec % CYCLE_DURATION; // 0 to 39

    if (cyclePos < BETTING_DURATION) {
      const remainingBet = BETTING_DURATION - cyclePos; // 30 down to 1
      state.timerSec = remainingBet;

      if (state.gameState !== 'BETTING') {
        resetBets();
        state.gameState = 'BETTING';
        if (el.selectTimeLabel) el.selectTimeLabel.textContent = 'Select Time';
        updateUI();
      }

      el.timerDisplay.textContent = state.timerSec + 's';

      if (state.timerSec <= 5 && state.timerSec > 0) {
        playSound('tick');
      }
    } else {
      const remainingSpin = CYCLE_DURATION - cyclePos; // 10 down to 1
      state.timerSec = remainingSpin;

      if (state.gameState === 'BETTING') {
        startSpinRound();
      }
    }
  }, 1000);

  // --- Spin & Winner Outcome Generation ---
  function pickOutcome() {
    // Weighted selection logic
    const rand = Math.random();
    if (rand < 0.65) {
      // 65% chance: x5 items (tomato, cabbage, corn, carrot)
      const x5s = ['tomato', 'cabbage', 'corn', 'carrot'];
      return x5s[Math.floor(Math.random() * x5s.length)];
    } else if (rand < 0.88) {
      // 23% chance: x10/x15 items (hotdog, skewer)
      const x10s = ['hotdog', 'skewer'];
      return x10s[Math.floor(Math.random() * x10s.length)];
    } else {
      // 12% chance: x25/x45 items (ham, steak)
      const x25s = ['ham', 'steak'];
      return x25s[Math.floor(Math.random() * x25s.length)];
    }
  }

  function startSpinRound() {
    state.gameState = 'SPINNING';
    if (el.selectTimeLabel) el.selectTimeLabel.textContent = 'Predicting...';
    el.timerDisplay.textContent = 'Spin...';

    const winnerFood = pickOutcome();
    let winnerIndex = WHEEL_ITEMS.indexOf(winnerFood);

    // Deceleration step delays for the final landing phase
    const decelDelays = [100, 130, 170, 220, 290, 380, 500, 700];
    const decelSteps = decelDelays.length; // 8 steps

    // Calculate fastSteps so totalSteps lands precisely on winnerIndex after ~10s
    let targetFast = 100;
    let reqRemainder = (winnerIndex + 1 - decelSteps) % WHEEL_ITEMS.length;
    if (reqRemainder < 0) reqRemainder += WHEEL_ITEMS.length;

    let fastSteps = targetFast - (targetFast % WHEEL_ITEMS.length) + reqRemainder;
    let totalSteps = fastSteps + decelSteps;

    let currentIdx = 0;
    let stepCount = 0;

    function spinStep() {
      // Clear previous spin highlights
      el.foodBubbles.forEach(b => b.classList.remove('active-spin'));
      if (el.cornerBtns) el.cornerBtns.forEach(b => b.classList.remove('active-spin'));

      playSound('spinTick');

      const bubbleEl = document.querySelector(`.food-bubble[data-food="${WHEEL_ITEMS[currentIdx]}"]`);
      if (bubbleEl) bubbleEl.classList.add('active-spin');

      stepCount++;
      if (stepCount < totalSteps) {
        currentIdx = (currentIdx + 1) % WHEEL_ITEMS.length;
        let nextDelay = 75; // Fast spin delay
        if (stepCount >= fastSteps) {
          let decelIndex = stepCount - fastSteps;
          nextDelay = decelDelays[decelIndex] || 500;
        }
        setTimeout(spinStep, nextDelay);
      } else {
        finishSpinRound(winnerFood);
      }
    }

    spinStep();
  }

  function finishSpinRound(winnerFood) {
    el.foodBubbles.forEach(b => b.classList.remove('active-spin'));
    if (el.cornerBtns) el.cornerBtns.forEach(b => b.classList.remove('active-spin'));

    // Highlight winning element
    let winningEl = document.querySelector(`.food-bubble[data-food="${winnerFood}"]`) ||
                    document.querySelector(`.corner-btn[data-food="${winnerFood}"]`);

    if (winningEl) {
      winningEl.classList.add('winner');
      setTimeout(() => winningEl.classList.remove('winner'), 3000);
    }

    // Payout Calculation
    const foodInfo = FOOD_DATA[winnerFood];
    const userBet = state.bets[winnerFood] || 0;

    if (userBet > 0) {
      const winAmount = userBet * foodInfo.mult;
      state.balance += winAmount;
      state.todayWinnings += winAmount;
      playSound('win');
      showToast(`🎉 WINNER! ${foodInfo.name} (x${foodInfo.mult}) +${formatNum(winAmount)} Coins!`, 3500);
    } else {
      showToast(`Round Winner: ${foodInfo.name} (x${foodInfo.mult})`);
    }

    // Add outcome to history
    state.resultHistory.unshift(winnerFood);
    if (state.resultHistory.length > 8) state.resultHistory.pop();

    // Increment Jackpot slightly
    state.jackpot += Math.floor(Math.random() * 400) + 100;

    updateUI();

    // Reset for next round after delay
    setTimeout(() => {
      resetBets();
      state.timerSec = 30;
      state.gameState = 'BETTING';
      if (el.selectTimeLabel) el.selectTimeLabel.textContent = 'Select Time';
      el.timerDisplay.textContent = '30s';
      updateUI();
    }, 3000);
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

  el.menuBtn.addEventListener('click', () => {
    initAudio();
    el.menuModal.classList.remove('hidden');
  });

  el.closeMenuBtn.addEventListener('click', () => {
    el.menuModal.classList.add('hidden');
  });

  el.soundToggleBtn.addEventListener('click', () => {
    state.soundEnabled = !state.soundEnabled;
    el.soundToggleBtn.textContent = state.soundEnabled ? '🔊 ON' : '🔇 OFF';
  });

  el.modalAddCoins.addEventListener('click', () => {
    state.balance += 50000;
    playSound('win');
    updateUI();
    showToast('+50,000 Coins Added!');
  });

  el.resetGameBtn.addEventListener('click', () => {
    state.balance = 36871;
    state.todayWinnings = 0;
    state.timerSec = 30;
    state.gameState = 'BETTING';
    if (el.selectTimeLabel) el.selectTimeLabel.textContent = 'Select Time';
    el.timerDisplay.textContent = '30s';
    resetBets();
    updateUI();
    showToast('Game Reset!');
    el.menuModal.classList.add('hidden');
  });

  el.addCoinsBtn.addEventListener('click', () => {
    initAudio();
    el.coinsModal.classList.remove('hidden');
  });

  el.claimCoinsBtn.addEventListener('click', () => {
    state.balance += 100000;
    playSound('win');
    updateUI();
    showToast('🎉 100,000 Free Coins Claimed!');
    el.coinsModal.classList.add('hidden');
  });

  el.closeCoinsBtn.addEventListener('click', () => {
    el.coinsModal.classList.add('hidden');
  });

  // Initial Render
  updateUI();

})();
