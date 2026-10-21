/**
 * ROYAL SLOT MACHINE GAME ENGINE
 * Independent Standalone JavaScript Module - Premium Edition
 */

(function () {
  'use strict';

  // --- SYMBOL TABLE CONFIGURATION ---
  const SYMBOLS = [
    { id: 'crown',      name: 'Crown',       image: 'assets/crown.svg',      weight: 1, multiplier: 50 },
    { id: 'seven',      name: 'Red Seven',   image: 'assets/seven.svg',      weight: 2, multiplier: 25 },
    { id: 'bell',       name: 'Golden Bell', image: 'assets/bell.svg',       weight: 3, multiplier: 15 },
    { id: 'grape',      name: 'Purple Grape',image: 'assets/grape.svg',      weight: 4, multiplier: 10 },
    { id: 'watermelon', name: 'Watermelon',  image: 'assets/watermelon.svg', weight: 5, multiplier: 5 },
    { id: 'cherry',     name: 'Cherry',      image: 'assets/cherry.svg',     weight: 6, multiplier: 3 }
  ];

  const BET_OPTIONS = [1, 5, 10, 20, 50, 100];
  const SYMBOL_HEIGHT = 85; // Exact symbol cell height in px matching CSS
  const VISIBLE_ROWS = 3;

  // Paylines definitions [reel0_row, reel1_row, reel2_row]
  const PAYLINES = [
    { id: 'line-top',   coords: [0, 0, 0] }, // Top Row
    { id: 'line-mid',   coords: [1, 1, 1] }, // Middle Row
    { id: 'line-bot',   coords: [2, 2, 2] }, // Bottom Row
    { id: 'line-diag1', coords: [0, 1, 2] }, // Top-Left to Bottom-Right
    { id: 'line-diag2', coords: [2, 1, 0] }  // Bottom-Left to Top-Right
  ];

  // --- GAME STATE ---
  const state = {
    balance: 174, // Default per reference screenshot requirement
    currentBetIndex: 2, // 10
    totalBet: 10,
    lastWin: 0,
    todaysWin: 0,
    isSpinning: false,
    isAutoSpin: false,
    soundEnabled: true,
    testOutcome: 'random',
    spinHistory: [],
    reelResults: [[], [], []] // Will hold symbol ids for rows 0,1,2 of each reel
  };

  // --- DOM ELEMENT REFERENCES ---
  const dom = {
    balanceVal: document.getElementById('balance-val'),
    totalBetVal: document.getElementById('total-bet-val'),
    winVal: document.getElementById('win-val'),
    todaysWinVal: document.getElementById('todays-win-val'),

    btnSpin: document.getElementById('btn-spin'),
    btnBet: document.getElementById('btn-bet'),
    btnBetPlus: document.getElementById('btn-bet-plus'),
    btnAuto: document.getElementById('btn-auto'),
    autoBtnLabel: document.getElementById('auto-btn-label'),

    btnHistory: document.getElementById('btn-history'),
    btnSound: document.getElementById('btn-sound'),
    soundIconImg: document.getElementById('sound-icon-img'),
    btnHelp: document.getElementById('btn-help'),
    btnClose: document.getElementById('btn-close'),
    btnTestMode: document.getElementById('btn-test-mode'),

    modalHelp: document.getElementById('modal-help'),
    modalHistory: document.getElementById('modal-history'),
    modalTest: document.getElementById('modal-test'),

    modalHelpClose: document.getElementById('modal-help-close'),
    modalHistoryClose: document.getElementById('modal-history-close'),
    modalTestClose: document.getElementById('modal-test-close'),

    testResultSelect: document.getElementById('test-result-select'),
    btnAddDiamonds: document.getElementById('btn-add-diamonds'),
    btnResetBalance: document.getElementById('btn-reset-balance'),
    historyList: document.getElementById('history-list'),

    winBanner: document.getElementById('win-banner'),
    winBannerTitle: document.getElementById('win-banner-title'),
    winBannerAmount: document.getElementById('win-banner-amount'),

    reelStrips: [
      document.getElementById('reel-strip-0'),
      document.getElementById('reel-strip-1'),
      document.getElementById('reel-strip-2')
    ],

    bgParticles: document.getElementById('bg-particles')
  };

  // --- SYNTHESIZED AUDIO SYSTEM (WEB AUDIO API) ---
  class SoundEffectsManager {
    constructor() {
      this.ctx = null;
    }

    init() {
      if (!this.ctx && (window.AudioContext || window.webkitAudioContext)) {
        const AudioCtx = window.AudioContext || window.webkitAudioContext;
        this.ctx = new AudioCtx();
      }
    }

    playClick() {
      if (!state.soundEnabled) return;
      this.init();
      if (!this.ctx) return;

      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();
      osc.type = 'sine';
      osc.frequency.setValueAtTime(600, this.ctx.currentTime);
      osc.frequency.exponentialRampToValueAtTime(150, this.ctx.currentTime + 0.05);
      gain.gain.setValueAtTime(0.3, this.ctx.currentTime);
      gain.gain.linearRampToValueAtTime(0.01, this.ctx.currentTime + 0.05);

      osc.connect(gain);
      gain.connect(this.ctx.destination);
      osc.start();
      osc.stop(this.ctx.currentTime + 0.05);
    }

    playReelStop() {
      if (!state.soundEnabled) return;
      this.init();
      if (!this.ctx) return;

      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();
      osc.type = 'triangle';
      osc.frequency.setValueAtTime(180, this.ctx.currentTime);
      osc.frequency.exponentialRampToValueAtTime(40, this.ctx.currentTime + 0.1);
      gain.gain.setValueAtTime(0.5, this.ctx.currentTime);
      gain.gain.linearRampToValueAtTime(0.01, this.ctx.currentTime + 0.1);

      osc.connect(gain);
      gain.connect(this.ctx.destination);
      osc.start();
      osc.stop(this.ctx.currentTime + 0.1);
    }

    playWinChime() {
      if (!state.soundEnabled) return;
      this.init();
      if (!this.ctx) return;

      const notes = [523.25, 659.25, 783.99, 1046.50]; // C5, E5, G5, C6
      notes.forEach((freq, idx) => {
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();
        const startTime = this.ctx.currentTime + idx * 0.08;

        osc.type = 'sine';
        osc.frequency.setValueAtTime(freq, startTime);
        gain.gain.setValueAtTime(0.3, startTime);
        gain.gain.exponentialRampToValueAtTime(0.001, startTime + 0.25);

        osc.connect(gain);
        gain.connect(this.ctx.destination);
        osc.start(startTime);
        osc.stop(startTime + 0.25);
      });
    }

    playJackpotFanfare() {
      if (!state.soundEnabled) return;
      this.init();
      if (!this.ctx) return;

      const freqs = [440, 554.37, 659.25, 880, 1108.73, 1318.51];
      freqs.forEach((freq, i) => {
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();
        const startTime = this.ctx.currentTime + i * 0.1;

        osc.type = 'triangle';
        osc.frequency.setValueAtTime(freq, startTime);
        gain.gain.setValueAtTime(0.4, startTime);
        gain.gain.exponentialRampToValueAtTime(0.001, startTime + 0.5);

        osc.connect(gain);
        gain.connect(this.ctx.destination);
        osc.start(startTime);
        osc.stop(startTime + 0.5);
      });
    }
  }

  const sfx = new SoundEffectsManager();

  // --- HELPER FUNCTIONS ---
  function getRandomSymbol() {
    const totalWeight = SYMBOLS.reduce((sum, s) => sum + s.weight, 0);
    let rand = Math.random() * totalWeight;
    for (const sym of SYMBOLS) {
      if (rand < sym.weight) return sym;
      rand -= sym.weight;
    }
    return SYMBOLS[0];
  }

  function getSymbolById(id) {
    return SYMBOLS.find(s => s.id === id) || SYMBOLS[0];
  }

  // Generate initial reel strips
  function buildReelStrip(stripElem) {
    stripElem.innerHTML = '';
    // Create 24 symbols for spinning buffer
    const list = [];
    for (let i = 0; i < 24; i++) {
      const sym = getRandomSymbol();
      list.push(sym);
      const cell = document.createElement('div');
      cell.className = 'symbol-cell';
      cell.dataset.symbolId = sym.id;

      const img = document.createElement('img');
      img.src = sym.image;
      img.alt = sym.name;
      cell.appendChild(img);
      stripElem.appendChild(cell);
    }
    return list;
  }

  // --- INITIALIZATION ---
  function initGame() {
    updateUI();
    dom.reelStrips.forEach(strip => buildReelStrip(strip));
    createBackgroundParticles();
    bindEvents();
    startSyncTimer();
  }

  function startSyncTimer() {
    const SLOT_CYCLE = 15;
    setInterval(() => {
      const nowSec = Math.floor(Date.now() / 1000);
      const cyclePos = nowSec % SLOT_CYCLE;
      const rem = SLOT_CYCLE - cyclePos;

      if (dom.btnSpin && !state.isSpinning) {
        dom.btnSpin.textContent = 'SPIN (' + rem + 's)';
      } else if (dom.btnSpin && state.isSpinning) {
        dom.btnSpin.textContent = 'SPINNING';
      }

      if (cyclePos === 0 && !state.isSpinning && state.balance >= state.totalBet) {
        spinReels();
      }
    }, 1000);
  }

  function createBackgroundParticles() {
    if (!dom.bgParticles) return;
    for (let i = 0; i < 15; i++) {
      const star = document.createElement('div');
      star.style.position = 'absolute';
      star.style.width = Math.random() * 3 + 2 + 'px';
      star.style.height = star.style.width;
      star.style.backgroundColor = Math.random() > 0.5 ? '#c084fc' : '#38bdf8';
      star.style.borderRadius = '50%';
      star.style.top = Math.random() * 100 + '%';
      star.style.left = Math.random() * 100 + '%';
      star.style.opacity = Math.random() * 0.6 + 0.2;
      star.style.boxShadow = '0 0 4px ' + star.style.backgroundColor;
      dom.bgParticles.appendChild(star);
    }
  }

  // --- UI UPDATE ---
  function updateUI() {
    state.totalBet = BET_OPTIONS[state.currentBetIndex];
    dom.balanceVal.textContent = state.balance;
    dom.totalBetVal.textContent = state.totalBet;
    dom.winVal.textContent = state.lastWin;
    dom.todaysWinVal.textContent = state.todaysWin;

    // Spin button disable condition if insufficient funds
    if (state.balance < state.totalBet && !state.isSpinning) {
      dom.btnSpin.classList.add('disabled');
    } else {
      dom.btnSpin.classList.remove('disabled');
    }

    // Auto button visual toggle
    if (state.isAutoSpin) {
      dom.btnAuto.classList.add('active');
      dom.autoBtnLabel.textContent = 'STOP';
    } else {
      dom.btnAuto.classList.remove('active');
      dom.autoBtnLabel.textContent = 'AUTO';
    }
  }

  function animateNumber(element, start, end, duration = 800) {
    let startTime = null;
    function step(timestamp) {
      if (!startTime) startTime = timestamp;
      const progress = Math.min((timestamp - startTime) / duration, 1);
      const val = Math.floor(progress * (end - start) + start);
      element.textContent = val;
      if (progress < 1) {
        requestAnimationFrame(step);
      } else {
        element.textContent = end;
      }
    }
    requestAnimationFrame(step);
  }

  // --- REEL SPINNING ENGINE ---
  function spinReels() {
    if (state.isSpinning) return;

    if (state.balance < state.totalBet) {
      stopAutoSpin();
      alert('Insufficient balance! Lower your bet or add more diamonds in Test Mode.');
      return;
    }

    // Deduct bet
    state.isSpinning = true;
    state.balance -= state.totalBet;
    state.lastWin = 0;
    updateUI();
    sfx.playClick();

    // Reset winning visual effects
    clearWinHighlights();

    // Determine target symbols for the 3 reels x 3 visible rows
    const targetOutcome = determineOutcome();

    // Perform spin animation on each reel with staggered stopping times
    const reelPromises = dom.reelStrips.map((stripElem, reelIdx) => {
      return animateSingleReel(stripElem, reelIdx, targetOutcome[reelIdx]);
    });

    Promise.all(reelPromises).then(() => {
      state.isSpinning = false;
      evaluateWin(targetOutcome);

      // Handle Auto Spin delay
      if (state.isAutoSpin) {
        if (state.balance >= state.totalBet) {
          setTimeout(() => {
            if (state.isAutoSpin) spinReels();
          }, 1200);
        } else {
          stopAutoSpin();
        }
      }
    });
  }

  function animateSingleReel(stripElem, reelIdx, targetSymbols) {
    return new Promise(resolve => {
      stripElem.classList.add('spinning');

      // Rebuild strip with target symbols at the top (positions 0, 1, 2)
      const cells = stripElem.querySelectorAll('.symbol-cell');

      // Randomize middle buffer cells
      for (let i = 3; i < cells.length; i++) {
        const randSym = getRandomSymbol();
        cells[i].dataset.symbolId = randSym.id;
        cells[i].querySelector('img').src = randSym.image;
      }

      // Set target symbols in positions 0, 1, 2
      for (let r = 0; r < 3; r++) {
        const sym = targetSymbols[r];
        cells[r].dataset.symbolId = sym.id;
        cells[r].querySelector('img').src = sym.image;
      }

      // Animate translate Y down
      const spinDuration = 1000 + reelIdx * 350; // Staggered delay (1s, 1.35s, 1.7s)
      const totalShift = (cells.length - 3) * SYMBOL_HEIGHT;

      stripElem.style.transition = 'none';
      stripElem.style.transform = `translate3d(0, -${totalShift}px, 0)`;

      // Force Reflow
      stripElem.offsetHeight;

      stripElem.style.transition = `transform ${spinDuration}ms cubic-bezier(0.12, 0.88, 0.25, 1.05)`;
      stripElem.style.transform = 'translate3d(0, 0, 0)';

      setTimeout(() => {
        stripElem.classList.remove('spinning');
        sfx.playReelStop();
        resolve();
      }, spinDuration);
    });
  }

  // --- OUTCOME DETERMINATION (NORMAL + TEST MODE) ---
  function determineOutcome() {
    let r0, r1, r2;

    if (state.testOutcome === 'seven_triple') {
      r0 = [getSymbolById('seven'), getSymbolById('seven'), getSymbolById('seven')];
      r1 = [getSymbolById('seven'), getSymbolById('seven'), getSymbolById('seven')];
      r2 = [getSymbolById('seven'), getSymbolById('seven'), getSymbolById('seven')];
    } else if (state.testOutcome === 'bell_triple') {
      r0 = [getSymbolById('bell'), getSymbolById('bell'), getSymbolById('bell')];
      r1 = [getSymbolById('bell'), getSymbolById('bell'), getSymbolById('bell')];
      r2 = [getSymbolById('bell'), getSymbolById('bell'), getSymbolById('bell')];
    } else if (state.testOutcome === 'grape_triple') {
      r0 = [getSymbolById('grape'), getSymbolById('grape'), getSymbolById('grape')];
      r1 = [getSymbolById('grape'), getSymbolById('grape'), getSymbolById('grape')];
      r2 = [getSymbolById('grape'), getSymbolById('grape'), getSymbolById('grape')];
    } else if (state.testOutcome === 'crown_triple') {
      r0 = [getSymbolById('crown'), getSymbolById('crown'), getSymbolById('crown')];
      r1 = [getSymbolById('crown'), getSymbolById('crown'), getSymbolById('crown')];
      r2 = [getSymbolById('crown'), getSymbolById('crown'), getSymbolById('crown')];
    } else if (state.testOutcome === 'cherry_pair') {
      r0 = [getSymbolById('cherry'), getSymbolById('cherry'), getSymbolById('cherry')];
      r1 = [getSymbolById('cherry'), getSymbolById('bell'), getSymbolById('cherry')];
      r2 = [getSymbolById('watermelon'), getSymbolById('seven'), getSymbolById('watermelon')];
    } else if (state.testOutcome === 'no_win') {
      r0 = [getSymbolById('cherry'), getSymbolById('bell'), getSymbolById('grape')];
      r1 = [getSymbolById('seven'), getSymbolById('crown'), getSymbolById('watermelon')];
      r2 = [getSymbolById('grape'), getSymbolById('cherry'), getSymbolById('bell')];
    } else {
      // Random calculation per reel
      r0 = [getRandomSymbol(), getRandomSymbol(), getRandomSymbol()];
      r1 = [getRandomSymbol(), getRandomSymbol(), getRandomSymbol()];
      r2 = [getRandomSymbol(), getRandomSymbol(), getRandomSymbol()];
    }

    return [r0, r1, r2];
  }

  // --- WIN EVALUATOR ---
  function evaluateWin(outcome) {
    let totalWin = 0;
    const lineBet = Math.max(1, Math.floor(state.totalBet / 5));
    const activeWinningLines = [];

    // Evaluate each payline
    PAYLINES.forEach(line => {
      const s0 = outcome[0][line.coords[0]];
      const s1 = outcome[1][line.coords[1]];
      const s2 = outcome[2][line.coords[2]];

      if (s0.id === s1.id && s1.id === s2.id) {
        // 3 of a kind match!
        const winAmt = s0.multiplier * lineBet;
        totalWin += winAmt;
        activeWinningLines.push({ lineId: line.id, coords: line.coords, sym: s0 });
      } else if (line.id === 'line-mid' && s0.id === s1.id) {
        // 2 match on middle line
        const winAmt = Math.floor(s0.multiplier * 0.4 * lineBet);
        if (winAmt > 0) {
          totalWin += winAmt;
          activeWinningLines.push({ lineId: line.id, coords: line.coords, sym: s0 });
        }
      }
    });

    state.lastWin = totalWin;
    state.todaysWin += totalWin;
    state.balance += totalWin;

    updateUI();

    if (totalWin > 0) {
      animateNumber(dom.winVal, 0, totalWin);
      highlightWinningLines(activeWinningLines);

      if (totalWin >= state.totalBet * 10) {
        sfx.playJackpotFanfare();
        showWinBanner('JACKPOT WIN!', totalWin);
      } else {
        sfx.playWinChime();
        showWinBanner('BIG WIN!', totalWin);
      }
    }

    // Log to history
    logSpinHistory(state.totalBet, totalWin);
  }

  function highlightWinningLines(winningLines) {
    winningLines.forEach(item => {
      // Activate SVG payline beam
      const svgLine = document.getElementById(item.lineId);
      if (svgLine) svgLine.classList.add('active');

      // Highlight corresponding symbol cells
      for (let reelIdx = 0; reelIdx < 3; reelIdx++) {
        const rowIdx = item.coords[reelIdx];
        const stripElem = dom.reelStrips[reelIdx];
        const cells = stripElem.querySelectorAll('.symbol-cell');
        if (cells[rowIdx]) {
          cells[rowIdx].classList.add('win-highlight');
        }
      }
    });
  }

  function clearWinHighlights() {
    document.querySelectorAll('.payline').forEach(line => line.classList.remove('active'));
    document.querySelectorAll('.symbol-cell').forEach(cell => cell.classList.remove('win-highlight'));
    dom.winBanner.classList.add('hidden');
  }

  function showWinBanner(title, amount) {
    dom.winBannerTitle.textContent = title;
    dom.winBannerAmount.textContent = '+' + amount;
    dom.winBanner.classList.remove('hidden');

    setTimeout(() => {
      dom.winBanner.classList.add('hidden');
    }, 2200);
  }

  function logSpinHistory(bet, win) {
    const entry = {
      id: Date.now(),
      bet,
      win,
      net: win - bet,
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })
    };
    state.spinHistory.unshift(entry);
    if (state.spinHistory.length > 20) state.spinHistory.pop();
    renderHistory();
  }

  function renderHistory() {
    if (!dom.historyList) return;
    if (state.spinHistory.length === 0) {
      dom.historyList.innerHTML = '<p class="empty-history">No spins logged yet. Hit SPIN to play!</p>';
      return;
    }

    dom.historyList.innerHTML = state.spinHistory.map(item => `
      <div class="history-item">
        <span>[${item.time}] Bet: ${item.bet}</span>
        <span style="color: ${item.win > 0 ? '#4ade80' : '#f87171'}; font-weight: 800;">
          ${item.win > 0 ? '+' + item.win : '- ' + item.bet}
        </span>
      </div>
    `).join('');
  }

  function stopAutoSpin() {
    state.isAutoSpin = false;
    updateUI();
  }

  // --- EVENT BINDINGS ---
  function bindEvents() {
    // Spin button
    dom.btnSpin.addEventListener('click', () => {
      if (!state.isSpinning) spinReels();
    });

    // Bet button (Cycles options)
    dom.btnBet.addEventListener('click', () => {
      if (state.isSpinning) return;
      sfx.playClick();
      state.currentBetIndex = (state.currentBetIndex + 1) % BET_OPTIONS.length;
      updateUI();
    });

    // Bet + button (Steps increment up to max)
    dom.btnBetPlus.addEventListener('click', () => {
      if (state.isSpinning) return;
      sfx.playClick();
      if (state.currentBetIndex < BET_OPTIONS.length - 1) {
        state.currentBetIndex++;
      } else {
        state.currentBetIndex = 0;
      }
      updateUI();
    });

    // Auto Spin button
    dom.btnAuto.addEventListener('click', () => {
      if (state.isSpinning && !state.isAutoSpin) return;
      sfx.playClick();
      state.isAutoSpin = !state.isAutoSpin;
      updateUI();

      if (state.isAutoSpin && !state.isSpinning) {
        spinReels();
      }
    });

    // Sound toggle
    dom.btnSound.addEventListener('click', () => {
      state.soundEnabled = !state.soundEnabled;
      dom.soundIconImg.src = state.soundEnabled ? 'assets/icon_sound.svg' : 'assets/icon_sound_off.svg';
      sfx.playClick();
    });

    // Modals open/close
    dom.btnHelp.addEventListener('click', () => {
      sfx.playClick();
      dom.modalHelp.classList.remove('hidden');
    });
    dom.modalHelpClose.addEventListener('click', () => dom.modalHelp.classList.add('hidden'));

    dom.btnHistory.addEventListener('click', () => {
      sfx.playClick();
      renderHistory();
      dom.modalHistory.classList.remove('hidden');
    });
    dom.modalHistoryClose.addEventListener('click', () => dom.modalHistory.classList.add('hidden'));

    dom.btnTestMode.addEventListener('click', () => {
      sfx.playClick();
      dom.modalTest.classList.remove('hidden');
    });
    dom.modalTestClose.addEventListener('click', () => dom.modalTest.classList.add('hidden'));

    dom.btnClose.addEventListener('click', () => {
      sfx.playClick();
      if (confirm('Are you sure you want to exit the Slot Game?')) {
        window.location.reload();
      }
    });

    // Test mode controls
    dom.testResultSelect.addEventListener('change', (e) => {
      state.testOutcome = e.target.value;
    });

    dom.btnAddDiamonds.addEventListener('click', () => {
      state.balance += 100;
      updateUI();
      sfx.playClick();
    });

    dom.btnResetBalance.addEventListener('click', () => {
      state.balance = 174;
      state.todaysWin = 0;
      state.lastWin = 0;
      updateUI();
      sfx.playClick();
    });

    // Close modals when clicking overlay backdrop
    [dom.modalHelp, dom.modalHistory, dom.modalTest].forEach(modal => {
      modal.addEventListener('click', (e) => {
        if (e.target === modal) modal.classList.add('hidden');
      });
    });
  }

  // Run on DOM ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initGame);
  } else {
    initGame();
  }

})();
