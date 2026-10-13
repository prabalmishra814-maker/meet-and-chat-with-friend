package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomchatapps.Pmishra.adapters.FamilyRankAdapter;
import com.roomchatapps.Pmishra.models.FamilyRankModel;
import com.roomchatapps.Pmishra.utils.StatusBarUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class FamilyRankingActivity extends AppCompatActivity {

    private TextView tabMyFamily, tabFamilySquare;
    private TextView btnFilterDaily, btnFilterWeekly, btnFilterSeason;
    private TextView tvSelectedDate, tvBottomFamilyStatus;
    private ImageView btnPrevDate, btnNextDate;
    private RecyclerView rvFamilyLeaderboard;

    private FamilyRankAdapter adapter;
    private final List<FamilyRankModel> familyList = new ArrayList<>();

    private Calendar selectedCalendar = Calendar.getInstance();
    private String activePeriod = "Daily"; // "Daily", "Weekly", "Season"
    private boolean isFamilySquare = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.makeTransparent(this);
        setContentView(R.layout.activity_family_ranking);

        setupWindowInsets();
        initViews();
        setupListeners();
        updateDateDisplay();
        loadFamilyData();
    }

    private void setupWindowInsets() {
        View topBar = findViewById(R.id.topBar);
        if (topBar != null) {
            ViewCompat.setOnApplyWindowInsetsListener(topBar, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(
                        v.getPaddingLeft(),
                        systemBars.top,
                        v.getPaddingRight(),
                        v.getPaddingBottom()
                );
                return insets;
            });
        }

        View bottomStatusContainer = findViewById(R.id.bottomStatusContainer);
        if (bottomStatusContainer != null) {
            ViewCompat.setOnApplyWindowInsetsListener(bottomStatusContainer, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(
                        v.getPaddingLeft(),
                        v.getPaddingTop(),
                        v.getPaddingRight(),
                        systemBars.bottom
                );
                return insets;
            });
        }
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView btnHelp = findViewById(R.id.btnHelp);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (btnHelp != null) btnHelp.setOnClickListener(v -> showFamilyHelpDialog());

        tabMyFamily = findViewById(R.id.tabMyFamily);
        tabFamilySquare = findViewById(R.id.tabFamilySquare);

        btnFilterDaily = findViewById(R.id.btnFilterDaily);
        btnFilterWeekly = findViewById(R.id.btnFilterWeekly);
        btnFilterSeason = findViewById(R.id.btnFilterSeason);

        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        btnPrevDate = findViewById(R.id.btnPrevDate);
        btnNextDate = findViewById(R.id.btnNextDate);

        tvBottomFamilyStatus = findViewById(R.id.tvBottomFamilyStatus);

        rvFamilyLeaderboard = findViewById(R.id.rvFamilyLeaderboard);
        if (rvFamilyLeaderboard != null) {
            rvFamilyLeaderboard.setLayoutManager(new LinearLayoutManager(this));
            adapter = new FamilyRankAdapter(this, familyList);
            rvFamilyLeaderboard.setAdapter(adapter);

            adapter.setOnFamilyClickListener((family, position) -> {
                Toast.makeText(FamilyRankingActivity.this, "Family: " + family.getFamilyName(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setupListeners() {
        // Top Tabs
        if (tabMyFamily != null) {
            tabMyFamily.setOnClickListener(v -> {
                isFamilySquare = false;
                tabMyFamily.setTextColor(Color.WHITE);
                tabMyFamily.setTypeface(null, Typeface.BOLD);
                tabFamilySquare.setTextColor(Color.parseColor("#A0FFFFFF"));
                tabFamilySquare.setTypeface(null, Typeface.NORMAL);
                checkUserFamilyAndShowPromptIfNeeded();
            });
        }

        if (tabFamilySquare != null) {
            tabFamilySquare.setOnClickListener(v -> {
                isFamilySquare = true;
                tabFamilySquare.setTextColor(Color.WHITE);
                tabFamilySquare.setTypeface(null, Typeface.BOLD);
                tabMyFamily.setTextColor(Color.parseColor("#A0FFFFFF"));
                tabMyFamily.setTypeface(null, Typeface.NORMAL);
                loadFamilyData();
            });
        }

        // Filter Buttons (Daily | Weekly | Season)
        if (btnFilterDaily != null) {
            btnFilterDaily.setOnClickListener(v -> selectPeriod("Daily"));
        }
        if (btnFilterWeekly != null) {
            btnFilterWeekly.setOnClickListener(v -> selectPeriod("Weekly"));
        }
        if (btnFilterSeason != null) {
            btnFilterSeason.setOnClickListener(v -> selectPeriod("Season"));
        }

        // Date Navigation
        if (btnPrevDate != null) {
            btnPrevDate.setOnClickListener(v -> {
                selectedCalendar.add(Calendar.DAY_OF_MONTH, -1);
                updateDateDisplay();
                loadFamilyData();
            });
        }

        if (btnNextDate != null) {
            btnNextDate.setOnClickListener(v -> {
                selectedCalendar.add(Calendar.DAY_OF_MONTH, 1);
                updateDateDisplay();
                loadFamilyData();
            });
        }
    }

    private void selectPeriod(String period) {
        activePeriod = period;

        int activeColor = Color.parseColor("#3D2600");
        int inactiveColor = Color.parseColor("#1A232A");

        if (btnFilterDaily != null) {
            boolean active = "Daily".equals(period);
            btnFilterDaily.setBackgroundResource(active ? R.drawable.bg_family_filter_active : R.drawable.bg_family_filter_inactive);
            btnFilterDaily.setTextColor(active ? activeColor : inactiveColor);
        }

        if (btnFilterWeekly != null) {
            boolean active = "Weekly".equals(period);
            btnFilterWeekly.setBackgroundResource(active ? R.drawable.bg_family_filter_active : R.drawable.bg_family_filter_inactive);
            btnFilterWeekly.setTextColor(active ? activeColor : inactiveColor);
        }

        if (btnFilterSeason != null) {
            boolean active = "Season".equals(period);
            btnFilterSeason.setBackgroundResource(active ? R.drawable.bg_family_filter_active : R.drawable.bg_family_filter_inactive);
            btnFilterSeason.setTextColor(active ? activeColor : inactiveColor);
        }

        loadFamilyData();
    }

    private void updateDateDisplay() {
        if (tvSelectedDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            tvSelectedDate.setText(sdf.format(selectedCalendar.getTime()));
        }
    }

    private void loadFamilyData() {
        String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCalendar.getTime());
        String periodKey = activePeriod != null ? activePeriod.toLowerCase(Locale.ROOT) : "daily";

        DatabaseReference rankRef = FirebaseDatabase.getInstance().getReference("family_rankings")
                .child(periodKey).child(dateStr);

        rankRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                familyList.clear();
                if (snapshot.exists() && snapshot.hasChildren()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        FamilyRankModel model = child.getValue(FamilyRankModel.class);
                        if (model != null) {
                            if (model.getFamilyId() == null) {
                                model.setFamilyId(child.getKey());
                            }
                            familyList.add(model);
                        }
                    }
                    processAndDisplayRankings();
                } else {
                    loadFromFamiliesNode();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadFromFamiliesNode();
            }
        });
    }

    private void loadFromFamiliesNode() {
        DatabaseReference familiesRef = FirebaseDatabase.getInstance().getReference("families");
        familiesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                familyList.clear();
                if (snapshot.exists() && snapshot.hasChildren()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String fId = child.getKey();
                        String name = child.child("familyName").getValue(String.class);
                        if (name == null) name = child.child("name").getValue(String.class);

                        String logo = child.child("familyLogo").getValue(String.class);
                        if (logo == null) logo = child.child("logo").getValue(String.class);

                        Long scoreVal = child.child("score").getValue(Long.class);
                        if (scoreVal == null) scoreVal = child.child("crownPoints").getValue(Long.class);
                        if (scoreVal == null) scoreVal = child.child("points").getValue(Long.class);
                        long score = scoreVal != null ? scoreVal : 0;

                        List<String> avatars = new ArrayList<>();
                        DataSnapshot membersSnap = child.child("topMemberAvatars");
                        if (!membersSnap.exists()) membersSnap = child.child("members");
                        if (membersSnap.exists()) {
                            for (DataSnapshot m : membersSnap.getChildren()) {
                                String av = m.getValue(String.class);
                                if (av != null && !av.isEmpty()) {
                                    avatars.add(av);
                                }
                            }
                        }

                        if (name != null) {
                            familyList.add(new FamilyRankModel(fId, name, logo, 0, score, avatars));
                        }
                    }
                }

                if (familyList.isEmpty()) {
                    populateDefaultFamilyRankings();
                }
                processAndDisplayRankings();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                populateDefaultFamilyRankings();
                processAndDisplayRankings();
            }
        });
    }

    private void processAndDisplayRankings() {
        Collections.sort(familyList, (a, b) -> Long.compare(b.getScore(), a.getScore()));
        for (int i = 0; i < familyList.size(); i++) {
            familyList.get(i).setRank(i + 1);
        }

        List<FamilyRankModel> displayList = new ArrayList<>(familyList);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUid = currentUser != null ? currentUser.getUid() : null;

        if (currentUid != null) {
            checkUserFamilyStatus(currentUid, displayList);
        } else {
            updateBottomStatusText("Not joining the family");
            updateAdapter(displayList);
        }
    }

    private void checkUserFamilyStatus(String currentUid, List<FamilyRankModel> displayList) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String myFamilyId = snapshot.child("familyId").getValue(String.class);
                String myFamilyName = snapshot.child("familyName").getValue(String.class);

                if (myFamilyName != null && !myFamilyName.isEmpty()) {
                    updateBottomStatusText("Joined Family: " + myFamilyName);
                } else {
                    updateBottomStatusText("Not joining the family");
                }

                if (!isFamilySquare && myFamilyId != null && !myFamilyId.isEmpty()) {
                    List<FamilyRankModel> filtered = new ArrayList<>();
                    for (FamilyRankModel f : displayList) {
                        if (myFamilyId.equals(f.getFamilyId())) {
                            filtered.add(f);
                        }
                    }
                    updateAdapter(filtered);
                } else {
                    updateAdapter(displayList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                updateBottomStatusText("Not joining the family");
                updateAdapter(displayList);
            }
        });
    }

    private void updateBottomStatusText(String text) {
        if (tvBottomFamilyStatus != null) {
            tvBottomFamilyStatus.setText(text);
        }
    }

    private void updateAdapter(List<FamilyRankModel> list) {
        if (adapter != null) {
            adapter.updateList(list);
        }
    }

    private void populateDefaultFamilyRankings() {
        familyList.clear();

        List<String> m1 = new ArrayList<>();
        m1.add("https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=200");
        m1.add("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=200");
        m1.add("https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=200");

        List<String> m2 = new ArrayList<>();
        m2.add("https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=200");
        m2.add("https://images.unsplash.com/photo-1438761681033-6461ffad8d80?q=80&w=200");
        m2.add("https://images.unsplash.com/photo-1524504388940-b1c1722653e1?q=80&w=200");

        List<String> m3 = new ArrayList<>();
        m3.add("https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?q=80&w=200");
        m3.add("https://images.unsplash.com/photo-1517841905240-472988babdf9?q=80&w=200");

        familyList.add(new FamilyRankModel("f1", "HELL RULER", "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=200", 1, 1574, m1));
        familyList.add(new FamilyRankModel("f2", "DIL ME HO TUM", "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?q=80&w=200", 2, 799, m2));
        familyList.add(new FamilyRankModel("f3", "SUKOON FAMILY", "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=200", 3, 474, m3));
        familyList.add(new FamilyRankModel("f4", "EMPIRE OF DEATH", "https://images.unsplash.com/photo-1563089145-599997674d42?q=80&w=200", 4, 396, m1));
        familyList.add(new FamilyRankModel("f5", "🇨🇦 KOLKATA 420 🇨🇦", "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?q=80&w=200", 5, 394, m2));
        familyList.add(new FamilyRankModel("f6", "THE DANGER", "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?q=80&w=200", 6, 367, m3));
    }

    private void showFamilyHelpDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Family Ranking Rules")
                .setMessage("1. Members send gifts in rooms to earn Crown Points 👑 for their Family.\n\n" +
                        "2. Family Rankings update daily, weekly, and seasonally.\n\n" +
                        "3. Top Families earn exclusive Family Frames, Badges, and Room Privileges!")
                .setPositiveButton("Got It", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void checkUserFamilyAndShowPromptIfNeeded() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            showCreateFamilyPromptDialog();
            return;
        }

        String uid = currentUser.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String familyId = snapshot.child("familyId").getValue(String.class);
                if (familyId == null || familyId.isEmpty()) {
                    showCreateFamilyPromptDialog();
                } else {
                    loadFamilyData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showCreateFamilyPromptDialog();
            }
        });
    }

    private void showCreateFamilyPromptDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_family_prompt, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ImageView btnDialogClose = dialogView.findViewById(R.id.btnDialogClose);
        TextView btnDialogCreate = dialogView.findViewById(R.id.btnDialogCreate);

        if (btnDialogClose != null) {
            btnDialogClose.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnDialogCreate != null) {
            btnDialogCreate.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(FamilyRankingActivity.this, CreateFamilyActivity.class));
            });
        }

        dialog.show();
    }
}
