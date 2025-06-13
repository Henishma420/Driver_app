package com.eles.driver;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ViewDriverProfileActivity extends AppCompatActivity {

    private DatabaseReference driversRef;
    private String driverId;
    private static final String TAG = "ViewDriverProfile";

    private TextView nameTextView, ageTextView, phoneTextView, vehicleTextView;
    private ImageView profileImageView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_driver_profile);

        nameTextView = findViewById(R.id.textViewName);
        ageTextView = findViewById(R.id.textViewAge);
        phoneTextView = findViewById(R.id.textViewPhone);
        vehicleTextView = findViewById(R.id.textViewVehicle);
        profileImageView = findViewById(R.id.imageViewProfile);

        driverId = getIntent().getStringExtra("driverId");
        if (driverId == null || driverId.isEmpty()) {
            driverId = "4537D";
        }

        driversRef = FirebaseDatabase.getInstance().getReference("drivers");
        loadDriverProfile();
    }

    private void loadDriverProfile() {
        driversRef.child(driverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    Integer age = snapshot.child("age").getValue(Integer.class);
                    String phone = snapshot.child("phoneNumber").getValue(String.class);
                    String vehicle = snapshot.child("vehicle_number").getValue(String.class);
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);

                    nameTextView.setText("Name: " + name);
                    ageTextView.setText("Age: " + (age != null ? age : ""));
                    phoneTextView.setText("Phone: " + phone);
                    vehicleTextView.setText("Vehicle: " + vehicle);

                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(ViewDriverProfileActivity.this)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.profile_image)
                                .into(profileImageView);
                    } else {
                        profileImageView.setImageResource(R.drawable.profile_image);
                    }
                } else {
                    Toast.makeText(ViewDriverProfileActivity.this, "Profile not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load profile: " + error.getMessage());
                Toast.makeText(ViewDriverProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
