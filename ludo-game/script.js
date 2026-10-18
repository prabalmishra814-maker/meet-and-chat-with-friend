/**
 * 4v4 Ludo King - HTML5 Mobile Game Script
 * Isolated IIFE Scope
 */
(function () {
  'use strict';

  // --- GAME CONSTANTS ---
  const COLORS = ['red', 'green', 'yellow', 'blue'];

  // Start Indices on the 52-step Main Track
  const START_MAIN_INDEX = {
    red: 0,
    green: 13,
    yellow: 26,
    blue: 39
  };

  // Safe Square Indices on the 52-step Main Track
  const SAFE_INDICES = [0, 8, 13, 21, 26, 34, 39, 47];

  // 52-Step Main Track Coordinates (Row 1..15, Col 1..15)
  const MAIN_TRACK = [
    { r: 7, c: 2 },  { r: 7, c: 3 },  { r: 7, c: 4 },  { r: 7, c: 5 },  { r: 7, c: 6 },  // 0-4 (Red Start = 0)
    { r: 6, c: 7 },  { r: 5, c: 7 },  { r: 4, c: 7 },  { r: 3, c: 7 },  { r: 2, c: 7 },  { r: 1, c: 7 },  // 5-10 (Star = 8)
    { r: 1, c: 8 },  // 11
    { r: 1, c: 9 },  { r: 2, c: 9 },  { r: 3, c: 9 },  { r: 4, c: 9 },  { r: 5, c: 9 },  { r: 6, c: 9 },  // 12-17 (Green Start = 13)
    { r: 7, c: 10 }, { r: 7, c: 11 }, { r: 7, c: 12 }, { r: 7, c: 13 }, { r: 7, c: 14 }, { r: 7, c: 15 }, // 18-23 (Star = 21)
    { r: 8, c: 15 }, // 24
    { r: 9, c: 15 }, { r: 9, c: 14 }, { r: 9, c: 13 }, { r: 9, c: 12 }, { r: 9, c: 11 }, { r: 9, c: 10 }, // 25-30 (Yellow Start = 26)
    { r: 10, c: 9 }, { r: 11, c: 9 }, { r: 12, c: 9 }, { r: 13, c: 9 }, { r: 14, c: 9 }, { r: 15, c: 9 }, // 31-36 (Star = 34)
    { r: 15, c: 8 }, // 37
    { r: 15, c: 7 }, { r: 14, c: 7 }, { r: 13, c: 7 }, { r: 12, c: 7 }, { r: 11, c: 7 }, { r: 10, c: 7 }, // 38-43 (Blue Start = 39)
    { r: 9, c: 6 },  { r: 9, c: 5 },  { r: 9, c: 4 },  { r: 9, c: 3 },  { r: 9, c: 2 },  { r: 9, c: 1 },  // 44-49 (Star = 47)
    { r: 8, c: 1 },  // 50
    { r: 7, c: 1 }   // 51
  ];

  // Home Paths (5 steps leading to Finish)
  const HOME_PATHS = {
    red:    [{ r: 8, c: 2 },  { r: 8, c: 3 },  { r: 8, c: 4 },  { r: 8, c: 5 },  { r: 8, c: 6 }],
    green:  [{ r: 2, c: 8 },  { r: 3, c: 8 },  { r: 4, c: 8 },  { r: 5, c: 8 },  { r: 6, c: 8 }],
    yellow: [{ r: 8, c: 14 }, { r: 8, c: 13 }, { r: 8, c: 12 }, { r: 8, c: 11 }, { r: 8, c: 10 }],
    blue:   [{ r: 14, c: 8 }, { r: 13, c: 8 }, { r: 12, c: 8 }, { r: 11, c: 8 }, { r: 10, c: 8 }]
  };

  // Center Finish Coordinates (Step 56)
  const FINISH_COORDS = {
    red:    { r: 8, c: 7 },
    green:  { r: 7, c: 8 },
    yellow: { r: 8, c: 9 },
    blue:   { r: 9, c: 8 }
  };

  // Yard Socket Coordinates (Inside Home Bases for 4 tokens)
  const YARD_SOCKETS = {
    red:    [{ r: 2, c: 2 },  { r: 2, c: 5 },  { r: 5, c: 2 },  { r: 5, c: 5 }],
    green:  [{ r: 2, c: 11 }, { r: 2, c: 14 }, { r: 5, c: 11 }, { r: 5, c: 14 }],
    yellow: [{ r: 11, c: 11 },{ r: 11, c: 14 },{ r: 14, c: 11 },{ r: 14, c: 14 }],
    blue:   [{ r: 11, c: 2 }, { r: 11, c: 5 }, { r: 14, c: 2 }, { r: 14, c: 5 }]
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
      osc.frequency.setValueAtTime(200, this.ctx.currentTime);
      osc.frequency.exponentialRampToValueAtTime(600, this.ctx.currentTime + 0.15);
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
      osc.frequency.setValueAtTime(400, this.ctx.currentTime);
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
      osc.frequency.exponentialRampToValueAtTime(150, this.ctx.currentTime + 0.3);
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
      const notes = [440, 554.37, 659.25, 880];
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
      this.turnIndex = 0; // 0=red, 1=green, 2=yellow, 3=blue
      this.diceValue = null;
      this.phase = 'WAIT_ROLL'; // 'WAIT_ROLL', 'WAIT_MOVE', 'GAME_OVER'
      this.consecutiveSixes = 0;
      this.grantExtraRoll = false;
      this.isRolling = false;
      this.autoMoveSingle = true;
      this.devMode = false;
      this.forcedRoll = null;

      // Player Configurations
      this.players = {
        red:    { isAi: false, finishedCount: 0, rank: null },
        green:  { isAi: true,  finishedCount: 0, rank: null },
        yellow: { isAi: true,  finishedCount: 0, rank: null },
        blue:   { isAi: true,  finishedCount: 0, rank: null }
      };

      // Rank tracker
      this.finishedRankList = [];

      // Tokens: 16 total (4 per color)
      // position: -1 (Yard), 0..50 (Track steps), 51..55 (Home Path steps), 56 (Finished)
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
      this.updateUiState();
    }

    initDomReferences() {
      this.boardEl = document.getElementById('ludo-board');
      this.turnBanner = document.getElementById('turn-banner');
      this.turnText = document.getElementById('turn-text');
      this.extraRollBadge = document.getElementById('extra-roll-badge');
      this.diceBox = document.getElementById('dice-box');
      this.dice3d = document.getElementById('dice-3d');
      this.btnRoll = document.getElementById('btn-roll');
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

    // Render the 15x15 Ludo Board Grid Elements
    buildBoardDom() {
      this.boardEl.innerHTML = '';

      // 1. Render Home Bases (Yards)
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

      // 2. Render 15x15 Track & Home Cells
      for (let r = 1; r <= 15; r++) {
        for (let c = 1; c <= 15; c++) {
          // Skip Yard areas (1..6 x 1..6, 1..6 x 10..15, 10..15 x 10..15, 10..15 x 1..6)
          if ((r <= 6 && c <= 6) || (r <= 6 && c >= 10) || (r >= 10 && c >= 10) || (r >= 10 && c <= 6)) {
            continue;
          }
          // Skip Center Finish area (7..9 x 7..9)
          if (r >= 7 && r <= 9 && c >= 7 && c <= 9) {
            continue;
          }

          const cellEl = document.createElement('div');
          cellEl.className = 'cell';
          cellEl.style.gridArea = `${r} / ${c} / ${r + 1} / ${c + 1}`;
          cellEl.dataset.r = r;
          cellEl.dataset.c = c;

          // Color specific path cells
          if (r === 8 && c >= 2 && c <= 6) cellEl.classList.add('cell-red');
          else if (c === 8 && r >= 2 && r <= 6) cellEl.classList.add('cell-green');
          else if (r === 8 && c >= 10 && c <= 14) cellEl.classList.add('cell-yellow');
          else if (c === 8 && r >= 10 && r <= 14) cellEl.classList.add('cell-blue');
          else if (r === 7 && c === 2) cellEl.classList.add('cell-red');
          else if (r === 2 && c === 9) cellEl.classList.add('cell-green');
          else if (r === 9 && c === 14) cellEl.classList.add('cell-yellow');
          else if (r === 14 && c === 7) cellEl.classList.add('cell-blue');
          else cellEl.classList.add('path-white');

          // Safe Star Cells
          const isSafeStar = (r === 3 && c === 7) || (r === 7 && c === 13) || (r === 13 && c === 9) || (r === 9 && c === 3);
          if (isSafeStar) {
            cellEl.classList.add('cell-safe');
          }

          this.boardEl.appendChild(cellEl);
        }
      }

      // 3. Render Center Finish Area
      const finishEl = document.createElement('div');
      finishEl.className = 'center-finish';
      finishEl.innerHTML = `
        <div class="tri-red"></div>
        <div class="tri-green"></div>
        <div class="tri-yellow"></div>
        <div class="tri-blue"></div>
        <img src="assets/trophy_center.svg" class="center-trophy" alt="Center Finish">
      `;
      this.boardEl.appendChild(finishEl);

      // 4. Render 16 Token Elements
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

    // Convert Token Position to {r, c} Grid Coordinates
    getTokenCoordinates(token) {
      if (token.position === -1) {
        // Yard Socket
        return YARD_SOCKETS[token.color][token.id];
      }
      if (token.position === 56) {
        // Center Finish
        return FINISH_COORDS[token.color];
      }
      if (token.position >= 51 && token.position <= 55) {
        // Home Path
        const homeIdx = token.position - 51;
        return HOME_PATHS[token.color][homeIdx];
      }

      // Main Track step (0..50)
      const startIdx = START_MAIN_INDEX[token.color];
      const mainIdx = (startIdx + token.position) % 52;
      return MAIN_TRACK[mainIdx];
    }

    // Position Token DOM Elements on Grid
    renderTokens() {
      // Map to track occupied tiles for stack count badges
      const tileOccupancy = {};

      COLORS.forEach(color => {
        this.tokens[color].forEach(token => {
          const coords = this.getTokenCoordinates(token);
          const key = `${coords.r}_${coords.c}`;
          if (!tileOccupancy[key]) tileOccupancy[key] = [];
          tileOccupancy[key].push(token);
        });
      });

      COLORS.forEach(color => {
        this.tokens[color].forEach(token => {
          const tokenEl = document.getElementById(`token-${color}-${token.id}`);
          if (!tokenEl) return;

          const coords = this.getTokenCoordinates(token);
          const key = `${coords.r}_${coords.c}`;
          const stack = tileOccupancy[key];

          tokenEl.style.gridArea = `${coords.r} / ${coords.c} / ${coords.r + 1} / ${coords.c + 1}`;

          // If stacked with multiple tokens on track, apply offset
          if (stack.length > 1 && token.position !== -1 && token.position !== 56) {
            const indexInStack = stack.indexOf(token);
            const offset = (indexInStack - (stack.length - 1) / 2) * 5;
            tokenEl.style.transform = `translate(${offset}px, ${offset}px) scale(0.9)`;
          } else {
            tokenEl.style.transform = 'none';
          }

          // Remove stack badge if present
          const existingBadge = tokenEl.querySelector('.token-stack-badge');
          if (existingBadge) existingBadge.remove();

          // Add stack badge to top token if stack > 1
          if (stack.length > 1 && stack[0] === token && token.position !== -1 && token.position !== 56) {
            const badge = document.createElement('div');
            badge.className = 'token-stack-badge';
            badge.innerText = stack.length;
            tokenEl.appendChild(badge);
          }
        });
      });
    }

    bindEvents() {
      // Dice Roll Trigger
      const triggerRoll = () => {
        if (this.phase === 'WAIT_ROLL' && !this.isRolling && !this.players[this.getCurrentColor()].isAi) {
          this.rollDice();
        }
      };

      this.btnRoll.addEventListener('click', triggerRoll);
      this.diceBox.addEventListener('click', triggerRoll);

      // Token Click Event
      this.boardEl.addEventListener('click', (e) => {
        const tokenEl = e.target.closest('.token');
        if (!tokenEl) return;
        const color = tokenEl.dataset.color;
        const id = parseInt(tokenEl.dataset.id, 10);

        if (color === this.getCurrentColor() && this.phase === 'WAIT_MOVE') {
          this.onTokenSelected(color, id);
        }
      });

      // Header Actions
      document.getElementById('btn-sound').addEventListener('click', () => {
        this.sound.enabled = !this.sound.enabled;
        const img = document.getElementById('img-sound');
        img.src = this.sound.enabled ? 'assets/icon_sound_on.svg' : 'assets/icon_sound_off.svg';
      });

      document.getElementById('btn-settings').addEventListener('click', () => {
        this.modalSettings.classList.remove('hidden');
      });

      document.getElementById('btn-rules').addEventListener('click', () => {
        this.modalRules.classList.remove('hidden');
      });

      // Close Modals
      document.querySelectorAll('.modal-close').forEach(btn => {
        btn.addEventListener('click', (e) => {
          e.target.closest('.modal-overlay').classList.add('hidden');
        });
      });

      // Restart Game
      document.getElementById('btn-restart-game').addEventListener('click', () => {
        this.modalSettings.classList.add('hidden');
        this.restartGame();
      });

      document.getElementById('btn-play-again').addEventListener('click', () => {
        this.modalWinner.classList.add('hidden');
        this.restartGame();
      });

      // Game Mode Selector
      this.selectGameMode.addEventListener('change', (e) => {
        const mode = e.target.value;
        if (mode === 'vs_ai') {
          this.players = {
            red:    { isAi: false },
            green:  { isAi: true },
            yellow: { isAi: true },
            blue:   { isAi: true }
          };
        } else if (mode === 'pass_play') {
          this.players = {
            red:    { isAi: false },
            green:  { isAi: false },
            yellow: { isAi: false },
            blue:   { isAi: false }
          };
        } else if (mode === '2_vs_2') {
          this.players = {
            red:    { isAi: false },
            green:  { isAi: true },
            yellow: { isAi: false },
            blue:   { isAi: true }
          };
        }
        this.updatePlayerTypeBadges();
      });

      // Dev Mode Checkbox
      this.checkDevMode.addEventListener('change', (e) => {
        this.devMode = e.target.checked;
        if (this.devMode) {
          this.devTestPanel.classList.remove('hidden');
        } else {
          this.devTestPanel.classList.add('hidden');
          this.forcedRoll = null;
        }
      });

      // Dev Force Roll Buttons
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

    updatePlayerTypeBadges() {
      COLORS.forEach(color => {
        const card = document.getElementById(`card-${color}`);
        if (!card) return;
        const badge = card.querySelector('.p-type-badge');
        if (badge) {
          badge.innerText = this.players[color].isAi ? 'BOT' : 'HUMAN';
        }
      });
    }

    // Roll Dice Logic
    rollDice() {
      if (this.isRolling || this.phase !== 'WAIT_ROLL') return;

      this.isRolling = true;
      this.btnRoll.disabled = true;
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

      // Three consecutive 6s rule: forfeit roll and pass turn
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
        this.statusMsg.innerText = `Rolled a ${this.diceValue}! Select a token to move.`;

        // Highlight valid tokens
        this.clearTokenHighlights();
        validTokens.forEach(t => {
          const el = document.getElementById(`token-${currentColor}-${t.id}`);
          if (el) el.classList.add('token-movable');
        });

        // Auto-move if only 1 valid token and autoMoveSingle enabled or if player is AI
        if (this.players[currentColor].isAi) {
          setTimeout(() => this.makeAiMove(validTokens), 600);
        } else if (validTokens.length === 1 && this.autoMoveSingle) {
          setTimeout(() => this.onTokenSelected(currentColor, validTokens[0].id), 400);
        }
      }
    }

    // Calculate Legal Token Moves
    getValidMoveTokens(color, roll) {
      return this.tokens[color].filter(token => {
        // Token in Yard: Needs a 6 to get out
        if (token.position === -1) {
          return roll === 6;
        }
        // Token already Finished
        if (token.position === 56) {
          return false;
        }
        // Moving on Track / Home Path
        const targetPos = token.position + roll;
        // Cannot exceed 56 (exact roll required for Finish)
        return targetPos <= 56;
      });
    }

    clearTokenHighlights() {
      document.querySelectorAll('.token-movable').forEach(el => {
        el.classList.remove('token-movable');
      });
    }

    // Execute Token Movement
    onTokenSelected(color, tokenId) {
      if (this.phase !== 'WAIT_MOVE' || color !== this.getCurrentColor()) return;

      const token = this.tokens[color][tokenId];
      const validTokens = this.getValidMoveTokens(color, this.diceValue);

      if (!validTokens.some(t => t.id === tokenId)) {
        return; // Invalid token selected
      }

      this.clearTokenHighlights();
      this.phase = 'ANIMATING';

      if (token.position === -1 && this.diceValue === 6) {
        // Bring token out of Yard to Start cell
        token.position = 0;
        this.sound.playStep();
        this.renderTokens();
        this.postMoveCheck(token, true); // 6 grants extra roll
      } else {
        // Move step by step
        this.animateStepByStep(token, this.diceValue, () => {
          const captured = this.checkCapture(token);
          const reachedHome = token.position === 56;

          if (reachedHome) {
            this.players[color].finishedCount++;
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

    // Capture Check
    checkCapture(movedToken) {
      if (movedToken.position < 0 || movedToken.position > 50) {
        return false; // Cannot capture in Yard or Home Path
      }

      const startIdx = START_MAIN_INDEX[movedToken.color];
      const movedMainIdx = (startIdx + movedToken.position) % 52;

      // Check if square is safe
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
              // CAPTURED!
              otherToken.position = -1; // Send back to Yard
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
        this.statusMsg.innerText = "EXTRA ROLL granted! Tap ROLL.";
        this.extraRollBadge.classList.remove('hidden');

        if (this.players[this.getCurrentColor()].isAi) {
          setTimeout(() => this.rollDice(), 800);
        } else {
          this.btnRoll.disabled = false;
        }
      } else {
        this.grantExtraRoll = false;
        this.extraRollBadge.classList.add('hidden');
        this.nextTurn();
      }
    }

    // AI Bot Decision Engine
    makeAiMove(validTokens) {
      if (validTokens.length === 0) return;

      let chosenToken = validTokens[0];
      let highestScore = -999;

      validTokens.forEach(token => {
        let score = 0;
        const targetPos = token.position === -1 ? 0 : token.position + this.diceValue;

        // 1. Prioritize Capturing Opponent
        if (targetPos <= 50) {
          const startIdx = START_MAIN_INDEX[token.color];
          const targetMainIdx = (startIdx + targetPos) % 52;
          if (!SAFE_INDICES.includes(targetMainIdx)) {
            COLORS.forEach(oc => {
              if (oc !== token.color) {
                this.tokens[oc].forEach(ot => {
                  if (ot.position >= 0 && ot.position <= 50) {
                    const oMain = (START_MAIN_INDEX[oc] + ot.position) % 52;
                    if (oMain === targetMainIdx) score += 100; // Capture bonus
                  }
                });
              }
            });
          }
        }

        // 2. Prioritize Reaching Finish
        if (targetPos === 56) score += 80;

        // 3. Prioritize Releasing Token on 6
        if (token.position === -1 && this.diceValue === 6) score += 50;

        // 4. Prioritize Safe Tiles
        if (targetPos <= 50) {
          const mainIdx = (START_MAIN_INDEX[token.color] + targetPos) % 52;
          if (SAFE_INDICES.includes(mainIdx)) score += 30;
        }

        // 5. Advance furthest token
        score += token.position;

        if (score > highestScore) {
          highestScore = score;
          chosenToken = token;
        }
      });

      this.onTokenSelected(chosenToken.color, chosenToken.id);
    }

    // Advance to Next Player's Turn
    nextTurn() {
      if (this.phase === 'GAME_OVER') return;

      this.extraRollBadge.classList.add('hidden');
      this.consecutiveSixes = 0;

      // Find next active player
      let attempts = 0;
      do {
        this.turnIndex = (this.turnIndex + 1) % 4;
        attempts++;
      } while (this.players[this.getCurrentColor()].finishedCount === 4 && attempts < 4);

      if (attempts >= 4) {
        // All players finished
        this.endGame();
        return;
      }

      this.phase = 'WAIT_ROLL';
      this.updateUiState();

      // If AI, trigger automated roll
      if (this.players[this.getCurrentColor()].isAi) {
        this.btnRoll.disabled = true;
        setTimeout(() => this.rollDice(), 800);
      } else {
        this.btnRoll.disabled = false;
      }
    }

    checkWinCondition(color) {
      if (this.players[color].finishedCount === 4 && !this.finishedRankList.includes(color)) {
        this.finishedRankList.push(color);
        this.players[color].rank = this.finishedRankList.length;

        this.showToast(`${color.toUpperCase()} Team Finished #${this.players[color].rank}!`);

        if (this.finishedRankList.length === 3 || (this.players.red.isAi === false && color === 'red')) {
          // Trigger GameOver/Winner screen if human wins or top 3 finished
          setTimeout(() => this.endGame(), 1000);
        }
      }
    }

    endGame() {
      this.phase = 'GAME_OVER';
      this.sound.playVictory();

      const winnerColor = this.finishedRankList[0] || 'red';
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
      this.diceValue = null;
      this.phase = 'WAIT_ROLL';
      this.consecutiveSixes = 0;
      this.grantExtraRoll = false;
      this.isRolling = false;
      this.finishedRankList = [];

      COLORS.forEach(color => {
        this.players[color].finishedCount = 0;
        this.players[color].rank = null;
        this.tokens[color].forEach(t => t.position = -1);
      });

      this.renderTokens();
      this.updateUiState();
      this.btnRoll.disabled = false;
      this.showToast("New Game Started!");
    }

    updateUiState() {
      const activeColor = this.getCurrentColor();

      // Update Turn Banner
      this.turnBanner.className = `turn-banner turn-${activeColor}`;
      this.turnText.innerText = `${activeColor.toUpperCase()}'S TURN`;

      // Update Player Cards
      COLORS.forEach(color => {
        const card = document.getElementById(`card-${color}`);
        if (card) {
          if (color === activeColor) {
            card.classList.add('active');
          } else {
            card.classList.remove('active');
          }
          const homeCountEl = card.querySelector('.p-home-count');
          if (homeCountEl) {
            homeCountEl.innerText = `${this.players[color].finishedCount}/4 Home`;
          }
        }
      });

      this.statusMsg.innerText = this.players[activeColor].isAi ? "BOT thinking..." : "Tap ROLL or the Dice";
    }

    showToast(msg) {
      this.toastMsg.innerText = msg;
      this.toastMsg.classList.remove('hidden');
      setTimeout(() => {
        this.toastMsg.classList.add('hidden');
      }, 1800);
    }
  }

  // Initialize Game on DOM Load
  document.addEventListener('DOMContentLoaded', () => {
    window.ludoGameInstance = new LudoGame();
  });

})();
