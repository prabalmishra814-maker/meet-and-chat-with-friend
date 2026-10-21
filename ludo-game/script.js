/**
 * 4v4 Ludo King - Dynamic Options, Pass & Play Name Customization & Online Matchmaking Timeout
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
      this.turnIndex = 0; // index into COLORS
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

      // Game Type & Mode State
      this.gameType = 'pass_play'; // 'pass_play', 'vs_ai', 'online'
      this.subMode = '4v4'; // '1v1', '2v2', '4v4'
      this.activeColors = ['green', 'blue', 'yellow', 'red'];
      this.teams = null; // { green: 'A', yellow: 'A', blue: 'B', red: 'B' } for 2v2

      this.playerNames = {
        green: 'Player 1',
        blue: 'Player 2',
        yellow: 'Player 3',
        red: 'Player 4'
      };

      // Player Configurations
      this.players = {
        green:  { isAi: false, name: 'Player 1', finishedCount: 0, rank: null },
        blue:   { isAi: false, name: 'Player 2', finishedCount: 0, rank: null },
        yellow: { isAi: false, name: 'Player 3', finishedCount: 0, rank: null },
        red:    { isAi: false, name: 'Player 4', finishedCount: 0, rank: null }
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

      this.turnTimerInterval = null;
      this.turnTimerSeconds = 0;

      this.initDomReferences();
      this.buildBoardDom();
      this.bindEvents();
      this.onlineManager = new LudoOnlineManager(this);

      // Show Main Game Mode Selection Menu on startup
      setTimeout(() => {
        this.showMainMenu();
      }, 100);
    }

    initDomReferences() {
      this.viewportEl = document.getElementById('game-viewport');
      this.boardEl = document.getElementById('ludo-board');
      this.turnBanner = document.getElementById('turn-banner');
      this.turnText = document.getElementById('turn-text');
      this.turnTimerEl = document.getElementById('turn-timer');
      this.extraRollBadge = document.getElementById('extra-roll-badge');
      this.diceBox = document.getElementById('dice-box');
      this.dice3d = document.getElementById('dice-3d');
      this.btnRoll = document.getElementById('btn-roll');
      this.btnBack = document.getElementById('btn-back');
      this.statusMsg = document.getElementById('status-message');
      this.toastMsg = document.getElementById('toast-message');

      // Modals
      this.modalMainMenu = document.getElementById('modal-main-menu');
      this.menuStep1 = document.getElementById('menu-step-1');
      this.menuStep2 = document.getElementById('menu-step-2');
      this.menuStep3 = document.getElementById('menu-step-3');

      this.modalSettings = document.getElementById('modal-settings');
      this.modalRules = document.getElementById('modal-rules');
      this.modalWinner = document.getElementById('modal-winner');

      // Settings elements
      this.selectGameMode = document.getElementById('select-game-mode');
      this.checkDevMode = document.getElementById('check-dev-mode');
      this.checkAutoMove = document.getElementById('check-auto-move');
      this.devTestPanel = document.getElementById('dev-test-panel');
    }

    stopTurnTimer() {
      if (this.turnTimerInterval) {
        clearInterval(this.turnTimerInterval);
        this.turnTimerInterval = null;
      }
      if (this.turnTimerEl) {
        this.turnTimerEl.classList.add('hidden');
        this.turnTimerEl.classList.remove('urgent');
      }
    }

    startTurnTimer(seconds) {
      this.stopTurnTimer();

      if (this.phase === 'GAME_OVER') return;

      const currentColor = this.getCurrentColor();
      const pData = this.players[currentColor];
      const isMyTurn = !this.isOnlineActive || (currentColor === this.myColor);

      // Private Turns: Active player sees countdown timer.
      // Opponent sees "Opponent's turn" and private timer controls are hidden.
      if (!isMyTurn) {
        if (this.turnTimerEl) this.turnTimerEl.classList.add('hidden');

        // Fallback timer for remote opponent if they freeze or disconnect
        let fallbackSeconds = seconds + 4; // 14s for dice, 34s for move
        const activePhaseAtStart = this.phase;
        const activeColorAtStart = currentColor;

        this.turnTimerInterval = setInterval(() => {
          fallbackSeconds--;
          if (this.phase !== activePhaseAtStart || this.getCurrentColor() !== activeColorAtStart) {
            this.stopTurnTimer();
            return;
          }
          if (fallbackSeconds <= 0) {
            this.stopTurnTimer();
            this.handleTurnTimeout(activePhaseAtStart, activeColorAtStart);
          }
        }, 1000);
        return;
      }

      this.turnTimerSeconds = seconds;

      if (this.turnTimerEl) {
        this.turnTimerEl.classList.remove('hidden');
        this.turnTimerEl.innerText = `${this.turnTimerSeconds}s`;
      }

      const activePhaseAtStart = this.phase;
      const activeColorAtStart = currentColor;

      this.turnTimerInterval = setInterval(() => {
        this.turnTimerSeconds--;

        if (this.phase !== activePhaseAtStart || this.getCurrentColor() !== activeColorAtStart) {
          this.stopTurnTimer();
          return;
        }

        if (this.turnTimerEl) {
          this.turnTimerEl.innerText = `${this.turnTimerSeconds}s`;
          if (this.turnTimerSeconds <= 3) {
            this.turnTimerEl.classList.add('urgent');
          } else {
            this.turnTimerEl.classList.remove('urgent');
          }
        }

        if (this.turnTimerSeconds <= 0) {
          this.stopTurnTimer();
          this.handleTurnTimeout(activePhaseAtStart, activeColorAtStart);
        }
      }, 1000);
    }

    handleTurnTimeout(phaseAtStart, colorAtStart) {
      if (this.phase !== phaseAtStart || this.getCurrentColor() !== colorAtStart) return;

      if (phaseAtStart === 'WAIT_ROLL') {
        this.showToast("Time's up! Auto rolling dice...");
        this.rollDice();
      } else if (phaseAtStart === 'WAIT_MOVE') {
        this.showToast("Time's up! Auto moving token...");
        const validTokens = this.getValidMoveTokens(colorAtStart, this.diceValue);
        if (validTokens.length > 0) {
          this.makeAiMove(validTokens);
        } else {
          this.nextTurn();
        }
      }
    }

    showMainMenu() {
      if (this.modalMainMenu) {
        this.modalMainMenu.classList.remove('hidden');
        this.showMenuStep(1);
      }
    }

    showMenuStep(stepNum) {
      if (this.menuStep1) this.menuStep1.classList.toggle('hidden', stepNum !== 1);
      if (this.menuStep2) this.menuStep2.classList.toggle('hidden', stepNum !== 2);
      if (this.menuStep3) this.menuStep3.classList.toggle('hidden', stepNum !== 3);

      if (stepNum === 2) {
        const typeLabels = {
          'pass_play': 'PASS & PLAY',
          'vs_ai': 'VS COMPUTER',
          'online': 'ONLINE PLAY'
        };
        const titleEl = document.getElementById('step2-title');
        if (titleEl) {
          titleEl.innerText = `${typeLabels[this.gameType] || 'LUDO'} - SELECT MODE`;
        }
      }

      if (stepNum === 3) {
        // Toggle input field visibility based on 1v1 vs 1v2 vs 1v4/4v4
        const fieldBlue = document.getElementById('field-wrap-blue');
        const fieldYellow = document.getElementById('field-wrap-yellow');
        const fieldRed = document.getElementById('field-wrap-red');

        if (this.subMode === '1v1') {
          if (fieldBlue) fieldBlue.classList.add('hidden');
          if (fieldYellow) fieldYellow.classList.remove('hidden');
          if (fieldRed) fieldRed.classList.add('hidden');
        } else if (this.subMode === '1v2') {
          if (fieldBlue) fieldBlue.classList.remove('hidden');
          if (fieldYellow) fieldYellow.classList.remove('hidden');
          if (fieldRed) fieldRed.classList.add('hidden');
        } else {
          if (fieldBlue) fieldBlue.classList.remove('hidden');
          if (fieldYellow) fieldYellow.classList.remove('hidden');
          if (fieldRed) fieldRed.classList.remove('hidden');
        }
      }
    }

    applyAndStartGame() {
      if (this.modalMainMenu) this.modalMainMenu.classList.add('hidden');

      if (this.gameType === 'pass_play') {
        this.isOnlineActive = false;

        const nameG = document.getElementById('input-name-green')?.value.trim() || 'Player 1';
        const nameB = document.getElementById('input-name-blue')?.value.trim() || 'Player 2';
        const nameY = document.getElementById('input-name-yellow')?.value.trim() || 'Player 3';
        const nameR = document.getElementById('input-name-red')?.value.trim() || 'Player 4';

        this.playerNames = {
          green: nameG,
          blue: nameB,
          yellow: nameY,
          red: nameR
        };

        if (this.subMode === '1v1') {
          this.activeColors = ['green', 'yellow'];
          this.teams = null;
          this.players = {
            green:  { isAi: false, name: nameG, finishedCount: 0, rank: null },
            blue:   { isAi: false, name: "INACTIVE", finishedCount: 4, rank: null },
            yellow: { isAi: false, name: nameY, finishedCount: 0, rank: null },
            red:    { isAi: false, name: "INACTIVE", finishedCount: 4, rank: null }
          };
        } else if (this.subMode === '1v2') {
          this.activeColors = ['green', 'blue', 'yellow'];
          this.teams = null;
          this.players = {
            green:  { isAi: false, name: nameG, finishedCount: 0, rank: null },
            blue:   { isAi: false, name: nameB, finishedCount: 0, rank: null },
            yellow: { isAi: false, name: nameY, finishedCount: 0, rank: null },
            red:    { isAi: false, name: "INACTIVE", finishedCount: 4, rank: null }
          };
        } else { // 4v4 or 1v4
          this.activeColors = ['green', 'blue', 'yellow', 'red'];
          this.teams = null;
          this.players = {
            green:  { isAi: false, name: nameG, finishedCount: 0, rank: null },
            blue:   { isAi: false, name: nameB, finishedCount: 0, rank: null },
            yellow: { isAi: false, name: nameY, finishedCount: 0, rank: null },
            red:    { isAi: false, name: nameR, finishedCount: 0, rank: null }
          };
        }
      } else if (this.gameType === 'vs_ai') {
        this.isOnlineActive = false;

        if (this.subMode === '1v1') {
          this.activeColors = ['green', 'yellow'];
          this.teams = null;
          this.players = {
            green:  { isAi: false, name: "YOU", finishedCount: 0, rank: null },
            blue:   { isAi: true, name: "BOT", finishedCount: 4, rank: null },
            yellow: { isAi: true, name: "BOT ALEX", finishedCount: 0, rank: null },
            red:    { isAi: true, name: "BOT", finishedCount: 4, rank: null }
          };
        } else if (this.subMode === '1v2') {
          this.activeColors = ['green', 'blue', 'yellow'];
          this.teams = null;
          this.players = {
            green:  { isAi: false, name: "YOU", finishedCount: 0, rank: null },
            blue:   { isAi: true, name: "BOT MAX", finishedCount: 0, rank: null },
            yellow: { isAi: true, name: "BOT ALEX", finishedCount: 0, rank: null },
            red:    { isAi: true, name: "BOT", finishedCount: 4, rank: null }
          };
        } else { // 4v4 or 1v4
          this.activeColors = ['green', 'blue', 'yellow', 'red'];
          this.teams = null;
          this.players = {
            green:  { isAi: false, name: "YOU", finishedCount: 0, rank: null },
            blue:   { isAi: true, name: "BOT MAX", finishedCount: 0, rank: null },
            yellow: { isAi: true, name: "BOT ALEX", finishedCount: 0, rank: null },
            red:    { isAi: true, name: "BOT VIPER", finishedCount: 0, rank: null }
          };
        }
      } else if (this.gameType === 'online') {
        if (this.onlineManager) {
          this.onlineManager.startMatchmaking('auto');
        }
        return;
      }

      this.updateProfileCardsAndBoardVisibility();
      this.restartGame();
    }

    updateProfileCardsAndBoardVisibility() {
      COLORS.forEach(color => {
        const card = document.getElementById(`profile-${color}`);
        const isActive = this.activeColors.includes(color);

        if (card) {
          card.classList.toggle('hidden', !isActive);
          const nameEl = card.querySelector('.player-name');
          const botBadge = card.querySelector('.bot-badge');
          const youBadge = card.querySelector('.you-badge');

          const pData = this.players[color];
          if (nameEl && pData) {
            let label = pData.name.toUpperCase();
            if (this.subMode === '2v2' && this.teams) {
              label += ` (${this.teams[color] === 'A' ? 'T-A' : 'T-B'})`;
            }
            nameEl.innerText = label;
          }

          if (botBadge && pData) botBadge.classList.toggle('hidden', !pData.isAi);
          if (youBadge && pData) youBadge.classList.toggle('hidden', pData.isAi || this.gameType === 'pass_play');

          if (this.gameType === 'pass_play' && isActive) {
            card.classList.add('nameplate-editable');
          } else {
            card.classList.remove('nameplate-editable');
          }
        }

        // Yard element styling
        const yardEl = document.querySelector(`.yard-${color}`);
        if (yardEl) {
          yardEl.classList.toggle('inactive-yard', !isActive);
        }

        // Token elements visibility
        this.tokens[color].forEach(token => {
          const tokenEl = document.getElementById(`token-${color}-${token.id}`);
          if (tokenEl) {
            tokenEl.classList.toggle('hidden', !isActive);
          }
        });
      });
    }

    // Dynamic Position Calculator for Active Player Dice
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

        if (activeColor === 'red' || activeColor === 'yellow') {
          const minY = boardBottomInViewport + 4;
          if (y < minY) y = minY;
        } else if (activeColor === 'green' || activeColor === 'blue') {
          const maxY = boardTopInViewport - 4 - diceHeight;
          if (y > maxY) y = maxY;
        }
      }

      const maxViewportX = viewportRect.width - diceWidth - 4;
      if (x < 4) x = 4;
      if (x > maxViewportX) x = maxViewportX;

      this.diceBox.style.transform = `translate3d(${Math.round(x)}px, ${Math.round(y)}px, 0)`;

      const isMyTurn = !this.isOnlineActive || (activeColor === this.myColor);
      if (!this.players[activeColor].isAi && isMyTurn && this.phase === 'WAIT_ROLL') {
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
        if (!this.activeColors.includes(color)) return;
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
        const isActive = this.activeColors.includes(color);
        this.tokens[color].forEach(token => {
          const tokenEl = document.getElementById(`token-${color}-${token.id}`);
          if (!tokenEl) return;

          if (!isActive) {
            tokenEl.classList.add('hidden');
            return;
          } else {
            tokenEl.classList.remove('hidden');
          }

          if (token.position === -1) {
            const socketEl = document.getElementById(`socket-${color}-${token.id}`);
            if (socketEl && tokenEl.parentElement !== socketEl) {
              socketEl.appendChild(tokenEl);
            }
            tokenEl.style.gridArea = 'none';
            tokenEl.style.transform = 'translate(-50%, -50%)';
          } else {
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
        if (this.phase === 'WAIT_ROLL' && !this.isRolling) {
          if (this.isOnlineActive) {
            if (this.getCurrentColor() === this.myColor) {
              this.rollDice();
            }
          } else if (!this.players[this.getCurrentColor()].isAi) {
            this.rollDice();
          }
        }
      };

      if (this.btnRoll) this.btnRoll.addEventListener('click', triggerRoll);
      if (this.diceBox) this.diceBox.addEventListener('click', triggerRoll);

      if (this.btnBack) {
        this.btnBack.addEventListener('click', () => {
          this.showMainMenu();
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
          const isMyTurn = !this.isOnlineActive || (color === this.myColor);
          if (isMyTurn) {
            this.onTokenSelected(color, id);
          }
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
          this.gameType = 'online';
          this.subMode = 'auto';
          this.applyAndStartGame();
        });
      }

      document.getElementById('btn-restart-game').addEventListener('click', () => {
        this.modalSettings.classList.add('hidden');
        this.restartGame();
      });

      const btnOpenMenu = document.getElementById('btn-open-main-menu');
      if (btnOpenMenu) {
        btnOpenMenu.addEventListener('click', () => {
          this.modalSettings.classList.add('hidden');
          this.showMainMenu();
        });
      }

      document.getElementById('btn-play-again').addEventListener('click', () => {
        this.modalWinner.classList.add('hidden');
        this.showMainMenu();
      });

      // Step 1 Game Type Selection
      document.getElementById('btn-select-passplay')?.addEventListener('click', () => {
        this.gameType = 'pass_play';
        this.showMenuStep(2);
      });

      document.getElementById('btn-select-vscomputer')?.addEventListener('click', () => {
        this.gameType = 'vs_ai';
        this.showMenuStep(2);
      });

      document.getElementById('btn-select-online')?.addEventListener('click', () => {
        this.gameType = 'online';
        this.subMode = 'auto';
        this.applyAndStartGame();
      });

      // Step 2 Sub-Mode Selection
      document.getElementById('btn-back-step1')?.addEventListener('click', () => {
        this.showMenuStep(1);
      });

      document.getElementById('btn-mode-1v1')?.addEventListener('click', () => {
        this.subMode = '1v1';
        if (this.gameType === 'pass_play') {
          this.showMenuStep(3);
        } else {
          this.applyAndStartGame();
        }
      });

      document.getElementById('btn-mode-1v2')?.addEventListener('click', () => {
        this.subMode = '1v2';
        if (this.gameType === 'pass_play') {
          this.showMenuStep(3);
        } else {
          this.applyAndStartGame();
        }
      });

      document.getElementById('btn-mode-1v4')?.addEventListener('click', () => {
        this.subMode = '1v4';
        if (this.gameType === 'pass_play') {
          this.showMenuStep(3);
        } else {
          this.applyAndStartGame();
        }
      });

      // Step 3 Player Names Back & Start
      document.getElementById('btn-back-step2')?.addEventListener('click', () => {
        this.showMenuStep(2);
      });

      document.getElementById('btn-start-game')?.addEventListener('click', () => {
        this.applyAndStartGame();
      });

      // Allow tapping player nameplate in Pass & Play to quick-edit name
      COLORS.forEach(color => {
        const card = document.getElementById(`profile-${color}`);
        if (card) {
          card.addEventListener('click', () => {
            if (this.gameType === 'pass_play' && this.activeColors.includes(color)) {
              const oldName = this.players[color].name;
              const newName = prompt(`Edit name for ${color.toUpperCase()}:`, oldName);
              if (newName && newName.trim().length > 0) {
                const trimmed = newName.trim().substring(0, 12);
                this.players[color].name = trimmed;
                this.updateProfileCardsAndBoardVisibility();
                this.showToast(`Updated ${color.toUpperCase()}'s name to ${trimmed}`);
              }
            }
          });
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

      this.stopTurnTimer();

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

        const isMyTurn = !this.isOnlineActive || (this.getCurrentColor() === this.myColor);
        if (this.isOnlineActive && this.onlineManager && isMyTurn) {
          this.onlineManager.sendAction('ROLL', { diceVal: this.diceValue, color: this.getCurrentColor() });
        }

        this.handleDiceResult();
      }, 500);
    }

    handleDiceResult() {
      const currentColor = this.getCurrentColor();
      const isMyTurn = !this.isOnlineActive || (currentColor === this.myColor);

      if (this.diceValue === 6) {
        this.consecutiveSixes++;
        this.sound.playSix();
      } else {
        this.consecutiveSixes = 0;
      }

      if (this.consecutiveSixes === 3) {
        this.showToast("3 Sixes in a row! Turn forfeited.");
        this.consecutiveSixes = 0;
        this.stopTurnTimer();
        setTimeout(() => this.nextTurn(), 1200);
        return;
      }

      const validTokens = this.getValidMoveTokens(currentColor, this.diceValue);

      if (validTokens.length === 0) {
        if (isMyTurn) {
          this.statusMsg.innerText = `Rolled a ${this.diceValue}. No valid moves!`;
        } else {
          this.statusMsg.innerText = `Opponent rolled ${this.diceValue}. No valid moves!`;
        }
        this.stopTurnTimer();
        setTimeout(() => this.nextTurn(), 1000);
      } else {
        this.phase = 'WAIT_MOVE';
        if (isMyTurn) {
          this.statusMsg.innerText = `Rolled a ${this.diceValue}! Select a token.`;
        } else {
          this.statusMsg.innerText = `Opponent's turn (Rolled ${this.diceValue})...`;
        }

        this.clearTokenHighlights();
        if (isMyTurn) {
          validTokens.forEach(t => {
            const el = document.getElementById(`token-${currentColor}-${t.id}`);
            if (el) el.classList.add('token-movable');
          });
        }

        this.startTurnTimer(30);

        if (this.players[currentColor].isAi) {
          setTimeout(() => this.makeAiMove(validTokens), 600);
        } else if (isMyTurn && validTokens.length === 1 && this.autoMoveSingle) {
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
            this.showToast(`${this.players[color].name.toUpperCase()} token reached Home!`);
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
        if (!this.activeColors.includes(otherColor)) return;

        // In 2v2 Team mode, teammates CANNOT capture each other!
        if (this.subMode === '2v2' && this.teams && this.teams[movedToken.color] === this.teams[otherColor]) {
          return;
        }

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

        this.startTurnTimer(10);

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

      this.stopTurnTimer();

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
              if (oc !== token.color && this.activeColors.includes(oc)) {
                if (this.subMode === '2v2' && this.teams && this.teams[token.color] === this.teams[oc]) {
                  // Do not target teammate
                } else {
                  this.tokens[oc].forEach(ot => {
                    if (ot.position >= 0 && ot.position <= 50) {
                      const oMain = (START_MAIN_INDEX[oc] + ot.position) % 52;
                      if (oMain === targetMainIdx) score += 100;
                    }
                  });
                }
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

      this.stopTurnTimer();
      this.extraRollBadge.classList.add('hidden');
      this.consecutiveSixes = 0;

      let attempts = 0;
      do {
        this.turnIndex = (this.turnIndex + 1) % COLORS.length;
        attempts++;
      } while (
        (!this.activeColors.includes(this.getCurrentColor()) ||
         this.players[this.getCurrentColor()].finishedCount === 4) &&
        attempts < COLORS.length * 2
      );

      if (this.checkGameFinishCondition()) {
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

    checkGameFinishCondition() {
      if (this.subMode === '2v2' && this.teams) {
        const teamAFinished = (this.players.green.finishedCount + this.players.yellow.finishedCount) === 8;
        const teamBFinished = (this.players.blue.finishedCount + this.players.red.finishedCount) === 8;
        return teamAFinished || teamBFinished;
      }

      const activeFinished = this.activeColors.filter(c => this.players[c].finishedCount === 4).length;
      return activeFinished >= this.activeColors.length - 1;
    }

    checkWinCondition(color) {
      if (this.subMode === '2v2' && this.teams) {
        const team = this.teams[color];
        const teamColors = team === 'A' ? ['green', 'yellow'] : ['blue', 'red'];
        const totalTeamFinished = teamColors.reduce((sum, c) => sum + this.players[c].finishedCount, 0);

        if (totalTeamFinished === 8) {
          setTimeout(() => this.endGame(`TEAM ${team}`), 800);
          return;
        }
      }

      if (this.players[color].finishedCount === 4 && !this.finishedRankList.includes(color)) {
        this.finishedRankList.push(color);
        this.players[color].rank = this.finishedRankList.length;

        this.showToast(`${this.players[color].name.toUpperCase()} Finished #${this.players[color].rank}!`);

        if (this.checkGameFinishCondition()) {
          setTimeout(() => this.endGame(), 1000);
        }
      }
    }

    endGame(customWinnerName) {
      this.phase = 'GAME_OVER';
      this.stopTurnTimer();
      this.sound.playVictory();

      let winnerText = customWinnerName;
      if (!winnerText) {
        const winnerColor = this.finishedRankList[0] || 'yellow';
        winnerText = this.players[winnerColor]?.name || winnerColor.toUpperCase();
      }

      document.getElementById('winner-title').innerText = `${winnerText.toUpperCase()} WINS!`;
      document.getElementById('winner-desc').innerText = `${winnerText.toUpperCase()} has conquered the board!`;

      const leaderboardEl = document.getElementById('leaderboard-list');
      leaderboardEl.innerHTML = '';

      this.activeColors.forEach(c => {
        const item = document.createElement('div');
        item.className = `rank-item ${c}`;
        const rank = this.finishedRankList.indexOf(c) + 1;
        const rankStr = rank > 0 ? `#${rank}` : 'DNF';
        const pName = this.players[c]?.name || c.toUpperCase();
        item.innerHTML = `<span>${pName.toUpperCase()}</span><span>${rankStr}</span>`;
        leaderboardEl.appendChild(item);
      });

      this.modalWinner.classList.remove('hidden');
    }

    restartGame(resetAll = true) {
      this.stopTurnTimer();

      // Find initial turn index from active colors
      this.turnIndex = COLORS.indexOf(this.activeColors[0] || 'green');
      this.diceValue = 6;
      this.phase = 'WAIT_ROLL';
      this.consecutiveSixes = 0;
      this.grantExtraRoll = false;
      this.isRolling = false;
      this.finishedRankList = [];

      COLORS.forEach(color => {
        if (resetAll || this.activeColors.includes(color)) {
          this.players[color].finishedCount = 0;
          this.players[color].rank = null;
        }
        this.tokens[color].forEach(t => t.position = -1);

        const countEl = document.getElementById(`count-${color}`);
        if (countEl) countEl.innerText = '0';
      });

      this.updateProfileCardsAndBoardVisibility();
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
      const pData = this.players[activeColor];
      const isMyTurn = !this.isOnlineActive || (activeColor === this.myColor);

      // Update Turn Banner
      this.turnBanner.className = `turn-banner turn-${activeColor}`;
      const activePlayerName = pData?.name || activeColor.toUpperCase();

      if (this.isOnlineActive) {
        this.turnText.innerText = isMyTurn ? `YOUR TURN (${activeColor.toUpperCase()})` : `${activePlayerName.toUpperCase()}'S TURN`;
      } else {
        this.turnText.innerText = `${activePlayerName.toUpperCase()}'S TURN`;
      }

      // Update Profile Cards Active Glow
      COLORS.forEach(color => {
        const card = document.getElementById(`profile-${color}`);
        if (card) {
          if (color === activeColor && this.activeColors.includes(color)) {
            card.classList.add('active');
          } else {
            card.classList.remove('active');
          }
        }
      });

      this.positionDice(activeColor);

      if (this.phase === 'WAIT_ROLL') {
        if (isMyTurn) {
          this.statusMsg.innerText = pData && pData.isAi ? "BOT thinking..." : "Tap Dice to Roll";
        } else {
          this.statusMsg.innerText = "Opponent's turn...";
        }
        this.startTurnTimer(10);
      }
    }

    showToast(msg, duration = 3500) {
      this.toastMsg.innerText = msg;
      this.toastMsg.classList.remove('hidden');
      if (this.toastTimeout) clearTimeout(this.toastTimeout);
      this.toastTimeout = setTimeout(() => {
        this.toastMsg.classList.add('hidden');
      }, duration);
    }

    startOnlineGame(roomData, mySlot, userInfo) {
      this.isOnlineActive = true;
      this.mySlot = mySlot;

      if (this.subMode === '1v1') {
        this.myColor = (mySlot === 0 ? 'green' : 'yellow');
        this.activeColors = ['green', 'yellow'];
        this.teams = null;
      } else if (this.subMode === '1v2') {
        this.myColor = (mySlot === 0 ? 'green' : mySlot === 1 ? 'blue' : 'yellow');
        this.activeColors = ['green', 'blue', 'yellow'];
        this.teams = null;
      } else {
        this.myColor = COLORS[mySlot];
        this.activeColors = ['green', 'blue', 'yellow', 'red'];
        this.teams = null;
      }

      COLORS.forEach(c => {
        this.players[c] = { isAi: true, name: 'INACTIVE', finishedCount: 4, rank: null };
      });

      if (roomData && roomData.players) {
        roomData.players.forEach((p, idx) => {
          let color = COLORS[idx];
          if (this.subMode === '1v1') {
            color = (idx === 0 ? 'green' : 'yellow');
          } else if (this.subMode === '1v2') {
            color = (idx === 0 ? 'green' : idx === 1 ? 'blue' : 'yellow');
          }
          const hasUid = p && p.uid && p.uid.length > 0;
          const isBot = p ? (p.isBot || !hasUid) : true;
          const isMe = (idx === mySlot);
          const isRemote = !isMe && !isBot;

          this.players[color] = {
            isAi: isBot,
            isRemote: isRemote,
            name: isMe ? (this.userInfo ? (this.userInfo.name || "YOU") : "YOU") : (hasUid ? (p.name || color.toUpperCase()) : `BOT ${color.toUpperCase()}`),
            avatar: (p && p.avatar && p.avatar.length > 0) ? p.avatar : `assets/avatar_p${idx + 1}.svg`,
            finishedCount: 0,
            rank: null
          };

          const card = document.getElementById(`profile-${color}`);
          if (card) {
            const nameEl = card.querySelector('.player-name');
            const avatarEl = card.querySelector('.avatar-img');
            const botBadge = card.querySelector('.bot-badge');
            const youBadge = card.querySelector('.you-badge');

            if (nameEl) nameEl.innerText = this.players[color].name.toUpperCase();
            if (avatarEl && this.players[color].avatar) avatarEl.src = this.players[color].avatar;
            if (botBadge) botBadge.classList.toggle('hidden', !isBot);
            if (youBadge) youBadge.classList.toggle('hidden', !isMe);
          }
        });
      }

      this.updateProfileCardsAndBoardVisibility();
      this.restartGame(false);
      this.showToast(`⚡ Online Match Connected (${this.subMode})!`);
    }

    handleRemoteAction(action) {
      if (!action || !action.timestamp || action.timestamp <= this.lastProcessedTimestamp) return;
      this.lastProcessedTimestamp = action.timestamp;

      if (action.slot === this.mySlot) return; // Local action

      this.stopTurnTimer();

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
      this.timerSeconds = 25;
      this.timerInterval = null;
      this.subMode = 'auto';

      this.initUiReferences();
      this.bindEvents();
      this.checkBridge();
    }

    initUiReferences() {
      this.btnFindFriends = document.getElementById('btn-find-friends');
      this.modalMatchmaking = document.getElementById('modal-matchmaking');
      this.btnCancelMatchmaking = document.getElementById('btn-cancel-matchmaking');
      this.btnStartNow = document.getElementById('btn-start-now');
      this.statusText = document.getElementById('matchmaking-status-text');
      this.timerBadge = document.getElementById('matchmaking-timer');
      this.matchmakingSubtitle = document.querySelector('.matchmaking-subtitle');
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
      if (this.btnCancelMatchmaking) {
        this.btnCancelMatchmaking.addEventListener('click', () => {
          this.cancelMatchmaking(true);
        });
      }

      if (this.btnStartNow) {
        this.btnStartNow.addEventListener('click', () => {
          if (window.AndroidLudo) {
            window.AndroidLudo.forceStartMatch();
          } else {
            this.handleMatchmakingTimeout();
          }
        });
      }

      window.onLudoRoomUpdated = (roomData) => {
        this.onRoomUpdated(roomData);
      };
    }

    startMatchmaking(subMode = 'auto') {
      this.isOnline = true;
      this.subMode = subMode;

      if (this.modalMatchmaking) this.modalMatchmaking.classList.remove('hidden');

      if (this.matchmakingSubtitle) {
        this.matchmakingSubtitle.innerText = "Searching Online Players (1 to 4 Players)";
      }

      this.resetSlotCards();

      if (this.statusText) {
        this.statusText.innerText = "Finding online players... (1/4)";
      }

      // 25 seconds countdown timer
      this.timerSeconds = 25;
      if (this.timerBadge) this.timerBadge.innerText = `${this.timerSeconds}s`;

      if (this.timerInterval) clearInterval(this.timerInterval);
      this.timerInterval = setInterval(() => {
        this.timerSeconds--;
        if (this.timerBadge) this.timerBadge.innerText = `${this.timerSeconds}s`;

        if (this.timerSeconds <= 0) {
          clearInterval(this.timerInterval);
          this.handleMatchmakingTimeout();
        }
      }, 1000);

      if (window.AndroidLudo) {
        window.AndroidLudo.findMatch('auto');
      } else {
        this.simulateWebMatchmaking('auto');
      }
    }

    getConnectedPlayerCount() {
      let count = 0;
      for (let i = 0; i < 4; i++) {
        const slotCard = document.getElementById(`slot-card-${i}`);
        if (slotCard && slotCard.classList.contains('slot-found')) {
          count++;
        }
      }
      return count > 0 ? count : 1;
    }

    handleMatchmakingTimeout() {
      if (!this.isOnline) return;

      const connectedCount = this.getConnectedPlayerCount();

      if (connectedCount >= 2) {
        if (window.AndroidLudo) {
          window.AndroidLudo.forceStartMatch();
        } else {
          const finalSubMode = (connectedCount === 2) ? '1v1' : (connectedCount === 3) ? '1v2' : '4v4';
          this.game.subMode = finalSubMode;
          const mockPlayers = [];
          for (let s = 0; s < connectedCount; s++) {
            mockPlayers.push({
              slot: s,
              uid: (s === 0 ? this.userInfo.uid : `p_${s}`),
              name: (s === 0 ? (this.userInfo.name || "YOU") : `Player ${s + 1}`),
              avatar: (s === 0 ? (this.userInfo.avatar || "assets/avatar_p4.svg") : `assets/avatar_p${s + 1}.svg`),
              isBot: false
            });
          }
          const mockRoom = {
            roomId: "web_room_1",
            state: "PLAYING",
            subMode: finalSubMode,
            mySlot: 0,
            players: mockPlayers
          };
          this.onRoomUpdated(mockRoom);
        }
      } else {
        this.cancelMatchmaking(false);
        this.game.showToast("No user available, please try again!", 4000);
        this.game.showMainMenu();
      }
    }

    simulateWebMatchmaking(subMode) {
      let found = 1;
      const names = ["Aarav", "Neha", "Rohan", "Priya"];

      const interval = setInterval(() => {
        if (!this.isOnline) {
          clearInterval(interval);
          return;
        }

        if (Math.random() > 0.3 && found < 4) {
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

          if (this.statusText) {
            this.statusText.innerText = `Finding online players... (${found}/4)`;
          }
        }

        if (found >= 4) {
          clearInterval(interval);
          if (this.timerInterval) clearInterval(this.timerInterval);

          setTimeout(() => {
            if (this.modalMatchmaking) this.modalMatchmaking.classList.add('hidden');

            const mockPlayers = [];
            for (let s = 0; s < 4; s++) {
              mockPlayers.push({
                slot: s,
                uid: s === 0 ? this.userInfo.uid : `p_${s}`,
                name: s === 0 ? (this.userInfo.name || "YOU") : names[s - 1],
                avatar: s === 0 ? (this.userInfo.avatar || "assets/avatar_p4.svg") : `assets/avatar_p${s + 1}.svg`,
                isBot: false
              });
            }

            const mockRoom = {
              roomId: "web_room_1",
              state: "PLAYING",
              subMode: "4v4",
              mySlot: 0,
              players: mockPlayers
            };
            this.onRoomUpdated(mockRoom);
          }, 800);
        }
      }, 2000);
    }

    resetSlotCards() {
      if (this.btnStartNow) this.btnStartNow.classList.add('hidden');

      for (let i = 0; i < 4; i++) {
        const slotCard = document.getElementById(`slot-card-${i}`);
        const slotName = document.getElementById(`slot-name-${i}`);
        const slotStatus = document.getElementById(`slot-status-${i}`);
        const slotAvatar = document.getElementById(`slot-avatar-${i}`);
        const slotBadge = slotCard ? slotCard.querySelector('.slot-badge') : null;

        if (slotCard) {
          slotCard.classList.remove('hidden');
          slotCard.className = (i === 0) ? 'match-slot-card slot-found' : 'match-slot-card slot-searching';
        }

        if (i === 0) {
          if (slotName) slotName.innerText = this.userInfo ? (this.userInfo.name || "YOU") : "YOU";
          if (slotStatus) {
            slotStatus.className = 'slot-status status-ready';
            slotStatus.innerText = 'CONNECTED';
          }
          if (slotBadge) {
            slotBadge.className = 'slot-badge badge-you';
            slotBadge.innerText = 'YOU';
          }
          if (slotAvatar && this.userInfo && this.userInfo.avatar && this.userInfo.avatar.length > 0) {
            slotAvatar.src = this.userInfo.avatar;
          }
        } else {
          if (slotName) slotName.innerText = "Searching...";
          if (slotStatus) {
            slotStatus.className = 'slot-status status-wait';
            slotStatus.innerText = 'SEARCHING';
          }
          if (slotBadge) {
            slotBadge.className = 'slot-badge';
            slotBadge.innerText = `P${i + 1}`;
          }
          if (slotAvatar) {
            slotAvatar.src = `assets/avatar_p${i + 1}.svg`;
          }
        }
      }
    }

    cancelMatchmaking(showMenu = true) {
      if (this.timerInterval) clearInterval(this.timerInterval);
      if (this.modalMatchmaking) this.modalMatchmaking.classList.add('hidden');
      if (this.btnStartNow) this.btnStartNow.classList.add('hidden');
      this.isOnline = false;

      if (window.AndroidLudo) {
        window.AndroidLudo.leaveMatch();
      }

      if (showMenu) {
        this.game.showMainMenu();
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
          if (idx >= 4) return;

          const slotCard = document.getElementById(`slot-card-${idx}`);
          const slotName = document.getElementById(`slot-name-${idx}`);
          const slotStatus = document.getElementById(`slot-status-${idx}`);
          const slotAvatar = document.getElementById(`slot-avatar-${idx}`);
          const slotBadge = slotCard ? slotCard.querySelector('.slot-badge') : null;

          const hasPlayer = p && p.uid && p.uid.length > 0;

          if (hasPlayer) {
            connectedCount++;
            const isMe = (idx === this.mySlot || p.uid === this.userInfo.uid);

            if (slotCard) {
              slotCard.classList.remove('hidden');
              slotCard.className = 'match-slot-card slot-found';
            }
            if (slotName) {
              slotName.innerText = isMe ? (this.userInfo ? (this.userInfo.name || "YOU") : "YOU") : (p.name || `Player ${idx + 1}`);
            }
            if (slotBadge) {
              if (isMe) {
                slotBadge.className = 'slot-badge badge-you';
                slotBadge.innerText = 'YOU';
              } else {
                slotBadge.className = 'slot-badge';
                slotBadge.innerText = `P${idx + 1}`;
              }
            }
            if (slotStatus) {
              slotStatus.className = 'slot-status status-ready';
              slotStatus.innerText = p.isBot ? 'BOT' : 'CONNECTED';
            }
            if (slotAvatar && p.avatar && p.avatar.length > 0) {
              slotAvatar.src = p.avatar;
            }
          } else {
            if (slotCard) {
              slotCard.classList.remove('hidden');
              slotCard.className = 'match-slot-card slot-searching';
            }
            if (slotName) {
              slotName.innerText = "Searching...";
            }
            if (slotBadge) {
              slotBadge.className = 'slot-badge';
              slotBadge.innerText = `P${idx + 1}`;
            }
            if (slotStatus) {
              slotStatus.className = 'slot-status status-wait';
              slotStatus.innerText = 'SEARCHING';
            }
            if (slotAvatar) {
              slotAvatar.src = `assets/avatar_p${idx + 1}.svg`;
            }
          }
        });

        if (this.statusText) {
          this.statusText.innerText = `Finding online players... (${connectedCount}/4)`;
        }

        if (this.btnStartNow) {
          if (connectedCount >= 2) {
            this.btnStartNow.classList.remove('hidden');
          } else {
            this.btnStartNow.classList.add('hidden');
          }
        }
      }

      if (roomData.state === "PLAYING" && !this.game.isOnlineActive) {
        if (this.timerInterval) clearInterval(this.timerInterval);
        if (this.modalMatchmaking) this.modalMatchmaking.classList.add('hidden');
        if (this.btnStartNow) this.btnStartNow.classList.add('hidden');

        const activeCount = roomData.players ? roomData.players.filter(p => p && p.uid && p.uid.length > 0).length : 4;
        const finalSubMode = (roomData.subMode && roomData.subMode !== 'auto') ? roomData.subMode : (activeCount === 2 ? '1v1' : activeCount === 3 ? '1v2' : '4v4');
        this.game.subMode = finalSubMode;
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

  // Handle Android Native Back Button
  window.handleAndroidBack = function() {
    if (!window.ludoGameInstance) return false;
    const game = window.ludoGameInstance;

    // 1. If any overlay modal is open (Settings, Rules, Winner)
    if (game.modalSettings && !game.modalSettings.classList.contains('hidden')) {
      game.modalSettings.classList.add('hidden');
      return true;
    }
    if (game.modalRules && !game.modalRules.classList.contains('hidden')) {
      game.modalRules.classList.add('hidden');
      return true;
    }
    if (game.modalWinner && !game.modalWinner.classList.contains('hidden')) {
      game.modalWinner.classList.add('hidden');
      return true;
    }

    // 2. If inside Main Menu steps
    if (game.modalMainMenu && !game.modalMainMenu.classList.contains('hidden')) {
      if (game.menuStep3 && !game.menuStep3.classList.contains('hidden')) {
        game.showMenuStep(2);
        return true;
      }
      if (game.menuStep2 && !game.menuStep2.classList.contains('hidden')) {
        game.showMenuStep(1);
        return true;
      }
      return false;
    }

    // 3. If playing a game on board, back button shows main menu
    game.showMainMenu();
    return true;
  };

})();
