/**
 * LUCKY FRUIT JACKPOT GAME
 * HTML5 / CSS3 / Vanilla JavaScript Game Engine
 */

// GAME CONFIGURATION
const CONFIG = {
    initialBalance: 35985,
    initialJackpot: 20232558,
    initialRound: 8,
    centerDefaultNumber: 18,
    spinDuration: 3200, // Duration in ms
    countdownDuration: 30, // Auto-predict interval in seconds
    quantities: [
        { label: '1K', value: 1000 },
        { label: '10K', value: 10000 },
        { label: '100K', value: 100000 },
        { label: '500K', value: 500000 }
    ],
    // Perimeter grid sequence indices: 0, 1, 2, 5, 8, 7, 6, 3
    perimeterSequence: [0, 1, 2, 5, 8, 7, 6, 3],
    fruits: {
        0: { id: 'orange', name: 'Orange', emoji: '🍊', iconUrl: 'assets/fruit_orange.svg', multiplier: 5 },
        1: { id: 'lemon', name: 'Lemon', emoji: '🍋', iconUrl: 'assets/fruit_lemon.svg', multiplier: 5 },
        2: { id: 'grapes', name: 'Grapes', emoji: '🍇', iconUrl: 'assets/fruit_grapes.svg', multiplier: 5 },
        3: { id: 'cherries', name: 'Cherries', emoji: '🍒', iconUrl: 'assets/fruit_cherries.svg', multiplier: 5 },
        5: { id: 'apple', name: 'Green Apple', emoji: '🍏', iconUrl: 'assets/fruit_apple.svg', multiplier: 10 },
        6: { id: 'watermelon', name: 'Watermelon', emoji: '🍉', iconUrl: 'assets/fruit_watermelon.svg', multiplier: 15 },
        7: { id: 'mango', name: 'Mango', emoji: '🥭', iconUrl: 'assets/fruit_mango.svg', multiplier: 25 },
        8: { id: 'strawberry', name: 'Strawberry', emoji: '🍓', iconUrl: 'assets/fruit_strawberry.svg', multiplier: 45 }
    }
};

// GAME STATE
let state = {
    balance: CONFIG.initialBalance,
    todaysWinnings: 0,
    jackpot: CONFIG.initialJackpot,
    currentRound: CONFIG.initialRound,
    selectedQuantity: 1000,
    tileBets: {
        0: 0,
        1: 0,
        2: 0,
        3: 0,
        5: 0,
        6: 0,
        7: 0,
        8: 0
    },
    recentResults: [0, 5, 1, 0, 2, 1, 2],
    isSpinning: false,
    gameState: 'BETTING', // 'BETTING' | 'SPINNING'
    testMode: false,
    testNextWinIndex: 1, // Default Lemon win for test mode
    countdownSeconds: 30,
    showResultInCenter: false
};

// DOM ELEMENTS
const DOM = {
    jackpotValue: document.getElementById('jackpotValue'),
    roundCounter: document.getElementById('roundCounter'),
    balanceDisplay: document.getElementById('balanceDisplay'),
    winningsDisplay: document.getElementById('winningsDisplay'),
    centerDisplay: document.getElementById('centerDisplay'),
    fruitGrid: document.getElementById('fruitGrid'),
    timerValue: document.getElementById('timerValue'),
    clearBetsBtn: document.getElementById('clearBetsBtn'),
    testModeBtn: document.getElementById('testModeBtn'),
    resultsStrip: document.getElementById('resultsStrip'),
    toastNotification: document.getElementById('toastNotification'),
    toastMessage: document.getElementById('toastMessage'),
    winModal: document.getElementById('winModal'),
    winFruitIcon: document.getElementById('winFruitIcon'),
    winMultiplierText: document.getElementById('winMultiplierText'),
    winAmountText: document.getElementById('winAmountText'),
    winCloseBtn: document.getElementById('winCloseBtn'),
    ledContainer: document.getElementById('ledContainer'),
    chipBtns: document.querySelectorAll('.chip-btn'),
    leaderboardBtn: document.getElementById('leaderboardBtn'),
    leaderboardModal: document.getElementById('leaderboardModal'),
    closeLeaderboardBtn: document.getElementById('closeLeaderboardBtn'),
    lbTabCurrent: document.getElementById('lbTabCurrent'),
    lbTabPrev: document.getElementById('lbTabPrev'),
    leaderboardList: document.getElementById('leaderboardList'),
    timerLabel: document.querySelector('.timer-label')
};

