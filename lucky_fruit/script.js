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
    chipBtns: document.querySelectorAll('.chip-btn')
};

// INITIALIZATION
document.addEventListener('DOMContentLoaded', () => {
    initLEDs();
    initParticleCanvas();
    updateUI();
    renderRecentResults();
    attachEventListeners();
    startCountdownTimer();
});

let timerInterval = null;

// START CONTINUOUS 30-SECOND COUNTDOWN TIMER
function startCountdownTimer() {
    if (timerInterval) clearInterval(timerInterval);
    state.countdownSeconds = CONFIG.countdownDuration || 30;
    updateTimerUI();

    timerInterval = setInterval(() => {
        state.countdownSeconds--;

        if (state.countdownSeconds <= 0) {
            triggerSpin();
            state.countdownSeconds = CONFIG.countdownDuration || 30;
        }

        updateTimerUI();
    }, 1000);
}

// UPDATE COUNTDOWN TIMER DISPLAY
function updateTimerUI() {
    const text = `${state.countdownSeconds}s`;
    if (DOM.timerValue) {
        DOM.timerValue.textContent = text;
    }
    if (!state.isSpinning && !state.showResultInCenter && DOM.centerDisplay) {
        DOM.centerDisplay.textContent = text;
    }
}

// INITIALIZE LED BULBS AROUND CABINET
function initLEDs() {
    const totalLEDs = 20;
    DOM.ledContainer.innerHTML = '';
    for (let i = 0; i < totalLEDs; i++) {
        const led = document.createElement('div');
        led.className = 'led-bulb';
        // Position LEDs around border perimeter
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

// ATTACH EVENT LISTENERS
function attachEventListeners() {
    // Tile betting click
    const fruitTiles = document.querySelectorAll('.fruit-tile');
    fruitTiles.forEach(tile => {
        tile.addEventListener('click', () => {
            if (state.isSpinning) return;
            const index = parseInt(tile.getAttribute('data-index'));
            placeBetOnTile(index);
        });
    });

    // Quantity Chip Selection
    DOM.chipBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            const amount = parseInt(btn.getAttribute('data-amount'));
            setSelectedQuantity(amount);
        });
    });

    // Clear Bets
    DOM.clearBetsBtn.addEventListener('click', () => {
        if (state.isSpinning) return;
        for (let idx in state.tileBets) {
            state.tileBets[idx] = 0;
        }
        updateBetBadges();
    });

    // Test Mode Toggle
    DOM.testModeBtn.addEventListener('click', () => {
        state.testMode = !state.testMode;
        DOM.testModeBtn.textContent = `Test: ${state.testMode ? 'ON' : 'OFF'}`;
        DOM.testModeBtn.style.color = state.testMode ? '#00ff88' : '#f3d6ff';
        showToast(state.testMode ? "Test Mode: Wins forced to Lemon (🍋)" : "Standard Mode Active");
    });

    // Close Win Modal
    DOM.winCloseBtn.addEventListener('click', () => {
        DOM.winModal.classList.add('hidden');
    });
}

// SET SELECTED BET QUANTITY
function setSelectedQuantity(amount) {
    state.selectedQuantity = amount;

    // Update chip buttons active state
    DOM.chipBtns.forEach(btn => {
        if (parseInt(btn.getAttribute('data-amount')) === amount) {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    });
}

// PLACE BET ON A SPECIFIC TILE
function placeBetOnTile(index) {
    const totalCurrentBet = getTotalBet();
    if (state.balance < totalCurrentBet + state.selectedQuantity) {
        showToast("Not enough gold coins!");
        return;
    }
    state.tileBets[index] = (state.tileBets[index] || 0) + state.selectedQuantity;
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
        const betAmount = state.tileBets[idx] || 0;
        if (badge) {
            if (betAmount > 0) {
                badge.textContent = betAmount >= 1000 ? `${betAmount / 1000}K` : betAmount;
                badge.classList.remove('hidden');
            } else {
                badge.classList.add('hidden');
            }
        }
    }
}

// CLEAR ALL TILE BETS
function clearAllBets() {
    for (let idx in state.tileBets) {
        state.tileBets[idx] = 0;
    }
    updateBetBadges();
}

