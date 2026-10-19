package com.roomchatapps.Pmishra;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.auth.FirebaseUser;
import com.roomchatapps.Pmishra.adapters.CollectionAdapter;
import com.roomchatapps.Pmishra.adapters.EmojiAdapter;
import com.roomchatapps.Pmishra.adapters.GiftStoreAdapter;
import com.roomchatapps.Pmishra.dialogs.JoinAgencyDialog;
import com.roomchatapps.Pmishra.models.CollectionItemModel;
import com.roomchatapps.Pmishra.models.StoreItemModel;
import com.roomchatapps.Pmishra.spinwheel.SpinWheelController;
import com.roomchatapps.Pmishra.utils.CpBindingManager;
import com.roomchatapps.Pmishra.utils.CpIntimacyManager;
import com.roomchatapps.Pmishra.utils.GiftCatalog;
import com.roomchatapps.Pmishra.utils.GlobalGiftBannerManager;
import com.roomchatapps.Pmishra.utils.StoreManager;
import com.roomchatapps.Pmishra.spinwheel.SpinWheelView;
import com.roomchatapps.Pmishra.spinwheel.SpinWinnerDialog;
import com.roomchatapps.Pmishra.utils.CoinUtils;
import com.roomchatapps.Pmishra.utils.RoomFloatingManager;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.database.Cursor;
import android.content.ContentUris;
import android.provider.MediaStore;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileOutputStream;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import java.util.Collections;
import com.roomchatapps.Pmishra.adapters.RoomMusicAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.roomchatapps.Pmishra.adapters.AdminMemberAdapter;
import com.roomchatapps.Pmishra.adapters.AdminMemberAdapter.AdminMemberItem;
import com.roomchatapps.Pmishra.adapters.AdminMemberAdapter.OnRoleActionListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.roomchatapps.Pmishra.adapters.AdminMemberAdapter;
import com.roomchatapps.Pmishra.adapters.AudienceAdapter;
import com.roomchatapps.Pmishra.adapters.BlacklistAdapter;
import com.roomchatapps.Pmishra.adapters.ChatAdapter;
import com.roomchatapps.Pmishra.adapters.GiftRecipientAdapter;
import com.roomchatapps.Pmishra.adapters.GiftStoreAdapter;
import com.roomchatapps.Pmishra.adapters.GiftStoreAdapter.GiftStoreItem;
import com.roomchatapps.Pmishra.adapters.RoomContributionAdapter;
import com.roomchatapps.Pmishra.adapters.SeatAdapter;
import com.roomchatapps.Pmishra.models.ChatMessage;
import com.roomchatapps.Pmishra.models.FriendRequestModel;
import com.roomchatapps.Pmishra.models.GiftRecipientModel;
import com.roomchatapps.Pmishra.models.RoomContributionModel;
import com.roomchatapps.Pmishra.models.TransactionModel;
import com.roomchatapps.Pmishra.models.User;
import com.roomchatapps.Pmishra.utils.EconomyConfig;
import com.roomchatapps.Pmishra.utils.TwentyFourHourRuleManager;
import com.roomchatapps.Pmishra.utils.FrameUtils;
import com.roomchatapps.Pmishra.utils.LevelUtils;
import com.roomchatapps.Pmishra.utils.NotificationHelper;
import com.roomchatapps.Pmishra.utils.SessionManager;
import com.roomchatapps.Pmishra.utils.UserProfileCache;
import com.roomchatapps.Pmishra.utils.WalletManager;
import com.roomchatapps.Pmishra.zego.SeatManager;
import com.roomchatapps.Pmishra.zego.SeatModel;
import com.roomchatapps.Pmishra.zego.ZegoManager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Random;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;

import im.zego.zegoexpress.constants.ZegoRoomStateChangedReason;
import im.zego.zegoexpress.entity.ZegoBroadcastMessageInfo;
import im.zego.zegoexpress.entity.ZegoRoomExtraInfo;
import im.zego.zegoexpress.entity.ZegoUser;
import kotlin.Unit;

public class RoomChatActivity extends AppCompatActivity {

    private final long appID = 1179755079L;
    private final String appSign = "4244b91291217b7cb9b2b4bd61ec80d3d892d1327e8ac52e3ba66cad189aa319";

    private String roomID;
    private String userID;
    private String userName;
    private String roomNameLabel;
    private String roomImg;
    private boolean isHost;

    private NotificationAnimator notificationAnimator;
    private SVGAImageView svgaPlayer;
    private SVGAParser svgaParser;

    private TextView tvTrophyCount;
    private TextView tvMemberCount;

    private View bannerGiftContainer;
    private SVGAImageView svgaBannerPlayer;
    private TextView tvBannerNotice;

    private View bannerEntryContainer;
    private SVGAImageView svgaEntryBannerPlayer;
    private TextView tvEntryBannerNotice;

    private DatabaseReference roomGiftsRef;
    private ChildEventListener giftsChildEventListener;
    private DatabaseReference globalRoomGiftsRef;
    private ChildEventListener globalGiftsChildEventListener;

    private DatabaseReference roomInfoRef;
    private ValueEventListener roomInfoValueEventListener;

    private DatabaseReference roomSeatsRef;
    private ValueEventListener seatsValueEventListener;

    private DatabaseReference roomMessagesRef;
    private ChildEventListener roomMessagesChildEventListener;

    private DatabaseReference roomMusicRef;
    private ValueEventListener roomMusicValueEventListener;

    private DatabaseReference roomThemeRef;
    private ValueEventListener roomThemeValueEventListener;

    private DatabaseReference roomEntriesRef;
    private ChildEventListener roomEntriesChildEventListener;

    private DatabaseReference roomEmojiRef;
    private ChildEventListener roomEmojiEventListener;

    private DatabaseReference roomOnlineUsersRef;
    private DatabaseReference mySeatInviteRef;
    private ValueEventListener mySeatInviteListener;
    private Dialog currentInviteDialog;

    // ROOM ROLE PERSISTENCE
    private DatabaseReference roomRolesRef;
    private ValueEventListener roomRolesValueListener;
    private String currentRoomRole = "member";
    private final Map<String, String> userRoomRolesMap = new HashMap<>();

    // ROOM WELCOME MESSAGES & CLEAR SCREEN
    private String currentRoomWelcomeMessage = "";
    private String currentNewUserWelcomeMessage = "";
    private long lastClearedTimestamp = 0L;
    // SEAT SYSTEM FIX
    private long lastSeatLimitClickTime = 0L;

    // ADMIN PERMISSION FIX
    private boolean isAdminOrHost() {
        return isHost || "admin".equalsIgnoreCase(currentRoomRole) || "host".equalsIgnoreCase(currentRoomRole);
    }

