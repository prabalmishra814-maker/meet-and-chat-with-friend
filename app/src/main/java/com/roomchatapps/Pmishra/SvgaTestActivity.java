package com.roomchatapps.Pmishra;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import org.jetbrains.annotations.NotNull;

public class SvgaTestActivity extends AppCompatActivity {

    private SVGAImageView svgaPlayer;
    private SVGAParser svgaParser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_svga_test);

        svgaPlayer = findViewById(R.id.svgaPlayer);
        svgaParser = new SVGAParser(this);

        findViewById(R.id.btnPlaySvga).setOnClickListener(v -> playSvgaAnimation("gift/aladdin.svga"));
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