// WEB AUDIO API SYNTHESIZER
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
        } else if (type === 'spinTick') {
            osc.type = 'sine';
            osc.frequency.setValueAtTime(900, now);
            gain.gain.setValueAtTime(0.2, now);
            gain.gain.linearRampToValueAtTime(0.01, now + 0.05);
            osc.start(now);
            osc.stop(now + 0.05);
        } else if (type === 'win') {
            osc.type = 'triangle';
            osc.frequency.setValueAtTime(523.25, now);
            osc.frequency.setValueAtTime(659.25, now + 0.1);
            osc.frequency.setValueAtTime(783.99, now + 0.2);
            osc.frequency.setValueAtTime(1046.50, now + 0.3);
            gain.gain.setValueAtTime(0.4, now);
            gain.gain.linearRampToValueAtTime(0.01, now + 0.6);
            osc.start(now);
            osc.stop(now + 0.6);
        }
    } catch (e) {
        console.warn('Audio play error:', e);
    }
}

function initMultiplierBadges() {
    const tiles = document.querySelectorAll('.fruit-tile');
    tiles.forEach(tile => {
        const idx = tile.getAttribute('data-index');
        if (idx !== null && CONFIG.fruits[idx]) {
            let badge = tile.querySelector('.multiplier-badge');
            if (!badge) {
                badge = document.createElement('div');
                badge.className = 'multiplier-badge';
                tile.appendChild(badge);
            }
            badge.textContent = `${CONFIG.fruits[idx].multiplier}x`;
        }
    });
}

function startLuckyFruitApp() {
    initLEDs();
    initParticleCanvas();
    initMultiplierBadges();
    updateUI();
    renderRecentResults();
    attachEventListeners();
    startCountdownTimer();

    // Check for Android Bridge to load real user coin balance & user profile
    if (window.AndroidLuckyFruit) {
        window.updateLuckyFruitBalance = function(newBalance) {
            if (typeof newBalance === 'number' && !isNaN(newBalance)) {
                state.balance = newBalance;
                updateUI();
            }
        };
        window.updateLuckyFruitUserProfile = function(jsonStr) {
            try {
                const profile = typeof jsonStr === 'string' ? JSON.parse(jsonStr) : jsonStr;
                if (profile && profile.userId) {
                    state.user = profile;
                }
            } catch(e) {}
        };
        try {
            window.AndroidLuckyFruit.requestUserCoins();
            if (typeof window.AndroidLuckyFruit.requestUserProfile === 'function') {
                const cbName = '_cb_uf_' + Date.now();
                window[cbName] = function(jsonStr) {
                    delete window[cbName];
                    window.updateLuckyFruitUserProfile(jsonStr);
                };
                window.AndroidLuckyFruit.requestUserProfile(cbName);
            }
        } catch(e) {
            console.error("Error requesting user info:", e);
        }
    }
}

if (document.readyState === 'complete' || document.readyState === 'interactive') {
    startLuckyFruitApp();
} else {
    document.addEventListener('DOMContentLoaded', startLuckyFruitApp);
}

let timerInterval = null;
let currentRoundIdx = -1;
let spinTriggeredRound = -1;

// WALL-CLOCK SYNCHRONIZED COUNTDOWN TIMER & OUTCOME SEEDING
const CYCLE_DURATION = 40; // 30s betting + 10s spin
const BETTING_DURATION = 30;

function mulberry32(a) {
    return function() {
        let t = a += 0x6D2B79F5;
        t = Math.imul(t ^ (t >>> 15), t | 1);
        t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
        return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
    };
}

function getLuckyFruitOutcome(roundIndex) {
    if (state.testMode) {
        return state.testNextWinIndex;
    }
    const prng = mulberry32(roundIndex * 7919 + 10007);
    const seq = CONFIG.perimeterSequence;
    const pos = Math.floor(prng() * seq.length);
    return seq[pos];
}

function startCountdownTimer() {
    if (timerInterval) clearInterval(timerInterval);
    syncTimer();
    timerInterval = setInterval(syncTimer, 1000);
}

