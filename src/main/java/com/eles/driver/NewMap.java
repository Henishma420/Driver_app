package com.eles.driver;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NewMap extends AppCompatActivity {

    private MapView mapView;
    private double driverLat = ... ;//static
    private double driverLon = ... ;//static
    private Button accept;
    private String requestId;
    private FirebaseAuth mAuth;
    private DatabaseReference sosRequestsRef;
    private static final String TAG = "NewMap";
    private DatabaseReference driversRef;
    private String currentDriverId;
    private DatabaseReference audioUrlsRef;
    private MediaPlayer mediaPlayer;
    private Handler audioHandler = new Handler();
    private List<String> audioUrls = new ArrayList<>();
    private int currentAudioIndex = 0;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_map);

        Configuration.getInstance().setUserAgentValue(getPackageName());
        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        accept = findViewById(R.id.accept_btn);

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        sosRequestsRef = database.getReference("sos_requests");
        driversRef = database.getReference("drivers");
        currentDriverId = "4537D";//value static

        Intent intent = getIntent();
        double sosLatitude = intent.getDoubleExtra("lat", 0);
        double sosLongitude = intent.getDoubleExtra("lon", 0);
        requestId = intent.getStringExtra("request_id");

        // Initialize MediaPlayer
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(mp -> playNextAudio());

        if (requestId != null) {
            audioUrlsRef = database.getReference("sos_audio_urls").child(requestId);
            listenForAudioUrls();
        }

        accept.setOnClickListener(view -> {
            sosRequestsRef.child(requestId).child("status").setValue("accepted")
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            sendNotificationToEmergencyContacts(requestId);
                            Toast.makeText(NewMap.this, "Request is accepted ✅", Toast.LENGTH_SHORT).show();

                            Intent intent1 = new Intent(NewMap.this, AcceptedDetails.class);
                            intent1.putExtra("lat", sosLatitude);
                            intent1.putExtra("lon", sosLongitude);

                            startActivity(intent1);
                            finish();
                        } else {
                            Toast.makeText(NewMap.this, "Error accepting request", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        if (sosLatitude != 0 && sosLongitude != 0) {
            GeoPoint sosPoint = new GeoPoint(sosLatitude, sosLongitude);
            GeoPoint driverPoint = new GeoPoint(driverLat, driverLon);

            Marker sosMarker = new Marker(mapView);
            sosMarker.setPosition(sosPoint);
            sosMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            sosMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.marker_default));
            mapView.getOverlays().add(sosMarker);


            Marker driverMarker = new Marker(mapView);
            driverMarker.setPosition(driverPoint);
            driverMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(driverMarker);

            getRoute(driverPoint, sosPoint);

            IMapController mapController = mapView.getController();
            mapController.setZoom(12.0);
            mapController.animateTo(new GeoPoint((sosLatitude + driverLat) / 2, (sosLongitude + driverLon) / 2));
        }
    }

    private void listenForAudioUrls() {
        audioUrlsRef.addChildEventListener(new com.google.firebase.database.ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                String audioUrl = snapshot.getValue(String.class);
                if (audioUrl != null) {
                    audioUrls.add(audioUrl);
                    if (audioUrls.size() == 1 && !mediaPlayer.isPlaying()) {
                        playNextAudio();
                    }
                    Log.d(TAG, "New audio URL received: " + audioUrl);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String removedUrl = snapshot.getValue(String.class);
                if (removedUrl != null) {
                    audioUrls.remove(removedUrl);
                    if (removedUrl.equals(audioUrls.get(currentAudioIndex))) {
                        stopAudio();
                        if (!audioUrls.isEmpty()) {
                            currentAudioIndex = 0;
                            playNextAudio();
                        } else {
                            currentAudioIndex = 0;
                        }
                    } else {
                        int index = audioUrls.indexOf(removedUrl);
                        if (index < currentAudioIndex) {
                            currentAudioIndex--;
                        }
                    }
                    Log.d(TAG, "Audio URL removed: " + removedUrl);
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Audio URL listener cancelled: " + error.getMessage());
                Toast.makeText(NewMap.this, "Error listening for audio.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void playNextAudio() {
        if (currentAudioIndex < audioUrls.size()) {
            String url = audioUrls.get(currentAudioIndex);
            playAudioFromUrl(url);
            currentAudioIndex++;
        } else {
            Log.d(TAG, "No more audio to play.");

        }
    }

    private void playAudioFromUrl(String url) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(this, Uri.parse(url));
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                Toast.makeText(NewMap.this, "Playing audio...", Toast.LENGTH_SHORT).show();
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer Error: What=" + what + ", Extra=" + extra);
                Toast.makeText(NewMap.this, "Error playing audio.", Toast.LENGTH_SHORT).show();
                playNextAudio();
                return true;
            });
        } catch (IOException e) {
            Log.e(TAG, "Error setting data source: " + e.getMessage());
            Toast.makeText(NewMap.this, "Error setting audio source.", Toast.LENGTH_SHORT).show();
            playNextAudio();
        }
    }

    private void stopAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void sendNotificationToEmergencyContacts(String requestId) {
        DatabaseReference emergencyNotificationsRef = FirebaseDatabase.getInstance()
                .getReference("notifications")
                .child(requestId);

        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("title", "Driver is ready");
        notificationData.put("body", "Your driver has accepted the request.");
        notificationData.put("timestamp", System.currentTimeMillis());

        emergencyNotificationsRef.setValue(notificationData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Notification saved to Firebase!");
                    } else {
                        Log.e(TAG, "Failed to save notification: " + task.getException());
                    }
                });
    }

    private void getRoute(GeoPoint startPoint, GeoPoint endPoint) {
        String coordinates = startPoint.getLongitude() + "," + startPoint.getLatitude() + ";" +
                endPoint.getLongitude() + "," + endPoint.getLatitude();
        String overview = "full";
        String geometries = "geojson";
        String baseUrl = "http://router.project-osrm.org/";

        Gson gson = new GsonBuilder().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        OSRMService service = retrofit.create(OSRMService.class);
        Call<RouteResponse> call = service.getRoute(coordinates, overview, geometries);

        call.enqueue(new Callback<RouteResponse>() {
            @Override
            public void onResponse(Call<RouteResponse> call, Response<RouteResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().getRoutes().isEmpty()) {
                    RouteResponse routeResponse = response.body();
                    List<List<Double>> coordinates = routeResponse.getRoutes().get(0).getGeometry().getCoordinates();

                    List<GeoPoint> routePoints = new ArrayList<>();
                    for (List<Double> coord : coordinates) {
                        double lon = coord.get(0);
                        double lat = coord.get(1);
                        routePoints.add(new GeoPoint(lat, lon));
                    }

                    drawRoute(routePoints);
                } else {
                    Toast.makeText(NewMap.this, "Failed to get route", Toast.LENGTH_SHORT).show();
                    Log.e("OSRM Error", "Response was not successful or body was null: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<RouteResponse> call, Throwable t) {
                Toast.makeText(NewMap.this, "Error connecting to routing service", Toast.LENGTH_SHORT).show();
                Log.e("OSRM Error", "Network error: " + t.getMessage());
            }
        });
    }

    private void drawRoute(List<GeoPoint> routePoints) {
        if (routePoints.isEmpty()) {
            Log.e("Draw Route", "No points to draw!");
            return;
        }

        Polyline roadOverlay = new Polyline();
        roadOverlay.setPoints(routePoints);
        roadOverlay.setColor(Color.RED);
        roadOverlay.setWidth(7f);
        mapView.getOverlays().add(roadOverlay);

        Log.d("Draw Route", "Route drawn with " + routePoints.size() + " points.");

        mapView.invalidate();
    }
}
