package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.roomchatapps.Pmishra.adapters.RingAdapter;
import com.roomchatapps.Pmishra.models.RingModel;
import com.roomchatapps.Pmishra.utils.StatusBarUtils;

import java.util.ArrayList;
import java.util.List;

public class RingSelectActivity extends AppCompatActivity {

    private View mainLayout;
    private View headerBar;
    private ImageView btnBack;
    private RecyclerView rvRings;
    private TextView btnConfirmRing;

    private RingAdapter adapter;
    private final List<RingModel> ringList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.makeTransparent(this);
        setContentView(R.layout.activity_ring_select);

        initViews();
        setupWindowInsets();
        setupToolbar();
        buildRingData();
        setupRecyclerView();

        btnConfirmRing.setOnClickListener(v -> confirmSelection());
    }

    private void initViews() {
        mainLayout = findViewById(R.id.mainLayout);
        headerBar = findViewById(R.id.headerBar);
        btnBack = findViewById(R.id.btnBack);
        rvRings = findViewById(R.id.rvRings);
        btnConfirmRing = findViewById(R.id.btnConfirmRing);
    }

    private void setupWindowInsets() {
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                headerBar.setPadding(
                        headerBar.getPaddingLeft(),
                        systemBars.top,
                        headerBar.getPaddingRight(),
                        headerBar.getPaddingBottom()
                );
                v.setPadding(
                        v.getPaddingLeft(),
                        0,
                        v.getPaddingRight(),
                        systemBars.bottom
                );
                return insets;
            });
        }
    }

    private void setupToolbar() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void buildRingData() {
        ringList.clear();
        ringList.add(new RingModel("1", "Blue Winged Ring", R.drawable.ic_ring_blue_winged, "gift/blue_ring_love.svga", 4999999L, true));
        ringList.add(new RingModel("2", "Pink Crown Ring", R.drawable.ic_ring_pink_winged, "gift/diamond_ring_gift.svga", 4999999L, false));
        ringList.add(new RingModel("3", "Gold Celestial Ring", R.drawable.ic_ring_gold_spire, "gift/golden_rings.svga", 20000000L, false));
    }

    private void setupRecyclerView() {
        adapter = new RingAdapter(ringList, new RingAdapter.OnRingClickListener() {
            @Override
            public void onRingClick(RingModel ring, int position) {
                // Ring selected
            }

            @Override
            public void onPlayPreviewClick(RingModel ring, int position) {
                Toast.makeText(RingSelectActivity.this, "Previewing " + ring.getName() + " animation", Toast.LENGTH_SHORT).show();
            }
        });

        rvRings.setLayoutManager(new GridLayoutManager(this, 2));
        rvRings.setAdapter(adapter);
    }

    private void confirmSelection() {
        RingModel selectedRing = adapter.getSelectedRing();
        if (selectedRing == null) {
            Toast.makeText(this, "Please select an engagement ring", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("ringId", selectedRing.getId());
        resultIntent.putExtra("ringName", selectedRing.getName());
        resultIntent.putExtra("ringIconRes", selectedRing.getIconRes());
        resultIntent.putExtra("ringPrice", selectedRing.getPriceCoins());
        setResult(RESULT_OK, resultIntent);

        Toast.makeText(this, "Selected " + selectedRing.getName(), Toast.LENGTH_SHORT).show();
        finish();
    }
}
