package com.eles.driver;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Request extends AppCompatActivity {

    private List<SOSData> sosDataList = new ArrayList<>();
    private DatabaseReference dbRef;
    private final double driverLat = 18.36;//static
    private final double driverLon = 98.14;//static
    private RecyclerView recyclerView;
    private SOSAdapter sosAdapter;

    public static HashMap<String, SOSData> keys = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_sos);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        sosAdapter = new SOSAdapter(this, sosDataList);
        recyclerView.setAdapter(sosAdapter);

        dbRef = FirebaseDatabase.getInstance().getReference("sos_requests");
        listenForSOS();
    }

    private void listenForSOS() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sosDataList.clear();
                keys.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String requestId = dataSnapshot.child("requestId").getValue(String.class);
                    Double latitude = dataSnapshot.child("latitude").getValue(Double.class);
                    Double longitude = dataSnapshot.child("longitude").getValue(Double.class);
                    String status = dataSnapshot.child("status").getValue(String.class);

                    if (latitude != null && longitude != null && requestId != null && !requestId.isEmpty()) {
                        float[] results = new float[1];
                        Location.distanceBetween(driverLat, driverLon, latitude, longitude, results);
                        double distanceKm = results[0] / 1000.0;

                        SOSData sosData = new SOSData(requestId, status, latitude, longitude, distanceKm);
                        if (!"accepted".equalsIgnoreCase(status)) {
                            sosDataList.add(sosData);
                        }

                        keys.put(requestId, sosData);
                    } else {
                        Log.w("RequestActivity", "Skipping entry due to missing data: " + dataSnapshot.getKey());
                    }
                }

                sosAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Request.this, "Failed to load SOS", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
