package com.roomchatapps.Pmishra;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private RecyclerView rvRooms;
    private ProgressBar progressBar;
    private TextView tvNoRooms;
    private RoomAdapter roomAdapter;
    private List<RoomModel> roomList;
    private DatabaseReference databaseReference;

    private View tabPopular, tabFollowing;
    private TextView tvPopular, tvFollowing;
    private View indicatorPopular, indicatorFollowing;

    private TextView[] categoryChips;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvRooms = view.findViewById(R.id.rvRooms);
        progressBar = view.findViewById(R.id.progressBar);
        tvNoRooms = view.findViewById(R.id.tvNoRooms);

        // Entrance animations
        AnimationHelper.fadeIn(view.findViewById(R.id.header), 500);
        AnimationHelper.scaleIn(view.findViewById(R.id.cvBanner), 700);

        setupImageSlider(view);
        setupRecyclerView();
        loadRoomsFromFirebase();
        setupClickListeners(view);
    }

    private void setupImageSlider(View view) {
        try {
            ImageSlider imageSlider = view.findViewById(R.id.image_slider);
            if (imageSlider != null) {
                List<SlideModel> slideModels = new ArrayList<>();
                slideModels.add(new SlideModel(R.drawable.file_000000001d048211a9aabf5cb34e92dd, ScaleTypes.FIT));
                slideModels.add(new SlideModel(R.drawable.file_000000007c788207aa23306b0aa06455, ScaleTypes.FIT));
                imageSlider.setImageList(slideModels, ScaleTypes.FIT);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in setupImageSlider", e);
        }
    }

    private void setupRecyclerView() {
        try {
            if (getContext() == null) return;
            roomList = new ArrayList<>();
            roomAdapter = new RoomAdapter(getContext(), roomList);
            if (rvRooms != null) {
                rvRooms.setLayoutManager(new GridLayoutManager(getContext(), 2));
                rvRooms.setAdapter(roomAdapter);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in setupRecyclerView", e);
        }
    }

    private void loadRoomsFromFirebase() {
        try {
            databaseReference = FirebaseDatabase.getInstance().getReference("rooms");
            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!isAdded() || getContext() == null) return;

                    roomList.clear();
                    if (snapshot.exists()) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            RoomModel room = ds.getValue(RoomModel.class);
                            if (room != null) roomList.add(room);
                        }
                    }
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (tvNoRooms != null) {
                        tvNoRooms.setVisibility(roomList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                    if (roomAdapter != null) roomAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (!isAdded() || getContext() == null) return;
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Firebase Error: " + error.getMessage());
                    if (getContext() != null) {
                        android.widget.Toast.makeText(getContext(), "Database Error: " + error.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in loadRoomsFromFirebase", e);
        }
    }

    private void setupClickListeners(View view) {
        try {
            View ivTrophy = view.findViewById(R.id.ivTrophy);
            if (ivTrophy != null) {
                ivTrophy.setOnClickListener(v -> {
                    if (getActivity() != null) {
                        startActivity(new Intent(getActivity(), LeaderboardActivity.class));
                    }
                });
            }

            View ivAdd = view.findViewById(R.id.ivAdd);
            if (ivAdd != null) {
                ivAdd.setOnClickListener(v -> {
                    if (getActivity() != null) {
                        startActivity(new Intent(getActivity(), CreateRoomActivity.class));
                    }
                });
            }

            View ivSearch = view.findViewById(R.id.ivSearch);
            if (ivSearch != null) {
                ivSearch.setOnClickListener(v -> {
                    if (getActivity() != null) {
                        startActivity(new Intent(getActivity(), SearchActivity.class));
                    }
                });
            }

            View flNotification = view.findViewById(R.id.flNotification);
            if (flNotification != null) {
                flNotification.setOnClickListener(v -> {
                    if (getActivity() != null) {
                        startActivity(new Intent(getActivity(), NotificationActivity.class));
                    }
                });
            }

            setupTabNavigation(view);
            setupCategoryChips(view);

        } catch (Exception e) {
            Log.e(TAG, "Error in setupClickListeners", e);
        }
    }

    private void setupTabNavigation(View view) {
        tabPopular = view.findViewById(R.id.tabPopular);
        tabFollowing = view.findViewById(R.id.tabFollowing);

        tvPopular = view.findViewById(R.id.tvPopular);
        tvFollowing = view.findViewById(R.id.tvFollowing);

        indicatorPopular = view.findViewById(R.id.indicatorPopular);
        indicatorFollowing = view.findViewById(R.id.indicatorFollowing);

        if (tabPopular != null) {
            tabPopular.setOnClickListener(v -> selectTab(0));
        }
        if (tabFollowing != null) {
            tabFollowing.setOnClickListener(v -> selectTab(1));
        }
    }

    private void selectTab(int index) {
        if (tvPopular != null) {
            tvPopular.setTextColor(index == 0 ? 0xFFFFFFFF : 0x80FFFFFF);
            tvPopular.setTextSize(index == 0 ? 20 : 16);
            if (indicatorPopular != null) indicatorPopular.setVisibility(index == 0 ? View.VISIBLE : View.INVISIBLE);
        }
        if (tvFollowing != null) {
            tvFollowing.setTextColor(index == 1 ? 0xFFFFFFFF : 0x80FFFFFF);
            tvFollowing.setTextSize(index == 1 ? 20 : 16);
            if (indicatorFollowing != null) indicatorFollowing.setVisibility(index == 1 ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private void setupCategoryChips(View view) {
        TextView chipHot = view.findViewById(R.id.chipHot);
        TextView chipIndia = view.findViewById(R.id.chipIndia);
        TextView chipSaudi = view.findViewById(R.id.chipSaudi);
        TextView chipNepal = view.findViewById(R.id.chipNepal);
        TextView chipKuwait = view.findViewById(R.id.chipKuwait);

        categoryChips = new TextView[]{chipHot, chipIndia, chipSaudi, chipNepal, chipKuwait};

        for (int i = 0; i < categoryChips.length; i++) {
            int index = i;
            if (categoryChips[i] != null) {
                categoryChips[i].setOnClickListener(v -> selectChip(index));
            }
        }
    }

    private void selectChip(int selectedIndex) {
        if (categoryChips == null) return;
        for (int i = 0; i < categoryChips.length; i++) {
            if (categoryChips[i] != null) {
                if (i == selectedIndex) {
                    categoryChips[i].setBackgroundResource(R.drawable.bg_wallet_chip_selected);
                    categoryChips[i].setTextColor(0xFFFFFFFF);
                } else {
                    categoryChips[i].setBackgroundResource(R.drawable.bg_wallet_chip_unselected);
                    categoryChips[i].setTextColor(0xD0FFFFFF);
                }
            }
        }
    }

}
