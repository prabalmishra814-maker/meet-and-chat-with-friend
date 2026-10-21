package com.roomchatapps.Pmishra;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.utils.GameLeaderboardManager;
import com.roomchatapps.Pmishra.utils.WalletManager;

import java.util.Locale;

/**
 * JavaScript Interface for Lucky Fruit Game.
 * Bridges real Firebase coin balance (used for sending gifts) with Lucky Fruit betting, payouts, and round leaderboard.
 */
public class LuckyFruitBridge {
    private static final String TAG = "LuckyFruitBridge";
    private final WebView webView;
    private final String userId;
    private DatabaseReference userCoinsRef;
    private ValueEventListener coinsEventListener;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public LuckyFruitBridge(Context context, WebView webView, String userId) {
        this.webView = webView;
        this.userId = userId;
        listenToUserCoins();
    }

    private void listenToUserCoins() {
        if (userId == null || userId.isEmpty()) return;
        userCoinsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("coins");
        coinsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long coins = 0;
                if (snapshot.exists() && snapshot.getValue() != null) {
                    try {
                        coins = Long.parseLong(String.valueOf(snapshot.getValue()));
                    } catch (Exception ignored) {}
                }
                notifyJsBalanceChanged(coins);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error listening to user coins: " + error.getMessage());
            }
        };
        userCoinsRef.addValueEventListener(coinsEventListener);
    }

    public void cleanup() {
        if (userCoinsRef != null && coinsEventListener != null) {
            userCoinsRef.removeEventListener(coinsEventListener);
            coinsEventListener = null;
        }
    }

    private void notifyJsBalanceChanged(long newBalance) {
        mainHandler.post(() -> {
            if (webView != null) {
                webView.evaluateJavascript(
                    "if (window.updateLuckyFruitBalance) { window.updateLuckyFruitBalance(" + newBalance + "); }",
                    null
                );
            }
        });
    }

    @JavascriptInterface
    public void requestUserCoins() {
        if (userId == null || userId.isEmpty()) {
            notifyJsBalanceChanged(0);
            return;
        }
        WalletManager.getUserCoins(userId, this::notifyJsBalanceChanged);
    }

    @JavascriptInterface
    public void deductBetCoins(long betAmount, String callbackJsMethod) {
        deductBetCoins(betAmount, 0, callbackJsMethod);
    }

    @JavascriptInterface
    public void deductBetCoins(long betAmount, long roundIndex, String callbackJsMethod) {
        if (userId == null || userId.isEmpty()) {
            sendCallback(callbackJsMethod, false, 0, "Invalid user ID");
            return;
        }
        if (betAmount <= 0) {
            sendCallback(callbackJsMethod, false, 0, "Invalid bet amount");
            return;
        }

        WalletManager.spendCoinsForGift(userId, null, betAmount, "Lucky Fruit Bet", new WalletManager.WalletCallback() {
            @Override
            public void onSuccess(String message, long newCoinBalance) {
                if (roundIndex > 0) {
                    GameLeaderboardManager.recordBet("lucky_fruit", roundIndex, userId, betAmount);
                }
                sendCallback(callbackJsMethod, true, newCoinBalance, "Bet placed successfully");
            }

            @Override
            public void onError(String error) {
                sendCallback(callbackJsMethod, false, 0, error);
            }
        });
    }

    @JavascriptInterface
    public void addWinPayout(long winAmount, String callbackJsMethod) {
        addWinPayout(winAmount, 0, callbackJsMethod);
    }

    @JavascriptInterface
    public void addWinPayout(long winAmount, long roundIndex, String callbackJsMethod) {
        if (userId == null || userId.isEmpty() || winAmount <= 0) {
            sendCallback(callbackJsMethod, false, 0, "Invalid win amount");
            return;
        }

        WalletManager.addCoins(userId, winAmount, "GAME_WIN", "Lucky Fruit Win", "Won " + winAmount + " coins in Lucky Fruit", new WalletManager.WalletCallback() {
            @Override
            public void onSuccess(String message, long newCoinBalance) {
                if (roundIndex > 0) {
                    GameLeaderboardManager.recordWin("lucky_fruit", roundIndex, userId, winAmount);
                }
                sendCallback(callbackJsMethod, true, newCoinBalance, "Payout added successfully");
            }

            @Override
            public void onError(String error) {
                sendCallback(callbackJsMethod, false, 0, error);
            }
        });
    }

    @JavascriptInterface
    public void getRoundLeaderboard(long roundIndex, String callbackJsMethod) {
        if (callbackJsMethod == null || callbackJsMethod.isEmpty()) return;
        GameLeaderboardManager.fetchRoundLeaderboard("lucky_fruit", roundIndex, jsonResult -> {
            mainHandler.post(() -> {
                if (webView != null) {
                    String safeJson = jsonResult != null ? jsonResult.replace("'", "\\'") : "[]";
                    String js = String.format(Locale.US, "if (window['%s']) { window['%s']('%s'); }",
                            callbackJsMethod, callbackJsMethod, safeJson);
                    webView.evaluateJavascript(js, null);
                }
            });
        });
    }

    private void sendCallback(String method, boolean success, long newBalance, String message) {
        if (method == null || method.isEmpty()) return;
        mainHandler.post(() -> {
            if (webView != null) {
                String safeMsg = message != null ? message.replace("'", "\\'").replace("\n", " ") : "";
                String js = String.format(Locale.US, "if (window['%s']) { window['%s'](%b, %d, '%s'); }",
                        method, method, success, newBalance, safeMsg);
                webView.evaluateJavascript(js, null);
            }
        });
    }
}