let isFirstSync = true;

function syncTimer() {
    const nowSec = Math.floor(Date.now() / 1000);
    const cyclePos = nowSec % CYCLE_DURATION; // 0 to 39
    const roundIdx = Math.floor(nowSec / CYCLE_DURATION);

    if (isFirstSync) {
        isFirstSync = false;
        currentRoundIdx = roundIdx;
        // Do NOT set spinTriggeredRound = roundIdx on initial sync so spin activates cleanly at 10s mark
    }

    if (cyclePos < BETTING_DURATION) {
        const remainingBet = BETTING_DURATION - cyclePos; // 30 down to 1
        state.countdownSeconds = remainingBet;

        if (state.gameState !== 'BETTING' || roundIdx !== currentRoundIdx) {
            currentRoundIdx = roundIdx;
            state.gameState = 'BETTING';
            state.isSpinning = false;
            document.querySelectorAll('.tile').forEach(t => t.classList.remove('highlighted'));
            clearAllBets();
            updateUI();
        }

        if (DOM.timerLabel) DOM.timerLabel.textContent = "NEXT PREDICTION";
        updateTimerUI();
    } else {
        const remainingSpin = CYCLE_DURATION - cyclePos; // 10 down to 1
        state.countdownSeconds = remainingSpin;

        if (DOM.timerLabel) DOM.timerLabel.textContent = "PREDICTING...";
        updateTimerUI();

        if (spinTriggeredRound !== roundIdx) {
            spinTriggeredRound = roundIdx;
            currentRoundIdx = roundIdx;
            triggerSpin(roundIdx, remainingSpin);
        }
    }
}

// UPDATE COUNTDOWN TIMER DISPLAY
function updateTimerUI() {
    const text = `${state.countdownSeconds}s`;
    if (DOM.timerValue) {
        DOM.timerValue.textContent = text;
    }
    if (!state.showResultInCenter && DOM.centerDisplay) {
        DOM.centerDisplay.textContent = text;
    }
}

// INITIALIZE LED BULBS AROUND CABINET
function initLEDs() {
    const totalLEDs = 20;
    if (!DOM.ledContainer) return;
    DOM.ledContainer.innerHTML = '';
    for (let i = 0; i < totalLEDs; i++) {
        const led = document.createElement('div');
        led.className = 'led-bulb';
        if (i < 6) { // Top row
            led.style.top = '2px';
            led.style.left = `${(i / 5) * 94 + 3}%`;
        } else if (i < 10) { // Right column
            led.style.right = '2px';
            led.style.top = `${((i - 5) / 5) * 94 + 3}%`;
        } else if (i < 16) { // Bottom row
            led.style.bottom = '2px';
            led.style.left = `${((15 - i) / 5) * 94 + 3}%`;
        } else { // Left column
            led.style.left = '2px';
            led.style.top = `${((19 - i) / 4) * 94 + 3}%`;
        }
        DOM.ledContainer.appendChild(led);
    }
}

function handleRequestAddCoins() {
    const totalBet = getTotalBet();
    if (state.isSpinning || state.gameState === 'SPINNING' || totalBet > 0) {
        showToast("Please wait for the result");
        if (window.AndroidLuckyFruit && typeof window.AndroidLuckyFruit.setActiveBetState === 'function') {
            window.AndroidLuckyFruit.setActiveBetState(true);
        }
        return;
    }
    if (window.AndroidLuckyFruit && typeof window.AndroidLuckyFruit.openRechargePage === 'function') {
        window.AndroidLuckyFruit.openRechargePage();
    } else {
        showToast("Opening recharge page...");
    }
}