// TRIGGER SPIN / PREDICTION ROUND
function triggerSpin() {
    if (state.isSpinning) return;

    if (DOM.winModal) {
        DOM.winModal.classList.add('hidden');
    }

    let totalBet = getTotalBet();
    const userPlacedBet = (totalBet > 0);

    // If user placed bets, verify balance and deduct bet amount
    if (userPlacedBet) {
        if (state.balance < totalBet) {
            showToast("Not enough gold coins!");
            return;
        }
        state.balance -= totalBet;
    }

    state.isSpinning = true;
    updateUI();

    // Determine target winning tile
    let winIndex;
    if (state.testMode) {
        winIndex = state.testNextWinIndex; // Lemon 🍋
    } else {
        const randomPos = Math.floor(Math.random() * CONFIG.perimeterSequence.length);
        winIndex = CONFIG.perimeterSequence[randomPos];
    }

    // Start spin animation
    animateSpin(winIndex, totalBet, userPlacedBet);
}

// ANIMATE SPIN EFFECT AROUND PERIMETER TILES
function animateSpin(targetIndex, totalBet, userPlacedBet) {
    const seq = CONFIG.perimeterSequence;
    let currentStep = 0;
    const totalSteps = seq.length * 3 + seq.indexOf(targetIndex); // 3 full loops + target
    let intervalTime = 60; // ms per step

    // Remove previous highlights
    document.querySelectorAll('.tile').forEach(t => t.classList.remove('highlighted'));

    const interval = setInterval(() => {
        const tileIndex = seq[currentStep % seq.length];

        // Highlight active tile in sequence
        document.querySelectorAll('.tile').forEach(t => t.classList.remove('highlighted'));
        const currentTile = document.querySelector(`.tile[data-index="${tileIndex}"]`);
        if (currentTile) {
            currentTile.classList.add('highlighted');
        }

        // Animate LEDs and Center number
        animateLEDs(currentStep);
        DOM.centerDisplay.textContent = Math.floor(Math.random() * 90) + 10;

        currentStep++;

        if (currentStep > totalSteps) {
            clearInterval(interval);
            finishSpin(targetIndex, totalBet, userPlacedBet);
        }
    }, intervalTime);
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

    // Set center tile display to winning fruit icon
    const winningFruit = CONFIG.fruits[winIndex];
    state.showResultInCenter = true;
    DOM.centerDisplay.innerHTML = getFruitIconHtml(winningFruit, 'center-win-img');

    // Revert center tile back to countdown timer after 2.5 seconds
    setTimeout(() => {
        state.showResultInCenter = false;
        updateTimerUI();
    }, 2500);

    // Increment Jackpot slightly
    state.jackpot += Math.floor(Math.random() * 500) + 100;

    // Update Recent Results History
    state.recentResults.unshift(winIndex);
    if (state.recentResults.length > 8) {
        state.recentResults.pop();
    }
    renderRecentResults();

    // Process Outcome ONLY if user placed a bet in this round
    if (userPlacedBet) {
        const betOnWinTile = state.tileBets[winIndex] || 0;
        const winMultiplier = winningFruit.multiplier;
        const payout = betOnWinTile * winMultiplier;

        if (payout > 0) {
            state.balance += payout;
            state.todaysWinnings += payout;
            showWinModal(winningFruit, winMultiplier, payout);
        } else {
            showToast(`Result: ${winningFruit.name}. Better luck next time!`);
        }
    }

    // Reset tile bets for next round
    clearAllBets();

    updateUI();
}

// SHOW WIN MODAL
function showWinModal(fruit, multiplier, amount) {
    DOM.winFruitIcon.innerHTML = getFruitIconHtml(fruit, 'modal-win-img');
    DOM.winMultiplierText.textContent = `${multiplier}x Multiplier! (${fruit.name})`;
    DOM.winAmountText.textContent = amount.toLocaleString();
    DOM.winModal.classList.remove('hidden');
}

// SHOW TOAST NOTIFICATION
function showToast(msg) {
    DOM.toastMessage.textContent = msg;
    DOM.toastNotification.classList.remove('hidden');
    setTimeout(() => {
        DOM.toastNotification.classList.add('hidden');
    }, 2800);
}

// UPDATE RECENT RESULTS STRIP AT BOTTOM
function renderRecentResults() {
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
    DOM.balanceDisplay.textContent = state.balance.toLocaleString();
    DOM.winningsDisplay.textContent = state.todaysWinnings.toLocaleString();
    DOM.jackpotValue.textContent = state.jackpot.toLocaleString();
    DOM.roundCounter.textContent = `Round ${state.currentRound} of today`;
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
