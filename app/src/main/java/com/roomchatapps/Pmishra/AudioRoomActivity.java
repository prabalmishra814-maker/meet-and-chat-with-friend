package com.roomchatapps.Pmishra;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.jetbrains.annotations.NotNull;

public class AudioRoomActivity extends AppCompatActivity {

    private FloatingActionButton giftFab;
    private RecyclerView rvSpeakers, rvChat;
    private SVGAImageView svgaPlayer;
    private SVGAParser svgaParser;
    private ImageView ivTitleDropdown;
    private VideoView videoBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        svgaParser = new SVGAParser(this);
        initViews();
        setupClickListeners();
        startVideoTheme(); // Auto-start background theme
    }

    private void initViews() {
        giftFab = findViewById(R.id.giftFab);
        rvSpeakers = findViewById(R.id.rvSpeakers);
        rvChat = findViewById(R.id.rvChat);
        svgaPlayer = findViewById(R.id.svgaPlayer);
        ivTitleDropdown = findViewById(R.id.ivTitleDropdown);
        videoBackground = findViewById(R.id.videoBackground);

        // Basic setup for RecyclerViews to avoid issues if they are empty
        if (rvSpeakers != null) {
            rvSpeakers.setLayoutManager(new GridLayoutManager(this, 4));
        }
        if (rvChat != null) {
            rvChat.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    private void setupClickListeners() {
        if (giftFab != null) {
            giftFab.setOnClickListener(v -> showGiftDialog());
        }
        if (ivTitleDropdown != null) {
            ivTitleDropdown.setOnClickListener(v -> {
                showRoomDetailsDialog();
            });
        }
    }

    private void showRoomDetailsDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_room_details, null);
        dialog.setContentView(dialogView);

        View btnChangeTheme = dialogView.findViewById(R.id.btnChangeTheme);
        if (btnChangeTheme != null) {
            btnChangeTheme.setOnClickListener(v -> {
                startVideoTheme();
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void startVideoTheme() {
        if (videoBackground == null) return;

        try {
            videoBackground.setVisibility(View.VISIBLE);
            
            videoBackground.getHolder().addCallback(new android.view.SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(android.view.SurfaceHolder holder) {
                    try {
                        MediaPlayer mMediaPlayer = new MediaPlayer();
                        mMediaPlayer.setDisplay(holder);
                        AssetFileDescriptor afd = getAssets().openFd("theme/theme1.mp4");
                        mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                        afd.close();
                        mMediaPlayer.prepare();
                        mMediaPlayer.setLooping(true);
                        mMediaPlayer.setVolume(0, 0); // Background theme is usually silent
                        mMediaPlayer.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                @Override public void surfaceChanged(android.view.SurfaceHolder holder, int format, int width, int height) {}
                @Override public void surfaceDestroyed(android.view.SurfaceHolder holder) {}
            });
            
            // Force recreation to trigger surfaceCreated if already shown
            videoBackground.setVisibility(View.GONE);
            videoBackground.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            Toast.makeText(this, "Error playing video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void showGiftDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_svga_play, null);
        dialog.setContentView(dialogView);

        final String[] selectedSvga = {null};

        View aladdinCard = dialogView.findViewById(R.id.cardAladdin);
        ShapeableImageView ivAladdin = dialogView.findViewById(R.id.ivAladdin);
        if (aladdinCard != null && ivAladdin != null) {
            ivAladdin.setOnClickListener(v -> {
                selectedSvga[0] = "gift/aladdin.svga";
                // Apply Flute-style selection to the container
                aladdinCard.setBackgroundResource(R.drawable.bg_selected_item);
                // Remove the previous blue border from image to match the reference
                ivAladdin.setStrokeWidth(0f); 
            });
        }

        View sendBtn = dialogView.findViewById(R.id.btnSendAnimation);
        if (sendBtn != null) {
            sendBtn.setOnClickListener(v -> {
                if (selectedSvga[0] != null) {
                    playSvgaAnimation(selectedSvga[0]);
                    dialog.dismiss();
                }
            });
        }

        dialog.show();
    }

    private void playSvgaAnimation(String fileName) {
        if (svgaPlayer == null || svgaParser == null) return;

        svgaParser.decodeFromAssets(fileName, new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                svgaPlayer.setVisibility(View.VISIBLE);
                svgaPlayer.setVideoItem(videoItem);
                svgaPlayer.stepToFrame(0, true);

                svgaPlayer.setCallback(new SVGACallback() {
                    @Override
                    public void onPause() {}

                    @Override
                    public void onFinished() {
                        runOnUiThread(() -> svgaPlayer.setVisibility(View.GONE));
                    }

                    @Override
                    public void onStep(int frame, double percentage) {}

                    @Override
                    public void onRepeat() {}
                });
            }

            @Override
            public void onError() {
                // Failed to load animation
            }
        }, null);
    }
}