    private final ActivityResultLauncher<Intent> audioPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri audioUri = result.getData().getData();
                    if (audioUri != null) {
                        handleSelectedAudioUri(audioUri);
                    }
                }
            }
    );

    public static class DeviceAudioTrack {
        public long id;
        public String title;
        public String artist;
        public long duration;
        public Uri uri;
        public String path;
        public Uri albumArtUri;

        public DeviceAudioTrack(long id, String title, String artist, long duration, Uri uri, String path, Uri albumArtUri) {
            this.id = id;
            this.title = title != null && !title.trim().isEmpty() ? title.trim() : "Unknown Track";
            this.artist = artist != null && !artist.trim().isEmpty() && !artist.contains("<unknown>") ? artist.trim() : "Local Track";
            this.duration = Math.max(0, duration);
            this.uri = uri;
            this.path = path;
            this.albumArtUri = albumArtUri;
        }

        public DeviceAudioTrack(String title, Uri uri, String path) {
            this(0, title, "Local Track", 0, uri, path, null);
        }
    }

    private final List<DeviceAudioTrack> deviceAudioTracks = new ArrayList<>();
    private int currentDeviceTrackIndex = -1;

    private BottomSheetDialog currentMusicControlSheet = null;

    private String[] getRequiredMusicPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[] {
                    Manifest.permission.READ_MEDIA_AUDIO
            };
        } else {
            return new String[] {
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };
        }
    }

    private boolean hasMusicPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private final ActivityResultLauncher<String[]> musicPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            resultMap -> {
                boolean granted = false;
                for (Boolean isGranted : resultMap.values()) {
                    if (Boolean.TRUE.equals(isGranted)) {
                        granted = true;
                        break;
                    }
                }
                if (granted || hasMusicPermission()) {
                    new Thread(() -> {
                        loadDeviceAudioTracks();
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Loaded " + deviceAudioTracks.size() + " device music tracks!", Toast.LENGTH_SHORT).show();
                            if (currentMusicControlSheet != null && currentMusicControlSheet.isShowing()) {
                                showMusicControlSheet();
                            }
                        });
                    }).start();
                } else {
                    Toast.makeText(this, "Permission required to access device music", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private String cleanAudioTitle(String rawTitle) {
        if (rawTitle == null) return "Audio Track";
        String clean = rawTitle.trim();
        int dotIdx = clean.lastIndexOf('.');
        if (dotIdx > 0 && dotIdx >= clean.length() - 5) {
            clean = clean.substring(0, dotIdx);
        }
        return clean.trim();
    }

    private List<DeviceAudioTrack> queryDeviceAudioTracks() {
        List<DeviceAudioTrack> list = new ArrayList<>();
        if (!hasMusicPermission()) {
            return list;
        }

        Set<String> seenKeys = new HashSet<>();

        String[] projection = new String[] {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID
        };

        Uri[] collections;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collections = new Uri[] {
                    MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL),
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    MediaStore.Audio.Media.INTERNAL_CONTENT_URI
            };
        } else {
            collections = new Uri[] {
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    MediaStore.Audio.Media.INTERNAL_CONTENT_URI
            };
        }

        for (Uri collection : collections) {
            if (collection == null) continue;
            try (Cursor cursor = getContentResolver().query(collection, projection, null, null, MediaStore.Audio.Media.TITLE + " ASC")) {
                if (cursor != null) {
                    int idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                    int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                    int nameColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
                    int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                    int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
                    int dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                    int albumIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);

                    while (cursor.moveToNext()) {
                        long id = (idColumn != -1) ? cursor.getLong(idColumn) : System.currentTimeMillis();
                        String title = (titleColumn != -1) ? cursor.getString(titleColumn) : null;
                        String displayName = (nameColumn != -1) ? cursor.getString(nameColumn) : null;

                        if (title == null || title.trim().isEmpty() || title.equalsIgnoreCase("<unknown>")) {
                            title = displayName;
                        }
                        if (title == null || title.trim().isEmpty()) {
                            continue;
                        }
                        title = cleanAudioTitle(title);

                        String artist = (artistColumn != -1) ? cursor.getString(artistColumn) : "Local Track";
                        if (artist == null || artist.contains("<unknown>")) artist = "Local Track";

                        long duration = (durationColumn != -1) ? cursor.getLong(durationColumn) : 0;
                        String path = (dataColumn != -1) ? cursor.getString(dataColumn) : null;

                        Uri contentUri = ContentUris.withAppendedId(collection, id);

                        long albumId = (albumIdColumn != -1) ? cursor.getLong(albumIdColumn) : -1;
                        Uri albumArtUri = (albumId > 0) ? ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId) : null;

                        String key = title.toLowerCase() + "_" + (path != null ? path.toLowerCase() : String.valueOf(id));
                        if (seenKeys.add(key)) {
                            list.add(new DeviceAudioTrack(id, title, artist, duration, contentUri, path, albumArtUri));
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("RoomChatActivity", "Error querying MediaStore collection: " + collection, e);
            }
        }

        // Query MediaStore.Files database for any audio files
        try {
            Uri filesUri = MediaStore.Files.getContentUri("external");
            String filesSelection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO
                    + " OR " + MediaStore.Files.FileColumns.MIME_TYPE + " LIKE 'audio/%'";
            String[] filesProjection = new String[] {
                    MediaStore.Files.FileColumns._ID,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.TITLE,
                    MediaStore.Files.FileColumns.DATA
            };
            try (Cursor cursor = getContentResolver().query(filesUri, filesProjection, filesSelection, null, null)) {
                if (cursor != null) {
                    int idCol = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID);
                    int titleCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.TITLE);
                    int nameCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME);
                    int dataCol = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);

                    while (cursor.moveToNext()) {
                        long id = (idCol != -1) ? cursor.getLong(idCol) : System.currentTimeMillis();
                        String title = (titleCol != -1) ? cursor.getString(titleCol) : null;
                        String name = (nameCol != -1) ? cursor.getString(nameCol) : null;
                        if (title == null || title.isEmpty()) title = name;
                        if (title == null || title.isEmpty()) continue;
                        title = cleanAudioTitle(title);

                        String path = (dataCol != -1) ? cursor.getString(dataCol) : null;
                        Uri contentUri = ContentUris.withAppendedId(filesUri, id);

                        String key = title.toLowerCase() + "_" + (path != null ? path.toLowerCase() : String.valueOf(id));
                        if (seenKeys.add(key)) {
                            list.add(new DeviceAudioTrack(id, title, "Audio File", 0, contentUri, path, null));
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e("RoomChatActivity", "Error querying MediaStore.Files", e);
        }

        // 2. Fast Targeted File System Scan Fallback for Public Audio Directories
        try {
            List<File> scanDirs = new ArrayList<>();
            scanDirs.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
            scanDirs.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
            scanDirs.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS));
            scanDirs.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES));
            scanDirs.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS));

            File rootExt = Environment.getExternalStorageDirectory();
            if (rootExt != null && rootExt.exists()) {
                scanDirs.add(new File(rootExt, "Music"));
                scanDirs.add(new File(rootExt, "Download"));
                scanDirs.add(new File(rootExt, "WhatsApp/Media/WhatsApp Audio"));
                scanDirs.add(new File(rootExt, "Telegram/Telegram Audio"));
                scanDirs.add(new File(rootExt, "Snaptube/download"));
                scanDirs.add(new File(rootExt, "Vidmate/download"));
            }

            for (File dir : scanDirs) {
                if (dir != null && dir.exists() && dir.isDirectory() && dir.canRead()) {
                    scanAudioDirectory(dir, list, seenKeys, 0);
                }
            }
        } catch (Exception e) {
            Log.e("RoomChatActivity", "Error in filesystem fallback scan", e);
        }

        // Sort tracks alphabetically by Title
        Collections.sort(list, (a, b) -> a.title.compareToIgnoreCase(b.title));

        return list;
    }

    private void scanAudioDirectory(File dir, List<DeviceAudioTrack> list, Set<String> seenKeys, int depth) {
        if (dir == null || !dir.exists() || !dir.canRead() || depth > 4) return;
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f == null || !f.canRead()) continue;
            if (f.isDirectory()) {
                String folderName = f.getName().toLowerCase();
                if (folderName.startsWith(".") || folderName.equals("android") || folderName.equals("cache")) {
                    continue;
                }
                scanAudioDirectory(f, list, seenKeys, depth + 1);
            } else if (f.isFile()) {
                String name = f.getName();
                String lowerName = name.toLowerCase();
                if (lowerName.endsWith(".mp3") || lowerName.endsWith(".m4a") || lowerName.endsWith(".aac") ||
                    lowerName.endsWith(".wav") || lowerName.endsWith(".ogg") || lowerName.endsWith(".flac") ||
                    lowerName.endsWith(".opus") || lowerName.endsWith(".wma") || lowerName.endsWith(".amr")) {

                    String title = cleanAudioTitle(name);
                    String path = f.getAbsolutePath();
                    String key = title.toLowerCase() + "_" + path.toLowerCase();

                    if (seenKeys.add(key)) {
                        list.add(new DeviceAudioTrack(
                                System.currentTimeMillis() + list.size(),
                                title,
                                "Device Audio",
                                f.length() > 0 ? 180000L : 0L,
                                Uri.fromFile(f),
                                path,
                                null
                        ));
                    }
                }
            }
        }
    }

    private void loadDeviceAudioTracks() {
        List<DeviceAudioTrack> scanned = queryDeviceAudioTracks();
        if (!scanned.isEmpty()) {
            deviceAudioTracks.clear();
            deviceAudioTracks.addAll(scanned);
        }
    }

    private void playDeviceTrack(int index) {
        if (!isAdminOrHost()) {
            Toast.makeText(this, "Only Room Host or Administrators can play music!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!hasMusicPermission()) {
            musicPermissionLauncher.launch(getRequiredMusicPermissions());
            return;
        }

        if (deviceAudioTracks.isEmpty()) {
            loadDeviceAudioTracks();
        }

        if (deviceAudioTracks.isEmpty()) {
            Toast.makeText(this, "No music files found on device!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (index < 0) {
            index = deviceAudioTracks.size() - 1;
        } else if (index >= deviceAudioTracks.size()) {
            index = 0;
        }

        currentDeviceTrackIndex = index;
        DeviceAudioTrack track = deviceAudioTracks.get(currentDeviceTrackIndex);

        String audioPath = null;
        if (track.path != null && new File(track.path).canRead()) {
            audioPath = track.path;
        } else if (track.uri != null) {
            try {
                File cacheFile = new File(getCacheDir(), "room_music_track_" + track.id + ".mp3");
                try (InputStream in = getContentResolver().openInputStream(track.uri);
                     OutputStream out = new FileOutputStream(cacheFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((in != null) && (bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
                if (cacheFile.exists()) {
                    audioPath = cacheFile.getAbsolutePath();
                }
            } catch (Exception e) {
                Log.e("RoomChatActivity", "Error caching device audio", e);
            }
        }

        if (audioPath != null) {
            currentPlayingSongName = track.title;
            ZegoManager.getInstance().playMusicResource(audioPath, track.title);
            syncMusicStateToFirebase(track.title, true);
            Toast.makeText(this, "🎵 Playing: " + track.title, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Unable to play track: " + track.title, Toast.LENGTH_SHORT).show();
        }
    }

    private void playNextDeviceTrack() {
        if (deviceAudioTracks.isEmpty()) {
            playDeviceTrack(0);
        } else {
            playDeviceTrack((currentDeviceTrackIndex + 1) % deviceAudioTracks.size());
        }
    }

    private void playPrevDeviceTrack() {
        if (deviceAudioTracks.isEmpty()) {
            playDeviceTrack(0);
        } else {
            int prevIndex = currentDeviceTrackIndex - 1;
            if (prevIndex < 0) prevIndex = deviceAudioTracks.size() - 1;
            playDeviceTrack(prevIndex);
        }
    }

    private Uri editRoomSelectedImageUri = null;
    private ImageView ivEditRoomCoverRef = null;

    private final ActivityResultLauncher<Intent> editRoomImagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    editRoomSelectedImageUri = result.getData().getData();
                    if (ivEditRoomCoverRef != null) {
                        Glide.with(this)
                                .load(editRoomSelectedImageUri)
                                .placeholder(R.drawable.logo_placeholder)
                                .into(ivEditRoomCoverRef);
                    }
                }
            }
    );

    private final long roomJoinTime = System.currentTimeMillis();

    private AudioRoomBackgroundView backgroundView;
    private SeatAdapter seatAdapter;
    private ChatAdapter chatAdapter;
    private RecyclerView rvSeats, rvChat;
    private BottomSheetDialog morePanelDialog;

    private ImageView btnMic, btnSpeaker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }

        setContentView(R.layout.activity_room_chat);

        initParams();
        initViews();
        
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 102);
        } else {
            initZego();
        }
        
        setupFirebaseListeners();

        if (hasMusicPermission()) {
            new Thread(this::loadDeviceAudioTracks).start();
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitOptionsDialog();
            }
        });
    }

    private void initParams() {
        roomID = getIntent().getStringExtra("roomID");
        userName = getIntent().getStringExtra("username");
        userID = getIntent().getStringExtra("userID");
        isHost = getIntent().getBooleanExtra("host", false);
        roomNameLabel = getIntent().getStringExtra("room_name");
        roomImg = getIntent().getStringExtra("img");

        if (roomID == null || roomID.trim().isEmpty()) roomID = "default_room";

        FirebaseUser currentAuthUser = FirebaseAuth.getInstance().getCurrentUser();

        if (userID == null || userID.trim().isEmpty()) {
            if (currentAuthUser != null && currentAuthUser.getUid() != null) {
                userID = currentAuthUser.getUid();
            } else {
                userID = "user_" + System.currentTimeMillis();
            }
        }

        if (userName == null || userName.trim().isEmpty()) {
            if (currentAuthUser != null && currentAuthUser.getDisplayName() != null && !currentAuthUser.getDisplayName().trim().isEmpty()) {
                userName = currentAuthUser.getDisplayName();
            } else {
                userName = SessionManager.getInstance(this).getName();
                if (userName == null || userName.trim().isEmpty()) {
                    userName = "User_" + new Random().nextInt(1000);
                }
            }
        }

        String hostUid = getIntent().getStringExtra("uid");
        if (hostUid == null || hostUid.trim().isEmpty()) {
            if (isHost) hostUid = userID;
        }
        SeatManager.getInstance().setHostUserID(hostUid);
        SeatManager.getInstance().setCurrentRoomID(roomID);

        UserProfileCache.getUserProfile(userID, profile -> {});
        TwentyFourHourRuleManager.record24HourEvent(userID, roomID);
        verifyPrivateRoomAccess();
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        String newRoomId = intent.getStringExtra("roomID");
        if (newRoomId != null && roomID != null && !newRoomId.equals(roomID)) {
            leaveRoom();
            startActivity(intent);
        } else {
            if (isMinimized) { // ROOM MINIMIZE FIX
                isMinimized = false; // ROOM MINIMIZE FIX
                RoomFloatingManager.getInstance().removeFloatingBubble(); // ROOM MINIMIZE FIX
            }
            verifyPrivateRoomAccess();
        }
    }

    private void verifyPrivateRoomAccess() {
        if (roomID == null || roomID.trim().isEmpty()) return;
        boolean isPinVerified = getIntent().getBooleanExtra("pinVerified", false);
        boolean isInvitedFlag = getIntent().getBooleanExtra("isInvited", false);

        if (isHost || isPinVerified || isInvitedFlag) return;

        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomID);
        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;
                if (snapshot.exists()) {
                    Boolean isPrivateVal = snapshot.child("isPrivate").getValue(Boolean.class);
                    String roomPinVal = snapshot.child("roomPin").getValue(String.class);
                    if (roomPinVal == null) roomPinVal = snapshot.child("pin").getValue(String.class);
                    String lockImgVal = snapshot.child("privateImage").getValue(String.class);

                    if (isPrivateVal != null && isPrivateVal) {
                        DatabaseReference inviteRef = FirebaseDatabase.getInstance().getReference("seat_invites").child(userID);
                        final String expectedPin = roomPinVal;
                        final String lockImg = lockImgVal;

                        inviteRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot inviteSnap) {
                                if (isFinishing() || isDestroyed()) return;
                                boolean hasInvite = false;
                                if (inviteSnap.exists()) {
                                    String targetRoom = inviteSnap.child("roomId").getValue(String.class);
                                    String status = inviteSnap.child("status").getValue(String.class);
                                    if (roomID.equals(targetRoom) && !"DECLINED".equalsIgnoreCase(status)) {
                                        hasInvite = true;
                                    }
                                }

                                if (!hasInvite) {
                                    showInRoomPinDialog(expectedPin, lockImg);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                showInRoomPinDialog(expectedPin, lockImg);
                            }
                        });
                    }
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showInRoomPinDialog(String expectedPin, String lockImg) {
        if (isFinishing() || isDestroyed()) return;

        // Mute all room audio playback until correct PIN is entered!
        ZegoManager.getInstance().setSpeakerOn(false);
        if (btnSpeaker != null) {
            btnSpeaker.setImageResource(R.drawable.ic_speaker_off);
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        applyGlassyStyle(dialog);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_enter_room_pin, null, false);
        if (dialogView == null) return;

        ImageView btnClosePinDialog = dialogView.findViewById(R.id.btnClosePinDialog);
        ImageView ivPrivateRoomBanner = dialogView.findViewById(R.id.ivPrivateRoomBanner);
        TextView tvPrivateRoomTitle = dialogView.findViewById(R.id.tvPrivateRoomTitle);
        EditText etEnterPin = dialogView.findViewById(R.id.etEnterPin);
        View btnUnlockRoom = dialogView.findViewById(R.id.btnUnlockRoom);

        if (btnClosePinDialog != null) {
            btnClosePinDialog.setOnClickListener(v -> {
                dialog.dismiss();
                finish();
            });
        }

        if (tvPrivateRoomTitle != null && roomNameLabel != null) {
            tvPrivateRoomTitle.setText("🔒 " + roomNameLabel + " (Private)");
        }

        if (ivPrivateRoomBanner != null) {
            String banner = (lockImg != null && !lockImg.trim().isEmpty()) ? lockImg : roomImg;
            if (banner != null && !banner.trim().isEmpty()) {
                Glide.with(this).load(banner).placeholder(R.drawable.bg_room_gradient).into(ivPrivateRoomBanner);
            }
        }

        if (btnUnlockRoom != null) {
            btnUnlockRoom.setOnClickListener(v -> {
                String inputPin = etEnterPin != null ? etEnterPin.getText().toString().trim() : "";
                if (inputPin.isEmpty()) {
                    Toast.makeText(this, "Please enter 4-digit PIN!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (expectedPin != null && inputPin.equals(expectedPin.trim())) {
                    Toast.makeText(this, "🔓 Room Unlocked!", Toast.LENGTH_SHORT).show();
                    // Unmute room audio playback once PIN is verified!
                    ZegoManager.getInstance().setSpeakerOn(true);
                    if (btnSpeaker != null) {
                        btnSpeaker.setImageResource(R.drawable.ic_speaker_on);
                    }
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "❌ Incorrect Room PIN!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        dialog.setOnCancelListener(d -> finish());
        dialog.setContentView(dialogView);
        dialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopGameCarouselAutoScroll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startGameCarouselAutoScroll();
        if (isMinimized) { // ROOM MINIMIZE FIX
            isMinimized = false; // ROOM MINIMIZE FIX
            RoomFloatingManager.getInstance().removeFloatingBubble(); // ROOM MINIMIZE FIX
        } // ROOM MINIMIZE FIX

        View container = findViewById(R.id.globalBannerGiftContainer);
        SVGAImageView player = findViewById(R.id.globalSvgaBannerPlayer);
        TextView tvNotice = findViewById(R.id.globalTvBannerNotice);
        GlobalGiftBannerManager.getInstance().bindContainer(this, container, player, tvNotice);

        if (userID != null) {
            UserProfileCache.invalidate(userID);
            UserProfileCache.getUserProfile(userID, profile -> {
                if (profile != null && profile.name != null && !profile.name.isEmpty()) {
                    userName = profile.name;
                }
            });
        }
    }

    private void initViews() {
        svgaPlayer = findViewById(R.id.svgaPlayer);
        if (svgaPlayer != null) {
            svgaPlayer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        bannerGiftContainer = findViewById(R.id.bannerGiftContainer);
        svgaBannerPlayer = findViewById(R.id.svgaBannerPlayer);
        tvBannerNotice = findViewById(R.id.tvBannerNotice);
        if (svgaBannerPlayer != null) {
            svgaBannerPlayer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        bannerEntryContainer = findViewById(R.id.bannerEntryContainer);
        svgaEntryBannerPlayer = findViewById(R.id.svgaEntryBannerPlayer);
        tvEntryBannerNotice = findViewById(R.id.tvEntryBannerNotice);
        if (svgaEntryBannerPlayer != null) {
            svgaEntryBannerPlayer.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        try {
            ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 4, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
            SVGAParser.Companion.setThreadPoolExecutor(executor);
        } catch (Exception ignored) {}

        svgaParser = new SVGAParser(this);
        notificationAnimator = new NotificationAnimator(findViewById(R.id.giftOverlayContainer));

        backgroundView = findViewById(R.id.audioRoomBackground);
        backgroundView.setRoomName(roomNameLabel != null ? roomNameLabel : "Room");
        backgroundView.setRoomID(roomID);

        TextView tvRoomName = findViewById(R.id.tvRoomName);
        TextView tvRoomId = findViewById(R.id.tvRoomId);
        ImageView ivRoomAvatar = findViewById(R.id.ivRoomAvatar);
        tvTrophyCount = findViewById(R.id.tvTrophyCount);
        tvMemberCount = findViewById(R.id.tvMemberCount);

        ImageView ivMemberIcon = findViewById(R.id.ivMemberIcon);
        View layoutMemberCount = findViewById(R.id.layoutMemberCount);
        View.OnClickListener openMembersListener = v -> {
            if (!isFastClick(v)) {
                showRoomMembersDialog();
            }
        };
        if (layoutMemberCount != null) {
            layoutMemberCount.setOnClickListener(openMembersListener);
        }
        if (tvMemberCount != null) {
            tvMemberCount.setOnClickListener(openMembersListener);
        }
        if (ivMemberIcon != null) {
            ivMemberIcon.setOnClickListener(openMembersListener);
        }


        // CONTRIBUTION ARROW: Click listener on trophy arrow & room trophy container
        View ivTrophyArrow = findViewById(R.id.ivTrophyArrow);
        View layoutRoomTrophy = findViewById(R.id.layoutRoomTrophy);
        View.OnClickListener openContributionsListener = v -> {
            if (!isFastClick(v)) {
                showRoomContributionsDialog();
            }
        };
        if (ivTrophyArrow != null) {
            ivTrophyArrow.setOnClickListener(openContributionsListener);
        }
        if (layoutRoomTrophy != null) {
            layoutRoomTrophy.setOnClickListener(openContributionsListener);
        }

        View layoutRoomMusic = findViewById(R.id.layoutRoomMusic);
        if (layoutRoomMusic != null) {
            layoutRoomMusic.setVisibility(isAdminOrHost() ? View.VISIBLE : View.GONE);
            layoutRoomMusic.setOnClickListener(v -> {
                if (!isFastClick(v)) {
                    showMusicControlSheet();
                }
            });
        }

        if (tvRoomName != null && roomNameLabel != null && !roomNameLabel.isEmpty()) {
            tvRoomName.setText(roomNameLabel);
        }
        if (tvRoomId != null && roomID != null && !roomID.isEmpty()) {
            tvRoomId.setText("ID:" + roomID);
        }

        View.OnClickListener copyRoomIdListener = v -> {
            if (roomID != null && !roomID.trim().isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Room ID", roomID);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(RoomChatActivity.this, "Room ID (" + roomID + ") copied to clipboard!", Toast.LENGTH_SHORT).show();
                }
            }
        };

        View btnCopyRoomId = findViewById(R.id.btnCopyRoomId);
        if (btnCopyRoomId != null) {
            btnCopyRoomId.setOnClickListener(v -> { if (!isFastClick(v)) copyRoomIdListener.onClick(v); });
        }
        if (tvRoomId != null) {
            tvRoomId.setOnClickListener(v -> { if (!isFastClick(v)) copyRoomIdListener.onClick(v); });
        }
        View layoutRoomId = findViewById(R.id.layoutRoomId);
        if (layoutRoomId != null) {
            layoutRoomId.setOnClickListener(v -> { if (!isFastClick(v)) copyRoomIdListener.onClick(v); });
        }
        if (ivRoomAvatar != null && roomImg != null && !roomImg.isEmpty()) {
            Glide.with(this)
                    .load(roomImg)
                    .placeholder(R.drawable.logo_placeholder)
                    .error(R.drawable.logo_placeholder)
                    .into(ivRoomAvatar);
        }

        View btnRoomClose = findViewById(R.id.btnRoomClose);
        if (btnRoomClose != null) {
            btnRoomClose.setOnClickListener(v -> {
                if (isFastClick(v)) return;
                showExitOptionsDialog();
            });
        }

        View btnRoomAgencyIcon = findViewById(R.id.btnRoomAgencyIcon);
        if (btnRoomAgencyIcon != null) {
            btnRoomAgencyIcon.setVisibility((isHost || isAdminOrHost()) ? View.VISIBLE : View.GONE);
            btnRoomAgencyIcon.setOnClickListener(v -> {
                if (isFastClick(v)) return;
                if (userID != null) {
                    new JoinAgencyDialog(RoomChatActivity.this, userID).show();
                }
            });
        }

        View layoutUserInfo = findViewById(R.id.layoutUserInfo);
        if (layoutUserInfo != null) {
            layoutUserInfo.setOnClickListener(v -> {
                if (isFastClick(v)) return;
                List<SeatModel> seats = SeatManager.getInstance().getSeats();
                if (seats != null && !seats.isEmpty()) {
                    String hostUid = SeatManager.getInstance().getHostUserID();
                    int hostSeatIdx = SeatManager.getInstance().findUserSeatIndex(hostUid);
                    if (hostSeatIdx != -1) {
                        onSeatClicked(seats.get(hostSeatIdx));
                    } else {
                        SeatModel hostSeat = seats.get(0);
                        if (hostSeat != null) {
                            onSeatClicked(hostSeat);
                        }
                    }
                }
            });
        }


        View btnRoomShare = findViewById(R.id.btnRoomShare);
        if (btnRoomShare != null) {
            btnRoomShare.setOnClickListener(v -> {
                if (isFastClick(v)) return;
                String displayRoomName = (roomNameLabel != null && !roomNameLabel.trim().isEmpty()) ? roomNameLabel : "Audio Party Room";
                String displayHostName = (userName != null && !userName.trim().isEmpty()) ? userName : "Room Host";

                String shareMessage = "🎉 You're invited to join our Live Voice Room! 🎙️\n\n"
                        + "🏠 Room Name: " + displayRoomName + "\n"
                        + "🔑 Room ID: " + roomID + "\n"
                        + "👤 Host: " + displayHostName + "\n\n"
                        + "🔥 Join us now for live voice chat, music, fun games & gifts! 💬🎁🎵\n\n"
                        + "👇 Open Room Chat App & enter Room ID (" + roomID + ") to join:\n"
                        + "https://play.google.com/store/apps/details?id=" + getPackageName();

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Join " + displayRoomName + " (Room ID: " + roomID + ")");
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(shareIntent, "Invite Friends via..."));
            });
        }

        rvSeats = findViewById(R.id.rvSeats);
        GridLayoutManager seatLayoutManager = new GridLayoutManager(this, 4) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        seatLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (position == 0) ? 4 : 1;
            }
        });
        rvSeats.setLayoutManager(seatLayoutManager);
        seatAdapter = new SeatAdapter(this::onSeatClicked);
        rvSeats.setAdapter(seatAdapter);
        seatAdapter.setSeats(SeatManager.getInstance().getSeats());

        rvSeats.post(() -> {
            if (rvSeats != null && seatAdapter != null) {
                int width = rvSeats.getWidth() - rvSeats.getPaddingLeft() - rvSeats.getPaddingRight();
                int height = rvSeats.getHeight() - rvSeats.getPaddingTop() - rvSeats.getPaddingBottom();
                if (height > 0) {
                    seatAdapter.setAvailableDimensions(width, height);
                }
            }
        });

        rvSeats.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (rvSeats != null && seatAdapter != null) {
                int width = (right - left) - rvSeats.getPaddingLeft() - rvSeats.getPaddingRight();
                int height = (bottom - top) - rvSeats.getPaddingTop() - rvSeats.getPaddingBottom();
                if (height > 0 && (seatAdapter.getAvailableHeight() != height || seatAdapter.getAvailableWidth() != width)) {
                    seatAdapter.setAvailableDimensions(width, height);
                }
            }
        });

        rvChat = findViewById(R.id.rvChatMessages);
        LinearLayoutManager chatLayoutManager = new LinearLayoutManager(this);
        chatLayoutManager.setStackFromEnd(true);
        rvChat.setLayoutManager(chatLayoutManager);
        chatAdapter = new ChatAdapter();
        rvChat.setAdapter(chatAdapter);

        setupBottomButtons();
    }

    private void setupBottomButtons() {
        findViewById(R.id.btnChat).setOnClickListener(v -> {
            if (isFastClick(v)) return;
            showCommentInputDialog();
        });

        btnSpeaker = findViewById(R.id.btnSpeaker);
        if (btnSpeaker != null) {
            boolean speakerState = ZegoManager.getInstance().isSpeakerOn();
            btnSpeaker.setImageResource(speakerState ? R.drawable.ic_speaker_on : R.drawable.ic_speaker_off);
            btnSpeaker.setOnClickListener(v -> {
                if (isFastClick(v)) return;
                boolean newState = !ZegoManager.getInstance().isSpeakerOn();
                ZegoManager.getInstance().setSpeakerOn(newState);
                btnSpeaker.setImageResource(newState ? R.drawable.ic_speaker_on : R.drawable.ic_speaker_off);
                Toast.makeText(this, newState ? "Speaker Turned On 🔊" : "Speaker Muted 🔇", Toast.LENGTH_SHORT).show();
            });
        }

        btnMic = findViewById(R.id.btnMic);
        if (btnMic != null) {
            btnMic.setOnClickListener(v -> {
                if (isFastClick(v)) return;
                int seatIndex = SeatManager.getInstance().findUserSeatIndex(userID);
                if (seatIndex != -1) {
                    SeatModel model = SeatManager.getInstance().getSeats().get(seatIndex);
                    if (model.isMuted) {
                        Toast.makeText(this, "The host has muted your microphone!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    boolean newState = !model.isMicOn;
                    SeatManager.getInstance().updateMicStatus(seatIndex, newState);
                    ZegoManager.getInstance().setMicEnabled(newState);
                    if (newState) {
                        ZegoManager.getInstance().startPublishing();
                    }
                    btnMic.setImageResource(newState ? R.drawable.ic_mic_on : R.drawable.ic_mic_off);
                    Toast.makeText(this, newState ? "Microphone Unmuted 🎙️" : "Microphone Muted 🔇", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Take a seat first to use mic", Toast.LENGTH_SHORT).show();
                }
            });
        }

        View moreView = findViewById(R.id.more);
        if (moreView != null) {
            moreView.setVisibility(isAdminOrHost() ? View.VISIBLE : View.GONE);
            moreView.setOnClickListener(v -> {
                if (isFastClick(v)) return;
                showMorePanelDialog();
            });
        }

        View btnMore = findViewById(R.id.btnMore);
        if (btnMore != null) {
            btnMore.setVisibility(View.GONE);
        }

        View btnGame = findViewById(R.id.btnGame);
        if (btnGame != null) {
            AnimationHelper.applyClickAnimation(btnGame);
            btnGame.setOnClickListener(v -> {
                if (isFastClick(v)) return;
                showRoomGameDialog();
            });
        }
        setupGameCarousel();

        findViewById(R.id.btnGift).setOnClickListener(v -> {
            if (isFastClick(v)) return;
            showGiftDialog();
        });

        View btnEmoji = findViewById(R.id.btnEmoji);
        if (btnEmoji != null) {
            AnimationHelper.applyClickAnimation(btnEmoji);
            btnEmoji.setOnClickListener(v -> {
                if (isFastClick(v)) return;
                showEmojiPickerDialog();
            });
        }

        View btnSpinWheel = findViewById(R.id.btnSpinWheel);
        if (btnSpinWheel != null) {
            btnSpinWheel.setVisibility(View.GONE);
            AnimationHelper.applyClickAnimation(btnSpinWheel);
            btnSpinWheel.setOnClickListener(v -> {
                if (isFastClick(v)) return;
                showLuckySpinWheelDialog();
            });
        }

        ImageView btnSettings = findViewById(R.id.btnSettings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                if (isFastClick(v)) return;
                showThemeSelectionDialog();
            });
        }
    }

    private void initZego() {
        ZegoManager.getInstance().init(getApplication(), appID, appSign);
        ZegoManager.getInstance().addListener(zegoListener);
        ZegoManager.getInstance().loginRoom(roomID, userID, userName, isHost);

        SeatManager.getInstance().addListener(seatListener);
        updateGlobalUserCount();
    }

    private final ZegoManager.ZegoManagerListener zegoListener = new ZegoManager.ZegoManagerListener() {
        @Override
        public void onLoginResult(int errorCode) {
            if (errorCode == 0) {
                broadcastUserEntry();
                if (isHost) {
                    ZegoManager.getInstance().setMicEnabled(true);
                    ZegoManager.getInstance().startPublishing();
                }
            } else {
                Toast.makeText(RoomChatActivity.this, "Failed to join room: " + errorCode, Toast.LENGTH_LONG).show();
                finish();
            }
        }

        @Override
        public void onUserJoined(ZegoUser user) {
            updateGlobalUserCount();
            if (user != null && user.userName != null) {
                String displayName = user.userName.trim().isEmpty() ? "User" : user.userName;
                ChatMessage systemMsg = new ChatMessage();
                systemMsg.setSenderId("SYSTEM");
                systemMsg.setMessage(displayName + " joined the room");
                systemMsg.setTimestamp(System.currentTimeMillis());
                runOnUiThread(() -> {
                    if (chatAdapter != null) {
                        chatAdapter.addAutoExpiringMessage(systemMsg, ChatAdapter.ROOM_MESSAGE_EXPIRE_MS);
                    }
                });
            }
        }

        @Override
        public void onUserLeft(ZegoUser user) {
            updateGlobalUserCount();
            if (user != null && user.userID != null && !user.userID.isEmpty()) {
                String displayName = (user.userName != null && !user.userName.trim().isEmpty()) ? user.userName : "User";
                ChatMessage systemMsg = new ChatMessage();
                systemMsg.setSenderId("SYSTEM");
                systemMsg.setMessage(displayName + " left the room");
                systemMsg.setTimestamp(System.currentTimeMillis());
                runOnUiThread(() -> {
                    if (chatAdapter != null) {
                        chatAdapter.addAutoExpiringMessage(systemMsg, ChatAdapter.ROOM_MESSAGE_EXPIRE_MS);
                    }
                });
            }
        }

        @Override
        public void onRoomExtraInfoUpdate(String roomID, List<ZegoRoomExtraInfo> roomExtraInfoList) {
            SeatManager.getInstance().updateSeatsFromExtraInfo(roomExtraInfoList);
        }

        @Override
        public void onAudioLevelUpdate(String userID, float soundLevel) {
            runOnUiThread(() -> {
                // soundLevel > 5 is usually enough to consider someone speaking
                seatAdapter.setSpeaking(userID, soundLevel > 5);
            });
        }

        @Override
        public void onIMRecvBroadcastMessage(String roomID, List<ZegoBroadcastMessageInfo> messageList) {
            if (messageList == null) return;
            runOnUiThread(() -> {
                List<ZegoBroadcastMessageInfo> textMessages = new ArrayList<>();
                for (ZegoBroadcastMessageInfo info : messageList) {
                    if (info != null && info.message != null && info.message.trim().startsWith("{\"type\":\"GIFT\"")) {
                        try {
                            JSONObject json = new JSONObject(info.message);
                            String giftId = json.optString("giftId", "");
                            String sName = json.optString("senderName", "User");
                            String gName = json.optString("giftName", "Gift");
                            if (giftId.isEmpty()) {
                                giftId = sName + "_" + gName + "_" + (System.currentTimeMillis() / 1500);
                            }
                            if (markGiftProcessed(giftId)) continue;

                            String sAvatar = json.optString("senderAvatar", "");
                            String rName = json.optString("recipientName", "");
                            String gSvga = json.optString("giftSvga", "");
                            int iconRes = json.optInt("iconRes", R.drawable.gift_icon);

                            int qty = json.optInt("quantity", 1);
                            if (qty <= 1) {
                                int xIdx = gName.indexOf(" (x");
                                if (xIdx != -1) {
                                    try {
                                        String numStr = gName.substring(xIdx + 3, gName.indexOf(")", xIdx));
                                        qty = Integer.parseInt(numStr);
                                    } catch (Exception ignored) {}
                                }
                            }

                            // Enqueue unified gift event (animation + banner synchronized together!)
                            enqueueGiftEvent(giftId, sName, gName, rName, qty, gSvga);
                        } catch (Exception ignored) {}
                    } else {
                        textMessages.add(info);
                    }
                }
                if (!textMessages.isEmpty()) {
                    chatAdapter.addMessages(textMessages);
                    if (rvChat != null && chatAdapter != null && chatAdapter.getItemCount() > 0) {
                        rvChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                    }
                }
            });
        }
    };

    private final SeatManager.SeatListener seatListener = seats -> runOnUiThread(() -> {
        seatAdapter.setSeats(seats);

        if (morePanelDialog != null && morePanelDialog.isShowing()) {
            TextView tvSub = morePanelDialog.findViewById(R.id.tvSeatsSubtitle);
            TextView tvSel = morePanelDialog.findViewById(R.id.tvSeatsSelected);
            int total = SeatManager.getInstance().getTotalSeats();
            int occupied = 0;
            for (SeatModel s : seats) {
                if (s != null && !s.isEmpty()) occupied++;
            }
            int free = Math.max(0, total - occupied);
            if (tvSub != null) {
                tvSub.setText(total + " Total Seats · " + occupied + " Occupied, " + free + " Free");
            }
            if (tvSel != null) {
                tvSel.setText("✓ Selected: " + total + " Seats");
            }
        }

        // Sync local mic icon if self seat changed
        int myIndex = SeatManager.getInstance().findUserSeatIndex(userID);

        if (myIndex != -1) {
            SeatModel mySeat = seats.get(myIndex);
            boolean isMicOn = mySeat.isMicOn;

            // Strict Mute Preservation: Check local ZegoManager mic state
            boolean currentLocalMic = ZegoManager.getInstance().isMicEnabled();
            if (!currentLocalMic) {
                isMicOn = false;
            }

            // Host enforcement: if muted, force mic off locally
            if (mySeat.isMuted) {
                isMicOn = false;
            }

            ZegoManager.getInstance().setMicEnabled(isMicOn);
            ZegoManager.getInstance().startPublishing();
            if (btnMic != null) {
                btnMic.setImageResource(isMicOn ? R.drawable.ic_mic_on : R.drawable.ic_mic_off);
            }
        } else {
            // Not on seat, ensure mic publishing is stopped unless music is playing
            if (!ZegoManager.getInstance().isMusicPlaying()) {
                ZegoManager.getInstance().stopPublishing();
            } else {
                ZegoManager.getInstance().setMicEnabled(false);
                ZegoManager.getInstance().startPublishing();
            }
            if (btnMic != null) {
                btnMic.setImageResource(R.drawable.ic_mic_off);
            }
        }
    });

    private void updateGlobalUserCount() {
        runOnUiThread(() -> {
            int count = ZegoManager.getInstance().getRoomUserCount();
            int displayCount = Math.max(count, 1);
            if (backgroundView != null) {
                backgroundView.setUserCount(displayCount);
            }
            if (tvMemberCount != null) {
                tvMemberCount.setText(String.valueOf(displayCount));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission granted. Click the seat again.", Toast.LENGTH_SHORT).show();
        } else if (requestCode == 102 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initZego();
        }
    }

    private void onSeatClicked(SeatModel model) {
        if (model == null) return;
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 101);
            return;
        }

        boolean isEmpty = model.userID == null || model.userID.trim().isEmpty();

        if (isEmpty) {
            // Seat 0 Security: Host Seat is strictly reserved for the Host
            if (model.index == 0 && !isHost) {
                Toast.makeText(this, "Host Seat is reserved for the Host!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Locked Seat Security: If seat is locked, normal members cannot sit directly
            if (model.isClosed && !isAdminOrHost()) {
                Toast.makeText(this, "This seat is locked 🔒 Ask Host or Admin to unlock or invite you!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Normal members clicking an OPEN seat: take seat directly in 1 tap!
            if (!isAdminOrHost()) {
                String myAvatar = SessionManager.getInstance(this).getAvatar();
                UserProfileCache.getUserProfile(userID, profile -> {
                    String frame = (profile != null && profile.equippedFrame != null) ? profile.equippedFrame : "";
                    boolean success = SeatManager.getInstance().takeSeat(model.index, userID, userName, myAvatar, frame, false);
                    if (!success) {
                        Toast.makeText(this, "This seat is already occupied!", Toast.LENGTH_SHORT).show();
                    } else {
                        ZegoManager.getInstance().startPublishing();
                        String seatNameLabel = model.index == 0 ? "Host Seat" : ("Seat " + model.index);
                        Toast.makeText(this, "You took " + seatNameLabel + "!", Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }

            BottomSheetDialog emptyDialog = new BottomSheetDialog(this);
            applyGlassyStyle(emptyDialog);
            View emptyView = getLayoutInflater().inflate(R.layout.dialog_empty_seat_host, null, false);
            emptyDialog.setContentView(emptyView);

            TextView tvOptionOnMic = emptyView.findViewById(R.id.tvOptionOnMic);
            TextView tvOptionInvite = emptyView.findViewById(R.id.tvOptionInvite);
            TextView tvOptionLock = emptyView.findViewById(R.id.tvOptionLock);
            TextView tvOptionMute = emptyView.findViewById(R.id.tvOptionMute);
            View btnOptionCancel = emptyView.findViewById(R.id.btnOptionCancel);

            if (tvOptionLock != null) {
                tvOptionLock.setVisibility(isAdminOrHost() ? View.VISIBLE : View.GONE);
                tvOptionLock.setText(model.isClosed ? "Unlock" : "Lock");
            }

            if (tvOptionMute != null) {
                tvOptionMute.setVisibility(isAdminOrHost() ? View.VISIBLE : View.GONE);
                tvOptionMute.setText(model.isMuted ? "Unmute" : "Mute");
            }

            if (tvOptionInvite != null) {
                tvOptionInvite.setVisibility(isAdminOrHost() ? View.VISIBLE : View.GONE);
            }

            // 1. Take Seat option
            if (tvOptionOnMic != null) {
                tvOptionOnMic.setText("Take Seat");
                tvOptionOnMic.setOnClickListener(v -> {
                    if (model.isClosed && !isAdminOrHost()) {
                        Toast.makeText(this, "This seat is locked 🔒", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (model.index == 0 && !isHost) {
                        Toast.makeText(this, "Host Seat is reserved for the Host!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String myAvatar = SessionManager.getInstance(this).getAvatar();
                    UserProfileCache.getUserProfile(userID, profile -> {
                        String frame = (profile != null && profile.equippedFrame != null) ? profile.equippedFrame : "";
                        boolean success = SeatManager.getInstance().takeSeat(model.index, userID, userName, myAvatar, frame, isAdminOrHost());
                        if (!success) {
                            Toast.makeText(this, "This seat is already occupied!", Toast.LENGTH_SHORT).show();
                        } else {
                            ZegoManager.getInstance().startPublishing();
                            String seatNameLabel = model.index == 0 ? "Host Seat" : ("Seat " + model.index);
                            Toast.makeText(this, "You took " + seatNameLabel + "!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    emptyDialog.dismiss();
                });
            }

            // 2. Invite
            if (tvOptionInvite != null) {
                tvOptionInvite.setOnClickListener(v -> {
                    emptyDialog.dismiss();
                    showRoomMembersDialog(model.index);
                });
            }

            // 3. Lock / Unlock (Admin & Host Only)
            if (tvOptionLock != null) {
                tvOptionLock.setOnClickListener(v -> {
                    if (!isAdminOrHost()) {
                        Toast.makeText(this, "Only Room Host or Administrators can lock or unlock seats 🔒", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    boolean newClosedState = !model.isClosed;
                    SeatManager.getInstance().closeSeat(model.index, newClosedState);
                    Toast.makeText(this, newClosedState ? "Seat Locked 🔒" : "Seat Unlocked 🔓", Toast.LENGTH_SHORT).show();
                    emptyDialog.dismiss();
                });
            }

            // 4. Mute / Unmute
            if (tvOptionMute != null) {
                tvOptionMute.setOnClickListener(v -> {
                    boolean newMuteState = !model.isMuted;
                    SeatManager.getInstance().muteSeat(model.index, newMuteState);
                    Toast.makeText(this, newMuteState ? "Seat Muted" : "Seat Unmuted", Toast.LENGTH_SHORT).show();
                    emptyDialog.dismiss();
                });
            }

            // 5. Cancel
            if (btnOptionCancel != null) {
                btnOptionCancel.setOnClickListener(v -> emptyDialog.dismiss());
            }

            emptyDialog.show();
            return;
        }

        // Occupied Seat (Self or Other User)
        BottomSheetDialog profileDialog = new BottomSheetDialog(this);
        applyGlassyStyle(profileDialog);
        View profileView = getLayoutInflater().inflate(R.layout.dialog_user_profile_card, null, false);
        profileDialog.setContentView(profileView);
        profileDialog.show();

        ImageView ivProfileAvatar = profileView.findViewById(R.id.ivUserProfileAvatar);
        ImageView ivProfileFrame = profileView.findViewById(R.id.ivUserProfileFrame);
        SVGAImageView svgaProfileFrame = profileView.findViewById(R.id.svgaUserProfileFrame);
        TextView tvName = profileView.findViewById(R.id.tvUserName);
        TextView tvGenderBadge = profileView.findViewById(R.id.tvGenderBadge);
        TextView tvProfileId = profileView.findViewById(R.id.tvProfileId);
        TextView tvFollowersCount = profileView.findViewById(R.id.tvFollowersCount);
        TextView tvLikesBadge = profileView.findViewById(R.id.tvLikesBadge);

        View btnLikeUser = profileView.findViewById(R.id.btnLikeUser);
        ImageView ivLikeHeart = profileView.findViewById(R.id.ivLikeHeart);
        TextView tvLikesCount = profileView.findViewById(R.id.tvLikesCount);

        View btnMoreOptions = profileView.findViewById(R.id.btnMoreOptions);
        View btnActionFollow = profileView.findViewById(R.id.btnActionFollow);
        TextView tvActionFollowText = profileView.findViewById(R.id.tvActionFollowText);

        View btnActionAdd = profileView.findViewById(R.id.btnActionAdd);
        ImageView ivActionAddIcon = profileView.findViewById(R.id.ivActionAddIcon);
        TextView tvActionAddText = profileView.findViewById(R.id.tvActionAddText);


        View btnActionSend = profileView.findViewById(R.id.btnActionSend);

        String targetUid = model.userID != null ? model.userID : "";
        String targetRole = SeatManager.getInstance().getUserRole(targetUid);
        String roleTag = "host".equalsIgnoreCase(targetRole) || model.isHost() ? " (Host)" : ("admin".equalsIgnoreCase(targetRole) ? " (Admin)" : "");

        String initialName = (model.userName != null && !model.userName.isEmpty()) ? model.userName : "User";
        if (tvName != null) tvName.setText(initialName + roleTag);

        UserProfileCache.UserProfile cachedTarget = UserProfileCache.getDirectCachedProfile(targetUid);
        String initialPid = UserProfileCache.getDisplayNumericId(targetUid, cachedTarget != null ? cachedTarget.profileId : null);
        if (tvProfileId != null) tvProfileId.setText("ID: " + initialPid);

        if (ivProfileAvatar != null) {
            if (model.userAvatar != null && !model.userAvatar.isEmpty()) {
                Glide.with(this).load(model.userAvatar).placeholder(R.drawable.logo_placeholder).into(ivProfileAvatar);
            } else {
                Glide.with(this).load(R.drawable.logo_placeholder).into(ivProfileAvatar);
            }
        }

        DatabaseReference targetUserRef = FirebaseDatabase.getInstance().getReference("users").child(targetUid);
        targetUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;
                if (snapshot.exists()) {
                    String dbName = snapshot.child("name").getValue(String.class);
                    if (dbName != null && !dbName.isEmpty() && tvName != null) {
                        tvName.setText(dbName + roleTag);
                    }

                    String dbAvatar = snapshot.child("avtar").getValue(String.class);
                    if (dbAvatar != null && !dbAvatar.isEmpty() && ivProfileAvatar != null) {
                        Glide.with(RoomChatActivity.this).load(dbAvatar).placeholder(R.drawable.logo_placeholder).into(ivProfileAvatar);
                    }

                    String rawPid = snapshot.child("profileId").getValue(String.class);
                    String numericPid = UserProfileCache.getDisplayNumericId(targetUid, rawPid);
                    if (rawPid == null || !rawPid.equals(numericPid)) {
                        FirebaseDatabase.getInstance().getReference("users").child(targetUid).child("profileId").setValue(numericPid);
                        FirebaseDatabase.getInstance().getReference("users").child(targetUid).child("userId").setValue(numericPid);
                    }
                    if (tvProfileId != null) tvProfileId.setText("ID: " + numericPid);

                    String followers = snapshot.child("Followers").getValue(String.class);
                    if (tvFollowersCount != null) tvFollowersCount.setText((followers != null ? followers : "1") + " followers");

                    String gender = snapshot.child("gender").getValue(String.class);
                    String ageStr = "20";
                    if (snapshot.child("age").exists() && snapshot.child("age").getValue() != null) {
                        ageStr = String.valueOf(snapshot.child("age").getValue());
                    }
                    if (tvGenderBadge != null) {
                        if ("female".equalsIgnoreCase(gender)) {
                            tvGenderBadge.setText("♀" + ageStr);
                            tvGenderBadge.setBackgroundResource(R.drawable.bg_gender_male_pill);
                        } else {
                            tvGenderBadge.setText("♂" + ageStr);
                            tvGenderBadge.setBackgroundResource(R.drawable.bg_gender_male_pill);
                        }
                    }

                    TextView tvLocation = profileView.findViewById(R.id.tvLocation);
                    if (tvLocation != null) {
                        String loc = snapshot.child("location").getValue(String.class);
                        if (loc == null || loc.trim().isEmpty()) loc = "India";
                        tvLocation.setText(loc);
                    }

                    // Frame Overlay on Front Avatar
                    String equippedFrame = snapshot.child("equipped_frame").getValue(String.class);
                    FrameUtils.displayFrame(RoomChatActivity.this, equippedFrame, ivProfileFrame, svgaProfileFrame);

                    // Check CP Binding for Double Avatar Display
                    View flCpPartnerAvatarContainer = profileView.findViewById(R.id.flCpPartnerAvatarContainer);
                    ImageView ivCpPartnerAvatar = profileView.findViewById(R.id.ivCpPartnerAvatar);
                    View layoutCpJunctionBadge = profileView.findViewById(R.id.layoutCpJunctionBadge);
                    TextView tvCpLevelBadgeText = profileView.findViewById(R.id.tvCpLevelBadgeText);

                    Runnable hideCpPartner = () -> {
                        if (flCpPartnerAvatarContainer != null) flCpPartnerAvatarContainer.setVisibility(View.GONE);
                        if (layoutCpJunctionBadge != null) layoutCpJunctionBadge.setVisibility(View.GONE);
                    };

                    DatabaseReference cpBindingRootRef = FirebaseDatabase.getInstance().getReference("cp_bindings");
                    cpBindingRootRef.child(targetUid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot cpSnap) {
                            if (isFinishing() || isDestroyed()) return;

                            String cpPartnerUid = null;
                            Long timestamp = null;

                            if (cpSnap.exists() && cpSnap.hasChild("partnerUid")) {
                                cpPartnerUid = cpSnap.child("partnerUid").getValue(String.class);
                                timestamp = cpSnap.child("timestamp").getValue(Long.class);
                            }

                            if (!TextUtils.isEmpty(cpPartnerUid)) {
                                bindCpPartnerInfo(targetUid, cpPartnerUid, timestamp, flCpPartnerAvatarContainer, layoutCpJunctionBadge, tvCpLevelBadgeText, ivCpPartnerAvatar);
                            } else {
                                // Search fallback if stored under partner key
                                cpBindingRootRef.orderByChild("partnerUid").equalTo(targetUid).limitToFirst(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot searchSnap) {
                                                if (isFinishing() || isDestroyed()) return;
                                                if (searchSnap.exists() && searchSnap.hasChildren()) {
                                                    for (DataSnapshot child : searchSnap.getChildren()) {
                                                        String foundPartnerUid = child.getKey();
                                                        Long ts = child.child("timestamp").getValue(Long.class);
                                                        if (!TextUtils.isEmpty(foundPartnerUid)) {
                                                            bindCpPartnerInfo(targetUid, foundPartnerUid, ts, flCpPartnerAvatarContainer, layoutCpJunctionBadge, tvCpLevelBadgeText, ivCpPartnerAvatar);
                                                            return;
                                                        }
                                                    }
                                                }
                                                hideCpPartner.run();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                hideCpPartner.run();
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            hideCpPartner.run();
                        }
                    });

                    // Level & XP Progress
                    TextView tvLevelBadge = profileView.findViewById(R.id.tvLevelBadge);
                    ProgressBar pbLevelXp = profileView.findViewById(R.id.pbLevelXp);
                    TextView tvLevelXpText = profileView.findViewById(R.id.tvLevelXpText);

                    long coinsSpent = 0;
                    if (snapshot.child("coinsSpent").exists()) {
                        try {
                            coinsSpent = Long.parseLong(String.valueOf(snapshot.child("coinsSpent").getValue()));
                        } catch (Exception ignored) {}
                    } else if (snapshot.child("level").exists()) {
                        try {
                            long lvl = Long.parseLong(String.valueOf(snapshot.child("level").getValue()));
                            coinsSpent = Math.max(0, (lvl - 1) * LevelUtils.COINS_PER_LEVEL);
                        } catch (Exception ignored) {}
                    }

                    long level = LevelUtils.calculateLevel(coinsSpent);
                    int xpInLevel = LevelUtils.calculateCurrentXpInLevel(coinsSpent);
                    long maxXpInLevel = LevelUtils.getXpNeededForNextLevelFromStart(level);
                    int xpProgressPct = LevelUtils.calculateXpPercentageInLevel(coinsSpent);

                    if (tvLevelBadge != null) tvLevelBadge.setText("Lv." + level);
                    if (pbLevelXp != null) pbLevelXp.setProgress(xpProgressPct);
                    if (tvLevelXpText != null) tvLevelXpText.setText(xpInLevel + " / " + maxXpInLevel + " XP (" + CoinUtils.formatCoins(coinsSpent) + " Coins)");
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Profile Like System
        DatabaseReference userLikesRef = FirebaseDatabase.getInstance().getReference("UserLikes").child(targetUid);
        DatabaseReference myLikeRef = userLikesRef.child(userID);

        userLikesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long likesCount = snapshot.getChildrenCount();
                if (tvLikesCount != null) tvLikesCount.setText(String.valueOf(likesCount));
                if (tvLikesBadge != null) tvLikesBadge.setText(likesCount + " Likes");
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        myLikeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isLiked = snapshot.exists();
                if (ivLikeHeart != null) {
                    if (isLiked) {
                        ivLikeHeart.setColorFilter(Color.parseColor("#FF007A"));
                    } else {
                        ivLikeHeart.setColorFilter(Color.parseColor("#A0FFFFFF"));
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        if (btnLikeUser != null) {
            btnLikeUser.setOnClickListener(v -> {
                myLikeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            myLikeRef.removeValue();
                            Toast.makeText(RoomChatActivity.this, "Unliked " + initialName, Toast.LENGTH_SHORT).show();
                        } else {
                            myLikeRef.setValue(true);
                            if (ivLikeHeart != null) AnimationHelper.bounceAnimation(ivLikeHeart);
                            Toast.makeText(RoomChatActivity.this, "You liked " + initialName + "! ❤️", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
            });
        }

        boolean isSelf = targetUid.equals(userID);

        // Own Profile vs Other User Action Rows (Hayi/Hayaa style)
        View llOwnUserActionsRow = profileView.findViewById(R.id.llOwnUserActionsRow);
        View llOtherUserActionsRow = profileView.findViewById(R.id.llOtherUserActionsRow);

        if (llOwnUserActionsRow != null) {
            llOwnUserActionsRow.setVisibility(isSelf ? View.VISIBLE : View.GONE);
        }
        if (llOtherUserActionsRow != null) {
            llOtherUserActionsRow.setVisibility(isSelf ? View.GONE : View.VISIBLE);
        }

        // Profile Collection Grid Below
        RecyclerView rvProfileCollection = profileView.findViewById(R.id.rvProfileCollection);
        TextView tvEmptyProfileCollection = profileView.findViewById(R.id.tvEmptyProfileCollection);
        TextView tvCollectionCountBadge = profileView.findViewById(R.id.tvCollectionCountBadge);
        if (rvProfileCollection != null) {
            rvProfileCollection.setLayoutManager(new GridLayoutManager(this, 3));
            rvProfileCollection.setNestedScrollingEnabled(false);
            loadProfileCardCollection(targetUid, rvProfileCollection, tvEmptyProfileCollection, tvCollectionCountBadge);
        }

        // Collection Card & Wear Button Click Logic
        // 1st click on card: expands inline collection grid inside the dialog card
        // 2nd click on card OR clicking "Wear ›": opens full CollectionActivity / OtherCollectionActivity
        Runnable openFullCollection = () -> {
            profileDialog.dismiss();
            if (isSelf) {
                Intent intent = new Intent(RoomChatActivity.this, CollectionActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(RoomChatActivity.this, OtherCollectionActivity.class);
                intent.putExtra("selectedUserId", targetUid);
                intent.putExtra("selectedUserName", initialName);
                startActivity(intent);
            }
        };

        View layoutCollectionHeader = profileView.findViewById(R.id.layoutCollectionHeader);
        if (layoutCollectionHeader != null) {
            layoutCollectionHeader.setOnClickListener(null);
            layoutCollectionHeader.setClickable(false);
            layoutCollectionHeader.setFocusable(false);
        }

        if (tvCollectionCountBadge != null) {
            tvCollectionCountBadge.setOnClickListener(v -> {
                v.setSelected(true);
                openFullCollection.run();
            });
        }

        // Own Profile Action Buttons (Leave the Mic, Send Gift)
        View btnOwnLeaveMic = profileView.findViewById(R.id.btnOwnLeaveMic);
        View btnOwnSendGift = profileView.findViewById(R.id.btnOwnSendGift);

        if (btnOwnLeaveMic != null) {
            btnOwnLeaveMic.setOnClickListener(v -> {
                if (model != null && model.index >= 0 && userID.equals(model.userID)) {
                    SeatManager.getInstance().leaveSeat(model.index);
                    ZegoManager.getInstance().stopPublishing();
                    ZegoManager.getInstance().sendInRoomTextMessage(userName + " left the seat.");
                    Toast.makeText(RoomChatActivity.this, "You left the seat 🎙️", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RoomChatActivity.this, "You left the mic 🎙️", Toast.LENGTH_SHORT).show();
                }
                profileDialog.dismiss();
            });
        }

        if (btnOwnSendGift != null) {
            btnOwnSendGift.setOnClickListener(v -> {
                profileDialog.dismiss();
                showGiftDialog(null); // Open general gift store (cannot gift self)
            });
        }

        // 1. Follow Action Button (Other users)
        if (btnActionFollow != null) {
            DatabaseReference followRef = FirebaseDatabase.getInstance().getReference("Follow")
                    .child(userID).child("following").child(targetUid);

            followRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        if (tvActionFollowText != null) tvActionFollowText.setText("Following");
                    } else {
                        if (tvActionFollowText != null) tvActionFollowText.setText("Follow");
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });

            btnActionFollow.setOnClickListener(v -> {
                DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference("Follow")
                        .child(userID).child("following").child(targetUid);
                DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference("Follow")
                        .child(targetUid).child("followers").child(userID);

                followingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            followingRef.removeValue();
                            followersRef.removeValue();
                            Toast.makeText(RoomChatActivity.this, "Unfollowed " + initialName, Toast.LENGTH_SHORT).show();
                        } else {
                            followingRef.setValue(true);
                            followersRef.setValue(true);
                            NotificationHelper.sendFollowNotification(targetUid);
                            Toast.makeText(RoomChatActivity.this, "Now following " + initialName + " ❤️", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
            });
        }

        // 2. Add Friend Button (Handles Pending Request State)
        DatabaseReference reqRef = FirebaseDatabase.getInstance().getReference("FriendRequests")
                .child(targetUid).child(userID);

        reqRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (ivActionAddIcon != null) {
                        ivActionAddIcon.setImageResource(R.drawable.ic_check);
                        ivActionAddIcon.setColorFilter(Color.parseColor("#FFB703"));
                    }
                    if (tvActionAddText != null) {
                        tvActionAddText.setText("Requested");
                        tvActionAddText.setTextColor(Color.parseColor("#FFB703"));
                    }
                } else {
                    if (ivActionAddIcon != null) {
                        ivActionAddIcon.setImageResource(R.drawable.ic_person);
                        ivActionAddIcon.setColorFilter(Color.parseColor("#00FFC6"));
                    }
                    if (tvActionAddText != null) {
                        tvActionAddText.setText("Add");
                        tvActionAddText.setTextColor(Color.WHITE);
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        if (btnActionAdd != null) {
            btnActionAdd.setOnClickListener(v -> {
                reqRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            reqRef.removeValue();
                            Toast.makeText(RoomChatActivity.this, "Friend request cancelled", Toast.LENGTH_SHORT).show();
                        } else {
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("senderId", userID);
                            map.put("senderName", userName != null ? userName : "User");
                            map.put("senderAvatar", SessionManager.getInstance(RoomChatActivity.this).getAvatar());
                            map.put("timestamp", System.currentTimeMillis());
                            map.put("status", "pending");
                            reqRef.setValue(map);
                            Toast.makeText(RoomChatActivity.this, "Friend request sent to " + initialName + "! 🤝", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
            });
        }


        // 3. Send Gift Button
        if (btnActionSend != null) {
            btnActionSend.setOnClickListener(v -> {
                profileDialog.dismiss();
                showGiftDialog(targetUid);
            });
        }

        // OTHER COLLECTION NAVIGATION
        View btnActionCollection = profileView.findViewById(R.id.btnActionCollection);
        if (btnActionCollection != null) {
            btnActionCollection.setOnClickListener(v -> {
                Intent intent = new Intent(RoomChatActivity.this, OtherCollectionActivity.class);
                intent.putExtra("selectedUserId", targetUid);
                intent.putExtra("selectedUserName", initialName);
                startActivity(intent);
                profileDialog.dismiss();
            });
        }

        // Host Seat Controls Row
        View vHostDivider = profileView.findViewById(R.id.vHostDivider);
        View llHostSeatControlsRow = profileView.findViewById(R.id.llHostSeatControlsRow);

        View btnHostMic = profileView.findViewById(R.id.btnHostMic);
        ImageView ivHostMicIcon = profileView.findViewById(R.id.ivHostMicIcon);

        View btnHostMute = profileView.findViewById(R.id.btnHostMute);
        ImageView ivHostMuteIcon = profileView.findViewById(R.id.ivHostMuteIcon);

        View btnHostLock = profileView.findViewById(R.id.btnHostLock);
        ImageView ivHostLockIcon = profileView.findViewById(R.id.ivHostLockIcon);

        View btnHostLeaveSeat = profileView.findViewById(R.id.btnHostLeaveSeat);

        View btnActionKick = profileView.findViewById(R.id.btnActionKick);

        // Host seat controls are shown ONLY when viewing another user's profile and viewer is Admin/Host
        boolean showHostControls = !isSelf && isAdminOrHost();
        if (llHostSeatControlsRow != null) {
            llHostSeatControlsRow.setVisibility(showHostControls ? View.VISIBLE : View.GONE);
        }
        if (vHostDivider != null) {
            vHostDivider.setVisibility(showHostControls ? View.VISIBLE : View.GONE);
        }

        // Lock Seat and Kick User buttons are strictly reserved for Host and Administrators (Like Hayaa/Hayi)
        if (btnHostLock != null) {
            btnHostLock.setVisibility(showHostControls ? View.VISIBLE : View.GONE);
        }
        if (btnActionKick != null) {
            btnActionKick.setVisibility(showHostControls ? View.VISIBLE : View.GONE);
        }

        if (showHostControls) {
            // Initial states
            if (ivHostMicIcon != null) {
                ivHostMicIcon.setImageResource(model.isMicOn ? R.drawable.ic_mic_on : R.drawable.ic_mic_off);
            }
            if (ivHostMuteIcon != null) {
                ivHostMuteIcon.setImageResource(model.isMuted ? R.drawable.ic_speaker_off : R.drawable.ic_speaker_on);
            }
            if (ivHostLockIcon != null) {
                ivHostLockIcon.setImageResource(model.isClosed ? R.drawable.ic_lock : R.drawable.ic_lock_open);
            }

            if (btnHostMic != null) {
                btnHostMic.setOnClickListener(v -> {
                    boolean newState = !model.isMicOn;
                    model.isMicOn = newState;
                    SeatManager.getInstance().updateMicStatus(model.index, newState);
                    if (ivHostMicIcon != null) {
                        ivHostMicIcon.setImageResource(newState ? R.drawable.ic_mic_on : R.drawable.ic_mic_off);
                    }
                    Toast.makeText(RoomChatActivity.this, initialName + " Mic " + (newState ? "Unmuted 🎙️" : "Muted 🔇"), Toast.LENGTH_SHORT).show();
                });
            }

            if (btnHostMute != null) {
                btnHostMute.setOnClickListener(v -> {
                    boolean newMuteState = !model.isMuted;
                    model.isMuted = newMuteState;
                    SeatManager.getInstance().muteSeat(model.index, newMuteState);
                    if (ivHostMuteIcon != null) {
                        ivHostMuteIcon.setImageResource(newMuteState ? R.drawable.ic_speaker_off : R.drawable.ic_speaker_on);
                    }
                    Toast.makeText(RoomChatActivity.this, (newMuteState ? "Muted " : "Unmuted ") + initialName, Toast.LENGTH_SHORT).show();
                });
            }

            if (btnHostLock != null) {
                btnHostLock.setOnClickListener(v -> {
                    if (!isAdminOrHost()) {
                        Toast.makeText(RoomChatActivity.this, "Only Room Host or Administrators can lock or unlock seats 🔒", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    boolean newClosedState = !model.isClosed;
                    model.isClosed = newClosedState;
                    SeatManager.getInstance().closeSeat(model.index, newClosedState);
                    if (ivHostLockIcon != null) {
                        ivHostLockIcon.setImageResource(newClosedState ? R.drawable.ic_lock : R.drawable.ic_lock_open);
                    }
                    String targetSeatName = model.index == 0 ? "Host Seat" : ("Seat " + model.index);
                    Toast.makeText(RoomChatActivity.this, (newClosedState ? "Locked " : "Unlocked ") + targetSeatName, Toast.LENGTH_SHORT).show();
                });
            }

            if (btnHostLeaveSeat != null) {
                btnHostLeaveSeat.setOnClickListener(v -> {
                    SeatManager.getInstance().leaveSeat(model.index);
                    String notice = "Host removed " + initialName + " from seat.";
                    ZegoManager.getInstance().sendInRoomTextMessage(notice);
                    Toast.makeText(RoomChatActivity.this, "Removed " + initialName + " from seat", Toast.LENGTH_SHORT).show();
                    profileDialog.dismiss();
                });
            }

            if (btnActionKick != null) {
                btnActionKick.setOnClickListener(v -> {
                    if (!isAdminOrHost()) {
                        Toast.makeText(RoomChatActivity.this, "Only Room Host or Administrators can kick/blacklist users 🚫", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (targetUid.equals(userID)) {
                        Toast.makeText(RoomChatActivity.this, "You cannot kick yourself!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String hostUid = SeatManager.getInstance().getHostUserID();
                    if (targetUid.equals(hostUid)) {
                        Toast.makeText(RoomChatActivity.this, "Cannot kick the Room Host!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    profileDialog.dismiss();
                    showKickDurationDialog(targetUid, initialName, model != null ? model.index : -1);
                });
            }
        }

        // Open Full User Profile Detail Activity (CollectionActivity / OtherCollectionActivity)
        View.OnClickListener openFullProfile = v -> {
            profileDialog.dismiss();
            if (userID != null && userID.equals(targetUid)) {
                Intent intent = new Intent(RoomChatActivity.this, CollectionActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(RoomChatActivity.this, OtherCollectionActivity.class);
                intent.putExtra("selectedUserId", targetUid);
                intent.putExtra("uid", targetUid);
                intent.putExtra("selectedUserName", initialName);
                startActivity(intent);
            }
        };

        if (btnMoreOptions != null) btnMoreOptions.setOnClickListener(openFullProfile);
        if (ivProfileAvatar != null) ivProfileAvatar.setOnClickListener(openFullProfile);
        if (tvName != null) tvName.setOnClickListener(openFullProfile);
        if (tvProfileId != null) tvProfileId.setOnClickListener(openFullProfile);

        // Open Followers List
        if (tvFollowersCount != null) {
            tvFollowersCount.setOnClickListener(v -> {
                profileDialog.dismiss();
                Intent intent = new Intent(RoomChatActivity.this, FollowListActivity.class);
                intent.putExtra("uid", targetUid);
                intent.putExtra("type", "followers");
                startActivity(intent);
            });
        }

        profileDialog.show();
    }

    private void loadProfileCardCollection(String targetUid, RecyclerView rvCollection, TextView tvEmpty, TextView tvCountBadge) {
        if (targetUid == null || targetUid.isEmpty() || rvCollection == null) return;

        StoreManager.getStoreCatalog(targetUid, "ALL", new StoreManager.CatalogCallback() {
            @Override
            public void onCatalogLoaded(List<StoreItemModel> catalog) {
                if (isFinishing() || isDestroyed()) return;
                List<CollectionItemModel> storeOwnedItems = new ArrayList<>();

                for (StoreItemModel item : catalog) {
                    if (item.isOwned()) {
                        if ("ENTRANCE".equalsIgnoreCase(item.getCategory())) {
                            storeOwnedItems.add(new CollectionItemModel(CollectionItemModel.ItemType.ENTRY_EFFECT, item));
                        } else if ("FRAME".equalsIgnoreCase(item.getCategory())) {
                            storeOwnedItems.add(new CollectionItemModel(CollectionItemModel.ItemType.FRAME, item));
                        }
                    }
                }

                loadProfileCardReceivedGifts(targetUid, storeOwnedItems, rvCollection, tvEmpty, tvCountBadge);
            }

            @Override
            public void onError(String error) {
                if (isFinishing() || isDestroyed()) return;
                loadProfileCardReceivedGifts(targetUid, new ArrayList<>(), rvCollection, tvEmpty, tvCountBadge);
            }
        });
    }

    private void loadProfileCardReceivedGifts(String targetUid, List<CollectionItemModel> storeOwnedItems, RecyclerView rvCollection, TextView tvEmpty, TextView tvCountBadge) {
        DatabaseReference txRef = FirebaseDatabase.getInstance().getReference("wallet_transactions").child(targetUid);
        DatabaseReference userGiftsRef = FirebaseDatabase.getInstance().getReference("users").child(targetUid).child("received_gifts");

        txRef.limitToLast(200).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;
                Map<String, Integer> giftCountMap = new HashMap<>();

                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        String type = ds.child("type").getValue(String.class);
                        if ("GIFT_RECEIVED".equalsIgnoreCase(type)) {
                            String giftName = ds.child("giftName").getValue(String.class);
                            if (giftName == null || giftName.trim().isEmpty()) {
                                String title = ds.child("title").getValue(String.class);
                                if (title != null && title.startsWith("Received Gift: ")) {
                                    giftName = title.substring("Received Gift: ".length()).trim();
                                } else if (title != null && title.startsWith("Earned Energy: ")) {
                                    giftName = title.substring("Earned Energy: ".length()).trim();
                                }
                            }
                            if (giftName == null || giftName.trim().isEmpty()) {
                                String desc = ds.child("description").getValue(String.class);
                                if (desc != null && desc.contains("Received ") && desc.contains(" from ")) {
                                    int start = desc.indexOf("Received ") + "Received ".length();
                                    int end = desc.indexOf(" from ");
                                    if (start < end) {
                                        giftName = desc.substring(start, end).trim();
                                    }
                                }
                            }
                            if (giftName != null && !giftName.trim().isEmpty()) {
                                String cleanName = giftName.replaceAll("\\s*\\(.*?\\)", "").trim();
                                int qty = 1;
                                Long qObj = ds.child("quantity").getValue(Long.class);
                                if (qObj != null && qObj > 0) qty = qObj.intValue();

                                int currentCount = giftCountMap.containsKey(cleanName) ? giftCountMap.get(cleanName) : 0;
                                giftCountMap.put(cleanName, currentCount + qty);
                            }
                        }
                    }
                }

                userGiftsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot giftsSnapshot) {
                        if (isFinishing() || isDestroyed()) return;
                        if (giftsSnapshot.exists()) {
                            for (DataSnapshot ds : giftsSnapshot.getChildren()) {
                                String gKey = ds.getKey();
                                Long countVal = ds.getValue(Long.class);
                                if (gKey != null && countVal != null && countVal > 0) {
                                    String cleanKey = gKey.replaceAll("\\s*\\(.*?\\)", "").trim();
                                    int existing = giftCountMap.containsKey(cleanKey) ? giftCountMap.get(cleanKey) : 0;
                                    giftCountMap.put(cleanKey, Math.max(existing, countVal.intValue()));
                                }
                            }
                        }

                        buildAndBindProfileCollection(storeOwnedItems, giftCountMap, rvCollection, tvEmpty, tvCountBadge);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        buildAndBindProfileCollection(storeOwnedItems, giftCountMap, rvCollection, tvEmpty, tvCountBadge);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                buildAndBindProfileCollection(storeOwnedItems, new HashMap<>(), rvCollection, tvEmpty, tvCountBadge);
            }
        });
    }

    private void buildAndBindProfileCollection(List<CollectionItemModel> storeOwnedItems, Map<String, Integer> giftCountMap, RecyclerView rvCollection, TextView tvEmpty, TextView tvCountBadge) {
        if (isFinishing() || isDestroyed()) return;
        List<CollectionItemModel> finalCollection = new ArrayList<>(storeOwnedItems);

        for (Map.Entry<String, Integer> entry : giftCountMap.entrySet()) {
            String giftName = entry.getKey();
            int count = entry.getValue();
            GiftStoreItem catalogGift = GiftCatalog.findGiftByName(giftName);
            if (catalogGift != null) {
                finalCollection.add(new CollectionItemModel(catalogGift, count));
            } else {
                finalCollection.add(new CollectionItemModel(giftName, R.drawable.gift_icon, 100000L, count));
            }
        }

        if (tvCountBadge != null) {
            tvCountBadge.setText("Wear ›");
        }

        if (rvCollection != null) {
            rvCollection.setVisibility(View.GONE);
            if (!finalCollection.isEmpty()) {
                CollectionAdapter adapter = new CollectionAdapter(finalCollection, item -> {
                    Toast.makeText(RoomChatActivity.this, "Collection Item: " + (item.getGiftStoreItem() != null ? item.getGiftStoreItem().name : (item.getStoreItem() != null ? item.getStoreItem().getName() : "Item")), Toast.LENGTH_SHORT).show();
                });
                rvCollection.setAdapter(adapter);
            }
        }
        if (tvEmpty != null) {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    // Prompt Admin/Host to select kick/blacklist duration
    private void showKickDurationDialog(String targetUid, String targetName, int seatIndex) {
        if (!isAdminOrHost()) return;

        BottomSheetDialog durationDialog = new BottomSheetDialog(this);
        applyGlassyStyle(durationDialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_kick_duration, null, false);
        if (dialogView == null) return;

        TextView tvTitle = dialogView.findViewById(R.id.tvKickDurationTitle);
        if (tvTitle != null && targetName != null) {
            tvTitle.setText("Blacklist " + targetName + " 🚫");
        }

        View btn2Hr = dialogView.findViewById(R.id.btnDuration2Hr);
        View btn12Hr = dialogView.findViewById(R.id.btnDuration12Hr);
        View btn24Hr = dialogView.findViewById(R.id.btnDuration24Hr);
        View btnPermanent = dialogView.findViewById(R.id.btnDurationPermanent);

        View.OnClickListener clickListener = v -> {
            long now = System.currentTimeMillis();
            long expiryTs = 0L;
            int id = v.getId();
            if (id == R.id.btnDuration2Hr) {
                expiryTs = now + (2L * 3600 * 1000);
            } else if (id == R.id.btnDuration12Hr) {
                expiryTs = now + (12L * 3600 * 1000);
            } else if (id == R.id.btnDuration24Hr) {
                expiryTs = now + (24L * 3600 * 1000);
            } else if (id == R.id.btnDurationPermanent) {
                expiryTs = 0L; // Permanent
            }
            durationDialog.dismiss();
            executeKickUser(targetUid, targetName, seatIndex, expiryTs);
        };

        if (btn2Hr != null) btn2Hr.setOnClickListener(clickListener);
        if (btn12Hr != null) btn12Hr.setOnClickListener(clickListener);
        if (btn24Hr != null) btn24Hr.setOnClickListener(clickListener);
        if (btnPermanent != null) btnPermanent.setOnClickListener(clickListener);

        durationDialog.setContentView(dialogView);
        durationDialog.show();
    }

    private void executeKickUser(String targetUid, String targetName, int seatIndex, long expiryTimestamp) {
        if (seatIndex >= 0) {
            SeatManager.getInstance().leaveSeat(seatIndex);
        }

        DatabaseReference kickedRef = FirebaseDatabase.getInstance().getReference("rooms")
                .child(roomID).child("kicked_users").child(targetUid);

        Map<String, Object> kickData = new HashMap<>();
        kickData.put("timestamp", System.currentTimeMillis());
        kickData.put("kickedBy", userID);
        kickData.put("userName", targetName != null ? targetName : "User");
        kickData.put("expiryTimestamp", expiryTimestamp);

        kickedRef.setValue(kickData).addOnSuccessListener(aVoid -> {
            String durationLabel = expiryTimestamp == 0L ? "permanently" : "temporarily";
            String notice = "Admin blacklisted " + targetName + " from room (" + durationLabel + ").";
            ZegoManager.getInstance().sendInRoomTextMessage(notice);
            Toast.makeText(RoomChatActivity.this, "Blacklisted " + targetName + " from room 🚫", Toast.LENGTH_SHORT).show();
        });
    }

    private void leaveRoom() {
        try {
            isRoomLeft = true;
            isMinimized = false; // ROOM MINIMIZE FIX
            RoomFloatingManager.getInstance().clearRoomState(); // ROOM MINIMIZE FIX
            int mySeatIndex = SeatManager.getInstance().findUserSeatIndex(userID);
            if (mySeatIndex != -1) {
                SeatManager.getInstance().leaveSeat(mySeatIndex);
            }
            if (isHost) {
                SeatManager.getInstance().leaveSeat(0);
            }
            if (roomInfoRef != null && roomInfoValueEventListener != null) {
                roomInfoRef.removeEventListener(roomInfoValueEventListener);
            }
            if (roomSeatsRef != null && seatsValueEventListener != null) {
                roomSeatsRef.removeEventListener(seatsValueEventListener);
            }
            if (roomRolesRef != null && roomRolesValueListener != null) { // ROOM ROLE PERSISTENCE
                roomRolesRef.removeEventListener(roomRolesValueListener); // ROOM ROLE PERSISTENCE
            }
            if (roomMessagesRef != null && roomMessagesChildEventListener != null) {
                roomMessagesRef.removeEventListener(roomMessagesChildEventListener);
            }
            if (roomGiftsRef != null && giftsChildEventListener != null) { // GIFT SVGA FIX: remove orphaned Firebase gift listener
                roomGiftsRef.removeEventListener(giftsChildEventListener); // GIFT SVGA FIX
            } // GIFT SVGA FIX
            if (globalRoomGiftsRef != null && globalGiftsChildEventListener != null) {
                globalRoomGiftsRef.removeEventListener(globalGiftsChildEventListener);
            }
            if (roomMusicRef != null && roomMusicValueEventListener != null) {
                roomMusicRef.removeEventListener(roomMusicValueEventListener);
            }
            if (roomThemeRef != null && roomThemeValueEventListener != null) {
                roomThemeRef.removeEventListener(roomThemeValueEventListener);
            }
            if (roomEntriesRef != null && roomEntriesChildEventListener != null) {
                roomEntriesRef.removeEventListener(roomEntriesChildEventListener);
            }
            if (roomEmojiRef != null && roomEmojiEventListener != null) {
                roomEmojiRef.removeEventListener(roomEmojiEventListener);
            }
            if (roomOnlineUsersListRef != null && roomOnlineUsersValueListener != null) {
                roomOnlineUsersListRef.removeEventListener(roomOnlineUsersValueListener);
            }
            if (myKickRef != null && myKickListener != null) {
                myKickRef.removeEventListener(myKickListener);
            }
            unregisterOnlineUser();
            if (mySeatInviteRef != null && mySeatInviteListener != null) {
                mySeatInviteRef.removeEventListener(mySeatInviteListener);
            }
            ZegoManager.getInstance().stopMusic();
            ZegoManager.getInstance().logoutRoom();
            ZegoManager.getInstance().removeListener(zegoListener);
            SeatManager.getInstance().removeListener(seatListener);
            if (!isFinishing() && !isDestroyed()) {
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.slide_out_bottom); // ROOM ANIMATION FIX
            }
        } catch (Exception e) {
            Log.e("RoomChatActivity", "Error leaving room", e);
        }
    }

    private DatabaseReference myKickRef;
    private ValueEventListener myKickListener;

    private void listenForRoomKick() {
        if (userID == null || roomID == null) return;
        myKickRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomID).child("kicked_users").child(userID);
        myKickListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && !isHost && !isFinishing() && !isDestroyed()) {
                    Long expiryTs = snapshot.child("expiryTimestamp").getValue(Long.class);
                    if (expiryTs != null && expiryTs > 0 && System.currentTimeMillis() > expiryTs) {
                        // Ban expired! Clean up Firebase entry
                        myKickRef.removeValue();
                        return;
                    }
                    Toast.makeText(RoomChatActivity.this, "🚫 You were blacklisted from this room by Admin!", Toast.LENGTH_LONG).show();
                    if (myKickRef != null && myKickListener != null) {
                        myKickRef.removeEventListener(myKickListener);
                    }
                    leaveRoom();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        myKickRef.addValueEventListener(myKickListener);
    }

    private void checkInitialRoomKick() {
        if (isHost || userID == null || roomID == null) return;
        DatabaseReference checkKickRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomID).child("kicked_users").child(userID);
        checkKickRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && !isFinishing() && !isDestroyed()) {
                    Long expiryTs = snapshot.child("expiryTimestamp").getValue(Long.class);
                    if (expiryTs != null && expiryTs > 0 && System.currentTimeMillis() > expiryTs) {
                        // Ban expired! Clean up Firebase entry
                        checkKickRef.removeValue();
                        return;
                    }
                    Toast.makeText(RoomChatActivity.this, "🚫 You are blacklisted from this room by Admin!", Toast.LENGTH_LONG).show();
                    leaveRoom();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private DatabaseReference roomOnlineUsersListRef;
    private ValueEventListener roomOnlineUsersValueListener;

    private void setupRoomOnlineUsersListener() {
        if (roomID == null || roomID.trim().isEmpty()) return;
        roomOnlineUsersListRef = FirebaseDatabase.getInstance().getReference("room_users").child(roomID);
        roomOnlineUsersValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;
                long count = snapshot.getChildrenCount();
                int displayCount = (int) Math.max(count, 1);
                if (backgroundView != null) {
                    backgroundView.setUserCount(displayCount);
                }
                if (tvMemberCount != null) {
                    tvMemberCount.setText(String.valueOf(displayCount));
                }
                if (roomInfoRef != null) {
                    roomInfoRef.child("onlineCount").setValue(count);
                    roomInfoRef.child("memberCount").setValue(count);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        roomOnlineUsersListRef.addValueEventListener(roomOnlineUsersValueListener);
    }

    private void setupFirebaseListeners() {
        setupRoomInfoListener();
        setupRoomGiftListener();
        setupGlobalRoomGiftListener();
        setupRoomSeatsListener();
        setupRoomRolesListener(); // ROOM ROLE PERSISTENCE
        setupRoomMessagesListener();
        setupRoomMusicListener();
        setupRoomThemeListener();
        setupRoomEntriesListener();
        setupRoomEmojiListener();
        setupRoomOnlineUsersListener();
        listenForRoomKick();
        checkInitialRoomKick();
        registerOnlineUser();
        setupSeatInviteListener();
    }

    // ROOM ROLE PERSISTENCE
    private void setupRoomRolesListener() {
        if (roomID == null || roomID.trim().isEmpty()) return;
        if (roomRolesRef != null && roomRolesValueListener != null) {
            roomRolesRef.removeEventListener(roomRolesValueListener);
        }

        roomRolesRef = FirebaseDatabase.getInstance().getReference("room_roles").child(roomID);
        roomRolesValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;
                userRoomRolesMap.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String targetUid = child.getKey();
                        String roleVal = child.child("role").getValue(String.class);
                        if (targetUid != null && roleVal != null) {
                            userRoomRolesMap.put(targetUid, roleVal.toLowerCase());
                            SeatManager.getInstance().updateUserRole(targetUid, roleVal.toLowerCase());
                        }
                    }
                }

                if (isHost) {
                    currentRoomRole = "host";
                } else if (userID != null && userRoomRolesMap.containsKey(userID)) {
                    currentRoomRole = userRoomRolesMap.get(userID);
                } else {
                    currentRoomRole = "member";
                }

                updateAdminUIVisibility();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RoomChatActivity", "Error listening to room roles", error.toException());
            }
        };
        roomRolesRef.addValueEventListener(roomRolesValueListener);
    }

    // ADMIN PERMISSION FIX
    private void updateAdminUIVisibility() {
        View btnMore = findViewById(R.id.btnMore);
        View moreView = findViewById(R.id.more);
        boolean hasAdminAccess = isAdminOrHost();

        if (btnMore != null) {
            btnMore.setVisibility(View.GONE);
        }
        if (moreView != null) {
            moreView.setVisibility(hasAdminAccess ? View.VISIBLE : View.GONE);
        }
        View layoutRoomMusic = findViewById(R.id.layoutRoomMusic);
        if (layoutRoomMusic != null) {
            layoutRoomMusic.setVisibility(hasAdminAccess ? View.VISIBLE : View.GONE);
        }
        View btnRoomAgencyIcon = findViewById(R.id.btnRoomAgencyIcon);
        if (btnRoomAgencyIcon != null) {
            boolean isOnSeat = SeatManager.getInstance().findUserSeatIndex(userID) != -1;
            btnRoomAgencyIcon.setVisibility((hasAdminAccess || isHost || isOnSeat) ? View.VISIBLE : View.GONE);
        }
    }

    private void setupRoomInfoListener() {
        roomInfoRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomID);
        roomInfoValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;
                if (snapshot.exists()) {
                    String newName = snapshot.child("room_name").getValue(String.class);
                    String newImg = snapshot.child("img").getValue(String.class);

                    if (newName != null && !newName.trim().isEmpty()) {
                        roomNameLabel = newName;
                        TextView tvRoomName = findViewById(R.id.tvRoomName);
                        if (tvRoomName != null) {
                            tvRoomName.setText(newName);
                        }
                        if (backgroundView != null) {
                            backgroundView.setRoomName(newName);
                        }
                    }

                    if (newImg != null && !newImg.trim().isEmpty()) {
                        roomImg = newImg;
                        ImageView ivRoomAvatar = findViewById(R.id.ivRoomAvatar);
                        if (ivRoomAvatar != null) {
                            Glide.with(RoomChatActivity.this)
                                    .load(newImg)
                                    .placeholder(R.drawable.logo_placeholder)
                                    .error(R.drawable.logo_placeholder)
                                    .into(ivRoomAvatar);
                        }
                    }

                    // Welcome messages sync
                    String wMsg = snapshot.child("welcomeMessage").getValue(String.class);
                    String newWMsg = snapshot.child("newUserWelcomeMessage").getValue(String.class);
                    if (wMsg != null) currentRoomWelcomeMessage = wMsg;
                    if (newWMsg != null) currentNewUserWelcomeMessage = newWMsg;

                    // SEAT LIMIT SYNC FIX
                    DataSnapshot seatCountSnap = snapshot.child("seatCount");
                    if (seatCountSnap.exists()) {
                        Integer seatCountVal = seatCountSnap.getValue(Integer.class);
                        if (seatCountVal != null && (seatCountVal == 9 || seatCountVal == 17 || seatCountVal == 21)) {
                            if (SeatManager.getInstance().getTotalSeats() != seatCountVal) {
                                SeatManager.getInstance().setTotalSeats(seatCountVal);
                            }
                        }
                    }

                    // Real-Time Clear Room Chat Screen Sync
                    Long clearTs = snapshot.child("clearScreenTimestamp").getValue(Long.class);
                    if (clearTs != null && clearTs > lastClearedTimestamp) {
                        lastClearedTimestamp = clearTs;
                        if (chatAdapter != null) {
                            chatAdapter.clearAllMessages();
                        }
                    }

                    // ROOM MEMBER COUNT SYNC
                    Long onlineCount = snapshot.child("onlineCount").getValue(Long.class);
                    if (onlineCount == null) {
                        onlineCount = snapshot.child("memberCount").getValue(Long.class);
                    }
                    if (onlineCount != null && tvMemberCount != null) {
                        tvMemberCount.setText(String.valueOf(Math.max(onlineCount, 1)));
                    }

                    // ROOM COIN SYNC: Update tvTrophyCount with current room's cumulative spending
                    Long totalSpend = snapshot.child("totalCoinSpend").getValue(Long.class);
                    if (totalSpend == null) {
                        totalSpend = 0L;
                    }
                    totalSpend = Math.max(0L, totalSpend);
                    if (tvTrophyCount != null) {
                        tvTrophyCount.setText(CoinUtils.formatCompactCoins(totalSpend));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        roomInfoRef.addValueEventListener(roomInfoValueEventListener);
    }

    private void setupRoomGiftListener() {
        roomGiftsRef = FirebaseDatabase.getInstance().getReference("room_gifts").child(roomID);
        giftsChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (isFinishing() || isDestroyed()) return;
                if (snapshot.exists()) {
                    String senderName = snapshot.child("senderName").getValue(String.class);
                    String giftName = snapshot.child("giftName").getValue(String.class);
                    Long iconResLong = snapshot.child("iconRes").getValue(Long.class);
                    Long ts = snapshot.child("timestamp").getValue(Long.class);

                    if (ts != null && ts < roomJoinTime - 3000) return;

                    if (senderName != null && giftName != null) {
                        String recipientName = snapshot.child("recipientName").getValue(String.class);
                        Long quantityLong = snapshot.child("quantity").getValue(Long.class);
                        int quantity = (quantityLong != null) ? quantityLong.intValue() : 1;

                        String giftId = snapshot.child("giftId").getValue(String.class);
                        if (giftId == null || giftId.isEmpty()) {
                            giftId = senderName + "_" + giftName + "_" + (ts != null ? ts : 0);
                        }
                        if (markGiftProcessed(giftId)) return;

                        String senderAvatar = snapshot.child("senderAvatar").getValue(String.class);
                        int iconRes = (iconResLong != null) ? iconResLong.intValue() : R.drawable.gift_icon;

                        String giftSvga = snapshot.child("giftSvga").getValue(String.class);
                        // Enqueue unified gift event for all room members (animation + banner synchronized together!)
                        enqueueGiftEvent(giftId, senderName, giftName, recipientName, quantity, giftSvga);
                    }
                }
            }
            @Override public void onChildChanged(@NonNull DataSnapshot s, @Nullable String p) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot s) {}
            @Override public void onChildMoved(@NonNull DataSnapshot s, @Nullable String p) {}
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        };
        roomGiftsRef.addChildEventListener(giftsChildEventListener);
    }

    private void setupGlobalRoomGiftListener() {
        globalRoomGiftsRef = FirebaseDatabase.getInstance().getReference("global_room_gifts");
        globalGiftsChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (isFinishing() || isDestroyed() || !snapshot.exists()) return;

                Long ts = snapshot.child("timestamp").getValue(Long.class);
                if (ts != null && ts < roomJoinTime - 3000) return;

                String giftId = snapshot.child("giftId").getValue(String.class);
                String senderName = snapshot.child("senderName").getValue(String.class);
                String giftName = snapshot.child("giftName").getValue(String.class);
                String recipientName = snapshot.child("recipientName").getValue(String.class);
                Long quantityLong = snapshot.child("quantity").getValue(Long.class);
                int quantity = (quantityLong != null) ? quantityLong.intValue() : 1;

                if (senderName == null || giftName == null) return;

                if (giftId == null || giftId.isEmpty()) {
                    giftId = senderName + "_" + giftName + "_" + (ts != null ? ts : 0);
                }

                showGoldenGiftBanner(giftId, senderName, giftName, recipientName, quantity);
            }

            @Override public void onChildChanged(@NonNull DataSnapshot s, @Nullable String p) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot s) {}
            @Override public void onChildMoved(@NonNull DataSnapshot s, @Nullable String p) {}
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        };
        globalRoomGiftsRef.addChildEventListener(globalGiftsChildEventListener);
    }

    private void setupRoomSeatsListener() {
        roomSeatsRef = FirebaseDatabase.getInstance().getReference("room_seats").child(roomID);
        seatsValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;
                if (snapshot.exists()) {
                    List<SeatModel> updatedSeats = new ArrayList<>();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        SeatModel seat = child.getValue(SeatModel.class);
                        if (seat != null) {
                            String hostUid = SeatManager.getInstance().getHostUserID();
                            if (seat.index == 0 && hostUid != null && !hostUid.isEmpty() && !seat.userID.equals(hostUid)) {
                                seat.clear();
                            }
                            updatedSeats.add(seat);
                        }
                    }
                    if (!updatedSeats.isEmpty()) {
                        SeatManager.getInstance().setSeatsFromExternal(updatedSeats);
                    }
                    // Ensure Host is placed on Seat 0 only when entering the room for the first time (if not on any seat yet)
                    if (isHost && userID != null && !userID.isEmpty()) {
                        int hostIdx = SeatManager.getInstance().findUserSeatIndex(userID);
                        if (hostIdx == -1) {
                            String myAvatar = SessionManager.getInstance(RoomChatActivity.this).getAvatar();
                            UserProfileCache.UserProfile cached = UserProfileCache.getDirectCachedProfile(userID);
                            String frame = (cached != null && cached.equippedFrame != null) ? cached.equippedFrame : "";
                            SeatManager.getInstance().takeSeat(0, userID, userName, myAvatar, frame, true);
                        }
                    }
                } else {
                    // First time room initialization on Firebase by Host
                    SeatManager.getInstance().setFirebaseSynced(true);
                    if (isHost && userID != null && !userID.isEmpty()) {
                        String myAvatar = SessionManager.getInstance(RoomChatActivity.this).getAvatar();
                        UserProfileCache.UserProfile cached = UserProfileCache.getDirectCachedProfile(userID);
                        String frame = (cached != null && cached.equippedFrame != null) ? cached.equippedFrame : "";
                        SeatManager.getInstance().takeSeat(0, userID, userName, myAvatar, frame, true);
                    } else {
                        SeatManager.getInstance().syncSeatsToFirebase();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        roomSeatsRef.addValueEventListener(seatsValueEventListener);
    }

    private void setupRoomMessagesListener() {
        roomMessagesRef = FirebaseDatabase.getInstance().getReference("room_messages").child(roomID);
        roomMessagesChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (isFinishing() || isDestroyed()) return;
                if (snapshot.exists()) {
                    String senderId = snapshot.child("senderId").getValue(String.class);
                    String senderName = snapshot.child("senderName").getValue(String.class);
                    String senderAvatar = snapshot.child("senderAvatar").getValue(String.class);
                    if (senderAvatar == null || senderAvatar.trim().isEmpty()) {
                        senderAvatar = snapshot.child("avtar").getValue(String.class);
                    }
                    String text = snapshot.child("text").getValue(String.class);
                    Long giftIconLong = snapshot.child("giftIconRes").getValue(Long.class);
                    int giftIconRes = (giftIconLong != null) ? giftIconLong.intValue() : 0;
                    Long ts = snapshot.child("timestamp").getValue(Long.class);

                    if (ts != null && ts < roomJoinTime - 5000) return;

                    if (text != null && !text.trim().isEmpty()) {
                        ChatMessage msg = new ChatMessage();
                        msg.setSenderId(senderId != null ? senderId : "");
                        msg.setSenderAvatar(senderAvatar);
                        msg.setGiftIconRes(giftIconRes);
                        String name = senderName != null ? senderName : "User";
                        boolean isGiftMsg = (text.startsWith("sent ") && text.contains(" 🎁")) || giftIconRes != 0;
                        long expireMs = isGiftMsg ? ChatAdapter.GIFT_MESSAGE_EXPIRE_MS : ChatAdapter.USER_MESSAGE_EXPIRE_MS;

                        if (text.startsWith("sent ") && text.contains(" 🎁")) {
                            msg.setMessage(name + " " + text);
                        } else {
                            msg.setMessage(name + " : " + text);
                        }
                        msg.setTimestamp(ts != null ? ts : System.currentTimeMillis());

                        if (chatAdapter != null) {
                            chatAdapter.addAutoExpiringMessage(msg, expireMs);
                        }
                        if (rvChat != null && chatAdapter != null && chatAdapter.getItemCount() > 0) {
                            rvChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                        }
                    }
                }
            }
            @Override public void onChildChanged(@NonNull DataSnapshot s, @Nullable String p) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot s) {}
            @Override public void onChildMoved(@NonNull DataSnapshot s, @Nullable String p) {}
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        };
        roomMessagesRef.addChildEventListener(roomMessagesChildEventListener);
    }

    private String currentPlayingSongName = "";

    private void setupRoomMusicListener() {
        ZegoManager.getInstance().setMusicStateChangeListener(() -> runOnUiThread(this::playNextDeviceTrack));
        roomMusicRef = FirebaseDatabase.getInstance().getReference("room_music").child(roomID);
        roomMusicValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;
                View layoutMusicWidget = findViewById(R.id.layoutMusicWidget);
                TextView tvMusicTitle = findViewById(R.id.tvMusicTitle);
                View btnWidgetMusicPause = findViewById(R.id.btnWidgetMusicPause);

                if (snapshot.exists()) {
                    String songName = snapshot.child("songName").getValue(String.class);
                    Boolean isPlayingObj = snapshot.child("isPlaying").getValue(Boolean.class);
                    boolean isPlaying = Boolean.TRUE.equals(isPlayingObj);

                    if (isPlaying && songName != null && !songName.isEmpty()) {
                        currentPlayingSongName = songName;
                        if (layoutMusicWidget != null) {
                            layoutMusicWidget.setVisibility(View.VISIBLE);
                            layoutMusicWidget.setOnClickListener(v -> showMusicControlSheet());
                        }
                        if (tvMusicTitle != null) {
                            tvMusicTitle.setText(songName);
                            tvMusicTitle.setSelected(true);
                        }

                        if (btnWidgetMusicPause != null) {
                            btnWidgetMusicPause.setVisibility(isHost ? View.VISIBLE : View.GONE);
                            btnWidgetMusicPause.setOnClickListener(v -> {
                                ZegoManager.getInstance().stopMusic();
                                syncMusicStateToFirebase("", false);
                            });
                        }
                    } else {
                        currentPlayingSongName = "";
                        if (layoutMusicWidget != null) layoutMusicWidget.setVisibility(View.GONE);
                    }
                } else {
                    currentPlayingSongName = "";
                    if (layoutMusicWidget != null) layoutMusicWidget.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        roomMusicRef.addValueEventListener(roomMusicValueEventListener);
    }

    private void syncThemeToFirebase(String type, String value) {
        if (roomID == null || roomID.isEmpty()) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("room_themes").child(roomID);
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("themeValue", value);
        map.put("timestamp", System.currentTimeMillis());
        ref.setValue(map);
    }

    private void setupRoomThemeListener() {
        roomThemeRef = FirebaseDatabase.getInstance().getReference("room_themes").child(roomID);
        roomThemeValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;
                if (snapshot.exists()) {
                    String type = snapshot.child("type").getValue(String.class);
                    String value = snapshot.child("themeValue").getValue(String.class);

                    if (type != null && value != null && backgroundView != null) {
                        if ("VIDEO".equals(type)) {
                            backgroundView.setThemeVideo(value);
                        } else if ("IMAGE".equals(type)) {
                            backgroundView.setThemeImage(R.drawable.bg_room_gradient);
                        }
                    }
                } else if (backgroundView != null) {
                    backgroundView.setThemeImage(R.drawable.bg_room_gradient);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        roomThemeRef.addValueEventListener(roomThemeValueEventListener);
    }

    private boolean isEntryBroadcasted = false;

    private void setupRoomEntriesListener() {
        broadcastUserEntry();
        roomEntriesRef = FirebaseDatabase.getInstance().getReference("room_entries").child(roomID);
        roomEntriesChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (isFinishing() || isDestroyed()) return;
                if (snapshot.exists()) {
                    String senderId = snapshot.child("userId").getValue(String.class);
                    String senderName = snapshot.child("userName").getValue(String.class);
                    String entranceSvga = snapshot.child("entranceSvga").getValue(String.class);
                    Long ts = snapshot.child("timestamp").getValue(Long.class);

                    if (ts != null && ts < roomJoinTime - 5000) return;

                    String displayName = (senderName != null && !senderName.trim().isEmpty()) ? senderName : "User";

                    // Add room chat entrance message
                    ChatMessage entryMsg = new ChatMessage();
                    entryMsg.setSenderId(senderId != null ? senderId : "");
                    entryMsg.setSenderAvatar(SessionManager.getInstance(RoomChatActivity.this).getAvatar());
                    entryMsg.setMessage(displayName + " Enter the room");
                    entryMsg.setTimestamp(ts != null ? ts : System.currentTimeMillis());

                    if (chatAdapter != null) {
                        chatAdapter.addAutoExpiringMessage(entryMsg, ChatAdapter.ROOM_MESSAGE_EXPIRE_MS);
                    }

                    // 1. ROOM WELCOME MESSAGE (Show in chat when someone enters with @username)
                    String rawWelcome = (currentRoomWelcomeMessage != null && !currentRoomWelcomeMessage.trim().isEmpty())
                            ? currentRoomWelcomeMessage.trim()
                            : ("Welcome to " + (roomNameLabel != null ? roomNameLabel : "our room") + "! Enjoy chatting & party 🎙️");

                    String formattedWelcomeText;
                    if (rawWelcome.contains("{user}")) {
                        formattedWelcomeText = rawWelcome.replace("{user}", "@" + displayName);
                    } else if (rawWelcome.contains("@user")) {
                        formattedWelcomeText = rawWelcome.replace("@user", "@" + displayName);
                    } else if (!rawWelcome.contains("@" + displayName)) {
                        formattedWelcomeText = "@" + displayName + " " + rawWelcome;
                    } else {
                        formattedWelcomeText = rawWelcome;
                    }

                    if (!formattedWelcomeText.startsWith("📢")) {
                        formattedWelcomeText = "📢 " + formattedWelcomeText;
                    }

                    ChatMessage roomWelcomeNotice = new ChatMessage();
                    roomWelcomeNotice.setSenderId("ROOM_WELCOME");
                    roomWelcomeNotice.setMessage(formattedWelcomeText);
                    roomWelcomeNotice.setTimestamp((ts != null ? ts : System.currentTimeMillis()) + 1);

                    if (chatAdapter != null) {
                        chatAdapter.addAutoExpiringMessage(roomWelcomeNotice, ChatAdapter.ROOM_MESSAGE_EXPIRE_MS);
                    }

                    // 2. NEW USER WELCOME MESSAGE (Show dedicated message to new member entering room with @username)
                    if (userID != null && userID.equals(senderId)) {
                        SharedPreferences prefs = getSharedPreferences("RoomNewUserPrefs", MODE_PRIVATE);
                        String prefKey = "is_new_user_room_" + roomID + "_" + userID;
                        boolean hasEnteredRoomBefore = prefs.getBoolean(prefKey, false);

                        if (!hasEnteredRoomBefore) {
                            prefs.edit().putBoolean(prefKey, true).apply();

                            String rawNewUserMsg = (currentNewUserWelcomeMessage != null && !currentNewUserWelcomeMessage.trim().isEmpty())
                                    ? currentNewUserWelcomeMessage.trim()
                                    : "Welcome new member! Feel free to tap a seat to join the mic or send gifts.";

                            String formattedNewUserMsg;
                            if (rawNewUserMsg.contains("{user}")) {
                                formattedNewUserMsg = rawNewUserMsg.replace("{user}", "@" + displayName);
                            } else if (rawNewUserMsg.contains("@user")) {
                                formattedNewUserMsg = rawNewUserMsg.replace("@user", "@" + displayName);
                            } else if (!rawNewUserMsg.contains("@" + displayName)) {
                                formattedNewUserMsg = "@" + displayName + " " + rawNewUserMsg;
                            } else {
                                formattedNewUserMsg = rawNewUserMsg;
                            }

                            if (!formattedNewUserMsg.startsWith("👋")) {
                                formattedNewUserMsg = "👋 " + formattedNewUserMsg;
                            }

                            ChatMessage newUserNotice = new ChatMessage();
                            newUserNotice.setSenderId("NEW_USER_WELCOME");
                            newUserNotice.setMessage(formattedNewUserMsg);
                            newUserNotice.setTimestamp((ts != null ? ts : System.currentTimeMillis()) + 2);

                            if (chatAdapter != null) {
                                chatAdapter.addAutoExpiringMessage(newUserNotice, ChatAdapter.ROOM_MESSAGE_EXPIRE_MS * 2);
                            }
                            Toast.makeText(RoomChatActivity.this, formattedNewUserMsg, Toast.LENGTH_LONG).show();
                        }
                    }

                    if (rvChat != null && chatAdapter != null && chatAdapter.getItemCount() > 0) {
                        rvChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                    }

                    // Show User Entry Banner Overlay
                    showUserEntryBanner(displayName);

                    // Show Entrance Animation Scene ONLY if user has bought & equipped an entrance effect
                    if (entranceSvga != null && !entranceSvga.trim().isEmpty()) {
                        playSvgaAnimation(entranceSvga);
                    }
                }
            }

            @Override public void onChildChanged(@NonNull DataSnapshot s, @Nullable String p) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot s) {}
            @Override public void onChildMoved(@NonNull DataSnapshot s, @Nullable String p) {}
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        };
        roomEntriesRef.addChildEventListener(roomEntriesChildEventListener);
    }

    // ANIMATED PROFILE EMOJI PICKER DIALOG & REALTIME SYNC
    private void showEmojiPickerDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        applyGlassyStyle(dialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_emoji_picker, null, false);
        if (dialogView == null) return;

        RecyclerView rvEmojiGrid = dialogView.findViewById(R.id.rvEmojiGrid);
        if (rvEmojiGrid != null) {
            rvEmojiGrid.setLayoutManager(new GridLayoutManager(this, 4));
            EmojiAdapter adapter = new EmojiAdapter(emojiId -> {
                dialog.dismiss();
                sendEmojiToRoom(emojiId);
            });
            rvEmojiGrid.setAdapter(adapter);

            // Category Tabs: All, Reactions, Love, Party, Expressive
            TextView tabAll = dialogView.findViewById(R.id.tabEmojiAll);
            TextView tabReactions = dialogView.findViewById(R.id.tabEmojiReactions);
            TextView tabLove = dialogView.findViewById(R.id.tabEmojiLove);
            TextView tabParty = dialogView.findViewById(R.id.tabEmojiParty);
            TextView tabExpressive = dialogView.findViewById(R.id.tabEmojiExpressive);

            TextView[] tabs = new TextView[]{tabAll, tabReactions, tabLove, tabParty, tabExpressive};

            View.OnClickListener onTabClick = v -> {
                for (TextView t : tabs) {
                    if (t != null) {
                        t.setBackgroundResource(t == v ? R.drawable.bg_wallet_chip_selected : R.drawable.bg_wallet_chip_unselected);
                        t.setTextColor(t == v ? Color.WHITE : Color.parseColor("#A0FFFFFF"));
                        t.setTypeface(null, t == v ? Typeface.BOLD : Typeface.NORMAL);
                    }
                }

                List<String> filteredList = new ArrayList<>();
                int id = v.getId();
                if (id == R.id.tabEmojiReactions) { // IDs 1 to 5
                    for (int i = 1; i <= 5; i++) filteredList.add(String.valueOf(i));
                } else if (id == R.id.tabEmojiLove) { // IDs 6 to 10
                    for (int i = 6; i <= 10; i++) filteredList.add(String.valueOf(i));
                } else if (id == R.id.tabEmojiParty) { // IDs 11 to 15
                    for (int i = 11; i <= 15; i++) filteredList.add(String.valueOf(i));
                } else if (id == R.id.tabEmojiExpressive) { // IDs 16 to 20
                    for (int i = 16; i <= 20; i++) filteredList.add(String.valueOf(i));
                } else { // All: IDs 1 to 20
                    for (int i = 1; i <= 20; i++) filteredList.add(String.valueOf(i));
                }
                adapter.updateList(filteredList);
            };

            for (TextView t : tabs) {
                if (t != null) t.setOnClickListener(onTabClick);
            }
        }

        dialog.setContentView(dialogView);
        dialog.show();
    }

    private void sendEmojiToRoom(String emojiId) {
        if (roomID == null || userID == null || emojiId == null) return;

        Map<String, Object> emojiData = new HashMap<>();
        emojiData.put("senderUid", userID);
        emojiData.put("senderName", userName != null ? userName : "User");
        emojiData.put("emojiId", emojiId);
        emojiData.put("timestamp", System.currentTimeMillis());

        FirebaseDatabase.getInstance().getReference("room_emojis")
                .child(roomID)
                .push()
                .setValue(emojiData);
    }

    private void setupRoomEmojiListener() {
        if (roomID == null || roomID.trim().isEmpty()) return;
        if (roomEmojiRef != null && roomEmojiEventListener != null) {
            roomEmojiRef.removeEventListener(roomEmojiEventListener);
        }

        roomEmojiRef = FirebaseDatabase.getInstance().getReference("room_emojis").child(roomID);
        roomEmojiEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (isFinishing() || isDestroyed()) return;
                if (!snapshot.exists()) return;

                String senderUid = snapshot.child("senderUid").getValue(String.class);
                String emojiId = snapshot.child("emojiId").getValue(String.class);
                Long ts = snapshot.child("timestamp").getValue(Long.class);

                if (ts != null && ts < roomJoinTime - 5000) return;

                if (senderUid != null && emojiId != null && seatAdapter != null) {
                    seatAdapter.playEmojiOnSeat(RoomChatActivity.this, senderUid, emojiId);
                }
            }

            @Override public void onChildChanged(@NonNull DataSnapshot s, @Nullable String p) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot s) {}
            @Override public void onChildMoved(@NonNull DataSnapshot s, @Nullable String p) {}
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        };
        roomEmojiRef.addChildEventListener(roomEmojiEventListener);
    }

    private void broadcastUserEntry() {
        if (isEntryBroadcasted) return;
        if (userID == null || userID.trim().isEmpty() || roomID == null) return;

        // Blacklist Check: Do NOT broadcast entry or show notifications if user is blacklisted/banned
        DatabaseReference checkKickRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomID).child("kicked_users").child(userID);
        checkKickRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot kickSnapshot) {
                if (isFinishing() || isDestroyed()) return;

                if (kickSnapshot.exists() && !isHost) {
                    Long expiryTs = kickSnapshot.child("expiryTimestamp").getValue(Long.class);
                    long now = System.currentTimeMillis();
                    if (expiryTs == null || expiryTs == 0L || now <= expiryTs) {
                        // User is blacklisted/banned! Immediately abort without writing to room_entries
                        leaveRoom();
                        return;
                    } else {
                        // Ban expired! Remove from Firebase
                        checkKickRef.removeValue();
                    }
                }

                if (isEntryBroadcasted) return;
                isEntryBroadcasted = true;

                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userID);
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String equippedEntranceId = snapshot.child("equipped_entrance").getValue(String.class);
                        String entranceSvga = FrameUtils.getEntranceSvgaPath(equippedEntranceId);

                        Map<String, Object> entryMap = new HashMap<>();
                        entryMap.put("userId", userID);
                        entryMap.put("userName", userName != null ? userName : "User");
                        if (entranceSvga != null) {
                            entryMap.put("entranceSvga", entranceSvga);
                        }
                        if (equippedEntranceId != null) {
                            entryMap.put("entranceId", equippedEntranceId);
                        }
                        entryMap.put("timestamp", System.currentTimeMillis());

                        FirebaseDatabase.getInstance().getReference("room_entries")
                                .child(roomID).push().setValue(entryMap);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }



    private void applyGlassyStyle(BottomSheetDialog dialog) {
        if (dialog == null) return;
        dialog.setOnShowListener(d -> {
            BottomSheetDialog bsd = (BottomSheetDialog) d;
            View bottomSheet = bsd.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackground(new ColorDrawable(Color.TRANSPARENT));
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        });
    }

    private void showMusicControlSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        applyGlassyStyle(dialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_music_control, null, false);
        if (dialogView == null) return;

        currentMusicControlSheet = dialog;
        dialog.setOnDismissListener(d -> currentMusicControlSheet = null);

        TextView tvSongTitle = dialogView.findViewById(R.id.tvMusicDialogSongTitle);
        TextView tvTrackCount = dialogView.findViewById(R.id.tvMusicTrackCount);
        TextView tvVolumeText = dialogView.findViewById(R.id.tvMusicVolumeText);
        SeekBar seekBarVolume = dialogView.findViewById(R.id.seekBarMusicVolume);

        ProgressBar progressBarMusicLoading = dialogView.findViewById(R.id.progressBarMusicLoading);
        View llMusicEmptyState = dialogView.findViewById(R.id.llMusicEmptyState);
        TextView tvMusicEmptyMessage = dialogView.findViewById(R.id.tvMusicEmptyMessage);
        View btnGrantMusicPermission = dialogView.findViewById(R.id.btnGrantMusicPermission);
        View btnRefreshMusic = dialogView.findViewById(R.id.btnRefreshMusic);

        RecyclerView rvMusicList = dialogView.findViewById(R.id.rvMusicList);

        MaterialButton btnPrev = dialogView.findViewById(R.id.btnMusicPrev);
        MaterialButton btnPlayPause = dialogView.findViewById(R.id.btnMusicPlayPause);
        MaterialButton btnNext = dialogView.findViewById(R.id.btnMusicNext);
        View btnSelectSong = dialogView.findViewById(R.id.btnDialogSelectSong);
        View btnStopMusic = dialogView.findViewById(R.id.btnDialogStopMusic);

        if (tvSongTitle != null) {
            tvSongTitle.setSelected(true);
        }

        final RoomMusicAdapter[] adapterHolder = new RoomMusicAdapter[1];

        Runnable updateUiState = () -> {
            boolean isPlaying = ZegoManager.getInstance().isMusicPlaying();
            boolean isPaused = ZegoManager.getInstance().isMusicPaused();

            if (tvSongTitle != null) {
                if (isPlaying || isPaused) {
                    tvSongTitle.setText((isPlaying ? "🎵 Playing: " : "⏸ Paused: ") + (currentPlayingSongName.isEmpty() ? "Device Song" : currentPlayingSongName));
                } else {
                    tvSongTitle.setText("No Song Playing");
                }
            }

            if (adapterHolder[0] != null) {
                adapterHolder[0].setPlayingPosition(currentDeviceTrackIndex);
            }

            if (tvTrackCount != null) {
                if (deviceAudioTracks.isEmpty()) {
                    tvTrackCount.setText("No device music tracks found");
                } else if (currentDeviceTrackIndex >= 0 && currentDeviceTrackIndex < deviceAudioTracks.size()) {
                    tvTrackCount.setText("Track " + (currentDeviceTrackIndex + 1) + " of " + deviceAudioTracks.size() + " • Available Songs");
                } else {
                    tvTrackCount.setText("Available Songs (" + deviceAudioTracks.size() + " loaded)");
                }
            }

            if (btnPlayPause != null) {
                if (isPlaying) {
                    btnPlayPause.setIconResource(R.drawable.ic_pause);
                } else {
                    btnPlayPause.setIconResource(R.drawable.ic_play);
                }
            }
        };

        RoomMusicAdapter musicAdapter = new RoomMusicAdapter(deviceAudioTracks, (track, position) -> {
            if (!isAdminOrHost()) {
                Toast.makeText(this, "Only Room Host or Administrators can select and play live music!", Toast.LENGTH_SHORT).show();
                return;
            }
            playDeviceTrack(position);
            updateUiState.run();
        });
        adapterHolder[0] = musicAdapter;

        if (rvMusicList != null) {
            rvMusicList.setLayoutManager(new LinearLayoutManager(this));
            rvMusicList.setAdapter(musicAdapter);
            if (!deviceAudioTracks.isEmpty()) {
                rvMusicList.setVisibility(View.VISIBLE);
            }
        }

        Runnable scanAndRefreshMusicList = () -> {
            if (!hasMusicPermission()) {
                musicPermissionLauncher.launch(getRequiredMusicPermissions());
                if (progressBarMusicLoading != null) progressBarMusicLoading.setVisibility(View.GONE);
                if (rvMusicList != null && deviceAudioTracks.isEmpty()) rvMusicList.setVisibility(View.GONE);
                if (llMusicEmptyState != null && deviceAudioTracks.isEmpty()) {
                    llMusicEmptyState.setVisibility(View.VISIBLE);
                    if (tvMusicEmptyMessage != null) {
                        tvMusicEmptyMessage.setText("Permission required to access audio files on your device.");
                    }
                }
                return;
            }

            if (deviceAudioTracks.isEmpty() && progressBarMusicLoading != null) {
                progressBarMusicLoading.setVisibility(View.VISIBLE);
            }
            if (llMusicEmptyState != null) llMusicEmptyState.setVisibility(View.GONE);

            new Thread(() -> {
                List<DeviceAudioTrack> scanned = queryDeviceAudioTracks();
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (progressBarMusicLoading != null) progressBarMusicLoading.setVisibility(View.GONE);

                    if (!scanned.isEmpty()) {
                        deviceAudioTracks.clear();
                        deviceAudioTracks.addAll(scanned);
                    }

                    if (deviceAudioTracks.isEmpty()) {
                        if (rvMusicList != null) rvMusicList.setVisibility(View.GONE);
                        if (llMusicEmptyState != null) {
                            llMusicEmptyState.setVisibility(View.VISIBLE);
                            if (tvMusicEmptyMessage != null) {
                                tvMusicEmptyMessage.setText("No music files found on your device storage.");
                            }
                        }
                    } else {
                        if (llMusicEmptyState != null) llMusicEmptyState.setVisibility(View.GONE);
                        if (rvMusicList != null) rvMusicList.setVisibility(View.VISIBLE);
                        if (musicAdapter != null) {
                            musicAdapter.updateTracks(deviceAudioTracks, currentDeviceTrackIndex);
                        }
                    }
                    updateUiState.run();
                });
            }).start();
        };

        scanAndRefreshMusicList.run();

        if (btnGrantMusicPermission != null) {
            btnGrantMusicPermission.setOnClickListener(v -> {
                musicPermissionLauncher.launch(getRequiredMusicPermissions());
            });
        }

        if (btnRefreshMusic != null) {
            btnRefreshMusic.setOnClickListener(v -> scanAndRefreshMusicList.run());
        }

        int currentVol = ZegoManager.getInstance().getMusicVolume();
        if (seekBarVolume != null) {
            seekBarVolume.setProgress(currentVol);
            if (tvVolumeText != null) {
                tvVolumeText.setText(currentVol + "%");
            }
            seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    ZegoManager.getInstance().setMusicVolume(progress);
                    if (tvVolumeText != null) {
                        tvVolumeText.setText(progress + "%");
                    }
                }

                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        if (btnPlayPause != null) {
            btnPlayPause.setOnClickListener(v -> {
                if (!isAdminOrHost()) {
                    Toast.makeText(this, "Only Room Host or Administrators can control music!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (ZegoManager.getInstance().isMusicPlaying()) {
                    ZegoManager.getInstance().pauseMusic();
                    syncMusicStateToFirebase(currentPlayingSongName, false);
                } else if (ZegoManager.getInstance().isMusicPaused()) {
                    ZegoManager.getInstance().resumeMusic();
                    syncMusicStateToFirebase(currentPlayingSongName, true);
                } else {
                    playNextDeviceTrack();
                }
                updateUiState.run();
            });
        }

        if (btnPrev != null) {
            btnPrev.setOnClickListener(v -> {
                if (!isAdminOrHost()) {
                    Toast.makeText(this, "Only Room Host or Administrators can control music!", Toast.LENGTH_SHORT).show();
                    return;
                }
                playPrevDeviceTrack();
                updateUiState.run();
                if (rvMusicList != null && currentDeviceTrackIndex >= 0) {
                    rvMusicList.smoothScrollToPosition(currentDeviceTrackIndex);
                }
            });
        }

        if (btnNext != null) {
            btnNext.setOnClickListener(v -> {
                if (!isAdminOrHost()) {
                    Toast.makeText(this, "Only Room Host or Administrators can control music!", Toast.LENGTH_SHORT).show();
                    return;
                }
                playNextDeviceTrack();
                updateUiState.run();
                if (rvMusicList != null && currentDeviceTrackIndex >= 0) {
                    rvMusicList.smoothScrollToPosition(currentDeviceTrackIndex);
                }
            });
        }

        if (btnSelectSong != null) {
            btnSelectSong.setVisibility(isAdminOrHost() ? View.VISIBLE : View.GONE);
            btnSelectSong.setOnClickListener(v -> {
                dialog.dismiss();
                openAudioFilePicker();
            });
        }

        if (btnStopMusic != null) {
            btnStopMusic.setVisibility(isAdminOrHost() ? View.VISIBLE : View.GONE);
            btnStopMusic.setOnClickListener(v -> {
                ZegoManager.getInstance().stopMusic();
                syncMusicStateToFirebase("", false);
                updateUiState.run();
                dialog.dismiss();
            });
        }

        dialog.setContentView(dialogView);
        dialog.show();
    }

    private void openAudioFilePicker() {
        if (!isAdminOrHost()) {
            Toast.makeText(this, "Only Room Host or Administrators can select and play live music!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        audioPickerLauncher.launch(intent);
    }

    private void handleSelectedAudioUri(Uri uri) {
        try {
            String fileName = getFileNameFromUri(uri);

            File cacheFile = new File(getCacheDir(), "room_music_" + System.currentTimeMillis() + ".mp3");
            try (InputStream in = getContentResolver().openInputStream(uri);
                 OutputStream out = new FileOutputStream(cacheFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((in != null) && (bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            if (cacheFile.exists()) {
                String audioPath = cacheFile.getAbsolutePath();
                ZegoManager.getInstance().playMusicResource(audioPath, fileName);
                syncMusicStateToFirebase(fileName, true);
                Toast.makeText(this, "🎵 Playing: " + fileName + " (Live to room)", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e("RoomChatActivity", "Error loading audio file", e);
            Toast.makeText(this, "Failed to load audio file", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception ignored) {}
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result != null ? result : "Selected Song.mp3";
    }

    private void syncMusicStateToFirebase(String songName, boolean isPlaying) {
        if (roomID == null || roomID.isEmpty()) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("room_music").child(roomID);
        Map<String, Object> map = new HashMap<>();
        map.put("songName", songName != null ? songName : "");
        map.put("isPlaying", isPlaying);
        map.put("timestamp", System.currentTimeMillis());
        ref.setValue(map);
    }

    private BottomSheetDialog roomGameDialog;
    private Dialog activeWebGameDialog;
    private ViewPager2 vpGameCarousel;
    private View layoutGameCarouselContainer;
    private final Handler gameCarouselHandler = new Handler(Looper.getMainLooper());
    private Runnable gameCarouselRunnable;
    private static final long CAROUSEL_AUTO_SCROLL_DELAY = 3000L;

    private static class CarouselGameItem {
        final String title;
        final int iconRes;
        final String url;

        CarouselGameItem(String title, int iconRes, String url) {
            this.title = title;
            this.iconRes = iconRes;
            this.url = url;
        }
    }

    private interface OnGameClickListener {
        void onGameClick(CarouselGameItem item);
    }

    private class GameCarouselAdapter extends RecyclerView.Adapter<GameCarouselAdapter.GameViewHolder> {
        private final List<CarouselGameItem> gameList;
        private final OnGameClickListener listener;

        GameCarouselAdapter(List<CarouselGameItem> gameList, OnGameClickListener listener) {
            this.gameList = gameList;
            this.listener = listener;
        }

        @NonNull
        @Override
        public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_game_carousel, parent, false);
            return new GameViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
            if (gameList.isEmpty()) return;
            CarouselGameItem game = gameList.get(position % gameList.size());
            holder.ivIcon.setImageResource(game.iconRes);
            AnimationHelper.applyClickAnimation(holder.itemView);
            holder.itemView.setOnClickListener(v -> {
                if (isFastClick(v)) return;
                if (listener != null) {
                    listener.onGameClick(game);
                }
            });
        }

        @Override
        public int getItemCount() {
            return gameList.isEmpty() ? 0 : Integer.MAX_VALUE;
        }

        class GameViewHolder extends RecyclerView.ViewHolder {
            ImageView ivIcon;

            GameViewHolder(@NonNull View itemView) {
                super(itemView);
                ivIcon = itemView.findViewById(R.id.ivGameIcon);
            }
        }
    }

    private void setupGameCarousel() {
        vpGameCarousel = findViewById(R.id.vpGameCarousel);
        layoutGameCarouselContainer = findViewById(R.id.layoutGameCarouselContainer);

        if (vpGameCarousel == null) return;

        List<CarouselGameItem> gameList = new ArrayList<>();
        gameList.add(new CarouselGameItem("Royal Slots", R.drawable.slot_icon, "file:///android_asset/slot_machine.html"));
        gameList.add(new CarouselGameItem("Lucky Fruit", R.drawable.lucky_fruit, "file:///android_asset/lucky_fruit/index.html"));
        gameList.add(new CarouselGameItem("Greedy Game", R.drawable.greedy_pro_icon, "file:///android_asset/greedy-game/index.html"));
        gameList.add(new CarouselGameItem("Ludo", R.drawable.ludo_icon, "file:///android_asset/ludo-game/index.html"));

        GameCarouselAdapter adapter = new GameCarouselAdapter(gameList, item -> {
            showWebGameDialog(item.title, item.url);
        });

        vpGameCarousel.setAdapter(adapter);

        int initialPos = (Integer.MAX_VALUE / 2) - ((Integer.MAX_VALUE / 2) % gameList.size());
        vpGameCarousel.setCurrentItem(initialPos, false);

        gameCarouselRunnable = new Runnable() {
            @Override
            public void run() {
                if (vpGameCarousel != null && !gameList.isEmpty()) {
                    int current = vpGameCarousel.getCurrentItem();
                    vpGameCarousel.setCurrentItem(current + 1, true);
                    gameCarouselHandler.postDelayed(this, CAROUSEL_AUTO_SCROLL_DELAY);
                }
            }
        };

        vpGameCarousel.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    stopGameCarouselAutoScroll();
                } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    startGameCarouselAutoScroll();
                }
            }
        });

        if (layoutGameCarouselContainer != null) {
            AnimationHelper.applyClickAnimation(layoutGameCarouselContainer);
            layoutGameCarouselContainer.setOnClickListener(v -> {
                if (isFastClick(v)) return;
                if (vpGameCarousel != null && !gameList.isEmpty()) {
                    int currentItemIndex = vpGameCarousel.getCurrentItem() % gameList.size();
                    CarouselGameItem currentItem = gameList.get(currentItemIndex);
                    showWebGameDialog(currentItem.title, currentItem.url);
                }
            });
        }

        startGameCarouselAutoScroll();
    }

    private void startGameCarouselAutoScroll() {
        stopGameCarouselAutoScroll();
        if (gameCarouselHandler != null && gameCarouselRunnable != null) {
            gameCarouselHandler.postDelayed(gameCarouselRunnable, CAROUSEL_AUTO_SCROLL_DELAY);
        }
    }

    private void stopGameCarouselAutoScroll() {
        if (gameCarouselHandler != null && gameCarouselRunnable != null) {
            gameCarouselHandler.removeCallbacks(gameCarouselRunnable);
        }
    }

    private void showRoomGameDialog() {
        if (roomGameDialog != null && roomGameDialog.isShowing()) return;
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        roomGameDialog = dialog;
        dialog.setOnDismissListener(d -> {
            if (roomGameDialog == d) roomGameDialog = null;
        });
        applyGlassyStyle(dialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_room_game, null, false);
        if (dialogView != null) {
            dialog.setContentView(dialogView);

            View btnClose = dialogView.findViewById(R.id.btnClose);
            if (btnClose != null) {
                btnClose.setOnClickListener(v -> dialog.dismiss());
            }

            View cardSlotGame = dialogView.findViewById(R.id.cardSlotGame);
            if (cardSlotGame != null) {
                AnimationHelper.applyClickAnimation(cardSlotGame);
                cardSlotGame.setOnClickListener(v -> {
                    dialog.dismiss();
                    showWebGameDialog("Royal Slots", "file:///android_asset/slot_machine.html");
                });
            }

            View cardLuckySpin = dialogView.findViewById(R.id.cardLuckySpin);
            if (cardLuckySpin != null) {
                AnimationHelper.applyClickAnimation(cardLuckySpin);
                cardLuckySpin.setOnClickListener(v -> {
                    dialog.dismiss();
                    showWebGameDialog("Lucky Fruit", "file:///android_asset/lucky_fruit/index.html");
                });
            }

            View cardGreedyGame = dialogView.findViewById(R.id.cardGreedyGame);
            if (cardGreedyGame != null) {
                AnimationHelper.applyClickAnimation(cardGreedyGame);
                cardGreedyGame.setOnClickListener(v -> {
                    dialog.dismiss();
                    showWebGameDialog("Greedy Game", "file:///android_asset/greedy-game/index.html");
                });
            }

            View cardLudoGame = dialogView.findViewById(R.id.cardLudoGame);
            if (cardLudoGame != null) {
                AnimationHelper.applyClickAnimation(cardLudoGame);
                cardLudoGame.setOnClickListener(v -> {
                    dialog.dismiss();
                    showWebGameDialog("Ludo", "file:///android_asset/ludo-game/index.html");
                });
            }

            dialog.show();
        }
    }

    private void showWebGameDialog(String gameTitle, String gameUrl) {
        if (activeWebGameDialog != null && activeWebGameDialog.isShowing()) {
            activeWebGameDialog.dismiss();
        }
        Dialog dialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        activeWebGameDialog = dialog;
        dialog.setOnDismissListener(d -> {
            if (activeWebGameDialog == d) activeWebGameDialog = null;
        });

        int layoutId = (gameUrl != null && gameUrl.contains("ludo-game"))
                ? R.layout.dialog_ludo_game
                : R.layout.dialog_web_game;

        View dialogView = getLayoutInflater().inflate(layoutId, null, false);
        if (dialogView == null) return;

        dialog.setContentView(dialogView);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }

        dialogView.setOnClickListener(v -> dialog.dismiss());

        View cardContainer = dialogView.findViewById(R.id.cardGameContainer);
        if (cardContainer != null) {
            cardContainer.setOnClickListener(v -> {}); // Consume click on game card
        }

        TextView tvGameTitle = dialogView.findViewById(R.id.tvGameTitle);
        if (tvGameTitle != null && gameTitle != null) {
            tvGameTitle.setText(gameTitle);
        }

        View btnCloseGame = dialogView.findViewById(R.id.btnCloseGame);
        if (btnCloseGame != null) {
            btnCloseGame.setOnClickListener(v -> dialog.dismiss());
        }

        WebView webView = dialogView.findViewById(R.id.webViewGame);
        ProgressBar progressBar = dialogView.findViewById(R.id.progressBarGame);
        if (webView != null) {
            webView.setBackgroundColor(Color.TRANSPARENT);
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

            WebSettings settings = webView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setDomStorageEnabled(true);
            settings.setAllowFileAccess(true);
            settings.setAllowContentAccess(true);
            settings.setLoadWithOverviewMode(false);
            settings.setUseWideViewPort(false);
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

            settings.setBuiltInZoomControls(false);
            settings.setDisplayZoomControls(false);
            settings.setSupportZoom(false);

            webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
            webView.setHorizontalScrollBarEnabled(false);
            webView.setVerticalScrollBarEnabled(false);

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                }
            });

            webView.loadUrl(gameUrl);
        }

        dialog.setOnKeyListener((dialogInterface, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                if (webView != null && webView.canGoBack()) {
                    webView.goBack();
                    return true;
                } else {
                    dialog.dismiss();
                    return true;
                }
            }
            return false;
        });

        dialog.show();
    }

    private void showCoinExchangeDialog() {
        if (isFinishing() || isDestroyed()) return;
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        applyGlassyStyle(dialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_coin_exchange, null, false);
        if (dialogView == null) return;

        TextView tvAvailableCoins = dialogView.findViewById(R.id.tvExchangeAvailableCoins);
        EditText etCoinsInput = dialogView.findViewById(R.id.etCoinsToExchange);
        TextView tvEnergyPreview = dialogView.findViewById(R.id.tvEnergyGainPreview);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmExchange);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelExchange);

        WalletManager.getUserCoins(userID, balance -> runOnUiThread(() -> {
            if (tvAvailableCoins != null) tvAvailableCoins.setText(String.valueOf(balance));
        }));

        if (etCoinsInput != null) {
            etCoinsInput.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        long val = Long.parseLong(s.toString().trim());
                        long energyGained = (long) Math.floor(val * EconomyConfig.COIN_TO_ENERGY_RATE);
                        if (tvEnergyPreview != null) tvEnergyPreview.setText(energyGained + " Energy ⚡");
                    } catch (Exception e) {
                        if (tvEnergyPreview != null) tvEnergyPreview.setText("0 Energy ⚡");
                    }
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                String inputStr = etCoinsInput != null ? etCoinsInput.getText().toString().trim() : "";
                if (inputStr.isEmpty()) {
                    Toast.makeText(this, "Please enter coin amount to exchange", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    long coinsToExchange = Long.parseLong(inputStr);
                    if (coinsToExchange <= 0) {
                        Toast.makeText(this, "Please enter a valid coin amount", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    btnConfirm.setEnabled(false);
                    WalletManager.exchangeCoinsForEnergy(userID, coinsToExchange, new WalletManager.WalletCallback() {
                        @Override
                        public void onSuccess(String message, long newCoinBalance) {
                            btnConfirm.setEnabled(true);
                            dialog.dismiss();
                            Toast.makeText(RoomChatActivity.this, message, Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onError(String error) {
                            btnConfirm.setEnabled(true);
                            Toast.makeText(RoomChatActivity.this, "❌ " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
                }
            });
        }

        dialog.setContentView(dialogView);
        dialog.show();
    }

    private void showMorePanelDialog() {
        if (morePanelDialog != null && morePanelDialog.isShowing()) return;
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        morePanelDialog = dialog;
        dialog.setOnDismissListener(d -> {
            if (morePanelDialog == d) morePanelDialog = null;
        });
        applyGlassyStyle(dialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_more_panel, null, false);
        if (dialogView == null) return;

        TextView tvMemberSubtitle = dialogView.findViewById(R.id.tvMemberSubtitle);
        TextView tvSeatsSubtitle = dialogView.findViewById(R.id.tvSeatsSubtitle);

        View btnOptionTheme = dialogView.findViewById(R.id.btnOptionTheme);
        View btnOptionMembers = dialogView.findViewById(R.id.btnOptionMembers);
        View btnOptionSeats = dialogView.findViewById(R.id.btnOptionSeats);
        View btnOptionMusic = dialogView.findViewById(R.id.btnOptionMusic);

        List<SeatModel> currentSeats = SeatManager.getInstance().getSeats();
        int occupiedCount = 0;
        for (SeatModel seat : currentSeats) {
            if (seat != null && !seat.isEmpty()) {
                occupiedCount++;
            }
        }

        int totalMembersCount = Math.max(occupiedCount, 1);

        if (tvMemberSubtitle != null) {
            tvMemberSubtitle.setText(totalMembersCount + " Member" + (totalMembersCount > 1 ? "s" : "") + " currently in room");
        }

        int totalSeatsCount = SeatManager.getInstance().getTotalSeats();
        TextView tvSeatsSelected = dialogView.findViewById(R.id.tvSeatsSelected);
        int freeSeatsCount = Math.max(0, totalSeatsCount - occupiedCount);

        if (tvSeatsSubtitle != null) {
            tvSeatsSubtitle.setText(totalSeatsCount + " Total Seats · " + occupiedCount + " Occupied, " + freeSeatsCount + " Free");
        }
        if (tvSeatsSelected != null) {
            tvSeatsSelected.setText("✓ Selected: " + totalSeatsCount + " Seats");
        }



        // ROOM ADMIN FIX: Option for Administrator Management (Host Only)
        View btnOptionAdministrator = dialogView.findViewById(R.id.btnOptionAdministrator);
        if (btnOptionAdministrator != null) {
            btnOptionAdministrator.setVisibility(isHost ? View.VISIBLE : View.GONE);
            btnOptionAdministrator.setOnClickListener(v -> {
                dialog.dismiss();
                if (isHost) {
                    showAdminManagementDialog();
                } else {
                    Toast.makeText(RoomChatActivity.this, "Only Room Host can manage administrators 👑", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // ADMIN PERMISSION FIX: Option 0: Edit Room Info (Title & Cover Thumbnail)
        View btnOptionEditRoom = dialogView.findViewById(R.id.btnOptionEditRoom);
        if (btnOptionEditRoom != null) {
            btnOptionEditRoom.setOnClickListener(v -> {
                dialog.dismiss();
                if (isAdminOrHost()) {
                    showEditRoomDialog();
                } else {
                    Toast.makeText(RoomChatActivity.this, "Only Room Host or Administrators can edit room details 👑", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Option: Room Welcome Message
        View btnOptionRoomWelcome = dialogView.findViewById(R.id.btnOptionRoomWelcome);
        if (btnOptionRoomWelcome != null) {
            btnOptionRoomWelcome.setVisibility(isAdminOrHost() ? View.VISIBLE : View.GONE);
            btnOptionRoomWelcome.setOnClickListener(v -> {
                dialog.dismiss();
                if (isAdminOrHost()) {
                    showEditWelcomeMessageDialog(false);
                } else {
                    Toast.makeText(RoomChatActivity.this, "Only Room Host or Administrators can edit welcome message 📢", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Option: New User Welcome Message
        View btnOptionNewUserWelcome = dialogView.findViewById(R.id.btnOptionNewUserWelcome);
        if (btnOptionNewUserWelcome != null) {
            btnOptionNewUserWelcome.setVisibility(isAdminOrHost() ? View.VISIBLE : View.GONE);
            btnOptionNewUserWelcome.setOnClickListener(v -> {
                dialog.dismiss();
                if (isAdminOrHost()) {
                    showEditWelcomeMessageDialog(true);
                } else {
                    Toast.makeText(RoomChatActivity.this, "Only Room Host or Administrators can edit new user welcome message 👋", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Option: Clear Room Screen
        View btnOptionClearScreen = dialogView.findViewById(R.id.btnOptionClearScreen);
        if (btnOptionClearScreen != null) {
            btnOptionClearScreen.setVisibility(isAdminOrHost() ? View.VISIBLE : View.GONE);
            btnOptionClearScreen.setOnClickListener(v -> {
                dialog.dismiss();
                if (isAdminOrHost()) {
                    FirebaseDatabase.getInstance().getReference("rooms").child(roomID)
                            .child("clearScreenTimestamp").setValue(System.currentTimeMillis());
                    Toast.makeText(RoomChatActivity.this, "Room chat screen cleared 🧹", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RoomChatActivity.this, "Only Room Host or Administrators can clear the screen 🧹", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Option: Room Blacklist
        View btnOptionBlacklist = dialogView.findViewById(R.id.btnOptionBlacklist);
        if (btnOptionBlacklist != null) {
            btnOptionBlacklist.setVisibility(isAdminOrHost() ? View.VISIBLE : View.GONE);
            btnOptionBlacklist.setOnClickListener(v -> {
                dialog.dismiss();
                if (isAdminOrHost()) {
                    showBlacklistManagementDialog();
                } else {
                    Toast.makeText(RoomChatActivity.this, "Only Room Host or Administrators can manage blacklist 🚫", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Option 1: Theme Selection
        if (btnOptionTheme != null) {
            btnOptionTheme.setOnClickListener(v -> {
                dialog.dismiss();
                showThemeSelectionDialog();
            });
        }

        // Option 2: Members & Audience
        if (btnOptionMembers != null) {
            btnOptionMembers.setOnClickListener(v -> {
                dialog.dismiss();
                showRoomMembersDialog();
            });
        }

        // Option 3: Seats Management
        if (btnOptionSeats != null) {
            btnOptionSeats.setOnClickListener(v -> {
                dialog.dismiss();
                showSeatCapacityDialog();
            });
        }

        // Option 4: Room Music System
        if (btnOptionMusic != null) {
            btnOptionMusic.setOnClickListener(v -> {
                dialog.dismiss();
                showMusicControlSheet();
            });
        }

        // Option 5: Frame Store
        View btnOptionStore = dialogView.findViewById(R.id.btnOptionStore);
        if (btnOptionStore != null) {
            btnOptionStore.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(RoomChatActivity.this, StoreActivity.class));
            });
        }

        // Option 6: Edit Profile & Avatar
        View btnOptionProfile = dialogView.findViewById(R.id.btnOptionProfile);
        if (btnOptionProfile != null) {
            btnOptionProfile.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(RoomChatActivity.this, EditProfileActivity.class));
            });
        }

        // Option 7: Energy -> Coin Exchange
        View btnOptionExchange = dialogView.findViewById(R.id.btnOptionExchange);
        if (btnOptionExchange != null) {
            btnOptionExchange.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(RoomChatActivity.this, EnergyConvertActivity.class));
            });
        }

        // Option 8: Minimize Room
        View btnOptionMinimize = dialogView.findViewById(R.id.btnOptionMinimize);
        if (btnOptionMinimize != null) {
            btnOptionMinimize.setOnClickListener(v -> {
                dialog.dismiss();
                Toast.makeText(RoomChatActivity.this, "Room running in background 🎙️", Toast.LENGTH_SHORT).show();
                minimizeRoom();
            });
        }

        // Option: Private Your Room
        View btnOptionPrivateRoom = dialogView.findViewById(R.id.btnOptionPrivateRoom);
        if (btnOptionPrivateRoom != null) {
            btnOptionPrivateRoom.setVisibility(isAdminOrHost() ? View.VISIBLE : View.GONE);
            btnOptionPrivateRoom.setOnClickListener(v -> {
                dialog.dismiss();
                showPrivateRoomSettingsDialog();
            });
        }

        dialog.setContentView(dialogView);
        dialog.show();
    }

    private void showPrivateRoomSettingsDialog() {
        if (!isAdminOrHost()) {
            Toast.makeText(this, "Only Room Host or Admin can manage private room settings", Toast.LENGTH_SHORT).show();
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        applyGlassyStyle(dialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_private_room_settings, null, false);
        if (dialogView == null) return;

        MaterialSwitch swPrivateRoom = dialogView.findViewById(R.id.swPrivateRoom);
        EditText etRoomPin = dialogView.findViewById(R.id.etRoomPin);
        View btnSavePrivateSettings = dialogView.findViewById(R.id.btnSavePrivateSettings);

        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomID);
        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;
                if (snapshot.exists()) {
                    Boolean isPrivateVal = snapshot.child("isPrivate").getValue(Boolean.class);
                    String pinVal = snapshot.child("roomPin").getValue(String.class);
                    if (pinVal == null) pinVal = snapshot.child("pin").getValue(String.class);

                    if (swPrivateRoom != null && isPrivateVal != null) {
                        swPrivateRoom.setChecked(isPrivateVal);
                    }
                    if (etRoomPin != null && pinVal != null) {
                        etRoomPin.setText(pinVal);
                    }
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        if (btnSavePrivateSettings != null) {
            btnSavePrivateSettings.setOnClickListener(v -> {
                boolean isPrivateChecked = swPrivateRoom != null && swPrivateRoom.isChecked();
                String enteredPin = etRoomPin != null ? etRoomPin.getText().toString().trim() : "";

                if (isPrivateChecked && (enteredPin.isEmpty() || enteredPin.length() < 4)) {
                    Toast.makeText(this, "Please enter a valid 4-digit PIN for Private Room!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> updates = new HashMap<>();
                updates.put("isPrivate", isPrivateChecked);
                updates.put("roomPin", enteredPin);
                updates.put("pin", enteredPin);

                roomRef.updateChildren(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RoomChatActivity.this, isPrivateChecked ? "🔒 Room set to Private with PIN!" : "🔓 Room is now Public!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(RoomChatActivity.this, "Failed to update room privacy", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        dialog.setContentView(dialogView);
        dialog.show();
    }

    // ROOM ADMIN FIX: Administrator Management BottomSheet Dialog
    private void showAdminManagementDialog() {
        if (!isHost) {
            Toast.makeText(this, "Only Room Host can manage administrators", Toast.LENGTH_SHORT).show();
            return;
        }

        BottomSheetDialog adminDialog = new BottomSheetDialog(this);
        applyGlassyStyle(adminDialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_admin_management, null, false);
        if (dialogView == null) return;

        TextView tvAdminCountBadge = dialogView.findViewById(R.id.tvAdminCountBadge);
        EditText etSearchAdminMembers = dialogView.findViewById(R.id.etSearchAdminMembers);
        RecyclerView rvAdminMembers = dialogView.findViewById(R.id.rvAdminMembers);

        List<AdminMemberItem> adminMemberList = new ArrayList<>();
        Map<String, AdminMemberItem> itemMap = new HashMap<>();

        final String effectiveHostUid = (SeatManager.getInstance().getHostUserID() != null && !SeatManager.getInstance().getHostUserID().trim().isEmpty())
                ? SeatManager.getInstance().getHostUserID() : userID;

        // 1. Add seated members
        List<SeatModel> seats = SeatManager.getInstance().getSeats();
        for (SeatModel seat : seats) {
            if (seat != null && !seat.isEmpty()) {
                String uid = seat.userID;
                String name = seat.userName != null && !seat.userName.trim().isEmpty() ? seat.userName : "Member";
                String avatar = seat.userAvatar;

                String role = "member";
                if (uid.equals(effectiveHostUid)) {
                    role = "host";
                } else if (userRoomRolesMap.containsKey(uid)) {
                    role = userRoomRolesMap.get(uid);
                }

                AdminMemberItem item = new AdminMemberItem(uid, name, avatar, role);
                itemMap.put(uid, item);
                adminMemberList.add(item);
            }
        }

        // 2. Add connected room users from room_users/{roomID}
        DatabaseReference onlineRef = FirebaseDatabase.getInstance().getReference("room_users").child(roomID);
        onlineRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;

                for (DataSnapshot child : snapshot.getChildren()) {
                    String uid = child.child("userId").getValue(String.class);
                    if (uid == null) uid = child.getKey();
                    if (uid != null && !itemMap.containsKey(uid)) {
                        String name = child.child("userName").getValue(String.class);
                        String avatar = child.child("userAvatar").getValue(String.class);

                        String role = "member";
                        if (uid.equals(effectiveHostUid)) {
                            role = "host";
                        } else if (userRoomRolesMap.containsKey(uid)) {
                            role = userRoomRolesMap.get(uid);
                        }

                        AdminMemberItem item = new AdminMemberItem(uid, name, avatar, role);
                        itemMap.put(uid, item);
                        adminMemberList.add(item);
                    }
                }

                // 3. Ensure Host is included
                if (!itemMap.containsKey(effectiveHostUid)) {
                    String myAvatar = SessionManager.getInstance(RoomChatActivity.this).getAvatar();
                    AdminMemberItem hostItem = new AdminMemberItem(effectiveHostUid, userName != null ? userName : "Host", myAvatar, "host");
                    adminMemberList.add(0, hostItem);
                    itemMap.put(effectiveHostUid, hostItem);
                }

                int adminCount = 0;
                for (AdminMemberItem item : adminMemberList) {
                    if (item.isAdmin()) adminCount++;
                }

                if (tvAdminCountBadge != null) {
                    tvAdminCountBadge.setText(adminCount + " Admin" + (adminCount != 1 ? "s" : ""));
                }

                if (rvAdminMembers != null) {
                    rvAdminMembers.setLayoutManager(new LinearLayoutManager(RoomChatActivity.this));
                    AdminMemberAdapter adapter = new AdminMemberAdapter(adminMemberList, new OnRoleActionListener() {
                        @Override
                        public void onMakeAdmin(AdminMemberItem item) {
                            showPromoteAdminConfirmation(item, adminDialog);
                        }

                        @Override
                        public void onRemoveAdmin(AdminMemberItem item) {
                            showRemoveAdminConfirmation(item, adminDialog);
                        }
                    });
                    rvAdminMembers.setAdapter(adapter);

                    // Real-Time Search Filtering for Admin Management
                    if (etSearchAdminMembers != null) {
                        etSearchAdminMembers.addTextChangedListener(new TextWatcher() {
                            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                String query = s.toString().trim().toLowerCase();
                                List<AdminMemberItem> filtered = new ArrayList<>();
                                for (AdminMemberItem item : adminMemberList) {
                                    String name = item.getUserName() != null ? item.getUserName().toLowerCase() : "";
                                    String uid = item.getUserId() != null ? item.getUserId().toLowerCase() : "";
                                    if (name.contains(query) || uid.contains(query)) {
                                        filtered.add(item);
                                    }
                                }
                                adapter.updateList(filtered);
                            }
                            @Override public void afterTextChanged(Editable s) {}
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RoomChatActivity", "Error loading room members for admin management", error.toException());
            }
        });

        adminDialog.setContentView(dialogView);
        adminDialog.show();
    }

    // Room Blacklist Management Dialog
    private void showBlacklistManagementDialog() {
        if (!isAdminOrHost()) {
            Toast.makeText(this, "Only Room Host or Administrators can manage blacklist", Toast.LENGTH_SHORT).show();
            return;
        }

        BottomSheetDialog blacklistDialog = new BottomSheetDialog(this);
        applyGlassyStyle(blacklistDialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_black_list, null, false);
        if (dialogView == null) return;

        TextView tvCountBadge = dialogView.findViewById(R.id.tvBlacklistCountBadge);
        EditText etSearch = dialogView.findViewById(R.id.etSearchBlacklist);
        RecyclerView rvBlacklist = dialogView.findViewById(R.id.rvBlacklistMembers);

        List<BlacklistAdapter.BlacklistItem> blacklist = new ArrayList<>();
        DatabaseReference kickedUsersRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomID).child("kicked_users");

        kickedUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;

                long now = System.currentTimeMillis();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String uid = child.getKey();
                    if (uid == null) continue;

                    Long expiryTs = child.child("expiryTimestamp").getValue(Long.class);
                    long exp = expiryTs != null ? expiryTs : 0L;

                    if (exp > 0 && now > exp) {
                        // Expired ban -> Clean up Firebase
                        kickedUsersRef.child(uid).removeValue();
                        continue;
                    }

                    String name = child.child("userName").getValue(String.class);
                    String avatar = child.child("userAvatar").getValue(String.class);

                    blacklist.add(new BlacklistAdapter.BlacklistItem(uid, name != null ? name : "User", avatar != null ? avatar : "", exp));
                }

                if (tvCountBadge != null) {
                    tvCountBadge.setText(blacklist.size() + " Banned");
                }

                if (rvBlacklist != null) {
                    rvBlacklist.setLayoutManager(new LinearLayoutManager(RoomChatActivity.this));
                    final BlacklistAdapter[] adapterHolder = new BlacklistAdapter[1];
                    BlacklistAdapter adapter = new BlacklistAdapter(blacklist, item -> {
                        kickedUsersRef.child(item.getUserId()).removeValue().addOnSuccessListener(aVoid -> {
                            blacklist.remove(item);
                            if (adapterHolder[0] != null) adapterHolder[0].updateList(blacklist);
                            if (tvCountBadge != null) {
                                tvCountBadge.setText(blacklist.size() + " Banned");
                            }
                            Toast.makeText(RoomChatActivity.this, "Removed " + item.getUserName() + " from room blacklist 🔓", Toast.LENGTH_SHORT).show();
                        });
                    });
                    adapterHolder[0] = adapter;
                    rvBlacklist.setAdapter(adapter);

                    if (etSearch != null) {
                        etSearch.addTextChangedListener(new TextWatcher() {
                            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                String query = s.toString().trim().toLowerCase();
                                List<BlacklistAdapter.BlacklistItem> filtered = new ArrayList<>();
                                for (BlacklistAdapter.BlacklistItem item : blacklist) {
                                    String name = item.getUserName() != null ? item.getUserName().toLowerCase() : "";
                                    String uid = item.getUserId() != null ? item.getUserId().toLowerCase() : "";
                                    if (name.contains(query) || uid.contains(query)) {
                                        filtered.add(item);
                                    }
                                }
                                if (adapterHolder[0] != null) adapterHolder[0].updateList(filtered);
                            }
                            @Override public void afterTextChanged(Editable s) {}
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        blacklistDialog.setContentView(dialogView);
        blacklistDialog.show();
    }

    // Edit Welcome Message Dialog for Admins and Host
    private void showEditWelcomeMessageDialog(boolean isNewUserMessage) {
        if (!isAdminOrHost()) {
            Toast.makeText(this, "Only Room Host or Administrators can edit welcome messages", Toast.LENGTH_SHORT).show();
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        applyGlassyStyle(dialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_welcome_message, null, false);
        if (dialogView == null) return;

        TextView tvTitle = dialogView.findViewById(R.id.tvWelcomeDialogTitle);
        TextView tvSubtitle = dialogView.findViewById(R.id.tvWelcomeDialogSubtitle);
        EditText etInput = dialogView.findViewById(R.id.etWelcomeMessageInput);
        ProgressBar pbLoading = dialogView.findViewById(R.id.pbWelcomeLoading);
        View btnCancel = dialogView.findViewById(R.id.btnCancelWelcome);
        View btnSave = dialogView.findViewById(R.id.btnSaveWelcome);

        if (tvTitle != null) {
            tvTitle.setText(isNewUserMessage ? "New User Welcome Message 👋" : "Room Welcome Message 📢");
        }
        if (tvSubtitle != null) {
            tvSubtitle.setText(isNewUserMessage ?
                    "Set the dedicated welcome message that will be displayed when new members enter the room." :
                    "Set the welcome message that will be displayed in room chat to all members on entry.");
        }

        String currentMsg = isNewUserMessage ? currentNewUserWelcomeMessage : currentRoomWelcomeMessage;
        if (etInput != null && currentMsg != null && !currentMsg.trim().isEmpty()) {
            etInput.setText(currentMsg);
            etInput.setSelection(currentMsg.length());
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                String inputMsg = etInput != null ? etInput.getText().toString().trim() : "";
                if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
                btnSave.setEnabled(false);

                String key = isNewUserMessage ? "newUserWelcomeMessage" : "welcomeMessage";
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("rooms").child(roomID).child(key);

                ref.setValue(inputMsg).addOnCompleteListener(task -> {
                    if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    if (task.isSuccessful()) {
                        if (isNewUserMessage) {
                            currentNewUserWelcomeMessage = inputMsg;
                        } else {
                            currentRoomWelcomeMessage = inputMsg;
                        }
                        Toast.makeText(RoomChatActivity.this, "Welcome message saved successfully! 📢", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(RoomChatActivity.this, "Failed to save message. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        dialog.setContentView(dialogView);
        dialog.show();
    }

    // ROOM ADMIN FIX: Confirmation Dialog to Promote to Admin
    private void showPromoteAdminConfirmation(AdminMemberItem item, BottomSheetDialog parentDialog) {
        if (!isHost) return;
        String name = item.getUserName() != null ? item.getUserName() : "User";

        new AlertDialog.Builder(this)
                .setTitle("Make Administrator?")
                .setMessage(name + " will receive administrator permissions in this room.")
                .setPositiveButton("Make Admin", (dialog, which) -> {
                    promoteToAdmin(item);
                    if (parentDialog != null && parentDialog.isShowing()) {
                        parentDialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ROOM ADMIN FIX: Confirmation Dialog to Remove Admin
    private void showRemoveAdminConfirmation(AdminMemberItem item, BottomSheetDialog parentDialog) {
        if (!isHost) return;
        String name = item.getUserName() != null ? item.getUserName() : "User";

        new AlertDialog.Builder(this)
                .setTitle("Remove Administrator?")
                .setMessage(name + " will lose administrator permissions in this room.")
                .setPositiveButton("Remove", (dialog, which) -> {
                    removeAdmin(item);
                    if (parentDialog != null && parentDialog.isShowing()) {
                        parentDialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ROOM ROLE PERSISTENCE: Save role in Firebase Realtime Database
    private void promoteToAdmin(AdminMemberItem item) {
        if (roomID == null || item == null || item.getUserId() == null) return;
        String targetUid = item.getUserId();

        Map<String, Object> roleData = new HashMap<>();
        roleData.put("role", "admin");
        roleData.put("updatedAt", System.currentTimeMillis());
        roleData.put("updatedBy", userID);

        FirebaseDatabase.getInstance().getReference("room_roles")
                .child(roomID)
                .child(targetUid)
                .setValue(roleData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RoomChatActivity.this, item.getUserName() + " is now an Administrator 🛡️", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RoomChatActivity.this, "Failed to promote user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // ROOM ROLE PERSISTENCE: Remove admin role in Firebase Realtime Database
    private void removeAdmin(AdminMemberItem item) {
        if (roomID == null || item == null || item.getUserId() == null) return;
        String targetUid = item.getUserId();

        Map<String, Object> roleData = new HashMap<>();
        roleData.put("role", "member");
        roleData.put("updatedAt", System.currentTimeMillis());
        roleData.put("updatedBy", userID);

        FirebaseDatabase.getInstance().getReference("room_roles")
                .child(roomID)
                .child(targetUid)
                .setValue(roleData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RoomChatActivity.this, "Removed administrator permissions from " + item.getUserName(), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RoomChatActivity.this, "Failed to remove admin role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showEditRoomDialog() {
        if (isFinishing() || isDestroyed()) return;

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        applyGlassyStyle(dialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_room, null, false);
        if (dialogView == null) return;

        ivEditRoomCoverRef = dialogView.findViewById(R.id.ivEditRoomCover);
        View btnPickEditRoomImg = dialogView.findViewById(R.id.btnPickEditRoomImg);
        View rlEditRoomCover = dialogView.findViewById(R.id.rlEditRoomCover);
        EditText etEditRoomTitle = dialogView.findViewById(R.id.etEditRoomTitle);
        ProgressBar pbEditRoomLoading = dialogView.findViewById(R.id.pbEditRoomLoading);
        View btnSaveEditRoom = dialogView.findViewById(R.id.btnSaveEditRoom);
        View btnCancelEditRoom = dialogView.findViewById(R.id.btnCancelEditRoom);

        // Reset image picker uri
        editRoomSelectedImageUri = null;

        // Load current room image
        if (ivEditRoomCoverRef != null) {
            Glide.with(this)
                    .load(roomImg != null && !roomImg.isEmpty() ? roomImg : R.drawable.logo_placeholder)
                    .placeholder(R.drawable.logo_placeholder)
                    .error(R.drawable.logo_placeholder)
                    .into(ivEditRoomCoverRef);
        }

        // Load current room title
        if (etEditRoomTitle != null) {
            etEditRoomTitle.setText(roomNameLabel != null ? roomNameLabel : "");
            etEditRoomTitle.setSelection(etEditRoomTitle.getText().length());
        }

        // Image picker click listeners
        View.OnClickListener pickImageListener = v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            editRoomImagePickerLauncher.launch(intent);
        };

        if (rlEditRoomCover != null) rlEditRoomCover.setOnClickListener(pickImageListener);
        if (btnPickEditRoomImg != null) btnPickEditRoomImg.setOnClickListener(pickImageListener);

        if (btnCancelEditRoom != null) {
            btnCancelEditRoom.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnSaveEditRoom != null) {
            btnSaveEditRoom.setOnClickListener(v -> {
                String newTitle = etEditRoomTitle != null ? etEditRoomTitle.getText().toString().trim() : "";
                if (newTitle.isEmpty()) {
                    Toast.makeText(RoomChatActivity.this, "Please enter room title", Toast.LENGTH_SHORT).show();
                    return;
                }

                btnSaveEditRoom.setEnabled(false);
                if (pbEditRoomLoading != null) pbEditRoomLoading.setVisibility(View.VISIBLE);

                if (editRoomSelectedImageUri != null) {
                    // Upload image first, then save
                    uploadRoomCoverImage(editRoomSelectedImageUri, newImgUrl -> {
                        saveRoomDetails(newTitle, newImgUrl, dialog, btnSaveEditRoom, pbEditRoomLoading);
                    });
                } else {
                    // No new image selected, keep existing roomImg
                    String currentImg = (roomImg != null && !roomImg.isEmpty()) ? roomImg : "https://i.ibb.co/Q7dp3r5h/IMG-20260705-WA0006.jpg";
                    saveRoomDetails(newTitle, currentImg, dialog, btnSaveEditRoom, pbEditRoomLoading);
                }
            });
        }

        dialog.setContentView(dialogView);
        dialog.show();
    }

    private interface OnRoomCoverUploadCallback {
        void onUploaded(String imageUrl);
    }

    private void uploadRoomCoverImage(Uri imageUri, OnRoomCoverUploadCallback callback) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                String fallback = (roomImg != null && !roomImg.isEmpty()) ? roomImg : "https://i.ibb.co/Q7dp3r5h/IMG-20260705-WA0006.jpg";
                callback.onUploaded(fallback);
                return;
            }

            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            byte[] bytes = byteBuffer.toByteArray();
            inputStream.close();

            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("key", "d909717479f29f4de1b6efc62ec33528")
                    .addFormDataPart("image", "room_cover.jpg",
                            RequestBody.create(bytes, MediaType.parse("image/*")))
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.imgbb.com/1/upload")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(RoomChatActivity.this, "Image upload failed. Keeping existing thumbnail.", Toast.LENGTH_SHORT).show();
                        String fallback = (roomImg != null && !roomImg.isEmpty()) ? roomImg : "https://i.ibb.co/Q7dp3r5h/IMG-20260705-WA0006.jpg";
                        callback.onUploaded(fallback);
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    String uploadedUrl = null;
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String responseData = response.body().string();
                            JSONObject jsonObject = new JSONObject(responseData);
                            uploadedUrl = jsonObject.getJSONObject("data").getString("url");
                        } catch (Exception ignored) {}
                    }
                    if (uploadedUrl == null || uploadedUrl.isEmpty()) {
                        uploadedUrl = (roomImg != null && !roomImg.isEmpty()) ? roomImg : "https://i.ibb.co/Q7dp3r5h/IMG-20260705-WA0006.jpg";
                    }
                    final String finalUrl = uploadedUrl;
                    runOnUiThread(() -> callback.onUploaded(finalUrl));
                }
            });

        } catch (Exception e) {
            String fallback = (roomImg != null && !roomImg.isEmpty()) ? roomImg : "https://i.ibb.co/Q7dp3r5h/IMG-20260705-WA0006.jpg";
            callback.onUploaded(fallback);
        }
    }

    private void saveRoomDetails(String newTitle, String newImgUrl, Dialog dialog, View btnSave, ProgressBar pbLoading) {
        HashMap<String, Object> updateMap = new HashMap<>();
        updateMap.put("room_name", newTitle);
        updateMap.put("img", newImgUrl);

        DatabaseReference roomsRef = FirebaseDatabase.getInstance().getReference("rooms");

        // ROOM CREATION FIX: Update room details at canonical location rooms/{roomID}
        if (roomID != null && !roomID.isEmpty()) {
            roomsRef.child(roomID).updateChildren(updateMap);
            // ROOM CREATION FIX: Clean up legacy rooms/{userID} node if distinct from roomID
            if (userID != null && !userID.isEmpty() && !userID.equals(roomID)) {
                roomsRef.child(userID).removeValue(); // ROOM CREATION FIX
            }
        }

        // 3. Find any matching room node by roomId in rooms list
        roomsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        RoomModel r = child.getValue(RoomModel.class);
                        if (r != null && roomID != null && roomID.equals(r.getRoomId())) {
                            child.getRef().updateChildren(updateMap);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Update local state & UI
        roomNameLabel = newTitle;
        roomImg = newImgUrl;

        TextView tvRoomName = findViewById(R.id.tvRoomName);
        if (tvRoomName != null) {
            tvRoomName.setText(newTitle);
        }

        ImageView ivRoomAvatar = findViewById(R.id.ivRoomAvatar);
        if (ivRoomAvatar != null) {
            Glide.with(RoomChatActivity.this)
                    .load(newImgUrl)
                    .placeholder(R.drawable.logo_placeholder)
                    .error(R.drawable.logo_placeholder)
                    .into(ivRoomAvatar);
        }

        if (backgroundView != null) {
            backgroundView.setRoomName(newTitle);
        }

        if (pbLoading != null) pbLoading.setVisibility(View.GONE);
        if (btnSave != null) btnSave.setEnabled(true);
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        Toast.makeText(RoomChatActivity.this, "Room Details Updated! 🎉", Toast.LENGTH_SHORT).show();
    }

    private Dialog exitOptionsDialog;

    private void showExitOptionsDialog() {
        if (isFinishing() || isDestroyed()) return;
        if (exitOptionsDialog != null && exitOptionsDialog.isShowing()) return;
        Dialog dialog = new Dialog(this);
        exitOptionsDialog = dialog;
        dialog.setOnDismissListener(d -> {
            if (exitOptionsDialog == d) exitOptionsDialog = null;
        });
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_exit_room, null, false);
        if (dialogView == null) return;
        dialog.setContentView(dialogView);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            dialog.getWindow().setDimAmount(0.65f);
        }

        View btnMinimize = dialogView.findViewById(R.id.btnDialogMinimize);
        View btnExit = dialogView.findViewById(R.id.btnDialogExit);

        if (btnMinimize != null) {
            btnMinimize.setOnClickListener(v -> {
                dialog.dismiss();
                minimizeRoom(); // ROOM MINIMIZE FIX
            });
        }

        if (btnExit != null) {
            btnExit.setOnClickListener(v -> {
                dialog.dismiss();
                leaveRoom();
            });
        }

        dialog.show();
    }

    // ROOM MINIMIZE FIX
    private boolean isMinimized = false;
    private boolean isRoomLeft = false;

    // ROOM MINIMIZE FIX
    public void minimizeRoom() {
        isMinimized = true;
        RoomFloatingManager.getInstance().showFloatingBubble(this, roomID, roomNameLabel, roomImg);
        
        try {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        } catch (Exception e) {
            moveTaskToBack(true);
        }
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_bottom);
    }

    // SEAT LIMIT SYNC FIX
    private void updateRoomSeatCapacity(int newCount) {
        if (newCount != 9 && newCount != 17 && newCount != 21) return;
        SeatManager.getInstance().setTotalSeats(newCount);
        if (roomID != null && !roomID.trim().isEmpty()) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("rooms").child(roomID);
            Map<String, Object> map = new HashMap<>();
            map.put("seatCount", newCount);
            ref.updateChildren(map);
        }
    }

    // SEAT SYSTEM FIX
    private void showSeatCapacityDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        applyGlassyStyle(dialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_seat_capacity, null, false);
        if (dialogView == null) return;

        View cardOption9 = dialogView.findViewById(R.id.cardOption9Seats);
        View cardOption17 = dialogView.findViewById(R.id.cardOption17Seats);
        View cardOption21 = dialogView.findViewById(R.id.cardOption21Seats);

        View.OnClickListener select9 = v -> {
            long now = SystemClock.elapsedRealtime();
            if (now - lastSeatLimitClickTime < 1000) return;
            lastSeatLimitClickTime = now;

            if (!isAdminOrHost()) {
                Toast.makeText(this, "Only Room Host or Administrators can change seat capacity!", Toast.LENGTH_SHORT).show();
                return;
            }
            updateRoomSeatCapacity(9);
            Toast.makeText(this, "Room Seat Capacity updated to 9 Seats Grid!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        };

        View.OnClickListener select17 = v -> {
            long now = SystemClock.elapsedRealtime();
            if (now - lastSeatLimitClickTime < 1000) return;
            lastSeatLimitClickTime = now;

            if (!isAdminOrHost()) {
                Toast.makeText(this, "Only Room Host or Administrators can change seat capacity!", Toast.LENGTH_SHORT).show();
                return;
            }
            updateRoomSeatCapacity(17);
            Toast.makeText(this, "Room Seat Capacity updated to 17 Seats Grid!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        };

        View.OnClickListener select21 = v -> {
            long now = SystemClock.elapsedRealtime();
            if (now - lastSeatLimitClickTime < 1000) return;
            lastSeatLimitClickTime = now;

            if (!isAdminOrHost()) {
                Toast.makeText(this, "Only Room Host or Administrators can change seat capacity!", Toast.LENGTH_SHORT).show();
                return;
            }
            updateRoomSeatCapacity(21);
            Toast.makeText(this, "Room Seat Capacity updated to 21 Seats Grid!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        };

        if (cardOption9 != null) cardOption9.setOnClickListener(select9);
        if (cardOption17 != null) cardOption17.setOnClickListener(select17);
        if (cardOption21 != null) cardOption21.setOnClickListener(select21);

        dialog.setContentView(dialogView);
        dialog.show();
    }

    private void registerOnlineUser() {
        if (roomID == null || userID == null) return;
        roomOnlineUsersRef = FirebaseDatabase.getInstance()
                .getReference("room_users")
                .child(roomID)
                .child(userID);

        Map<String, Object> map = new HashMap<>();
        map.put("userId", userID);
        map.put("userName", userName != null ? userName : "User");
        String avatar = SessionManager.getInstance(this).getAvatar();
        map.put("userAvatar", avatar != null ? avatar : "");
        map.put("isHost", isHost);
        map.put("joinedAt", System.currentTimeMillis());

        roomOnlineUsersRef.setValue(map);
        roomOnlineUsersRef.onDisconnect().removeValue();
    }

    private void unregisterOnlineUser() {
        if (roomOnlineUsersRef != null) {
            roomOnlineUsersRef.removeValue();
        } else if (roomID != null && userID != null) {
            FirebaseDatabase.getInstance()
                    .getReference("room_users")
                    .child(roomID)
                    .child(userID)
                    .removeValue();
        }
    }

    public void sendSeatInvitation(String targetUserId, String targetUserName, int seatIndex) {
        if (targetUserId == null || targetUserId.trim().isEmpty()) return;

        // Prevent self-invitation: No user (Admin/Host/Member) can invite themselves
        if (userID != null && userID.equals(targetUserId.trim())) {
            Toast.makeText(RoomChatActivity.this, "You cannot invite yourself to a seat! 🎙️", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference inviteRef = FirebaseDatabase.getInstance()
                .getReference("room_seat_invitations")
                .child(roomID)
                .child(targetUserId);

        Map<String, Object> map = new HashMap<>();
        map.put("invitationId", inviteRef.push().getKey());
        map.put("roomId", roomID);
        map.put("roomName", roomNameLabel != null ? roomNameLabel : "Audio Room");
        map.put("hostId", userID);
        map.put("hostName", userName != null ? userName : "Host");
        map.put("targetUserId", targetUserId);
        map.put("targetUserName", targetUserName);
        map.put("seatIndex", seatIndex);
        map.put("status", "PENDING");
        map.put("timestamp", System.currentTimeMillis());

        inviteRef.setValue(map).addOnSuccessListener(aVoid -> {
            Toast.makeText(RoomChatActivity.this, "Invitation sent to " + targetUserName + " 🎙️", Toast.LENGTH_SHORT).show();
            NotificationHelper.sendRoomInviteNotification(targetUserId, roomID);
        }).addOnFailureListener(e -> {
            Toast.makeText(RoomChatActivity.this, "Failed to send invitation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSeatInviteListener() {
        if (roomID == null || userID == null) return;
        mySeatInviteRef = FirebaseDatabase.getInstance()
                .getReference("room_seat_invitations")
                .child(roomID)
                .child(userID);

        mySeatInviteListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;
                if (!snapshot.exists()) return;

                String status = snapshot.child("status").getValue(String.class);
                if (!"PENDING".equalsIgnoreCase(status)) return;

                Long timestamp = snapshot.child("timestamp").getValue(Long.class);
                if (timestamp != null && (System.currentTimeMillis() - timestamp > 120000)) {
                    return;
                }

                String hostName = snapshot.child("hostName").getValue(String.class);
                Integer seatIndex = snapshot.child("seatIndex").getValue(Integer.class);
                int targetSeat = seatIndex != null ? seatIndex : -1;

                if (SeatManager.getInstance().findUserSeatIndex(userID) != -1) {
                    return;
                }

                showSeatInvitationDialog(hostName != null ? hostName : "Host", targetSeat);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        mySeatInviteRef.addValueEventListener(mySeatInviteListener);
    }

    private void showSeatInvitationDialog(String hostName, int targetSeat) {
        if (isFinishing() || isDestroyed()) return;
        if (currentInviteDialog != null && currentInviteDialog.isShowing()) {
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        applyGlassyStyle(dialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_seat_invitation, null, false);
        if (dialogView == null) return;

        TextView tvInviteTitle = dialogView.findViewById(R.id.tvInviteTitle);
        TextView tvInviteMessage = dialogView.findViewById(R.id.tvInviteMessage);
        View btnAccept = dialogView.findViewById(R.id.btnAcceptInvitation);
        View btnDecline = dialogView.findViewById(R.id.btnDeclineInvitation);

        String seatMsg = (targetSeat >= 0) ? ("Seat " + (targetSeat + 1)) : "a Seat";
        if (tvInviteTitle != null) {
            tvInviteTitle.setText("Seat Invitation");
        }
        if (tvInviteMessage != null) {
            tvInviteMessage.setText("Host " + hostName + " has invited you to take " + seatMsg + " in the room! Would you like to join the mic?");
        }

        if (btnAccept != null) {
            btnAccept.setOnClickListener(v -> {
                dialog.dismiss();
                currentInviteDialog = null;
                acceptSeatInvitation(targetSeat);
            });
        }

        if (btnDecline != null) {
            btnDecline.setOnClickListener(v -> {
                dialog.dismiss();
                currentInviteDialog = null;
                declineSeatInvitation();
            });
        }

        dialog.setCancelable(false);
        dialog.setContentView(dialogView);
        dialog.setOnDismissListener(d -> currentInviteDialog = null);
        currentInviteDialog = dialog;
        dialog.show();
    }

    private void acceptSeatInvitation(int targetSeat) {
        if (mySeatInviteRef != null) {
            mySeatInviteRef.child("status").setValue("ACCEPTED");
        }

        int seatToTake = targetSeat;
        List<SeatModel> seats = SeatManager.getInstance().getSeats();

        // Exact Target Seat Priority: If the target invited seat is valid and empty (even if locked), take that exact seat!
        if (seatToTake < 0 || seatToTake >= seats.size() || !seats.get(seatToTake).isEmpty()) {
            // Fallback: If target seat is occupied or invalid, find first open unlocked seat
            seatToTake = -1;
            for (SeatModel seat : seats) {
                if (seat != null && seat.isEmpty() && !seat.isClosed) {
                    if (seat.index == 0 && !isHost) continue;
                    seatToTake = seat.index;
                    break;
                }
            }
        }

        if (seatToTake == -1) {
            Toast.makeText(this, "Sorry, the invited seat is no longer available!", Toast.LENGTH_SHORT).show();
            return;
        }

        final int finalSeatToTake = seatToTake;
        String myAvatar = SessionManager.getInstance(this).getAvatar();
        UserProfileCache.getUserProfile(userID, profile -> {
            String frame = (profile != null && profile.equippedFrame != null) ? profile.equippedFrame : "";
            // Allow taking locked seat because Admin/Host explicitly invited the user to this seat
            boolean success = SeatManager.getInstance().takeSeat(finalSeatToTake, userID, userName, myAvatar, frame, true);
            if (success) {
                ZegoManager.getInstance().startPublishing();
                Toast.makeText(this, "You accepted the invitation and took Seat " + (finalSeatToTake + 1) + "! 🎙️", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Could not take seat. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void declineSeatInvitation() {
        if (mySeatInviteRef != null) {
            mySeatInviteRef.child("status").setValue("DECLINED");
        }
        Toast.makeText(this, "Invitation declined", Toast.LENGTH_SHORT).show();
    }

    public String getUserID() {
        return userID;
    }

    private void showRoomMembersDialog() {
        showRoomMembersDialog(-1);
    }

    private void showRoomMembersDialog(int targetSeatIndex) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        applyGlassyStyle(dialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_room_members, null, false);
        if (dialogView == null) return;

        TextView tvMembersCountBadge = dialogView.findViewById(R.id.tvMembersCountBadge);
        TextView tvMembersDialogTitle = dialogView.findViewById(R.id.tvMembersDialogTitle);
        EditText etSearchMembers = dialogView.findViewById(R.id.etSearchMembers);
        RecyclerView rvMembers = dialogView.findViewById(R.id.rvRoomMembers);

        List<User> activeUsers = new ArrayList<>();
        Map<String, User> userMap = new HashMap<>();

        List<SeatModel> seats = SeatManager.getInstance().getSeats();
        for (SeatModel seat : seats) {
            if (seat != null && !seat.isEmpty()) {
                if (targetSeatIndex >= 0 && userID != null && userID.equals(seat.userID)) continue; // Do not invite self when targeting seat
                User u = new User();
                u.setUserId(seat.userID);
                u.setUserName(seat.userName != null && !seat.userName.isEmpty() ? seat.userName : "Member");
                u.setUserIcon(seat.userAvatar);
                if (seat.index == 0) u.setHost(true);
                userMap.put(seat.userID, u);
                activeUsers.add(u);
            }
        }

        DatabaseReference onlineRef = FirebaseDatabase.getInstance().getReference("room_users").child(roomID);
        onlineRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;
                for (DataSnapshot child : snapshot.getChildren()) {
                    String uid = child.child("userId").getValue(String.class);
                    if (uid == null) uid = child.getKey();
                    if (targetSeatIndex >= 0 && uid != null && uid.equals(userID)) continue; // Do not invite self when targeting seat
                    if (uid != null && !userMap.containsKey(uid)) {
                        String name = child.child("userName").getValue(String.class);
                        String avatar = child.child("userAvatar").getValue(String.class);
                        Boolean hostFlag = child.child("isHost").getValue(Boolean.class);

                        User u = new User();
                        u.setUserId(uid);
                        u.setUserName(name != null ? name : "Member");
                        u.setUserIcon(avatar);
                        if (hostFlag != null) u.setHost(hostFlag);
                        userMap.put(uid, u);
                        activeUsers.add(u);
                    }
                }

                // If viewing connected members (targetSeatIndex < 0), ensure current user is added if missing
                String myAvatar = SessionManager.getInstance(RoomChatActivity.this).getAvatar();
                if (targetSeatIndex < 0 && userID != null && !userMap.containsKey(userID)) {
                    User u = new User();
                    u.setUserId(userID);
                    u.setUserName(userName != null && !userName.isEmpty() ? userName : "Member");
                    u.setUserIcon(myAvatar);
                    u.setHost(isAdminOrHost());
                    userMap.put(userID, u);
                    activeUsers.add(0, u);
                }

                // Ensure Host is included if missing
                String effectiveHostUid = (SeatManager.getInstance().getHostUserID() != null && !SeatManager.getInstance().getHostUserID().isEmpty())
                        ? SeatManager.getInstance().getHostUserID() : (isHost ? userID : null);
                if (targetSeatIndex < 0 && effectiveHostUid != null && !userMap.containsKey(effectiveHostUid)) {
                    User u = new User();
                    u.setUserId(effectiveHostUid);
                    u.setUserName("Host");
                    u.setHost(true);
                    userMap.put(effectiveHostUid, u);
                    activeUsers.add(0, u);
                }

                if (activeUsers.isEmpty() && userID != null) {
                    User u = new User();
                    u.setUserId(userID);
                    u.setUserName(userName != null ? userName : "Host");
                    u.setUserIcon(myAvatar);
                    u.setHost(isAdminOrHost());
                    activeUsers.add(u);
                }

                int totalCount = activeUsers.size();
                if (tvMembersCountBadge != null) {
                    tvMembersCountBadge.setText(totalCount + " Online");
                }
                if (tvMembersDialogTitle != null) {
                    tvMembersDialogTitle.setText("Connected Room Members (" + totalCount + ")");
                }

                if (rvMembers != null) {
                    rvMembers.setLayoutManager(new LinearLayoutManager(RoomChatActivity.this));
                    AudienceAdapter adapter = new AudienceAdapter(activeUsers, isAdminOrHost(), targetUser -> {
                        sendSeatInvitation(targetUser.getUserId(), targetUser.getUserName(), targetSeatIndex);
                        dialog.dismiss();
                    });
                    rvMembers.setAdapter(adapter);

                    // Real-Time Search Filtering for Room Members / Invitation
                    if (etSearchMembers != null) {
                        etSearchMembers.addTextChangedListener(new TextWatcher() {
                            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                String query = s.toString().trim().toLowerCase();
                                List<User> filtered = new ArrayList<>();
                                for (User u : activeUsers) {
                                    String name = u.getUserName() != null ? u.getUserName().toLowerCase() : "";
                                    String uid = u.getUserId() != null ? u.getUserId().toLowerCase() : "";
                                    if (name.contains(query) || uid.contains(query)) {
                                        filtered.add(u);
                                    }
                                }
                                adapter.updateList(filtered);
                            }
                            @Override public void afterTextChanged(Editable s) {}
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        dialog.setContentView(dialogView);
        dialog.show();
    }

    private BottomSheetDialog themeSelectionDialog;

    private void showThemeSelectionDialog() {
        if (themeSelectionDialog != null && themeSelectionDialog.isShowing()) return;
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        themeSelectionDialog = dialog;
        dialog.setOnDismissListener(d -> {
            if (themeSelectionDialog == d) themeSelectionDialog = null;
        });
        applyGlassyStyle(dialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_theme_selection, null, false);
        if (dialogView == null) return;

        // Theme 1: Cyber Neon Party (Free - 0 Coins)
        View cardCyber = dialogView.findViewById(R.id.cardThemeCyber);
        View tvUseCyber = dialogView.findViewById(R.id.tvUseCyber);
        View imgPosterTheme1 = dialogView.findViewById(R.id.imgPosterTheme1);
        View.OnClickListener applyCyber = v -> {
            if (backgroundView != null) {
                backgroundView.setThemeVideo("theme/theme1.mp4");
            }
            syncThemeToFirebase("VIDEO", "theme/theme1.mp4");
            dialog.dismiss();
        };
        if (cardCyber != null) cardCyber.setOnClickListener(applyCyber);
        if (tvUseCyber != null) tvUseCyber.setOnClickListener(applyCyber);
        if (imgPosterTheme1 != null) imgPosterTheme1.setOnClickListener(applyCyber);

        // Theme 2: Galaxy (50 Coins)
        View cardGalaxy = dialogView.findViewById(R.id.cardThemeGalaxy);
        View tvUseGalaxy = dialogView.findViewById(R.id.tvUseGalaxy);
        View imgPosterTheme2 = dialogView.findViewById(R.id.imgPosterTheme2);
        View.OnClickListener applyTheme2 = v -> {
            WalletManager.spendCoinsForGift(userID, null, 50, "Theme: Galaxy", new WalletManager.WalletCallback() {
                @Override
                public void onSuccess(String message, long newCoinBalance) {
                    // ROOM CONTRIBUTION TRACKING: Atomically increment total & user contribution
                    if (roomID != null && !roomID.trim().isEmpty()) {
                        WalletManager.trackRoomContribution(roomID, userID, userName, SessionManager.getInstance(RoomChatActivity.this).getAvatar(), 50);
                    }
                    if (backgroundView != null) {
                        backgroundView.setThemeVideo("theme/theme2.mp4");
                    }
                    syncThemeToFirebase("VIDEO", "theme/theme2.mp4");
                    dialog.dismiss();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(RoomChatActivity.this, "❌ " + error, Toast.LENGTH_LONG).show();
                }
            });
        };
        if (cardGalaxy != null) cardGalaxy.setOnClickListener(applyTheme2);
        if (tvUseGalaxy != null) tvUseGalaxy.setOnClickListener(applyTheme2);
        if (imgPosterTheme2 != null) imgPosterTheme2.setOnClickListener(applyTheme2);

        // Theme 3: Sunset (100 Coins)
        View cardSunset = dialogView.findViewById(R.id.cardThemeSunset);
        View tvUseSunset = dialogView.findViewById(R.id.tvUseSunset);
        View imgPosterTheme3 = dialogView.findViewById(R.id.imgPosterTheme3);
        View.OnClickListener applyTheme3 = v -> {
            WalletManager.spendCoinsForGift(userID, null, 100, "Theme: Sunset", new WalletManager.WalletCallback() {
                @Override
                public void onSuccess(String message, long newCoinBalance) {
                    // ROOM CONTRIBUTION TRACKING: Atomically increment total & user contribution
                    if (roomID != null && !roomID.trim().isEmpty()) {
                        WalletManager.trackRoomContribution(roomID, userID, userName, SessionManager.getInstance(RoomChatActivity.this).getAvatar(), 100);
                    }
                    if (backgroundView != null) {
                        backgroundView.setThemeVideo("theme/theme3.mp4");
                    }
                    syncThemeToFirebase("VIDEO", "theme/theme3.mp4");
                    dialog.dismiss();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(RoomChatActivity.this, "❌ " + error, Toast.LENGTH_LONG).show();
                }
            });
        };
        if (cardSunset != null) cardSunset.setOnClickListener(applyTheme3);
        if (tvUseSunset != null) tvUseSunset.setOnClickListener(applyTheme3);
        if (imgPosterTheme3 != null) imgPosterTheme3.setOnClickListener(applyTheme3);

        // Theme 4: Aurora Glass (150 Coins)
        View cardAurora = dialogView.findViewById(R.id.cardThemeAurora);
        View tvUseAurora = dialogView.findViewById(R.id.tvUseAurora);
        View.OnClickListener applyAurora = v -> {
            WalletManager.spendCoinsForGift(userID, null, 150, "Theme: Aurora", new WalletManager.WalletCallback() {
                @Override
                public void onSuccess(String message, long newCoinBalance) {
                    // ROOM CONTRIBUTION TRACKING: Atomically increment total & user contribution
                    if (roomID != null && !roomID.trim().isEmpty()) {
                        WalletManager.trackRoomContribution(roomID, userID, userName, SessionManager.getInstance(RoomChatActivity.this).getAvatar(), 150);
                    }
                    if (backgroundView != null) {
                        backgroundView.setThemeImage(R.drawable.bg_main_gradient);
                    }
                    syncThemeToFirebase("IMAGE", "bg_main_gradient");
                    dialog.dismiss();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(RoomChatActivity.this, "❌ " + error, Toast.LENGTH_LONG).show();
                }
            });
        };
        if (cardAurora != null) cardAurora.setOnClickListener(applyAurora);
        if (tvUseAurora != null) tvUseAurora.setOnClickListener(applyAurora);

        dialog.setContentView(dialogView);
        dialog.show();
    }

    private BottomSheetDialog commentInputDialog;

    private void showCommentInputDialog() {
        showCommentInputDialogWithText(null);
    }

    private void showCommentInputDialogWithText(String initialText) {
        if (commentInputDialog != null && commentInputDialog.isShowing()) return;
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        commentInputDialog = dialog;
        dialog.setOnDismissListener(d -> {
            if (commentInputDialog == d) commentInputDialog = null;
        });
        applyGlassyStyle(dialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_comment_input, null, false);
        if (dialogView == null) return;

        EditText etInput = dialogView.findViewById(R.id.etCommentInput);
        if (etInput != null && initialText != null && !initialText.trim().isEmpty()) {
            etInput.setText(initialText);
            etInput.setSelection(initialText.length());
        }

        View btnSend = dialogView.findViewById(R.id.btnSendComment);

        // Quick Phrase Chips
        int[] chipIds = {R.id.chipHi, R.id.chipWelcome, R.id.chipWave, R.id.chipRose, R.id.chipClap};
        for (int id : chipIds) {
            View chip = dialogView.findViewById(id);
            if (chip instanceof TextView) {
                chip.setOnClickListener(v -> {
                    String chipText = ((TextView) chip).getText().toString();
                    sendUserComment(chipText);
                    dialog.dismiss();
                });
            }
        }

        // Send Button Click
        if (btnSend != null) {
            btnSend.setOnClickListener(v -> {
                if (etInput != null) {
                    String text = etInput.getText().toString().trim();
                    if (!text.isEmpty()) {
                        sendUserComment(text);
                        dialog.dismiss();
                    }
                }
            });
        }

        // Keyboard Action Send
        if (etInput != null) {
            etInput.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    String text = etInput.getText().toString().trim();
                    if (!text.isEmpty()) {
                        sendUserComment(text);
                        dialog.dismiss();
                    }
                    return true;
                }
                return false;
            });
        }

        dialog.setContentView(dialogView);
        dialog.show();
    }

    private void sendUserComment(String text) {
        if (text == null || text.trim().isEmpty()) return;

        if (roomMessagesRef != null) {
            String myAvatar = SessionManager.getInstance(this).getAvatar();
            Map<String, Object> msgMap = new HashMap<>();
            msgMap.put("senderId", userID != null ? userID : "");
            msgMap.put("senderName", userName != null ? userName : "User");
            msgMap.put("senderAvatar", myAvatar != null ? myAvatar : "");
            msgMap.put("text", text.trim());
            msgMap.put("timestamp", System.currentTimeMillis());
            roomMessagesRef.push().setValue(msgMap);
        }
    }

    private void showMessageCenterDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        applyGlassyStyle(dialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_message_center, null, false);
        if (dialogView != null) {
            dialog.setContentView(dialogView);
            dialog.show();
        }
    }

    private boolean isFastClick(View v) {
        if (v == null) return false;
        long currentTime = SystemClock.elapsedRealtime();
        Object tag = v.getTag(R.id.tvRoomName);
        if (tag instanceof Long) {
            long lastTime = (Long) tag;
            if (currentTime - lastTime < 400) {
                return true;
            }
        }
        v.setTag(R.id.tvRoomName, currentTime);
        return false;
    }

    private String selectedGiftSvga = "gift/aladdin.svga";
    private BottomSheetDialog giftBottomSheetDialog;

    private void showGiftDialog() {
        showGiftDialog(null);
    }

    private void showGiftDialog(String targetUid) {
        if (giftBottomSheetDialog != null && giftBottomSheetDialog.isShowing()) {
            return;
        }
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        giftBottomSheetDialog = dialog;
        dialog.setOnDismissListener(d -> {
            if (giftBottomSheetDialog == d) {
                giftBottomSheetDialog = null;
            }
        });
        applyGlassyStyle(dialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_gift_store, null, false);
        if (dialogView == null) return;

        RecyclerView rvGiftRecipients = dialogView.findViewById(R.id.rvGiftRecipients);
        RecyclerView rvGifts = dialogView.findViewById(R.id.rvGifts);
        View btnSendAction = dialogView.findViewById(R.id.btnSendGiftAction);
        TabLayout tabCategory = dialogView.findViewById(R.id.tabCategoryGifts);
        TextView tvGiftDialogCoins = dialogView.findViewById(R.id.tvGiftDialogCoins);
        View llCoinBalance = dialogView.findViewById(R.id.llCoinBalance);

        if (tvGiftDialogCoins != null) {
            WalletManager.getUserCoins(userID, balance -> runOnUiThread(() ->
                    tvGiftDialogCoins.setText(String.valueOf(balance))
            ));
        }

        if (llCoinBalance != null) {
            llCoinBalance.setOnClickListener(v -> {
                startActivity(new Intent(RoomChatActivity.this, CoinRechargeActivity.class));
            });
        }

        // Setup Recipients List (Room members / Mic seats)
        List<GiftRecipientModel> recipientList = new ArrayList<>();
        Set<String> addedUids = new HashSet<>();

        // Self-gifting check: Do not target self
        if (targetUid != null && targetUid.equals(userID)) {
            targetUid = null;
        }

        boolean isTargeted = (targetUid != null && !targetUid.isEmpty());
        boolean autoSelectAll = !isTargeted;

        // "ALL" option (Auto-selected by default)
        GiftRecipientModel allItem = new GiftRecipientModel("", "All", "", "ALL", true, true);
        recipientList.add(allItem);

        // 1. Host Info (Room Host / Admin - Default Selected)
        String roomHostUid = SeatManager.getInstance().getHostUserID();
        if (roomHostUid == null || roomHostUid.isEmpty()) {
            roomHostUid = getIntent().getStringExtra("uid");
        }

        List<SeatModel> seats = SeatManager.getInstance().getSeats();

        if (roomHostUid != null && !roomHostUid.isEmpty() && !roomHostUid.equals(userID)) {
            String hName = "Host";
            String hAvatar = "";
            String hFrame = "";

            if (seats != null) {
                for (SeatModel seat : seats) {
                    if (seat != null && !seat.isEmpty() && roomHostUid.equals(seat.userID)) {
                        if (seat.userName != null && !seat.userName.isEmpty()) hName = seat.userName;
                        if (seat.userAvatar != null && !seat.userAvatar.isEmpty()) hAvatar = seat.userAvatar;
                        if (seat.equippedFrame != null) hFrame = seat.equippedFrame;
                        break;
                    }
                }
            }

            // Room Host / Admin is default SELECTED until manually unselected by user
            boolean selectHost = true;
            recipientList.add(new GiftRecipientModel(roomHostUid, hName, hAvatar, hFrame, "Host", false, selectHost));
            addedUids.add(roomHostUid);
        }

        // 2. Seated Members (Exclude self - Default Selected)
        if (seats != null) {
            for (SeatModel seat : seats) {
                if (seat != null && !seat.isEmpty() && !seat.userID.equals(userID) && !addedUids.contains(seat.userID)) {
                    String seatBadge = seat.isHost() ? "Host" : String.valueOf(seat.index + 1);
                    String uName = (seat.userName != null && !seat.userName.isEmpty()) ? seat.userName : "Member";
                    boolean selectSeat = true;
                    recipientList.add(new GiftRecipientModel(seat.userID, uName, seat.userAvatar, seat.equippedFrame, seatBadge, false, selectSeat));
                    addedUids.add(seat.userID);
                }
            }
        }

        GiftRecipientAdapter recipientAdapter = new GiftRecipientAdapter(recipientList);
        if (rvGiftRecipients != null) {
            rvGiftRecipients.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvGiftRecipients.setNestedScrollingEnabled(false);
            rvGiftRecipients.setAdapter(recipientAdapter);
        }

        List<GiftStoreItem> giftList = GiftCatalog.getAllGifts();

        String[] categories = new String[]{"Gift", "Relationship", "Nation Flag", "Luxury", "Customization"};

        List<GiftStoreItem> initialCategoryGifts = new ArrayList<>();
        for (GiftStoreItem item : giftList) {
            if ("Gift".equalsIgnoreCase(item.category)) {
                initialCategoryGifts.add(item);
            }
        }
        if (initialCategoryGifts.isEmpty()) {
            initialCategoryGifts.addAll(giftList);
        }

        TextView tvSendActionText = dialogView.findViewById(R.id.tvSendActionText);

        // Quantity Selection Chips
        final int[] selectedQuantity = new int[]{1};
        TextView chipQty1 = dialogView.findViewById(R.id.chipQty1);
        TextView chipQty7 = dialogView.findViewById(R.id.chipQty7);
        TextView chipQty77 = dialogView.findViewById(R.id.chipQty77);
        TextView chipQty777 = dialogView.findViewById(R.id.chipQty777);

        View.OnClickListener qtyClickListener = v -> {
            if (chipQty1 != null) chipQty1.setBackgroundResource(R.drawable.bg_room_chat_pill);
            if (chipQty7 != null) chipQty7.setBackgroundResource(R.drawable.bg_room_chat_pill);
            if (chipQty77 != null) chipQty77.setBackgroundResource(R.drawable.bg_room_chat_pill);
            if (chipQty777 != null) chipQty777.setBackgroundResource(R.drawable.bg_room_chat_pill);

            v.setBackgroundResource(R.drawable.bg_send_button_glow);
            int qty = 1;
            if (v.getId() == R.id.chipQty7) qty = 7;
            else if (v.getId() == R.id.chipQty77) qty = 77;
            else if (v.getId() == R.id.chipQty777) qty = 777;

            selectedQuantity[0] = qty;
            if (tvSendActionText != null) {
                tvSendActionText.setText(qty > 1 ? "Send (x" + qty + ")" : "Send");
            }
        };

        if (chipQty1 != null) chipQty1.setOnClickListener(qtyClickListener);
        if (chipQty7 != null) chipQty7.setOnClickListener(qtyClickListener);
        if (chipQty77 != null) chipQty77.setOnClickListener(qtyClickListener);
        if (chipQty777 != null) chipQty777.setOnClickListener(qtyClickListener);

        if (rvGifts != null) {
            rvGifts.setLayoutManager(new GridLayoutManager(this, 4));
            rvGifts.setHasFixedSize(true);
            rvGifts.setItemViewCacheSize(20);
            rvGifts.setNestedScrollingEnabled(true);
            GiftStoreAdapter adapter = new GiftStoreAdapter(initialCategoryGifts);
            rvGifts.setAdapter(adapter);

            // All Relationship Gifts unlocked by default
            adapter.setRelationshipUnlocked(true);

            adapter.setOnGiftSelectedListener((item, position, isReSelected) -> {
                if (isReSelected) {
                    selectedQuantity[0]++;
                } else {
                    selectedQuantity[0] = 1;
                }

                int qty = selectedQuantity[0];
                if (chipQty1 != null) chipQty1.setBackgroundResource(qty == 1 ? R.drawable.bg_send_button_glow : R.drawable.bg_room_chat_pill);
                if (chipQty7 != null) chipQty7.setBackgroundResource(qty == 7 ? R.drawable.bg_send_button_glow : R.drawable.bg_room_chat_pill);
                if (chipQty77 != null) chipQty77.setBackgroundResource(qty == 77 ? R.drawable.bg_send_button_glow : R.drawable.bg_room_chat_pill);
                if (chipQty777 != null) chipQty777.setBackgroundResource(qty == 777 ? R.drawable.bg_send_button_glow : R.drawable.bg_room_chat_pill);

                if (tvSendActionText != null) {
                    tvSendActionText.setText(qty > 1 ? "Send (x" + qty + ")" : "Send");
                }
            });

            if (tabCategory != null) {
                tabCategory.removeAllTabs();
                for (String cat : categories) {
                    tabCategory.addTab(tabCategory.newTab().setText(cat));
                }

                tabCategory.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        String selectedCategory = categories[tab.getPosition()];
                        List<GiftStoreItem> filtered = new ArrayList<>();
                        for (GiftStoreItem item : giftList) {
                            if (item.category.equalsIgnoreCase(selectedCategory)) {
                                filtered.add(item);
                            }
                        }
                        if (filtered.isEmpty()) {
                            filtered.addAll(giftList);
                        }
                        adapter.updateItems(filtered);
                        rvGifts.scrollToPosition(0);
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {}

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {}
                });
            }

            if (btnSendAction != null) {
                final boolean[] isSendingGift = new boolean[]{false};
                btnSendAction.setOnClickListener(v -> {
                    // Prevent duplicate rapid click execution if processing or button disabled
                    if (isSendingGift[0] || !btnSendAction.isEnabled()) {
                        return;
                    }

                    GiftStoreItem selectedItem = adapter.getSelectedGift();
                    if (selectedItem == null) {
                        Toast.makeText(RoomChatActivity.this, "Please select a gift first! 🎁", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<GiftRecipientModel> selectedRecipients = recipientAdapter.getSelectedRecipients();
                    if (selectedRecipients.isEmpty()) {
                        Toast.makeText(RoomChatActivity.this, "Please select at least one recipient! 👤", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // GIFT COIN/ENERGY FIX
                    // Immediately lock button and set processing flag
                    isSendingGift[0] = true;
                    btnSendAction.setEnabled(false);

                    long singleCost = selectedItem.cost;
                    int quantity = selectedQuantity[0];
                    String giftName = selectedItem.name;
                    selectedGiftSvga = selectedItem.svgaPath;

                    // Generate unique gift transaction ID BEFORE transaction execution
                    String uniqueGiftId = userID + "_" + System.currentTimeMillis() + "_" + (new Random().nextInt(9000) + 1000);

                    boolean isAllSelected = recipientAdapter.isAllSelected();
                    List<String> targetUidsList = null;
                    if (!isAllSelected && !selectedRecipients.isEmpty()) {
                        targetUidsList = new ArrayList<>();
                        for (GiftRecipientModel model : selectedRecipients) {
                            if (!model.isAll() && model.getUid() != null && !model.getUid().trim().isEmpty()) {
                                targetUidsList.add(model.getUid().trim());
                            }
                        }
                        if (targetUidsList.isEmpty()) {
                            targetUidsList = null;
                        }
                    }

                    final List<String> targetUids = targetUidsList;
                    boolean isRoomGift = (targetUids == null || targetUids.isEmpty());
                    int recipientCount = isRoomGift ? 1 : targetUids.size();
                    long totalCost = EconomyConfig.calculateTotalCost(singleCost, quantity, recipientCount, isRoomGift);

                    List<String> activeRoomMemberUids = getActiveRoomMemberUids();

                    try {
                        WalletManager.processGiftTransaction(
                            uniqueGiftId,
                            roomID,
                            userID,
                            targetUids,
                            totalCost,
                            giftName,
                            activeRoomMemberUids,
                            new WalletManager.WalletCallback() {
                                @Override
                                public void onSuccess(String message, long newCoinBalance) {
                                    // Unlock button after operation completes successfully
                                    isSendingGift[0] = false;
                                    btnSendAction.setEnabled(true);
                                    if (tvGiftDialogCoins != null) {
                                        tvGiftDialogCoins.setText(String.valueOf(newCoinBalance));
                                    }

                                    StringBuilder recipientNames = new StringBuilder();
                                    if (isRoomGift) {
                                        recipientNames.append("All Members");
                                    } else {
                                        for (int i = 0; i < selectedRecipients.size(); i++) {
                                            GiftRecipientModel recipient = selectedRecipients.get(i);
                                            if (i > 0) recipientNames.append(", ");
                                            recipientNames.append(recipient.getName());
                                        }
                                    }

                                    if (roomGiftsRef != null) {
                                        Map<String, Object> giftData = new HashMap<>();
                                        giftData.put("giftId", uniqueGiftId);
                                        giftData.put("senderName", userName != null ? userName : "User");
                                        giftData.put("senderAvatar", SessionManager.getInstance(RoomChatActivity.this).getAvatar());
                                        giftData.put("recipientName", recipientNames.toString());
                                        giftData.put("recipientUid", isRoomGift ? "" : (targetUids != null && !targetUids.isEmpty() ? targetUids.get(0) : ""));
                                        giftData.put("giftName", giftName + (quantity > 1 ? " (x" + quantity + ")" : ""));
                                        giftData.put("giftSvga", selectedGiftSvga);
                                        giftData.put("iconRes", (long) selectedItem.iconRes);
                                        giftData.put("timestamp", System.currentTimeMillis());
                                        roomGiftsRef.push().setValue(giftData);
                                    }

                                    String senderDisplayName = userName != null ? userName : "User";

                                    // Automatically push gift message to room chat list
                                    if (roomMessagesRef != null) {
                                        try {
                                            String rNotice = recipientNames.length() > 0 ? " to " + recipientNames.toString() : "";
                                            String qtyText = (quantity > 1) ? (quantity + "x ") : "";
                                            String giftChatMsg = "sent " + qtyText + giftName + rNotice + " 🎁";

                                            Map<String, Object> giftMsgMap = new HashMap<>();
                                            giftMsgMap.put("senderId", userID);
                                            giftMsgMap.put("senderName", senderDisplayName);
                                            giftMsgMap.put("text", giftChatMsg);
                                            giftMsgMap.put("giftIconRes", (long) selectedItem.iconRes);
                                            giftMsgMap.put("timestamp", System.currentTimeMillis());
                                            giftMsgMap.put("senderAvatar", SessionManager.getInstance(RoomChatActivity.this).getAvatar());
                                            UserProfileCache.UserProfile cachedProf = UserProfileCache.getDirectCachedProfile(userID);
                                            String userFrame = (cachedProf != null && cachedProf.equippedFrame != null) ? cachedProf.equippedFrame : "";
                                            giftMsgMap.put("equippedFrame", userFrame);
                                            roomMessagesRef.push().setValue(giftMsgMap);
                                        } catch (Exception ignored) {}
                                    }

                                    try {
                                        JSONObject giftJson = new JSONObject();
                                        giftJson.put("type", "GIFT");
                                        giftJson.put("giftId", uniqueGiftId);
                                        giftJson.put("giftId", uniqueGiftId);
                                        giftJson.put("senderName", senderDisplayName);
                                        giftJson.put("senderAvatar", SessionManager.getInstance(RoomChatActivity.this).getAvatar());
                                        giftJson.put("recipientName", recipientNames.toString());
                                        giftJson.put("giftName", giftName + (quantity > 1 ? " (x" + quantity + ")" : ""));
                                        giftJson.put("quantity", quantity);
                                        giftJson.put("giftSvga", selectedGiftSvga != null ? selectedGiftSvga : "");
                                        giftJson.put("iconRes", selectedItem.iconRes);
                                        ZegoManager.getInstance().sendInRoomTextMessage(giftJson.toString());
                                    } catch (Exception ignored) {}

                                    markGiftProcessed(uniqueGiftId);
                                    enqueueGiftEvent(uniqueGiftId, senderDisplayName, giftName, recipientNames.toString(), quantity, selectedGiftSvga);

                                    // Push Global Cross-Room Gift Event (for rednotification.svga banner outside room in MainActivity)
                                    try {
                                        DatabaseReference globalGiftsRef = FirebaseDatabase.getInstance().getReference("global_room_gifts");
                                        Map<String, Object> globalGiftMap = new HashMap<>();
                                        globalGiftMap.put("giftId", uniqueGiftId);
                                        globalGiftMap.put("senderName", senderDisplayName);
                                        globalGiftMap.put("senderAvatar", SessionManager.getInstance(RoomChatActivity.this).getAvatar());
                                        globalGiftMap.put("giftName", giftName + (quantity > 1 ? " (x" + quantity + ")" : ""));
                                        globalGiftMap.put("quantity", quantity);
                                        globalGiftMap.put("recipientName", recipientNames.toString());
                                        globalGiftMap.put("roomName", roomNameLabel != null ? roomNameLabel : "Audio Room");
                                        globalGiftMap.put("roomId", roomID != null ? roomID : "");
                                        globalGiftMap.put("iconRes", selectedItem.iconRes);
                                        globalGiftMap.put("timestamp", System.currentTimeMillis());
                                        globalGiftsRef.push().setValue(globalGiftMap);
                                    } catch (Exception ignored) {}

                                    Toast.makeText(RoomChatActivity.this, "🎁 Sent " + giftName + (quantity > 1 ? " x" + quantity : "") + "!", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onError(String error) {
                                    isSendingGift[0] = false;
                                    btnSendAction.setEnabled(true);
                                    Toast.makeText(RoomChatActivity.this, "❌ " + error, Toast.LENGTH_LONG).show();
                                }
                            }
                        );
                    } catch (Exception e) {
                        isSendingGift[0] = false;
                        btnSendAction.setEnabled(true);
                        Toast.makeText(RoomChatActivity.this, "❌ Error sending gift: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        dialog.setContentView(dialogView);
        View parentView = (View) dialogView.getParent();
        if (parentView != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(parentView);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
        }
        dialog.show();
    }

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private BottomSheetDialog luckySpinWheelDialog;

    private void showLuckySpinWheelDialog() {
        if (luckySpinWheelDialog != null && luckySpinWheelDialog.isShowing()) return;
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        luckySpinWheelDialog = dialog;
        dialog.setOnDismissListener(d -> {
            if (luckySpinWheelDialog == d) luckySpinWheelDialog = null;
        });
        applyGlassyStyle(dialog);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_lucky_spin_wheel, null, false);
        if (dialogView == null) return;
        dialog.setContentView(dialogView);

        TextView tvWheelCoins = dialogView.findViewById(R.id.tvWheelCoins);
        ImageView btnCloseWheel = dialogView.findViewById(R.id.btnCloseWheel);
        SpinWheelView spinWheelView = dialogView.findViewById(R.id.spinWheelView);
        View btnSpin = dialogView.findViewById(R.id.btnSpin);

        // Cache local balance so spin starts INSTANTLY with 0ms network latency
        final long[] cachedCoins = new long[]{0};

        Runnable refreshCoins = () -> WalletManager.getUserCoins(userID, balance -> runOnUiThread(() -> {
            cachedCoins[0] = balance;
            if (tvWheelCoins != null) tvWheelCoins.setText("🪙 " + balance);
        }));
        refreshCoins.run();

        if (btnCloseWheel != null) btnCloseWheel.setOnClickListener(v -> dialog.dismiss());

        Random random = new Random();
        final boolean[] isSpinInProgress = new boolean[]{false}; // LUCKY WHEEL FIX: Rapid click guard

        View.OnClickListener spinAction = v -> {
            if (isFastClick(v)) return; // LUCKY WHEEL FIX
            if (isSpinInProgress[0]) return; // LUCKY WHEEL FIX
            if (spinWheelView != null && spinWheelView.isSpinning()) return; // LUCKY WHEEL FIX
            if (btnSpin != null && !btnSpin.isEnabled()) return; // LUCKY WHEEL FIX

            final long spinCost = 100;
            if (cachedCoins[0] > 0 && cachedCoins[0] < spinCost) {
                Toast.makeText(this, "Insufficient coins! Please top-up.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Immediately set spinning guard & disable spin button to prevent multiple rapid clicks
            isSpinInProgress[0] = true; // LUCKY WHEEL FIX
            if (btnSpin != null) btnSpin.setEnabled(false); // LUCKY WHEEL FIX

            // Immediately update local UI balance
            cachedCoins[0] = Math.max(0, cachedCoins[0] - spinCost);
            if (tvWheelCoins != null) tvWheelCoins.setText("🪙 " + cachedCoins[0]);

            // Select single authoritative winning segment ONCE before starting animation
            int randomWinnerIndex = random.nextInt(SpinWheelController.NUM_SEGMENTS); // LUCKY WHEEL FIX
            SpinWheelController.SpinSegment winningSegment = SpinWheelController.INSTANCE.getSEGMENTS().get(randomWinnerIndex); // LUCKY WHEEL FIX

            // START SPINNING INSTANTLY
            if (spinWheelView != null) {
                spinWheelView.spinToSegment(
                        winningSegment.getIndex(), // LUCKY WHEEL FIX: Target authoritative segment index
                        SpinWheelController.SPIN_DURATION_MS,
                        SpinWheelController.DEFAULT_FULL_ROTATIONS,
                        winner -> {
                            long rewardCoins = winner.getRewardCoins(); // LUCKY WHEEL FIX

                            // Add won coins to user balance in Firebase & UI if reward > 0
                            if (rewardCoins > 0) {
                                WalletManager.addCoins(userID, rewardCoins, "LUCKY_SPIN", "Lucky Spin Reward", "Won " + rewardCoins + " Coins on Lucky Wheel", new WalletManager.WalletCallback() {
                                    @Override
                                    public void onSuccess(String message, long newCoinBalance) {
                                        runOnUiThread(() -> {
                                            cachedCoins[0] = newCoinBalance;
                                            if (tvWheelCoins != null) tvWheelCoins.setText("🪙 " + newCoinBalance);
                                        });
                                    }

                                    @Override
                                    public void onError(String error) {
                                        refreshCoins.run();
                                    }
                                });
                            }

                            // Show Custom Winner Result Dialog with authoritative winner segment
                            SpinWinnerDialog winnerDialog = new SpinWinnerDialog(RoomChatActivity.this); // LUCKY WHEEL FIX
                            winnerDialog.showWinner(winner, () -> { // LUCKY WHEEL FIX: Re-enable spin button only after claim
                                isSpinInProgress[0] = false; // LUCKY WHEEL FIX
                                if (btnSpin != null) btnSpin.setEnabled(true); // LUCKY WHEEL FIX
                                refreshCoins.run();
                                return Unit.INSTANCE;
                            });
                        }
                );
            } else {
                isSpinInProgress[0] = false; // LUCKY WHEEL FIX
                if (btnSpin != null) btnSpin.setEnabled(true); // LUCKY WHEEL FIX
            }

            // Deduct coins on Firebase asynchronously in background while wheel is spinning
            WalletManager.spendCoinsForGift(userID, null, spinCost, "Lucky Wheel Spin", new WalletManager.WalletCallback() {
                @Override
                public void onSuccess(String message, long newCoinBalance) {
                    // ROOM CONTRIBUTION TRACKING: Atomically increment total & user contribution
                    if (roomID != null && !roomID.trim().isEmpty()) {
                        WalletManager.trackRoomContribution(roomID, userID, userName, SessionManager.getInstance(RoomChatActivity.this).getAvatar(), spinCost);
                    }
                    runOnUiThread(() -> {
                        cachedCoins[0] = newCoinBalance;
                        if (tvWheelCoins != null) tvWheelCoins.setText("🪙 " + newCoinBalance);
                    });
                }

                @Override
                public void onError(String error) {
                    refreshCoins.run();
                }
            });
        };

        if (spinWheelView != null) {
            spinWheelView.setOnSpinButtonClickListener(() -> spinAction.onClick(spinWheelView));
        }
        if (btnSpin != null) btnSpin.setOnClickListener(spinAction);

        dialog.show();
    }

    // CONTRIBUTION DIALOG
    private BottomSheetDialog contributionsDialog;

    private void showRoomContributionsDialog() {
        if (roomID == null || roomID.trim().isEmpty()) return;
        if (contributionsDialog != null && contributionsDialog.isShowing()) return;

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        contributionsDialog = dialog;
        dialog.setOnDismissListener(d -> {
            if (contributionsDialog == d) contributionsDialog = null;
        });
        applyGlassyStyle(dialog);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_room_contributions, null, false);
        if (dialogView == null) return;
        dialog.setContentView(dialogView);

        ImageView btnClose = dialogView.findViewById(R.id.btnCloseContributions);
        ProgressBar pbLoading = dialogView.findViewById(R.id.pbLoadingContributions);
        View llEmpty = dialogView.findViewById(R.id.llEmptyContributions);
        RecyclerView rvContributions = dialogView.findViewById(R.id.rvContributions);
        TextView tvTotal = dialogView.findViewById(R.id.tvTotalContribution);

        TextView tabDaily = dialogView.findViewById(R.id.tabDaily);
        TextView tabWeekly = dialogView.findViewById(R.id.tabWeekly);
        TextView tabMonthly = dialogView.findViewById(R.id.tabMonthly);
        TextView tabTotal = dialogView.findViewById(R.id.tabTotal);

        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());

        List<RoomContributionModel> contributionList = new ArrayList<>();
        RoomContributionAdapter adapter = new RoomContributionAdapter(contributionList);

        if (rvContributions != null) {
            rvContributions.setLayoutManager(new LinearLayoutManager(this));
            rvContributions.setAdapter(adapter);
        }

        View.OnClickListener tabClickListener = v -> {
            if (tabDaily != null) {
                tabDaily.setBackgroundResource(R.drawable.bg_wallet_chip_unselected);
                tabDaily.setTextColor(Color.parseColor("#D0FFFFFF"));
            }
            if (tabWeekly != null) {
                tabWeekly.setBackgroundResource(R.drawable.bg_wallet_chip_unselected);
                tabWeekly.setTextColor(Color.parseColor("#D0FFFFFF"));
            }
            if (tabMonthly != null) {
                tabMonthly.setBackgroundResource(R.drawable.bg_wallet_chip_unselected);
                tabMonthly.setTextColor(Color.parseColor("#D0FFFFFF"));
            }
            if (tabTotal != null) {
                tabTotal.setBackgroundResource(R.drawable.bg_wallet_chip_unselected);
                tabTotal.setTextColor(Color.parseColor("#D0FFFFFF"));
            }

            if (v == tabDaily) {
                currentContributionTab = "DAILY";
                if (tabDaily != null) {
                    tabDaily.setBackgroundResource(R.drawable.bg_wallet_chip_selected);
                    tabDaily.setTextColor(Color.WHITE);
                }
            } else if (v == tabWeekly) {
                currentContributionTab = "WEEKLY";
                if (tabWeekly != null) {
                    tabWeekly.setBackgroundResource(R.drawable.bg_wallet_chip_selected);
                    tabWeekly.setTextColor(Color.WHITE);
                }
            } else if (v == tabMonthly) {
                currentContributionTab = "MONTHLY";
                if (tabMonthly != null) {
                    tabMonthly.setBackgroundResource(R.drawable.bg_wallet_chip_selected);
                    tabMonthly.setTextColor(Color.WHITE);
                }
            } else if (v == tabTotal) {
                currentContributionTab = "TOTAL";
                if (tabTotal != null) {
                    tabTotal.setBackgroundResource(R.drawable.bg_wallet_chip_selected);
                    tabTotal.setTextColor(Color.WHITE);
                }
            }

            loadContributionsForTab(roomID, currentContributionTab, adapter, pbLoading, llEmpty, rvContributions, tvTotal);
        };

        if (tabDaily != null) tabDaily.setOnClickListener(tabClickListener);
        if (tabWeekly != null) tabWeekly.setOnClickListener(tabClickListener);
        if (tabMonthly != null) tabMonthly.setOnClickListener(tabClickListener);
        if (tabTotal != null) tabTotal.setOnClickListener(tabClickListener);

        currentContributionTab = "DAILY";
        loadContributionsForTab(roomID, currentContributionTab, adapter, pbLoading, llEmpty, rvContributions, tvTotal);

        dialog.show();
    }

    private String currentContributionTab = "DAILY";

    private void loadContributionsForTab(String roomId, String tab, RoomContributionAdapter adapter, ProgressBar pbLoading, View llEmpty, RecyclerView rvContributions, TextView tvTotal) {
        if (isFinishing() || isDestroyed()) return;
        if (pbLoading != null) pbLoading.setVisibility(View.VISIBLE);
        if (llEmpty != null) llEmpty.setVisibility(View.GONE);

        Date now = new Date();
        String dailyKey = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(now);
        String weeklyKey = new SimpleDateFormat("yyyy-'W'ww", Locale.US).format(now);
        String monthlyKey = new SimpleDateFormat("yyyy-MM", Locale.US).format(now);

        Calendar todayCal = Calendar.getInstance();
        todayCal.set(Calendar.HOUR_OF_DAY, 0);
        todayCal.set(Calendar.MINUTE, 0);
        todayCal.set(Calendar.SECOND, 0);
        todayCal.set(Calendar.MILLISECOND, 0);
        long todayStart = todayCal.getTimeInMillis();

        Calendar weekCal = Calendar.getInstance();
        weekCal.set(Calendar.DAY_OF_WEEK, weekCal.getFirstDayOfWeek());
        weekCal.set(Calendar.HOUR_OF_DAY, 0);
        weekCal.set(Calendar.MINUTE, 0);
        weekCal.set(Calendar.SECOND, 0);
        weekCal.set(Calendar.MILLISECOND, 0);
        long weekStart = weekCal.getTimeInMillis();

        Calendar monthCal = Calendar.getInstance();
        monthCal.set(Calendar.DAY_OF_MONTH, 1);
        monthCal.set(Calendar.HOUR_OF_DAY, 0);
        monthCal.set(Calendar.MINUTE, 0);
        monthCal.set(Calendar.SECOND, 0);
        monthCal.set(Calendar.MILLISECOND, 0);
        long monthStart = monthCal.getTimeInMillis();

        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomId.trim());
        DatabaseReference targetContribRef;

        if ("DAILY".equals(tab)) {
            targetContribRef = roomRef.child("room_contributions_daily").child(dailyKey);
        } else if ("WEEKLY".equals(tab)) {
            targetContribRef = roomRef.child("room_contributions_weekly").child(weeklyKey);
        } else if ("MONTHLY".equals(tab)) {
            targetContribRef = roomRef.child("room_contributions_monthly").child(monthlyKey);
        } else {
            targetContribRef = roomRef.child("room_contributions");
        }

        targetContribRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot contribSnap) {
                if (isFinishing() || isDestroyed()) return;

                List<RoomContributionModel> newList = new ArrayList<>();
                long periodTotal = 0L;

                if (contribSnap.exists()) {
                    for (DataSnapshot child : contribSnap.getChildren()) {
                        String uid = child.child("userId").getValue(String.class);
                        if (uid == null || uid.isEmpty()) uid = child.getKey();
                        String name = child.child("userName").getValue(String.class);
                        String avatar = child.child("userAvatar").getValue(String.class);
                        Long amt = child.child("amount").getValue(Long.class);
                        if (amt != null && amt > 0) {
                            newList.add(new RoomContributionModel(uid, name, avatar, amt));
                            periodTotal += amt;
                        }
                    }
                }

                // Fallback: If timeline bucket node has no data yet, filter room_contributions by timestamp
                if (newList.isEmpty() && !"TOTAL".equals(tab)) {
                    roomRef.child("room_contributions").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot allSnap) {
                            if (isFinishing() || isDestroyed()) return;
                            if (pbLoading != null) pbLoading.setVisibility(View.GONE);

                            List<RoomContributionModel> fbList = new ArrayList<>();
                            long fbTotal = 0L;
                            long cutoff = "DAILY".equals(tab) ? todayStart : ("WEEKLY".equals(tab) ? weekStart : monthStart);

                            if (allSnap.exists()) {
                                for (DataSnapshot child : allSnap.getChildren()) {
                                    Long ts = child.child("timestamp").getValue(Long.class);
                                    if (ts == null || ts >= cutoff) {
                                        String uid = child.child("userId").getValue(String.class);
                                        if (uid == null || uid.isEmpty()) uid = child.getKey();
                                        String name = child.child("userName").getValue(String.class);
                                        String avatar = child.child("userAvatar").getValue(String.class);
                                        Long amt = child.child("amount").getValue(Long.class);
                                        if (amt != null && amt > 0) {
                                            fbList.add(new RoomContributionModel(uid, name, avatar, amt));
                                            fbTotal += amt;
                                        }
                                    }
                                }
                            }

                            Collections.sort(fbList, (a, b) -> Long.compare(b.getAmount(), a.getAmount()));
                            adapter.updateList(fbList);
                            if (tvTotal != null) tvTotal.setText(CoinUtils.formatCompactCoins(fbTotal));

                            if (fbList.isEmpty()) {
                                if (llEmpty != null) llEmpty.setVisibility(View.VISIBLE);
                                if (rvContributions != null) rvContributions.setVisibility(View.GONE);
                            } else {
                                if (llEmpty != null) llEmpty.setVisibility(View.GONE);
                                if (rvContributions != null) rvContributions.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                            if (llEmpty != null) llEmpty.setVisibility(View.VISIBLE);
                        }
                    });
                    return;
                }

                if (pbLoading != null) pbLoading.setVisibility(View.GONE);

                if ("TOTAL".equals(tab)) {
                    roomRef.child("totalCoinSpend").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Long totalSpend = snapshot.getValue(Long.class);
                            if (totalSpend == null) totalSpend = 0L;
                            if (tvTotal != null) {
                                tvTotal.setText(CoinUtils.formatCompactCoins(Math.max(0L, totalSpend)));
                            }
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error) {}
                    });
                } else {
                    if (tvTotal != null) {
                        tvTotal.setText(CoinUtils.formatCompactCoins(periodTotal));
                    }
                }

                Collections.sort(newList, (a, b) -> Long.compare(b.getAmount(), a.getAmount()));
                adapter.updateList(newList);

                if (newList.isEmpty()) {
                    if (llEmpty != null) llEmpty.setVisibility(View.VISIBLE);
                    if (rvContributions != null) rvContributions.setVisibility(View.GONE);
                } else {
                    if (llEmpty != null) llEmpty.setVisibility(View.GONE);
                    if (rvContributions != null) rvContributions.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isFinishing() || isDestroyed()) return;
                if (pbLoading != null) pbLoading.setVisibility(View.GONE);
                if (llEmpty != null) llEmpty.setVisibility(View.VISIBLE);
                if (rvContributions != null) rvContributions.setVisibility(View.GONE);
            }
        });
    }

    // --- ANIMATION & BANNER QUEUE MANAGER ---
    private final Handler giftHandler = new Handler(Looper.getMainLooper());
    private Runnable giftTimeoutRunnable = null;
    private final Set<String> processedGiftIds = Collections.synchronizedSet(new HashSet<>());

    // GIFT COIN/ENERGY FIX
    private List<String> getActiveRoomMemberUids() {
        List<String> uids = new ArrayList<>();
        Set<String> set = new HashSet<>();

        List<SeatModel> seats = SeatManager.getInstance().getSeats();
        if (seats != null) {
            for (SeatModel seat : seats) {
                if (seat != null && !seat.isEmpty() && seat.userID != null && !seat.userID.trim().isEmpty()) {
                    if (set.add(seat.userID.trim())) {
                        uids.add(seat.userID.trim());
                    }
                }
            }
        }
        if (userID != null && !userID.trim().isEmpty() && set.add(userID.trim())) {
            uids.add(userID.trim());
        }
        return uids;
    }

    private boolean markGiftProcessed(String giftId) {
        if (giftId == null || giftId.trim().isEmpty()) return false;
        String clean = giftId.trim();
        if (processedGiftIds.contains(clean)) {
            return true;
        }
        if (processedGiftIds.size() > 300) {
            processedGiftIds.clear();
        }
        processedGiftIds.add(clean);
        return false;
    }

    static class GiftEventItem {
        final String giftSvgaPath;
        final String bannerNoticeHtml;
        final String giftId;

        GiftEventItem(String giftSvgaPath, String bannerNoticeHtml, String giftId) {
            this.giftSvgaPath = giftSvgaPath;
            this.bannerNoticeHtml = bannerNoticeHtml;
            this.giftId = giftId;
        }
    }

    private final Queue<GiftEventItem> giftEventQueue = new ConcurrentLinkedQueue<>();
    private boolean isGiftEventPlaying = false;
    private long currentGiftEventPlayId = 0L;

    public void enqueueGiftEvent(String giftId, String senderName, String giftName, String recipientName, int quantity, String giftSvga) {
        if (isFinishing() || isDestroyed()) return;
        if (giftId != null && !giftId.trim().isEmpty()) {
            if (markGiftProcessed("event_" + giftId)) return;
        }

        String sName = (senderName != null && !senderName.trim().isEmpty()) ? senderName : "User";
        String gName = (giftName != null && !giftName.trim().isEmpty()) ? giftName : "Gift";
        String rName = (recipientName != null && !recipientName.trim().isEmpty()) ? (" to <font color='#00FFC6'><b>" + recipientName.trim() + "</b></font>") : "";

        int qty = Math.max(1, quantity);
        if (qty <= 1) {
            int xIdx = gName.indexOf(" (x");
            if (xIdx != -1) {
                try {
                    String numStr = gName.substring(xIdx + 3, gName.indexOf(")", xIdx));
                    qty = Integer.parseInt(numStr);
                } catch (Exception ignored) {}
            }
        }

        String cleanGiftName = gName;
        int idx = cleanGiftName.indexOf(" (x");
        if (idx != -1) {
            cleanGiftName = cleanGiftName.substring(0, idx);
        }

        String qtyText = (qty > 1) ? (qty + "x ") : "";
        String htmlNotice = "<font color='#FFD700'><b>" + sName + "</b></font> sent <font color='#FF007A'><b>" + qtyText + cleanGiftName + "</b></font>" + rName + " 🎁";

        String animPath = giftSvga;
        if (animPath == null || animPath.trim().isEmpty()) {
            GiftStoreItem matched = GiftCatalog.findGiftByAny(gName, giftSvga);
            if (matched != null && matched.svgaPath != null && !matched.svgaPath.trim().isEmpty()) {
                animPath = matched.svgaPath;
            } else {
                animPath = "gift/rose.svga";
            }
        }

        giftEventQueue.offer(new GiftEventItem(animPath, htmlNotice, giftId));
        giftHandler.post(this::processNextGiftEvent);
    }

    public void playSvgaAnimation(String fileName) {
        playSvgaAnimation(fileName, 1);
    }

    public void playSvgaAnimation(String fileName, int count) {
        if (isFinishing() || isDestroyed()) return;
        if (fileName == null || fileName.trim().isEmpty()) return;
        int times = Math.max(1, count);
        for (int i = 0; i < times; i++) {
            enqueueGiftEvent(null, "User", "Gift", "", 1, fileName);
        }
    }

    public void showGoldenGiftBanner(String senderName, String giftName) {
        enqueueGiftEvent(null, senderName, giftName, "", 1, null);
    }

    public void showGoldenGiftBanner(String senderName, String giftName, String recipientName) {
        enqueueGiftEvent(null, senderName, giftName, recipientName, 1, null);
    }

    public void showGoldenGiftBanner(String senderName, String giftName, String recipientName, int quantity) {
        enqueueGiftEvent(null, senderName, giftName, recipientName, quantity, null);
    }

    public void showGoldenGiftBanner(String giftId, String senderName, String giftName, String recipientName, int quantity) {
        enqueueGiftEvent(giftId, senderName, giftName, recipientName, quantity, null);
    }

    private void processNextGiftEvent() {
        if (isFinishing() || isDestroyed()) return;
        if (Looper.myLooper() != Looper.getMainLooper()) {
            giftHandler.post(this::processNextGiftEvent);
            return;
        }
        if (isGiftEventPlaying) return;

        GiftEventItem nextItem = giftEventQueue.poll();
        if (nextItem == null) return;

        isGiftEventPlaying = true;
        final long playId = ++currentGiftEventPlayId;

        if (giftTimeoutRunnable != null) {
            giftHandler.removeCallbacks(giftTimeoutRunnable);
        }
        giftTimeoutRunnable = () -> finishCurrentGiftEvent(playId);

        playUnifiedGiftEvent(nextItem, playId);
    }

    private void finishCurrentGiftEvent(final long playId) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            runOnUiThread(() -> finishCurrentGiftEvent(playId));
            return;
        }

        if (playId != currentGiftEventPlayId || playId == 0L) return;
        currentGiftEventPlayId = 0L;

        if (giftTimeoutRunnable != null) {
            giftHandler.removeCallbacks(giftTimeoutRunnable);
            giftTimeoutRunnable = null;
        }

        if (svgaPlayer != null) {
            try {
                svgaPlayer.setCallback(null);
                svgaPlayer.stopAnimation();
                svgaPlayer.clear();
                svgaPlayer.setVisibility(View.GONE);
            } catch (Exception ignored) {}
        }

        if (tvBannerNotice != null) {
            try {
                tvBannerNotice.setVisibility(View.GONE);
                tvBannerNotice.setText("");
            } catch (Exception ignored) {}
        }
        if (svgaBannerPlayer != null) {
            try {
                svgaBannerPlayer.setCallback(null);
                svgaBannerPlayer.stopAnimation();
                svgaBannerPlayer.clear();
                svgaBannerPlayer.setVisibility(View.GONE);
            } catch (Exception ignored) {}
        }
        if (bannerGiftContainer != null) {
            try {
                bannerGiftContainer.setVisibility(View.GONE);
            } catch (Exception ignored) {}
        }

        isGiftEventPlaying = false;
        giftHandler.postDelayed(this::processNextGiftEvent, 100L);
    }

    private void playUnifiedGiftEvent(GiftEventItem item, final long playId) {
        if (isFinishing() || isDestroyed() || item == null || playId != currentGiftEventPlayId) {
            finishCurrentGiftEvent(playId);
            return;
        }

        SVGAParser parser = (svgaParser != null) ? svgaParser : new SVGAParser(this);
        parser.decodeFromAssets("Notification/rednotification.svga", new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete(@NotNull SVGAVideoEntity bannerVideoItem) {
                parser.decodeFromAssets(item.giftSvgaPath, new SVGAParser.ParseCompletion() {
                    @Override
                    public void onComplete(@NotNull SVGAVideoEntity giftVideoItem) {
                        runOnUiThread(() -> {
                            if (playId != currentGiftEventPlayId) return;
                            startDualGiftPlayback(bannerVideoItem, giftVideoItem, item, playId);
                        });
                    }

                    @Override
                    public void onError() {
                        parser.decodeFromAssets("gift/rose.svga", new SVGAParser.ParseCompletion() {
                            @Override
                            public void onComplete(@NotNull SVGAVideoEntity fallbackItem) {
                                runOnUiThread(() -> {
                                    if (playId != currentGiftEventPlayId) return;
                                    startDualGiftPlayback(bannerVideoItem, fallbackItem, item, playId);
                                });
                            }

                            @Override
                            public void onError() {
                                finishCurrentGiftEvent(playId);
                            }
                        }, null);
                    }
                }, null);
            }

            @Override
            public void onError() {
                parser.decodeFromAssets(item.giftSvgaPath, new SVGAParser.ParseCompletion() {
                    @Override
                    public void onComplete(@NotNull SVGAVideoEntity giftVideoItem) {
                        runOnUiThread(() -> {
                            if (playId != currentGiftEventPlayId) return;
                            startDualGiftPlayback(null, giftVideoItem, item, playId);
                        });
                    }

                    @Override
                    public void onError() {
                        finishCurrentGiftEvent(playId);
                    }
                }, null);
            }
        }, null);
    }

    private void startDualGiftPlayback(SVGAVideoEntity bannerItem, SVGAVideoEntity giftItem, GiftEventItem item, final long playId) {
        if (isFinishing() || isDestroyed() || playId != currentGiftEventPlayId) {
            finishCurrentGiftEvent(playId);
            return;
        }

        try {
            int frames = giftItem != null ? giftItem.getFrames() : 60;
            int fps = (giftItem != null && giftItem.getFPS() > 0) ? giftItem.getFPS() : 20;
            long giftDurationMs = (long) (((double) frames / fps) * 1000L);
            long displayDurationMs = Math.max(3500L, giftDurationMs);

            if (giftTimeoutRunnable != null) {
                giftHandler.removeCallbacks(giftTimeoutRunnable);
            }
            giftTimeoutRunnable = () -> finishCurrentGiftEvent(playId);
            giftHandler.postDelayed(giftTimeoutRunnable, displayDurationMs + 800L);

            // 1. Start Banner SVGA & Notice Text together at exact same instant for 3 seconds only!
            if (bannerItem != null && svgaBannerPlayer != null && bannerGiftContainer != null && tvBannerNotice != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    tvBannerNotice.setText(Html.fromHtml(item.bannerNoticeHtml, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    tvBannerNotice.setText(Html.fromHtml(item.bannerNoticeHtml));
                }

                bannerGiftContainer.bringToFront();
                svgaBannerPlayer.stopAnimation();
                svgaBannerPlayer.clear();
                svgaBannerPlayer.setVideoItem(bannerItem);
                svgaBannerPlayer.setLoops(0);
                svgaBannerPlayer.stepToFrame(0, false);

                tvBannerNotice.setVisibility(View.VISIBLE);
                svgaBannerPlayer.setVisibility(View.VISIBLE);
                bannerGiftContainer.setVisibility(View.VISIBLE);

                svgaBannerPlayer.startAnimation();

                // Automatically hide notification banner after EXACTLY 3 seconds
                giftHandler.postDelayed(() -> {
                    try {
                        if (bannerGiftContainer != null) bannerGiftContainer.setVisibility(View.GONE);
                        if (tvBannerNotice != null) {
                            tvBannerNotice.setVisibility(View.GONE);
                            tvBannerNotice.setText("");
                        }
                        if (svgaBannerPlayer != null) {
                            svgaBannerPlayer.stopAnimation();
                            svgaBannerPlayer.clear();
                            svgaBannerPlayer.setVisibility(View.GONE);
                        }
                    } catch (Exception ignored) {}
                }, 3000L);
            }

            // 2. Start Main Gift SVGA if available
            if (giftItem != null && svgaPlayer != null) {
                svgaPlayer.stopAnimation();
                svgaPlayer.clear();
                svgaPlayer.setVideoItem(giftItem);
                svgaPlayer.setLoops(1);
                svgaPlayer.stepToFrame(0, false);
                svgaPlayer.setVisibility(View.VISIBLE);

                svgaPlayer.setCallback(new SVGACallback() {
                    private boolean isHandled = false;

                    @Override
                    public void onFinished() {
                        if (!isHandled) {
                            isHandled = true;
                            finishCurrentGiftEvent(playId);
                        }
                    }

                    @Override public void onPause() {}
                    @Override public void onRepeat() {}
                    @Override public void onStep(int frame, double percentage) {}
                });

                svgaPlayer.startAnimation();
            } else {
                giftHandler.postDelayed(() -> finishCurrentGiftEvent(playId), displayDurationMs);
            }
        } catch (Exception e) {
            Log.e("RoomChatActivity", "Error starting dual gift playback", e);
            finishCurrentGiftEvent(playId);
        }
    }

    public void showUserEntryBanner(String displayUserName) {
        if (isFinishing() || isDestroyed()) return;
        String name = (displayUserName != null && !displayUserName.trim().isEmpty()) ? displayUserName : "User";
        String htmlNotice = "<font color='#FFD700'><b>" + name + "</b></font> entered the room 🎉";

        runOnUiThread(() -> {
            if (tvEntryBannerNotice != null && bannerEntryContainer != null && svgaEntryBannerPlayer != null) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        tvEntryBannerNotice.setText(Html.fromHtml(htmlNotice, Html.FROM_HTML_MODE_LEGACY));
                    } else {
                        tvEntryBannerNotice.setText(Html.fromHtml(htmlNotice));
                    }

                    SVGAParser parser = (svgaParser != null) ? svgaParser : new SVGAParser(this);
                    parser.decodeFromAssets("Notification/rednotification.svga", new SVGAParser.ParseCompletion() {
                        @Override
                        public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                            runOnUiThread(() -> {
                                if (isFinishing() || isDestroyed()) return;
                                try {
                                    bannerEntryContainer.bringToFront();
                                    svgaEntryBannerPlayer.stopAnimation();
                                    svgaEntryBannerPlayer.clear();
                                    svgaEntryBannerPlayer.setVideoItem(videoItem);
                                    svgaEntryBannerPlayer.setLoops(1);
                                    tvEntryBannerNotice.setVisibility(View.VISIBLE);
                                    svgaEntryBannerPlayer.setVisibility(View.VISIBLE);
                                    bannerEntryContainer.setVisibility(View.VISIBLE);
                                    svgaEntryBannerPlayer.startAnimation();

                                    giftHandler.postDelayed(() -> {
                                        if (bannerEntryContainer != null) bannerEntryContainer.setVisibility(View.GONE);
                                        if (tvEntryBannerNotice != null) {
                                            tvEntryBannerNotice.setVisibility(View.GONE);
                                            tvEntryBannerNotice.setText("");
                                        }
                                        if (svgaEntryBannerPlayer != null) {
                                            svgaEntryBannerPlayer.stopAnimation();
                                            svgaEntryBannerPlayer.clear();
                                            svgaEntryBannerPlayer.setVisibility(View.GONE);
                                        }
                                    }, 3500L);
                                } catch (Exception ignored) {}
                            });
                        }

                        @Override
                        public void onError() {}
                    }, null);
                } catch (Exception ignored) {}
            }
        });
    }

    private void playEntrySceneVideo() {
        // Entry video dialog removed
    }

    private void bindCpPartnerInfo(String targetUid, String pUid, Long timestamp, View flCpPartnerAvatarContainer, View layoutCpJunctionBadge, TextView tvCpLevelBadgeText, ImageView ivCpPartnerAvatar) {
        if (isFinishing() || isDestroyed()) return;

        if (flCpPartnerAvatarContainer != null) flCpPartnerAvatarContainer.setVisibility(View.VISIBLE);
        if (layoutCpJunctionBadge != null) layoutCpJunctionBadge.setVisibility(View.VISIBLE);

        // Query "cp_guard_values" to get real intimacy score and calculate actual CP Level (LV.1 to LV.10)
        DatabaseReference cpGuardRootRef = FirebaseDatabase.getInstance().getReference("cp_guard_values");
        cpGuardRootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isFinishing() || isDestroyed()) return;

                long maxIntimacy = 0;
                if (snapshot.exists()) {
                    if (snapshot.hasChild(targetUid) && snapshot.child(targetUid).hasChild(pUid)) {
                        maxIntimacy = Math.max(maxIntimacy, parseGuardValue(snapshot.child(targetUid).child(pUid)));
                    }
                    if (snapshot.hasChild(pUid) && snapshot.child(pUid).hasChild(targetUid)) {
                        maxIntimacy = Math.max(maxIntimacy, parseGuardValue(snapshot.child(pUid).child(targetUid)));
                    }
                    String pairKey1 = targetUid + "_" + pUid;
                    String pairKey2 = pUid + "_" + targetUid;
                    if (snapshot.hasChild(pairKey1)) {
                        maxIntimacy = Math.max(maxIntimacy, parseGuardValue(snapshot.child(pairKey1)));
                    }
                    if (snapshot.hasChild(pairKey2)) {
                        maxIntimacy = Math.max(maxIntimacy, parseGuardValue(snapshot.child(pairKey2)));
                    }
                }

                int cpLevel = CpIntimacyManager.calculateCpLevel(maxIntimacy);
                if (tvCpLevelBadgeText != null) {
                    tvCpLevelBadgeText.setText("LV." + cpLevel);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (tvCpLevelBadgeText != null) {
                    tvCpLevelBadgeText.setText("LV.1");
                }
            }
        });

        View.OnClickListener cpClickListener = v -> {
            Intent intent = new Intent(RoomChatActivity.this, CpLevelActivity.class);
            startActivity(intent);
        };

        if (layoutCpJunctionBadge != null) layoutCpJunctionBadge.setOnClickListener(cpClickListener);

        DatabaseReference cpUserRef = FirebaseDatabase.getInstance().getReference("users").child(pUid);
        cpUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot partnerUserSnap) {
                if (isFinishing() || isDestroyed()) return;
                if (partnerUserSnap.exists()) {
                    String pAvatar = partnerUserSnap.child("avtar").getValue(String.class);
                    if (TextUtils.isEmpty(pAvatar)) pAvatar = partnerUserSnap.child("avatar").getValue(String.class);
                    if (TextUtils.isEmpty(pAvatar)) pAvatar = partnerUserSnap.child("photoUrl").getValue(String.class);
                    if (TextUtils.isEmpty(pAvatar)) pAvatar = partnerUserSnap.child("image").getValue(String.class);
                    if (TextUtils.isEmpty(pAvatar)) pAvatar = partnerUserSnap.child("userIcon").getValue(String.class);

                    if (!TextUtils.isEmpty(pAvatar) && ivCpPartnerAvatar != null) {
                        Glide.with(RoomChatActivity.this)
                                .load(pAvatar)
                                .placeholder(R.drawable.logo_placeholder)
                                .error(R.drawable.logo_placeholder)
                                .into(ivCpPartnerAvatar);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private long parseGuardValue(DataSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) return 0;
        if (snapshot.hasChild("value")) {
            Object val = snapshot.child("value").getValue();
            if (val != null) {
                try {
                    return Long.parseLong(String.valueOf(val).trim());
                } catch (Exception ignored) {}
            }
        }
        Object rootVal = snapshot.getValue();
        if (rootVal != null) {
            try {
                return Long.parseLong(String.valueOf(rootVal).trim());
            } catch (Exception ignored) {}
        }
        return 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopGameCarouselAutoScroll();
        if (giftTimeoutRunnable != null) giftHandler.removeCallbacks(giftTimeoutRunnable);
        giftEventQueue.clear();
        currentGiftEventPlayId = 0L;
        isGiftEventPlaying = false;
        if (svgaPlayer != null) {
            try {
                svgaPlayer.setCallback(null);
                svgaPlayer.stopAnimation();
                svgaPlayer.clear();
                svgaPlayer.setVisibility(View.GONE);
            } catch (Exception ignored) {}
        }
        if (bannerGiftContainer != null) {
            try {
                bannerGiftContainer.setVisibility(View.GONE);
            } catch (Exception ignored) {}
        }
        if (svgaBannerPlayer != null) {
            try {
                svgaBannerPlayer.setCallback(null);
                svgaBannerPlayer.stopAnimation();
                svgaBannerPlayer.clear();
            } catch (Exception ignored) {}
        }
        if (!isMinimized) {
            RoomFloatingManager.getInstance().removeFloatingBubble();
            if (!isRoomLeft) {
                leaveRoom();
            }
        }
    }
}
