package com.eles.driver;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class Profile extends AppCompatActivity {

    private EditText vehicleEditText;
    private EditText nameEditText;
    private EditText ageEditText;
    private EditText phoneEditText;
    private ImageView profileImageView;
    private Uri selectedImageUri;
    private Button saveProfileButton;
    private Button viewProfileButton;

    private FirebaseAuth mAuth;
    private DatabaseReference driversRef;
    private StorageReference profileImagesRef;
    private String driverId = "4537D";//static

    private static final int REQUEST_STORAGE_PERMISSION = 101;
    private ActivityResultLauncher<Intent> galleryActivityResultLauncher;

    private static final String TAG = "Profile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        vehicleEditText = findViewById(R.id.editTextVehicle);
        nameEditText = findViewById(R.id.editTextName);
        ageEditText = findViewById(R.id.editTextAge);
        phoneEditText = findViewById(R.id.editTextPhone);
        profileImageView = findViewById(R.id.imageViewProfile);
        saveProfileButton = findViewById(R.id.buttonSaveProfile);
        viewProfileButton = findViewById(R.id.buttonViewProfile);

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        driversRef = database.getReference("drivers");
        FirebaseStorage storage = FirebaseStorage.getInstance();
        profileImagesRef = storage.getReference().child("profile_images");

        loadExistingProfile();


        galleryActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        profileImageView.setImageURI(selectedImageUri);
                    }
                }
        );

        profileImageView.setOnClickListener(v -> checkStoragePermission());

        saveProfileButton.setOnClickListener(v -> saveDriverProfile());
        viewProfileButton.setOnClickListener(v -> startActivity(new Intent(Profile.this, ViewDriverProfileActivity.class)));
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        } else {
            openGallery();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Storage permission is required to select a profile picture", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private void loadExistingProfile() {
        driversRef.child(driverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    Integer age = snapshot.child("age").getValue(Integer.class);
                    String phoneNumber = snapshot.child("phoneNumber").getValue(String.class);
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                    String vehicleNumber = snapshot.child("vehicle_number").getValue(String.class);
                    nameEditText.setText(name);
                    if (age != null) {
                        ageEditText.setText(String.valueOf(age));
                    }
                    phoneEditText.setText(phoneNumber);
                    vehicleEditText.setText(vehicleNumber);
                    if (profileImageUrl != null) {
                        Glide.with(Profile.this)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.profile_image)
                                .into(profileImageView);
                    } else {
                        profileImageView.setImageResource(R.drawable.profile_image);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load profile: " + error.getMessage());
                Toast.makeText(Profile.this, "Failed to load existing profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveDriverProfile() {
        String name = nameEditText.getText().toString().trim();
        String ageStr = ageEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String vehicleNumber = vehicleEditText.getText().toString().trim();

        if (name.isEmpty() || ageStr.isEmpty() || phone.isEmpty() || vehicleNumber.isEmpty()) {
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid age format", Toast.LENGTH_SHORT).show();
            return;
        }


        if (selectedImageUri != null) {
            StorageReference imageRef = profileImagesRef.child(driverId + ".jpg");
            UploadTask uploadTask = imageRef.putFile(selectedImageUri);

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveProfileData(driverId, name, age, phone, vehicleNumber, uri.toString());
                });
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error uploading image: " + e.getMessage());
                Toast.makeText(this, "Failed to upload profile image", Toast.LENGTH_SHORT).show();
                saveProfileData(driverId, name, age, phone, vehicleNumber, null);
            });
        } else {
            saveProfileData(driverId, name, age, phone, vehicleNumber, null);
        }
    }

    private void saveProfileData(String driverId, String name, int age, String phone, String vehicleNumber, String profileImageUrl) {
        Map<String, Object> driverProfile = new HashMap<>();
        driverProfile.put("name", name);
        driverProfile.put("age", age);
        driverProfile.put("phoneNumber", phone);
        driverProfile.put("vehicle_number", vehicleNumber);
        if (profileImageUrl != null) {
            driverProfile.put("profileImageUrl", profileImageUrl);
        }


        driversRef.child(driverId).setValue(driverProfile)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(Profile.this, "Profile saved successfully", Toast.LENGTH_SHORT).show();
                    loadExistingProfile();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving profile: " + e.getMessage());
                    Toast.makeText(Profile.this, "Failed to save profile", Toast.LENGTH_SHORT).show();
                });
    }
}