// ATTACH EVENT LISTENERS
function attachEventListeners() {
    const fruitTiles = document.querySelectorAll('.fruit-tile');
    fruitTiles.forEach(tile => {
        tile.addEventListener('click', () => {
            if (state.isSpinning || state.gameState === 'SPINNING') return;
            const index = parseInt(tile.getAttribute('data-index'));
            placeBetOnTile(index);
        });
    });

    const balanceCard = document.querySelector('.balance-card');
    if (balanceCard) {
        balanceCard.style.cursor = 'pointer';
        balanceCard.addEventListener('click', handleRequestAddCoins);
    }

    DOM.chipBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            const amount = parseInt(btn.getAttribute('data-amount'));
            setSelectedQuantity(amount);
        });
    });

    if (DOM.clearBetsBtn) {
        DOM.clearBetsBtn.addEventListener('click', () => {
            if (state.isSpinning || state.gameState === 'SPINNING') return;
            for (let idx in state.tileBets) {
                state.tileBets[idx] = 0;
            }
            updateBetBadges();
        });
    }

    if (DOM.testModeBtn) {
        DOM.testModeBtn.addEventListener('click', () => {
            state.testMode = !state.testMode;
            DOM.testModeBtn.textContent = `Test: ${state.testMode ? 'ON' : 'OFF'}`;
            DOM.testModeBtn.style.color = state.testMode ? '#00ff88' : '#f3d6ff';
            showToast(state.testMode ? "Test Mode: Wins forced to Lemon (🍋)" : "Standard Mode Active");
        });
    }

    if (DOM.winCloseBtn) {
        DOM.winCloseBtn.addEventListener('click', () => {
            DOM.winModal.classList.add('hidden');
            document.querySelectorAll('.tile').forEach(t => t.classList.remove('highlighted'));
        });
    }

    if (DOM.leaderboardBtn) {
        DOM.leaderboardBtn.addEventListener('click', openLeaderboard);
    }
    if (DOM.closeLeaderboardBtn) {
        DOM.closeLeaderboardBtn.addEventListener('click', () => {
            if (DOM.leaderboardModal) DOM.leaderboardModal.classList.add('hidden');
        });
    }
    if (DOM.lbTabCurrent) {
        DOM.lbTabCurrent.addEventListener('click', () => {
            loadLeaderboardForRound(currentRoundIdx);
        });
    }
}

function openLeaderboard() {
    if (!DOM.leaderboardModal) return;
    DOM.leaderboardModal.classList.remove('hidden');
    loadLeaderboardForRound(currentRoundIdx);
}

function loadLeaderboardForRound(roundIdx) {
    if (!DOM.leaderboardList) return;
    DOM.leaderboardList.innerHTML = '<div class="lb-empty">Loading leaderboard...</div>';

    if (window.AndroidLuckyFruit && typeof window.AndroidLuckyFruit.getRoundLeaderboard === 'function') {
        const callbackName = '_cb_lf_lb_' + Date.now();
        window[callbackName] = function(jsonStr) {
            delete window[callbackName];
            renderLeaderboardItems(jsonStr);
        };
        window.AndroidLuckyFruit.getRoundLeaderboard(roundIdx, callbackName);
    } else {
        renderLeaderboardItems('[]');
    }
}

function renderLeaderboardItems(jsonStr) {
    if (!DOM.leaderboardList) return;
    let list = [];
    try {
        list = JSON.parse(jsonStr || '[]');
    } catch(e) {
        list = [];
    }

    if (!list || list.length === 0) {
        DOM.leaderboardList.innerHTML = '<div class="lb-empty">No participants in this round!</div>';
        return;
    }

    DOM.leaderboardList.innerHTML = '';
    list.forEach(item => {
        const row = document.createElement('div');
        row.className = 'lb-item';

        let rankDisplay = `#${item.rank}`;
        if (item.rank === 1) rankDisplay = '🥇';
        else if (item.rank === 2) rankDisplay = '🥈';
        else if (item.rank === 3) rankDisplay = '🥉';

        const avatarUrl = (item.userAvatar && item.userAvatar.trim() !== '')
            ? item.userAvatar
            : 'assets/gold_coin.svg';

        const betFormatted = (item.betAmount || 0).toLocaleString();
        const winVal = item.winAmount || 0;
        const winFormatted = winVal > 0 ? `+${winVal.toLocaleString()}` : '0';
        const winClass = winVal > 0 ? 'lb-win' : 'lb-win zero';

        row.innerHTML = `
            <div class="lb-left">
                <div class="lb-rank">${rankDisplay}</div>
                <img class="lb-avatar" src="${avatarUrl}" onerror="this.src='assets/gold_coin.svg'" alt="Avatar">
                <div class="lb-name">${escapeHtml(item.userName || 'Player')}</div>
            </div>
            <div class="lb-right">
                <div class="lb-win ${winClass}">Win: ${winFormatted}</div>
                <div class="lb-bet">Bet: ${betFormatted}</div>
            </div>
        `;
        DOM.leaderboardList.appendChild(row);
    });
}

