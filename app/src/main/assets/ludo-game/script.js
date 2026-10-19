/**
 * 4v4 Ludo King - Perfectly Centered Tokens & Moving Dice Edition
 * Isolated IIFE Scope
 */
(function () {
  'use strict';

  // --- GAME CONSTANTS ---
  const COLORS = ['green', 'blue', 'yellow', 'red'];

  // Start Indices on the 52-step Main Track
  const START_MAIN_INDEX = {
    green: 0,
    blue: 13,
    yellow: 26,
    red: 39
  };

  // Safe Square Indices on the 52-step Main Track
  const SAFE_INDICES = [0, 8, 13, 21, 26, 34, 39, 47];

  // 52-Step Main Track Coordinates (Row 1..15, Col 1..15)
  const MAIN_TRACK = [
    { r: 7, c: 2 },  { r: 7, c: 3 },  { r: 7, c: 4 },  { r: 7, c: 5 },  { r: 7, c: 6 },  // 0-4 (Green Start = 0)
    { r: 6, c: 7 },  { r: 5, c: 7 },  { r: 4, c: 7 },  { r: 3, c: 7 },  { r: 2, c: 7 },  { r: 1, c: 7 },  // 5-10 (Star = 8)
    { r: 1, c: 8 },  // 11
    { r: 1, c: 9 },  { r: 2, c: 9 },  { r: 3, c: 9 },  { r: 4, c: 9 },  { r: 5, c: 9 },  { r: 6, c: 9 },  // 12-17 (Blue Start = 13)
    { r: 7, c: 10 }, { r: 7, c: 11 }, { r: 7, c: 12 }, { r: 7, c: 13 }, { r: 7, c: 14 }, { r: 7, c: 15 }, // 18-23 (Star = 21)
    { r: 8, c: 15 }, // 24
    { r: 9, c: 15 }, { r: 9, c: 14 }, { r: 9, c: 13 }, { r: 9, c: 12 }, { r: 9, c: 11 }, { r: 9, c: 10 }, // 25-30 (Yellow Start = 26)
    { r: 10, c: 9 }, { r: 11, c: 9 }, { r: 12, c: 9 }, { r: 13, c: 9 }, { r: 14, c: 9 }, { r: 15, c: 9 }, // 31-36 (Star = 34)
    { r: 15, c: 8 }, // 37
    { r: 15, c: 7 }, { r: 14, c: 7 }, { r: 13, c: 7 }, { r: 12, c: 7 }, { r: 11, c: 7 }, { r: 10, c: 7 }, // 38-43 (Red Start = 39)
    { r: 9, c: 6 },  { r: 9, c: 5 },  { r: 9, c: 4 },  { r: 9, c: 3 },  { r: 9, c: 2 },  { r: 9, c: 1 },  // 44-49 (Star = 47)
    { r: 8, c: 1 },  // 50
    { r: 7, c: 1 }   // 51
  ];

  // Home Paths (5 steps leading to Finish)
  const HOME_PATHS = {
    green:  [{ r: 8, c: 2 },  { r: 8, c: 3 },  { r: 8, c: 4 },  { r: 8, c: 5 },  { r: 8, c: 6 }],
    blue:   [{ r: 2, c: 8 },  { r: 3, c: 8 },  { r: 4, c: 8 },  { r: 5, c: 8 },  { r: 6, c: 8 }],
    yellow: [{ r: 8, c: 14 }, { r: 8, c: 13 }, { r: 8, c: 12 }, { r: 8, c: 11 }, { r: 8, c: 10 }],
    red:    [{ r: 14, c: 8 }, { r: 13, c: 8 }, { r: 12, c: 8 }, { r: 11, c: 8 }, { r: 10, c: 8 }]
  };

  // Center Finish Coordinates (Step 56)
  const FINISH_COORDS = {
    green:  { r: 8, c: 7 },
    blue:   { r: 7, c: 8 },
    yellow: { r: 8, c: 9 },
    red:    { r: 9, c: 8 }
  };

  // Yard Socket Coordinates (Inside Home Bases for 4 tokens)
  const YARD_SOCKETS = {
    green:  [{ r: 2, c: 2 },  { r: 2, c: 5 },  { r: 5, c: 2 },  { r: 5, c: 5 }],
    blue:   [{ r: 2, c: 11 }, { r: 2, c: 14 }, { r: 5, c: 11 }, { r: 5, c: 14 }],
    yellow: [{ r: 11, c: 11 },{ r: 11, c: 14 },{ r: 14, c: 11 },{ r: 14, c: 14 }],
    red:    [{ r: 11, c: 2 }, { r: 11, c: 5 }, { r: 14, c: 2 }, { r: 14, c: 5 }]
  };

  // --- AUDIO SYNTHESIZER ---
  class SoundManager {
    constructor() {
      this.enabled = true;
      this.ctx = null;
    }

    init() {
      if (!this.ctx && (window.AudioContext || window.webkitAudioContext)) {
        this.ctx = new (window.AudioContext || window.webkitAudioContext)();
      }
    }

    playRoll() {
      if (!this.enabled) return;
      this.init();
      if (!this.ctx) return;
      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();
      osc.type = 'triangle';
      osc.frequency.setValueAtTime(220, this.ctx.currentTime);
      osc.frequency.exponentialRampToValueAtTime(650, this.ctx.currentTime + 0.15);
      gain.gain.setValueAtTime(0.2, this.ctx.currentTime);
      gain.gain.linearRampToValueAtTime(0.01, this.ctx.currentTime + 0.15);
      osc.connect(gain);
      gain.connect(this.ctx.destination);
      osc.start();
      osc.stop(this.ctx.currentTime + 0.15);
    }

    playStep() {
      if (!this.enabled) return;
      this.init();
      if (!this.ctx) return;
      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();
      osc.type = 'sine';
      osc.frequency.setValueAtTime(440, this.ctx.currentTime);
      gain.gain.setValueAtTime(0.15, this.ctx.currentTime);
      gain.gain.linearRampToValueAtTime(0.01, this.ctx.currentTime + 0.08);
      osc.connect(gain);
      gain.connect(this.ctx.destination);
      osc.start();
      osc.stop(this.ctx.currentTime + 0.08);
    }

    playCapture() {
      if (!this.enabled) return;
      this.init();
      if (!this.ctx) return;
      const osc = this.ctx.createOscillator();
      const gain = this.ctx.createGain();
      osc.type = 'sawtooth';
      osc.frequency.setValueAtTime(800, this.ctx.currentTime);
      osc.frequency.exponentialRampToValueAtTime(160, this.ctx.currentTime + 0.3);
      gain.gain.setValueAtTime(0.3, this.ctx.currentTime);
      gain.gain.linearRampToValueAtTime(0.01, this.ctx.currentTime + 0.3);
      osc.connect(gain);
      gain.connect(this.ctx.destination);
      osc.start();
      osc.stop(this.ctx.currentTime + 0.3);
    }

    playSix() {
      if (!this.enabled) return;
      this.init();
      if (!this.ctx) return;
      const now = this.ctx.currentTime;
      [523.25, 659.25, 783.99].forEach((freq, idx) => {
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();
        osc.type = 'sine';
        osc.frequency.setValueAtTime(freq, now + idx * 0.08);
        gain.gain.setValueAtTime(0.2, now + idx * 0.08);
        gain.gain.linearRampToValueAtTime(0.01, now + idx * 0.08 + 0.12);
        osc.connect(gain);
        gain.connect(this.ctx.destination);
        osc.start(now + idx * 0.08);
        osc.stop(now + idx * 0.08 + 0.12);
      });
    }

    playVictory() {
      if (!this.enabled) return;
      this.init();
      if (!this.ctx) return;
      const now = this.ctx.currentTime;
      const notes = [523.25, 659.25, 783.99, 1046.50];
      notes.forEach((freq, idx) => {
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();
        osc.type = 'triangle';
        osc.frequency.setValueAtTime(freq, now + idx * 0.12);
        gain.gain.setValueAtTime(0.3, now + idx * 0.12);
        gain.gain.linearRampToValueAtTime(0.01, now + idx * 0.12 + 0.25);
        osc.connect(gain);
        gain.connect(this.ctx.destination);
        osc.start(now + idx * 0.12);
        osc.stop(now + idx * 0.12 + 0.25);
      });
    }
  }

  // --- GAME STATE CLASS ---
  class LudoGame {
    constructor() {
      this.sound = new SoundManager();
      this.turnIndex = 0; // 0=green, 1=blue, 2=yellow, 3=red
      this.diceValue = 6;
      this.phase = 'WAIT_ROLL'; // 'WAIT_ROLL', 'WAIT_MOVE', 'GAME_OVER'
      this.consecutiveSixes = 0;
      this.grantExtraRoll = false;
      this.isRolling = false;
      this.autoMoveSingle = true;
      this.devMode = false;
      this.forcedRoll = null;

      this.isOnlineActive = false;
      this.lastProcessedTimestamp = 0;

      // Player Configurations: Yellow = YOU (Human), Green/Blue/Red = BOTs
      this.players = {
        green:  { isAi: true,  finishedCount: 0, rank: null },
        blue:   { isAi: true,  finishedCount: 0, rank: null },
        yellow: { isAi: false, finishedCount: 0, rank: null },
        red:    { isAi: true,  finishedCount: 0, rank: null }
      };

      this.finishedRankList = [];

      // Tokens: 16 total (4 per color)
      this.tokens = {};
      COLORS.forEach(color => {
        this.tokens[color] = [0, 1, 2, 3].map(id => ({
          color: color,
          id: id,
          position: -1
        }));
      });

      this.initDomReferences();
      this.buildBoardDom();
      this.bindEvents();
      this.onlineManager = new LudoOnlineManager(this);
      this.updateUiState();

      // Initial position for dynamic floating dice
      setTimeout(() => {
        this.positionDice(this.getCurrentColor());
      }, 80);
      setTimeout(() => {
        this.positionDice(this.getCurrentColor());
      }, 300);

      // Trigger AI roll if player 0 is AI
      if (this.players[this.getCurrentColor()].isAi) {
        if (this.btnRoll) this.btnRoll.disabled = true;
        setTimeout(() => this.rollDice(), 800);
      }
    }

    initDomReferences() {
      this.viewportEl = document.getElementById('game-viewport');
      this.boardEl = document.getElementById('ludo-board');
      this.turnBanner = document.getElementById('turn-banner');
      this.turnText = document.getElementById('turn-text');
      this.extraRollBadge = document.getElementById('extra-roll-badge');
      this.diceBox = document.getElementById('dice-box');
      this.dice3d = document.getElementById('dice-3d');
      this.btnRoll = document.getElementById('btn-roll');
      this.btnBack = document.getElementById('btn-back');
      this.statusMsg = document.getElementById('status-message');
      this.toastMsg = document.getElementById('toast-message');

      // Modals
      this.modalSettings = document.getElementById('modal-settings');
      this.modalRules = document.getElementById('modal-rules');
      this.modalWinner = document.getElementById('modal-winner');

      // Settings elements
      this.selectGameMode = document.getElementById('select-game-mode');
      this.checkDevMode = document.getElementById('check-dev-mode');
      this.checkAutoMove = document.getElementById('check-auto-move');
      this.devTestPanel = document.getElementById('dev-test-panel');
    }

    // Dynamic Position Calculator for Active Player Dice with Board Safety
    positionDice(color) {
      if (!this.diceBox || !this.viewportEl) return;
      const activeColor = color || this.getCurrentColor();

      const profileCard = document.getElementById(`profile-${activeColor}`);
      if (!profileCard) return;

      const boardWrapper = document.querySelector('.wood-frame') || document.querySelector('.board-wrapper');
      const anchor = profileCard.querySelector('.dice-anchor') || profileCard;
      const viewportRect = this.viewportEl.getBoundingClientRect();
      const anchorRect = anchor.getBoundingClientRect();

      const diceWidth = this.diceBox.offsetWidth || 48;
      const diceHeight = this.diceBox.offsetHeight || 48;

      let x = anchorRect.left - viewportRect.left + (anchorRect.width / 2) - (diceWidth / 2);
      let y = anchorRect.top - viewportRect.top + (anchorRect.height / 2) - (diceHeight / 2);

      // Prevent ANY overlap with Ludo Board Frame
      if (boardWrapper) {
        const boardRect = boardWrapper.getBoundingClientRect();
        const boardTopInViewport = boardRect.top - viewportRect.top;
        const boardBottomInViewport = boardRect.bottom - viewportRect.top;

        // Bottom players (Red & Yellow): Dice MUST stay strictly below the board
        if (activeColor === 'red' || activeColor === 'yellow') {
          const minY = boardBottomInViewport + 4; // 4px safe margin below board
          if (y < minY) {
            y = minY;
          }
        }
        // Top players (Green & Blue): Dice MUST stay strictly above the board
        else if (activeColor === 'green' || activeColor === 'blue') {
          const maxY = boardTopInViewport - 4 - diceHeight; // 4px safe margin above board
          if (y > maxY) {
            y = maxY;
          }
        }
      }

      // Horizontal boundary safety inside viewport
      const maxViewportX = viewportRect.width - diceWidth - 4;
      if (x < 4) x = 4;
      if (x > maxViewportX) x = maxViewportX;

      this.diceBox.style.transform = `translate3d(${Math.round(x)}px, ${Math.round(y)}px, 0)`;

      // Active glow for human player's turn to roll
      if (!this.players[activeColor].isAi && this.phase === 'WAIT_ROLL') {
        this.diceBox.classList.add('glow-active');
      } else {
        this.diceBox.classList.remove('glow-active');
      }
    }

    // Render 15x15 Board Grid
    buildBoardDom() {
      this.boardEl.innerHTML = '';

      COLORS.forEach(color => {
        const yardEl = document.createElement('div');
        yardEl.className = `yard yard-${color}`;

        const boxEl = document.createElement('div');
        boxEl.className = 'yard-box';

        for (let i = 0; i < 4; i++) {
          const socketEl = document.createElement('div');
          socketEl.className = `yard-socket socket-${color}`;
          socketEl.id = `socket-${color}-${i}`;
          boxEl.appendChild(socketEl);
        }

        yardEl.appendChild(boxEl);
        this.boardEl.appendChild(yardEl);
      });

      for (let r = 1; r <= 15; r++) {
        for (let c = 1; c <= 15; c++) {
          if ((r <= 6 && c <= 6) || (r <= 6 && c >= 10) || (r >= 10 && c >= 10) || (r >= 10 && c <= 6)) {
            continue;
          }
          if (r >= 7 && r <= 9 && c >= 7 && c <= 9) {
            continue;
          }

          const cellEl = document.createElement('div');
          cellEl.className = 'cell';
          cellEl.style.gridArea = `${r} / ${c} / ${r + 1} / ${c + 1}`;
          cellEl.dataset.r = r;
          cellEl.dataset.c = c;

          if (r === 8 && c >= 2 && c <= 6) cellEl.classList.add('cell-green');
          else if (c === 8 && r >= 2 && r <= 6) cellEl.classList.add('cell-blue');
          else if (r === 8 && c >= 10 && c <= 14) cellEl.classList.add('cell-yellow');
          else if (c === 8 && r >= 10 && r <= 14) cellEl.classList.add('cell-red');
          else if (r === 7 && c === 2) { cellEl.classList.add('cell-green', 'cell-arrow'); cellEl.setAttribute('data-arrow', '→'); }
          else if (r === 2 && c === 9) { cellEl.classList.add('cell-blue', 'cell-arrow'); cellEl.setAttribute('data-arrow', '↓'); }
          else if (r === 9 && c === 14) { cellEl.classList.add('cell-yellow', 'cell-arrow'); cellEl.setAttribute('data-arrow', '←'); }
          else if (r === 14 && c === 7) { cellEl.classList.add('cell-red', 'cell-arrow'); cellEl.setAttribute('data-arrow', '↑'); }
          else cellEl.classList.add('path-white');

          const isSafeStar = (r === 9 && c === 3) || (r === 3 && c === 7) || (r === 7 && c === 13) || (r === 13 && c === 9);
          if (isSafeStar) {
            cellEl.classList.add('cell-safe');
          }

          this.boardEl.appendChild(cellEl);
        }
      }

      const finishEl = document.createElement('div');
      finishEl.className = 'center-finish';
      finishEl.innerHTML = `
        <div class="tri-green"></div>
        <div class="tri-blue"></div>
        <div class="tri-yellow"></div>
        <div class="tri-red"></div>
        <div class="finish-circle circle-green" id="count-green">0</div>
        <div class="finish-circle circle-blue" id="count-blue">0</div>
        <div class="finish-circle circle-yellow" id="count-yellow">0</div>
        <div class="finish-circle circle-red" id="count-red">0</div>
      `;
      this.boardEl.appendChild(finishEl);

      COLORS.forEach(color => {
        for (let i = 0; i < 4; i++) {
          const tokenEl = document.createElement('div');
          tokenEl.className = `token token-${color}`;
          tokenEl.id = `token-${color}-${i}`;
          tokenEl.dataset.color = color;
          tokenEl.dataset.id = i;
          tokenEl.innerHTML = `<img src="assets/token_${color}.svg" alt="${color} token">`;
          this.boardEl.appendChild(tokenEl);
        }
      });

      this.renderTokens();
    }

    getTokenCoordinates(token) {
      if (token.position === -1) {
        return YARD_SOCKETS[token.color][token.id];
      }
      if (token.position === 56) {
        return FINISH_COORDS[token.color];
      }
      if (token.position >= 51 && token.position <= 55) {
        const homeIdx = token.position - 51;
        return HOME_PATHS[token.color][homeIdx];
      }

      const startIdx = START_MAIN_INDEX[token.color];
      const mainIdx = (startIdx + token.position) % 52;
      return MAIN_TRACK[mainIdx];
    }

    renderTokens() {
      const tileOccupancy = {};

      COLORS.forEach(color => {
        this.tokens[color].forEach(token => {
          if (token.position !== -1) {
            const coords = this.getTokenCoordinates(token);
            const key = `${coords.r}_${coords.c}`;
            if (!tileOccupancy[key]) tileOccupancy[key] = [];
            tileOccupancy[key].push(token);
          }
        });
      });

      COLORS.forEach(color => {
        this.tokens[color].forEach(token => {
          const tokenEl = document.getElementById(`token-${color}-${token.id}`);
          if (!tokenEl) return;

          if (token.position === -1) {
            // Token is in Home Yard Socket: Append directly inside socket circle
            const socketEl = document.getElementById(`socket-${color}-${token.id}`);
            if (socketEl && tokenEl.parentElement !== socketEl) {
              socketEl.appendChild(tokenEl);
            }
            tokenEl.style.gridArea = 'none';
            tokenEl.style.transform = 'translate(-50%, -50%)';
          } else {
            // Token is on Main Board Track / Home Path
            if (tokenEl.parentElement !== this.boardEl) {
              this.boardEl.appendChild(tokenEl);
            }
            const coords = this.getTokenCoordinates(token);
            const key = `${coords.r}_${coords.c}`;
            const stack = tileOccupancy[key] || [];

            tokenEl.style.gridArea = `${coords.r} / ${coords.c} / ${coords.r + 1} / ${coords.c + 1}`;

            if (stack.length > 1 && token.position !== 56) {
              const indexInStack = stack.indexOf(token);
              const offset = (indexInStack - (stack.length - 1) / 2) * 5;
              tokenEl.style.transform = `translate(calc(-50% + ${offset}px), calc(-50% + ${offset}px)) scale(0.9)`;
            } else {
              tokenEl.style.transform = 'translate(-50%, -50%)';
            }

            const existingBadge = tokenEl.querySelector('.token-stack-badge');
            if (existingBadge) existingBadge.remove();

            if (stack.length > 1 && stack[0] === token && token.position !== 56) {
              const badge = document.createElement('div');
              badge.className = 'token-stack-badge';
              badge.innerText = stack.length;
              tokenEl.appendChild(badge);
            }
          }
        });
      });
    }

    bindEvents() {
      const triggerRoll = () => {
        if (this.phase === 'WAIT_ROLL' && !this.isRolling && !this.players[this.getCurrentColor()].isAi) {
          this.rollDice();
        }
      };

      if (this.btnRoll) this.btnRoll.addEventListener('click', triggerRoll);
      if (this.diceBox) this.diceBox.addEventListener('click', triggerRoll);

      if (this.btnBack) {
        this.btnBack.addEventListener('click', () => {
          this.showToast("Closing Game...");
          setTimeout(() => {
            if (window.history && window.history.length > 1) {
              window.history.back();
            }
          }, 300);
        });
      }

      window.addEventListener('resize', () => {
        this.positionDice(this.getCurrentColor());
      });

      this.boardEl.addEventListener('click', (e) => {
        const tokenEl = e.target.closest('.token');
        if (!tokenEl) return;
        const color = tokenEl.dataset.color;
        const id = parseInt(tokenEl.dataset.id, 10);

        if (color === this.getCurrentColor() && this.phase === 'WAIT_MOVE') {
          this.onTokenSelected(color, id);
        }
      });

      document.getElementById('btn-sound').addEventListener('click', () => {
        this.sound.enabled = !this.sound.enabled;
        const img = document.getElementById('img-sound');
        img.src = this.sound.enabled ? 'assets/icon_sound_on.svg' : 'assets/icon_sound_off.svg';
      });

      document.querySelectorAll('.modal-close').forEach(btn => {
        btn.addEventListener('click', (e) => {
          e.target.closest('.modal-overlay').classList.add('hidden');
        });
      });

      const btnFind = document.getElementById('btn-find-friends');
      if (btnFind) {
        btnFind.addEventListener('click', () => {
          if (this.onlineManager) this.onlineManager.startMatchmaking();
        });
      }

      document.getElementById('btn-restart-game').addEventListener('click', () => {
        this.modalSettings.classList.add('hidden');
        this.restartGame();
      });

      document.getElementById('btn-play-again').addEventListener('click', () => {
        this.modalWinner.classList.add('hidden');
        this.restartGame();
      });

      this.selectGameMode.addEventListener('change', (e) => {
        const mode = e.target.value;
        if (mode === 'online_4p') {
          this.modalSettings.classList.add('hidden');
          if (this.onlineManager) this.onlineManager.startMatchmaking();
        } else if (mode === 'vs_ai') {
          this.isOnlineActive = false;
          this.players = {
            green:  { isAi: true },
            blue:   { isAi: true },
            yellow: { isAi: false },
            red:    { isAi: true }
          };
        } else if (mode === 'pass_play') {
          this.isOnlineActive = false;
          this.players = {
            green:  { isAi: false },
            blue:   { isAi: false },
            yellow: { isAi: false },
            red:    { isAi: false }
          };
        }
      });

      this.checkDevMode.addEventListener('change', (e) => {
        this.devMode = e.target.checked;
        if (this.devMode) {
          this.devTestPanel.classList.remove('hidden');
        } else {
          this.devTestPanel.classList.add('hidden');
          this.forcedRoll = null;
        }
      });

      document.querySelectorAll('.dev-dice-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
          this.forcedRoll = parseInt(e.target.dataset.val, 10);
          this.showToast(`Dev Force Roll set to: ${this.forcedRoll}`);
        });
      });

      this.checkAutoMove.addEventListener('change', (e) => {
        this.autoMoveSingle = e.target.checked;
      });
    }

    getCurrentColor() {
      return COLORS[this.turnIndex];
    }

    rollDice() {
      if (this.isRolling || this.phase !== 'WAIT_ROLL') return;

      this.isRolling = true;
      if (this.btnRoll) this.btnRoll.disabled = true;
      if (this.diceBox) this.diceBox.classList.remove('glow-active');

      this.sound.playRoll();
      this.dice3d.classList.add('dice-rolling');

      setTimeout(() => {
        this.dice3d.classList.remove('dice-rolling');

        if (this.forcedRoll) {
          this.diceValue = this.forcedRoll;
        } else {
          this.diceValue = Math.floor(Math.random() * 6) + 1;
        }

        this.dice3d.className = `dice-3d show-${this.diceValue}`;
        this.isRolling = false;

        if (this.isOnlineActive && this.onlineManager) {
          this.onlineManager.sendAction('ROLL', { diceVal: this.diceValue, color: this.getCurrentColor() });
        }

        this.handleDiceResult();
      }, 500);
    }

    handleDiceResult() {
      const currentColor = this.getCurrentColor();

      if (this.diceValue === 6) {
        this.consecutiveSixes++;
        this.sound.playSix();
      } else {
        this.consecutiveSixes = 0;
      }

      if (this.consecutiveSixes === 3) {
        this.showToast("3 Sixes in a row! Turn forfeited.");
        this.consecutiveSixes = 0;
        setTimeout(() => this.nextTurn(), 1200);
        return;
      }

      const validTokens = this.getValidMoveTokens(currentColor, this.diceValue);

      if (validTokens.length === 0) {
        this.statusMsg.innerText = `Rolled a ${this.diceValue}. No valid moves!`;
        setTimeout(() => this.nextTurn(), 1000);
      } else {
        this.phase = 'WAIT_MOVE';
        this.statusMsg.innerText = `Rolled a ${this.diceValue}! Select a token.`;

        this.clearTokenHighlights();
        validTokens.forEach(t => {
          const el = document.getElementById(`token-${currentColor}-${t.id}`);
          if (el) el.classList.add('token-movable');
        });

        if (this.players[currentColor].isAi) {
          setTimeout(() => this.makeAiMove(validTokens), 600);
        } else if (validTokens.length === 1 && this.autoMoveSingle) {
          setTimeout(() => this.onTokenSelected(currentColor, validTokens[0].id), 400);
        }
      }
    }

    getValidMoveTokens(color, roll) {
      return this.tokens[color].filter(token => {
        if (token.position === -1) {
          return roll === 6;
        }
        if (token.position === 56) {
          return false;
        }
        const targetPos = token.position + roll;
        return targetPos <= 56;
      });
    }

    clearTokenHighlights() {
      document.querySelectorAll('.token-movable').forEach(el => {
        el.classList.remove('token-movable');
      });
    }

    onTokenSelected(color, tokenId) {
      if (this.phase !== 'WAIT_MOVE' || color !== this.getCurrentColor()) return;

      const token = this.tokens[color][tokenId];
      const validTokens = this.getValidMoveTokens(color, this.diceValue);

      if (!validTokens.some(t => t.id === tokenId)) {
        return;
      }

      if (this.isOnlineActive && this.onlineManager) {
        this.onlineManager.sendAction('MOVE', { color: color, tokenId: tokenId });
      }

      this.clearTokenHighlights();
      this.phase = 'ANIMATING';

      if (token.position === -1 && this.diceValue === 6) {
        token.position = 0;
        this.sound.playStep();
        this.renderTokens();
        this.postMoveCheck(token, true);
      } else {
        this.animateStepByStep(token, this.diceValue, () => {
          const captured = this.checkCapture(token);
          const reachedHome = token.position === 56;

          if (reachedHome) {
            this.players[color].finishedCount++;
            const countEl = document.getElementById(`count-${color}`);
            if (countEl) countEl.innerText = this.players[color].finishedCount;

            this.sound.playVictory();
            this.showToast(`${color.toUpperCase()} token reached Home!`);
            this.checkWinCondition(color);
          }

          const extraRoll = (this.diceValue === 6) || captured || reachedHome;
          this.postMoveCheck(token, extraRoll);
        });
      }
    }

    animateStepByStep(token, steps, callback) {
      let currentStep = 0;
      const interval = setInterval(() => {
        token.position++;
        currentStep++;
        this.sound.playStep();
        this.renderTokens();

        if (currentStep >= steps) {
          clearInterval(interval);
          setTimeout(callback, 150);
        }
      }, 180);
    }

    checkCapture(movedToken) {
      if (movedToken.position < 0 || movedToken.position > 50) {
        return false;
      }

      const startIdx = START_MAIN_INDEX[movedToken.color];
      const movedMainIdx = (startIdx + movedToken.position) % 52;

      if (SAFE_INDICES.includes(movedMainIdx)) {
        return false;
      }

      let capturedAny = false;

      COLORS.forEach(otherColor => {
        if (otherColor === movedToken.color) return;

        this.tokens[otherColor].forEach(otherToken => {
          if (otherToken.position >= 0 && otherToken.position <= 50) {
            const otherStartIdx = START_MAIN_INDEX[otherColor];
            const otherMainIdx = (otherStartIdx + otherToken.position) % 52;

            if (otherMainIdx === movedMainIdx) {
              otherToken.position = -1;
              capturedAny = true;
              this.sound.playCapture();
              this.showToast(`CAPTURED! ${otherColor.toUpperCase()} sent back to Yard!`);
            }
          }
        });
      });

      if (capturedAny) {
        this.renderTokens();
      }

      return capturedAny;
    }

    postMoveCheck(token, grantExtraRoll) {
      if (grantExtraRoll && this.players[this.getCurrentColor()].finishedCount < 4) {
        this.grantExtraRoll = true;
        this.phase = 'WAIT_ROLL';
        this.statusMsg.innerText = "EXTRA ROLL granted! Tap Dice.";
        this.extraRollBadge.classList.remove('hidden');

        this.positionDice(this.getCurrentColor());

        if (this.players[this.getCurrentColor()].isAi) {
          setTimeout(() => this.rollDice(), 800);
        } else {
          if (this.btnRoll) this.btnRoll.disabled = false;
        }
      } else {
        this.grantExtraRoll = false;
        this.extraRollBadge.classList.add('hidden');
        this.nextTurn();
      }
    }

    makeAiMove(validTokens) {
      if (validTokens.length === 0) return;

      let chosenToken = validTokens[0];
      let highestScore = -999;

      validTokens.forEach(token => {
        let score = 0;
        const targetPos = token.position === -1 ? 0 : token.position + this.diceValue;

        if (targetPos <= 50) {
          const startIdx = START_MAIN_INDEX[token.color];
          const targetMainIdx = (startIdx + targetPos) % 52;
          if (!SAFE_INDICES.includes(targetMainIdx)) {
            COLORS.forEach(oc => {
              if (oc !== token.color) {
                this.tokens[oc].forEach(ot => {
                  if (ot.position >= 0 && ot.position <= 50) {
                    const oMain = (START_MAIN_INDEX[oc] + ot.position) % 52;
                    if (oMain === targetMainIdx) score += 100;
                  }
                });
              }
            });
          }
        }

        if (targetPos === 56) score += 80;
        if (token.position === -1 && this.diceValue === 6) score += 50;
        if (targetPos <= 50) {
          const mainIdx = (START_MAIN_INDEX[token.color] + targetPos) % 52;
          if (SAFE_INDICES.includes(mainIdx)) score += 30;
        }
        score += token.position;

        if (score > highestScore) {
          highestScore = score;
          chosenToken = token;
        }
      });

      this.onTokenSelected(chosenToken.color, chosenToken.id);
    }

    nextTurn() {
      if (this.phase === 'GAME_OVER') return;

      this.extraRollBadge.classList.add('hidden');
      this.consecutiveSixes = 0;

      let attempts = 0;
      do {
        this.turnIndex = (this.turnIndex + 1) % 4;
        attempts++;
      } while (this.players[this.getCurrentColor()].finishedCount === 4 && attempts < 4);

      if (attempts >= 4) {
        this.endGame();
        return;
      }

      this.phase = 'WAIT_ROLL';
      this.updateUiState();

      if (this.players[this.getCurrentColor()].isAi) {
        if (this.btnRoll) this.btnRoll.disabled = true;
        setTimeout(() => this.rollDice(), 800);
      } else {
        if (this.btnRoll) this.btnRoll.disabled = false;
      }
    }

    checkWinCondition(color) {
      if (this.players[color].finishedCount === 4 && !this.finishedRankList.includes(color)) {
        this.finishedRankList.push(color);
        this.players[color].rank = this.finishedRankList.length;

        this.showToast(`${color.toUpperCase()} Team Finished #${this.players[color].rank}!`);

        if (this.finishedRankList.length === 3 || (this.players.yellow.isAi === false && color === 'yellow')) {
          setTimeout(() => this.endGame(), 1000);
        }
      }
    }

    endGame() {
      this.phase = 'GAME_OVER';
      this.sound.playVictory();

      const winnerColor = this.finishedRankList[0] || 'yellow';
      document.getElementById('winner-title').innerText = `${winnerColor.toUpperCase()} WINS!`;
      document.getElementById('winner-desc').innerText = `${winnerColor.toUpperCase()} Team has conquered the board!`;

      const leaderboardEl = document.getElementById('leaderboard-list');
      leaderboardEl.innerHTML = '';

      COLORS.forEach(c => {
        const item = document.createElement('div');
        item.className = `rank-item ${c}`;
        const rank = this.finishedRankList.indexOf(c) + 1;
        const rankStr = rank > 0 ? `#${rank}` : 'DNF';
        item.innerHTML = `<span>${c.toUpperCase()}</span><span>${rankStr}</span>`;
        leaderboardEl.appendChild(item);
      });

      this.modalWinner.classList.remove('hidden');
    }

    restartGame() {
      this.turnIndex = 0;
      this.diceValue = 6;
      this.phase = 'WAIT_ROLL';
      this.consecutiveSixes = 0;
      this.grantExtraRoll = false;
      this.isRolling = false;
      this.finishedRankList = [];

      COLORS.forEach(color => {
        this.players[color].finishedCount = 0;
        this.players[color].rank = null;
        this.tokens[color].forEach(t => t.position = -1);

        const countEl = document.getElementById(`count-${color}`);
        if (countEl) countEl.innerText = '0';
      });

      this.renderTokens();
      this.updateUiState();
      if (this.btnRoll) this.btnRoll.disabled = false;
      this.showToast("New Game Started!");

      if (this.players[this.getCurrentColor()].isAi) {
        if (this.btnRoll) this.btnRoll.disabled = true;
        setTimeout(() => this.rollDice(), 800);
      }
    }

    updateUiState() {
      const activeColor = this.getCurrentColor();

      // Update Turn Banner
      this.turnBanner.className = `turn-banner turn-${activeColor}`;
      const nameText = (activeColor === 'yellow' && !this.players.yellow.isAi) ? "YOUR" : activeColor.toUpperCase();
      this.turnText.innerText = `${nameText}'S TURN`;

      // Update Profile Cards Active Glow
      COLORS.forEach(color => {
        const card = document.getElementById(`profile-${color}`);
        if (card) {
          if (color === activeColor) {
            card.classList.add('active');
          } else {
            card.classList.remove('active');
          }
        }
      });

      // Smoothly slide dynamic dice to the active player's profile
      this.positionDice(activeColor);

      this.statusMsg.innerText = this.players[activeColor].isAi ? "BOT thinking..." : "Tap Dice to Roll";
    }

    showToast(msg) {
      this.toastMsg.innerText = msg;
      this.toastMsg.classList.remove('hidden');
      setTimeout(() => {
        this.toastMsg.classList.add('hidden');
      }, 1800);
    }

    startOnlineGame(roomData, mySlot, userInfo) {
      this.isOnlineActive = true;
      this.mySlot = mySlot;
      this.myColor = COLORS[mySlot];

      if (roomData && roomData.players) {
        roomData.players.forEach((p, idx) => {
          const color = COLORS[idx];
          const isMe = (idx === mySlot);
          this.players[color] = {
            isAi: p.isBot,
            isRemote: !isMe && !p.isBot,
            name: p.name || color.toUpperCase(),
            avatar: p.avatar || `assets/avatar_p${idx + 1}.svg`,
            finishedCount: 0
          };

          const card = document.getElementById(`profile-${color}`);
          if (card) {
            const nameEl = card.querySelector('.player-name');
            const avatarEl = card.querySelector('.avatar-img');
            const botBadge = card.querySelector('.bot-badge');
            const youBadge = card.querySelector('.you-badge');

            if (nameEl) nameEl.innerText = p.name ? p.name.toUpperCase() : color.toUpperCase();
            if (avatarEl && p.avatar && p.avatar.length > 0) avatarEl.src = p.avatar;
            if (botBadge) botBadge.classList.toggle('hidden', !p.isBot);
            if (youBadge) youBadge.classList.toggle('hidden', !isMe);
          }
        });
      }

      this.restartGame(false);
      this.showToast("⚡ Online 4-Player Match Connected!");
    }

    handleRemoteAction(action) {
      if (!action || !action.timestamp || action.timestamp <= this.lastProcessedTimestamp) return;
      this.lastProcessedTimestamp = action.timestamp;

      if (action.slot === this.mySlot) return; // Local action

      if (action.type === 'ROLL') {
        this.forcedRoll = action.diceVal;
        this.rollDice();
        this.forcedRoll = null;
      } else if (action.type === 'MOVE') {
        this.onTokenSelected(action.color, action.tokenId);
      }
    }
  }

  // --- ONLINE MULTIPLAYER & MATCHMAKING MANAGER ---
  class LudoOnlineManager {
    constructor(game) {
      this.game = game;
      this.isOnline = false;
      this.mySlot = 0;
      this.roomId = null;
      this.timerSeconds = 12;
      this.timerInterval = null;

      this.initUiReferences();
      this.bindEvents();
      this.checkBridge();
    }

    initUiReferences() {
      this.btnFindFriends = document.getElementById('btn-find-friends');
      this.modalMatchmaking = document.getElementById('modal-matchmaking');
      this.btnCancelMatchmaking = document.getElementById('btn-cancel-matchmaking');
      this.statusText = document.getElementById('matchmaking-status-text');
      this.timerBadge = document.getElementById('matchmaking-timer');
    }

    checkBridge() {
      if (window.AndroidLudo) {
        try {
          const userInfoStr = window.AndroidLudo.getUserInfo();
          if (userInfoStr) {
            this.userInfo = JSON.parse(userInfoStr);
          }
        } catch (e) {
          console.error("Error reading userInfo from AndroidLudo", e);
        }
      }
      if (!this.userInfo) {
        this.userInfo = {
          uid: "guest_" + Math.floor(Math.random() * 8999 + 1000),
          name: "YOU",
          avatar: "assets/avatar_p4.svg"
        };
      }
    }

    bindEvents() {
      if (this.btnFindFriends) {
        this.btnFindFriends.addEventListener('click', () => {
          this.startMatchmaking();
        });
      }

      if (this.btnCancelMatchmaking) {
        this.btnCancelMatchmaking.addEventListener('click', () => {
          this.cancelMatchmaking();
        });
      }

      window.onLudoRoomUpdated = (roomData) => {
        this.onRoomUpdated(roomData);
      };
    }

    startMatchmaking() {
      this.isOnline = true;
      if (this.modalMatchmaking) this.modalMatchmaking.classList.remove('hidden');
      this.resetSlotCards();
      if (this.statusText) this.statusText.innerText = "Finding online players... (1/4)";

      this.timerSeconds = 12;
      if (this.timerBadge) this.timerBadge.innerText = `${this.timerSeconds}s`;

      if (this.timerInterval) clearInterval(this.timerInterval);
      this.timerInterval = setInterval(() => {
        this.timerSeconds--;
        if (this.timerSeconds <= 0) {
          clearInterval(this.timerInterval);
        } else {
          if (this.timerBadge) this.timerBadge.innerText = `${this.timerSeconds}s`;
        }
      }, 1000);

      if (window.AndroidLudo) {
        window.AndroidLudo.findMatch("online_4p");
      } else {
        this.simulateWebMatchmaking();
      }
    }

    simulateWebMatchmaking() {
      let found = 1;
      const names = ["Aarav", "Neha", "Rohan", "Priya"];
      const interval = setInterval(() => {
        found++;
        const slotCard = document.getElementById(`slot-card-${found - 1}`);
        const slotName = document.getElementById(`slot-name-${found - 1}`);
        const slotStatus = document.getElementById(`slot-status-${found - 1}`);

        if (slotCard) {
          slotCard.className = 'match-slot-card slot-found';
          if (slotName) slotName.innerText = names[found - 1] || "Player";
          if (slotStatus) {
            slotStatus.className = 'slot-status status-ready';
            slotStatus.innerText = 'CONNECTED';
          }
        }

        if (this.statusText) this.statusText.innerText = `Finding online players... (${found}/4)`;

        if (found >= 4 || this.timerSeconds <= 0) {
          clearInterval(interval);
          setTimeout(() => {
            if (this.modalMatchmaking) this.modalMatchmaking.classList.add('hidden');
            const mockRoom = {
              roomId: "web_room_1",
              state: "PLAYING",
              mySlot: 2,
              players: [
                { slot: 0, uid: "p1", name: names[0], avatar: "assets/avatar_p1.svg", isBot: false },
                { slot: 1, uid: "p2", name: names[1], avatar: "assets/avatar_p2.svg", isBot: false },
                { slot: 2, uid: this.userInfo.uid, name: this.userInfo.name || "YOU", avatar: this.userInfo.avatar || "assets/avatar_p4.svg", isBot: false },
                { slot: 3, uid: "p3", name: names[3], avatar: "assets/avatar_p3.svg", isBot: false }
              ]
            };
            this.onRoomUpdated(mockRoom);
          }, 800);
        }
      }, 1200);
    }

    resetSlotCards() {
      for (let i = 0; i < 4; i++) {
        const slotCard = document.getElementById(`slot-card-${i}`);
        const slotName = document.getElementById(`slot-name-${i}`);
        const slotStatus = document.getElementById(`slot-status-${i}`);
        const slotAvatar = document.getElementById(`slot-avatar-${i}`);

        if (i === 0) {
          if (slotCard) slotCard.className = 'match-slot-card slot-found';
          if (slotName) slotName.innerText = this.userInfo ? this.userInfo.name : "YOU";
          if (slotStatus) {
            slotStatus.className = 'slot-status status-ready';
            slotStatus.innerText = 'CONNECTED';
          }
          if (slotAvatar && this.userInfo && this.userInfo.avatar && this.userInfo.avatar.length > 0) {
            slotAvatar.src = this.userInfo.avatar;
          }
        } else {
          if (slotCard) slotCard.className = 'match-slot-card slot-searching';
          if (slotName) slotName.innerText = "Searching...";
          if (slotStatus) {
            slotStatus.className = 'slot-status status-wait';
            slotStatus.innerText = 'SEARCHING';
          }
        }
      }
    }

    cancelMatchmaking() {
      if (this.timerInterval) clearInterval(this.timerInterval);
      if (this.modalMatchmaking) this.modalMatchmaking.classList.add('hidden');
      this.isOnline = false;

      if (window.AndroidLudo) {
        window.AndroidLudo.leaveMatch();
      }
    }

    onRoomUpdated(roomData) {
      if (!roomData) return;

      this.roomId = roomData.roomId;

      if (roomData.mySlot !== undefined) {
        this.mySlot = roomData.mySlot;
      }

      if (roomData.players && Array.isArray(roomData.players)) {
        let connectedCount = 0;
        roomData.players.forEach((p, idx) => {
          const slotCard = document.getElementById(`slot-card-${idx}`);
          const slotName = document.getElementById(`slot-name-${idx}`);
          const slotStatus = document.getElementById(`slot-status-${idx}`);
          const slotAvatar = document.getElementById(`slot-avatar-${idx}`);

          if (p && p.uid && p.uid.length > 0) {
            connectedCount++;
            if (slotCard) slotCard.className = 'match-slot-card slot-found';
            if (slotName) slotName.innerText = p.name || `Player ${idx + 1}`;
            if (slotStatus) {
              slotStatus.className = 'slot-status status-ready';
              slotStatus.innerText = p.isBot ? 'BOT' : 'CONNECTED';
            }
            if (slotAvatar && p.avatar && p.avatar.length > 0) {
              slotAvatar.src = p.avatar;
            }
          }
        });

        if (this.statusText) this.statusText.innerText = `Finding online players... (${connectedCount}/4)`;
      }

      if (roomData.state === "PLAYING" && !this.game.isOnlineActive) {
        if (this.timerInterval) clearInterval(this.timerInterval);
        if (this.modalMatchmaking) this.modalMatchmaking.classList.add('hidden');

        this.game.startOnlineGame(roomData, this.mySlot, this.userInfo);
      }

      if (roomData.lastAction && this.game.isOnlineActive) {
        this.game.handleRemoteAction(roomData.lastAction);
      }
    }

    sendAction(actionType, actionData) {
      if (!this.isOnline) return;
      if (window.AndroidLudo) {
        window.AndroidLudo.sendGameAction(actionType, JSON.stringify(actionData));
      }
    }
  }

  // Initialize Game on DOM Load
  document.addEventListener('DOMContentLoaded', () => {
    window.ludoGameInstance = new LudoGame();
  });

})();