function escapeHtml(str) {
    return String(str || '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

// SET SELECTED BET QUANTITY
function setSelectedQuantity(amount) {
    state.selectedQuantity = amount;
    DOM.chipBtns.forEach(btn => {
        if (parseInt(btn.getAttribute('data-amount')) === amount) {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    });
}

function notifyActiveBetState() {
    let totalBet = getTotalBet();
    const hasActive = totalBet > 0 || state.isSpinning;
    if (window.AndroidLuckyFruit && typeof window.AndroidLuckyFruit.setActiveBetState === 'function') {
        window.AndroidLuckyFruit.setActiveBetState(hasActive);
    }
}

// PLACE BET ON A SPECIFIC TILE
function placeBetOnTile(index) {
    if (state.isSpinning || state.gameState === 'SPINNING') {
        showToast("Wait for the next round!");
        return;
    }
    const totalCurrentBet = getTotalBet();
    if (state.balance < totalCurrentBet + state.selectedQuantity) {
        showToast("Not enough gold coins!");
        return;
    }
    state.tileBets[index] = (state.tileBets[index] || 0) + state.selectedQuantity;
    notifyActiveBetState();
    playSound('chip');
    updateBetBadges();
}

// GET TOTAL BET AMOUNT
function getTotalBet() {
    let total = 0;
    for (let idx in state.tileBets) {
        total += state.tileBets[idx];
    }
    return total;
}

// UPDATE BADGE DISPLAYS ON FRUIT TILES
function updateBetBadges() {
    for (let idx in CONFIG.fruits) {
        const badge = document.getElementById(`badge-${idx}`);
        const tile = document.querySelector(`.tile[data-index="${idx}"]`);
        const betAmount = state.tileBets[idx] || 0;
        if (badge) {
            if (betAmount > 0) {
                badge.textContent = betAmount >= 1000 ? `${betAmount / 1000}K` : betAmount;
                badge.classList.remove('hidden');
            } else {
                badge.classList.add('hidden');
            }
        }
        if (tile) {
            if (betAmount > 0) {
                tile.classList.add('selected-tile');
            } else {
                tile.classList.remove('selected-tile');
            }
        }
    }
}

// CLEAR ALL TILE BETS
function clearAllBets() {
    for (let idx in state.tileBets) {
        state.tileBets[idx] = 0;
    }
    notifyActiveBetState();
    updateBetBadges();
}

// TRIGGER SPIN / PREDICTION ROUND
function triggerSpin(roundIdx, remainingSpinSec) {
    spinTriggeredRound = roundIdx;

    if (DOM.winModal) {
        DOM.winModal.classList.add('hidden');
    }

    let totalBet = getTotalBet();
    const userPlacedBet = (totalBet > 0);

    if (userPlacedBet) {
        if (state.balance < totalBet) {
            showToast("Not enough gold coins!");
            clearAllBets();
            return;
        }

        if (window.AndroidLuckyFruit) {
            const callbackName = '_cb_bet_' + roundIdx + '_' + Date.now();
            window[callbackName] = function(success, newBalance, errorMsg) {
                delete window[callbackName];
                if (success) {
                    if (typeof newBalance === 'number' && !isNaN(newBalance)) {
                        state.balance = newBalance;
                    }
                    state.gameState = 'SPINNING';
                    state.isSpinning = true;
                    updateUI();
                    const winIndex = getLuckyFruitOutcome(roundIdx);
                    animateSpin(winIndex, totalBet, true, remainingSpinSec);
                } else {
                    showToast(errorMsg || "Bet deduction failed!");
                    clearAllBets();
                    state.gameState = 'BETTING';
                    state.isSpinning = false;
                    updateUI();
                }
            };
            window.AndroidLuckyFruit.deductBetCoins(totalBet, roundIdx, callbackName);
            return;
        } else {
            state.balance -= totalBet;
        }
    }

    state.gameState = 'SPINNING';
    state.isSpinning = true;
    updateUI();

    const winIndex = getLuckyFruitOutcome(roundIdx);
    animateSpin(winIndex, totalBet, userPlacedBet, remainingSpinSec);
}

// ANIMATE SPIN EFFECT AROUND PERIMETER TILES
function animateSpin(targetIndex, totalBet, userPlacedBet, remainingSpinSec) {
    const seq = CONFIG.perimeterSequence; // [0, 1, 2, 5, 8, 7, 6, 3]
    const winnerSeqIdx = seq.indexOf(targetIndex);

    const decelDelays = [100, 130, 170, 220, 290, 380, 500, 700];
    const decelSteps = decelDelays.length;

    // Scale animation to complete cleanly before cyclePos hits 40
    const targetDurationMs = Math.max(1500, Math.min(7500, (remainingSpinSec - 2) * 1000));
    const fastDuration = Math.max(500, targetDurationMs - 2490);
    const fastDelay = Math.max(35, Math.floor(fastDuration / 60));

    let reqRemainder = (winnerSeqIdx + 1 - decelSteps) % seq.length;
    if (reqRemainder < 0) reqRemainder += seq.length;

    let fastSteps = Math.floor(fastDuration / fastDelay);
    fastSteps = fastSteps - (fastSteps % seq.length) + reqRemainder;
    if (fastSteps < seq.length) fastSteps += seq.length;

    let totalSteps = fastSteps + decelSteps;
    let currentStep = 0;

    document.querySelectorAll('.tile').forEach(t => t.classList.remove('highlighted'));

    function spinStep() {
        const tileIndex = seq[currentStep % seq.length];

        playSound('spinTick');

        document.querySelectorAll('.tile').forEach(t => t.classList.remove('highlighted'));
        const currentTile = document.querySelector(`.tile[data-index="${tileIndex}"]`);
        if (currentTile) {
            currentTile.classList.add('highlighted');
        }

        animateLEDs(currentStep);
        currentStep++;

        if (currentStep < totalSteps) {
            let nextDelay = fastDelay;
            if (currentStep >= fastSteps) {
                let decelIndex = currentStep - fastSteps;
                nextDelay = decelDelays[decelIndex] || 500;
            }
            setTimeout(spinStep, nextDelay);
        } else {
            finishSpin(targetIndex, totalBet, userPlacedBet);
        }
    }

    spinStep();
}

// HELPER TO GENERATE FRUIT IMAGE HTML
function getFruitIconHtml(fruit, customClass = 'fruit-img-icon') {
    if (fruit && fruit.iconUrl) {
        return `<img class="${customClass}" src="${fruit.iconUrl}" alt="${fruit.name}">`;
    }
    return fruit ? fruit.emoji || '' : '';
}

// LED BULB FLASH EFFECT
function animateLEDs(step) {
    const leds = document.querySelectorAll('.led-bulb');
    leds.forEach((led, idx) => {
        if ((idx + step) % 2 === 0) {
            led.classList.add('active');
        } else {
            led.classList.remove('active');
        }
    });
}

// FINISH SPIN & HANDLE WIN/LOSS
function finishSpin(winIndex, totalBet, userPlacedBet) {
    state.isSpinning = false;
    state.currentRound++;

    const winningFruit = CONFIG.fruits[winIndex];
    state.showResultInCenter = true;
    if (DOM.centerDisplay) {
        DOM.centerDisplay.innerHTML = getFruitIconHtml(winningFruit, 'center-win-img');
    }

    setTimeout(() => {
        state.showResultInCenter = false;
        document.querySelectorAll('.tile').forEach(t => t.classList.remove('highlighted'));
        updateTimerUI();
    }, 2500);

    state.jackpot += Math.floor(Math.random() * 500) + 100;

    state.recentResults.unshift(winIndex);
    if (state.recentResults.length > 8) {
        state.recentResults.pop();
    }
    renderRecentResults();

    if (userPlacedBet) {
        const betOnWinTile = state.tileBets[winIndex] || 0;
        const winMultiplier = winningFruit.multiplier;
        const payout = betOnWinTile * winMultiplier;

        if (payout > 0) {
            playSound('win');
            if (window.AndroidLuckyFruit) {
                const targetRound = spinTriggeredRound || (state.currentRound - 1);
                const callbackName = '_cb_win_' + state.currentRound + '_' + Date.now();
                window[callbackName] = function(success, newBalance, errorMsg) {
                    delete window[callbackName];
                    if (success && typeof newBalance === 'number' && !isNaN(newBalance)) {
                        state.balance = newBalance;
                    } else {
                        state.balance += payout;
                    }
                    state.todaysWinnings += payout;
                    clearAllBets();
                    updateUI();
                };
                window.AndroidLuckyFruit.addWinPayout(payout, targetRound, callbackName);
            } else {
                state.balance += payout;
                state.todaysWinnings += payout;
                clearAllBets();
                updateUI();
            }
        } else {
            showToast(`Result: ${winningFruit.name}. Better luck next time!`);
            clearAllBets();
            updateUI();
        }
    } else {
        clearAllBets();
        updateUI();
    }

    clearAllBets();
    updateUI();

    // Auto-open Round Leaderboard for current round
    setTimeout(() => {
        openLeaderboard();
    }, 1200);
}

// SHOW WIN MODAL (Disabled: Leaderboard auto-opens instead)
function showWinModal(fruit, multiplier, amount) {
    playSound('win');
}

// SHOW TOAST NOTIFICATION
function showToast(msg) {
    if (!DOM.toastMessage || !DOM.toastNotification) return;
    DOM.toastMessage.textContent = msg;
    DOM.toastNotification.classList.remove('hidden');
    setTimeout(() => {
        DOM.toastNotification.classList.add('hidden');
    }, 2800);
}

// UPDATE RECENT RESULTS STRIP AT BOTTOM
function renderRecentResults() {
    if (!DOM.resultsStrip) return;
    DOM.resultsStrip.innerHTML = '';
    state.recentResults.forEach((fruitIndex, idx) => {
        const fruit = CONFIG.fruits[fruitIndex] || CONFIG.fruits[0];
        const item = document.createElement('div');
        item.className = 'result-item';
        item.innerHTML = getFruitIconHtml(fruit, 'result-img-icon');
        if (idx === 0) {
            const tag = document.createElement('span');
            tag.className = 'new-tag';
            tag.textContent = 'new';
            item.appendChild(tag);
        }
        DOM.resultsStrip.appendChild(item);
    });
}

// UPDATE ALL UI COUNTERS
function updateUI() {
    if (DOM.balanceDisplay) DOM.balanceDisplay.textContent = state.balance.toLocaleString();
    if (DOM.winningsDisplay) DOM.winningsDisplay.textContent = state.todaysWinnings.toLocaleString();
    if (DOM.jackpotValue) DOM.jackpotValue.textContent = state.jackpot.toLocaleString();
    if (DOM.roundCounter) DOM.roundCounter.textContent = `Round ${state.currentRound} of today`;
    updateBetBadges();
}

// CANVAS GOLD & PURPLE PARTICLES BACKGROUND
function initParticleCanvas() {
    const canvas = document.getElementById('particleCanvas');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    let width = canvas.width = window.innerWidth;
    let height = canvas.height = window.innerHeight;

    window.addEventListener('resize', () => {
        width = canvas.width = window.innerWidth;
        height = canvas.height = window.innerHeight;
    });

    const particles = [];
    const colors = ['#ffd700', '#ba55d3', '#e040fb', '#ffffff', '#ffea70'];
    for (let i = 0; i < 45; i++) {
        particles.push({
            x: Math.random() * width,
            y: Math.random() * height,
            radius: Math.random() * 2.2 + 0.8,
            color: colors[Math.floor(Math.random() * colors.length)],
            alpha: Math.random() * 0.7 + 0.3,
            speedY: Math.random() * 0.5 + 0.2,
            speedX: (Math.random() - 0.5) * 0.3
        });
    }

    function render() {
        ctx.clearRect(0, 0, width, height);
        particles.forEach(p => {
            ctx.beginPath();
            ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
            ctx.fillStyle = p.color;
            ctx.globalAlpha = p.alpha;
            ctx.shadowBlur = 8;
            ctx.shadowColor = p.color;
            ctx.fill();

            p.y -= p.speedY;
            p.x += p.speedX;

            if (p.y < 0) {
                p.y = height;
                p.x = Math.random() * width;
            }
        });
        ctx.globalAlpha = 1.0;
        requestAnimationFrame(render);
    }
    render();
}
